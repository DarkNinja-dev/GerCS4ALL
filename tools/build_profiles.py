#!/usr/bin/env python3
"""Generate the independent CloudStream profile manifests from curated lists.

The files under ``profiles/`` are separate CloudStream entry points, not
separate Git repositories.  Re-run this after changing one of ``lists/*.json``.
"""
from __future__ import annotations

import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
RAW_BASE = "https://raw.githubusercontent.com/DevMonkeyOps/GerCS4ALL/main"
ICON_URL = f"{RAW_BASE}/icons/repo.svg"
PROFILE_LISTS = ROOT / "lists" / "profiles"
PROFILE_REPOS = ROOT / "profiles"
MOVIE_OR_SERIES = {"Movie", "TvSeries"}

# These entries were audited as sport sources and are deliberately kept out of
# the non-Asian sport profile.  QuickIPTV explicitly bundles Japanese IPTV;
# Streamed points to streamed.pk.  Both reappear only in profile 09 if they
# satisfy its DE/EN language condition.
SPORT_NON_ASIA = {
    "BasketballReplays",
    "CricHD",
    "DamiTVProvider",
    "DeporTV",
    "Filemoon",
    "Footballia",
    "FullRaces",
    "LIVETVProvider",
    "ReplayZone",
    "StreamEast",
    "Time4tv",
    "TimStreams",
    "WatchWrestling",
}

# PublicSportsIPTV was intentionally parked in the general catalogue because
# it is an Asian sport source.  It is English-language and belongs only in the
# explicit profile that permits Asian DE/EN sport sources.
ASIAN_SPORT_DE_EN = {
    "PublicSportsIPTV": {
        "status": 1,
        "version": 5,
        "name": "PublicSportsIPTV",
        "internalName": "PublicSportsIPTV",
        "authors": ["Phisher98"],
        "repositoryUrl": "https://github.com/phisher98/cloudstream-extensions-phisher",
        "url": "https://raw.githubusercontent.com/phisher98/cloudstream-extensions-phisher/builds/PublicSportsIPTV.cs3",
        "fileSize": 0,
        "language": "en",
        "iconUrl": "https://www.thestatesman.com/wp-content/uploads/2021/05/fancode.jpg",
        "apiVersion": 1,
        "description": "Sports Live Streams (FanCode) — asiatische DE/EN-Sportauswahl",
        "tvTypes": ["Live"],
    },
}


def read_list(name: str) -> list[dict]:
    with (ROOT / "lists" / f"{name}.json").open(encoding="utf-8") as handle:
        return json.load(handle)


def sorted_unique(entries: list[dict]) -> list[dict]:
    """Deduplicate by CloudStream's stable plugin identity."""
    unique: dict[str, dict] = {}
    for entry in entries:
        unique.setdefault(entry["internalName"], entry)
    return sorted(unique.values(), key=lambda entry: entry["name"].casefold())


def movies_and_series(entries: list[dict], language: str) -> list[dict]:
    return sorted_unique([
        entry for entry in entries
        if entry.get("language") == language and MOVIE_OR_SERIES.intersection(entry.get("tvTypes", []))
    ])


def only_language(entries: list[dict], language: str) -> list[dict]:
    return sorted_unique([entry for entry in entries if entry.get("language") == language])


def write_json(path: Path, value: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def list_url(name: str) -> str:
    return f"{RAW_BASE}/lists/profiles/{name}.json"


def write_profile(slug: str, name: str, description: str, lists: list[str]) -> None:
    write_json(PROFILE_REPOS / slug / "repo.json", {
        "name": name,
        "description": description,
        "manifestVersion": 1,
        "iconUrl": ICON_URL,
        "pluginLists": [list_url(item) for item in lists],
    })


def main() -> None:
    anime = read_list("anime")
    german = read_list("ger")
    english = read_list("eng")

    anime_de = only_language(anime, "de")
    anime_en = only_language(anime, "en")
    movies_series_de = movies_and_series(german, "de")
    movies_series_en = movies_and_series(english, "en")

    all_known = {
        entry["internalName"]: entry
        for entry in anime + german + english
    }
    sport_non_asia = sorted_unique([
        all_known[name] for name in SPORT_NON_ASIA if name in all_known
    ])
    # The current catalogue's Streamed entry is an English source at
    # streamed.pk.  It is included in the explicit Asia-enabled profile only.
    sport_asia_de_en = sorted_unique([
        all_known["Streamed"],
        *ASIAN_SPORT_DE_EN.values(),
    ])

    generated_lists = {
        "anime-de": anime_de,
        "anime-en": anime_en,
        "anime-de-en": sorted_unique(anime_de + anime_en),
        "movies-series-de": movies_series_de,
        "movies-series-en": movies_series_en,
        "movies-series-de-en": sorted_unique(movies_series_de + movies_series_en),
        "sport-multilingual-no-asia": sport_non_asia,
        "sport-multilingual-plus-asia-de-en": sorted_unique(sport_non_asia + sport_asia_de_en),
    }
    for name, entries in generated_lists.items():
        write_json(PROFILE_LISTS / f"{name}.json", entries)

    write_profile(
        "02-de-sport-anime-de",
        "GerCS4ALL – DE: Filme/Serien + Sport + Anime DE",
        "Deutschsprachige Filme/Serien, mehrsprachiger Sport ohne Asien und deutsche Anime-Quellen.",
        ["movies-series-de", "sport-multilingual-no-asia", "anime-de"],
    )
    write_profile(
        "03-en-sport-anime-en",
        "GerCS4ALL – EN: Movies/Series + Sport + Anime EN",
        "Englische Filme/Serien, mehrsprachiger Sport ohne Asien und englische Anime-Quellen.",
        ["movies-series-en", "sport-multilingual-no-asia", "anime-en"],
    )
    write_profile(
        "04-de-sport-anime-de-en",
        "GerCS4ALL – DE: Filme/Serien + Sport + Anime DE/EN",
        "Deutschsprachige Filme/Serien, mehrsprachiger Sport ohne Asien sowie deutsche und englische Anime-Quellen.",
        ["movies-series-de", "sport-multilingual-no-asia", "anime-de-en"],
    )
    write_profile(
        "05-anime-de-en",
        "GerCS4ALL – Anime DE/EN",
        "Nur deutsche und englische Anime-Erweiterungen.",
        ["anime-de-en"],
    )
    write_profile(
        "06-sport-multilingual-no-asia",
        "GerCS4ALL – Sport multilingual ohne Asien",
        "Kuratiertes Sportangebot in mehreren Sprachen; Quellen mit eindeutig asiatischem Bezug sind ausgeschlossen.",
        ["sport-multilingual-no-asia"],
    )
    write_profile(
        "07-movies-series-de",
        "GerCS4ALL – Filme & Serien DE",
        "Nur Erweiterungen mit deutscher Sprachkennzeichnung für Filme und Serien.",
        ["movies-series-de"],
    )
    write_profile(
        "08-movies-series-de-en",
        "GerCS4ALL – Filme & Serien DE/EN",
        "Deutsche und englische Erweiterungen für Filme und Serien.",
        ["movies-series-de-en"],
    )
    write_profile(
        "09-sport-multilingual-plus-asia-de-en",
        "GerCS4ALL – Sport multilingual + Asien DE/EN",
        "Mehrsprachiger Sport ohne Asien sowie gezielt deutsche/englische Sportquellen mit asiatischem Bezug.",
        ["sport-multilingual-plus-asia-de-en"],
    )
    write_profile(
        "10-de-en-sport-anime-de-en",
        "GerCS4ALL – DE/EN: Filme/Serien + Sport + Anime DE/EN",
        "Deutsche und englische Filme/Serien, mehrsprachiger Sport ohne Asien sowie deutsche und englische Anime-Quellen.",
        ["movies-series-de-en", "sport-multilingual-no-asia", "anime-de-en"],
    )

    counts = {name: len(entries) for name, entries in generated_lists.items()}
    print(json.dumps(counts, ensure_ascii=False, sort_keys=True))


if __name__ == "__main__":
    main()
