package com.darkninja.quellen

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class MegakinoPlugin : BasePlugin() {
    override fun load() {
        registerMainAPI(KinoCatalogProvider("Megakino", "https://megakino.org"))
    }
}
