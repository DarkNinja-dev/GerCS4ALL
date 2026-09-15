version = 1

cloudstream {
    language = "de"
    description = "XcineRU mit 60 Treffern und erweiterten Kategorien."
    authors = listOf("DarkNinja-dev", "Bnyro")
    status = 1
    tvTypes = listOf("Movie", "TvSeries")
    iconUrl = "https://www.google.com/s2/favicons?domain=xcine.ru&sz=%size%"
}

android {
    sourceSets.getByName("main").java.srcDir("../KinoCatalogShared/src/main/kotlin")
}
