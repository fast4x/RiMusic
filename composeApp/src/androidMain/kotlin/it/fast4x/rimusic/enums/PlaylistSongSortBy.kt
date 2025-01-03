package it.fast4x.rimusic.enums

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import it.fast4x.rimusic.R

enum class PlaylistSongSortBy(
    @StringRes val textId: Int,
    @DrawableRes val iconId: Int
): MenuTitle, Drawable {

    Album( R.string.sort_album, R.drawable.album ),

    AlbumYear( R.string.sort_album_year, R.drawable.calendar ),

    Artist( R.string.sort_artist, R.drawable.artist ),

    ArtistAndAlbum( -1, R.drawable.artist ),

    DatePlayed( R.string.sort_date_played, R.drawable.up_right_arrow ),

    PlayTime( R.string.sort_listening_time, R.drawable.trending ),

    RelativePlayTime(R.string.sort_listening_time, R.drawable.trending), // TODO different icon than PlayTime

    Position( R.string.sort_position, R.drawable.position ),

    Title( R.string.sort_title, R.drawable.text ),

    Duration( R.string.sort_duration, R.drawable.time ),

    DateLiked( R.string.sort_date_liked, R.drawable.heart ),

    DateAdded( R.string.sort_date_added, R.drawable.time );

    override val titleId: Int
        get() {
            // Due to the requirement of 2 separated strings,
            // this needs to be handled separately
            if( this == ArtistAndAlbum )
                throw UnsupportedOperationException()

            return this.textId
        }

    override val icon: Painter
        @Composable
        get() = painterResource( this.iconId )
}
