package it.fast4x.rimusic.ui.screens.player

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import coil.compose.AsyncImage
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.appContext
import it.fast4x.rimusic.cleanPrefix
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.context
import it.fast4x.rimusic.enums.BackgroundProgress
import it.fast4x.rimusic.enums.MiniPlayerType
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.PopupType
import it.fast4x.rimusic.service.MyDownloadHelper
import it.fast4x.rimusic.service.modern.PlayerServiceModern
import it.fast4x.rimusic.thumbnailShape
import it.fast4x.rimusic.typography
import it.fast4x.rimusic.ui.components.themed.NowPlayingSongIndicator
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.ui.screens.settings.isYouTubeSyncEnabled
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.collapsedPlayerProgressBar
import it.fast4x.rimusic.ui.styling.favoritesIcon
import it.fast4x.rimusic.ui.styling.favoritesOverlay
import it.fast4x.rimusic.ui.styling.px
import it.fast4x.rimusic.utils.DisposableListener
import it.fast4x.rimusic.utils.addToYtLikedSong
import it.fast4x.rimusic.utils.backgroundProgressKey
import it.fast4x.rimusic.utils.conditional
import it.fast4x.rimusic.utils.disableClosingPlayerSwipingDownKey
import it.fast4x.rimusic.utils.disableScrollingTextKey
import it.fast4x.rimusic.utils.effectRotationKey
import it.fast4x.rimusic.utils.getLikedIcon
import it.fast4x.rimusic.utils.getUnlikedIcon
import it.fast4x.rimusic.utils.intent
import it.fast4x.rimusic.utils.isExplicit
import it.fast4x.rimusic.utils.isNetworkConnected
import it.fast4x.rimusic.utils.mediaItemToggleLike
import it.fast4x.rimusic.utils.miniPlayerTypeKey
import it.fast4x.rimusic.utils.playNext
import it.fast4x.rimusic.utils.playPrevious
import it.fast4x.rimusic.utils.positionAndDurationState
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.shouldBePlaying
import it.fast4x.rimusic.utils.thumbnail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalFoundationApi::class,
    ExperimentalFoundationApi::class, ExperimentalFoundationApi::class,
    ExperimentalFoundationApi::class)
@Composable
fun MiniPlayer(
    showPlayer: () -> Unit,
    hidePlayer: () -> Unit,
    navController: NavController? = null,
) {
    val binder = LocalPlayerServiceBinder.current
    binder?.player ?: return

    val context = LocalContext.current

    var nullableMediaItem by remember {
        mutableStateOf(
            binder.player.currentMediaItem,
            neverEqualPolicy()
        )
    }
    var shouldBePlaying by remember { mutableStateOf(binder.player.shouldBePlaying) }
    val hapticFeedback = LocalHapticFeedback.current

    var playerError by remember {
        mutableStateOf<PlaybackException?>(binder.player.playerError)
    }

    binder.player.DisposableListener {
        object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                nullableMediaItem = mediaItem
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                shouldBePlaying = if (playerError == null) binder.player.shouldBePlaying else false
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                playerError = binder.player.playerError
                shouldBePlaying = if (playerError == null) binder.player.shouldBePlaying else false
            }

            override fun onPlayerError(playbackException: PlaybackException) {
                playerError = playbackException
            }
        }
    }

    val mediaItem = nullableMediaItem ?: return

    playerError?.let { PlayerError(error = it) }

    var likedAt by rememberSaveable {
        mutableStateOf<Long?>(null)
    }
    var miniPlayerType by rememberPreference(
        miniPlayerTypeKey,
        MiniPlayerType.Modern
    )
    LaunchedEffect(mediaItem.mediaId) {
        Database.likedAt(mediaItem.mediaId).distinctUntilChanged().collect { likedAt = it }
    }

    var updateLike by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(updateLike) {
        if (updateLike) {
            if (!isNetworkConnected(appContext()) && isYouTubeSyncEnabled()) {
                SmartMessage(appContext().resources.getString(R.string.no_connection), context = appContext(), type = PopupType.Error)
            } else if (!isYouTubeSyncEnabled()){
                mediaItemToggleLike(mediaItem)
                if (likedAt == null)
                    SmartMessage(context.resources.getString(R.string.added_to_favorites), context = context)
                else
                    SmartMessage(context.resources.getString(R.string.removed_from_favorites), context = context)
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    addToYtLikedSong(mediaItem)
                }
            }
            updateLike = false
        }
    }

    val positionAndDuration by binder.player.positionAndDurationState()


    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd) if (miniPlayerType == MiniPlayerType.Essential) {updateLike = true;hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)} else {binder.player.seekToPrevious();hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)}
            else if (value == SwipeToDismissBoxValue.EndToStart) {binder.player.seekToNext();hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)}
            return@rememberSwipeToDismissBoxState false
        }
    )
    val backgroundProgress by rememberPreference(backgroundProgressKey, BackgroundProgress.MiniPlayer)
    val effectRotationEnabled by rememberPreference(effectRotationKey, true)
    val shouldBePlayingTransition = updateTransition(shouldBePlaying, label = "shouldBePlaying")
    val playPauseRoundness by shouldBePlayingTransition.animateDp(
        transitionSpec = { tween(durationMillis = 100, easing = LinearEasing) },
        label = "playPauseRoundness",
        targetValueByState = { if (it) 24.dp else 12.dp }
    )

    var isRotated by rememberSaveable { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isRotated) 360F else 0f,
        animationSpec = tween(durationMillis = 200), label = ""
    )
    val disableClosingPlayerSwipingDown by rememberPreference(disableClosingPlayerSwipingDownKey, false)

    val disableScrollingText by rememberPreference(disableScrollingTextKey, false)

    SwipeToDismissBox(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp)),
        state = dismissState,
        backgroundContent = {
            /*
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.primaryContainer
                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                },
                label = "background"
            )
             */

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorPalette().background1)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> Arrangement.Start
                    SwipeToDismissBoxValue.EndToStart -> Arrangement.End
                    SwipeToDismissBoxValue.Settled -> Arrangement.Center
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (dismissState.targetValue) {
                        SwipeToDismissBoxValue.StartToEnd -> {
                            if (miniPlayerType == MiniPlayerType.Modern) ImageVector.vectorResource(R.drawable.play_skip_back) else
                             if (likedAt == null)
                             ImageVector.vectorResource(R.drawable.heart_outline)
                             else ImageVector.vectorResource(R.drawable.heart)
                        }
                        SwipeToDismissBoxValue.EndToStart ->  ImageVector.vectorResource(R.drawable.play_skip_forward)
                        SwipeToDismissBoxValue.Settled ->  ImageVector.vectorResource(R.drawable.play)
                    },
                    contentDescription = null,
                    tint = colorPalette().iconButtonPlayer,
                )
            }
        }
    ) {
        val colorPalette = colorPalette()
        /***** */
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .combinedClickable(
                    onLongClick = {
                        navController?.navigate(NavRoutes.queue.name);
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onClick = {
                        //if (showPlayer != null)
                        showPlayer()
                        //else
                        //    navController?.navigate("player")
                    }
                )
                //.clickable(onClick = showPlayer)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, dragAmount ->
                            if (dragAmount < 0) showPlayer()
                            else if (dragAmount > 20) {
                                if (!disableClosingPlayerSwipingDown) {
                                    binder.stopRadio()
                                    binder.player.clearMediaItems()
                                    hidePlayer()
                                    runCatching {
                                        context.stopService(context.intent<PlayerServiceModern>())
                                    }
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                } else
                                    SmartMessage(
                                        context.resources.getString(R.string.player_swiping_down_is_disabled),
                                        context = context
                                    )
                            }
                        }
                    )
                }
                .background(colorPalette().background2)
                .fillMaxWidth()
                .drawBehind {
                    if (backgroundProgress == BackgroundProgress.Both || backgroundProgress == BackgroundProgress.MiniPlayer) {
                        drawRect(
                            color = colorPalette.favoritesOverlay,
                            topLeft = Offset.Zero,
                            size = Size(
                                width = positionAndDuration.first.toFloat() /
                                        positionAndDuration.second.absoluteValue * size.width,
                                height = size.maxDimension
                            )
                        )
                    }
                }
        ) {

            Spacer(
                modifier = Modifier
                    .width(2.dp)
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(Dimensions.miniPlayerHeight)
            ) {
                AsyncImage(
                    model = mediaItem.mediaMetadata.artworkUri.thumbnail(Dimensions.thumbnails.song.px),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clip(thumbnailShape())
                        .size(48.dp)
                )
                NowPlayingSongIndicator(mediaItem.mediaId, binder.player)
            }

            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .height(Dimensions.miniPlayerHeight)
                    .weight(1f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if ( mediaItem.isExplicit )
                        it.fast4x.rimusic.ui.components.themed.IconButton(
                            icon = R.drawable.explicit,
                            color = colorPalette().text,
                            enabled = true,
                            onClick = {},
                            modifier = Modifier
                                .size(14.dp)
                        )
                    BasicText(
                        text = cleanPrefix(mediaItem.mediaMetadata.title?.toString() ?: ""),
                        style = typography().xxs.semiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .conditional(!disableScrollingText) { basicMarquee(iterations = Int.MAX_VALUE) }
                    )
                }

                BasicText(
                    text = mediaItem.mediaMetadata.artist?.toString() ?: "",
                    style = typography().xxs.semiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .conditional(!disableScrollingText) { basicMarquee(iterations = Int.MAX_VALUE) }
                )
            }

            Spacer(
                modifier = Modifier
                    .width(2.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(Dimensions.miniPlayerHeight)
            ) {
               if (miniPlayerType == MiniPlayerType.Essential)
                it.fast4x.rimusic.ui.components.themed.IconButton(
                    icon = R.drawable.play_skip_back,
                    color = colorPalette().iconButtonPlayer,
                    onClick = {
                        binder.player.playPrevious()
                        if (effectRotationEnabled) isRotated = !isRotated
                    },
                    modifier = Modifier
                        .rotate(rotationAngle)
                        .padding(horizontal = 2.dp, vertical = 8.dp)
                        .size(24.dp)
                )

                if (positionAndDuration.second != C.TIME_UNSET) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(playPauseRoundness))
                            .clickable {
                                if (shouldBePlaying) {
                                    binder.callPause({})
                                    //binder.player.pause()
                                } else {
                                    if (binder.player.playbackState == Player.STATE_IDLE) {
                                        binder.player.prepare()
                                    }
                                    binder.player.play()
                                }
                                if (effectRotationEnabled) isRotated = !isRotated
                            }
                            .background(colorPalette().background2)
                            .size(42.dp)
                    ) {
                        Image(
                            painter = painterResource(if (shouldBePlaying) R.drawable.pause else R.drawable.play),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette().iconButtonPlayer),
                            modifier = Modifier
                                .rotate(rotationAngle)
                                .align(Alignment.Center)
                                .size(24.dp)
                        )
                    }
                } else CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = colorPalette().collapsedPlayerProgressBar
                )

               if (miniPlayerType == MiniPlayerType.Essential)
                it.fast4x.rimusic.ui.components.themed.IconButton(
                    icon = R.drawable.play_skip_forward,
                    color = colorPalette().iconButtonPlayer,
                    onClick = {
                        binder.player.playNext()
                        if (effectRotationEnabled) isRotated = !isRotated
                    },
                    modifier = Modifier
                        .rotate(rotationAngle)
                        .padding(horizontal = 2.dp, vertical = 8.dp)
                        .size(24.dp)
                )
                if (miniPlayerType == MiniPlayerType.Modern)
                 it.fast4x.rimusic.ui.components.themed.IconButton(
                     icon = if (likedAt == null) getUnlikedIcon() else getLikedIcon(),
                     color = colorPalette().favoritesIcon,
                     onClick = {
                         updateLike = true
                     },
                     modifier = Modifier
                         .rotate(rotationAngle)
                         .padding(horizontal = 2.dp, vertical = 8.dp)
                         .size(24.dp)
                 )

            }

            Spacer(
                modifier = Modifier
                    .width(2.dp)
            )
        }
        /*****  */

    }
}