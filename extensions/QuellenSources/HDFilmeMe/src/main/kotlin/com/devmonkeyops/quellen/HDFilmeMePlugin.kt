package com.darkninja.quellen

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class HDFilmeMePlugin : BasePlugin() {
    override fun load() {
        registerMainAPI(KinoCatalogProvider("HDFilmeMe", "https://hdfilme.me"))
    }
}
