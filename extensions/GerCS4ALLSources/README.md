# GerCS4ALL – eigene Erweiterungen

`GerCS4ALLAnime` ist ein echtes CloudStream-Plugin und registriert nur Anime-Anbieter. Seine Manifest-Typen sind ausschließlich `Anime`, `AnimeMovie` und `OVA`; deshalb erscheint es nie in der Film- oder Serienauswahl.

Die WordPress-Suche von KayoAnime und AnimeToast wird direkt über deren öffentliche REST-Schnittstelle abgefragt. Kayo-Dateien mit einer Google-Drive-Datei-ID werden zu einem abspielbaren Download-Link aufgelöst. Google-Drive-Ordner werden bewusst nicht als Video ausgegeben, weil sie keine einzelne abspielbare Mediendatei darstellen.

Lokaler Build (Java 21, Android SDK mit Platform 35):

```sh
cd extensions/GerCS4ALLSources
./gradlew -Dorg.gradle.java.home=/usr/lib/jvm/java-21-openjdk-amd64 make makePluginsJson
```

Die Compile-Abhängigkeit nutzt die offizielle ReCloudStream-Snapshot-Bibliothek auf JitPack. Der veröffentlichte `GerCS4ALLAnime.cs3` wurde gegen den entsprechenden öffentlichen CloudStream-Quellstand gebaut; wenn JitPack vorübergehend rate-limitiert, reicht ein erneuter Build später aus.
