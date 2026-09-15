#!/usr/bin/env python3
"""Publish selected locally built CloudStream plugins into this repository.

The CloudStream Gradle plugin writes each archive below
``extensions/QuellenSources/<module>/build/``.  This repository serves
the archives from ``plugins/`` instead, so copying the archive alone is not
enough: the version, size and SHA-256 in the corresponding plugin list must
move with it.  Run this after ``./gradlew make makePluginsJson`` and before
``tools/build_profiles.py``.
"""
from __future__ import annotations

import hashlib
import json
import shutil
import sys
import zipfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SOURCE_ROOT = ROOT / "extensions" / "QuellenSources"
LISTS = (ROOT / "lists" / "ger.json", ROOT / "lists" / "anime.json")

# Intentionally small: these are the maintained packages released from this
# repository.  Third-party list entries are never rewritten here.
PACKAGES = {
    "DMAX": "ger",
    "Tele5": "ger",
    "TLC": "ger",
    "InternetArchiveMoviesDE": "ger",
    "Einschalten": "ger",
    "InternetArchiveAnimeDE": "anime",
}


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def plugin_version(path: Path) -> int:
    with zipfile.ZipFile(path) as archive:
        manifest = json.loads(archive.read("manifest.json"))
    version = manifest.get("version")
    if not isinstance(version, int):
        raise ValueError(f"{path}: manifest.json has no numeric version")
    return version


def update_list(path: Path, metadata: dict[str, dict[str, object]]) -> set[str]:
    entries = json.loads(path.read_text(encoding="utf-8"))
    updated: set[str] = set()
    for entry in entries:
        internal_name = entry.get("internalName")
        package = metadata.get(internal_name)
        if package is None:
            continue
        entry["version"] = package["version"]
        entry["fileSize"] = package["fileSize"]
        entry["fileHash"] = package["fileHash"]
        updated.add(internal_name)
    path.write_text(json.dumps(entries, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    return updated


def main() -> int:
    metadata: dict[str, dict[str, object]] = {}
    for module, category in PACKAGES.items():
        source = SOURCE_ROOT / module / "build" / f"{module}.cs3"
        destination = ROOT / "plugins" / category / f"{module}.cs3"
        if not source.is_file():
            print(f"Missing build output: {source}", file=sys.stderr)
            return 1
        destination.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(source, destination)
        metadata[module] = {
            "version": plugin_version(destination),
            "fileSize": destination.stat().st_size,
            "fileHash": f"sha256-{sha256(destination)}",
        }
        print(f"Copied {source.relative_to(ROOT)} -> {destination.relative_to(ROOT)}")

    updated: set[str] = set()
    for list_path in LISTS:
        updated.update(update_list(list_path, metadata))
    missing = set(metadata).difference(updated)
    if missing:
        print(f"No list entry for: {', '.join(sorted(missing))}", file=sys.stderr)
        return 1
    print(f"Updated metadata for {', '.join(sorted(updated))}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
