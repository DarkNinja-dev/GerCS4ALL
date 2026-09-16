package com.darkninja.quellen

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class MegakinoOrgPlugin : BasePlugin() {
    override fun load() {
        registerMainAPI(KinoCatalogProvider("MegakinoOrg", "https://megakino.org"))
    }
}
