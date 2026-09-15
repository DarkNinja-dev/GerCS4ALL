version = 1

cloudstream {
    language = "de"
    description = "SerienStream mit auswählbarer offizieller Domain oder Direkt-IP. Nur Serien."
    authors = listOf("DevMonkeyOps", "Bnyro")
    status = 1
    tvTypes = listOf("TvSeries")
    iconUrl = "https://www.google.com/s2/favicons?domain=serienstream.to&sz=%size%"
}

// The actual Plugin base class belongs to the CloudStream Android host.  The
// tiny header is compile-only and therefore cannot shadow that host class in
// the generated extension.
dependencies {
    compileOnly(files("../host-stubs/host-plugin-stub.jar"))
    // `newExtractorLink` is suspend while the extractor callback is not; the
    // CloudStream host already provides coroutines at runtime.
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
}
