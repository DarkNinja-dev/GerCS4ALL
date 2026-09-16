# Eigene Erweiterungen

`AnimeToast` registriert nur AnimeToast und `KayoAnime` nur KayoAnime. Beide Manifest-Typen sind ausschließlich `Anime`, `AnimeMovie` und `OVA`; deshalb erscheinen sie nie in der Film- oder Serienauswahl. Die getrennten Sprachkennzeichnungen sorgen dafür, dass AnimeToast auch im deutschen Anime-Profil sichtbar ist.

`InternetArchiveMoviesDE` ist ausschließlich als `Movie` markiert und fragt den öffentlichen Internet-Archive-Katalog nach deutsch markierten Filmen ab. `InternetArchiveAnimeDE` ist ein getrenntes Anime-Paket für die Archivsuche `anime deutsch`; es kann nicht in einer Film- oder Serienliste erscheinen.

`SerienStream` registriert nur `TvSeries`. In seinen CloudStream-Plugin-Einstellungen können Nutzer die aktuell von SerienStream veröffentlichten Endpunkte selbst wählen: `https://serienstream.to`, `https://serienstream.cx` oder `http://186.2.175.5`. Die Einstellung wird sofort für die registrierte Quelle übernommen und dauerhaft gespeichert.

`HDFilme`, `Movie2k`, `Streamkiste` und `Megakino` bleiben vier eigenständige Erweiterungen. Auch bei übereinstimmenden Katalogantworten werden ihre Namen, Erweiterungs-IDs und Update-Kanäle nicht zusammengeführt. Die wiederhergestellten gleich klingenden Domains sind ebenfalls eigene Erweiterungen, jeweils mit einer festen Adresse und einer eigenen Erweiterungs-ID; sie sind ausdrücklich keine Spiegel oder Untermenüs einer anderen Quelle.

`KinoKiste`, `KKiste`, `Movie4k`, `Streamcloud` und `XcineRU` verwenden jeweils eine eigene Erweiterung mit ihrer festen Adresse. Die Katalogabfragen nutzen `limit=60` und enthalten zusätzliche Kategorien für Bewertungen, Beliebtheit, Marvel/MCU und Genres.

`DMAX`, `Tele5` und `TLC` sind jeweils einzeln installierbar. Für die Vorschaubilder wird die Kataloggrafik verwendet und bei Bedarf auf das Metadatenbild zurückgefallen. Der Player verwendet den offiziellen Discovery-Token-Endpunkt, den korrekten `wisteriaProperties`-Request-Körper und die formatbasierte Stream-Antwort; dadurch funktionieren die drei Quellen unabhängig voneinander.

`Einschalten` registriert genau eine Quelle mit dem sichtbaren Namen `Einschalten`; damit ersetzen wir die zwei alten, identischen `EinschaltenIn`-/`D000d`-Manifestzeilen.

Die WordPress-Suche von KayoAnime und AnimeToast wird direkt über deren öffentliche REST-Schnittstelle abgefragt. Kayo-Dateien mit einer Google-Drive-Datei-ID werden zu einem abspielbaren Download-Link aufgelöst. Google-Drive-Ordner werden bewusst nicht als Video ausgegeben, weil sie keine einzelne abspielbare Mediendatei darstellen.

Lokaler Build (Java 21, Android SDK mit Platform 35):

```sh
cd extensions/QuellenSources
./gradlew -Dorg.gradle.java.home=/usr/lib/jvm/java-21-openjdk-amd64 make makePluginsJson
```

Nach einem lokalen Build alle geänderten Dateien unter `plugins/` und `lists/` mit einchecken. Die Gradle-Datei ist ausführbar eingecheckt, damit der dokumentierte Aufruf auf Linux direkt funktioniert.

Vom Repository-Root aus übernimmt anschließend `python3 tools/sync_local_plugins.py` das Veröffentlichen der gepflegten lokalen Pakete und ihrer Metadaten. Danach immer `python3 tools/build_profiles.py` und `python3 tools/validate_repository.py` ausführen.

Die Compile-Abhängigkeit nutzt die offizielle ReCloudStream-Snapshot-Bibliothek auf JitPack. Die veröffentlichten lokalen `.cs3`-Pakete werden gegen den entsprechenden öffentlichen CloudStream-Quellstand gebaut; wenn JitPack vorübergehend rate-limitiert, reicht ein erneuter Build später aus.
