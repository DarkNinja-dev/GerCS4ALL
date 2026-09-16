package com.darkninja.quellen

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class StreamkisteBidPlugin : BasePlugin() {
    override fun load() {
        registerMainAPI(KinoCatalogProvider("StreamkisteBid", "https://streamkiste.bid"))
    }
}
