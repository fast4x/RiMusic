package it.fast4x.rimusic.ui.items

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import it.fast4x.innertube.Innertube
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.R
import it.fast4x.rimusic.models.PlaylistPreview
import it.fast4x.rimusic.ui.components.themed.TextPlaceholder
import it.fast4x.rimusic.PINNED_PREFIX
import it.fast4x.rimusic.PIPED_PREFIX
import it.fast4x.rimusic.ui.styling.onOverlay
import it.fast4x.rimusic.ui.styling.overlay
import it.fast4x.rimusic.ui.styling.shimmer
import it.fast4x.rimusic.MONTHLY_PREFIX
import it.fast4x.rimusic.cleanPrefix
import it.fast4x.rimusic.utils.checkFileExists
import it.fast4x.rimusic.utils.color
import it.fast4x.rimusic.utils.conditional
import it.fast4x.rimusic.utils.medium
import it.fast4x.rimusic.utils.secondary
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.thumbnail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.thumbnailShape
import it.fast4x.rimusic.typography
import timber.log.Timber

@Composable
fun PlaylistItem(
    @DrawableRes icon: Int,
    colorTint: Color,
    name: String?,
    songCount: Int?,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
    showName: Boolean = true,
    iconSize: Dp = 34.dp,
    disableScrollingText: Boolean,
    isYoutubePlaylist : Boolean = false,
    isEditable : Boolean = false
) {
    PlaylistItem(
        thumbnailContent = {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorTint),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(iconSize)
            )
        },
        songCount = songCount,
        name = name,
        channelName = null,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier,
        alternative = alternative,
        showName = showName,
        disableScrollingText = disableScrollingText,
        isYoutubePlaylist = isYoutubePlaylist,
        isEditable = isEditable
    )
}

@Composable
fun PlaylistItem(
    playlist: PlaylistPreview,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
    showName: Boolean = true,
    disableScrollingText: Boolean,
    isYoutubePlaylist: Boolean,
    isEditable: Boolean
) {
    val context = LocalContext.current

    val playlistThumbnailUrl = remember { mutableStateOf("") }

    fun initialisePlaylistThumbnail (){
        val thumbnailName = "thumbnail/playlist_${playlist.playlist.id}"
        val presentThumbnailUrl: String? = checkFileExists(context, thumbnailName)
        if (presentThumbnailUrl != null) {
            playlistThumbnailUrl.value = presentThumbnailUrl
        }
    }
    initialisePlaylistThumbnail()

    val thumbnails by remember {
        Database.playlistThumbnailUrls(playlist.playlist.id).distinctUntilChanged().map {
            it.map { url ->
                url.thumbnail(thumbnailSizePx / 2)
            }
        }
    }.collectAsState(initial = emptyList(), context = Dispatchers.IO)

    PlaylistItem(
        browseId = playlist.playlist.browseId,
        thumbnailContent = {
            if (playlistThumbnailUrl.value != "") {
                AsyncImage(
                    model = playlistThumbnailUrl.value,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (thumbnails.toSet().size == 1) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(thumbnails.first())
                        .setHeader("User-Agent", "Mozilla/5.0")
                        .build(), //thumbnails.first().thumbnail(thumbnailSizePx),
                    onError = {error ->
                        Timber.e("Failed AsyncImage in PlaylistItem ${error.result.throwable.stackTraceToString()}")
                    },
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
                        val thumbnail = thumbnails.getOrNull(index)
                        if (thumbnail != null)
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(thumbnail)
                                    .setHeader("User-Agent", "Mozilla/5.0")
                                    .build(),
                                onError = {error ->
                                    Timber.e("Failed AsyncImage 1 in PlaylistItem ${error.result.throwable.stackTraceToString()}")
                                },
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .align(alignment)
                                    .size(thumbnailSizeDp / 2)
                            )
                    }
                }
            }
        },
        songCount = playlist.songCount,
        name = playlist.playlist.name,
        channelName = null,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier,
        alternative = alternative,
        showName = showName,
        disableScrollingText = disableScrollingText,
        isYoutubePlaylist = isYoutubePlaylist,
        isEditable = isEditable
    )
}

@Composable
fun PlaylistItem(
    playlist: Innertube.PlaylistItem,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
    showSongsCount: Boolean = true,
    disableScrollingText: Boolean,
    isYoutubePlaylist : Boolean = false,
    isEditable : Boolean = false
) {
    PlaylistItem(
        thumbnailUrl = playlist.thumbnail?.url,
        songCount = playlist.songCount,
        showSongsCount = showSongsCount,
        name = playlist.info?.name,
        channelName = playlist.channel?.name,
        thumbnailSizePx = thumbnailSizePx,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier,
        alternative = alternative,
        disableScrollingText = disableScrollingText,
        isYoutubePlaylist = isYoutubePlaylist,
        isEditable = isEditable
    )
}

@Composable
fun PlaylistItem(
    thumbnailUrl: String?,
    songCount: Int?,
    name: String?,
    channelName: String?,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
    showSongsCount: Boolean = true,
    disableScrollingText: Boolean,
    isYoutubePlaylist : Boolean = false,
    isEditable : Boolean = false
) {
    PlaylistItem(
        thumbnailContent = {
            AsyncImage(
                model = thumbnailUrl?.thumbnail(thumbnailSizePx),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        },
        songCount = songCount,
        showSongsCount = showSongsCount,
        name = name,
        channelName = channelName,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier,
        alternative = alternative,
        disableScrollingText = disableScrollingText,
        isYoutubePlaylist = isYoutubePlaylist,
        isEditable = isEditable
    )
}

@Composable
fun PlaylistItem(
    browseId: String? = null,
    thumbnailContent: @Composable BoxScope.() -> Unit,
    songCount: Int?,
    name: String?,
    channelName: String?,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
    showName: Boolean = true,
    showSongsCount: Boolean = true,
    disableScrollingText: Boolean,
    isYoutubePlaylist : Boolean = false,
    isEditable : Boolean = false
) {
    ItemContainer(
        alternative = alternative,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier
    ) { //centeredModifier ->
        Box(
            modifier = Modifier
                .clip(thumbnailShape())
                .background(color = colorPalette().background4)
                .requiredSize(thumbnailSizeDp)
        ) {
            thumbnailContent(
                /*
                modifier = Modifier
                    .fillMaxSize()

                 */
            )

            name?.let {
                if (it.startsWith(PIPED_PREFIX,0,true)) {
                    Image(
                        painter = painterResource(R.drawable.piped_logo),
                        colorFilter = ColorFilter.tint(colorPalette().red),
                        modifier = Modifier
                            .size(40.dp)
                            .padding(all = 5.dp),
                        contentDescription = "Background Image",
                        contentScale = ContentScale.Fit
                    )
                }
                if (it.startsWith(PINNED_PREFIX,0,true)) {
                    Image(
                        painter = painterResource(R.drawable.pin),
                        colorFilter = ColorFilter.tint(colorPalette().accent),
                        modifier = Modifier
                            .size(40.dp)
                            .padding(all = 5.dp),
                        contentDescription = "Background Image",
                        contentScale = ContentScale.Fit
                    )
                }
                if (it.startsWith(MONTHLY_PREFIX,0,true)) {
                    Image(
                        painter = painterResource(R.drawable.stat_month),
                        colorFilter = ColorFilter.tint(colorPalette().accent),
                        modifier = Modifier
                            .size(40.dp)
                            .padding(all = 5.dp),
                        contentDescription = "Background Image",
                        contentScale = ContentScale.Fit
                    )
                }

            }
            if ((browseId?.isNotEmpty() == true && name?.startsWith(PIPED_PREFIX) == false) || isYoutubePlaylist) {
                Image(
                    painter = painterResource(R.drawable.ytmusic),
                    colorFilter = ColorFilter.tint(if (isYoutubePlaylist) Color.Red.copy(0.75f).compositeOver(Color.White) else colorPalette().text),
                    modifier = Modifier
                        .size(40.dp)
                        .padding(all = 5.dp),
                    contentDescription = "Background Image",
                    contentScale = ContentScale.Fit
                )
            }

            if (isYoutubePlaylist && !isEditable){
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

            if (showSongsCount)
                songCount?.let {
                    BasicText(
                        text = "$songCount",
                        style = typography().xxs.medium.color(colorPalette().onOverlay),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(all = 4.dp)
                            .background(color = colorPalette().overlay, shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                            .align(Alignment.BottomEnd)
                    )
                }

        }


        ItemInfoContainer(
            horizontalAlignment = if (alternative && channelName == null) Alignment.CenterHorizontally else Alignment.Start,
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (showName)
                if (name != null) {
                    BasicText(
                        //text = name.substringAfter(PINNED_PREFIX) ?: "",
                        text = cleanPrefix(name),
                        style = typography().xs.semiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .conditional(!disableScrollingText) { basicMarquee(iterations = Int.MAX_VALUE) }
                    )
                }

            channelName?.let {
                BasicText(
                    text = channelName,
                    style = typography().xs.semiBold.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .conditional(!disableScrollingText) { basicMarquee(iterations = Int.MAX_VALUE) }
                )
            }
        }
    }
}

@Composable
fun PlaylistItemPlaceholder(
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
) {
    ItemContainer(
        alternative = alternative,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier
    ) {
        Spacer(
            modifier = Modifier
                .background(color = colorPalette().shimmer, shape = thumbnailShape())
                .size(thumbnailSizeDp)
        )

        ItemInfoContainer(
            horizontalAlignment = if (alternative) Alignment.CenterHorizontally else Alignment.Start,
        ) {
            TextPlaceholder()
            TextPlaceholder()
        }
    }
}
