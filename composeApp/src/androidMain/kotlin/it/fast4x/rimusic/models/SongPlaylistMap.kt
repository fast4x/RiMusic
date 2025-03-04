package it.fast4x.rimusic.models

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Immutable
@Entity(
    primaryKeys = ["songId", "playlistId"],
    foreignKeys = [
        ForeignKey(
            entity = Song::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Playlist::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SongPlaylistMap(
    @ColumnInfo(index = true) val songId: String,
    @ColumnInfo(index = true) val playlistId: Long,
    val position: Int,
    val setVideoId: String? = null,
    val dateAdded: Long? = null
){
    fun default(): SongPlaylistMap {
        return copy(
            dateAdded = System.currentTimeMillis()
        )
    }
}
