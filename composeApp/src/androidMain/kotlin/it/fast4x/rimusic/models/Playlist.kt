package it.fast4x.rimusic.models

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val browseId: String? = null,
    val isEditable: Boolean = true,
    val isYoutubePlaylist: Boolean = false,
)
