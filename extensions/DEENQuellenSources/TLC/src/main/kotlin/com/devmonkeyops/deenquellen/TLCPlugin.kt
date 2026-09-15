package com.darkninja.deenquellen

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class TLCPlugin : BasePlugin() {
    override fun load() {
        registerMainAPI(TlcMediathek())
    }
}
