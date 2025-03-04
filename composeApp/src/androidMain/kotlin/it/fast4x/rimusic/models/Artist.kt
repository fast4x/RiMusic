package it.fast4x.rimusic.models

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity
data class Artist(
    @PrimaryKey val id: String,
    val name: String? = null,
    val thumbnailUrl: String? = null,
    val timestamp: Long? = null,
    val bookmarkedAt: Long? = null,
    val isYoutubeArtist: Boolean = false,
)
