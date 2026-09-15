package com.devmonkeyops.gercs4all

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageData
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.amap
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.mainPageOf
import com.lagradost.cloudstream3.newEpisode
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.newMovieLoadResponse
import com.lagradost.cloudstream3.newTvSeriesLoadResponse
import com.lagradost.cloudstream3.newTvSeriesSearchResponse
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import com.lagradost.cloudstream3.utils.AppUtils.toJson
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor
import java.net.URL
import java.net.URLEncoder

/**
 * These hosts were checked against the same public `/data/browse/` contract.
 * They are aliases of the same catalogue, not separate CloudStream sources.
 */
private enum class KinoKisteEndpoint(val label: String, val url: String) {
    KINOKISTE("kinokiste.club (Standard)", "https://kinokiste.club"),
    KKISTE("kkiste.eu", "https://kkiste.eu"),
    MOVIE4K("movie4k.sx", "https://movie4k.sx"),
    STREAMCLOUD("streamcloud.sx", "https://streamcloud.sx"),
    XCINE("xcine.ru", "https://xcine.ru"),
}

@CloudstreamPlugin
class GerCS4ALLKinoKistePlugin : Plugin() {
    private lateinit var preferences: android.content.SharedPreferences
    private var provider: KinoKisteProvider? = null

    override fun load(context: Context) {
        preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        provider = KinoKisteProvider(selectedEndpoint()).also(::registerMainAPI)
        openSettings = { settingsContext -> showEndpointChooser(settingsContext) }
    }

    private fun selectedEndpoint(): KinoKisteEndpoint {
        val savedUrl = preferences.getString(PREFERENCE_ENDPOINT, null)
        return KinoKisteEndpoint.entries.firstOrNull { it.url == savedUrl }
            ?: KinoKisteEndpoint.KINOKISTE
    }

    private fun showEndpointChooser(context: Context) {
        val endpoints = KinoKisteEndpoint.entries
        val active = endpoints.indexOf(selectedEndpoint()).coerceAtLeast(0)
        AlertDialog.Builder(context)
            .setTitle("KinoKiste-Adresse")
            .setMessage("Alle Optionen sind geprüfte Spiegel desselben Katalogs. Die Auswahl erstellt keine doppelte Quelle.")
            .setSingleChoiceItems(endpoints.map { it.label }.toTypedArray(), active) { dialog, index ->
                val endpoint = endpoints[index]
                preferences.edit().putString(PREFERENCE_ENDPOINT, endpoint.url).apply()
                provider?.setEndpoint(endpoint)
                Toast.makeText(context, "KinoKiste: ${endpoint.label}", Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private companion object {
        const val PREFERENCES_NAME = "GerCS4ALLKinoKiste"
        const val PREFERENCE_ENDPOINT = "endpoint"
    }
}

/**
 * Derived from the public GermanProviders XCine provider.  The provider is
 * intentionally singular: its domains are aliases, selected through settings.
 */
private class KinoKisteProvider(endpoint: KinoKisteEndpoint) : MainAPI() {
    override var name = "KinoKiste"
    override var mainUrl = endpoint.url
    override var lang = "de"
    override val hasQuickSearch = true
    override val hasMainPage = true
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    override val mainPage: List<MainPageData> = mainPageOf(
        "movies,trending," to "Film-Trends",
        "tvseries,trending," to "Serien-Trends",
        "movies,updates," to "Neue Filme",
        "tvseries,updates," to "Neue Serien",
        "movies,views," to "Meistgesehene Filme",
        "tvseries,views," to "Meistgesehene Serien",
        "movies,rating," to "Bestbewertete Filme",
        "tvseries,rating," to "Bestbewertete Serien",
        "movies,votes," to "Filme mit den meisten Bewertungen",
        "tvseries,votes," to "Serien mit den meisten Bewertungen",
        "search:Marvel" to "Marvel / MCU",
    ) + listOf(
        "Action", "Animation", "Komödie", "Dokumentation", "Drama", "Familie",
        "Horror", "Romantik", "Sci-Fi", "Thriller",
    ).flatMap { genre ->
        mainPageOf(
            ",trending,$genre" to "$genre-Trends",
            ",updates,$genre" to "Neu in $genre",
            ",rating,$genre" to "Bestbewertet: $genre",
        )
    }

    fun setEndpoint(endpoint: KinoKisteEndpoint) {
        mainUrl = endpoint.url
    }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val home = if (request.data.startsWith("search:")) {
            val keyword = URLEncoder.encode(request.data.removePrefix("search:"), "UTF-8")
            app.get(
                "$mainUrl/data/browse/?lang=2&keyword=$keyword&year=&networks=&rating=&votes=&genre=&country=&cast=&directors=&type=&order_by=views&page=$page&limit=$PAGE_SIZE",
                referer = "$mainUrl/",
            ).parsed<MediaResponse>().movies.orEmpty()
        } else {
            val (type, order, genre) = request.data.split(",", limit = 3)
            app.get(
                "$mainUrl/data/browse/?lang=2&type=$type&order_by=$order&genre=$genre&page=$page&limit=$PAGE_SIZE",
                referer = "$mainUrl/",
            ).parsed<MediaResponse>().movies.orEmpty()
        }.mapNotNull { it.toSearchResponse() }

        return newHomePageResponse(request.name, home, hasNext = home.size >= PAGE_SIZE)
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun search(query: String): List<SearchResponse> {
        val keyword = URLEncoder.encode(query, "UTF-8")
        return app.get(
            "$mainUrl/data/browse/?lang=2&keyword=$keyword&year=&networks=&rating=&votes=&genre=&country=&cast=&directors=&type=&order_by=&page=1&limit=$PAGE_SIZE",
            referer = "$mainUrl/",
        ).parsed<MediaResponse>().movies.orEmpty().mapNotNull { it.toSearchResponse() }
    }

    override suspend fun load(url: String): LoadResponse? {
        val id = jsonMapper.readValue(url, ItemData::class.java).id ?: return null
        val response = app.get("$mainUrl/data/watch/?_id=$id", referer = "$mainUrl/")
            .parsed<MediaDetail>()
        val posterUrl = response.posterPath.toPosterUrl() ?: response.backdropPath.toPosterUrl()
        val actors = when (val cast = response.cast) {
            is String -> cast.split(", ")
            is List<*> -> cast.filterIsInstance<String>()
            else -> emptyList()
        }

        val recommendations = app.get(
            "$mainUrl/data/related_movies/?lang=2&cat=${if (response.tv == 1) "tv" else "movie"}&_id=$id&server=0",
            referer = "$mainUrl/",
        ).parsed<RecommendationsResponse>().mapNotNull { it.toSearchResponse() }

        return if (response.tv == 1) {
            val episodes = response.streams.orEmpty().groupBy { it.episode }.mapNotNull { (episode, streams) ->
                episode ?: return@mapNotNull null
                newEpisode(LoadData(streams.mapNotNull { it.stream }).toJson()) {
                    this.episode = episode
                    name = streams.firstOrNull()?.episodeTitle
                }
            }
            newTvSeriesLoadResponse(response.title ?: return null, url, TvType.TvSeries, episodes) {
                this.posterUrl = posterUrl
                year = response.year
                plot = response.storyline ?: response.overview
                tags = response.genres.orEmpty().split(", ").filter { it.isNotBlank() }
                contentRating = response.rating
                this.recommendations = recommendations
                addActors(actors)
            }
        } else {
            newMovieLoadResponse(
                response.title ?: return null,
                url,
                TvType.Movie,
                LoadData(response.streams.orEmpty().mapNotNull { it.stream }).toJson(),
            ) {
                this.posterUrl = posterUrl
                year = response.year
                plot = response.storyline ?: response.overview
                tags = response.genres.orEmpty().split(", ").filter { it.isNotBlank() }
                contentRating = response.rating
                this.recommendations = recommendations
                addActors(actors)
            }
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit,
    ): Boolean {
        val links = jsonMapper.readValue(data, LoadData::class.java).links
            .groupBy { runCatching { URL(it).host }.getOrNull() ?: it }
            .flatMap { (_, group) -> group.take(3) + group.drop(3).shuffled().take(2) }
        links.amap { link -> loadExtractor(link, "$mainUrl/", subtitleCallback, callback) }
        return links.isNotEmpty()
    }

    private fun Media.toSearchResponse(): SearchResponse? = newTvSeriesSearchResponse(
        title ?: originalTitle ?: return null,
        ItemData(id).toJson(),
        TvType.TvSeries,
        false,
    ) {
        posterUrl = posterSeasonPath.toPosterUrl() ?: posterPath.toPosterUrl() ?: backdropPath.toPosterUrl()
    }

    private fun String?.toPosterUrl(): String? = when {
        isNullOrBlank() -> null
        startsWith("/") -> "https://image.tmdb.org/t/p/w500$this"
        else -> this
    }

    private data class ItemData(val id: String? = null)
    private data class LoadData(val links: List<String>)
    private data class MediaResponse(@JsonProperty("movies") val movies: List<Media>? = emptyList())
    private class RecommendationsResponse : ArrayList<Media>()
    private data class Media(
        @JsonProperty("_id") val id: String? = null,
        @JsonProperty("original_title") val originalTitle: String? = null,
        val title: String? = null,
        @JsonProperty("poster_path") val posterPath: String? = null,
        @JsonProperty("backdrop_path") val backdropPath: String? = null,
        @JsonProperty("poster_season_path") val posterSeasonPath: String? = null,
    )
    private data class MediaDetail(
        val tv: Int? = null,
        val title: String? = null,
        @JsonProperty("poster_path") val posterPath: String? = null,
        @JsonProperty("backdrop_path") val backdropPath: String? = null,
        val year: Int? = null,
        val rating: String? = null,
        val genres: String? = null,
        val storyline: String? = null,
        val overview: String? = null,
        val streams: List<Stream>? = emptyList(),
        val cast: Any? = null,
    )
    private data class Stream(
        val stream: String? = null,
        @JsonProperty("e") val episode: Int? = null,
        @JsonProperty("e_title") val episodeTitle: String? = null,
    )

    private companion object {
        const val PAGE_SIZE = 60
        val jsonMapper = jacksonObjectMapper()
    }
}
