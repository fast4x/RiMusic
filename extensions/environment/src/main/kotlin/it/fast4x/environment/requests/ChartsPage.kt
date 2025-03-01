package it.fast4x.environment.requests

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import it.fast4x.environment.Environment
import it.fast4x.environment.models.bodies.BrowseBodyWithLocale
import it.fast4x.environment.models.bodies.FormData
import it.fast4x.environment.models.v0624.charts.BrowseChartsResponse0624
import it.fast4x.environment.models.v0624.charts.MusicCarouselShelfRenderer
import it.fast4x.environment.models.v0624.charts.MusicCarouselShelfRendererContent

suspend fun Environment.chartsPage(countryCode: String = "") = runCatching {
    val response = client.post(_3djbhqyLpE) {
        setBody(BrowseBodyWithLocale(browseId = "FEmusic_charts", formData = FormData(listOf(countryCode))))
    }.body<BrowseChartsResponse0624>()

    val musicDetailRenderer =
        response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
            ?.tabRenderer?.content?.sectionListRenderer?.contents

    /*
    println("mediaItem chartsPage playists ${musicDetailRenderer
        ?.mapNotNull { it.musicCarouselShelfRenderer }
        ?.mapNotNull(Innertube.PlaylistItem::from)?.size}")
     */

/*
    println("mediaItem chartsPage artists ${musicDetailRenderer
        ?.mapNotNull { 
            it.musicCarouselShelfRenderer?.contents
        }
        //?.mapNotNull { it.musicCarouselShelfRenderer }
        ?.map(Innertube.ArtistItem::from)}")

 */

    /*
    println("mediaItem chartsPage Language ${
        musicDetailRenderer
            ?.musicShelfRenderer?.subheaders?.firstOrNull()
            ?.musicSideAlignedItemRenderer?.startItems?.firstOrNull()
            ?.musicSortFilterButtonRenderer
            ?.title?.runs?.firstOrNull()?.text}")
     */

    Environment.ChartsPage(
        playlists = musicDetailRenderer
            ?.mapNotNull { it.musicCarouselShelfRenderer }
            ?.mapNotNull(Environment.PlaylistItem::from)
    )

}.onFailure {
    println("mediaItem ERROR IN Innertube chartsPage " + it.message)
}

fun Environment.PlaylistItem.Companion.from(renderer: MusicCarouselShelfRenderer): Environment.PlaylistItem? {

    val thumbnail0 = renderer
        .contents?.firstOrNull()?.musicTwoRowItemRenderer
        ?.thumbnailRenderer
        ?.musicThumbnailRenderer
        ?.thumbnail
        ?.thumbnails
        ?.firstOrNull()?.toThumbnail()

    val thumbnail1 = renderer
        .contents?.firstOrNull()?.musicResponsiveListItemRenderer
        ?.thumbnail
        ?.musicThumbnailRenderer
        ?.thumbnail
        ?.thumbnails
        ?.firstOrNull()?.toThumbnail()

    return Environment.PlaylistItem(
        info = Environment.Info(
            name = renderer.header?.musicCarouselShelfBasicHeaderRenderer?.title?.runs?.firstOrNull()?.text,
            endpoint = it.fast4x.environment.models.NavigationEndpoint.Endpoint.Browse(
                browseId = renderer
                    .header?.musicCarouselShelfBasicHeaderRenderer?.title?.runs?.firstOrNull()?.navigationEndpoint?.browseEndpoint?.browseID,
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

fun Environment.ArtistItem.Companion.from(renderer: List<MusicCarouselShelfRendererContent>): List<Environment.ArtistItem> {

    val thumbnail = renderer.firstOrNull()?.musicResponsiveListItemRenderer
        ?.thumbnail
        ?.musicThumbnailRenderer
        ?.thumbnail
        ?.thumbnails
        ?.firstOrNull()?.toThumbnail()

    return listOf(Environment.ArtistItem(
        info = Environment.Info(
            name = renderer.firstOrNull()?.musicResponsiveListItemRenderer
                ?.flexColumns?.firstOrNull()
                ?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()
                ?.text,
            endpoint = it.fast4x.environment.models.NavigationEndpoint.Endpoint.Browse(
                browseId = renderer.firstOrNull()?.musicResponsiveListItemRenderer
                ?.navigationEndpoint?.browseEndpoint?.browseID,
                params = null,
                browseEndpointContextSupportedConfigs = null
            )
        ),
        subscribersCountText = renderer.firstOrNull()?.musicResponsiveListItemRenderer
        ?.flexColumns?.getOrNull(1)
            ?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()
            ?.text,
        thumbnail = thumbnail
    ))

}