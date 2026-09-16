package com.darkninja.quellen

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class HDFilmePlugin : BasePlugin() {
    override fun load() {
        registerMainAPI(KinoCatalogProvider("HDFilme", "https://hdfilme.to"))
    }
}
