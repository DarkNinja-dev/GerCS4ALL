package com.devmonkeyops.gercs4all

import android.content.Context
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
import com.lagradost.cloudstream3.newMovieSearchResponse
import com.lagradost.cloudstream3.newTvSeriesLoadResponse
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.newExtractorLink

/**
 * Official Warner Bros. Discovery catalogue endpoints. The upstream provider
 * only used one image field; these providers use the catalogue image and the
 * metadata image as fallbacks, so DMAX, TELE 5 and TLC consistently expose
 * cover art when either is present.
 */
@CloudstreamPlugin
class GerCS4ALLMediathekenPlugin : Plugin() {
    override fun load(context: Context) {
        registerMainAPI(DmaxMediathek())
        registerMainAPI(Tele5Mediathek())
        registerMainAPI(TlcMediathek())
    }
}

private open class DiscoveryMediathek : MainAPI() {
    override var lang = "de"
    override val hasMainPage = true
    override val hasQuickSearch = true
    override var name = "DMAX"
    override var mainUrl = "https://dmax.de"
    private val apiUrl = "https://public.aurora.enhanced.live"

    protected open val serviceIdentifier = "dmaxde"
    protected open val mediathekSlug = "sendungen"

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val response = app.get(
            "$apiUrl/site/page/homepage/?filter[environment]=$serviceIdentifier&v=2&include=default",
        ).parsed<MediaResult>()
        val pages = response.blocks.mapNotNull { block ->
            val items = block.items.filter { it.pageType == "showpage" }.map { it.toSearchResponse() }
            items.takeIf { it.isNotEmpty() }?.let { HomePageList(block.title.orEmpty(), it) }
        }
        return newHomePageResponse(pages, hasNext = false)
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun search(query: String): List<SearchResponse> {
        val response = app.get(
            "$apiUrl/site/search/page/?q=$query&filter[environment]=$serviceIdentifier&filter[type]=showpage&page[size]=60&v=2&include=default",
        ).parsed<SearchRoot>()
        return response.data.map { it.toSearchResponse() }
    }

    private fun MediaResult.toSearchResponse(): SearchResponse = newMovieSearchResponse(
        name = title.orEmpty(),
        url = "$mainUrl/$mediathekSlug/$slug",
        type = TvType.Movie,
    ) {
        this.posterUrl = image?.url ?: metaMedia.firstOrNull()?.media?.url
        this.year = datePublished?.take(4)?.toIntOrNull()
    }

    private fun EpisodeInfo.toSearchResponse(): SearchResponse = newMovieSearchResponse(
        name = title.orEmpty(),
        url = fixUrl(link?.url ?: "/$mediathekSlug/${alternateId ?: url.orEmpty()}"),
        type = TvType.Movie,
    ) {
        this.posterUrl = poster?.src ?: image?.url
    }

    override suspend fun load(url: String): LoadResponse? {
        val slug = url.removeSuffix("/").substringAfterLast("/")
        val response = app.get(
            "$apiUrl/site/page/$slug/?filter[environment]=$serviceIdentifier&parent_slug=$mediathekSlug&v=2",
        ).parsed<MediaResult>()
        val seriesBlock = response.blocks.firstOrNull { it.showId != null }
        val poster = response.image?.url ?: response.metaMedia.firstOrNull()?.media?.url

        if (seriesBlock != null) {
            val episodes = seriesBlock.items.mapNotNull { episode ->
                episode.id?.let { id ->
                    newEpisode(id) {
                        season = episode.seasonNumber?.toInt()
                        this.episode = episode.episodeNumber?.toInt()
                        name = episode.title
                        description = episode.description
                        runTime = episode.videoDuration?.div(60_000)?.toInt()
                        posterUrl = episode.poster?.src ?: episode.image?.url ?: poster
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
        val token = app.get("$apiUrl/token?realm=de").parsed<TokenResponse>().data?.attributes?.token ?: return false
        val response = app.post(
            "$apiUrl/playback/v3/videoPlaybackInfo",
            headers = mapOf("Authorization" to "Bearer $token"),
            json = VideoPlaybackRequest(videoId),
        ).parsed<VideoPlaybackResponse>()
        response.data?.attributes?.streaming.orEmpty().forEach { source ->
            callback(newExtractorLink(serviceIdentifier, name, source.url ?: return@forEach))
        }
        return response.data?.attributes?.streaming?.isNotEmpty() == true
    }

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
    )
    private data class Image(val url: String? = null)
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
        val pageType: String? = null,
    )
    private data class Link(val url: String? = null)
    private data class Poster(val src: String? = null)
    private data class VideoPlaybackRequest(
        val videoId: String,
        val deviceInfo: DeviceInfo = DeviceInfo(),
        val visteriaProperties: Map<String, String> = emptyMap(),
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
    private data class VideoAttributes(val streaming: List<Streaming> = emptyList())
    private data class Streaming(val url: String? = null)
}

private class DmaxMediathek : DiscoveryMediathek()
private class Tele5Mediathek : DiscoveryMediathek() {
    override var name = "TELE 5"
    override var mainUrl = "https://tele5.de"
    override val serviceIdentifier = "tele5"
    override val mediathekSlug = "mediathek"
}
private class TlcMediathek : DiscoveryMediathek() {
    override var name = "TLC"
    override var mainUrl = "https://tlc.de"
    override val serviceIdentifier = "tlcde"
}
