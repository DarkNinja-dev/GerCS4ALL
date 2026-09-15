package com.darkninja.quellen

import com.lagradost.cloudstream3.HomePageList
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.fixUrl
import com.lagradost.cloudstream3.newEpisode
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.newMovieLoadResponse
import com.lagradost.cloudstream3.newTvSeriesLoadResponse
import com.lagradost.cloudstream3.newTvSeriesSearchResponse
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.newExtractorLink

/**
 * Official Warner Bros. Discovery catalogue endpoints. The upstream provider
 * only used one image field; these providers use the catalogue image and the
 * metadata image as fallbacks, so DMAX, TELE 5 and TLC consistently expose
 * cover art when either is present.
 */
open class DiscoveryMediathek : MainAPI() {
    override var lang = "de"
    override val hasMainPage = true
    override val hasQuickSearch = true
    override var name = "DMAX"
    override var mainUrl = "https://dmax.de"
    private val apiUrl = "https://public.aurora.enhanced.live"
    private val playbackApiUrl = "https://eu1-prod.disco-api.com"

    protected open val serviceIdentifier = "dmaxde"
    protected open val mediathekSlug = "sendungen"
    /** The playback realm is not always the same as the CMS environment. */
    protected open val playbackRealm = "dmaxde"

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val response = app.get(
            "$apiUrl/site/page/homepage/?filter[environment]=$serviceIdentifier&v=2&include=default",
        ).parsed<MediaResult>()
        val pages = response.blocks.mapNotNull { block ->
            val items = block.items
                .filter { it.pageType.isNullOrBlank() || it.pageType == "showpage" }
                .mapNotNull { it.toSearchResponse() }
            items.takeIf { it.isNotEmpty() }?.let { HomePageList(block.title.orEmpty(), it) }
        }
        return newHomePageResponse(pages, hasNext = false)
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun search(query: String): List<SearchResponse> {
        val response = app.get(
            "$apiUrl/site/search/page/?q=$query&filter[environment]=$serviceIdentifier&filter[type]=showpage&page[size]=60&v=2&include=default",
        ).parsed<SearchRoot>()
        return response.data.mapNotNull { it.toSearchResponse() }
    }

    private fun MediaResult.toSearchResponse(): SearchResponse? {
        val destination = link?.url?.takeIf { it.isNotBlank() }
            ?: slug?.takeIf { it.isNotBlank() }?.let { "$mainUrl/$mediathekSlug/$it" }
            ?: return null
        return newTvSeriesSearchResponse(title.orEmpty(), fixUrl(destination), TvType.TvSeries, false) {
            posterUrl = posterUrl()
            year = datePublished?.take(4)?.toIntOrNull()
        }
    }

    private fun EpisodeInfo.toSearchResponse(): SearchResponse? {
        val destination = link?.url?.takeIf { it.isNotBlank() }
            ?: slug?.takeIf { it.isNotBlank() }?.let { "$mainUrl/$mediathekSlug/$it" }
            ?: alternateId?.takeIf { it.isNotBlank() }?.let { "/$mediathekSlug/$it" }
            ?: url?.takeIf { it.isNotBlank() }
            ?: return null
        return newTvSeriesSearchResponse(title.orEmpty(), fixUrl(destination), TvType.TvSeries, false) {
            posterUrl = poster?.urlOrNull() ?: image?.urlOrNull()
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val slug = url.removeSuffix("/").substringAfterLast("/")
        val response = app.get(
            "$apiUrl/site/page/$slug/?filter[environment]=$serviceIdentifier&parent_slug=$mediathekSlug&v=2",
        ).parsed<MediaResult>()
        val seriesBlock = response.blocks.firstOrNull { it.showId != null }
        val poster = response.posterUrl()

        if (seriesBlock != null) {
            val episodes = seriesBlock.items.mapNotNull { episode ->
                episode.id?.let { id ->
                    newEpisode(id) {
                        season = episode.seasonNumber?.toInt()
                        this.episode = episode.episodeNumber?.toInt()
                        name = episode.title
                        description = episode.description
                        runTime = episode.videoDuration?.div(60_000)?.toInt()
                        posterUrl = episode.poster?.urlOrNull() ?: episode.image?.urlOrNull() ?: poster
                    }
                }
            }
            return newTvSeriesLoadResponse(response.title.orEmpty(), url, TvType.TvSeries, episodes) {
                posterUrl = poster
                year = response.datePublished?.take(4)?.toIntOrNull()
                plot = response.description ?: response.metaDescription
                tags = response.taxonomies.mapNotNull { it.title }
            }
        }

        val videoId = response.blocks.firstOrNull { it.videoId != null && it.title == response.title }?.videoId
            ?: response.blocks.firstOrNull { it.videoId != null }?.videoId
        return newMovieLoadResponse(response.title.orEmpty(), url, TvType.Movie, videoId.orEmpty()) {
            posterUrl = poster
            year = response.datePublished?.take(4)?.toIntOrNull()
            plot = response.description ?: response.metaDescription
            tags = response.taxonomies.mapNotNull { it.title }
            comingSoon = videoId == null
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit,
    ): Boolean {
        val videoId = data
        if (videoId.isBlank()) return false
        val requestHeaders = mapOf(
            "Referer" to "$mainUrl/",
            "x-disco-params" to "realm=$playbackRealm",
            "x-disco-client" to "Alps:HyogaPlayer:0.0.0",
        )
        val token = app.get(
            "$playbackApiUrl/token?realm=$playbackRealm",
            headers = requestHeaders,
        ).parsed<TokenResponse>().data?.attributes?.token ?: return false
        val response = app.post(
            "$playbackApiUrl/playback/v3/videoPlaybackInfo",
            headers = requestHeaders + ("Authorization" to "Bearer $token"),
            json = VideoPlaybackRequest(videoId),
        ).parsed<VideoPlaybackResponse>()
        response.data?.attributes?.streaming.orEmpty().forEach { (format, source) ->
            val streamUrl = source.url ?: return@forEach
            callback(newExtractorLink(serviceIdentifier, "$name ($format)", streamUrl) {
                referer = "$mainUrl/"
            })
        }
        return response.data?.attributes?.streaming?.isNotEmpty() == true
    }

    private fun MediaResult.posterUrl(): String? = image?.urlOrNull()
        ?: metaMedia.asSequence().mapNotNull { it.media?.urlOrNull() }.firstOrNull()

    private fun Image.urlOrNull(): String? = url?.takeIf { it.isNotBlank() }
        ?: src?.takeIf { it.isNotBlank() }

    private data class TokenResponse(val data: TokenData? = null)
    private data class TokenData(val attributes: TokenAttributes? = null)
    private data class TokenAttributes(val token: String? = null)
    private data class SearchRoot(val data: List<MediaResult> = emptyList())
    private data class MediaResult(
        val title: String? = null,
        val datePublished: String? = null,
        val slug: String? = null,
        val description: String? = null,
        val metaDescription: String? = null,
        val taxonomies: List<Taxonomy> = emptyList(),
        val image: Image? = null,
        val metaMedia: List<MediaItem> = emptyList(),
        val blocks: List<Block> = emptyList(),
        val link: Link? = null,
    )
    private data class Image(
        val url: String? = null,
        val src: String? = null,
    )
    private data class Taxonomy(val title: String? = null)
    private data class MediaItem(val media: Image? = null)
    private data class Block(
        val title: String? = null,
        val videoId: String? = null,
        val showId: String? = null,
        val items: List<EpisodeInfo> = emptyList(),
    )
    private data class EpisodeInfo(
        val id: String? = null,
        val alternateId: String? = null,
        val description: String? = null,
        val videoDuration: Long? = null,
        val episodeNumber: Long? = null,
        val seasonNumber: Long? = null,
        val title: String? = null,
        val poster: Poster? = null,
        val image: Image? = null,
        val link: Link? = null,
        val url: String? = null,
        val slug: String? = null,
        val pageType: String? = null,
    )
    private data class Link(val url: String? = null)
    private data class Poster(
        val src: String? = null,
        val url: String? = null,
    ) {
        fun urlOrNull(): String? = url?.takeIf { it.isNotBlank() }
            ?: src?.takeIf { it.isNotBlank() }
    }
    private data class VideoPlaybackRequest(
        val videoId: String,
        val deviceInfo: DeviceInfo = DeviceInfo(),
        val wisteriaProperties: Map<String, String> = emptyMap(),
    )
    private data class DeviceInfo(
        val adBlocker: Boolean = false,
        val drmSupported: Boolean = false,
        val hdrCapabilities: List<String> = listOf("SDR"),
        val hwDecodingCapabilities: List<String> = emptyList(),
        val soundCapabilities: List<String> = listOf("STEREO"),
    )
    private data class VideoPlaybackResponse(val data: VideoInfoData? = null)
    private data class VideoInfoData(val attributes: VideoAttributes? = null)
    private data class VideoAttributes(val streaming: Map<String, Streaming> = emptyMap())
    private data class Streaming(val url: String? = null)
}

class DmaxMediathek : DiscoveryMediathek()
class Tele5Mediathek : DiscoveryMediathek() {
    override var name = "TELE 5"
    override var mainUrl = "https://tele5.de"
    override val serviceIdentifier = "tele5"
    override val mediathekSlug = "mediathek"
    override val playbackRealm = "dmaxde"
}
class TlcMediathek : DiscoveryMediathek() {
    override var name = "TLC"
    override var mainUrl = "https://tlc.de"
    override val serviceIdentifier = "tlcde"
    override val playbackRealm = "tlcde"
}
