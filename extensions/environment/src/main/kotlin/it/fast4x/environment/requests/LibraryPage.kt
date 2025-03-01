package com.zionhuang.innertube.pages

import it.fast4x.environment.Environment
import it.fast4x.environment.Environment.getBestQuality
import it.fast4x.environment.models.MusicResponsiveListItemRenderer
import it.fast4x.environment.models.MusicTwoRowItemRenderer
import it.fast4x.environment.models.NavigationEndpoint
import it.fast4x.environment.models.oddElements

data class LibraryPage(
    val items: List<Environment.Item>,
    val continuation: String?,
) {
    companion object {
        fun fromMusicTwoRowItemRenderer(renderer: MusicTwoRowItemRenderer): Environment.Item? {
            if (renderer.isPlaylist)
                println("LibraryPage renderer ${renderer.menu?.menuRenderer?.items?.map { it.menuServiceItemRenderer?.text }}")
            return when {
                renderer.isAlbum -> Environment.AlbumItem(
                    info = Environment.Info(
                        renderer.title?.runs?.firstOrNull()?.text,
                        renderer.navigationEndpoint?.browseEndpoint
                    ),
//                    browseId = renderer.navigationEndpoint.browseEndpoint?.browseId ?: return null,
//                    playlistId = renderer.thumbnailOverlay?.musicItemThumbnailOverlayRenderer?.content
//                        ?.musicPlayButtonRenderer?.playNavigationEndpoint
//                        ?.watchPlaylistEndpoint?.playlistId ?: return null,
//                    title = renderer.title.runs?.firstOrNull()?.text ?: return null,
                    //artists = parseArtists(renderer.subtitle?.runs),
                    authors = renderer.subtitle?.runs?.oddElements()?.map {
                        Environment.Info(
                            name = it.text,
                            endpoint = it.navigationEndpoint?.browseEndpoint
                        )
                    },
                    year = renderer.subtitle?.runs?.lastOrNull()?.text,
                    thumbnail = renderer.thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails?.getBestQuality()
//                    explicit = renderer.subtitleBadges?.find {
//                        it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
//                    } != null
                )

                renderer.isPlaylist -> Environment.PlaylistItem(
                    info = Environment.Info(
                        renderer.title?.runs?.firstOrNull()?.text,
                        renderer.navigationEndpoint?.browseEndpoint
                    ),
                    songCount = null,
                    thumbnail = renderer.thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails?.getBestQuality(),
                    channel = null,
                    isEditable = renderer.menu?.menuRenderer?.items?.find {
                        it.menuNavigationItemRenderer?.icon?.iconType == "EDIT"
                    } != null,
//                    id = renderer.navigationEndpoint.browseEndpoint?.browseId?.removePrefix("VL")
//                        ?: return null,
//                    title = renderer.title.runs?.firstOrNull()?.text ?: return null,
//                    author = renderer.subtitle?.runs?.getOrNull(2)?.let {
//                        Artist(
//                            name = it.text,
//                            id = it.navigationEndpoint?.browseEndpoint?.browseId
//                        )
//                    },
//                    songCountText = renderer.subtitle?.runs?.lastOrNull()?.text,
//                    thumbnail = if ((renderer.subtitle?.runs?.lastOrNull()?.text?.replace(
//                            "[^0-9]".toRegex(),
//                            ""
//                        )?.toIntOrNull() ?: 0) == 0
//                    ) null
//                    else renderer.thumbnailRenderer.musicThumbnailRenderer?.getThumbnailUrl(),
//                    playEndpoint = renderer.thumbnailOverlay
//                        ?.musicItemThumbnailOverlayRenderer?.content
//                        ?.musicPlayButtonRenderer?.playNavigationEndpoint
//                        ?.watchPlaylistEndpoint,
//                    shuffleEndpoint = renderer.menu?.menuRenderer?.items?.find {
//                        it.menuNavigationItemRenderer?.icon?.iconType == "MUSIC_SHUFFLE"
//                    }?.menuNavigationItemRenderer?.navigationEndpoint?.watchPlaylistEndpoint,
//                    radioEndpoint = renderer.menu?.menuRenderer?.items?.find {
//                        it.menuNavigationItemRenderer?.icon?.iconType == "MIX"
//                    }?.menuNavigationItemRenderer?.navigationEndpoint?.watchPlaylistEndpoint,

                )

                renderer.isArtist -> Environment.ArtistItem(
                    info = Environment.Info(
                        renderer.title?.runs?.firstOrNull()?.text,
                        renderer.navigationEndpoint?.browseEndpoint
                    ),
                    thumbnail = renderer.thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails?.getBestQuality(),
                    subscribersCountText = null
//                    id = renderer.navigationEndpoint.browseEndpoint?.browseId ?: return null,
//                    title = renderer.title.runs?.lastOrNull()?.text ?: return null,
//                    thumbnail = renderer.thumbnailRenderer.musicThumbnailRenderer?.getThumbnailUrl()
//                        ?: return null,
//                    shuffleEndpoint = renderer.menu?.menuRenderer?.items?.find {
//                        it.menuNavigationItemRenderer?.icon?.iconType == "MUSIC_SHUFFLE"
//                    }?.menuNavigationItemRenderer?.navigationEndpoint?.watchPlaylistEndpoint
//                        ?: return null,
//                    radioEndpoint = renderer.menu.menuRenderer.items.find {
//                        it.menuNavigationItemRenderer?.icon?.iconType == "MIX"
//                    }?.menuNavigationItemRenderer?.navigationEndpoint?.watchPlaylistEndpoint
//                        ?: return null,
                )

                else -> null
            }
        }

        fun fromMusicResponsiveListItemRenderer(renderer: MusicResponsiveListItemRenderer): Environment.Item? {
            return when {
                renderer.isSong -> Environment.SongItem(
                    info = Environment.Info(
                        renderer.flexColumns.firstOrNull()
                            ?.musicResponsiveListItemFlexColumnRenderer?.text
                            ?.runs?.firstOrNull()?.text,
                        endpoint = NavigationEndpoint.Endpoint.Watch(
                            videoId = renderer.playlistItemData?.videoId
                        )
                    ),
//                    id = renderer.playlistItemData?.videoId ?: return null,
//                    title = renderer.flexColumns.firstOrNull()
//                        ?.musicResponsiveListItemFlexColumnRenderer?.text
//                        ?.runs?.firstOrNull()?.text ?: return null,
                    authors = renderer.flexColumns.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.oddElements()?.map {
                        Environment.Info(
                            name = it.text,
                            endpoint = it.navigationEndpoint?.browseEndpoint
                        )
                    },
//                    artists = renderer.flexColumns.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.oddElements()
//                        ?.map {
//                            Artist(
//                                name = it.text,
//                                id = it.navigationEndpoint?.browseEndpoint?.browseId
//                            )
//                        } ?: emptyList(),
                    album = renderer.flexColumns.getOrNull(2)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()
                        ?.let {
                            Environment.Info(
                                name = it.text,
                                endpoint = it.navigationEndpoint?.browseEndpoint
                            )
                        },
                    durationText = renderer.fixedColumns?.firstOrNull()?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.text,
                    thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.getBestQuality()
                        ?: return null,
                    explicit = renderer.badges?.find {
                        it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                    } != null,
//                    endpoint = renderer.overlay?.musicItemThumbnailOverlayRenderer?.content?.musicPlayButtonRenderer?.playNavigationEndpoint?.watchEndpoint
                )

                renderer.isArtist -> Environment.ArtistItem(
                    info = Environment.Info(
                        renderer.flexColumns.firstOrNull()?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.text,
                        renderer.navigationEndpoint?.browseEndpoint
                    ),
                    thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.getBestQuality(),
                    subscribersCountText = null
//                    id = renderer.navigationEndpoint?.browseEndpoint?.browseId ?: return null,
//                    title = renderer.flexColumns.firstOrNull()?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.text
//                        ?: return null,
//                    thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.getThumbnailUrl()
//                        ?: return null,
//                    shuffleEndpoint = renderer.menu?.menuRenderer?.items
//                        ?.find { it.menuNavigationItemRenderer?.icon?.iconType == "MUSIC_SHUFFLE" }
//                        ?.menuNavigationItemRenderer?.navigationEndpoint?.watchPlaylistEndpoint,
//                    radioEndpoint = renderer.menu?.menuRenderer?.items
//                        ?.find { it.menuNavigationItemRenderer?.icon?.iconType == "MIX" }
//                        ?.menuNavigationItemRenderer?.navigationEndpoint?.watchPlaylistEndpoint
                )

                else -> null
            }
        }

//        private fun parseArtists(runs: List<Run>?): List<Artist> {
//            val artists = mutableListOf<Artist>()
//
//            if (runs != null) {
//                for (run in runs) {
//                    if (run.navigationEndpoint != null) {
//                        artists.add(
//                            Artist(
//                                id = run.navigationEndpoint.browseEndpoint?.browseId!!,
//                                name = run.text
//                            )
//                        )
//                    }
//                }
//            }
//            return artists
//        }
    }
}
