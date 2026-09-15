package com.darkninja.quellen

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class KKistePlugin : BasePlugin() {
    override fun load() {
        registerMainAPI(KinoCatalogProvider("KKiste", "https://kkiste.eu"))
    }
}
