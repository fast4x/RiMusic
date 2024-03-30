package it.fast4x.rimusic.models.ui

import androidx.media3.common.MediaItem

data class UiMedia(
    val id: String,
    val title: String,
    val artist: String,
    val duration: Long
)

fun MediaItem.toUiMedia(duration: Long) = UiMedia(
    id = mediaId,
    title = mediaMetadata.title?.toString() ?: "",
    artist = mediaMetadata.artist?.toString() ?: "",
    duration = duration
)