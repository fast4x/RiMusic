package it.fast4x.innertube.requests

import it.fast4x.innertube.Innertube
import it.fast4x.innertube.models.MusicCarouselShelfRenderer
import it.fast4x.innertube.models.MusicResponsiveListItemRenderer
import it.fast4x.innertube.models.MusicShelfRenderer
import it.fast4x.innertube.models.MusicTwoRowItemRenderer
import it.fast4x.innertube.models.NavigationEndpoint
import it.fast4x.innertube.models.SectionListRenderer
import it.fast4x.innertube.models.oddElements

data class ArtistSection(
    val title: String,
    val items: List<Innertube.Item>,
    val moreEndpoint: NavigationEndpoint.Endpoint.Browse?,
)

data class ArtistPage(
    val artist: Innertube.ArtistItem,
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

        private fun fromMusicResponsiveListItemRenderer(renderer: MusicResponsiveListItemRenderer): Innertube.SongItem? {
            return Innertube.SongItem(
                info = Innertube.Info(
                    name = renderer.flexColumns.firstOrNull()
                        ?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()
                        ?.text ?: "",
                    endpoint = NavigationEndpoint.Endpoint.Watch(
                        videoId = renderer.playlistItemData?.videoId
                    )
                ),
                authors = renderer.flexColumns.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.oddElements()
                    ?.map {
                        Innertube.Info(
                            name = it.text,
                            endpoint = it.navigationEndpoint?.browseEndpoint
                        )
                    } ?: emptyList(),
                album = renderer.flexColumns.getOrNull(3)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()
                    ?.let {
                        Innertube.Info(
                            name = it.text,
                            endpoint = it.navigationEndpoint?.browseEndpoint
                        )
                    },
                durationText = null,
                thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull()
                    ?: return null,
                explicit = renderer.badges?.find {
                    it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                } != null,
//                endpoint = renderer.overlay?.musicItemThumbnailOverlayRenderer?.content
//                    ?.musicPlayButtonRenderer?.playNavigationEndpoint?.watchEndpoint
            )
        }

        private fun fromMusicTwoRowItemRenderer(renderer: MusicTwoRowItemRenderer): Innertube.Item? {
            return when {
                renderer.isSong -> {
                    Innertube.SongItem(
                        info = Innertube.Info(
                            renderer.title?.runs?.firstOrNull()?.text,
                            renderer.navigationEndpoint?.watchEndpoint
                        ),
                        authors = renderer.subtitle?.runs?.map {
                            Innertube.Info(
                                name = it.text,
                                endpoint = it.navigationEndpoint?.browseEndpoint
                            )
                        },
                        album = null,
                        durationText = null,
                        thumbnail = renderer.thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull(),
                        explicit = renderer.subtitleBadges?.find {
                            it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                        } != null
                    )
                }

                renderer.isAlbum -> {
                    Innertube.AlbumItem(
                        info = Innertube.Info(
                            renderer.title?.runs?.firstOrNull()?.text,
                            renderer.navigationEndpoint?.browseEndpoint
                        ),
                        authors = null,
                        year = renderer.subtitle?.runs?.lastOrNull()?.text,
                        thumbnail = renderer.thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull(),
//                        explicit = renderer.subtitleBadges?.find {
//                            it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
//                        } != null
                    )
                }

                renderer.isPlaylist -> {
                    // Playlist from YouTube Music
                    Innertube.PlaylistItem(
                        info = Innertube.Info(
                            renderer.title?.runs?.firstOrNull()?.text,
                            renderer.navigationEndpoint?.browseEndpoint
                        ),
                        songCount = null,
                        thumbnail = renderer.thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull(),
                        channel = null,
                        isEditable = false
                    )
                }

                renderer.isArtist -> {
                    Innertube.ArtistItem(
                        info = Innertube.Info(
                            renderer.title?.runs?.firstOrNull()?.text,
                            renderer.navigationEndpoint?.browseEndpoint
                        ),
                        thumbnail = renderer.thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull(),
                        subscribersCountText = null,
                    )
                }

                else -> null
            }
        }
    }
}
