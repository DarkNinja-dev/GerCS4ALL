version = 1

cloudstream {
    language = "de"
    description = "Offizielle DMAX-, TELE 5- und TLC-Mediatheken mit korrigierten Coverbildern."
    authors = listOf("DevMonkeyOps", "Bnyro")
    status = 1
    tvTypes = listOf("Movie", "TvSeries")
    iconUrl = "https://www.google.com/s2/favicons?domain=dmax.de&sz=%size%"
}

dependencies {
    compileOnly(files("../host-stubs/host-plugin-stub.jar"))
}
