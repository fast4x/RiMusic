package it.fast4x.innertube.requests

import it.fast4x.innertube.Innertube
import it.fast4x.innertube.models.BrowseEndpoint
import it.fast4x.innertube.models.MusicCarouselShelfRenderer
import it.fast4x.innertube.models.MusicTwoRowItemRenderer
import it.fast4x.innertube.models.oddElements
import kotlinx.serialization.Serializable

@Serializable
data class HomePage(
    val sections: List<Section>,
) {
    @Serializable
    data class Section(
        val title: String,
        val label: String?,
        val thumbnail: String?,
        val endpoint: BrowseEndpoint?,
        val items: List<Innertube.Item?>,
    ) {
        companion object {
            fun fromMusicCarouselShelfRenderer(renderer: MusicCarouselShelfRenderer): Section? {
                println("getHomePage() fromMusicCarouselShelfRenderer musicTwoRowItemRenderer: section title ${renderer.header?.musicCarouselShelfBasicHeaderRenderer?.title?.runs?.firstOrNull()?.text}")
                println("getHomePage() fromMusicCarouselShelfRenderer musicTwoRowItemRenderer: section items ${renderer.contents.map { it.musicTwoRowItemRenderer?.title?.runs?.firstOrNull()?.text }}")
                return Section(
                    title = renderer.header?.musicCarouselShelfBasicHeaderRenderer?.title?.runs?.firstOrNull()?.text
                        ?: "",
                    label = renderer.header?.musicCarouselShelfBasicHeaderRenderer?.strapline?.runs?.firstOrNull()?.text,
                    thumbnail = renderer.header?.musicCarouselShelfBasicHeaderRenderer?.thumbnail?.musicThumbnailRenderer?.getThumbnailUrl(),

                    endpoint = BrowseEndpoint(
                        browseId = renderer.header?.musicCarouselShelfBasicHeaderRenderer?.moreContentButton?.buttonRenderer?.navigationEndpoint?.browseEndpoint?.browseId
                            ?: "",
                    ),
                    items = renderer.contents
                        .map {
                            fromMusicTwoRowItemRenderer(
                                it.musicTwoRowItemRenderer,
                                renderer.header?.musicCarouselShelfBasicHeaderRenderer?.title?.runs?.firstOrNull()?.text
                            )
                        } //.filter { it?.title?.isNotEmpty() == true }

                )
            }

            private fun fromMusicTwoRowItemRenderer(renderer: MusicTwoRowItemRenderer?, sectionTitle: String? = null): Innertube.Item? {
                println("getHomePage() fromMusicTwoRowItemRenderer for section $sectionTitle: ${renderer?.title?.runs?.firstOrNull()?.text}")
                return when {
                    renderer?.isSong == true -> {
                        println("getHomePage() fromMusicTwoRowItemRenderer isSong: ${renderer.title?.runs?.firstOrNull()?.text}")
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

                    renderer?.isAlbum == true -> {
                        println("getHomePage() fromMusicTwoRowItemRenderer isAlbum: ${renderer.title?.runs?.firstOrNull()?.text}")
                        Innertube.AlbumItem(
                            info = Innertube.Info(
                                renderer.title?.runs?.firstOrNull()?.text,
                                renderer.navigationEndpoint?.browseEndpoint
                            ),
//                            playlistId = renderer.thumbnailOverlay?.musicItemThumbnailOverlayRenderer?.content
//                                ?.musicPlayButtonRenderer?.playNavigationEndpoint
//                                ?.watchPlaylistEndpoint?.playlistId ?: return null,
//                            title = renderer.title.runs?.firstOrNull()?.text ?: return null,
                            authors = renderer.subtitle?.runs?.oddElements()?.drop(1)?.map {
                                Innertube.Info(
                                    name = it.text,
                                    endpoint = it.navigationEndpoint?.browseEndpoint
                                )
                            },
                            year = renderer.subtitle?.runs?.lastOrNull()?.text,
                            thumbnail = renderer.thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull(),
//                            explicit = renderer.subtitleBadges?.find {
//                                it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
//                            } != null
                        )
                    }

                    renderer?.isPlaylist == true -> {
                        println("getHomePage() fromMusicTwoRowItemRenderer isPlaylist: ${renderer.title?.runs?.firstOrNull()?.text}")
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

                    renderer?.isArtist == true -> {
                        println("getHomePage() fromMusicTwoRowItemRenderer isArtist: ${renderer.title?.runs?.firstOrNull()?.text}")
                        Innertube.ArtistItem(
                            info = Innertube.Info(
                                renderer.title?.runs?.firstOrNull()?.text,
                                renderer.navigationEndpoint?.browseEndpoint
                            ),
                            thumbnail = renderer.thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull(),
                            subscribersCountText = null
                        )
                    }

                    renderer?.isVideo == true -> {
                        println("getHomePage() fromMusicTwoRowItemRenderer isVideo: ${renderer.title?.runs?.firstOrNull()?.text}")
                        Innertube.VideoItem(
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
                            durationText = null,
                            thumbnail = renderer.thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull(),
                            viewsText = null
                        )

                    }

                    else -> {
                        println("getHomePage() fromMusicTwoRowItemRenderer else renderer: ${renderer}")
                        null
                    }
                }
            }

        }
    }
}

