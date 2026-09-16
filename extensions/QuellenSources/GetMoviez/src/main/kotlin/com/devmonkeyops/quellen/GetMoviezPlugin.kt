package com.darkninja.quellen

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class GetMoviezPlugin : BasePlugin() {
    override fun load() {
        registerMainAPI(KinoCatalogProvider("GetMoviez", "https://getmoviez.cc"))
    }
}
