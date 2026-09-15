# Eigene Erweiterungen

`AnimeToast` registriert nur AnimeToast und `KayoAnime` nur KayoAnime. Beide Manifest-Typen sind ausschließlich `Anime`, `AnimeMovie` und `OVA`; deshalb erscheinen sie nie in der Film- oder Serienauswahl. Die getrennten Sprachkennzeichnungen sorgen dafür, dass AnimeToast auch im deutschen Anime-Profil sichtbar ist.

`InternetArchiveMoviesDE` ist ausschließlich als `Movie` markiert und fragt den öffentlichen Internet-Archive-Katalog nach deutsch markierten Filmen ab. `InternetArchiveAnimeDE` ist ein getrenntes Anime-Paket für die Archivsuche `anime deutsch`; es kann nicht in einer Film- oder Serienliste erscheinen.

`SerienStream` registriert nur `TvSeries`. In seinen CloudStream-Plugin-Einstellungen können Nutzer die aktuell von SerienStream veröffentlichten Endpunkte selbst wählen: `https://serienstream.to`, `https://serienstream.cx` oder `http://186.2.175.5`. Die Einstellung wird sofort für die registrierte Quelle übernommen und dauerhaft gespeichert.

`KinoKiste`, `KKiste`, `Movie4k`, `Streamcloud` und `XcineRU` verwenden jeweils eine eigene Erweiterung mit ihrer festen Adresse. Die Katalogabfragen nutzen `limit=60` und enthalten zusätzliche Kategorien für Bewertungen, Beliebtheit, Marvel/MCU und Genres.

`DMAX`, `Tele5` und `TLC` sind jeweils einzeln installierbar. Für die Vorschaubilder wird die Kataloggrafik verwendet und bei Bedarf auf das Metadatenbild zurückgefallen.

`Einschalten` registriert genau eine Quelle mit dem sichtbaren Namen `Einschalten`; damit ersetzen wir die zwei alten, identischen `EinschaltenIn`-/`D000d`-Manifestzeilen.

Die WordPress-Suche von KayoAnime und AnimeToast wird direkt über deren öffentliche REST-Schnittstelle abgefragt. Kayo-Dateien mit einer Google-Drive-Datei-ID werden zu einem abspielbaren Download-Link aufgelöst. Google-Drive-Ordner werden bewusst nicht als Video ausgegeben, weil sie keine einzelne abspielbare Mediendatei darstellen.

Lokaler Build (Java 21, Android SDK mit Platform 35):

```sh
cd extensions/DEENQuellenSources
./gradlew -Dorg.gradle.java.home=/usr/lib/jvm/java-21-openjdk-amd64 make makePluginsJson
```

Die Compile-Abhängigkeit nutzt die offizielle ReCloudStream-Snapshot-Bibliothek auf JitPack. Die veröffentlichten lokalen `.cs3`-Pakete werden gegen den entsprechenden öffentlichen CloudStream-Quellstand gebaut; wenn JitPack vorübergehend rate-limitiert, reicht ein erneuter Build später aus.
