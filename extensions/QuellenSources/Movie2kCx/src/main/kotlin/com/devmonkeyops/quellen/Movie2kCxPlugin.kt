package com.darkninja.quellen

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class Movie2kCxPlugin : BasePlugin() {
    override fun load() {
        registerMainAPI(KinoCatalogProvider("Movie2kCx", "https://movie2k.cx"))
    }
}
