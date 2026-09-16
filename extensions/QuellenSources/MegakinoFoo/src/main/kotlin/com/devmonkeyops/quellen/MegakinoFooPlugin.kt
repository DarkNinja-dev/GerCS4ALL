package com.darkninja.quellen

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class MegakinoFooPlugin : BasePlugin() {
    override fun load() {
        registerMainAPI(KinoCatalogProvider("MegakinoFoo", "https://megakino.foo"))
    }
}
