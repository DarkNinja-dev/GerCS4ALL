version = 1

cloudstream {
    language = "de"
    description = "Einschalten – deutschsprachige Filme."
    authors = listOf("DevMonkeyOps", "Bnyro")
    status = 1
    tvTypes = listOf("Movie")
    iconUrl = "https://www.google.com/s2/favicons?domain=einschalten.in&sz=%size%"
}

dependencies {
    compileOnly(files("../host-stubs/host-plugin-stub.jar"))
}
