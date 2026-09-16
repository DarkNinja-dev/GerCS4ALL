package com.darkninja.quellen

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class Movie2kPlugin : BasePlugin() {
    override fun load() {
        registerMainAPI(KinoCatalogProvider("Movie2k", "https://movie2k.cx"))
    }
}
