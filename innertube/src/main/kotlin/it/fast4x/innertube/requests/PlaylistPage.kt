package it.fast4x.innertube.requests

import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import it.fast4x.innertube.Innertube
import it.fast4x.innertube.models.BrowseResponse
import it.fast4x.innertube.models.ContinuationResponse
import it.fast4x.innertube.models.MusicCarouselShelfRenderer
import it.fast4x.innertube.models.MusicShelfRenderer
import it.fast4x.innertube.models.bodies.BrowseBody
import it.fast4x.innertube.models.bodies.ContinuationBody
import it.fast4x.innertube.utils.from
import it.fast4x.innertube.utils.runCatchingNonCancellable

/**** api modified by youtube music 0624 ****/
suspend fun Innertube.playlistPage(body: BrowseBody) = runCatchingNonCancellable {
    val response = client.post(browse) {
        setBody(body)
        body.context.apply()
    }.body<BrowseResponse>()

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

        val musicShelfRenderer = contents
            ?.firstOrNull()
            ?.musicShelfRenderer

        val musicCarouselShelfRenderer = contents
            ?.getOrNull(1)
            ?.musicCarouselShelfRenderer

        Innertube.PlaylistOrAlbumPage(
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
                ?.map(Innertube::Info),
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
            songsPage = musicShelfRenderer
                ?.toSongsPage(),
            otherVersions = musicCarouselShelfRenderer
                ?.contents
                ?.mapNotNull(MusicCarouselShelfRenderer.Content::musicTwoRowItemRenderer)
                ?.mapNotNull(Innertube.AlbumItem::from),
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

        val musicShelfRenderer = contents
            ?.firstOrNull()
            ?.musicShelfRenderer

        val musicCarouselShelfRenderer = contents
            ?.getOrNull(1)
            ?.musicCarouselShelfRenderer

        Innertube.PlaylistOrAlbumPage(
            title = header
                ?.title
                ?.text,
            description = response.contents.twoColumnBrowseResultsRenderer.tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer
                ?.contents?.firstOrNull()?.musicResponsiveHeaderRenderer?.description?.musicDescriptionShelfRenderer?.description?.runs?.joinToString("") { it.text.toString() },
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
                ?.map(Innertube::Info),
            year = header
                ?.subtitle
                ?.runs
                ?.getOrNull(2)
                ?.text,
            url = response
                .microformat
                ?.microformatDataRenderer
                ?.urlCanonical,
            songsPage = musicShelfRenderer
                ?.toSongsPage(),
            otherVersions = musicCarouselShelfRenderer
                ?.contents
                ?.mapNotNull(MusicCarouselShelfRenderer.Content::musicTwoRowItemRenderer)
                ?.mapNotNull(Innertube.AlbumItem::from),
            otherInfo = response.contents.twoColumnBrowseResultsRenderer.tabs?.firstOrNull()
                ?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()
                ?.musicResponsiveHeaderRenderer?.secondSubtitle?.runs?.joinToString("") { it.text.toString() }
        )
    }

}?.onFailure {
    println("mediaItem ERROR IN Innertube playlistpage " + it.message)
}

suspend fun Innertube.playlistPage(body: ContinuationBody) = runCatchingNonCancellable {
    val response = client.post(browse) {
        setBody(body)
        parameter("continuation", body.continuation)
        parameter("ctoken", body.continuation)
        parameter("type", "next")
        body.context.apply()
    }.body<ContinuationResponse>()

    response
        .continuationContents
        ?.musicShelfContinuation
        ?.toSongsPage()
}

suspend fun Innertube.playlistPageLong(body: ContinuationBody) = runCatching {
    val response = client.post(browse) {
        //---
        //mask("continuationContents.musicPlaylistShelfContinuation(continuations,contents.$musicResponsiveListItemRendererMask)")
        //---
        parameter("continuation", body.continuation)
        parameter("ctoken", body.continuation)
        //parameter("type", "next")
        setBody(body) // required for long playlist
        body.context.apply()
    }.body<ContinuationResponse>()

    println("mediaItem ContinuationBody playlistPageLong continuation ${response.continuationContents?.musicShelfContinuation?.continuations?.firstOrNull()?.nextContinuationData?.continuation}")

    response
        .continuationContents
        ?.musicShelfContinuation
        ?.toSongsPage()
}

private fun MusicShelfRenderer?.toSongsPage() = Innertube.ItemsPage(
    items = this
        ?.contents
        ?.mapNotNull(MusicShelfRenderer.Content::musicResponsiveListItemRenderer)
        ?.mapNotNull(Innertube.SongItem::from)
        ?.also {
            println("mediaItem MusicShelfRenderer toSongsPage ${it.size}")
            it.forEach {
                println("mediaItem MusicShelfRenderer toSongsPage song name ${it.info?.name} videoId ${it.info?.endpoint?.videoId} ")
            }
        },
    continuation = this
        ?.continuations
        ?.firstOrNull()
        ?.nextContinuationData
        ?.continuation
)

