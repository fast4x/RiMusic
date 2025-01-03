package it.fast4x.rimusic.ui.components.themed

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.fast4x.rimusic.R
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.typography


@Composable
fun SortMenu (
    title: String? = null,
    onDismiss: () -> Unit,
    onTitle: (() -> Unit)? = null,
    onDatePlayed: (() -> Unit)? = null,
    onPlayTime: (() -> Unit)? = null,
    onRelativePlayTime: (() -> Unit)? = null,
    onName: (() -> Unit)? = null,
    onSongNumber: (() -> Unit)? = null,
    onPosition: (() -> Unit)? = null,
    onArtist: (() -> Unit)? = null,
    onArtistAndAlbum: (() -> Unit)? = null,
    onAlbum: (() -> Unit)? = null,
    onAlbumYear: (() -> Unit)? = null,
    onYear: (() -> Unit)? = null,
    onDateAdded: (() -> Unit)? = null,
    onDateLiked: (() -> Unit)? = null,
    onDuration: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var height by remember {
        mutableStateOf(0.dp)
    }
    val density = LocalDensity.current

    Menu(
        modifier = modifier
            .onPlaced { height = with(density) { it.size.height.toDp() } }

    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .padding(end = 12.dp)
        ) {
            if (title != null) {
                BasicText(
                    text = title,
                    style = typography().m.semiBold,
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 24.dp)
                )
            }
        }

        Spacer(
            modifier = Modifier
                .height(8.dp)
        )

        onTitle?.let {
            MenuEntry(
                icon = R.drawable.text,
                text = stringResource(R.string.sort_title),
                onClick = {
                    onDismiss()
                    onTitle()
                }
            )
        }
        onDatePlayed?.let {
            MenuEntry(
                icon = R.drawable.up_right_arrow,
                text = stringResource(R.string.sort_date_played),
                onClick = {
                    onDismiss()
                    onDatePlayed()
                }
            )
        }
        onDateLiked?.let {
            MenuEntry(
                icon = R.drawable.heart,
                text = stringResource(R.string.sort_date_liked),
                onClick = {
                    onDismiss()
                    onDateLiked()
                }
            )
        }
        onPlayTime?.let {
            MenuEntry(
                icon = R.drawable.trending,
                text = stringResource(R.string.sort_listening_time),
                onClick = {
                    onDismiss()
                    onPlayTime()
                }
            )
        }
        onRelativePlayTime?.let {
            MenuEntry(
                icon = R.drawable.trending,
                text = stringResource(R.string.sort_relative_listening_time),
                onClick = {
                    onDismiss()
                    onRelativePlayTime()
                }
            )
        }
        onName?.let {
            MenuEntry(
                icon = R.drawable.text,
                text = stringResource(R.string.sort_name),
                onClick = {
                    onDismiss()
                    onName()
                }
            )
        }
        onSongNumber?.let {
            MenuEntry(
                icon = R.drawable.medical,
                text = stringResource(R.string.sort_songs_number),
                onClick = {
                    onDismiss()
                    onSongNumber()
                }
            )
        }
        onPosition?.let {
            MenuEntry(
                icon = R.drawable.position,
                text = stringResource(R.string.sort_position),
                onClick = {
                    onDismiss()
                    onPosition()
                }
            )
        }
        onArtist?.let {
            MenuEntry(
                icon = R.drawable.artist,
                text = stringResource(R.string.sort_artist),
                onClick = {
                    onDismiss()
                    onArtist()
                }
            )
        }
        onAlbum?.let {
            MenuEntry(
                icon = R.drawable.album,
                text = stringResource(R.string.sort_album),
                onClick = {
                    onDismiss()
                    onAlbum()
                }
            )
        }
        onArtistAndAlbum?.let {
            MenuEntry(
                icon = R.drawable.artist,
                text = "${stringResource(R.string.sort_artist)}, ${stringResource(R.string.sort_album)}",
                onClick = {
                    onDismiss()
                    onArtistAndAlbum()
                }
            )
        }
        onAlbumYear?.let {
            MenuEntry(
                icon = R.drawable.calendar,
                text = stringResource(R.string.sort_album_year),
                onClick = {
                    onDismiss()
                    onAlbumYear()
                }
            )
        }
        onYear?.let {
            MenuEntry(
                icon = R.drawable.calendar,
                text = stringResource(R.string.sort_year),
                onClick = {
                    onDismiss()
                    onYear()
                }
            )
        }
        onDateAdded?.let {
            MenuEntry(
                icon = R.drawable.time,
                text = stringResource(R.string.sort_date_added),
                onClick = {
                    onDismiss()
                    onDateAdded()
                }
            )
        }

        onDuration?.let {
            MenuEntry(
                icon = R.drawable.time,
                text = stringResource(R.string.sort_duration),
                onClick = {
                    onDismiss()
                    onDuration()
                }
            )
        }

    }
}