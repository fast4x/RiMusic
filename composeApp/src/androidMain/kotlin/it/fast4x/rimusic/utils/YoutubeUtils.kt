package it.fast4x.rimusic.utils

import it.fast4x.innertube.models.PlayerResponse
import it.fast4x.innertube.utils.NewPipeUtils
import timber.log.Timber

fun getSignatureTimestampOrNull(
    videoId: String
): Int? {
    return NewPipeUtils.getSignatureTimestamp(videoId)
        .onFailure {
            Timber.e("NewPipeUtils getSignatureTimestampOrNull Error while getting signature timestamp ${it.stackTraceToString()}")
            println("NewPipeUtils getSignatureTimestampOrNull Error while getting signature timestamp ${it.stackTraceToString()}")
        }
        .getOrNull()
}

fun getStreamUrl(
    format: PlayerResponse.StreamingData.Format,
    videoId: String
): String? {
    val streamUrl =  NewPipeUtils.getStreamUrl(format, videoId)
        .onFailure {
            Timber.e("NewPipeUtils getStreamUrlOrNull Error while getting stream url ${it.stackTraceToString()}")
            println("NewPipeUtils getStreamUrlOrNull Error while getting stream url ${it.stackTraceToString()}")
        }
        .getOrNull()

    println("NewPipeUtils getStreamUrlOrNull streamUrl $streamUrl")

    return streamUrl
}
