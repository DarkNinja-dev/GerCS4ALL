package com.darkninja.quellen

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class Megakino7Plugin : BasePlugin() {
    override fun load() {
        registerMainAPI(KinoCatalogProvider("Megakino7", "https://7megakino.lol"))
    }
}
