version = 1

cloudstream {
    language = "de"
    description = "KinoKiste-/Movie4k-/Streamcloud-Klon mit wählbarer Domain, 60 Treffern und erweiterten Kategorien."
    authors = listOf("DevMonkeyOps", "Bnyro")
    status = 1
    tvTypes = listOf("Movie", "TvSeries")
    iconUrl = "https://www.google.com/s2/favicons?domain=kinokiste.club&sz=%size%"
}

dependencies {
    // CloudStream provides the actual Android Plugin type at runtime; the
    // compile-only header lets this extension expose a settings button.
    compileOnly(files("../host-stubs/host-plugin-stub.jar"))
}
