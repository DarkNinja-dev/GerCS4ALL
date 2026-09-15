package com.darkninja.deenquellen

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class DMAXPlugin : BasePlugin() {
    override fun load() {
        registerMainAPI(DmaxMediathek())
    }
}
