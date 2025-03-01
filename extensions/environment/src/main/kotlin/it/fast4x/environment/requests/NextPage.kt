package it.fast4x.environment.requests

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import it.fast4x.environment.Environment
import it.fast4x.environment.models.ContinuationResponse
import it.fast4x.environment.models.NextResponse
import it.fast4x.environment.models.bodies.ContinuationBody
import it.fast4x.environment.models.bodies.NextBody
import it.fast4x.environment.utils.from
import it.fast4x.environment.utils.runCatchingNonCancellable



suspend fun Environment.nextPage(body: NextBody): Result<Environment.NextPage>? =
    runCatchingNonCancellable {
        val response = client.post(_NXIvG4ve8N) {
            setBody(body)
            mask("contents.singleColumnMusicWatchNextResultsRenderer.tabbedRenderer.watchNextTabbedResultsRenderer.tabs.tabRenderer.content.musicQueueRenderer.content.playlistPanelRenderer(continuations,contents(automixPreviewVideoRenderer,$playlistPanelVideoRendererMask))")
        }.body<NextResponse>()

        val tabs = response
            .contents
            ?.singleColumnMusicWatchNextResultsRenderer
            ?.tabbedRenderer
            ?.watchNextTabbedResultsRenderer
            ?.tabs

        val playlistPanelRenderer = tabs
            ?.getOrNull(0)
            ?.tabRenderer
            ?.content
            ?.musicQueueRenderer
            ?.content
            ?.playlistPanelRenderer

        if (body.playlistId == null) {
            val endpoint = playlistPanelRenderer
                ?.contents
                ?.lastOrNull()
                ?.automixPreviewVideoRenderer
                ?.content
                ?.automixPlaylistVideoRenderer
                ?.navigationEndpoint
                ?.watchPlaylistEndpoint

            if (endpoint != null) {
                return nextPage(
                    body.copy(
                        playlistId = endpoint.playlistId,
                        params = endpoint.params
                    )
                )
            }
        }

        Environment.NextPage(
            playlistId = body.playlistId,
            playlistSetVideoId = body.playlistSetVideoId,
            params = body.params,
            itemsPage = playlistPanelRenderer
                ?.toSongsPage()
        )
    }

suspend fun Environment.nextPage(body: ContinuationBody) = runCatchingNonCancellable {
    val response = client.post(_NXIvG4ve8N) {
        setBody(body)
        mask("continuationContents.playlistPanelContinuation(continuations,contents.$playlistPanelVideoRendererMask)")
    }.body<ContinuationResponse>()

    response
        .continuationContents
        ?.playlistPanelContinuation
        ?.toSongsPage()
}

private fun NextResponse.MusicQueueRenderer.Content.PlaylistPanelRenderer?.toSongsPage() =
    Environment.ItemsPage(
        items = this
            ?.contents
            ?.mapNotNull(NextResponse.MusicQueueRenderer.Content.PlaylistPanelRenderer.Content::playlistPanelVideoRenderer)
            ?.mapNotNull(Environment.SongItem::from),
        continuation = this
            ?.continuations
            ?.firstOrNull()
            ?.nextContinuationData
            ?.continuation
    )
