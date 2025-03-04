package it.fast4x.environment.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ThumbnailRenderer(
    @JsonNames("croppedSquareThumbnailRenderer")
    val musicThumbnailRenderer: MusicThumbnailRenderer?,
    val croppedSquareThumbnailRenderer: MusicThumbnailRenderer?,
) {
    @Serializable
    data class MusicThumbnailRenderer(
        val thumbnail: Thumbnail?,
        val thumbnailCrop: String?,
        val thumbnailScale: String?,
    ) {
        @Serializable
        data class Thumbnail(
            val thumbnails: List<it.fast4x.environment.models.Thumbnail>?
        )

        fun getThumbnailUrl() = thumbnail?.thumbnails?.lastOrNull()?.url
    }
}


@Serializable
data class ThumbnailOverlay(
    val musicItemThumbnailOverlayRenderer: MusicItemThumbnailOverlayRenderer,
) {
    @Serializable
    data class MusicItemThumbnailOverlayRenderer(
        val content: Content,
    ) {
        @Serializable
        data class Content(
            val musicPlayButtonRenderer: MusicPlayButtonRenderer,
        ) {
            @Serializable
            data class MusicPlayButtonRenderer(
                val playNavigationEndpoint: NavigationEndpoint?
            )
        }
    }
}
