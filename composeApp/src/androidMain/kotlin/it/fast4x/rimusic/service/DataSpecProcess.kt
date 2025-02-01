package it.fast4x.rimusic.service

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.enums.AudioQualityFormat
import it.fast4x.rimusic.models.Format
import it.fast4x.rimusic.service.modern.PlayerServiceModern
import it.fast4x.rimusic.service.modern.getInnerTubeFormatUrl
import it.fast4x.rimusic.service.modern.getInnerTubeStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import me.knighthat.piped.Piped
import me.knighthat.piped.request.player

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

@OptIn(UnstableApi::class)
internal suspend fun PlayerService.dataSpecProcess(
    dataSpec: DataSpec,
    context: Context,
    connectionMetered: Boolean
): DataSpec {
    val songUri = dataSpec.uri.toString()
    val videoId = songUri.substringAfter("watch?v=")
    val chunkLength = 512 * 1024L

    if( dataSpec.isLocal ||
        cache.isCached(videoId, dataSpec.position, chunkLength) ||
        downloadCache.isCached(videoId, dataSpec.position, if (dataSpec.length >= 0) dataSpec.length else 1)
    ) {
        println("PlayerService DataSpecProcess Playing song ${videoId} from cached or local file")
        return dataSpec.withUri(Uri.parse(dataSpec.uri.toString()))
    }

    try {

        val format = getInnerTubeFormatUrl(videoId, audioQualityFormat, connectionMetered)

        println("PlayerService DataSpecProcess Playing song ${videoId} from format $format from url=${format?.url}")
        return dataSpec.withUri(Uri.parse(format?.url))

    } catch ( e: LoginRequiredException ) {
        try {
            // Switch to Piped
            val formatUrl = getPipedFormatUrl( videoId, audioQualityFormat )

            println("PlayerService DataSpecProcess Playing song $videoId from url $formatUrl")
            return dataSpec.withUri( formatUrl )

        } catch ( e: NoSuchElementException ) {
            throw e
            // Switch to Invidious
//            val formatUrl = getInvidiousFormatUrl( videoId, audioQualityFormat )
//
//            println("PlayerService DataSpecProcess Playing song $videoId from url $formatUrl")
//            return dataSpec.withUri( formatUrl )
        }

    } catch ( e: Exception ) {
        // Rethrow exception if it's not handled
        throw e
    }
}

@OptIn(UnstableApi::class)
internal suspend fun MyDownloadHelper.dataSpecProcess(
    dataSpec: DataSpec,
    context: Context,
    connectionMetered: Boolean = false
): DataSpec {
    val songUri = dataSpec.uri.toString()
    val videoId = songUri.substringAfter("watch?v=")
    val chunkLength = 512 * 1024L
    val length = if (dataSpec.length >= 0) dataSpec.length else 1

    println("MyDownloadHelper DataSpecProcess Playing song ${videoId} dataSpec position ${dataSpec.position} length ${dataSpec.length}")
    if( dataSpec.isLocal ||
        downloadCache.isCached(videoId, dataSpec.position, length)
    ) {
        println("MyDownloadHelper DataSpecProcess download song ${videoId} from cached or local file")
        return dataSpec.withUri(Uri.parse(dataSpec.uri.toString()))
    }

    var dataSpecReturn: DataSpec = dataSpec
    try {
        runBlocking(Dispatchers.IO) {
            val format = getInnerTubeStream(videoId, audioQualityFormat, connectionMetered)
            dataSpecReturn = dataSpec.withUri(Uri.parse(format?.url))
        }
        return dataSpecReturn

    } catch ( e: Exception ) {
        println("MyDownloadHelper DataSpecProcess Error: ${e.stackTraceToString()}")
        println("MyDownloadHelper DataSpecProcess Playing song $videoId from ALTERNATIVE url")
        val alternativeUrl = "https://jossred.josprox.com/yt/stream/$videoId"
        return dataSpec.withUri(alternativeUrl.toUri())

    } catch ( e: Exception ) {
        // Rethrow exception if it's not handled
        throw e
    }
}

//@OptIn(UnstableApi::class)
//internal suspend fun MyDownloadHelper.dataSpecProcess(
//    dataSpec: DataSpec,
//    context: Context,
//    connectionMetered: Boolean = false
//): DataSpec {
//    val songUri = dataSpec.uri.toString()
//    val videoId = songUri.substringAfter("watch?v=")
//
//    if( dataSpec.isLocal ||
//        downloadCache.isCached(videoId, dataSpec.position, if (dataSpec.length >= 0) dataSpec.length else 1)
//    ) {
//        println("MyDownloadHelper DataSpecProcess Playing song ${videoId} from cached or local file")
//        return dataSpec.withUri(Uri.parse(dataSpec.uri.toString()))
//    }
//
//    // specify range to avoid YouTube's throttling in download
//    val format = getInnerTubeFormatUrl(videoId, audioQualityFormat, connectionMetered)
//        ?.let { it.copy( url = "${it.url}&range=0-${it.contentLength ?: 10000000}") }
//
//    println("MyDownloadHelper DataSpecProcess Playing song $videoId from format $format from url=${format?.url}")
//    return dataSpec.withUri(Uri.parse(format?.url))
//
//}
