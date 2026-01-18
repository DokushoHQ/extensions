package eu.kanade.tachiyomi.extension.all.dokusho.dto

import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaginationDto(
    val page: Int,
    val pageSize: Int,
    val total: Int,
    val totalPages: Int
)

@Serializable
data class SerieListResponseDto(
    val data: List<SerieDto>,
    val pagination: PaginationDto
)

@Serializable
data class GenreDto(
    val id: String,
    val title: String
)

@Serializable
data class PersonDto(
    val id: String,
    val name: String
)

@Serializable
data class SourceInfoDto(
    val id: String,
    @SerialName("external_id")
    val externalId: String,
    val name: String
)

@Serializable
data class SerieSourceDto(
    val id: String,
    @SerialName("external_id")
    val externalId: String,
    @SerialName("is_primary")
    val isPrimary: Boolean,
    @SerialName("consecutive_failures")
    val consecutiveFailures: Int = 0,
    val source: SourceInfoDto
)

@Serializable
data class ChapterCountDto(
    val chapters: Int = 0
)

@Serializable
data class SerieDto(
    val id: String,
    val title: String,
    val synopsis: String? = null,
    val cover: String? = null,
    val type: String? = null,
    val status: List<String> = emptyList(),
    @SerialName("updated_at")
    val updatedAt: String? = null,
    val genres: List<GenreDto> = emptyList(),
    val authors: List<PersonDto> = emptyList(),
    val artists: List<PersonDto> = emptyList(),
    val sources: List<SerieSourceDto> = emptyList(),
    @SerialName("_count")
    val count: ChapterCountDto? = null
) {
    fun toSManga(baseUrl: String): SManga = SManga.create().apply {
        url = "/api/v1/serie/$id"
        title = this@SerieDto.title
        thumbnail_url = cover?.let { if (it.startsWith("http")) it else "$baseUrl$it" }
        description = synopsis
        author = authors.joinToString { it.name }
        artist = artists.joinToString { it.name }
        genre = genres.joinToString { it.title }
        status = when {
            this@SerieDto.status.contains("Completed") || this@SerieDto.status.contains("Scanlated") -> SManga.COMPLETED
            this@SerieDto.status.contains("Ongoing") || this@SerieDto.status.contains("Scanlating") -> SManga.ONGOING
            this@SerieDto.status.contains("Hiatus") -> SManga.ON_HIATUS
            this@SerieDto.status.contains("Canceled") -> SManga.CANCELLED
            else -> SManga.UNKNOWN
        }
    }
}

@Serializable
data class SerieDetailResponseDto(
    val id: String,
    val title: String,
    val synopsis: String? = null,
    val cover: String? = null,
    val type: String? = null,
    val status: List<String> = emptyList(),
    @SerialName("updated_at")
    val updatedAt: String? = null,
    val genres: List<GenreDto> = emptyList(),
    val authors: List<PersonDto> = emptyList(),
    val artists: List<PersonDto> = emptyList(),
    val sources: List<SerieSourceDto> = emptyList(),
    @SerialName("_count")
    val count: ChapterCountDto? = null
) {
    fun toSManga(baseUrl: String): SManga = SManga.create().apply {
        url = "/api/v1/serie/$id"
        title = this@SerieDetailResponseDto.title
        thumbnail_url = cover?.let { if (it.startsWith("http")) it else "$baseUrl$it" }
        description = synopsis
        author = authors.joinToString { it.name }
        artist = artists.joinToString { it.name }
        genre = genres.joinToString { it.title }
        status = when {
            this@SerieDetailResponseDto.status.contains("Completed") || this@SerieDetailResponseDto.status.contains("Scanlated") -> SManga.COMPLETED
            this@SerieDetailResponseDto.status.contains("Ongoing") || this@SerieDetailResponseDto.status.contains("Scanlating") -> SManga.ONGOING
            this@SerieDetailResponseDto.status.contains("Hiatus") -> SManga.ON_HIATUS
            this@SerieDetailResponseDto.status.contains("Canceled") -> SManga.CANCELLED
            else -> SManga.UNKNOWN
        }
    }
}

@Serializable
data class ScanlationGroupDto(
    val id: String,
    val name: String,
    val url: String? = null
)

@Serializable
data class ChapterDto(
    val id: String,
    @SerialName("serie_id")
    val serieId: String,
    @SerialName("source_id")
    val sourceId: String,
    @SerialName("external_id")
    val externalId: String,
    val title: String? = null,
    @SerialName("chapter_number")
    val chapterNumber: Float,
    @SerialName("volume_number")
    val volumeNumber: Int? = null,
    @SerialName("volume_name")
    val volumeName: String? = null,
    val language: String,
    @SerialName("date_upload")
    val dateUpload: String,
    @SerialName("external_url")
    val externalUrl: String? = null,
    val enabled: Boolean = true,
    @SerialName("page_fetch_status")
    val pageFetchStatus: String? = null,
    val groups: List<ScanlationGroupDto> = emptyList(),
    val source: SourceInfoDto? = null
) {
    fun toSChapter(serieId: String): SChapter = SChapter.create().apply {
        url = "/api/v1/serie/$serieId/chapters/${this@ChapterDto.id}/data"
        name = buildString {
            volumeNumber?.let { append("Vol. $it ") }
            append("Ch. $chapterNumber")
            title?.let { if (it.isNotBlank()) append(" - $it") }
        }
        chapter_number = chapterNumber
        date_upload = try {
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                .parse(dateUpload.substringBefore(".").substringBefore("Z"))?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
        scanlator = groups.joinToString { it.name }
    }
}

@Serializable
data class ChapterListResponseDto(
    val chapters: List<ChapterDto>
)

@Serializable
data class PageDataDto(
    val index: Int,
    val type: String,
    val url: String? = null,
    val content: String? = null,
    @SerialName("image_quality")
    val imageQuality: String? = null,
    @SerialName("metadata_issues")
    val metadataIssues: List<String>? = null,
    @SerialName("permanently_failed")
    val permanentlyFailed: Boolean = false
)

@Serializable
data class ChapterDataResponseDto(
    val pages: List<PageDataDto>,
    val hasData: Boolean
)
