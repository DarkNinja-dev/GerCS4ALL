package com.darkninja.quellen

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class CineToPlugin : BasePlugin() {
    override fun load() {
        registerMainAPI(KinoCatalogProvider("CineTo", "https://cine.to"))
    }
}
