package it.fast4x.environment.requests

import it.fast4x.environment.Environment
import it.fast4x.environment.Environment.getBestQuality
import it.fast4x.environment.models.MusicResponsiveListItemRenderer
import it.fast4x.environment.models.MusicTwoRowItemRenderer
import it.fast4x.environment.models.NavigationEndpoint
import it.fast4x.environment.models.oddElements

data class ArtistItemsPage(
    val title: String,
    val items: List<Environment.Item>,
    val continuation: String?,
) {
    companion object {
        fun fromMusicResponsiveListItemRenderer(renderer: MusicResponsiveListItemRenderer): Environment.SongItem? {
            return Environment.SongItem(
                info = Environment.Info(
                    name = renderer.flexColumns.firstOrNull()
                        ?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()
                        ?.text ?: "",
                    endpoint = NavigationEndpoint.Endpoint.Watch(
                        videoId = renderer.playlistItemData?.videoId
                    )
                ),
                authors = renderer.flexColumns.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.oddElements()
                    ?.map {
                        Environment.Info(
                            name = it.text,
                            endpoint = it.navigationEndpoint?.browseEndpoint
                        )
                    } ?: emptyList(),
                album = renderer.flexColumns.getOrNull(3)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()
                    ?.let {
                        Environment.Info(
                            name = it.text,
                            endpoint = it.navigationEndpoint?.browseEndpoint
                        )
                    },
                durationText = renderer.fixedColumns?.firstOrNull()
                    ?.musicResponsiveListItemFlexColumnRenderer?.text
                    ?.runs?.firstOrNull()
                    ?.text,
                thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.getBestQuality()
                    ?: return null,
                explicit = renderer.badges?.find {
                    it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                } != null,
                //endpoint = renderer.overlay?.musicItemThumbnailOverlayRenderer?.content?.musicPlayButtonRenderer?.playNavigationEndpoint?.watchEndpoint
            )
        }

        fun fromMusicTwoRowItemRenderer(renderer: MusicTwoRowItemRenderer): Environment.Item? {
            return when {
                renderer.isAlbum -> Environment.AlbumItem(
                    info = Environment.Info(
                        renderer.title?.runs?.firstOrNull()?.text,
                        renderer.navigationEndpoint?.browseEndpoint
                    ),
                    authors = null,
                    year = renderer.subtitle?.runs?.lastOrNull()?.text,
                    thumbnail = renderer.thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails?.getBestQuality(),
//                    explicit = renderer.subtitleBadges?.find {
//                        it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
//                    } != null
                )
                // Video
                renderer.isSong -> Environment.VideoItem(
                    info = Environment.Info(
                        renderer.title?.runs?.firstOrNull()?.text,
                        renderer.navigationEndpoint?.watchEndpoint
                    ),
                    authors = renderer.subtitle?.runs?.map {
                        Environment.Info(
                            name = it.text,
                            endpoint = it.navigationEndpoint?.browseEndpoint
                        )
                    },
                    durationText = null,
                    thumbnail = renderer.thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails?.getBestQuality(),
                    viewsText =null,
                )
                renderer.isPlaylist -> Environment.PlaylistItem(
                    info = Environment.Info(
                        renderer.title?.runs?.firstOrNull()?.text,
                        renderer.navigationEndpoint?.browseEndpoint
                    ),
                    songCount = renderer.subtitle?.runs?.getOrNull(4)?.text?.toInt(),
                    thumbnail = renderer.thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails?.getBestQuality(),
                    channel = null,
                    isEditable = false
                )
                else -> null
            }
        }
    }
}
