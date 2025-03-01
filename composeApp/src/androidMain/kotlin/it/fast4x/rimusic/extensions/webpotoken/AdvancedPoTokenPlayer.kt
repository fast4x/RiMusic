package it.fast4x.rimusic.extensions.webpotoken

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import io.ktor.client.call.body
import it.fast4x.environment.Environment.playerWithWebPoToken
import it.fast4x.environment.models.PlayerResponse
import it.fast4x.environment.models.bodies.PlayerBody
import it.fast4x.rimusic.service.UnplayableException
import it.fast4x.rimusic.utils.getSignatureTimestampOrNull

@OptIn(UnstableApi::class)
suspend fun advancedWebPoTokenPlayer(body: PlayerBody): Result<Triple<String?, PlayerResponse?, String?>> =
    runCatching {

        val maxRetries = 2
        var retryCount = 0
        var loop = true
        var response: PlayerResponse? = null

        println("advancedPoTokenPlayer with login videoId ${body.videoId}")


        val poTokenGenerator = PoTokenGenerator()
        val signatureTimestamp = getSignatureTimestampOrNull(body.videoId)
        val (webPlayerPot, webStreamingPot) = poTokenGenerator.getWebClientPoToken(body.videoId)
            ?.let {
                Pair(it.playerRequestPoToken, it.streamingDataPoToken)
            } ?: Pair(null, null)

//    while (loop == true) {
//
//        response = playerWithWebPoToken(
//            body.videoId,
//            body.playlistId,
//            signatureTimestamp,
//            webPlayerPot
//        ).body<PlayerResponse>()
//
//        println("advancedPoTokenPlayer with login webStreamingPot: $webStreamingPot webPlayerPot: $webPlayerPot signatureTimestamp: $signatureTimestamp")
//
//
//        if (response.playabilityStatus?.status == "OK" || retryCount >= maxRetries)
//            loop = false
//
//        println("advancedPoTokenPlayer retryCount: $retryCount")
//        retryCount++
//    }

        val call = if (signatureTimestamp != null && webPlayerPot != null)
            playerWithWebPoToken(
                body.videoId,
                body.playlistId,
                signatureTimestamp,
                webPlayerPot
            ) else throw UnplayableException()

        if (call.status.value == 200)
            response = call.body<PlayerResponse>()
        else throw UnplayableException()

        println("advancedPoTokenPlayer with login webStreamingPot: $webStreamingPot webPlayerPot: $webPlayerPot signatureTimestamp: $signatureTimestamp")


        return@runCatching Triple(null, response, webStreamingPot)

    }