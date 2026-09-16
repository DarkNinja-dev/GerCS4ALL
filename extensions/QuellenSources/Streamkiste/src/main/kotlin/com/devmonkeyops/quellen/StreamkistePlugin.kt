package com.darkninja.quellen

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class StreamkistePlugin : BasePlugin() {
    override fun load() {
        registerMainAPI(KinoCatalogProvider("Streamkiste", "https://streamkiste.sx"))
    }
}
