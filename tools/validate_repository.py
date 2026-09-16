#!/usr/bin/env python3
"""Validate local CloudStream repository references and package metadata.

The base lists are authoritative.  The lists below ``lists/profiles`` are
generated copies and are checked too, but they never authorize an archive on
their own.  Run this after syncing packages and regenerating profiles.
"""
from __future__ import annotations

import hashlib
import json
import sys
import zipfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
RAW_BASE = "https://raw.githubusercontent.com/DarkNinja-dev/GerCS4ALL/main/"
BASE_LISTS = tuple(sorted((ROOT / "lists").glob("*.json")))
ALL_LISTS = BASE_LISTS + tuple(sorted((ROOT / "lists" / "profiles").glob("*.json")))
REPOSITORIES = (ROOT / "repo.json", *sorted((ROOT / "profiles").glob("*/repo.json")), *sorted((ROOT / "single-repos").glob("*/repo.json")))


def read_json(path: Path) -> object:
    with path.open(encoding="utf-8") as handle:
        return json.load(handle)


def local_path(url: object) -> Path | None:
    if not isinstance(url, str) or not url.startswith(RAW_BASE):
        return None
    return ROOT / url.removeprefix(RAW_BASE)


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def validate_plugin_list(path: Path, referenced_archives: set[Path]) -> list[str]:
    errors: list[str] = []
    entries = read_json(path)
    if not isinstance(entries, list):
        return [f"{path.relative_to(ROOT)}: list root must be an array"]

    seen: set[str] = set()
    for index, entry in enumerate(entries):
        if not isinstance(entry, dict):
            errors.append(f"{path.relative_to(ROOT)}[{index}]: entry must be an object")
            continue
        internal_name = entry.get("internalName")
        if not isinstance(internal_name, str) or not internal_name:
            errors.append(f"{path.relative_to(ROOT)}[{index}]: missing internalName")
            continue
        if internal_name in seen:
            errors.append(f"{path.relative_to(ROOT)}: duplicate internalName {internal_name}")
        seen.add(internal_name)

        archive = local_path(entry.get("url"))
        if archive is None or not archive.as_posix().startswith((ROOT / "plugins").as_posix()):
            continue
        if not archive.is_file():
            errors.append(f"{path.relative_to(ROOT)}: {internal_name} points to missing {archive.relative_to(ROOT)}")
            continue
        referenced_archives.add(archive)
        if entry.get("fileSize") != archive.stat().st_size:
            errors.append(f"{path.relative_to(ROOT)}: stale size for {internal_name}")
        expected_hash = f"sha256-{sha256(archive)}"
        if entry.get("fileHash") != expected_hash:
            errors.append(f"{path.relative_to(ROOT)}: stale hash for {internal_name}")
        try:
            with zipfile.ZipFile(archive) as package:
                manifest = json.loads(package.read("manifest.json"))
        except (KeyError, OSError, ValueError, zipfile.BadZipFile) as exc:
            errors.append(f"{archive.relative_to(ROOT)}: invalid cs3 package ({exc})")
            continue
        if manifest.get("name") != internal_name:
            errors.append(f"{archive.relative_to(ROOT)}: manifest name does not match {internal_name}")
    return errors


def validate_repository(path: Path) -> list[str]:
    errors: list[str] = []
    data = read_json(path)
    if not isinstance(data, dict) or not isinstance(data.get("pluginLists"), list):
        return [f"{path.relative_to(ROOT)}: missing pluginLists array"]
    for url in data["pluginLists"]:
        list_path = local_path(url)
        if list_path is None or not list_path.is_file():
            errors.append(f"{path.relative_to(ROOT)}: missing local plugin list {url}")
    return errors


def main() -> int:
    errors: list[str] = []
    referenced_archives: set[Path] = set()
    for path in ALL_LISTS:
        errors.extend(validate_plugin_list(path, referenced_archives))
    for path in REPOSITORIES:
        errors.extend(validate_repository(path))

    base_referenced_archives: set[Path] = set()
    for path in BASE_LISTS:
        validate_plugin_list(path, base_referenced_archives)
    published_archives = set((ROOT / "plugins").glob("**/*.cs3"))
    for archive in sorted(published_archives - base_referenced_archives):
        errors.append(f"unreferenced local package: {archive.relative_to(ROOT)}")

    if errors:
        print("Repository validation failed:", *[f"- {error}" for error in errors], sep="\n", file=sys.stderr)
        return 1
    print(f"Repository validation passed ({len(ALL_LISTS)} lists, {len(published_archives)} local packages).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
