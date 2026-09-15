# DE/EN Quellen – kuratiertes CloudStream-Verzeichnis

Ein GitHub-Repository mit mehreren unabhängig einbindbaren CloudStream-Profilen für DE, EN, Anime, Sport und NSFW. Defekte Quellen und identische Installer-Duplikate werden nicht als separate Erweiterungen veröffentlicht.

## Inhaltsverzeichnis

- [Einbindung](#einbindung)
- [Profile](#profile)
- [Lokale, gepflegte Erweiterungen](#lokale-gepflegte-erweiterungen)
- [Wo erscheinen die Quellen?](#wo-erscheinen-die-quellen)
- [Sprach- und Sportfilter](#sprach--und-sportfilter)
- [Pflege](#pflege)

## Einbindung

Die vollständige Quelle (Profil 1, einschließlich Anime, Hentai/NSFW, DE und EN):

`https://raw.githubusercontent.com/DarkNinja-dev/GerCS4ALL/main/repo.json`

Für ein gezieltes Profil die passende Adresse aus der Tabelle in CloudStream einfügen. Alle Profile liegen im selben GitHub-Repository und können parallel eingebunden werden.

## Profile

| Nr. | Inhalt | CloudStream-URL |
| --- | --- | --- |
| 1 | Komplett: DE, EN, Anime und Hentai/NSFW | `https://raw.githubusercontent.com/DarkNinja-dev/GerCS4ALL/main/repo.json` |
| 2 | Filme/Serien DE + Sport multilingual ohne Asien + Anime DE | `https://raw.githubusercontent.com/DarkNinja-dev/GerCS4ALL/main/profiles/02-de-sport-anime-de/repo.json` |
| 3 | Filme/Serien EN + Sport multilingual ohne Asien + Anime EN | `https://raw.githubusercontent.com/DarkNinja-dev/GerCS4ALL/main/profiles/03-en-sport-anime-en/repo.json` |
| 4 | Filme/Serien DE + Sport multilingual ohne Asien + Anime DE/EN | `https://raw.githubusercontent.com/DarkNinja-dev/GerCS4ALL/main/profiles/04-de-sport-anime-de-en/repo.json` |
| 5 | Nur Anime DE/EN | `https://raw.githubusercontent.com/DarkNinja-dev/GerCS4ALL/main/profiles/05-anime-de-en/repo.json` |
| 6 | Nur Sport multilingual ohne Asien | `https://raw.githubusercontent.com/DarkNinja-dev/GerCS4ALL/main/profiles/06-sport-multilingual-no-asia/repo.json` |
| 7 | Nur Filme & Serien DE | `https://raw.githubusercontent.com/DarkNinja-dev/GerCS4ALL/main/profiles/07-movies-series-de/repo.json` |
| 8 | Nur Filme & Serien DE/EN | `https://raw.githubusercontent.com/DarkNinja-dev/GerCS4ALL/main/profiles/08-movies-series-de-en/repo.json` |
| 9 | Sport multilingual ohne Asien + asiatische Sportquellen DE/EN | `https://raw.githubusercontent.com/DarkNinja-dev/GerCS4ALL/main/profiles/09-sport-multilingual-plus-asia-de-en/repo.json` |
| 10 | Filme/Serien DE/EN + Sport multilingual ohne Asien + Anime DE/EN | `https://raw.githubusercontent.com/DarkNinja-dev/GerCS4ALL/main/profiles/10-de-en-sport-anime-de-en/repo.json` |
| Zusatz | Nur Hentai/NSFW | `https://raw.githubusercontent.com/DarkNinja-dev/GerCS4ALL/main/single-repos/hentai/repo.json` |

Die Einzelkategorien bleiben außerdem unter `single-repos/<kategorie>/repo.json` erreichbar.

## Lokale, gepflegte Erweiterungen

`AnimeToast` registriert ausschließlich AnimeToast als `Anime`, `AnimeMovie` und `OVA`. Es erscheint daher in Profil 2 sowie in den Anime-DE/DE-EN-Profilen, nie in Film- oder Serienfiltern.

`KayoAnime` registriert KayoAnime mit derselben reinen Anime-Kategorie für die englischen Anime-Profile. Normale Google-Drive-Dateien werden direkt aufgelöst; Drive-Ordner werden bewusst nicht als angebliche Streams angezeigt.

`InternetArchiveMoviesDE` durchsucht den öffentlichen Internet-Archive-Katalog für deutsch markierte Filme und stellt bis zu 60 Ergebnisse pro Seite bereit. `InternetArchiveAnimeDE` ist davon getrennt und führt die Archivsuche `anime deutsch` aus – Anime kann dadurch nicht bei Filmen oder Serien auftauchen.

`SerienStream` registriert nur Serien. In den Plugin-Einstellungen stehen `serienstream.to`, `serienstream.cx` und die HTTP-Direkt-IP `186.2.175.5` zur Auswahl.

`KinoKiste`, `KKiste`, `Movie4k`, `Streamcloud` und `XcineRU` sind jeweils getrennte Erweiterungen mit ihrer jeweiligen Adresse. Jede liefert bis zu 60 Treffer sowie Trends, Neuheiten, meistgesehene und bestbewertete Titel, Bewertungen, Marvel/MCU und Genrelisten.

`DMAX`, `Tele5` und `TLC` sind getrennte Erweiterungen für die jeweiligen offiziellen Katalogschnittstellen. Coverbilder verwenden einen Fallback auf die Metadatenbilder.

`Einschalten` ersetzt die doppelten alten Einträge. Die Quelle heißt im Client nur noch **Einschalten**.

Die reine DE-Film-/Serienliste umfasst derzeit 22 sichtbare Quellen: ARD, DMAX, Einschalten, FilmFrei24, Filmo, FilmPalast, InternetArchiveMoviesDE, KellerKino, KinoKing, KinoKiste, KKiste, Megakino, Moflix, Movie4k, Netzkino, PlutoTV, SerienStream, Southpark, Streamcloud, Tele5, TLC und XcineRU.

## Wo erscheinen die Quellen?

Ein Eintrag in der Erweiterungsverwaltung ist ein Plugin-Paket; die eigentlichen Quellen erscheinen nach dessen Aktivierung in CloudStream.

| Sichtbare Quelle in CloudStream | Installiertes Paket |
| --- | --- |
| AnimeToast | `AnimeToast` |
| KayoAnime | `KayoAnime` |
| Internet Archive – Filme DE | `InternetArchiveMoviesDE` |
| Internet Archive – Anime DE | `InternetArchiveAnimeDE` |
| Aniworld | bestehendes Paket `Aniworld` in der Anime-DE-Liste |
| DMAX, TELE 5, TLC | getrennt: `DMAX`, `Tele5`, `TLC` |
| KinoKiste, KKiste, Movie4k, Streamcloud, Xcine | jeweils getrennte Erweiterung |
| Einschalten | `Einschalten` |
| Haho.moe, Hanime | NSFW-Pakete `Haho moe` und `Hanime` – nur Profil 1 bzw. die NSFW-Einzelquelle |

AnimeCloud, Flixi, HDFilme, Huhu, Kinoger, TopStreamFilm und Xcine.top sind derzeit nicht veröffentlicht, weil die geprüften Schnittstellen leer, nicht erreichbar oder strukturell defekt waren. Sie werden nicht durch Attrappen mit leeren Kategorien ersetzt. Anime-Loads liefert beim Prüfen nur eine Cloudflare-Sperre; es kommt erst mit einer verifizierten CloudStream-Implementierung hinein.

## Sprach- und Sportfilter

Die Profile filtern Erweiterungen nach ihrer geprüften CloudStream-Sprachkennzeichnung. CloudStream kann die Sprache einzelner Titel innerhalb einer mehrsprachigen Erweiterung nicht global erzwingen; bietet ein Anbieter selbst DE und EN, können beide im Katalog sichtbar sein.

Die Sportprofile ohne Asien schließen Quellen mit eindeutig asiatischem Bezug aus. Profil 9 nimmt gezielt nur geprüfte asiatische Sportquellen mit deutscher oder englischer Kennzeichnung wieder auf.

## Pflege

Lokale Quellen liegen unter `extensions/DEENQuellenSources/`. Der Build erzeugt echte `.cs3`-Archive mit `manifest.json`, `pluginClassName` und `classes.dex`:

```sh
cd extensions/DEENQuellenSources
./gradlew -Dorg.gradle.java.home=/usr/lib/jvm/java-21-openjdk-amd64 make makePluginsJson
```

Nach Änderungen an Basislisten die Profile neu erzeugen:

```sh
python3 tools/build_profiles.py
```

Falls alte, bereits installierte Fremd-Plugins noch doppelt angezeigt werden, diese einmal in CloudStream entfernen und anschließend nur diese Repo-Quelle neu laden. Ein Manifest kann eine zuvor lokal installierte, inzwischen entfernte Erweiterung nicht selbst deinstallieren.
