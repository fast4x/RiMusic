package it.fast4x.environment.requests

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import it.fast4x.environment.Environment
import it.fast4x.environment.Environment.getBestQuality
import it.fast4x.environment.models.BrowseResponse
import it.fast4x.environment.models.MusicCarouselShelfRenderer
import it.fast4x.environment.models.MusicResponsiveListItemRenderer
import it.fast4x.environment.models.MusicShelfRenderer
import it.fast4x.environment.models.NavigationEndpoint
import it.fast4x.environment.models.bodies.BrowseBody
import it.fast4x.environment.models.bodies.ContinuationBody
import it.fast4x.environment.models.oddElements
import it.fast4x.environment.utils.from
import it.fast4x.environment.utils.runCatchingCancellable
import it.fast4x.environment.utils.runCatchingNonCancellable

data class PlaylistPage(
    val playlist: Environment.PlaylistItem,
    val description: String?,
    var songs: List<Environment.SongItem>,
    val songsContinuation: String?,
    val continuation: String?,
    val isEditable: Boolean? = false,
) {
    companion object {
        fun fromMusicResponsiveListItemRenderer(renderer: MusicResponsiveListItemRenderer): Environment.SongItem {
            println("YtMusic getPlaylist PlaylistPage setVideoId ${renderer.playlistItemData?.playlistSetVideoId}")
            return Environment.SongItem(
                info = Environment.Info(
                    name = renderer.flexColumns.firstOrNull()
                        ?.musicResponsiveListItemFlexColumnRenderer?.text
                        ?.runs?.firstOrNull()?.text,
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
                },
                album = renderer.flexColumns.getOrNull(2)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()
                    ?.let {
                        Environment.Info(
                            name = it.text,
                            endpoint = it.navigationEndpoint?.browseEndpoint
                        )
                    },
                durationText = renderer.fixedColumns?.firstOrNull()?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.text,
                thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.getBestQuality(),
                explicit = renderer.badges?.find {
                    it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                } != null,
                setVideoId = renderer.playlistItemData?.playlistSetVideoId,
//                endpoint = renderer.overlay?.musicItemThumbnailOverlayRenderer?.content?.musicPlayButtonRenderer?.playNavigationEndpoint?.watchEndpoint,

            )
        }
    }
}

suspend fun Environment.playlistPage(body: BrowseBody) = runCatchingCancellable {
    val response = client.post(_3djbhqyLpE) {
        setLogin(setLogin = true)
        setBody(body)
        //body.context.apply()
    }.body<BrowseResponse>()

//    val songsOld = response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
//        ?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()
//        ?.musicPlaylistShelfRenderer?.contents
//
//    val songsNew = response.contents?.twoColumnBrowseResultsRenderer?.secondaryContents?.sectionListRenderer
//        ?.contents?.firstOrNull()?.musicPlaylistShelfRenderer?.contents
//
//    println("mediaItem playlistPage songsOld ${songsOld?.size}")
//    println("mediaItem playlistPage songsNew ${songsNew?.size}")


    if (response.contents?.twoColumnBrowseResultsRenderer == null) {
        /* OLD */
        val header = response
            .header
            ?.musicDetailHeaderRenderer

        val contents = response
            .contents
            ?.singleColumnBrowseResultsRenderer
            ?.tabs
            ?.firstOrNull()
            ?.tabRenderer
            ?.content
            ?.sectionListRenderer
            ?.contents

//        val musicShelfRenderer = contents
//            ?.firstOrNull()
//            ?.musicShelfRenderer

        val musicCarouselShelfRenderer = contents
            ?.getOrNull(1)
            ?.musicCarouselShelfRenderer

        Environment.PlaylistOrAlbumPage(
            title = header
                ?.title
                ?.text,
            description = header
                ?.description
                ?.text,
            thumbnail = header
                ?.thumbnail
                ?.musicThumbnailRenderer
                ?.thumbnail
                ?.thumbnails
                ?.getBestQuality(),
                //?.maxByOrNull { (it.width ?: 0) * (it.height ?: 0) },
            authors = header
                ?.subtitle
                ?.splitBySeparator()
                ?.getOrNull(1)
                ?.map(Environment::Info),
            year = header
                ?.subtitle
                ?.splitBySeparator()
                ?.getOrNull(2)
                ?.firstOrNull()
                ?.text,
            url = response
                .microformat
                ?.microformatDataRenderer
                ?.urlCanonical,
            songsPage = Environment.ItemsPage(
                items = response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
                    ?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()
                    ?.musicPlaylistShelfRenderer?.contents?.mapNotNull {
                        it.musicResponsiveListItemRenderer?.let { it1 -> Environment.SongItem.from(it1) }
                    },
                continuation = response.continuationContents?.musicPlaylistShelfContinuation?.continuations
                    ?.firstOrNull()?.nextContinuationData?.continuation
            ),
            otherVersions = musicCarouselShelfRenderer
                ?.contents
                ?.mapNotNull(MusicCarouselShelfRenderer.Content::musicTwoRowItemRenderer)
                ?.mapNotNull(Environment.AlbumItem::from),
            otherInfo = header
                ?.secondSubtitle
                ?.text
        )
    } else {
        /* NEW */
        val header = response
            .contents
            .twoColumnBrowseResultsRenderer
            .tabs
            ?.firstOrNull()
            ?.tabRenderer
            ?.content
            ?.sectionListRenderer
            ?.contents
            ?.firstOrNull()
            ?.musicResponsiveHeaderRenderer

        val contents = response
            .contents
            .twoColumnBrowseResultsRenderer
            .secondaryContents
            ?.sectionListRenderer
            ?.contents

//        val musicShelfRenderer = contents
//            ?.firstOrNull()
//            ?.musicShelfRenderer

        val musicCarouselShelfRenderer = contents
            ?.getOrNull(1)
            ?.musicCarouselShelfRenderer

        Environment.PlaylistOrAlbumPage(
            title = header
                ?.title
                ?.text,
            description = null,
//            description = response.contents.twoColumnBrowseResultsRenderer.tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer
//                ?.contents?.firstOrNull()?.musicResponsiveHeaderRenderer?.description?.musicDescriptionShelfRenderer?.description?.runs?.joinToString("") { it.text.toString() },
            thumbnail = header
                ?.thumbnail
                ?.musicThumbnailRenderer
                ?.thumbnail
                ?.thumbnails
                ?.getBestQuality(),
            authors = header
                ?.straplineTextOne
                ?.splitBySeparator()
                ?.getOrNull(0)
                ?.map(Environment::Info),
            year = header
                ?.subtitle
                ?.runs
                ?.getOrNull(2)
                ?.text,
            url = response
                .microformat
                ?.microformatDataRenderer
                ?.urlCanonical,
            songsPage = Environment.ItemsPage(
                items = response.contents.twoColumnBrowseResultsRenderer.secondaryContents?.sectionListRenderer
                ?.contents?.firstOrNull()?.musicPlaylistShelfRenderer?.contents?.mapNotNull {
                    it.musicResponsiveListItemRenderer?.let { it1 -> Environment.SongItem.from(it1) }
                },
                continuation = response.contents.twoColumnBrowseResultsRenderer.secondaryContents?.sectionListRenderer
                    ?.continuations?.firstOrNull()?.nextContinuationData?.continuation

//                continuation = response.continuationContents?.musicPlaylistShelfContinuation?.continuations
//                    ?.firstOrNull()?.nextContinuationData?.continuation
            ),
            otherVersions = musicCarouselShelfRenderer
                ?.contents
                ?.mapNotNull(MusicCarouselShelfRenderer.Content::musicTwoRowItemRenderer)
                ?.mapNotNull(Environment.AlbumItem::from),
            otherInfo = response.contents.twoColumnBrowseResultsRenderer.tabs?.firstOrNull()
                ?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()
                ?.musicResponsiveHeaderRenderer?.secondSubtitle?.runs?.joinToString("") { it.text.toString() }
        )
    }

}?.onFailure {
    println("mediaItem ERROR IN Innertube playlistpage " + it.message)
}

suspend fun Environment.playlistPage(body: ContinuationBody) = runCatchingNonCancellable {
    val call = Environment.browse(continuation = body.continuation)
    val callResponse = call.bodyAsText()
    val response = call.body<BrowseResponse>()
//    println("mediaItem playlist completed() ContinuationResponse sectionListContinuation ${response.continuationContents?.sectionListContinuation
//        ?.contents?.map {
//            it.musicCarouselShelfRenderer?.contents?.map {
//                it.musicTwoRowItemRenderer.
//            }
//        }}")
//    println("mediaItem playlist completed() ContinuationResponse musicPlaylistShelfContinuation ${response.continuationContents?.musicPlaylistShelfContinuation
//        ?.continuations?.map {
//            it.nextContinuationData
//        }}")
//    println("mediaItem playlist completed() ContinuationResponse musicShelfContinuation ${response.continuationContents?.musicShelfContinuation}")
//    println("mediaItem playlist completed() ContinuationResponse gridContinuation ${response.continuationContents?.gridContinuation}")
//    val response = client.post(browse) {
//        setLogin(setLogin = true)
//        setBody(body)
//        parameter("continuation", body.continuation)
//        parameter("ctoken", body.continuation)
//        parameter("type", "next")
//        //body.context.apply()
//    }.body<ContinuationResponse>()

//    println("mediaItem playlist completed() ContinuationResponse ${response.continuationContents?.musicPlaylistShelfContinuation}")
////    response.continuationContents?.musicPlaylistShelfContinuation?.toSongsPage()

    println("mediaItem playlist completed() ContinuationResponse ${response.continuationContents?.musicShelfContinuation}")
    response
        .continuationContents
        ?.musicShelfContinuation
        ?.toSongsPage()
}

private fun MusicShelfRenderer?.toSongsPage() = Environment.ItemsPage(
    items = this
        ?.contents
        ?.mapNotNull(MusicShelfRenderer.Content::musicResponsiveListItemRenderer)
        ?.mapNotNull(Environment.SongItem::from),
        /*
        ?.also {
            println("mediaItem MusicShelfRenderer toSongsPage ${it.size}")
            it.forEach {
                println("mediaItem MusicShelfRenderer toSongsPage song name ${it.info?.name} videoId ${it.info?.endpoint?.videoId} ")
            }
        },
         */
    continuation = this
        ?.continuations
        ?.firstOrNull()
        ?.nextContinuationData
        ?.continuation
)
private fun BrowseResponse.ContinuationContents.MusicPlaylistShelfContinuation?.toSongsPage() = Environment.ItemsPage(
    items = this
        ?.contents
        ?.mapNotNull(MusicShelfRenderer.Content::musicResponsiveListItemRenderer)
        ?.mapNotNull(Environment.SongItem::from),
    /*
    ?.also {
        println("mediaItem MusicShelfRenderer toSongsPage ${it.size}")
        it.forEach {
            println("mediaItem MusicShelfRenderer toSongsPage song name ${it.info?.name} videoId ${it.info?.endpoint?.videoId} ")
        }
    },
     */
    continuation = this
        ?.continuations
        ?.firstOrNull()
        ?.nextContinuationData
        ?.continuation
)


