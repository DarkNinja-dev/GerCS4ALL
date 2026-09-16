package com.darkninja.quellen

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class KinozPlugin : BasePlugin() {
    override fun load() {
        registerMainAPI(KinoCatalogProvider("Kinoz", "https://kinoz.to"))
    }
}
