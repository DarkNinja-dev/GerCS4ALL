# GerCS4ALL – kuratierte CloudStream-Quellen

Ein GitHub-Repository, mehrere unabhängig einbindbare CloudStream-Profile für DE, EN, Anime, Sport und NSFW.

## Inhaltsverzeichnis

- [Einbindung](#einbindung)
- [Profile](#profile)
- [Sprach- und Sportfilter](#sprach--und-sportfilter)
- [Pflege](#pflege)

## Einbindung

Die vollständige Quelle (Profil 1, einschließlich der bisherigen Anime-, Hentai-, DE- und EN-Listen):

`https://raw.githubusercontent.com/DevMonkeyOps/GerCS4ALL/main/repo.json`

Für ein gezieltes Profil die jeweilige Adresse aus der folgenden Tabelle in CloudStream einfügen. Alle Profile liegen im selben GitHub-Repository und können parallel hinzugefügt werden.

## Profile

| Nr. | Inhalt | CloudStream-URL |
| --- | --- | --- |
| 1 | Komplett wie bisher: DE, EN, Anime und Hentai/NSFW | `https://raw.githubusercontent.com/DevMonkeyOps/GerCS4ALL/main/repo.json` |
| 2 | Filme/Serien DE + Sport multilingual ohne Asien + Anime DE | `https://raw.githubusercontent.com/DevMonkeyOps/GerCS4ALL/main/profiles/02-de-sport-anime-de/repo.json` |
| 3 | Filme/Serien EN + Sport multilingual ohne Asien + Anime EN | `https://raw.githubusercontent.com/DevMonkeyOps/GerCS4ALL/main/profiles/03-en-sport-anime-en/repo.json` |
| 4 | Filme/Serien DE + Sport multilingual ohne Asien + Anime DE/EN | `https://raw.githubusercontent.com/DevMonkeyOps/GerCS4ALL/main/profiles/04-de-sport-anime-de-en/repo.json` |
| 5 | Nur Anime DE/EN | `https://raw.githubusercontent.com/DevMonkeyOps/GerCS4ALL/main/profiles/05-anime-de-en/repo.json` |
| 6 | Nur Sport multilingual ohne Asien | `https://raw.githubusercontent.com/DevMonkeyOps/GerCS4ALL/main/profiles/06-sport-multilingual-no-asia/repo.json` |
| 7 | Nur Filme & Serien DE | `https://raw.githubusercontent.com/DevMonkeyOps/GerCS4ALL/main/profiles/07-movies-series-de/repo.json` |
| 8 | Nur Filme & Serien DE/EN | `https://raw.githubusercontent.com/DevMonkeyOps/GerCS4ALL/main/profiles/08-movies-series-de-en/repo.json` |
| 9 | Sport multilingual ohne Asien + asiatische Sportquellen DE/EN | `https://raw.githubusercontent.com/DevMonkeyOps/GerCS4ALL/main/profiles/09-sport-multilingual-plus-asia-de-en/repo.json` |
| 10 | Filme/Serien DE/EN + Sport multilingual ohne Asien + Anime DE/EN | `https://raw.githubusercontent.com/DevMonkeyOps/GerCS4ALL/main/profiles/10-de-en-sport-anime-de-en/repo.json` |
| Zusatz | Nur Hentai/NSFW | `https://raw.githubusercontent.com/DevMonkeyOps/GerCS4ALL/main/single-repos/hentai/repo.json` |

Die bereits vorhandenen Einzelkategorien bleiben zusätzlich unter `single-repos/<kategorie>/repo.json` erreichbar, etwa für Anime oder Hentai/NSFW.

## Sprach- und Sportfilter

Die Profile filtern Erweiterungen nach ihrer geprüften CloudStream-Sprachkennzeichnung. CloudStream kann aber die Sprache einzelner Titel innerhalb einer mehrsprachigen Erweiterung nicht global erzwingen; falls ein Anbieter selbst DE und EN anbietet, kann er beide im Katalog darstellen.

Die Sportprofile ohne Asien schließen Quellen mit eindeutig asiatischem Bezug sowie japanische IPTV-Bündel aus. Profil 9 nimmt gezielt nur die geprüften asiatischen Sportquellen mit deutscher oder englischer Kennzeichnung wieder auf.

## Pflege

Die Plugin-URLs zeigen grundsätzlich auf verifizierte Upstream-Repositories. `GerCS4ALLAnime` wird dagegen lokal gebaut, liegt unter `plugins/anime/` und registriert KayoAnime sowie AnimeToast. Seine Kategorien sind ausschließlich `Anime`, `AnimeMovie` und `OVA`.

`GerCS4ALLSerienStream` liegt unter `plugins/ger/`, wird ausschließlich als `TvSeries` eingeordnet und ersetzt die frühere Serienstream-Upstream-Erweiterung. Über die Plugin-Einstellungen kann man eine der aktuell von SerienStream veröffentlichten Adressen auswählen: `serienstream.to`, `serienstream.cx` oder die HTTP-Direkt-IP `186.2.175.5`.

Nach Änderungen an den Basislisten die Profil-Dateien neu erzeugen:

```sh
python3 tools/build_profiles.py
```
