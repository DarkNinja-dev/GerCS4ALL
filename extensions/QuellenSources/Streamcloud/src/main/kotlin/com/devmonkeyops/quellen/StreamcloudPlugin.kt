package com.darkninja.quellen

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class StreamcloudPlugin : BasePlugin() {
    override fun load() {
        registerMainAPI(KinoCatalogProvider("Streamcloud", "https://streamcloud.sx"))
    }
}
