package it.fast4x.environment.requests

import it.fast4x.environment.Environment
import it.fast4x.environment.Environment.getBestQuality
import it.fast4x.environment.models.MusicCarouselShelfRenderer
import it.fast4x.environment.models.MusicResponsiveListItemRenderer
import it.fast4x.environment.models.MusicShelfRenderer
import it.fast4x.environment.models.MusicTwoRowItemRenderer
import it.fast4x.environment.models.NavigationEndpoint
import it.fast4x.environment.models.SectionListRenderer
import it.fast4x.environment.models.oddElements

data class ArtistSection(
    val title: String,
    val items: List<Environment.Item>,
    val moreEndpoint: NavigationEndpoint.Endpoint.Browse?,
)

data class ArtistPage(
    val artist: Environment.ArtistItem,
    val sections: List<ArtistSection>,
    val description: String?,
    val subscribers: String?,
    val shuffleEndpoint: NavigationEndpoint.Endpoint.Watch?,
    val radioEndpoint: NavigationEndpoint.Endpoint.Watch?,
) {
    companion object {
        fun fromSectionListRendererContent(content: SectionListRenderer.Content): ArtistSection? {
            return when {
                content.musicShelfRenderer != null -> fromMusicShelfRenderer(content.musicShelfRenderer)
                content.musicCarouselShelfRenderer != null -> fromMusicCarouselShelfRenderer(content.musicCarouselShelfRenderer)
                else -> null
            }
        }

        private fun fromMusicShelfRenderer(renderer: MusicShelfRenderer): ArtistSection? {
            return ArtistSection(
                title = renderer.title?.runs?.firstOrNull()?.text ?: "",
                items = renderer.contents?.mapNotNull {
                    it.musicResponsiveListItemRenderer?.let { it1 ->
                        fromMusicResponsiveListItemRenderer(
                            it1
                        )
                    }
                }?.ifEmpty { null } ?: return null,
                moreEndpoint = renderer.title?.runs?.firstOrNull()?.navigationEndpoint?.browseEndpoint
            )
        }

        private fun fromMusicCarouselShelfRenderer(renderer: MusicCarouselShelfRenderer): ArtistSection? {
            return ArtistSection(
                title = renderer.header?.musicCarouselShelfBasicHeaderRenderer?.title?.runs?.firstOrNull()?.text ?: return null,
                items = renderer.contents.mapNotNull {
                    it.musicTwoRowItemRenderer?.let { renderer ->
                        fromMusicTwoRowItemRenderer(renderer)
                    }
                }.ifEmpty { null } ?: return null,
                moreEndpoint = renderer.header.musicCarouselShelfBasicHeaderRenderer.moreContentButton?.buttonRenderer?.navigationEndpoint?.browseEndpoint
            )
        }

        private fun fromMusicResponsiveListItemRenderer(renderer: MusicResponsiveListItemRenderer): Environment.SongItem? {
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
                durationText = null,
                thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.getBestQuality()
                    ?: return null,
                explicit = renderer.badges?.find {
                    it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                } != null,
//                endpoint = renderer.overlay?.musicItemThumbnailOverlayRenderer?.content
//                    ?.musicPlayButtonRenderer?.playNavigationEndpoint?.watchEndpoint
            )
        }

        private fun fromMusicTwoRowItemRenderer(renderer: MusicTwoRowItemRenderer): Environment.Item? {
            return when {
                renderer.isSong -> {
                    Environment.SongItem(
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
                        album = null,
                        durationText = null,
                        thumbnail = renderer.thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails?.getBestQuality(),
                        explicit = renderer.subtitleBadges?.find {
                            it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                        } != null
                    )
                }

                renderer.isAlbum -> {
                    Environment.AlbumItem(
                        info = Environment.Info(
                            renderer.title?.runs?.firstOrNull()?.text,
                            renderer.navigationEndpoint?.browseEndpoint
                        ),
                        authors = null,
                        year = renderer.subtitle?.runs?.lastOrNull()?.text,
                        thumbnail = renderer.thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails?.getBestQuality(),
//                        explicit = renderer.subtitleBadges?.find {
//                            it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
//                        } != null
                    )
                }

                renderer.isPlaylist -> {
                    // Playlist from YouTube Music
                    Environment.PlaylistItem(
                        info = Environment.Info(
                            renderer.title?.runs?.firstOrNull()?.text,
                            renderer.navigationEndpoint?.browseEndpoint
                        ),
                        songCount = null,
                        thumbnail = renderer.thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails?.getBestQuality(),
                        channel = null,
                        isEditable = false
                    )
                }

                renderer.isArtist -> {
                    Environment.ArtistItem(
                        info = Environment.Info(
                            renderer.title?.runs?.firstOrNull()?.text,
                            renderer.navigationEndpoint?.browseEndpoint
                        ),
                        thumbnail = renderer.thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails?.getBestQuality(),
                        subscribersCountText = null,
                    )
                }

                else -> null
            }
        }
    }
}
