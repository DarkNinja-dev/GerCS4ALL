package com.darkninja.quellen

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class MegakinoMePlugin : BasePlugin() {
    override fun load() {
        registerMainAPI(KinoCatalogProvider("MegakinoMe", "https://megakino.me"))
    }
}
