version = 1

cloudstream {
    language = "de"
    description = "Kinoz mit eigener Quelle."
    authors = listOf("DarkNinja-dev")
    status = 1
    tvTypes = listOf("Movie", "TvSeries")
    iconUrl = "https://www.google.com/s2/favicons?domain=kinoz.to&sz=%size%"
}

android {
    sourceSets.getByName("main").java.srcDir("../KinoCatalogShared/src/main/kotlin")
}
