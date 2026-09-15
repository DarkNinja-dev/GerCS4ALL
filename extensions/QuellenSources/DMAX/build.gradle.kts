version = 2

cloudstream {
    language = "de"
    description = "Offizielle DMAX-Mediathek mit korrigierten Coverbildern."
    authors = listOf("DarkNinja-dev", "Bnyro")
    status = 1
    tvTypes = listOf("Movie", "TvSeries")
    iconUrl = "https://www.google.com/s2/favicons?domain=dmax.de&sz=%size%"
}

android {
    sourceSets.getByName("main").java.srcDir("../DiscoveryShared/src/main/kotlin")
}
