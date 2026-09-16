package com.darkninja.quellen

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class Megakino19Plugin : BasePlugin() {
    override fun load() {
        registerMainAPI(KinoCatalogProvider("Megakino19", "https://megakino19.com"))
    }
}
