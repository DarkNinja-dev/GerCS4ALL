package com.darkninja.deenquellen

import android.content.Context
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageData
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.mainPageOf
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.newMovieLoadResponse
import com.lagradost.cloudstream3.newMovieSearchResponse
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor

/** A single, correctly named replacement for the duplicate upstream entries. */
@CloudstreamPlugin
class EinschaltenPlugin : Plugin() {
    override fun load(context: Context) {
        registerMainAPI(EinschaltenProvider())
    }
}

private class EinschaltenProvider : MainAPI() {
    override var name = "Einschalten"
    override var lang = "de"
    override var mainUrl = "https://einschalten.in"
    override val hasQuickSearch = true
    override val hasMainPage = true
    override val supportedTypes = setOf(TvType.Movie)
    override val mainPage: List<MainPageData> = mainPageOf(
        "new" to "Neue Filme",
        "added" to "Zuletzt hinzugefügt",
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val response = app.get("$mainUrl/api/movies?order=${request.data}").parsed<MovieResponse>()
        val films = response.data.map { it.toSearchResponse() }
        return newHomePageResponse(request.name, films, hasNext = response.pagination?.hasMore == true)
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun search(query: String): List<SearchResponse> {
        val response = app.post(
            "$mainUrl/api/search",
            json = mapOf("pageNumber" to "0", "pageSize" to "60", "query" to query),
            headers = mapOf("Content-Type" to "application/json"),
        ).parsed<MovieResponse>()
        return response.data.map { it.toSearchResponse() }
    }

    override suspend fun load(url: String): LoadResponse? {
        val id = url.substringAfterLast('/').toIntOrNull() ?: return null
        val movie = app.get("$mainUrl/api/movies/$id").parsed<MovieItem>()
        return newMovieLoadResponse(movie.title.orEmpty(), url, TvType.Movie, id.toString()) {
            posterUrl = movie.posterUrl()
            plot = movie.overview
            duration = movie.runtime
            year = movie.releaseDate?.take(4)?.toIntOrNull()
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit,
    ): Boolean {
        val source = app.get("$mainUrl/api/movies/$data/watch").parsed<StreamSource>()
        val streamUrl = source.streamUrl ?: return false
        loadExtractor(streamUrl, "$mainUrl/", subtitleCallback, callback)
        return true
    }

    private fun MovieItem.posterUrl(): String? = posterPath?.trimStart('/')?.let { "$mainUrl/api/image/poster/$it" }
    private fun MovieItem.toSearchResponse(): SearchResponse = newMovieSearchResponse(
        title.orEmpty(), "$mainUrl/movies/$id", TvType.Movie,
    ) {
        posterUrl = this@toSearchResponse.posterUrl()
        year = releaseDate?.take(4)?.toIntOrNull()
    }

    private data class MovieResponse(
        val data: List<MovieItem> = emptyList(),
        val pagination: Pagination? = null,
    )
    private data class Pagination(val hasMore: Boolean? = false)
    private data class MovieItem(
        val id: Int? = null,
        val title: String? = null,
        val releaseDate: String? = null,
        val posterPath: String? = null,
        val overview: String? = null,
        val runtime: Int? = null,
    )
    private data class StreamSource(val streamUrl: String? = null)
}
