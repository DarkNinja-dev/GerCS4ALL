package com.darkninja.quellen

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class KinoxW21Plugin : BasePlugin() {
    override fun load() {
        registerMainAPI(KinoCatalogProvider("KinoxW21", "https://www21.kinox.to"))
    }
}
