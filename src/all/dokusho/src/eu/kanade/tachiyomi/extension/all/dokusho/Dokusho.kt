package eu.kanade.tachiyomi.extension.all.dokusho

import android.content.SharedPreferences
import android.text.InputType
import androidx.preference.PreferenceScreen
import eu.kanade.tachiyomi.extension.all.dokusho.dto.ChapterDataResponseDto
import eu.kanade.tachiyomi.extension.all.dokusho.dto.ChapterListResponseDto
import eu.kanade.tachiyomi.extension.all.dokusho.dto.SerieDetailResponseDto
import eu.kanade.tachiyomi.extension.all.dokusho.dto.SerieListResponseDto
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.ConfigurableSource
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import keiyoushi.utils.getPreferencesLazy
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Dns
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.security.MessageDigest

class Dokusho : ConfigurableSource, HttpSource() {

    internal val preferences: SharedPreferences by getPreferencesLazy()

    private val displayName by lazy { preferences.getString(PREF_DISPLAY_NAME, "")!! }

    override val name by lazy {
        val displayNameSuffix = displayName.let { if (it.isNotBlank()) " ($it)" else "" }
        "Dokusho$displayNameSuffix"
    }

    override val lang = "all"

    override val baseUrl by lazy { preferences.getString(PREF_ADDRESS, "")!!.removeSuffix("/") }

    override val supportsLatest = true

    // Generate unique ID based on source name
    override val id by lazy {
        val key = "dokusho/all/$versionId"
        val bytes = MessageDigest.getInstance("MD5").digest(key.toByteArray())
        (0..7).map { bytes[it].toLong() and 0xff shl 8 * (7 - it) }.reduce(Long::or) and Long.MAX_VALUE
    }

    private val apiKey by lazy { preferences.getString(PREF_API_KEY, "")!! }

    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    override fun headersBuilder() = super.headersBuilder()
        .set("User-Agent", "TachiyomiDokusho/1.0")
        .also { builder ->
            if (apiKey.isNotBlank()) {
                builder.set("X-API-Key", apiKey)
            }
        }

    override val client: OkHttpClient =
        network.cloudflareClient.newBuilder()
            .dns(Dns.SYSTEM)
            .build()

    // Popular manga - sorted by most recently updated
    override fun popularMangaRequest(page: Int): Request =
        GET("$baseUrl/api/v1/serie?page=$page", headers)

    override fun popularMangaParse(response: Response): MangasPage {
        val result = response.parseAs<SerieListResponseDto>()
        val mangas = result.data.map { it.toSManga(baseUrl) }
        val hasNextPage = result.pagination.page < result.pagination.totalPages
        return MangasPage(mangas, hasNextPage)
    }

    // Latest updates
    override fun latestUpdatesRequest(page: Int): Request =
        GET("$baseUrl/api/v1/serie?page=$page", headers)

    override fun latestUpdatesParse(response: Response): MangasPage =
        popularMangaParse(response)

    // Search
    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val url = "$baseUrl/api/v1/serie".toHttpUrlOrNull()!!.newBuilder()
            .addQueryParameter("page", page.toString())
            .apply {
                if (query.isNotBlank()) {
                    addQueryParameter("q", query)
                }
            }
            .build()

        return GET(url, headers)
    }

    override fun searchMangaParse(response: Response): MangasPage =
        popularMangaParse(response)

    // Manga details
    override fun getMangaUrl(manga: SManga): String {
        // Extract serie ID from the URL
        val serieId = manga.url.removePrefix("/api/v1/serie/")
        return "$baseUrl/series/$serieId"
    }

    override fun mangaDetailsRequest(manga: SManga): Request =
        GET("$baseUrl${manga.url}", headers)

    override fun mangaDetailsParse(response: Response): SManga {
        val result = response.parseAs<SerieDetailResponseDto>()
        return result.toSManga(baseUrl)
    }

    // Chapter list
    override fun chapterListRequest(manga: SManga): Request {
        val serieId = manga.url.removePrefix("/api/v1/serie/")
        return GET("$baseUrl/api/v1/serie/$serieId/chapters", headers)
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        val result = response.parseAs<ChapterListResponseDto>()
        // Extract serie ID from the request URL
        val urlPath = response.request.url.encodedPath
        val serieId = urlPath.removePrefix("/api/v1/serie/").removeSuffix("/chapters")
        return result.chapters
            .filter { it.enabled }
            .map { it.toSChapter(serieId) }
            .sortedByDescending { it.chapter_number }
    }

    // Page list
    override fun pageListRequest(chapter: SChapter): Request =
        GET("$baseUrl${chapter.url}", headers)

    override fun pageListParse(response: Response): List<Page> {
        val result = response.parseAs<ChapterDataResponseDto>()
        return result.pages
            .filter { it.url != null && !it.permanentlyFailed }
            .map { page ->
                val imageUrl = if (page.url!!.startsWith("http")) {
                    page.url
                } else {
                    "$baseUrl${page.url}"
                }
                Page(page.index, imageUrl = imageUrl)
            }
    }

    override fun imageUrlParse(response: Response): String =
        throw UnsupportedOperationException()

    override fun imageRequest(page: Page): Request {
        return GET(page.imageUrl!!, headers = headersBuilder().add("Accept", "image/*,*/*;q=0.8").build())
    }

    // Preferences
    override fun setupPreferenceScreen(screen: PreferenceScreen) {
        screen.addEditTextPreference(
            title = "Source display name",
            default = "",
            summary = displayName.ifBlank { "Optional: Customize the displayed source name" },
            key = PREF_DISPLAY_NAME,
            restartRequired = true
        )
        screen.addEditTextPreference(
            title = "Server address",
            default = "",
            summary = baseUrl.ifBlank { "The Dokusho server address (e.g., https://dokusho.example.com)" },
            dialogMessage = "Enter the full URL of your Dokusho server. Do not include a trailing slash.",
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI,
            validate = { it.toHttpUrlOrNull() != null && !it.endsWith("/") },
            validationMessage = "The URL is invalid, malformed, or ends with a slash",
            key = PREF_ADDRESS,
            restartRequired = true
        )
        screen.addEditTextPreference(
            title = "API key",
            default = "",
            summary = if (apiKey.isBlank()) "Enter your Dokusho API key for authentication" else "*".repeat(apiKey.length.coerceAtMost(20)),
            dialogMessage = "Enter your API key. You can generate one in the Dokusho dashboard under your account settings.",
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
            key = PREF_API_KEY,
            restartRequired = true
        )
    }

    private inline fun <reified T> Response.parseAs(): T = use {
        json.decodeFromString<T>(body.string())
    }

    companion object {
        private const val PREF_DISPLAY_NAME = "Source display name"
        private const val PREF_ADDRESS = "Address"
        private const val PREF_API_KEY = "API key"
    }
}
