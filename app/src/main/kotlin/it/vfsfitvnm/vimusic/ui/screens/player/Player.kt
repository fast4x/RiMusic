package it.vfsfitvnm.vimusic.ui.screens.player

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import coil.compose.AsyncImage
import it.vfsfitvnm.compose.routing.OnGlobalRoute
import it.vfsfitvnm.innertube.models.NavigationEndpoint
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.BackgroundProgress
import it.vfsfitvnm.vimusic.enums.ColorPaletteName
import it.vfsfitvnm.vimusic.enums.PlayerThumbnailSize
import it.vfsfitvnm.vimusic.enums.PlayerVisualizerType
import it.vfsfitvnm.vimusic.enums.PlaylistSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import it.vfsfitvnm.vimusic.enums.UiType
import it.vfsfitvnm.vimusic.models.Info
import it.vfsfitvnm.vimusic.models.Song
import it.vfsfitvnm.vimusic.models.SongPlaylistMap
import it.vfsfitvnm.vimusic.models.ui.toUiMedia
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.service.PlayerService
import it.vfsfitvnm.vimusic.transaction
import it.vfsfitvnm.vimusic.ui.components.BottomSheet
import it.vfsfitvnm.vimusic.ui.components.BottomSheetState
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.rememberBottomSheetState
import it.vfsfitvnm.vimusic.ui.components.themed.BaseMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.CircularSlider
import it.vfsfitvnm.vimusic.ui.components.themed.ConfirmationDialog
import it.vfsfitvnm.vimusic.ui.components.themed.DefaultDialog
import it.vfsfitvnm.vimusic.ui.components.themed.DownloadStateIconButton
import it.vfsfitvnm.vimusic.ui.components.themed.IconButton
import it.vfsfitvnm.vimusic.ui.components.themed.PlayerMenu
import it.vfsfitvnm.vimusic.ui.components.themed.PlaylistsItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.SecondaryTextButton
import it.vfsfitvnm.vimusic.ui.components.themed.SelectorDialog
import it.vfsfitvnm.vimusic.ui.screens.homeRoute
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.collapsedPlayerProgressBar
import it.vfsfitvnm.vimusic.ui.styling.favoritesIcon
import it.vfsfitvnm.vimusic.ui.styling.favoritesOverlay
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.DisposableListener
import it.vfsfitvnm.vimusic.utils.UiTypeKey
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.colorPaletteNameKey
import it.vfsfitvnm.vimusic.utils.disableClosingPlayerSwipingDownKey
import it.vfsfitvnm.vimusic.utils.disablePlayerHorizontalSwipeKey
import it.vfsfitvnm.vimusic.utils.downloadedStateMedia
import it.vfsfitvnm.vimusic.utils.durationTextToMillis
import it.vfsfitvnm.vimusic.utils.effectRotationKey
import it.vfsfitvnm.vimusic.utils.forceSeekToNext
import it.vfsfitvnm.vimusic.utils.forceSeekToPrevious
import it.vfsfitvnm.vimusic.utils.formatAsDuration
import it.vfsfitvnm.vimusic.utils.formatAsTime
import it.vfsfitvnm.vimusic.utils.getDownloadState
import it.vfsfitvnm.vimusic.utils.isGradientBackgroundEnabledKey
import it.vfsfitvnm.vimusic.utils.isLandscape
import it.vfsfitvnm.vimusic.utils.manageDownload
import it.vfsfitvnm.vimusic.utils.playerThumbnailSizeKey
import it.vfsfitvnm.vimusic.utils.playerVisualizerTypeKey
import it.vfsfitvnm.vimusic.utils.positionAndDurationState
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.vimusic.utils.shouldBePlaying
import it.vfsfitvnm.vimusic.utils.showButtonPlayerAddToPlaylistKey
import it.vfsfitvnm.vimusic.utils.showButtonPlayerArrowKey
import it.vfsfitvnm.vimusic.utils.showButtonPlayerDownloadKey
import it.vfsfitvnm.vimusic.utils.showButtonPlayerLoopKey
import it.vfsfitvnm.vimusic.utils.showButtonPlayerLyricsKey
import it.vfsfitvnm.vimusic.utils.showButtonPlayerMenuKey
import it.vfsfitvnm.vimusic.utils.showButtonPlayerShuffleKey
import it.vfsfitvnm.vimusic.utils.showButtonPlayerSleepTimerKey
import it.vfsfitvnm.vimusic.utils.backgroundProgressKey
import it.vfsfitvnm.vimusic.utils.showTotalTimeQueueKey
import it.vfsfitvnm.vimusic.utils.shuffleQueue
import it.vfsfitvnm.vimusic.utils.thumbnail
import it.vfsfitvnm.vimusic.utils.thumbnailTapEnabledKey
import it.vfsfitvnm.vimusic.utils.toast
import it.vfsfitvnm.vimusic.utils.trackLoopEnabledKey
import it.vfsfitvnm.vimusic.utils.windows
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.absoluteValue



@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation", "RememberReturnType")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@UnstableApi
@Composable
fun Player(
    layoutState: BottomSheetState,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(
        topStart = 12.dp,
        topEnd = 12.dp
    )
) {
    val menuState = LocalMenuState.current

    val uiType by rememberPreference(UiTypeKey, UiType.RiMusic)

    val effectRotationEnabled by rememberPreference(effectRotationKey, true)

    val playerThumbnailSize by rememberPreference(
        playerThumbnailSizeKey,
        PlayerThumbnailSize.Medium
    )

    var disablePlayerHorizontalSwipe by rememberPreference(disablePlayerHorizontalSwipeKey, false)

    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
    //val colorPaletteName by rememberPreference(colorPaletteNameKey, ColorPaletteName.ModernBlack)
    val binder = LocalPlayerServiceBinder.current

    binder?.player ?: return

    var nullableMediaItem by remember {
        mutableStateOf(binder.player.currentMediaItem, neverEqualPolicy())
    }

    var shouldBePlaying by remember {
        mutableStateOf(binder.player.shouldBePlaying)
    }

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

    var playerVisualizerType by rememberPreference(
        playerVisualizerTypeKey,
        PlayerVisualizerType.Disabled
    )

    binder.player.DisposableListener {
        object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                nullableMediaItem = mediaItem
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                shouldBePlaying = binder.player.shouldBePlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                shouldBePlaying = binder.player.shouldBePlaying
            }
        }
    }

    val mediaItem = nullableMediaItem ?: return

    val positionAndDuration by binder.player.positionAndDurationState()

    val windowInsets = WindowInsets.systemBars

    val horizontalBottomPaddingValues = windowInsets
        .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom).asPaddingValues()

    var albumInfo by remember {
        mutableStateOf(mediaItem.mediaMetadata.extras?.getString("albumId")?.let { albumId ->
            Info(albumId, null)
        })
    }

    var artistsInfo by remember {
        mutableStateOf(
            mediaItem.mediaMetadata.extras?.getStringArrayList("artistNames")?.let { artistNames ->
                mediaItem.mediaMetadata.extras?.getStringArrayList("artistIds")?.let { artistIds ->
                    artistNames.zip(artistIds).map { (authorName, authorId) ->
                        Info(authorId, authorName)
                    }
                }
            }
        )
    }

    LaunchedEffect(mediaItem.mediaId) {
        withContext(Dispatchers.IO) {
            //if (albumInfo == null)
            albumInfo = Database.songAlbumInfo(mediaItem.mediaId)
            //if (artistsInfo == null)
            artistsInfo = Database.songArtistInfo(mediaItem.mediaId)
        }
    }


    val ExistIdsExtras =
        mediaItem.mediaMetadata.extras?.getStringArrayList("artistIds")?.size.toString()
    val ExistAlbumIdExtras = mediaItem.mediaMetadata.extras?.getString("albumId")

    var albumId = albumInfo?.id
    if (albumId == null) albumId = ExistAlbumIdExtras
    //var albumTitle = albumInfo?.name

    var artistIds = arrayListOf<String>()
    var artistNames = arrayListOf<String>()


    artistsInfo?.forEach { (id) -> artistIds = arrayListOf(id) }
    if (ExistIdsExtras.equals(0)
            .not()
    ) mediaItem.mediaMetadata.extras?.getStringArrayList("artistIds")?.toCollection(artistIds)

    artistsInfo?.forEach { (name) -> artistNames = arrayListOf(name) }
    if (ExistIdsExtras.equals(0)
            .not()
    ) mediaItem.mediaMetadata.extras?.getStringArrayList("artistNames")?.toCollection(artistNames)



    if (artistsInfo?.isEmpty() == true && ExistIdsExtras.equals(0).not()) {
        artistsInfo = artistNames.let { artistNames ->
            artistIds.let { artistIds ->
                artistNames.zip(artistIds).map {
                    Info(it.second, it.first)
                }
            }
        }
    }


    /*
    //Log.d("mediaItem_pl_mediaId",mediaItem.mediaId)
    Log.d("mediaItem_player","--- START LOG ARTIST ---")
    Log.d("mediaItem_player","ExistIdsExtras: $ExistIdsExtras")
    Log.d("mediaItem_player","metadata artisIds "+mediaItem.mediaMetadata.extras?.getStringArrayList("artistIds").toString())
    Log.d("mediaItem_player","variable artisIds "+artistIds.toString())
    Log.d("mediaItem_player","variable artisNames pre"+artistNames.toString())
    Log.d("mediaItem_player","variable artistsInfo pre "+artistsInfo.toString())

    //Log.d("mediaItem_pl_artinfo",artistsInfo.toString())
    //Log.d("mediaItem_pl_artId",artistIds.toString())
    Log.d("mediaItem_player","--- START LOG ALBUM ---")
    Log.d("mediaItem_player",ExistAlbumIdExtras.toString())
    Log.d("mediaItem_player","metadata albumId "+mediaItem.mediaMetadata.extras?.getString("albumId").toString())
    Log.d("mediaItem_player","metadata extra "+mediaItem.mediaMetadata.extras?.toString())
    Log.d("mediaItem_player","metadata full "+mediaItem.mediaMetadata.toString())
    //Log.d("mediaItem_pl_extrasArt",mediaItem.mediaMetadata.extras?.getStringArrayList("artistNames").toString())
    //Log.d("mediaItem_pl_extras",mediaItem.mediaMetadata.extras.toString())
    Log.d("mediaItem_player","albumInfo "+albumInfo.toString())
    Log.d("mediaItem_player","albumId "+albumId.toString())

    Log.d("mediaItem_pl","--- END LOG ---")

    */


    /*

    var nextmediaItemIndex = binder.player.nextMediaItemIndex ?: -1
    var nextmediaItemtitle = ""


    if (nextmediaItemIndex.toShort() > -1)
        nextmediaItemtitle = binder.player.getMediaItemAt(nextmediaItemIndex).mediaMetadata.title.toString()
    */

    var trackLoopEnabled by rememberPreference(trackLoopEnabledKey, defaultValue = false)


    var likedAt by rememberSaveable {
        mutableStateOf<Long?>(null)
    }
    LaunchedEffect(mediaItem.mediaId) {
        Database.likedAt(mediaItem.mediaId).distinctUntilChanged().collect { likedAt = it }
    }


    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }
    downloadState = getDownloadState(mediaItem.mediaId)

//    val isLocal by remember { derivedStateOf { mediaItem.isLocal } }

    var isDownloaded by rememberSaveable { mutableStateOf(false) }
    isDownloaded = downloadedStateMedia(mediaItem.mediaId)

    val showButtonPlayerAddToPlaylist by rememberPreference(showButtonPlayerAddToPlaylistKey, true)
    val showButtonPlayerArrow by rememberPreference(showButtonPlayerArrowKey, false)
    val showButtonPlayerDownload by rememberPreference(showButtonPlayerDownloadKey, true)
    val showButtonPlayerLoop by rememberPreference(showButtonPlayerLoopKey, true)
    val showButtonPlayerLyrics by rememberPreference(showButtonPlayerLyricsKey, true)
    val showButtonPlayerShuffle by rememberPreference(showButtonPlayerShuffleKey, true)
    val showButtonPlayerSleepTimer by rememberPreference(showButtonPlayerSleepTimerKey, false)
    val showButtonPlayerMenu by rememberPreference(showButtonPlayerMenuKey, false)
    val disableClosingPlayerSwipingDown by rememberPreference(disableClosingPlayerSwipingDownKey, true)
    val showTotalTimeQueue by rememberPreference(showTotalTimeQueueKey, true)
    val backgroundProgress by rememberPreference(backgroundProgressKey, BackgroundProgress.MiniPlayer)
    /*
    val playlistPreviews by remember {
        Database.playlistPreviews(PlaylistSortBy.Name, SortOrder.Ascending)
    }.collectAsState(initial = emptyList(), context = Dispatchers.IO)


    var showPlaylistSelectDialog by remember {
        mutableStateOf(false)
    }
     */

    var isShowingSleepTimerDialog by remember {
        mutableStateOf(false)
    }

    val sleepTimerMillisLeft by (binder?.sleepTimerMillisLeft
        ?: flowOf(null))
        .collectAsState(initial = null)

    var timeRemaining by remember { mutableIntStateOf(0) }

    if (positionAndDuration != null) {
        timeRemaining = positionAndDuration.second.toInt() - positionAndDuration.first.toInt()
    }

    var showCircularSlider by remember {
        mutableStateOf(false)
    }

    if (isShowingSleepTimerDialog) {
        if (sleepTimerMillisLeft != null) {
            ConfirmationDialog(
                text = stringResource(R.string.stop_sleep_timer),
                cancelText = stringResource(R.string.no),
                confirmText = stringResource(R.string.stop),
                onDismiss = { isShowingSleepTimerDialog = false },
                onConfirm = {
                    binder.cancelSleepTimer()
                    //onDismiss()
                }
            )
        } else {
            DefaultDialog(
                onDismiss = { isShowingSleepTimerDialog = false }
            ) {
                var amount by remember {
                    mutableStateOf(1)
                }

                BasicText(
                    text = stringResource(R.string.set_sleep_timer),
                    style = typography.s.semiBold,
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 24.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 16.dp,
                        alignment = Alignment.CenterHorizontally
                    ),
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                ) {
                    if (!showCircularSlider) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .alpha(if (amount <= 1) 0.5f else 1f)
                                .clip(CircleShape)
                                .clickable(enabled = amount > 1) { amount-- }
                                .size(48.dp)
                                .background(colorPalette.background0)
                        ) {
                            BasicText(
                                text = "-",
                                style = typography.xs.semiBold
                            )
                        }

                        Box(contentAlignment = Alignment.Center) {
                            BasicText(
                                text = stringResource(
                                    R.string.left,
                                    formatAsDuration(amount * 5 * 60 * 1000L)
                                ),
                                style = typography.s.semiBold,
                                modifier = Modifier
                                    .clickable {
                                        showCircularSlider = !showCircularSlider
                                    }
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .alpha(if (amount >= 60) 0.5f else 1f)
                                .clip(CircleShape)
                                .clickable(enabled = amount < 60) { amount++ }
                                .size(48.dp)
                                .background(colorPalette.background0)
                        ) {
                            BasicText(
                                text = "+",
                                style = typography.xs.semiBold
                            )
                        }

                    } else {
                        CircularSlider(
                            stroke = 40f,
                            thumbColor = colorPalette.accent,
                            text = formatAsDuration(amount * 5 * 60 * 1000L),
                            modifier = Modifier
                                .size(300.dp),
                            onChange = {
                                amount = (it * 120).toInt()
                            }
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .fillMaxWidth()
                ) {
                    SecondaryTextButton(
                        text = stringResource(R.string.set_to) + " "
                                + formatAsDuration(timeRemaining.toLong())
                                + " " + stringResource(R.string.end_of_song),
                        onClick = {
                            binder?.startSleepTimer(timeRemaining.toLong())
                            isShowingSleepTimerDialog = false
                        }
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {

                    IconButton(
                        onClick = { showCircularSlider = !showCircularSlider },
                        icon = R.drawable.time,
                        color = colorPalette.text
                    )
                    IconButton(
                        onClick = { isShowingSleepTimerDialog = false },
                        icon = R.drawable.close,
                        color = colorPalette.text
                    )
                    IconButton(
                        enabled = amount > 0,
                        onClick = {
                            binder?.startSleepTimer(amount * 5 * 60 * 1000L)
                            isShowingSleepTimerDialog = false
                        },
                        icon = R.drawable.checkmark,
                        color = colorPalette.accent
                    )
                }
            }
        }
    }

    var position by remember {
        mutableIntStateOf(0)
    }

    OnGlobalRoute {
        layoutState.collapseSoft()
    }

    val onGoToHome = homeRoute::global

    BottomSheet(
        state = layoutState,
        modifier = modifier,
        disableDismiss = disableClosingPlayerSwipingDown,
        onDismiss = {
            //if (disableClosingPlayerSwipingDown) {
                //val playerBottomSheetMinHeight = 70.dp
                //layoutState.snapTo(playerBottomSheetMinHeight)
            //} else {
                binder.stopRadio()
                binder.player.clearMediaItems()
            //}
        },
        collapsedContent = {
            var deltaX by remember { mutableStateOf(0f) }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .background(colorPalette.background1)
                    .clip(shape)
                    .fillMaxSize()
                    .padding(horizontalBottomPaddingValues)
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
                        /*
                        drawLine(
                            color = colorPalette.textDisabled,
                            start = Offset(x = 0f, y = 1.dp.toPx()),
                            end = Offset(x = size.maxDimension, y = 1.dp.toPx()),
                            strokeWidth = 2.dp.toPx()
                        )

                        val progress =
                            positionAndDuration.first.toFloat() / positionAndDuration.second.absoluteValue

                        drawLine(
                            color = colorPalette.collapsedPlayerProgressBar,
                            start = Offset(x = 0f, y = 1.dp.toPx()),
                            end = Offset(x = size.width * progress, y = 1.dp.toPx()),
                            strokeWidth = 60.dp.toPx()
                        )

                        drawLine(
                            color = colorPalette.collapsedPlayerProgressBar,
                            start = Offset(x = 0f, y = 1.dp.toPx()),
                            end = Offset(x = size.width * progress, y = 1.dp.toPx()),
                            strokeWidth = 2.dp.toPx()
                        )
                        drawCircle(
                            color = colorPalette.collapsedPlayerProgressBar,
                            radius = 3.dp.toPx(),
                            center = Offset(x = size.width * progress, y = 1.dp.toPx())
                        )
                         */
                    }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { change, dragAmount ->
                                deltaX = dragAmount
                            },
                            onDragStart = {
                                //Log.d("mediaItemGesture","ondragStart offset ${it}")
                            },
                            onDragEnd = {
                                if (!disablePlayerHorizontalSwipe) {
                                    if (deltaX > 0) {
                                        binder.player.seekToPrevious()
                                    } else {
                                        binder.player.forceSeekToNext()
                                    }

                                }

                            }

                        )
                    }

            ) {

                Spacer(
                    modifier = Modifier
                        .width(2.dp)
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(Dimensions.collapsedPlayer)
                ) {
                    AsyncImage(
                        model = mediaItem.mediaMetadata.artworkUri.thumbnail(Dimensions.thumbnails.song.px),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clip(thumbnailShape)
                            .size(48.dp)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .height(Dimensions.collapsedPlayer)
                        .weight(1f)
                ) {
                    /* minimized player */
                    BasicText(
                        text = mediaItem.mediaMetadata.title?.toString() ?: "",
                        style = typography.xxs.semiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    BasicText(
                        text = mediaItem.mediaMetadata.artist?.toString() ?: "",
                        style = typography.xxs.semiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
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
                        .height(Dimensions.collapsedPlayer)
                ) {
                    IconButton(
                        icon = R.drawable.play_skip_back,
                        color = colorPalette.iconButtonPlayer,
                        onClick = {
                            binder.player.forceSeekToPrevious()
                            if (effectRotationEnabled) isRotated = !isRotated
                        },
                        modifier = Modifier
                            .rotate(rotationAngle)
                            .padding(horizontal = 2.dp, vertical = 8.dp)
                            .size(24.dp)
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(playPauseRoundness))
                            .clickable {
                                if (shouldBePlaying) {
                                    binder.player.pause()
                                } else {
                                    if (binder.player.playbackState == Player.STATE_IDLE) {
                                        binder.player.prepare()
                                    }
                                    binder.player.play()
                                }
                                if (effectRotationEnabled) isRotated = !isRotated
                            }
                            .background(colorPalette.background2)
                            .size(42.dp)
                    ) {
                        Image(
                            painter = painterResource(if (shouldBePlaying) R.drawable.pause else R.drawable.play),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.iconButtonPlayer),
                            modifier = Modifier
                                .rotate(rotationAngle)
                                .align(Alignment.Center)
                                .size(24.dp)
                        )
                    }

                    IconButton(
                        icon = R.drawable.play_skip_forward,
                        color = colorPalette.iconButtonPlayer,
                        onClick = {
                            binder.player.forceSeekToNext()
                            if (effectRotationEnabled) isRotated = !isRotated
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
        }
    ) {


        var windows by remember {
            mutableStateOf(binder.player.currentTimeline.windows)
        }
        var queuedSongs by remember {
            mutableStateOf<List<Song>>(emptyList())
        }
        LaunchedEffect(mediaItem.mediaId, windows) {
            Database.getSongsList(
                windows.map {
                    it.mediaItem.mediaId
                }
            ).collect{ queuedSongs = it}
        }

        var totalPlayTimes = 0L
        queuedSongs.forEach {
            totalPlayTimes += it.durationText?.let { it1 ->
                durationTextToMillis(it1)
            }?.toLong() ?: 0
        }


        var isShowingLyrics by rememberSaveable {
            mutableStateOf(false)
        }

        var isShowingStatsForNerds by rememberSaveable {
            mutableStateOf(false)
        }

        var isShowingEqualizer by rememberSaveable {
            mutableStateOf(false)
        }

        val thumbnailTapEnabled by rememberPreference(thumbnailTapEnabledKey, false)

        val playerBottomSheetState = rememberBottomSheetState(
            60.dp + horizontalBottomPaddingValues.calculateBottomPadding(),
            layoutState.expandedBound
        )

        val lyricsBottomSheetState = rememberBottomSheetState(
            0.dp + horizontalBottomPaddingValues.calculateBottomPadding(),
            layoutState.expandedBound
        )

        val isGradientBackgroundEnabled by rememberPreference(isGradientBackgroundEnabledKey, false)
        val containerModifier =
            if (!isGradientBackgroundEnabled)
                Modifier
                    .clip(shape)
                    .background(colorPalette.background1)
                    .padding(
                        windowInsets
                            .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                            .asPaddingValues()
                    )
                    .padding(bottom = playerBottomSheetState.collapsedBound)
            else
                Modifier
                    .clip(shape)
                    .background(
                        Brush.verticalGradient(
                            0.0f to colorPalette.textSecondary,
                            1.0f to colorPalette.background2,
                            startY = 0.0f,
                            endY = 1500.0f
                        )
                    )
                    .padding(
                        windowInsets
                            .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                            .asPaddingValues()
                    )
                    .padding(bottom = playerBottomSheetState.collapsedBound)

        val thumbnailContent: @Composable (modifier: Modifier) -> Unit = { modifier ->
            var deltaX by remember { mutableStateOf(0f) }
            //var direction by remember { mutableIntStateOf(-1)}
            Thumbnail(
                thumbnailTapEnabledKey = thumbnailTapEnabled,
                isShowingLyrics = isShowingLyrics,
                onShowLyrics = { isShowingLyrics = it },
                isShowingStatsForNerds = isShowingStatsForNerds,
                onShowStatsForNerds = { isShowingStatsForNerds = it },
                isShowingEqualizer = isShowingEqualizer,
                onShowEqualizer = { isShowingEqualizer = it },
                onMaximize = { lyricsBottomSheetState.expandSoft() },
                onDoubleTap = {
                    val currentMediaItem = binder.player.currentMediaItem
                    query {
                        if (Database.like(
                                mediaItem.mediaId,
                                if (likedAt == null) System.currentTimeMillis() else null
                            ) == 0
                        ) {
                            currentMediaItem
                                ?.takeIf { it.mediaId == mediaItem.mediaId }
                                ?.let {
                                    Database.insert(currentMediaItem, Song::toggleLike)
                                }
                        }
                    }
                    if (effectRotationEnabled) isRotated = !isRotated
                },
                modifier = modifier
                    .nestedScroll(layoutState.preUpPostDownNestedScrollConnection)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { change, dragAmount ->
                                deltaX = dragAmount
                            },
                            onDragStart = {
                                //Log.d("mediaItemGesture","ondragStart offset ${it}")
                            },
                            onDragEnd = {
                                if (!disablePlayerHorizontalSwipe) {
                                    if (deltaX > 0) {
                                        binder.player.seekToPreviousMediaItem()
                                        //binder.player.forceSeekToPrevious()
                                        //Log.d("mediaItem","Swipe to LEFT")
                                    } else {
                                        binder.player.forceSeekToNext()
                                        //Log.d("mediaItem","Swipe to RIGHT")
                                    }

                                }

                            }

                        )
                    }

                /* **** */
                    /*
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val (x, y) = dragAmount
                                if(abs(x) > abs(y)){
                                    when {
                                        x > 0 -> {
                                            //right
                                            direction = 0
                                        }
                                        x < 0 -> {
                                            // left
                                            direction = 1
                                        }
                                    }
                                } else {
                                    when {
                                        y > 0 -> {
                                            // down
                                            direction = 2
                                        }
                                        y < 0 -> {
                                            // up
                                            direction = 3
                                        }
                                    }
                                }

                            },
                            onDragEnd = {
                                when (direction) {
                                    0 -> {
                                        //right swipe code here
                                        if (!disablePlayerHorizontalSwipe)
                                            binder.player.seekToPreviousMediaItem()
                                        //Log.d("mediaItem","Swipe to RIGHT")
                                    }
                                    1 -> {
                                        // left swipe code here
                                        if (!disablePlayerHorizontalSwipe)
                                            binder.player.forceSeekToNext()
                                        //Log.d("mediaItem","Swipe to LEFT")
                                    }

                                    2 -> {
                                        // down swipe code here
                                        layoutState.collapseSoft()
                                        //Log.d("mediaItem","Swipe to DOWN")
                                    }
                                    3 -> {
                                        // up swipe code here
                                        playerBottomSheetState.expandSoft()
                                        //Log.d("mediaItem","Swipe to UP")
                                    }

                                }

                            }
                        )
                    }
                */
                /* **** */
            )
        }


        val controlsContent: @Composable (modifier: Modifier) -> Unit = { modifier ->
            Controls(
                media = mediaItem.toUiMedia(positionAndDuration.second),
                mediaId = mediaItem.mediaId,
                title = mediaItem.mediaMetadata.title?.toString(),
                artist = mediaItem.mediaMetadata.artist?.toString(),
                artistIds = artistsInfo,
                albumId = albumId,
                shouldBePlaying = shouldBePlaying,
                position = positionAndDuration.first,
                duration = positionAndDuration.second,
                modifier = modifier
            )
        }

        if (isLandscape) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = containerModifier
                    .padding(top = 20.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(0.66f)
                        .padding(bottom = 10.dp)
                ) {

                    thumbnailContent(
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                    )
                }

                controlsContent(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxHeight()
                        .weight(1f)
                )
            }
        } else {
            /*
            var offsetX by remember { mutableStateOf(0f) }
            var deltaX by remember { mutableStateOf(0f) }
            var direction by remember { mutableStateOf(-1)}
             */
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = containerModifier
                    .padding(top = 10.dp)
                    .drawBehind {
                        if (backgroundProgress == BackgroundProgress.Both || backgroundProgress == BackgroundProgress.Player) {
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
                    /*
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { change, dragAmount ->
                                deltaX = dragAmount
                            },
                            onDragStart = {
                                //Log.d("mediaItemGesture","ondragStart offset ${it}")
                            },
                            onDragEnd = {
                                if (!disablePlayerHorizontalSwipe) {
                                    if (deltaX > 0) {
                                        binder.player.seekToPreviousMediaItem()
                                        //binder.player.forceSeekToPrevious()
                                        Log.d("mediaItem","Swipe to LEFT")
                                    }
                                    else {
                                        binder.player.forceSeekToNext()
                                        Log.d("mediaItem","Swipe to RIGHT")
                                    }

                                }

                            }

                        )
                    }
                     */

            ) {

                if (uiType != UiType.ViMusic) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(30.dp)
                    ) {

                        Image(
                            painter = painterResource(R.drawable.chevron_down),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.collapsedPlayerProgressBar),
                            modifier = Modifier
                                .clickable { layoutState.collapseSoft() }
                                .rotate(rotationAngle)
                                //.padding(10.dp)
                                .size(24.dp)

                        )
                        /*
                        IconButton(
                            icon = R.drawable.chevron_down,
                            color = colorPalette.text,
                            enabled = true,
                            onClick = {
                                layoutState.collapseSoft()
                            },
                            modifier = Modifier
                                //.padding(horizontal = 15.dp)
                                .size(24.dp)
                        )
                         */

                        Image(
                            painter = painterResource(R.drawable.app_icon),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.collapsedPlayerProgressBar),
                            modifier = Modifier
                                .clickable { onGoToHome() }
                                .rotate(rotationAngle)
                                //.padding(10.dp)
                                .size(24.dp)

                        )
                        /*
                        IconButton(
                            icon = R.drawable.app_icon,
                            color = colorPalette.text,
                            enabled = true,
                            onClick = {
                                onGoToHome()
                            },
                            modifier = Modifier
                                //.padding(horizontal = 4.dp)
                                .size(24.dp)
                        )
                         */

                        if(!showButtonPlayerMenu)
                            Image(
                                painter = painterResource(R.drawable.ellipsis_vertical),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorPalette.collapsedPlayerProgressBar),
                                modifier = Modifier
                                    .clickable {
                                        menuState.display {
                                            PlayerMenu(
                                                onDismiss = menuState::hide,
                                                mediaItem = mediaItem,
                                                binder = binder
                                            )
                                        }
                                    }
                                    .rotate(rotationAngle)
                                    //.padding(10.dp)
                                    .size(24.dp)

                            )

                    }
                }

                Spacer(
                    modifier = Modifier
                        .height(20.dp)
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        //.weight(0.5f)
                        .fillMaxHeight(0.55f)
                ) {
                    thumbnailContent(
                        modifier = Modifier
                            .clip(thumbnailShape)
                            .padding(
                                horizontal = playerThumbnailSize.size.dp,
                                vertical = 4.dp
                            )
                    )
                }


                if (showTotalTimeQueue)
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                    ) {
                        Image(
                            painter = painterResource(R.drawable.time),
                            colorFilter = ColorFilter.tint(colorPalette.accent),
                            modifier = Modifier
                                .size(20.dp)
                                .padding(horizontal = 5.dp),
                            contentDescription = "Background Image",
                            contentScale = ContentScale.Fit
                        )
                        BasicText(
                            text = " ${formatAsTime(totalPlayTimes)}",
                            style = typography.xxs.semiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }


                Spacer(
                    modifier = Modifier
                        .height(10.dp)
                )



                controlsContent(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth()
                )
            }
        }


        Queue(
            layoutState = playerBottomSheetState,
            content = {

                val context = LocalContext.current

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween, //Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 12.dp)
                        .fillMaxWidth()

                ) {
                    //if (uiType != UiType.ViMusic) {

                        if(showButtonPlayerDownload)
                        DownloadStateIconButton(
                            icon = if (isDownloaded) R.drawable.downloaded else R.drawable.download,
                            color = if (isDownloaded) colorPalette.text else colorPalette.textDisabled,
                            downloadState = downloadState,
                            onClick = {
                                //if (!isLocal)
                                manageDownload(
                                    context = context,
                                    songId = mediaItem.mediaId,
                                    songTitle = mediaItem.mediaMetadata.title.toString(),
                                    downloadState = isDownloaded
                                )
                            },
                            modifier = Modifier
                                //.padding(horizontal = 4.dp)
                                .size(24.dp)
                        )

                        /*
                        if (showPlaylistSelectDialog) {
                            SelectorDialog(
                                title = stringResource(R.string.playlists),
                                onDismiss = { showPlaylistSelectDialog = false },
                                showItemsIcon = true,
                                values = playlistPreviews.map {
                                    Info(
                                        it.playlist.id.toString(),
                                        "${it.playlist.name} \n ${it.songCount} ${stringResource(R.string.songs)}"
                                    )
                                },
                                onValueSelected = {
                                    //Log.d("mediaItem", it)
                                    var position = 0
                                    query {
                                        position =
                                            Database.getSongMaxPositionToPlaylist(it.toLong())
                                    }
                                    if (position > 0) position++
                                    //Log.d("mediaItemMaxPos", position.toString())
                                    transaction {
                                        Database.insert(mediaItem)
                                        Database.insert(
                                            SongPlaylistMap(
                                                songId = mediaItem.mediaId,
                                                playlistId = it.toLong(),
                                                position = position
                                            )
                                        )
                                    }
                                    //Log.d("mediaItemPos", "add position $position")
                                    showPlaylistSelectDialog = false
                                }
                            )
                        }
                        */

                        if(showButtonPlayerAddToPlaylist)
                            IconButton(
                                icon = R.drawable.add_in_playlist,
                                color = colorPalette.text,
                                onClick = {
                                    menuState.display {
                                        PlaylistsItemMenu(
                                            modifier = Modifier.fillMaxHeight(0.6f),
                                            onDismiss = menuState::hide,
                                            onAddToPlaylist = { playlistPreview ->
                                                position =
                                                    playlistPreview.songCount.minus(1) ?: 0
                                                if (position > 0) position++ else position = 0
                                                        transaction {
                                                            Database.insert(mediaItem)
                                                            Database.insert(
                                                                SongPlaylistMap(
                                                                    songId = mediaItem.mediaId,
                                                                    playlistId = playlistPreview.playlist.id,
                                                                    position = position + 1
                                                                )
                                                            )
                                                        }
                                                }
                                        )
                                    }
                                },
                                modifier = Modifier
                                    //.padding(horizontal = 4.dp)
                                    .size(24.dp)
                            )



                        if(showButtonPlayerLoop)
                        IconButton(
                            icon = R.drawable.repeat,
                            color = if (trackLoopEnabled) colorPalette.text else colorPalette.textDisabled,
                            onClick = {
                                trackLoopEnabled = !trackLoopEnabled
                                if (effectRotationEnabled) isRotated = !isRotated
                            },
                            modifier = Modifier
                                //.padding(horizontal = 4.dp)
                                .size(24.dp)
                        )

                        if(showButtonPlayerShuffle)
                        IconButton(
                            icon = R.drawable.shuffle,
                            color = colorPalette.text,
                            enabled = true,
                            onClick = {
                                binder?.player?.shuffleQueue()
                                binder.player.forceSeekToNext()
                            },
                            modifier = Modifier
                                .size(24.dp),
                        )

                        if(showButtonPlayerLyrics)
                        IconButton(
                            icon = R.drawable.song_lyrics,
                            color = if (isShowingLyrics) colorPalette.text else colorPalette.textDisabled,
                            enabled = true,
                            onClick = {
                                if (isShowingEqualizer) isShowingEqualizer = !isShowingEqualizer
                                isShowingLyrics = !isShowingLyrics
                            },
                            modifier = Modifier
                                .size(24.dp),
                        )


                        if (playerVisualizerType != PlayerVisualizerType.Disabled)
                            IconButton(
                                icon = R.drawable.sound_effect,
                                color = if (isShowingEqualizer) colorPalette.text else colorPalette.textDisabled,
                                enabled = true,
                                onClick = {
                                    if (isShowingLyrics) isShowingLyrics = !isShowingLyrics
                                    isShowingEqualizer = !isShowingEqualizer
                                },
                                modifier = Modifier
                                    .size(24.dp)
                            )


                        if (showButtonPlayerSleepTimer)
                        IconButton(
                            icon = R.drawable.alarm,
                            color = if (sleepTimerMillisLeft != null) colorPalette.text else colorPalette.textDisabled,
                            enabled = true,
                            onClick = {
                                isShowingSleepTimerDialog = true
                            },
                            modifier = Modifier
                                .size(24.dp),
                        )

                        if(showButtonPlayerArrow)
                        IconButton(
                            icon = R.drawable.chevron_up,
                            color = colorPalette.text,
                            enabled = true,
                            onClick = {
                                playerBottomSheetState.expandSoft()
                            },
                            modifier = Modifier
                                .size(24.dp),
                        )

                        if(showButtonPlayerMenu)
                        IconButton(
                            icon = R.drawable.ellipsis_vertical,
                            color = colorPalette.text,
                            onClick = {
                                menuState.display {
                                    PlayerMenu(
                                        onDismiss = menuState::hide,
                                        mediaItem = mediaItem,
                                        binder = binder
                                    )
                                }
                            },
                            modifier = Modifier
                                //.padding(horizontal = 15.dp)
                                .size(24.dp)
                        )


                        if (isLandscape) {
                            IconButton(
                                icon = R.drawable.ellipsis_horizontal,
                                color = colorPalette.text,
                                onClick = {
                                    menuState.display {
                                        PlayerMenu(
                                            onDismiss = menuState::hide,
                                            mediaItem = mediaItem,
                                            binder = binder

                                        )
                                    }
                                },
                                modifier = Modifier
                                    //.padding(horizontal = 4.dp)
                                    .size(24.dp)
                            )
                        }
                }
            },
            backgroundColorProvider = { colorPalette.background2 },
            modifier = Modifier
                .align(Alignment.BottomCenter),
            shape = shape
        )


        FullLyricsSheet(
            layoutState = lyricsBottomSheetState,
            content = {},
            backgroundColorProvider = { colorPalette.background2 },
            onMaximize = { lyricsBottomSheetState.collapseSoft() },
            onRefresh = {
                lyricsBottomSheetState.collapse(tween(50))
                lyricsBottomSheetState.expand(tween(50))
            }
        )

    }

}



