package it.fast4x.rimusic.extensions.webpotoken

import com.dd3boh.outertune.utils.potoken.PoTokenGenerator
import io.ktor.client.call.body
import it.fast4x.innertube.Innertube.player
import it.fast4x.innertube.Innertube.playerWithWebPoToken
import it.fast4x.innertube.models.PlayerResponse
import it.fast4x.innertube.models.bodies.PlayerBody
import it.fast4x.rimusic.utils.getSignatureTimestampOrNull

suspend fun advancedPoTokenPlayer(body: PlayerBody): Result<Triple<String?, PlayerResponse, String?>> = runCatching{
    val poTokenGenerator = PoTokenGenerator()
    val signatureTimestamp = getSignatureTimestampOrNull(body.videoId)
    val (webPlayerPot, webStreamingPot) = poTokenGenerator.getWebClientPoToken(body.videoId)?.let {
        Pair(it.playerRequestPoToken, it.streamingDataPoToken)
    } ?: Pair(null, null)

    val response = playerWithWebPoToken(
        body.videoId,
        body.playlistId,
        signatureTimestamp,
        webPlayerPot
    ).body<PlayerResponse>()

    println("advancedPoTokenPlayer webStreamingPot: $webStreamingPot webPlayerPot: $webPlayerPot signatureTimestamp: $signatureTimestamp")
    println("advancedPoTokenPlayer response urls: ${response.streamingData?.adaptiveFormats?.map { it.url }}")

    return@runCatching Triple(null, response, webStreamingPot)

}