package com.darkninja.quellen

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class HDFilmeCafePlugin : BasePlugin() {
    override fun load() {
        registerMainAPI(KinoCatalogProvider("HDFilmeCafe", "https://hdfilme.cafe"))
    }
}
