package me.knighthat.innertube.request

import io.ktor.client.call.body
import io.ktor.client.request.get
import it.fast4x.innertube.utils.runCatchingNonCancellable
import me.knighthat.innertube.Piped
import me.knighthat.innertube.response.PlayerResponse

suspend fun Piped.player(
    videoId: String,
) = runCatchingNonCancellable {
    client.get("/streams/$videoId").body<PlayerResponse>()
}