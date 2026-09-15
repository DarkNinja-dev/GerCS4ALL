package com.darkninja.deenquellen

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import com.lagradost.cloudstream3.HomePageList
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.amap
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.fixUrl
import com.lagradost.cloudstream3.fixUrlNull
import com.lagradost.cloudstream3.newEpisode
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.newTvSeriesLoadResponse
import com.lagradost.cloudstream3.newTvSeriesSearchResponse
import com.lagradost.cloudstream3.extractors.Urochsunloath
import com.lagradost.cloudstream3.extractors.Voe1
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor
import com.lagradost.cloudstream3.utils.newExtractorLink
import kotlinx.coroutines.runBlocking
import org.jsoup.nodes.Element

/**
 * The public addresses currently published by SerienStream itself.  We keep
 * this deliberately small: historic aliases such as s.to are not offered as
 * working choices once the service has retired them.
 */
private enum class SerienStreamEndpoint(val label: String, val url: String) {
    PRIMARY("serienstream.to (Standard)", "https://serienstream.to"),
    ALTERNATE("serienstream.cx", "https://serienstream.cx"),
    DIRECT_IP("186.2.175.5 (Direkt-IP, HTTP)", "http://186.2.175.5"),
}

@CloudstreamPlugin
class SerienStreamPlugin : Plugin() {
    private var provider: SerienStream? = null
    private lateinit var preferences: android.content.SharedPreferences

    override fun load(context: Context) {
        preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val endpoint = selectedEndpoint()

        provider = SerienStream(endpoint).also(::registerMainAPI)
        registerExtractorAPI(Voe1())
        registerExtractorAPI(Urochsunloath())

        // CloudStream exposes this callback as the extension's settings button.
        openSettings = { settingsContext -> showEndpointChooser(settingsContext) }
    }

    private fun selectedEndpoint(): SerienStreamEndpoint {
        val savedUrl = preferences.getString(PREFERENCE_ENDPOINT, null)
        return SerienStreamEndpoint.entries.firstOrNull { it.url == savedUrl }
            ?: SerienStreamEndpoint.PRIMARY
    }

    private fun showEndpointChooser(context: Context) {
        val entries = SerienStreamEndpoint.entries
        val activeIndex = entries.indexOf(selectedEndpoint()).coerceAtLeast(0)

        AlertDialog.Builder(context)
            .setTitle("SerienStream-Adresse")
            .setMessage("Wähle eine aktuell von SerienStream veröffentlichte Adresse.")
            .setSingleChoiceItems(entries.map { it.label }.toTypedArray(), activeIndex) { dialog, index ->
                val endpoint = entries[index]
                preferences.edit().putString(PREFERENCE_ENDPOINT, endpoint.url).apply()
                provider?.setEndpoint(endpoint)
                Toast.makeText(context, "SerienStream: ${endpoint.label}", Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private companion object {
        const val PREFERENCES_NAME = "SerienStream"
        const val PREFERENCE_ENDPOINT = "endpoint"
    }
}

/**
 * Adapted from the public GermanProviders Serienstream provider (Bnyro), with
 * dynamic endpoint selection added for this source catalogue.
 */
private class SerienStream(endpoint: SerienStreamEndpoint) : MainAPI() {
    override var mainUrl = endpoint.url
    override var name = "SerienStream"
    override var lang = "de"
    override val supportedTypes = setOf(TvType.TvSeries)
    override val hasMainPage = true

    fun setEndpoint(endpoint: SerienStreamEndpoint) {
        mainUrl = endpoint.url
    }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get("$mainUrl/beliebte-serien").document
        val lists = document.select(".popular-page > div").mapNotNull { section ->
            val header = section.selectFirst("div > h2")?.text() ?: return@mapNotNull null
            val items = section.select("a.show-card").mapNotNull { it.toSearchResult() }
            HomePageList(header, items).takeIf { items.isNotEmpty() }
        }
        return newHomePageResponse(lists, hasNext = false)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get(
            "$mainUrl/suche",
            params = mapOf("term" to query, "tab" to "shows"),
            referer = "$mainUrl/suche",
        ).document
        return document.select(".results-group .card").mapNotNull { it.toSearchResult() }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        val metadata = document.selectFirst(".show-header-wrapper .container-fluid > div")
            ?: return null
        val title = metadata.selectFirst("h1")?.text()?.takeIf { it.isNotBlank() } ?: return null
        val poster = fixUrlNull(metadata.selectFirst("img")?.attr("data-src"))
        val year = metadata.selectFirst("h1 + p > a")?.text()?.toIntOrNull()
        val description = metadata.select(".description-text").text()
        val actors = metadata.select("li.series-group:contains(Besetzung:) a").map { it.text() }
        val genres = metadata.select("li.series-group:contains(Genre:) a").map { it.text() }
        val trailerUrl = metadata.selectFirst("button[data-trailer-url]")?.attr("data-trailer-url")

        val episodes = document.select("#season-nav ul > li a").amap { season ->
            val seasonNumber = season.text().trim().toIntOrNull()
            val seasonDocument = app.get(fixUrl(season.attr("href"))).document
            seasonDocument.select(".episode-section .episode-row").mapNotNull { episode ->
                val episodeLink = episode.attr("onclick").substringAfter("=").trim('\'')
                    .takeIf { it.isNotBlank() } ?: return@mapNotNull null
                newEpisode(episodeLink) {
                    this.episode = episode.selectFirst(".episode-number-cell")?.text()?.toIntOrNull()
                    this.name = episode.select(".episode-title-cell > *")
                        .joinToString(" - ") { it.text() }
                    this.season = seasonNumber
                }
            }
        }.flatten()

        return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
            name = title
            posterUrl = poster
            this.year = year
            plot = description
            tags = genres
            addTrailer(trailerUrl)
            addActors(actors)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit,
    ): Boolean {
        val document = app.get(data).document
        document.select(".link-wrapper > button").amap { button ->
            val streamUrl = fixUrl(button.attr("data-play-url"))
            val source = button.attr("data-provider-name")
            val language = button.attr("data-language-label")
            val redirectedStreamUrl = app.get(streamUrl).url
            loadExtractor(redirectedStreamUrl, data, subtitleCallback) { link ->
                callback(
                    runBlocking {
                        newExtractorLink(source, "$source [$language]", link.url) {
                            referer = link.referer
                            quality = link.quality
                            type = link.type
                            headers = link.headers
                            extractorData = link.extractorData
                        }
                    }
                )
            }
        }
        return true
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val href = fixUrlNull(selectFirst("a")?.attr("href")) ?: return null
        val title = selectFirst("img")?.attr("alt")?.takeIf { it.isNotBlank() } ?: return null
        val posterUrl = fixUrlNull(selectFirst("img")?.let { image ->
            image.attr("data-src").ifBlank { image.attr("src") }
        })
        return newTvSeriesSearchResponse(title, href, TvType.TvSeries) { this.posterUrl = posterUrl }
    }
}
