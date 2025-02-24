package it.fast4x.rimusic.service.modern

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import it.fast4x.innertube.Innertube
import it.fast4x.innertube.models.PlayerResponse
import it.fast4x.innertube.models.bodies.PlayerBody
import it.fast4x.innertube.requests.player
import it.fast4x.innertube.requests.playerAdvanced
import it.fast4x.invidious.Invidious
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.enums.AudioQualityFormat
import it.fast4x.rimusic.extensions.webpotoken.advancedPoTokenPlayer
import it.fast4x.rimusic.isConnectionMeteredEnabled
import it.fast4x.rimusic.models.Format
import it.fast4x.rimusic.service.LoginRequiredException
import it.fast4x.rimusic.service.NoInternetException
import it.fast4x.rimusic.service.TimeoutException
import it.fast4x.rimusic.service.UnknownException
import it.fast4x.rimusic.service.UnplayableException
import it.fast4x.rimusic.service.isLocal
import it.fast4x.rimusic.ui.screens.settings.isYouTubeLoggedIn
import it.fast4x.rimusic.ui.screens.settings.isYouTubeLoginEnabled
import it.fast4x.rimusic.useYtLoginOnlyForBrowse
import it.fast4x.rimusic.utils.getSignatureTimestampOrNull
import it.fast4x.rimusic.utils.getStreamUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import me.knighthat.piped.Piped
import me.knighthat.piped.request.player
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@OptIn(UnstableApi::class)
internal suspend fun PlayerServiceModern.dataSpecProcess(
    dataSpec: DataSpec,
    context: Context,
    connectionMetered: Boolean = false
): DataSpec {
    val songUri = dataSpec.uri.toString()
    val videoId = songUri.substringAfter("watch?v=")
    val chunkLength = 512 * 1024L
    val length = if (dataSpec.length >= 0) dataSpec.length else 1

    println("PlayerServiceModern DataSpecProcess Playing song ${videoId} dataSpec position ${dataSpec.position} length ${dataSpec.length}")
    if( dataSpec.isLocal ||
        cache.isCached(videoId, dataSpec.position, chunkLength) ||
        downloadCache.isCached(videoId, dataSpec.position, length)
    ) {
        println("PlayerServiceModern DataSpecProcess Playing song ${videoId} from cached or local file")
        return dataSpec //.withUri(Uri.parse(dataSpec.uri.toString()))
    }

    //var dataSpecReturn: DataSpec = dataSpec
    try {
        //runBlocking(Dispatchers.IO) {
            //if loggedin use advanced player with webPotoken and new newpipe extractor
            val format = if (!isYouTubeLoggedIn()) getInnerTubeStream(videoId, audioQualityFormat, connectionMetered)
            else getAvancedInnerTubeStream(videoId, audioQualityFormat, connectionMetered)

            println("PlayerServiceModern DataSpecProcess Playing song ${videoId} from url=${format?.url}")
            return dataSpec.withUri(Uri.parse(format?.url)).subrange(dataSpec.uriPositionOffset, chunkLength)
        //}


    } catch ( e: Exception ) {
        println("PlayerServiceModern DataSpecProcess Error: ${e.stackTraceToString()}")
        val format = getInnerTubeStream(videoId, audioQualityFormat, connectionMetered)
        return dataSpec.withUri(Uri.parse(format?.url)).subrange(dataSpec.uriPositionOffset, chunkLength)

//        println("PlayerServiceModern DataSpecProcess Playing song $videoId from ALTERNATIVE url")
//        val alternativeUrl = "https://jossred.josprox.com/yt/stream/$videoId"
//        return dataSpec.withUri(alternativeUrl.toUri())

        // Temporary disabled piped and invidious
//        try {
//            // Switch to Piped
//            val formatUrl = getPipedFormatUrl( videoId, audioQualityFormat )
//
//            println("PlayerServiceModern DataSpecProcess Playing song $videoId from url $formatUrl")
//            return dataSpec.withUri( formatUrl )
//
//        } catch ( e: NoSuchElementException ) {
//            // Switch to Invidious
//            val formatUrl = getInvidiousFormatUrl( videoId, audioQualityFormat )
//
//            println("PlayerServiceModern DataSpecProcess Playing song $videoId from url $formatUrl")
//            return dataSpec.withUri( formatUrl )
//        }

    } catch ( e: Exception ) {
        // Rethrow exception if it's not handled
        throw e
    }
}

@OptIn(UnstableApi::class)
suspend fun getAvancedInnerTubeStream(
    videoId: String,
    audioQualityFormat: AudioQualityFormat,
    connectionMetered: Boolean
): PlayerResponse.StreamingData.Format? {
    return advancedPoTokenPlayer(
        body = PlayerBody(videoId = videoId),
    ).fold(
        { playerResponse ->
            println("PlayerServiceModern MyDownloadHelper DataSpecProcess getInnerTubeStream playabilityStatus ${playerResponse.second?.playabilityStatus?.status} for song $videoId from adaptiveFormats itag ${playerResponse.second?.streamingData?.adaptiveFormats?.map { it.itag }}")
            //println("PlayerServiceModern MyDownloadHelper DataSpecProcess getInnerTubeStream playabilityStatus ${playerResponse.second?.playabilityStatus?.status} for song $videoId from formats itag ${playerResponse.second?.streamingData?.formats?.map { it.itag }}")
            when(playerResponse.second?.playabilityStatus?.status) {
                "OK" -> {
                    // SELECT FORMAT BY ITAG
                    when (audioQualityFormat) {
                        AudioQualityFormat.Auto -> if (connectionMetered && isConnectionMeteredEnabled()) playerResponse.second?.streamingData?.mediumQualityFormat
                        else playerResponse.second?.streamingData?.autoMaxQualityFormat
                        AudioQualityFormat.High -> playerResponse.second?.streamingData?.highestQualityFormat
                        AudioQualityFormat.Medium -> playerResponse.second?.streamingData?.mediumQualityFormat
                        AudioQualityFormat.Low -> playerResponse.second?.streamingData?.lowestQualityFormat
                    }
                        // *********************

                        // SELECT FORMAT BY BITRATE
//                    val selectedFormat = playerResponse.streamingData?.formats?.filter { it.isAudio }
//                        ?: playerResponse.streamingData?.adaptiveFormats?.map { it.asFormat }?.filter { it.isAudio }
//                    playerResponse.streamingData?.adaptiveFormats
//                        ?.filter { it.isAudio }
//                        ?.maxByOrNull {
//                            ( it.bitrate?.times(
//                                when (audioQualityFormat) {
//                                    AudioQualityFormat.Auto -> if (!isConnectionMeteredEnabled() && !connectionMetered) 2 else 1
//                                    AudioQualityFormat.High -> 2
//                                    AudioQualityFormat.Medium -> 1
//                                    AudioQualityFormat.Low -> 0
//                                }
//                            ) ?: 0 ) + (if (it.mimeType.startsWith("audio/webm")) 10240 else 0)
//                        }
                        // *********************

                        .let {
                            it?.copy(url = getStreamUrl(it, videoId))
                        }
                        .let {
                            if (playerResponse.first != null) {
                                it?.copy(url = it.url?.plus("&cpn=${playerResponse.first}&range=0-${it.contentLength ?: 10000000}"))
                            } else {
                                it?.copy(url = it.url?.plus("&range=0-${it.contentLength ?: 10000000}"))
                            }
                        }
                        .let {
                            if (playerResponse.third != null)
                                it?.copy(url = it.url?.plus("&pot=${playerResponse.third}"))
                            else it
                        }
                        .also {
                            println("PlayerServiceModern MyDownloadHelper DataSpecProcess getAdvancedInnerTubeStream url ${it?.url}")

                            println("PlayerServiceModern MyDownloadHelper DataSpecProcess getInnerTubeStream song $videoId itag selected ${it}")
                            //println("PlayerServiceModern MyDownloadHelper DataSpecProcess getMediaFormat before upsert format $it")
                            Database.asyncTransaction {
                                if (songExist(videoId) > 0)
                                    upsert(
                                        Format(
                                            songId = videoId,
                                            itag = it?.itag?.toInt(),
                                            mimeType = it?.mimeType,
                                            contentLength = it?.contentLength,
                                            bitrate = it?.bitrate?.toLong(),
                                            lastModified = it?.lastModified,
                                            loudnessDb = it?.loudnessDb?.toFloat()
                                        )
                                    )
                            }
                            //println("PlayerServiceModern MyDownloadHelper DataSpecProcess getMediaFormat after upsert format $it")
                        }
                }
                "LOGIN_REQUIRED" -> throw LoginRequiredException()
                "UNPLAYABLE" -> throw UnplayableException()
                else -> throw UnknownException()
            }
        },
        { throwable ->
            when (throwable) {
                is ConnectException, is UnknownHostException -> {
                    throw NoInternetException()
                }

                is SocketTimeoutException -> {
                    throw TimeoutException()
                }

                else -> {
                    println("PlayerServiceModern MyDownloadHelper DataSpecProcess Error: ${throwable.stackTraceToString()}")
                    throw throwable
                }
            }

        }
    )
}

@OptIn(UnstableApi::class)
suspend fun getInnerTubeStream(
    videoId: String,
    audioQualityFormat: AudioQualityFormat,
    connectionMetered: Boolean
): PlayerResponse.StreamingData.Format? {
    return Innertube.playerAdvanced(
        body = PlayerBody(videoId = videoId),
        withLogin =  (!useYtLoginOnlyForBrowse() && isYouTubeLoginEnabled() && isYouTubeLoggedIn()),
    ).fold(
        { playerResponse ->
            println("PlayerServiceModern MyDownloadHelper DataSpecProcess getInnerTubeStream playabilityStatus ${playerResponse.second?.playabilityStatus?.status} for song $videoId from adaptiveFormats itag ${playerResponse.second?.streamingData?.adaptiveFormats?.map { it.itag }}")
            //println("PlayerServiceModern MyDownloadHelper DataSpecProcess getInnerTubeStream playabilityStatus ${playerResponse.second?.playabilityStatus?.status} for song $videoId from formats itag ${playerResponse.second?.streamingData?.formats?.map { it.itag }}")
            when(playerResponse.second?.playabilityStatus?.status) {
                "OK" -> {
                    // SELECT FORMAT BY ITAG
                    when (audioQualityFormat) {
                        AudioQualityFormat.Auto -> if (connectionMetered && isConnectionMeteredEnabled()) playerResponse.second?.streamingData?.mediumQualityFormat
                        else playerResponse.second?.streamingData?.autoMaxQualityFormat
                        AudioQualityFormat.High -> playerResponse.second?.streamingData?.highestQualityFormat
                        AudioQualityFormat.Medium -> playerResponse.second?.streamingData?.mediumQualityFormat
                        AudioQualityFormat.Low -> playerResponse.second?.streamingData?.lowestQualityFormat
                    }
                        // *********************

                        // SELECT FORMAT BY BITRATE
//                    val selectedFormat = playerResponse.streamingData?.formats?.filter { it.isAudio }
//                        ?: playerResponse.streamingData?.adaptiveFormats?.map { it.asFormat }?.filter { it.isAudio }
//                    playerResponse.streamingData?.adaptiveFormats
//                        ?.filter { it.isAudio }
//                        ?.maxByOrNull {
//                            ( it.bitrate?.times(
//                                when (audioQualityFormat) {
//                                    AudioQualityFormat.Auto -> if (!isConnectionMeteredEnabled() && !connectionMetered) 2 else 1
//                                    AudioQualityFormat.High -> 2
//                                    AudioQualityFormat.Medium -> 1
//                                    AudioQualityFormat.Low -> 0
//                                }
//                            ) ?: 0 ) + (if (it.mimeType.startsWith("audio/webm")) 10240 else 0)
//                        }
                        // *********************

                        .let {
                            if (playerResponse.first != null) {
                                it?.copy(url = it.url?.plus("&cpn=${playerResponse.first}&range=0-${it.contentLength ?: 10000000}"))
                            } else {
                                it?.copy(url = it.url?.plus("&range=0-${it.contentLength ?: 10000000}"))
                            }
                        }
                        .also {
                            println("PlayerServiceModern MyDownloadHelper DataSpecProcess getInnerTubeStream song $videoId itag selected ${it}")
                            //println("PlayerServiceModern MyDownloadHelper DataSpecProcess getMediaFormat before upsert format $it")
                            Database.asyncTransaction {
                                if (songExist(videoId) > 0)
                                    upsert(
                                        Format(
                                            songId = videoId,
                                            itag = it?.itag?.toInt(),
                                            mimeType = it?.mimeType,
                                            contentLength = it?.contentLength,
                                            bitrate = it?.bitrate?.toLong(),
                                            lastModified = it?.lastModified,
                                            loudnessDb = it?.loudnessDb?.toFloat()
                                        )
                                    )
                            }
                            //println("PlayerServiceModern MyDownloadHelper DataSpecProcess getMediaFormat after upsert format $it")
                        }
                }
                "LOGIN_REQUIRED" -> throw LoginRequiredException()
                "UNPLAYABLE" -> throw UnplayableException()
                else -> throw UnknownException()
            }
        },
        { throwable ->
            when (throwable) {
                is ConnectException, is UnknownHostException -> {
                    throw NoInternetException()
                }

                is SocketTimeoutException -> {
                    throw TimeoutException()
                }

                else -> {
                    println("PlayerServiceModern MyDownloadHelper DataSpecProcess Error: ${throwable.stackTraceToString()}")
                    throw throwable
                }
            }

        }
    )
}

// TODO remove in the future
@OptIn(UnstableApi::class)
suspend fun getInnerTubeFormatUrl(
    videoId: String,
    audioQualityFormat: AudioQualityFormat,
    connectionMetered: Boolean,
    signatureTimestamp: Int? = getSignatureTimestampOrNull(videoId)
): PlayerResponse.StreamingData.Format? {
    return Innertube.player(
        body = PlayerBody(videoId = videoId),
        //TODO manage login
        withLogin =  (!useYtLoginOnlyForBrowse() && isYouTubeLoginEnabled() && isYouTubeLoggedIn()),
        signatureTimestamp = signatureTimestamp
    ).fold(
        { playerResponse ->
            println("PlayerServiceModern MyDownloadHelper DataSpecProcess getInnertubeFormat playabilityStatus ${playerResponse.playabilityStatus?.status} for song $videoId from adaptiveFormats itag ${playerResponse.streamingData?.adaptiveFormats?.map { it.itag }}")
            println("PlayerServiceModern MyDownloadHelper DataSpecProcess getInnertubeFormat playabilityStatus ${playerResponse.playabilityStatus?.status} for song $videoId from formats itag ${playerResponse.streamingData?.formats?.map { it.itag }}")
            when(playerResponse.playabilityStatus?.status) {
                "OK" -> {
                    // SELECT FORMAT BY ITAG
                    when (audioQualityFormat) {
                        AudioQualityFormat.Auto -> if (!isConnectionMeteredEnabled() && !connectionMetered) playerResponse.streamingData?.autoMaxQualityFormat
                        else playerResponse.streamingData?.lowestQualityFormat
                        AudioQualityFormat.High -> playerResponse.streamingData?.highestQualityFormat
                        AudioQualityFormat.Medium -> playerResponse.streamingData?.mediumQualityFormat
                        AudioQualityFormat.Low -> playerResponse.streamingData?.lowestQualityFormat
                    }
                    // *********************

                    // SELECT FORMAT BY BITRATE
//                    val selectedFormat = playerResponse.streamingData?.formats?.filter { it.isAudio }
//                        ?: playerResponse.streamingData?.adaptiveFormats?.map { it.asFormat }?.filter { it.isAudio }
//                    playerResponse.streamingData?.adaptiveFormats
//                        ?.filter { it.isAudio }
//                        ?.maxByOrNull {
//                            ( it.bitrate?.times(
//                                when (audioQualityFormat) {
//                                    AudioQualityFormat.Auto -> if (!isConnectionMeteredEnabled() && !connectionMetered) 2 else 1
//                                    AudioQualityFormat.High -> 2
//                                    AudioQualityFormat.Medium -> 1
//                                    AudioQualityFormat.Low -> 0
//                                }
//                            ) ?: 0 ) + (if (it.mimeType.startsWith("audio/webm")) 10240 else 0)
//                        }
                    // *********************

                        .let {
                            it?.copy(url = getStreamUrl(it, videoId))
                        }
//                    .let {
//                        // recover streaming url with newpipe extractor and specify range to avoid YouTube's throttling
//                        if (it?.url != null)
//                            it.copy(url = "${it.url}&range=0-${it.contentLength ?: 10000000}")
//                        else
//                            it?.copy(url = getStreamUrl(it, videoId))
//
//                    }
                    .also {
                        println("PlayerServiceModern MyDownloadHelper DataSpecProcess getInnertubeFormat song $videoId itag selected ${it}")
                        //println("PlayerServiceModern MyDownloadHelper DataSpecProcess getMediaFormat before upsert format $it")
                        Database.asyncTransaction {
                            if (songExist(videoId) > 0)
                                upsert(
                                    Format(
                                        songId = videoId,
                                        itag = it?.itag?.toInt(),
                                        mimeType = it?.mimeType,
                                        contentLength = it?.contentLength,
                                        bitrate = it?.bitrate?.toLong(),
                                        lastModified = it?.lastModified,
                                        loudnessDb = it?.loudnessDb?.toFloat()
                                    )
                                )
                        }
                        //println("PlayerServiceModern MyDownloadHelper DataSpecProcess getMediaFormat after upsert format $it")
                    }
                }
                "LOGIN_REQUIRED" -> throw LoginRequiredException()
                "UNPLAYABLE" -> throw UnplayableException()
                else -> throw UnknownException()
            }
        },
        { throwable ->
            when (throwable) {
                is ConnectException, is UnknownHostException -> {
                    throw NoInternetException()
                }

                is SocketTimeoutException -> {
                    throw TimeoutException()
                }

                else -> {
                    println("PlayerServiceModern MyDownloadHelper DataSpecProcess Error: ${throwable.stackTraceToString()}")
                    throw throwable
                }
            }

        }
    )
}

private suspend fun getPipedFormatUrl(
    videoId: String,
    audioQualityFormat: AudioQualityFormat
): Uri {
    val format = Piped.player( videoId )?.fold(
        {
            when (audioQualityFormat) {
                AudioQualityFormat.Auto -> it?.autoMaxQualityFormat
                AudioQualityFormat.High -> it?.highestQualityFormat
                AudioQualityFormat.Medium -> it?.mediumQualityFormat
                AudioQualityFormat.Low -> it?.lowestQualityFormat
            }.also {
                //println("PlayerService MyDownloadHelper DataSpecProcess getPipedFormatUrl before upsert format $it")
                Database.asyncTransaction {
                    if ( songExist(videoId) > 0 )
                        upsert(
                            Format(
                                songId = videoId,
                                itag = it?.itag?.toInt(),
                                mimeType = it?.mimeType,
                                contentLength = it?.contentLength?.toLong(),
                                bitrate = it?.bitrate?.toLong()
                            )
                        )
                }
                //println("PlayerService MyDownloadHelper DataSpecProcess getPipedFormatUrl after upsert format $it")
            }
        },
        {
            println("PlayerService MyDownloadHelper DataSpecProcess Error: ${it.stackTraceToString()}")
            throw it
        }
    )

    // Return parsed URL to play song or throw error if none of the responses is valid
    return Uri.parse( format?.url ) ?: throw NoSuchElementException( "Could not find any playable format from Piped ($videoId)" )
}

private suspend fun getInvidiousFormatUrl(
    videoId: String,
    audioQualityFormat: AudioQualityFormat
): Uri {
    val format = Invidious.api.videos( videoId )?.fold(
        {
            when( audioQualityFormat ){
                AudioQualityFormat.Auto -> it.autoMaxQualityFormat
                AudioQualityFormat.High -> it.highestQualityFormat
                AudioQualityFormat.Medium -> it.mediumQualityFormat
                AudioQualityFormat.Low -> it.lowestQualityFormat
            }.also {
                //println("PlayerService MyDownloadHelper DataSpecProcess getInvidiousFormatUrl before upsert format $it")
                Database.asyncTransaction {
                    if( songExist(videoId) > 0 )
                        upsert(
                            Format(
                                songId = videoId,
                                itag = it?.itag?.toInt(),
                                mimeType = it?.mimeType,
                                bitrate = it?.bitrate?.toLong()
                            )
                        )
                }
                //println("PlayerService MyDownloadHelper DataSpecProcess getInvidiousFormatUrl after upsert format $it")
            }
        },
        {
            println("PlayerService MyDownloadHelper DataSpecProcess Error: ${it.stackTraceToString()}")
            throw it
        }
    )

    // Return parsed URL to play song or throw error if none of the responses is valid
    return Uri.parse( format?.url ) ?: throw NoSuchElementException( "Could not find any playable format from Piped ($videoId)" )
}


