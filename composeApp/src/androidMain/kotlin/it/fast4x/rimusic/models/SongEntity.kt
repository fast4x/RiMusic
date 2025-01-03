package it.fast4x.rimusic.models

import androidx.compose.runtime.Immutable
import androidx.room.Embedded


@Immutable
data class SongEntity(
    @Embedded val song: Song,
    val contentLength: Long? = null,
    val albumTitle: String? = null,
){
    fun relativePlayTime(): Double {
        return this.song.relativePlayTime()
    }
}