package com.darkninja.deenquellen

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class XcineRUPlugin : BasePlugin() {
    override fun load() {
        registerMainAPI(KinoCatalogProvider("XcineRU", "https://xcine.ru"))
    }
}
