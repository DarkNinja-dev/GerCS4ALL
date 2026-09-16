package com.darkninja.quellen

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class KinoXPlugin : BasePlugin() {
    override fun load() {
        registerMainAPI(KinoCatalogProvider("KinoX", "https://w11.kinox.to"))
    }
}
