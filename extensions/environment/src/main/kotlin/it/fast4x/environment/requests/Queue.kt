package it.fast4x.environment.requests

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import it.fast4x.environment.Environment
import it.fast4x.environment.models.GetQueueResponse
import it.fast4x.environment.models.bodies.QueueBody
import it.fast4x.environment.utils.from
import it.fast4x.environment.utils.runCatchingNonCancellable

suspend fun Environment.queue(body: QueueBody) = runCatchingNonCancellable {
    val response = client.post(_QPWiB5riY1) {
        setBody(body)
        mask("queueDatas.content.$playlistPanelVideoRendererMask")
    }.body<GetQueueResponse>()

    response
        .queueDatas
        ?.mapNotNull { queueData ->
            queueData
                .content
                ?.playlistPanelVideoRenderer
                ?.let(Environment.SongItem::from)
        }
}

suspend fun Environment.song(videoId: String): Result<Environment.SongItem?>? =
    queue(QueueBody(videoIds = listOf(videoId)))?.map { it?.firstOrNull() }
