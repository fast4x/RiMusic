package it.fast4x.innertube.requests

import io.ktor.http.Url
import it.fast4x.innertube.Innertube
import it.fast4x.innertube.Innertube.Info
import it.fast4x.innertube.Innertube.SearchFilter.Companion.Artist
import it.fast4x.innertube.Innertube.getBestQuality
import it.fast4x.innertube.models.BrowseResponse
import it.fast4x.innertube.models.MusicResponsiveHeaderRenderer
import it.fast4x.innertube.models.MusicResponsiveListItemRenderer
import it.fast4x.innertube.models.NavigationEndpoint
import it.fast4x.innertube.models.bodies.BrowseBody
import it.fast4x.innertube.models.oddElements
import it.fast4x.innertube.models.splitBySeparator
import it.fast4x.innertube.utils.PageHelper

data class AlbumPage(
    val album: Innertube.AlbumItem,
    val songs: List<Innertube.SongItem>,
    val otherVersions: List<Innertube.AlbumItem>,
    val url: String?,
    val description: String?
) {
    companion object {
        fun getPlaylistId(response: BrowseResponse): String? {
            var playlistId = response.microformat?.microformatDataRenderer?.urlCanonical?.substringAfterLast('=')
            if (playlistId == null)
            {
                playlistId = response.header?.musicDetailHeaderRenderer?.menu?.menuRenderer?.topLevelButtons?.firstOrNull()
                    ?.buttonRenderer?.navigationEndpoint?.watchPlaylistEndpoint?.playlistId
            }
            return playlistId
        }

        fun getTitle(response: BrowseResponse): String? {
            val title = getHeader(response)?.title ?: response.header?.musicDetailHeaderRenderer?.title
            return title?.runs?.firstOrNull()?.text
        }

        fun getYear(response: BrowseResponse): Int? {
            val title = getHeader(response)?.subtitle ?: response.header?.musicDetailHeaderRenderer?.subtitle
            return title?.runs?.lastOrNull()?.text?.toIntOrNull()
        }

        fun getThumbnail(response: BrowseResponse): String? {
            return response.background?.musicThumbnailRenderer?.getThumbnailUrl() ?: response.header?.musicDetailHeaderRenderer?.thumbnail
                ?.croppedSquareThumbnailRenderer?.getThumbnailUrl()
        }

        fun getArtists(response: BrowseResponse): List<Info<NavigationEndpoint.Endpoint.Browse>> {
            val artists = getHeader(response)?.straplineTextOne?.runs?.oddElements()?.map {
                    Innertube.Info(
                        name = it.text,
                        endpoint = NavigationEndpoint.Endpoint.Browse(
                            browseId = it.navigationEndpoint?.browseEndpoint?.browseId
                        )
                    )
            } ?: response.header?.musicDetailHeaderRenderer?.subtitle?.runs?.splitBySeparator()?.getOrNull(1)?.oddElements()?.map {
                Innertube.Info(
                    name = it.text,
                    endpoint = NavigationEndpoint.Endpoint.Browse(
                        browseId = it.navigationEndpoint?.browseEndpoint?.browseId
                    )
                )
            } ?: emptyList()

            return artists
        }

        private fun getHeader(response: BrowseResponse): MusicResponsiveHeaderRenderer? {
            val tabs = response.contents?.singleColumnBrowseResultsRenderer?.tabs
                ?: response.contents?.twoColumnBrowseResultsRenderer?.tabs
            val section =
                tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()
            val header = section?.musicResponsiveHeaderRenderer
            return header
        }

        fun getSongs(response: BrowseResponse, album: Innertube.AlbumItem): List<Innertube.SongItem> {
            val tabs = response.contents?.singleColumnBrowseResultsRenderer?.tabs ?: response.contents?.twoColumnBrowseResultsRenderer?.tabs
            val shelfRenderer = tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()?.musicShelfRenderer ?:
            response.contents?.twoColumnBrowseResultsRenderer?.secondaryContents?.sectionListRenderer?.contents?.firstOrNull()?.musicShelfRenderer

            val songs = shelfRenderer?.contents?.mapNotNull {
                it.musicResponsiveListItemRenderer?.let { it1 -> getSong(it1, album) }
            }
            return songs ?: emptyList()
        }

        fun getSong(renderer: MusicResponsiveListItemRenderer, album: Innertube.AlbumItem? = null): Innertube.SongItem {
            println("mediaItem getSong ${renderer.flexColumns.get(1).musicResponsiveListItemFlexColumnRenderer?.text?.runs}")
            return Innertube.SongItem(
                info = Info(
                    name = PageHelper.extractRuns(renderer.flexColumns, "MUSIC_VIDEO")
                        .firstOrNull()?.text ?: "",
                    endpoint = NavigationEndpoint.Endpoint.Watch(
                        videoId = renderer.playlistItemData?.videoId
                    )
                ),
//                authors = PageHelper.extractRuns(renderer.flexColumns, "MUSIC_PAGE_TYPE_ARTIST")
//                    .map {
//                        Info(
//                            name = it.text,
//                            endpoint = it.navigationEndpoint?.browseEndpoint
//
//                        )
//                    },
                authors = renderer.flexColumns.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs
                    ?.map {
                        Info(
                            name = it.text,
                            endpoint = it.navigationEndpoint?.browseEndpoint
                        )
                    },
                album = album?.info
                    ?: renderer.flexColumns.getOrNull(2)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs
                        ?.firstOrNull()?.let {
                            Info(
                                name = it.text,
                                endpoint = it.navigationEndpoint?.browseEndpoint
                            )
                        },
                durationText = renderer.fixedColumns?.firstOrNull()
                    ?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()
                    ?.text,
                thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.getBestQuality()
                    ?: album?.thumbnail!!,
                explicit = renderer.badges?.find {
                    it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                } != null
            )

        }
    }
}

suspend fun Innertube.albumPage(body: BrowseBody) = playlistPage(body)?.map { album ->
    album.url?.let { Url(it).parameters["list"] }?.let { playlistId ->
        playlistPage(BrowseBody(browseId = "VL$playlistId"))?.getOrNull()?.let { playlist ->
            println("mediaItem albumPage pre songsPage ${playlist.songsPage?.items?.size}")
            album.copy(songsPage = playlist.songsPage)
        }
    } ?: album
    }

    ?.map { album ->
        //println("mediaItem albumPage post songsPage ${album?.songsPage?.items?.size} des ${album?.description} browseId ${body.browseId}")
        /*
        println("mediaItem albumPage post songsPage songs id ${album?.songsPage?.items?.size}")
        album?.songsPage?.items?.forEach {
            println("mediaItem albumPage post songsPage song id ${it.info?.endpoint?.videoId} playlistId ")
        }
         */

        val albumInfo = Innertube.Info(
            name = album.title,
            endpoint = NavigationEndpoint.Endpoint.Browse(
                browseId = body.browseId,
                params = body.params
            )
        )

        album.copy(
            songsPage = album.songsPage?.copy(
                items = album.songsPage.items?.map { song ->
                    song.copy(
                        authors = song.authors ?: album.authors,
                        album = albumInfo,
                        thumbnail = album.thumbnail
                    )
                }
            )
        )

    }?.onFailure {
        println("ERROR IN Innertube albumPage " + it.message)
    }
