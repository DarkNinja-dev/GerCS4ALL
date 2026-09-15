package com.darkninja.deenquellen

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class Movie4kPlugin : BasePlugin() {
    override fun load() {
        registerMainAPI(KinoCatalogProvider("Movie4k", "https://movie4k.sx"))
    }
}
