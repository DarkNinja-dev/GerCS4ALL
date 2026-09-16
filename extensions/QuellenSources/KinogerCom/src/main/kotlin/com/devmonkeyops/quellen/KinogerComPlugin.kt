package com.darkninja.quellen

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class KinogerComPlugin : BasePlugin() {
    override fun load() {
        registerMainAPI(KinoCatalogProvider("KinogerCom", "https://kinoger.com"))
    }
}
