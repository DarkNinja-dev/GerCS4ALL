package com.darkninja.deenquellen

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class Tele5Plugin : BasePlugin() {
    override fun load() {
        registerMainAPI(Tele5Mediathek())
    }
}
