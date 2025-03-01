package it.fast4x.environment.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class PlayerResponse(
    val playabilityStatus: PlayabilityStatus?,
    val playerConfig: PlayerConfig?,
    val streamingData: StreamingData?,
    val videoDetails: VideoDetails?,
    @SerialName("playbackTracking")
    val playbackTracking: PlaybackTracking?,
) {
    @Serializable
    data class PlayabilityStatus(
        val status: String?,
        val reason: String?
    )

    @Serializable
    data class PlayerConfig(
        val audioConfig: AudioConfig?
    ) {
        @Serializable
        data class AudioConfig(
            val loudnessDb: Float?
        ) {
            // For music clients only
            val normalizedLoudnessDb: Float?
                get() = loudnessDb?.plus(7)
        }
    }

    @Serializable
    data class StreamingData(
        val formats: List<Format>?,
        val adaptiveFormats: List<Format>?,
        val expiresInSeconds: Int?,
    ) {

        val autoMaxQualityFormat: Format?
            get() = adaptiveFormats?.filter { it.url != null || it.signatureCipher != null }
                ?.let { formats ->
                    formats.findLast { it.itag == 774 || it.itag == 251 || it.itag == 141 ||
                            it.itag == 250 || it.itag == 140 ||
                            it.itag == 249 || it.itag == 139 || it.itag == 171
                    } ?: formats.maxByOrNull { it.bitrate ?: 0 }
                }


        val highestQualityFormat: Format?
            get() = adaptiveFormats?.filter { it.url != null || it.signatureCipher != null }
                ?.let { formats ->
                    formats.findLast { it.itag == 774 || it.itag == 251 || it.itag == 140 || it.itag == 141 }
                        ?: formats.maxByOrNull { it.bitrate ?: 0 }
                }

        val mediumQualityFormat: Format?
            get() = adaptiveFormats?.filter { it.url != null || it.signatureCipher != null }
                ?.let { formats ->
                    formats.findLast { it.itag == 250 || it.itag == 140 }
                        ?: formats.maxByOrNull { it.bitrate ?: 0 }
                }

        val lowestQualityFormat: Format?
            get() = adaptiveFormats?.filter { it.url != null || it.signatureCipher != null }
                ?.let { formats ->
                    formats.findLast { it.itag == 249 || it.itag == 139 || it.itag == 171 }
                        ?: formats.maxByOrNull { it.bitrate ?: 0 }
                }



//        @Serializable
//        data class AdaptiveFormat(
//            val itag: Int,
//            val mimeType: String,
//            val bitrate: Long?,
//            val averageBitrate: Long?,
//            val contentLength: Long?,
//            val audioQuality: String?,
//            val approxDurationMs: Long?,
//            val lastModified: Long?,
//            val loudnessDb: Double?,
//            val audioSampleRate: Int?,
//            val url: String?,
//            val width: Int?,
//            val signatureCipher: String?
//        ) {
//            val isAudio: Boolean
//                get() = width == null
//
//            val isVideo: Boolean
//                get() = width != null
//        }

        @Serializable
        data class Format(
            val itag: Int,
            val url: String?,
            val mimeType: String,
            val bitrate: Int,
            val width: Int?,
            val height: Int?,
            val contentLength: Long?,
            val quality: String,
            val fps: Int?,
            val qualityLabel: String?,
            val averageBitrate: Int?,
            val audioQuality: String?,
            val approxDurationMs: String?,
            val audioSampleRate: Int?,
            val audioChannels: Int?,
            val loudnessDb: Double?,
            val lastModified: Long?,
            val signatureCipher: String?,
        ) {
            val isAudio: Boolean
                get() = width == null
        }
    }


    @Serializable
    data class VideoDetails(
        val videoId: String?,
        val title: String?,
        val author: String?,
        val channelId: String?,
        val authorAvatar: String?,
        val authorSubCount: String?,
        val lengthSeconds: String?,
        val musicVideoType: String?,
        val viewCount: String?,
        val thumbnail: Thumbnails?,
        val description: String?,
    )

    @Serializable
    data class PlaybackTracking(
        @SerialName("videostatsPlaybackUrl")
        val videostatsPlaybackUrl: VideostatsPlaybackUrl?,
        @SerialName("videostatsWatchtimeUrl")
        val videostatsWatchtimeUrl: VideostatsWatchtimeUrl?,
        @SerialName("atrUrl")
        val atrUrl: AtrUrl?,
    ) {
        @Serializable
        data class VideostatsPlaybackUrl(
            @SerialName("baseUrl")
            val baseUrl: String?,
        )

        @Serializable
        data class VideostatsWatchtimeUrl(
            @SerialName("baseUrl")
            val baseUrl: String?,
        )
        @Serializable
        data class AtrUrl(
            @SerialName("baseUrl")
            val baseUrl: String?,
        )
    }
}
