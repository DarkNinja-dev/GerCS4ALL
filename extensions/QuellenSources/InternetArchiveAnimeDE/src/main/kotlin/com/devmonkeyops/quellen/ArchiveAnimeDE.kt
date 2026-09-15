package com.darkninja.quellen

import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.DubStatus
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageData
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.addEpisodes
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.mainPageOf
import com.lagradost.cloudstream3.newAnimeLoadResponse
import com.lagradost.cloudstream3.newAnimeSearchResponse
import com.lagradost.cloudstream3.newEpisode
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.newExtractorLink
import java.net.URLEncoder

@CloudstreamPlugin
class InternetArchiveAnimeDEPlugin : BasePlugin() {
    override fun load() {
        registerMainAPI(ArchiveAnimeDE())
    }
}

/** A separate anime-only package keeps Archive anime out of movie and series profiles. */
private class ArchiveAnimeDE : MainAPI() {
    override var mainUrl = "https://archive.org"
    override var name = "Internet Archive – Anime DE"
    override var lang = "de"
    override val hasQuickSearch = true
    override val hasMainPage = true
    override val supportedTypes = setOf(TvType.Anime, TvType.AnimeMovie, TvType.OVA)

    override val mainPage: List<MainPageData> = mainPageOf(
        "new" to "Neue Anime im Internet Archive",
        "popular" to "Beliebte Anime im Internet Archive",
        "recent" to "Kürzlich archivierte Anime",
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val sort = when (request.data) {
            "popular" -> "downloads desc"
            "recent" -> "addeddate desc"
            else -> "publicdate desc"
        }
        val results = searchArchive(BASE_QUERY, sort, page)
        return newHomePageResponse(request.name, results.map { it.toSearchResponse() }, results.size >= PAGE_SIZE)
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun search(query: String): List<SearchResponse> {
        val safeQuery = query.replace('"', ' ').trim()
        if (safeQuery.isBlank()) return emptyList()
        return searchArchive(
            "$BASE_QUERY AND (title:($safeQuery) OR description:($safeQuery))",
            "downloads desc",
            1,
        ).map { it.toSearchResponse() }
    }

    override suspend fun load(url: String): LoadResponse? {
        val item = metadata(url)
        val title = item.metadata.title.textOrNull() ?: return null
        return newAnimeLoadResponse(title, url, TvType.AnimeMovie) {
            posterUrl = poster(url)
            plot = item.metadata.description.textOrNull()
            year = item.metadata.year.textOrNull()?.toIntOrNull()
            tags = item.metadata.subject.textList()
            addEpisodes(DubStatus.Dubbed, listOf(newEpisode(url) { name = "Film"; episode = 1 }))
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit,
    ): Boolean {
        val files = metadata(data).files.filter { it.isPublicVideo() }
        files.forEach { file ->
            callback(newExtractorLink(name, file.name, downloadUrl(data, file.name)) {
                referer = "$mainUrl/details/$data"
            })
        }
        return files.isNotEmpty()
    }

    private suspend fun searchArchive(query: String, sort: String, page: Int): List<ArchiveDocument> {
        val url = buildString {
            append("$mainUrl/advancedsearch.php?q=")
            append(encode(query))
            append("&fl[]=identifier&fl[]=title&fl[]=description&fl[]=year&fl[]=subject")
            append("&rows=$PAGE_SIZE&page=$page&output=json&sort[]=")
            append(encode(sort))
        }
        return app.get(url).parsed<ArchiveSearchEnvelope>().response.docs
    }

    private suspend fun metadata(identifier: String): ArchiveMetadataEnvelope =
        app.get("$mainUrl/metadata/${encode(identifier)}").parsed()

    private fun ArchiveDocument.toSearchResponse(): SearchResponse = newAnimeSearchResponse(
        title.textOrNull() ?: identifier,
        identifier,
        TvType.AnimeMovie,
    ) {
        posterUrl = poster(identifier)
        year = year.textOrNull()?.toIntOrNull()
    }

    private fun ArchiveFile.isPublicVideo(): Boolean {
        if (privateFile?.equals("true", ignoreCase = true) == true) return false
        return name.substringAfterLast('.', "").lowercase() in VIDEO_EXTENSIONS
    }

    private fun poster(identifier: String) = "$mainUrl/services/img/${encode(identifier)}"
    private fun downloadUrl(identifier: String, filename: String) =
        "$mainUrl/download/${encode(identifier)}/${encode(filename)}"
    private fun encode(value: String) = URLEncoder.encode(value, "UTF-8").replace("+", "%20")

    private companion object {
        const val PAGE_SIZE = 60
        // Matches the same broad catalogue query exposed by the requested
        // Archive.org "anime deutsch" collection search.  Language metadata
        // alone is much too sparse there and would reduce the catalogue to a
        // single unrelated item.
        const val BASE_QUERY = "mediatype:movies AND anime AND deutsch"
        val VIDEO_EXTENSIONS = setOf("mp4", "m4v", "mkv", "webm", "avi", "mov", "ogv")
    }
}

private data class ArchiveSearchEnvelope(val response: ArchiveSearchResponse = ArchiveSearchResponse())
private data class ArchiveSearchResponse(val docs: List<ArchiveDocument> = emptyList())
private data class ArchiveDocument(
    val identifier: String = "",
    val title: Any? = null,
    val description: Any? = null,
    val year: Any? = null,
    val subject: Any? = null,
)
private data class ArchiveMetadataEnvelope(
    val metadata: ArchiveItemMetadata = ArchiveItemMetadata(),
    val files: List<ArchiveFile> = emptyList(),
)
private data class ArchiveItemMetadata(
    val title: Any? = null,
    val description: Any? = null,
    val year: Any? = null,
    val subject: Any? = null,
)
private data class ArchiveFile(
    val name: String = "",
    @JsonProperty("privatefile") val privateFile: String? = null,
)
private fun Any?.textOrNull(): String? = when (this) {
    null -> null
    is Collection<*> -> joinToString(" ") { it.textOrNull().orEmpty() }.trim().ifBlank { null }
    else -> toString().trim().ifBlank { null }
}
private fun Any?.textList(): List<String> = when (this) {
    is Collection<*> -> mapNotNull { it.textOrNull() }
    else -> textOrNull()?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }.orEmpty()
}
