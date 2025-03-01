package it.fast4x.environment.models

import kotlinx.serialization.Serializable

@Serializable
data class MusicTwoRowItemRenderer(
    val navigationEndpoint: NavigationEndpoint?,
    val thumbnailRenderer: ThumbnailRenderer?,
    val title: Runs?,
    val subtitle: Runs?,
    val thumbnailOverlay: ThumbnailOverlay?,
    val aspectRatio: String? = null,
    val subtitleBadges: List<Badges>?,
    val menu: Menu?,
){
    val isPlaylist: Boolean
        get() = navigationEndpoint?.browseEndpoint?.browseEndpointContextSupportedConfigs?.browseEndpointContextMusicConfig?.pageType == "MUSIC_PAGE_TYPE_PLAYLIST"
    val isAlbum: Boolean
        get() = navigationEndpoint?.browseEndpoint?.browseEndpointContextSupportedConfigs?.browseEndpointContextMusicConfig?.pageType == "MUSIC_PAGE_TYPE_ALBUM" ||
                navigationEndpoint?.browseEndpoint?.browseEndpointContextSupportedConfigs?.browseEndpointContextMusicConfig?.pageType == "MUSIC_PAGE_TYPE_AUDIOBOOK"
    val isArtist: Boolean
        get() = navigationEndpoint?.browseEndpoint?.browseEndpointContextSupportedConfigs?.browseEndpointContextMusicConfig?.pageType == "MUSIC_PAGE_TYPE_ARTIST"
    val isVideo: Boolean
        get() = navigationEndpoint?.watchEndpoint?.watchEndpointMusicSupportedConfigs
            ?.watchEndpointMusicConfig
            ?.musicVideoType in listOf("MUSIC_VIDEO_TYPE_ATV", "MUSIC_VIDEO_TYPE_UGC", "MUSIC_VIDEO_TYPE_OMV")
    val isSong: Boolean
        get() =
            navigationEndpoint?.watchEndpoint != null &&
                    (
                            if (aspectRatio != null) {
                                aspectRatio != "MUSIC_TWO_ROW_ITEM_THUMBNAIL_ASPECT_RATIO_RECTANGLE_16_9"
                            } else {
                                val thumbnail =
                                    thumbnailRenderer?.musicThumbnailRenderer
                                        ?.thumbnail
                                        ?.thumbnails
                                        ?.firstOrNull()
                                thumbnail != null && thumbnail.height == thumbnail.width
                            }
                            )

//    val isVideo: Boolean
//        get() = navigationEndpoint?.watchEndpoint?.watchEndpointMusicSupportedConfigs
//            ?.watchEndpointMusicConfig
//            ?.musicVideoType in listOf("MUSIC_VIDEO_TYPE_ATV", "MUSIC_VIDEO_TYPE_UGC", "MUSIC_VIDEO_TYPE_OMV")
//                && (if (aspectRatio != null) aspectRatio == "MUSIC_TWO_ROW_ITEM_THUMBNAIL_ASPECT_RATIO_RECTANGLE_16_9" else {
//            val thumbnail =
//                thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails?.firstOrNull()
//            thumbnail != null && thumbnail.height != thumbnail.width
//        })
//    val isSong: Boolean
//        get() = navigationEndpoint == null || navigationEndpoint.watchEndpoint != null || navigationEndpoint.watchPlaylistEndpoint != null

//********************************

//    val isSong: Boolean
//        get() =
//            navigationEndpoint?.watchEndpoint != null &&
//                    (
//                            if (aspectRatio != null) {
//                                aspectRatio != "MUSIC_TWO_ROW_ITEM_THUMBNAIL_ASPECT_RATIO_RECTANGLE_16_9"
//                            } else {
//                                val thumbnail =
//                                    thumbnailRenderer?.musicThumbnailRenderer
//                                        ?.thumbnail
//                                        ?.thumbnails
//                                        ?.firstOrNull()
//                                thumbnail != null && thumbnail.height == thumbnail.width
//                            }
//                            )
//    val isPlaylist: Boolean
//        get() =
//            navigationEndpoint?.browseEndpoint
//                ?.browseEndpointContextSupportedConfigs
//                ?.browseEndpointContextMusicConfig
//                ?.pageType ==
//                    MUSIC_PAGE_TYPE_PLAYLIST &&
//                    aspectRatio != "MUSIC_TWO_ROW_ITEM_THUMBNAIL_ASPECT_RATIO_RECTANGLE_16_9"
//    val isAlbum: Boolean
//        get() =
//            navigationEndpoint?.browseEndpoint
//                ?.browseEndpointContextSupportedConfigs
//                ?.browseEndpointContextMusicConfig
//                ?.pageType ==
//                    MUSIC_PAGE_TYPE_ALBUM ||
//                    navigationEndpoint?.browseEndpoint
//                        ?.browseEndpointContextSupportedConfigs
//                        ?.browseEndpointContextMusicConfig
//                        ?.pageType == MUSIC_PAGE_TYPE_AUDIOBOOK
//    val isArtist: Boolean
//        get() =
//            navigationEndpoint?.browseEndpoint
//                ?.browseEndpointContextSupportedConfigs
//                ?.browseEndpointContextMusicConfig
//                ?.pageType == MUSIC_PAGE_TYPE_ARTIST
//    val isVideo: Boolean
//        get() =
//            navigationEndpoint?.watchEndpoint != null &&
//                    (
//                            if (aspectRatio != null) {
//                                aspectRatio == "MUSIC_TWO_ROW_ITEM_THUMBNAIL_ASPECT_RATIO_RECTANGLE_16_9"
//                            } else {
//                                val thumbnail =
//                                    thumbnailRenderer?.musicThumbnailRenderer
//                                        ?.thumbnail
//                                        ?.thumbnails
//                                        ?.firstOrNull()
//                                thumbnail != null && thumbnail.height != thumbnail.width
//                            }
//                            )
}
