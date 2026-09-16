#!/usr/bin/env python3
"""Generate independent CloudStream modules for the requested catalogue URLs.

Each entry is intentionally a separate extension.  Do not consolidate entries
based on a matching response body, a similar name, a shared CDN, or an IP.
"""
from __future__ import annotations

import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SOURCE_ROOT = ROOT / "extensions" / "QuellenSources"
SETTINGS = SOURCE_ROOT / "settings.gradle.kts"
GER_LIST = ROOT / "lists" / "ger.json"
RAW_BASE = "https://raw.githubusercontent.com/DarkNinja-dev/GerCS4ALL/main"
REPOSITORY_URL = "https://github.com/DarkNinja-dev/GerCS4ALL"

# One package per address by explicit user decision.  Megakino keeps version 7
# to update the formerly published package with the same internal name.
MODULES: tuple[tuple[str, str, int], ...] = (
    ("CineTo", "https://cine.to", 1),
    ("GetMoviez", "https://getmoviez.cc", 1),
    ("HDFilme", "https://hdfilme.to", 1),
    ("HDFilmeCafe", "https://hdfilme.cafe", 1),
    ("HDFilmeHelp", "https://hdfilme-tv.help", 1),
    ("HDFilmeMe", "https://hdfilme.me", 1),
    ("HDFilmeTo", "https://hdfilme.to", 1),
    ("KKisteIO", "https://kkiste-io.ink", 1),
    ("KinoX", "https://w11.kinox.to", 1),
    ("Kinoger", "https://kinoger.com", 1),
    ("KinogerCom", "https://kinoger.com", 1),
    ("KinogerTo", "https://kinoger.to", 1),
    ("KinoxW11", "https://w11.kinox.to", 1),
    ("KinoxW21", "https://www21.kinox.to", 1),
    ("Kinoz", "https://kinoz.to", 1),
    ("Megakino", "https://megakino.org", 7),
    ("Megakino19", "https://megakino19.com", 1),
    ("Megakino7", "https://7megakino.lol", 1),
    ("MegakinoFoo", "https://megakino.foo", 1),
    ("MegakinoMe", "https://megakino.me", 1),
    ("MegakinoOrg", "https://megakino.org", 1),
    ("Movie2k", "https://movie2k.cx", 1),
    ("Movie2kAg", "https://movie2k.ag", 1),
    ("Movie2kCx", "https://movie2k.cx", 1),
    ("Streamkiste", "https://streamkiste.sx", 1),
    ("StreamkisteBid", "https://streamkiste.bid", 1),
)


def domain(url: str) -> str:
    return url.removeprefix("https://").removeprefix("http://")


def write_module(name: str, url: str, version: int) -> None:
    module = SOURCE_ROOT / name
    source = module / "src/main/kotlin/com/devmonkeyops/quellen" / f"{name}Plugin.kt"
    source.parent.mkdir(parents=True, exist_ok=True)
    (module / "build.gradle.kts").write_text(
        f'''version = {version}

cloudstream {{
    language = "de"
    description = "{name} mit eigener Quelle."
    authors = listOf("DarkNinja-dev")
    status = 1
    tvTypes = listOf("Movie", "TvSeries")
    iconUrl = "https://www.google.com/s2/favicons?domain={domain(url)}&sz=%size%"
}}

android {{
    sourceSets.getByName("main").java.srcDir("../KinoCatalogShared/src/main/kotlin")
}}
''',
        encoding="utf-8",
    )
    source.write_text(
        f'''package com.darkninja.quellen

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class {name}Plugin : BasePlugin() {{
    override fun load() {{
        registerMainAPI(KinoCatalogProvider("{name}", "{url}"))
    }}
}}
''',
        encoding="utf-8",
    )


def update_settings() -> None:
    lines = SETTINGS.read_text(encoding="utf-8").splitlines()
    existing = {line.strip() for line in lines}
    for name, _, _ in MODULES:
        line = f'include(":{name}")'
        if line not in existing:
            lines.append(line)
    SETTINGS.write_text("\n".join(lines) + "\n", encoding="utf-8")


def update_list() -> None:
    entries = json.loads(GER_LIST.read_text(encoding="utf-8"))
    by_name = {entry["internalName"]: entry for entry in entries}
    for name, url, version in MODULES:
        entry = by_name.get(name, {})
        entry.update({
            "status": 1,
            "version": version,
            "name": name,
            "internalName": name,
            "authors": ["DarkNinja-dev"],
            "repositoryUrl": REPOSITORY_URL,
            "url": f"{RAW_BASE}/plugins/ger/{name}.cs3",
            "fileSize": 0,
            "fileHash": "",
            "language": "de",
            "iconUrl": f"https://www.google.com/s2/favicons?domain={domain(url)}&sz=%size%",
            "apiVersion": 1,
            "description": f"{name} mit eigener Quelle.",
            "tvTypes": ["Movie", "TvSeries"],
        })
        by_name[name] = entry
    GER_LIST.write_text(
        json.dumps(sorted(by_name.values(), key=lambda entry: entry["name"].casefold()), ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )


def main() -> None:
    for module in MODULES:
        write_module(*module)
    update_settings()
    update_list()
    print(f"Generated {len(MODULES)} independent catalogue modules.")


if __name__ == "__main__":
    main()
