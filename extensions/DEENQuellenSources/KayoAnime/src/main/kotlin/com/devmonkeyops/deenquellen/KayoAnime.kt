package com.darkninja.deenquellen

import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.DubStatus
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.addEpisodes
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.newAnimeLoadResponse
import com.lagradost.cloudstream3.newAnimeSearchResponse
import com.lagradost.cloudstream3.newEpisode
import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor
import com.lagradost.cloudstream3.utils.newExtractorLink
import java.net.URLEncoder

@CloudstreamPlugin
class KayoAnimePlugin : BasePlugin() {
    override fun load() {
        registerMainAPI(KayoAnime())
    }
}

private data class WordPressSearchItem(
    @JsonProperty("title") val title: String,
    @JsonProperty("url") val url: String,
)

/** KayoAnime is isolated from the German package to preserve profile filters. */
private class KayoAnime : MainAPI() {
    override var mainUrl = "https://kayoanime.com"
    override var name = "KayoAnime"
    override var lang = "en"
    override val supportedTypes = setOf(TvType.Anime, TvType.AnimeMovie, TvType.OVA)

    override suspend fun search(query: String): List<SearchResponse> {
        val encoded = URLEncoder.encode(query, "UTF-8")
        return app.get("$mainUrl/wp-json/wp/v2/search?search=$encoded&per_page=30")
            .parsed<ArrayList<WordPressSearchItem>>()
            .map { result -> newAnimeSearchResponse(result.title.stripHtml(), result.url, TvType.Anime) }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        val title = document.selectFirst("h1.entry-title, h1.post-title, article h1, h1")
            ?.text()?.takeIf { it.isNotBlank() } ?: return null
        val poster = document.selectFirst("meta[property=og:image], .post-thumbnail img, .entry-content img, article img")
            ?.let { element -> element.attr("content").ifBlank { element.absUrl("src") } }
        val description = document.selectFirst(".entry-content, .post-content, article")?.text()

        return newAnimeLoadResponse(title, url, TvType.Anime) {
            posterUrl = poster
            plot = description
            addEpisodes(DubStatus.Subbed, listOf(newEpisode(url) { name = "Stream"; episode = 1 }))
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit,
    ): Boolean {
        val links = app.get(data).document.select(".entry-content a[href], .post-content a[href], article a[href]")
            .map { it.absUrl("href") }
            .filter { it.isCandidateLink() }
            .distinct()

        links.forEach { link ->
            when {
                link.isGoogleDriveFile() -> callback(
                    newExtractorLink(name, "$name – Google Drive", directGoogleDrive(link)) { referer = mainUrl }
                )
                else -> loadExtractor(link, data, subtitleCallback, callback)
            }
        }
        return links.isNotEmpty()
    }

    private fun String.isCandidateLink(): Boolean {
        val value = lowercase()
        return value.contains("drive.google.com/file/") || value.contains("drive.google.com/open") ||
            value.contains("/embed/") || value.endsWith(".m3u8") || value.endsWith(".mp4") ||
            value.contains("streamtape") || value.contains("dood") || value.contains("vidhide") ||
            value.contains("voe.sx") || value.contains("filemoon")
    }

    private fun String.isGoogleDriveFile() = contains("drive.google.com/file/") || contains("drive.google.com/open")

    private suspend fun directGoogleDrive(url: String): String {
        val id = Regex("(?:/d/|[?&]id=)([^/?&]+)").find(url)?.groupValues?.getOrNull(1) ?: return url
        val initial = "https://drive.google.com/uc?id=$id&export=download"
        val document = app.get(initial).document
        val form = document.selectFirst("form#download-form")?.attr("action")
        val token = document.selectFirst("input#uc-download-link")?.attr("value")
        return if (!form.isNullOrBlank() && !token.isNullOrBlank()) {
            app.post(form, data = mapOf("uc-download-link" to token)).url
        } else initial
    }

    private fun String.stripHtml() = replace(Regex("<[^>]*>"), "").trim()
}
