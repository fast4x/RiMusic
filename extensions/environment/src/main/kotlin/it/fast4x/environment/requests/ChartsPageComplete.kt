package it.fast4x.environment.requests

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import it.fast4x.environment.Environment
import it.fast4x.environment.Environment.getBestQuality
import it.fast4x.environment.models.BrowseResponse
import it.fast4x.environment.models.MusicCarouselShelfRenderer
import it.fast4x.environment.models.NavigationEndpoint
import it.fast4x.environment.models.SectionListRenderer
import it.fast4x.environment.models.bodies.BrowseBodyWithLocale
import it.fast4x.environment.models.bodies.FormData

suspend fun Environment.chartsPageComplete(countryCode: String = "") = runCatching {
    val response = client.post(_3djbhqyLpE) {
        setBody(
            BrowseBodyWithLocale(
                browseId = "FEmusic_charts",
                formData = FormData(listOf(countryCode))
            )
        )
    }.body<BrowseResponse>()

    val musicDetailRenderer =
        response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
            ?.tabRenderer?.content?.sectionListRenderer?.contents

    val data =
        response.contents?.singleColumnBrowseResultsRenderer?.tabs?.get(
            0,
        )?.tabRenderer?.content?.sectionListRenderer

    val chart = parseChart(data)

    Environment.ChartsPage(
        playlists = musicDetailRenderer
            ?.mapNotNull { it.musicCarouselShelfRenderer }
            ?.mapNotNull(Environment.PlaylistItem::fromComplete),
        artists = chart?.artists,
        videos = chart?.videos,
        songs = chart?.songs,
        trending = chart?.trending
    )

}.onFailure {
    println("mediaItem ERROR IN Innertube chartsPage " + it.stackTraceToString())
}

fun Environment.PlaylistItem.Companion.fromComplete(renderer: MusicCarouselShelfRenderer): Environment.PlaylistItem? {

    val thumbnail0 = renderer
        .contents?.firstOrNull()?.musicTwoRowItemRenderer
        ?.thumbnailRenderer
        ?.musicThumbnailRenderer
        ?.thumbnail
        ?.thumbnails
        ?.getBestQuality()?.toThumbnail()

    val thumbnail1 = renderer
        .contents?.firstOrNull()?.musicResponsiveListItemRenderer
        ?.thumbnail
        ?.musicThumbnailRenderer
        ?.thumbnail
        ?.thumbnails
        ?.getBestQuality()?.toThumbnail()

    return Environment.PlaylistItem(
        info = Environment.Info(
            name = renderer.header?.musicCarouselShelfBasicHeaderRenderer?.title?.runs?.firstOrNull()?.text,
            endpoint = it.fast4x.environment.models.NavigationEndpoint.Endpoint.Browse(
                browseId = renderer
                    .header?.musicCarouselShelfBasicHeaderRenderer?.title?.runs?.firstOrNull()?.navigationEndpoint?.browseEndpoint?.browseId,
                /*
                 params = renderer
                     .contents?.firstOrNull()?.musicTwoRowItemRenderer
                     ?.navigationEndpoint?.watchEndpoint?.params.toString(),
                 */
                browseEndpointContextSupportedConfigs = null
            )
        ),
        channel = null,
        songCount = renderer
            .contents?.size,
        thumbnail = thumbnail0 ?: thumbnail1,
        isEditable = false
    ).takeIf { it.info?.endpoint?.browseId != null }
}

fun Environment.ArtistItem.Companion.fromC(renderer: List<MusicCarouselShelfRenderer.Content>): List<Environment.ArtistItem> {

    val thumbnail = renderer.firstOrNull()?.musicResponsiveListItemRenderer
        ?.thumbnail
        ?.musicThumbnailRenderer
        ?.thumbnail
        ?.thumbnails
        ?.getBestQuality()?.toThumbnail()

    return listOf(
        Environment.ArtistItem(
            info = Environment.Info(
                name = renderer.firstOrNull()?.musicResponsiveListItemRenderer
                    ?.flexColumns?.firstOrNull()
                    ?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()
                    ?.text,
                endpoint = it.fast4x.environment.models.NavigationEndpoint.Endpoint.Browse(
                    browseId = renderer.firstOrNull()?.musicResponsiveListItemRenderer
                        ?.navigationEndpoint?.browseEndpoint?.browseId,
                    params = null,
                    browseEndpointContextSupportedConfigs = null
                )
            ),
            subscribersCountText = renderer.firstOrNull()?.musicResponsiveListItemRenderer
                ?.flexColumns?.getOrNull(1)
                ?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()
                ?.text,
            thumbnail = thumbnail
        )
    )

}

fun parseChart(data: SectionListRenderer?): Environment.ChartsPage? {
    if (data?.contents != null) {
        val listTrendingItem: ArrayList<Environment.SongItem> = arrayListOf()
        val listSongItem: ArrayList<Environment.SongItem> = arrayListOf()
        val listVideoItem: ArrayList<Environment.VideoItem> = arrayListOf()
        val listArtistItem: ArrayList<Environment.ArtistItem> = arrayListOf()
        var videoPlaylistId = ""
        for (section in data.contents!!) {
            if (section.musicCarouselShelfRenderer != null) {
                val musicCarouselShelfRenderer = section.musicCarouselShelfRenderer
                val pageType =
                    musicCarouselShelfRenderer?.header?.musicCarouselShelfBasicHeaderRenderer?.title?.runs?.get(
                        0
                    )?.navigationEndpoint?.browseEndpoint?.browseEndpointContextSupportedConfigs?.browseEndpointContextMusicConfig?.pageType
                if (pageType == "MUSIC_PAGE_TYPE_PLAYLIST" && musicCarouselShelfRenderer.numItemsPerColumn == null) {
                    videoPlaylistId =
                        musicCarouselShelfRenderer.header?.musicCarouselShelfBasicHeaderRenderer?.title?.runs?.get(
                            0
                        )?.navigationEndpoint?.browseEndpoint?.browseId ?: ""

                    val contents = musicCarouselShelfRenderer.contents
                    listVideoItem.addAll(parseSongChart(contents))
                } else if (pageType == "MUSIC_PAGE_TYPE_PLAYLIST" && musicCarouselShelfRenderer.numItemsPerColumn == "4") {
                    val contents = musicCarouselShelfRenderer.contents
                    contents.forEachIndexed { index, content ->
                        val musicResponsiveListItemRenderer =
                            content.musicResponsiveListItemRenderer
                        if (musicResponsiveListItemRenderer != null) {
                            val thumb =
                                musicResponsiveListItemRenderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails
                            val firstThumb = thumb?.getBestQuality()
                            val videoId = musicResponsiveListItemRenderer.flexColumns.firstOrNull()
                                ?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()
                                ?.navigationEndpoint?.watchEndpoint?.videoId

                            if (firstThumb != null && (firstThumb.width == firstThumb.height && firstThumb.width != null)) {
                                if (videoId != null) {
                                    val song =
                                        Environment.SongItem(
                                            album = musicResponsiveListItemRenderer.flexColumns.getOrNull(
                                                2
                                            )?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()
                                                ?.let {
                                                    Environment.Info(it)
                                                },
                                            info = musicResponsiveListItemRenderer.flexColumns.firstOrNull()
                                                ?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()
                                                ?.let {
                                                    Environment.Info(it)
                                                } ?: return null,
                                            authors = musicResponsiveListItemRenderer.flexColumns.getOrNull(
                                                1
                                            )?.musicResponsiveListItemFlexColumnRenderer?.text?.runs
                                                ?.map { Environment.Info(it) },
                                            durationText = "",
                                            thumbnail = thumb.getBestQuality()!!.toThumbnail(),
                                            explicit = false

                                        )

                                    listSongItem.add(song)
                                }
                            } else {
                                if (videoId != null) {
                                    val song =
                                        Environment.SongItem(
                                            album = musicResponsiveListItemRenderer.flexColumns.getOrNull(
                                                2
                                            )?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()
                                                ?.let {
                                                    Environment.Info(it)
                                                },
                                            info = musicResponsiveListItemRenderer.flexColumns.firstOrNull()
                                                ?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()
                                                ?.let {
                                                    Environment.Info(it)
                                                } ?: return null,
                                            authors = musicResponsiveListItemRenderer.flexColumns.getOrNull(
                                                1
                                            )?.musicResponsiveListItemFlexColumnRenderer?.text?.runs
                                                ?.map { Environment.Info(it) },
                                            durationText = "",
                                            thumbnail = thumb?.getBestQuality()!!.toThumbnail(),
                                            explicit = false

                                        )

                                    listTrendingItem.add(song)
                                }
                            }
                        }
                    }
                } else {
                    val contents = musicCarouselShelfRenderer.contents
                    parseArtistChart(contents)?.let { listArtistItem.addAll(it) }
                }
            }
        }
        println("mediaItem Innertube.chartsPageComplete parseChart listSongItem: ${listSongItem.size} -> $listSongItem")
        println("mediaItem Innertube.chartsPageComplete parseChart listVideoItem: ${listVideoItem.size} -> $listVideoItem")
        println("mediaItem Innertube.chartsPageComplete parseChart listArtistItem: ${listArtistItem.size} -> $listArtistItem")
        println("mediaItem Innertube.chartsPageComplete parseChart listTrendingItem: ${listTrendingItem.size} -> $listTrendingItem")

        return Environment.ChartsPage(
            artists = listArtistItem,
            videos = listVideoItem,
            songs = listSongItem,
            trending = listTrendingItem
        )
    } else {
        return null
    }
}

fun parseSongChart(contents: List<MusicCarouselShelfRenderer.Content>): ArrayList<Environment.VideoItem> {
    val listVideoItem: ArrayList<Environment.VideoItem> = arrayListOf()
    for (content in contents) {
        val title = content.musicTwoRowItemRenderer?.title?.runs?.get(0)?.text
        val runs = content.musicTwoRowItemRenderer?.subtitle?.runs
        var view = ""
        val artists: ArrayList<Environment.ArtistItem> = arrayListOf()
        val albums: ArrayList<Environment.AlbumItem> = arrayListOf()
        if (runs != null) {
            for (i in runs.indices) {
                if (i.rem(2) == 0) {
                    if (i == runs.size - 1) {
                        view += runs[i].text
                    } else {
                        val name = runs[i].text
                        val id = runs[i].navigationEndpoint?.browseEndpoint?.browseId
                        if (id != null) {
                            if (id.startsWith("MPRE")) {
                                albums.add(
                                    //Album(id = id, name = name))
                                    Environment.AlbumItem(
                                        info = Environment.Info(
                                            name = name,
                                            endpoint = NavigationEndpoint.Endpoint.Browse(browseId = id)
                                        ),
                                        authors = null,
                                        year = null,
                                        thumbnail = null
                                    )
                                )
                            } else {
                                artists.add(
                                    Environment.ArtistItem(
                                        info = Environment.Info(
                                            name = name,
                                            endpoint = NavigationEndpoint.Endpoint.Browse(browseId = id)
                                        ),
                                        subscribersCountText = null,
                                        thumbnail = null
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        val thumbnails =
            content.musicTwoRowItemRenderer?.thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails
        val videoId = content.musicTwoRowItemRenderer?.navigationEndpoint?.watchEndpoint?.videoId
        listVideoItem.add(
            /*
            ItemVideo(
                artists = artists,
                playlistId = "",
                thumbnails = thumbnails?.toListThumbnail() ?: listOf(),
                title = title ?: "",
                videoId = videoId ?: "",
                views = view
            )
             */
            Environment.VideoItem(
                info = Environment.Info(
                    name = title,
                    endpoint = NavigationEndpoint.Endpoint.Watch(videoId = videoId)
                ),
                authors = artists.map {
                    Environment.Info(
                        name = it.info?.name,
                        endpoint = it.info?.endpoint
                    )
                },
                viewsText = null,
                durationText = null,
                thumbnail = thumbnails?.toListThumbnail()?.getBestQuality()
            )
        )
    }
    return listVideoItem
}

fun parseArtistChart(contents: List<MusicCarouselShelfRenderer.Content>?): ArrayList<Environment.ArtistItem>? {
    return if (contents != null) {
        val artists: ArrayList<Environment.ArtistItem> = arrayListOf()
        for (i in contents.indices) {
            val content = contents[i]
            if (content.musicResponsiveListItemRenderer != null) {
                val title =
                    content.musicResponsiveListItemRenderer?.flexColumns?.get(0)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.get(
                        0
                    )?.text
                val subscriber =
                    content.musicResponsiveListItemRenderer?.flexColumns?.get(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.get(
                        0
                    )?.text
                val thumbnails =
                    content.musicResponsiveListItemRenderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails
                val artistId =
                    content.musicResponsiveListItemRenderer.navigationEndpoint?.browseEndpoint?.browseId
                artists.add(
                    Environment.ArtistItem(
                        info = Environment.Info(
                            name = title,
                            endpoint = NavigationEndpoint.Endpoint.Browse(browseId = artistId ?: "")
                        ),
                        subscribersCountText = subscriber ?: "",
                        thumbnail = thumbnails?.toListThumbnail()?.getBestQuality(),
                    )
                )
            }
        }
        artists
    } else {
        null
    }
}