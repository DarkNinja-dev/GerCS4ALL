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

Die Quelle besteht aus einem gültigen CloudStream-Repository: Jedes `repo.json` verweist per `pluginLists` auf JSON-Listen; jeder lokale Eintrag enthält die URL seines `.cs3`-Pakets sowie Version, Größe und SHA-256. Die Basislisten unter `lists/` sind die einzige Quelle der Wahrheit. Alle Listen unter `lists/profiles/` werden daraus mit `tools/build_profiles.py` erzeugt und nie manuell gepflegt.

Ähnliche Namen, Domains oder CDN-IP-Adressen reichen nicht für eine Zusammenlegung. `streamkiste.bid`, `movie2k.cx`, `hdfilme.to`, `hdfilme.cafe`, `hdfilme-tv.help`, `megakino.me`, `megakino.foo`, `megakino19.com` und `7megakino.lol` sind daher jeweils als eigene Quellen veröffentlicht: mit eigener Erweiterungs-ID, eigener Adresse und eigenem Paket. Sie sind keine Alternativadressen einer anderen Erweiterung.

`HDFilme`, `Movie2k`, `Streamkiste` und `Megakino` sind vier getrennte Quellen und daher vier getrennte CloudStream-Pakete. Eine übereinstimmende Katalogantwort ist kein Grund, ihre Identitäten, Update-Kanäle oder Erweiterungseinträge zusammenzuführen.

`KinoKiste`, `KKiste`, `Movie4k`, `Streamcloud` und `XcineRU` sind ebenfalls getrennte Erweiterungen mit eigener Adresse und eigener Implementierung.

`DMAX`, `Tele5` und `TLC` sind getrennte Erweiterungen für die jeweiligen offiziellen Katalogschnittstellen. Coverbilder verwenden einen Fallback auf die Metadatenbilder.

`Einschalten` ersetzt die doppelten alten Einträge. Die Quelle heißt im Client nur noch **Einschalten**.

### Update-Hinweis

Nach einem Update müssen die erzeugten `.cs3`-Pakete **und** die passenden JSON-Listen gemeinsam eingecheckt werden; nur Kotlin-Quellcode aktualisiert keine bereits installierte CloudStream-Erweiterung.

Nach dem Gradle-Build übernimmt `python3 tools/sync_local_plugins.py` das Kopieren aller gepflegten Archive nach `plugins/` sowie Version, Dateigröße und SHA-256 in den Basislisten. Danach `python3 tools/build_profiles.py` und `python3 tools/validate_repository.py` ausführen, damit Profile, Hashes und sämtliche lokalen Referenzen synchron geprüft sind.

`EinschaltenIn` bzw. ein zweiter Eintrag mit derselben Quelle kann nicht durch ein Repository automatisch entfernt werden: Es handelt sich um ein früher separat installiertes Paket. In CloudStream einmal unter **Erweiterungen → Installiert** entfernen und anschließend ausschließlich den Eintrag `Einschalten` aus diesem Repository installieren. Dasselbe Prinzip gilt für alte Doppelinstallationen von FilmPalast, Movie4k, KinoKiste, KinoKing, Streamcloud und Xcine.

Die Internet-Archive-Quellen liegen absichtlich getrennt: `Internet Archive – Filme DE` ist in den Film-/Serien-DE-Profilen, `Internet Archive – Anime DE` in den Anime-DE-Profilen. Der allgemeine Eintrag `Internet Archive` liegt in der EN-Liste und damit im vollständigen sowie in den EN-Profilen. `Aniworld` liegt in der Anime-DE-Liste. Alle erscheinen erst nach Installation des jeweiligen Pakets in der Erweiterungsverwaltung, nicht bereits beim bloßen Hinzufügen des Repository-Links.

## Wo erscheinen die Quellen?

Ein Eintrag in der Erweiterungsverwaltung ist ein Plugin-Paket; die eigentlichen Quellen erscheinen nach dessen Aktivierung in CloudStream.

| Sichtbare Quelle in CloudStream | Installiertes Paket |
| --- | --- |
| AnimeToast | `AnimeToast` |
| KayoAnime | `KayoAnime` |
| Internet Archive – Filme DE | `InternetArchiveMoviesDE` |
| Internet Archive – Anime DE | `InternetArchiveAnimeDE` |
| Aniworld | bestehendes Paket `Aniworld` in der Anime-DE-Liste |
| AnimeCloud | eigenständiges externes Paket in der Anime-DE-Liste |
| DMAX, TELE 5, TLC | getrennt: `DMAX`, `Tele5`, `TLC` |
| HDFilme, Movie2k, Streamkiste, Megakino sowie ihre wiederhergestellten Domainvarianten | jeweils getrennte Erweiterungen mit eigener Adresse und eigener Erweiterungs-ID |
| KinoKiste, KKiste, Movie4k, Streamcloud, Xcine | jeweils getrennte Erweiterung |
| Einschalten | `Einschalten` |
| Haho.moe, Hanime | NSFW-Pakete `Haho moe` und `Hanime` – nur Profil 1 bzw. die NSFW-Einzelquelle |

## Sprach- und Sportfilter

Die Profile filtern Erweiterungen nach ihrer geprüften CloudStream-Sprachkennzeichnung. CloudStream kann die Sprache einzelner Titel innerhalb einer mehrsprachigen Erweiterung nicht global erzwingen; bietet ein Anbieter selbst DE und EN, können beide im Katalog sichtbar sein.

Die Sportprofile ohne Asien schließen Quellen mit eindeutig asiatischem Bezug aus. Profil 9 nimmt gezielt nur geprüfte asiatische Sportquellen mit deutscher oder englischer Kennzeichnung wieder auf.

## Pflege

Lokale Quellen liegen unter `extensions/QuellenSources/`. Der Build erzeugt echte `.cs3`-Archive mit `manifest.json`, `pluginClassName` und `classes.dex`:

```sh
cd extensions/QuellenSources
./gradlew -Dorg.gradle.java.home=/usr/lib/jvm/java-21-openjdk-amd64 make makePluginsJson
```

Nach Änderungen an Basislisten die Profile neu erzeugen:

```sh
python3 tools/build_profiles.py
python3 tools/validate_repository.py
```

Falls alte, bereits installierte Fremd-Plugins noch doppelt angezeigt werden, diese einmal in CloudStream entfernen und anschließend nur diese Repo-Quelle neu laden. Ein Manifest kann eine zuvor lokal installierte, inzwischen entfernte Erweiterung nicht selbst deinstallieren.
