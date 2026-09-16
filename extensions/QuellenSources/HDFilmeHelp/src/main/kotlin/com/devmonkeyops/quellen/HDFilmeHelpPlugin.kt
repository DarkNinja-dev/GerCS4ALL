package com.darkninja.quellen

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class HDFilmeHelpPlugin : BasePlugin() {
    override fun load() {
        registerMainAPI(KinoCatalogProvider("HDFilmeHelp", "https://hdfilme-tv.help"))
    }
}
