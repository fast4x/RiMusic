package it.fast4x.rimusic.ui.components.themed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import it.fast4x.compose.persist.persistList
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.PIPED_PREFIX
import it.fast4x.rimusic.R
import it.fast4x.rimusic.cleanPrefix
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.models.PlaylistPreview
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.ui.items.PlaylistItem
import it.fast4x.rimusic.utils.thumbnail

@Composable
fun Playlist(
    playlist: PlaylistPreview,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
    showName: Boolean = true,
    disableScrollingText: Boolean,
    thumbnailUrl: String? = null,
) {
    var songs by persistList<Song>("playlist${playlist.playlist.id}/songsThumbnails")
    LaunchedEffect(playlist.playlist.id) {
        Database.songsPlaylistTop4Positions(playlist.playlist.id).collect{ songs = it }
    }
    val thumbnails = songs
        .takeWhile { it.thumbnailUrl?.isNotEmpty() ?: false }
        .take(4)
        .map { it.thumbnailUrl.thumbnail(thumbnailSizePx / 2) }


    PlaylistItem(
        thumbnailContent = {
            if (thumbnailUrl != null) {
                AsyncImage(
                    model = thumbnailUrl.thumbnail(thumbnailSizePx),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else if (thumbnails.toSet().size == 1) {
                AsyncImage(
                    model = thumbnails.first().thumbnail(thumbnailSizePx),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    //modifier = it KOTLIN 2
                )
            } else {
                Box(
                    modifier = Modifier // KOTLIN 2
                        .fillMaxSize()
                ) {
                    listOf(
                        Alignment.TopStart,
                        Alignment.TopEnd,
                        Alignment.BottomStart,
                        Alignment.BottomEnd
                    ).forEachIndexed { index, alignment ->
                        AsyncImage(
                            model = thumbnails.getOrNull(index),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .align(alignment)
                                .size(thumbnailSizeDp / 2)
                        )
                    }
                }
            }
            if (playlist.playlist.browseId?.isNotEmpty() == true && !playlist.playlist.name.startsWith(
                    PIPED_PREFIX)
            ) {
                Image(
                    painter = painterResource(R.drawable.ytmusic),
                    colorFilter = ColorFilter.tint(if (playlist.playlist.isYoutubePlaylist) Color.Red.copy(0.75f).compositeOver(
                        Color.White) else colorPalette().text),
                    modifier = Modifier
                        .size(40.dp)
                        .padding(all = 5.dp),
                    contentDescription = "Background Image",
                    contentScale = ContentScale.Fit
                )
            }
            if (playlist.playlist.isYoutubePlaylist && !playlist.playlist.isEditable){
                Image(
                    painter = painterResource(R.drawable.locked),
                    colorFilter = ColorFilter.tint(Color.Red),
                    modifier = Modifier
                        .padding(all = 5.dp)
                        .background(colorPalette().text, CircleShape)
                        .padding(all = 5.dp)
                        .size(18.dp)
                        .align(Alignment.BottomStart),
                    contentDescription = "Background Image",
                    contentScale = ContentScale.Fit
                )
            }
        },
        songCount = playlist.songCount,
        name = cleanPrefix(playlist.playlist.name),
        channelName = null,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier,
        alternative = alternative,
        showName = showName,
        disableScrollingText = disableScrollingText
    )
}