package com.darkninja.quellen

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class KinoxW11Plugin : BasePlugin() {
    override fun load() {
        registerMainAPI(KinoCatalogProvider("KinoxW11", "https://w11.kinox.to"))
    }
}
