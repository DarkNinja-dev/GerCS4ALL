package com.darkninja.quellen

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class KinogerToPlugin : BasePlugin() {
    override fun load() {
        registerMainAPI(KinoCatalogProvider("KinogerTo", "https://kinoger.to"))
    }
}
