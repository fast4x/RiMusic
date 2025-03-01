package it.fast4x.environment.requests


import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import it.fast4x.environment.Environment
import it.fast4x.environment.models.BrowseResponse
import it.fast4x.environment.models.MusicCarouselShelfRenderer
import it.fast4x.environment.models.NextResponse
import it.fast4x.environment.models.bodies.BrowseBody
import it.fast4x.environment.models.bodies.NextBody
import it.fast4x.environment.utils.findSectionByTitle
import it.fast4x.environment.utils.from
import it.fast4x.environment.utils.runCatchingNonCancellable



suspend fun Environment.relatedSongs(body: NextBody) = runCatchingNonCancellable {
    val nextResponse = client.post(_NXIvG4ve8N) {
        setBody(body)
        mask("contents.singleColumnMusicWatchNextResultsRenderer.tabbedRenderer.watchNextTabbedResultsRenderer.tabs.tabRenderer(endpoint,title)")
    }.body<NextResponse>()

    val browseId = nextResponse
        .contents
        ?.singleColumnMusicWatchNextResultsRenderer
        ?.tabbedRenderer
        ?.watchNextTabbedResultsRenderer
        ?.tabs
        ?.getOrNull(2)
        ?.tabRenderer
        ?.endpoint
        ?.browseEndpoint
        ?.browseId
        ?: return@runCatchingNonCancellable null

    val response = client.post(_3djbhqyLpE) {
        setBody(BrowseBody(browseId = browseId))
        mask("contents.sectionListRenderer.contents.musicCarouselShelfRenderer(header.musicCarouselShelfBasicHeaderRenderer(title,strapline),contents($musicResponsiveListItemRendererMask,$musicTwoRowItemRendererMask))")
    }.body<BrowseResponse>()

    val sectionListRenderer = response
        .contents
        ?.sectionListRenderer



    Environment.RelatedSongs(
        songs = sectionListRenderer
            ?.findSectionByTitle("You might also like")
            ?.musicCarouselShelfRenderer
            ?.contents
            ?.mapNotNull(MusicCarouselShelfRenderer.Content::musicResponsiveListItemRenderer)
            ?.mapNotNull(Environment.SongItem::from)
    )
}
