package me.knighthat.innertube.response

import javax.sound.sampled.AudioInputStream

data class PlayerResponse(
    val audioStreams: List<AudioStream>
) {

    val autoMaxQualityFormat: AudioStream
        get() = highestQualityFormat

    val highestQualityFormat: AudioStream
        get() = audioStreams.sortedBy(AudioStream::bitrate).last()

    val lowestQualityFormat: AudioStream
        get() = audioStreams.sortedBy(AudioStream::bitrate).first()

    val mediumQualityFormat: AudioStream
        get() = audioStreams.sortedBy(AudioStream::bitrate)[audioStreams.size / 2]


    data class AudioStream(
        val url: String,
        val bitrate: Int
    )
}