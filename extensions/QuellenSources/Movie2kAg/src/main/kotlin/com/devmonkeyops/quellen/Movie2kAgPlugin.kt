package com.darkninja.quellen

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class Movie2kAgPlugin : BasePlugin() {
    override fun load() {
        registerMainAPI(KinoCatalogProvider("Movie2kAg", "https://movie2k.ag"))
    }
}
