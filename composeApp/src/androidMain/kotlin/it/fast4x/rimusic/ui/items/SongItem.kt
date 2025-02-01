package it.fast4x.rimusic.ui.items

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadService
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import it.fast4x.innertube.Innertube
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.cleanPrefix
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.enums.ColorPaletteName
import it.fast4x.rimusic.enums.DownloadedStateMedia
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.service.MyDownloadService
import it.fast4x.rimusic.service.isLocal
import it.fast4x.rimusic.thumbnailShape
import it.fast4x.rimusic.typography
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.themed.AddToPlaylistPlayerMenu
import it.fast4x.rimusic.ui.components.themed.HeaderIconButton
import it.fast4x.rimusic.ui.components.themed.IconButton
import it.fast4x.rimusic.ui.components.themed.NowPlayingSongIndicator
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.ui.components.themed.TextPlaceholder
import it.fast4x.rimusic.ui.styling.LocalAppearance
import it.fast4x.rimusic.ui.styling.favoritesIcon
import it.fast4x.rimusic.ui.styling.favoritesOverlay
import it.fast4x.rimusic.ui.styling.shimmer
import it.fast4x.rimusic.utils.asMediaItem
import it.fast4x.rimusic.utils.asSong
import it.fast4x.rimusic.utils.colorPaletteNameKey
import it.fast4x.rimusic.utils.conditional
import it.fast4x.rimusic.utils.downloadedStateMedia
import it.fast4x.rimusic.utils.getLikeState
import it.fast4x.rimusic.utils.isExplicit
import it.fast4x.rimusic.utils.medium
import it.fast4x.rimusic.utils.playlistindicatorKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.secondary
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.shimmerEffect
import it.fast4x.rimusic.utils.thumbnail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@UnstableApi
@Composable
fun SongItem(
    song: Innertube.SongItem,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    onDownloadClick: () -> Unit,
    downloadState: Int,
    thumbnailContent: (@Composable BoxScope.() -> Unit)? = null,
    disableScrollingText: Boolean,
    isNowPlaying: Boolean = false,
    forceRecompose: Boolean = false
) {
    SongItem(
        thumbnailUrl = song.thumbnail?.size(thumbnailSizePx),
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier,
        onDownloadClick = {
            CoroutineScope(Dispatchers.IO).launch {
                Database.upsert(song.asSong)
            }
            onDownloadClick()
        },
        downloadState = downloadState,
        mediaItem = song.asMediaItem,
        onThumbnailContent = thumbnailContent,
        disableScrollingText = disableScrollingText,
        isNowPlaying = isNowPlaying,
        forceRecompose = forceRecompose
    )
}

@UnstableApi
@Composable
fun SongItem(
    song: MediaItem,
    thumbnailSizeDp: Dp,
    thumbnailSizePx: Int,
    modifier: Modifier = Modifier,
    onThumbnailContent: (@Composable BoxScope.() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onDownloadClick: () -> Unit,
    downloadState: Int,
    isRecommended: Boolean = false,
    disableScrollingText: Boolean,
    isNowPlaying: Boolean = false,
    forceRecompose: Boolean = false
) {
    SongItem(
        thumbnailUrl = song.mediaMetadata.artworkUri.thumbnail(thumbnailSizePx)?.toString(),
        thumbnailSizeDp = thumbnailSizeDp,
        onThumbnailContent = onThumbnailContent,
        trailingContent = trailingContent,
        modifier = modifier,
        onDownloadClick = {
            CoroutineScope(Dispatchers.IO).launch {
                Database.upsert(song.asSong)
            }
            onDownloadClick()
        },
        downloadState = downloadState,
        isRecommended = isRecommended,
        mediaItem = song,
        disableScrollingText = disableScrollingText,
        isNowPlaying = isNowPlaying,
        forceRecompose = forceRecompose
    )
}

@UnstableApi
@Composable
fun SongItem(
    song: Song,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    onThumbnailContent: (@Composable BoxScope.() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onDownloadClick: () -> Unit,
    downloadState: Int,
    disableScrollingText: Boolean,
    isNowPlaying: Boolean = false,
    forceRecompose: Boolean = false
) {
    SongItem(
        thumbnailUrl = song.thumbnailUrl?.thumbnail(thumbnailSizePx),
        thumbnailSizeDp = thumbnailSizeDp,
        onThumbnailContent = onThumbnailContent,
        trailingContent = trailingContent,
        modifier = modifier,
        onDownloadClick = {
            CoroutineScope(Dispatchers.IO).launch {
                Database.upsert(song)
            }
            onDownloadClick()
        },
        downloadState = downloadState,
        mediaItem = song.asMediaItem,
        disableScrollingText = disableScrollingText,
        isNowPlaying = isNowPlaying,
        forceRecompose = forceRecompose
    )
}

@UnstableApi
@Composable
fun SongItem(
    thumbnailUrl: String?,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    onThumbnailContent: (@Composable BoxScope.() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onDownloadClick: () -> Unit,
    downloadState: Int,
    isRecommended: Boolean = false,
    mediaItem: MediaItem,
    disableScrollingText: Boolean,
    isNowPlaying: Boolean = false,
    forceRecompose: Boolean = false
) {
    val binder = LocalPlayerServiceBinder.current

    SongItem(
        thumbnailSizeDp = thumbnailSizeDp,
        thumbnailContent = {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(thumbnailShape())
                    .fillMaxSize()
            )

            onThumbnailContent?.invoke(this)

            NowPlayingSongIndicator(
                mediaId = mediaItem.mediaId,
                player = binder?.player
            )
        },
        modifier = modifier,
        trailingContent = trailingContent,
        onDownloadClick = onDownloadClick,
        downloadState = downloadState,
        isRecommended = isRecommended,
        mediaItem = mediaItem,
        disableScrollingText = disableScrollingText,
        isNowPlaying = isNowPlaying,
        forceRecompose = forceRecompose
    )
}

/*
@Composable
fun SongItem(
    thumbnailContent: @Composable BoxScope.() -> Unit,
    title: String?,
    authors: String?,
    duration: String?,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null,
    isDownloaded: Boolean,
    onDownloadClick: () -> Unit,
    disableScrollingText: Boolean
) {
    ItemContainer(
        alternative = false,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(thumbnailSizeDp)
        ) {
            thumbnailContent()
        }

        ItemInfoContainer {
            trailingContent?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BasicText(
                        text = title ?: "",
                        style = typography().xs.semiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .conditional(!disableScrollingText) { basicMarquee(iterations = Int.MAX_VALUE) }
                    )

                    it()
                }
            } ?: BasicText(
                text = title ?: "",
                style = typography().xs.semiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .conditional(!disableScrollingText) { basicMarquee(iterations = Int.MAX_VALUE) }
            )


            Row(verticalAlignment = Alignment.CenterVertically) {

                IconButton(
                    onClick = onDownloadClick,
                    icon = if (isDownloaded) R.drawable.downloaded else R.drawable.download,
                    color = if (isDownloaded) colorPalette().text else colorPalette().textDisabled,
                    modifier = Modifier
                        .size(16.dp)
                )

                Spacer(modifier = Modifier.padding(horizontal = 2.dp))

                BasicText(
                    text = authors ?: "",
                    style = typography().xs.semiBold.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier
                        .weight(1f)
                        .conditional(!disableScrollingText) { basicMarquee(iterations = Int.MAX_VALUE) }
                )

                duration?.let {
                    BasicText(
                        text = duration,
                        style = typography().xxs.secondary.medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
*/

@OptIn(ExperimentalFoundationApi::class, ExperimentalTextApi::class,
    ExperimentalAnimationApi::class
)
@UnstableApi
@Composable
fun SongItem(
    thumbnailContent: @Composable BoxScope.() -> Unit,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null,
    onDownloadClick: () -> Unit,
    downloadState: Int,
    isRecommended: Boolean = false,
    mediaItem: MediaItem,
    disableScrollingText: Boolean,
    isNowPlaying: Boolean = false,
    forceRecompose: Boolean = false
) {

    var downloadedStateMedia by remember { mutableStateOf(DownloadedStateMedia.NOT_CACHED_OR_DOWNLOADED) }
    downloadedStateMedia = if (!mediaItem.isLocal) downloadedStateMedia(mediaItem.mediaId)
    else DownloadedStateMedia.DOWNLOADED

    val title = mediaItem.mediaMetadata.title.toString()
    val authors = mediaItem.mediaMetadata.artist.toString()
    val duration = mediaItem.mediaMetadata.extras?.getString("durationText")

    val playlistindicator by rememberPreference(playlistindicatorKey,false)
    var songPlaylist by remember {
        mutableIntStateOf(0)
    }
    val colorPaletteName by rememberPreference(colorPaletteNameKey, ColorPaletteName.Dynamic)

    // TODO improve playlist indicator without recompose
    // There's no need, turning this into Flow is much more efficient
    if (playlistindicator)
        LaunchedEffect(Unit, forceRecompose) {
            withContext(Dispatchers.IO) {
                songPlaylist = Database.songUsedInPlaylists(mediaItem.mediaId)
            }
        }


    val context = LocalContext.current
    val colorPalette = LocalAppearance.current.colorPalette

    ItemContainer(
        alternative = false,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier
            .padding(end = 8.dp)
            .clip(RoundedCornerShape(10.dp))
            .conditional(isNowPlaying){
                background(colorPalette.favoritesOverlay)
            }

    ) {
        Box(
            modifier = Modifier
                .size(thumbnailSizeDp)
        ) {
            thumbnailContent()


            var likedAt by remember {
                mutableStateOf<Long?>(null)
            }
            LaunchedEffect(Unit, mediaItem.mediaId) {
                Database.likedAt(mediaItem.mediaId).collect { likedAt = it }
            }
            if (likedAt != null)
                HeaderIconButton(
                    onClick = {},
                    icon = getLikeState(mediaItem.mediaId),
                    color = colorPalette().favoritesIcon,
                    iconSize = 12.dp,
                    modifier = Modifier
                        //.padding(start = 4.dp)
                        .align(Alignment.BottomStart)
                        .absoluteOffset(-8.dp, 0.dp)

                )
            /*
            if (totalPlayTimeMs != null) {
                if (totalPlayTimeMs <= 0 ) {
                    HeaderIconButton(
                        onClick = {},
                        icon = R.drawable.noteslashed,
                        color = colorPalette().favoritesIcon,
                        iconSize = 12.dp,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .align(Alignment.BottomStart)
                    )
                }
            }
             */

            /*
            BasicText(
                text = totalPlayTimeMs.toString() ?: "",
                style = typography().xs.semiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(all = 16.dp)
            )
             */
        }

        ItemInfoContainer {
            trailingContent?.let {
                val menuState = LocalMenuState.current
                val navController = rememberNavController()
                val binder = LocalPlayerServiceBinder.current
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isRecommended)
                        IconButton(
                            icon = R.drawable.smart_shuffle,
                            color = colorPalette().accent,
                            enabled = true,
                            onClick = {},
                            modifier = Modifier
                                .size(18.dp)
                        )

                    if (playlistindicator && (songPlaylist > 0)) {
                        IconButton(
                            icon = R.drawable.add_in_playlist,
                            color = if (colorPaletteName == ColorPaletteName.PureBlack) Color.Black else colorPalette().text,
                            enabled = true,
                            onClick = {},
                            modifier = Modifier
                                .size(14.dp)
                                .background(colorPalette().accent, CircleShape)
                                .padding(all = 3.dp)
                                .combinedClickable(onClick = {
                                    menuState.display {
                                        if (binder != null) {
                                            AddToPlaylistPlayerMenu(
                                                navController = navController,
                                                onDismiss = {
                                                    menuState.hide()
                                                    Database.asyncTransaction {
                                                        songPlaylist = songUsedInPlaylists(mediaItem.mediaId)
                                                    }
                                                },
                                                mediaItem = mediaItem,
                                                binder = binder,
                                                onClosePlayer = {},
                                            )
                                        }
                                    }
                                }, onLongClick = {
                                    SmartMessage(
                                        context.resources.getString(R.string.playlistindicatorinfo2),
                                        context = context
                                    )
                                })
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 3.dp))
                    }

                    if ( mediaItem.isExplicit )
                        IconButton(
                            icon = R.drawable.explicit,
                            color = colorPalette().text,
                            enabled = true,
                            onClick = {},
                            modifier = Modifier
                                .size(18.dp)
                        )

                    BasicText(
                        text = cleanPrefix(title),
                        style = typography().xs.semiBold,
                        /*
                        style = TextStyle(
                            color = if (isRecommended) colorPalette().accent else colorPalette().text,
                            fontStyle = typography().xs.semiBold.fontStyle,
                            fontSize = typography().xs.semiBold.fontSize
                        ),
                         */
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .conditional(!disableScrollingText) { basicMarquee(iterations = Int.MAX_VALUE) }
                    )

                    /*
                    if (playlistindicator && (songPlaylist > 0)) {
                        IconButton(
                            icon = R.drawable.add_in_playlist,
                            color = colorPalette().text,
                            enabled = true,
                            onClick = {},
                            modifier = Modifier
                                .size(18.dp)
                                .background(colorPalette().accent, CircleShape)
                                .padding(all = 3.dp)
                                .combinedClickable(onClick = {}, onLongClick = {
                                    SmartMessage(context.resources.getString(R.string.playlistindicatorinfo2), context = context)
                                })
                        )
                    }
                     */

                    it()
                }
            } ?: Row(verticalAlignment = Alignment.CenterVertically) {
                val menuState = LocalMenuState.current
                val navController = rememberNavController()
                val binder = LocalPlayerServiceBinder.current
                    if (isRecommended)
                        IconButton(
                            icon = R.drawable.smart_shuffle,
                            color = colorPalette().accent,
                            enabled = true,
                            onClick = {},
                            modifier = Modifier
                                .size(18.dp)
                        )

                    if ( mediaItem.isExplicit )
                        IconButton(
                            icon = R.drawable.explicit,
                            color = colorPalette().text,
                            enabled = true,
                            onClick = {},
                            modifier = Modifier
                                .size(18.dp)
                        )
                    BasicText(
                        text = cleanPrefix(title),
                        style = typography().xs.semiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .conditional(!disableScrollingText) { basicMarquee(iterations = Int.MAX_VALUE) }
                            .weight(1f)
                    )
                if (playlistindicator && (songPlaylist > 0)) {
                    IconButton(
                        icon = R.drawable.add_in_playlist,
                        color = if (colorPaletteName == ColorPaletteName.PureBlack) Color.Black else colorPalette().text,
                        enabled = true,
                        onClick = {},
                        modifier = Modifier
                            .size(18.dp)
                            .background(colorPalette().accent, CircleShape)
                            .padding(all = 3.dp)
                            .combinedClickable(onClick = {
                                menuState.display {
                                    if (binder != null) {
                                        AddToPlaylistPlayerMenu(
                                            navController = navController,
                                            onDismiss = {
                                                menuState.hide()
                                                Database.asyncTransaction {
                                                    songPlaylist = songUsedInPlaylists(mediaItem.mediaId)
                                                }
                                            },
                                            mediaItem = mediaItem,
                                            binder = binder,
                                            onClosePlayer = {},
                                        )
                                    }
                                }
                            }, onLongClick = {
                                SmartMessage(
                                    context.resources.getString(R.string.playlistindicatorinfo2),
                                    context = context
                                )
                            })
                    )
                }
            }


            Row(verticalAlignment = Alignment.CenterVertically) {

                //Log.d("downloadState",downloadState.toString())

                /*
                if ((downloadState == Download.STATE_DOWNLOADING
                            || downloadState == Download.STATE_QUEUED
                            || downloadState == Download.STATE_RESTARTING
                        )
                    && !isDownloaded) {
                    val context = LocalContext.current
                    IconButton(
                        onClick = {
                            DownloadService.sendRemoveDownload(
                                context,
                                MyDownloadService::class.java,
                                mediaId,
                                false
                            )
                        },
                        icon = R.drawable.download_progress,
                        color = colorPalette().text,
                        modifier = Modifier
                            .size(16.dp)
                    )
                    /*
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = colorPalette().text,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable {
                                DownloadService.sendRemoveDownload(
                                        context,
                                        MyDownloadService::class.java,
                                        mediaId,
                                        false
                                    )
                            }
                    )
                     */
                } else {
                   IconButton(
                        onClick = onDownloadClick,
                        icon = if (isDownloaded) R.drawable.downloaded else R.drawable.download,
                        color = if (isDownloaded) colorPalette().text else colorPalette().textDisabled,
                        modifier = Modifier
                            .size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                */

                BasicText(
                    text = authors,
                    style = typography().xs.semiBold.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier
                        .weight(1f)
                        .conditional(!disableScrollingText) { basicMarquee(iterations = Int.MAX_VALUE) }
                )

                duration?.let {
                    BasicText(
                        text = duration,
                        style = typography().xxs.secondary.medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.padding(horizontal = 4.dp))

                //println("downloadutil $mediaId $downloadState: $downloadState")

                if ((downloadState == Download.STATE_DOWNLOADING
                            || downloadState == Download.STATE_QUEUED
                            || downloadState == Download.STATE_RESTARTING
                            )
                    && downloadedStateMedia == DownloadedStateMedia.NOT_CACHED_OR_DOWNLOADED) {
                    //val context = LocalContext.current
                    IconButton(
                        onClick = {
                            DownloadService.sendRemoveDownload(
                                context,
                                MyDownloadService::class.java,
                                mediaItem.mediaId,
                                false
                            )
                        },
                        icon = R.drawable.download_progress,
                        color = colorPalette().text,
                        modifier = Modifier
                            .size(20.dp)
                    )
                } else {
                    IconButton(
                        onClick = onDownloadClick,
                        icon = downloadedStateMedia.icon,
                        color = when(downloadedStateMedia) {
                            DownloadedStateMedia.NOT_CACHED_OR_DOWNLOADED -> colorPalette().textDisabled
                            else -> colorPalette().text
                        },
                        modifier = Modifier
                            .size(20.dp)
                    )
                }

            }
        }
    }
}


@Composable
fun SongItemPlaceholder(
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier
) {
    ItemContainer(
        alternative = false,
        thumbnailSizeDp =thumbnailSizeDp,
        modifier = modifier
    ) {
        Spacer(
            modifier = Modifier
                .background(color = colorPalette().shimmer, shape = thumbnailShape())
                .size(thumbnailSizeDp)
        )

        ItemInfoContainer {
            TextPlaceholder()
            TextPlaceholder()
        }
    }
}

/**
 * New component is more resemble to the final
 * SongItem that's currently being used.
 */
@Composable
fun SongItemPlaceholder( thumbnailSizeDp: Dp ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy( 12.dp ),
        modifier = Modifier.fillMaxWidth()
                           .padding(
                               vertical = 8.dp,
                               horizontal = 16.dp
                           )
    ) {
        Box(
            Modifier.size( thumbnailSizeDp )
                    .clip( RoundedCornerShape(12.dp) )
                    .shimmerEffect()
        )

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth( .7f )
            ) {
                BasicText(
                    text = "",
                    style = typography().xs.semiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight( 1f ).shimmerEffect()
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box( Modifier.weight( 1f ).fillMaxWidth() ) {
                    BasicText(
                        text = "",
                        style = typography().xs.semiBold.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier.fillMaxWidth( .3f ).shimmerEffect()
                    )
                }

                BasicText(
                    text = "0:00",
                    style = typography().xxs.secondary.medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding( top = 4.dp )
                )

                Spacer(modifier = Modifier.padding( horizontal = 4.dp ))

                IconButton(
                    onClick = {},
                    icon = DownloadedStateMedia.NOT_CACHED_OR_DOWNLOADED.icon,
                    color = colorPalette().textDisabled,
                    modifier = Modifier.size( 20.dp ),
                    enabled = false
                )
            }
        }
    }
}