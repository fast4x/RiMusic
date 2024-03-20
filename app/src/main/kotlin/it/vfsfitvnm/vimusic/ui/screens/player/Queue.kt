package it.vfsfitvnm.vimusic.ui.screens.player


import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults.colors
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.offline.Download
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.valentinilk.shimmer.shimmer
import it.vfsfitvnm.compose.reordering.draggedItem
import it.vfsfitvnm.compose.reordering.rememberReorderingState
import it.vfsfitvnm.compose.reordering.reorder
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.BuiltInPlaylist
import it.vfsfitvnm.vimusic.enums.UiType
import it.vfsfitvnm.vimusic.models.Info
import it.vfsfitvnm.vimusic.models.Song
import it.vfsfitvnm.vimusic.models.SongPlaylistMap
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.service.isLocal
import it.vfsfitvnm.vimusic.transaction
import it.vfsfitvnm.vimusic.ui.components.BottomSheet
import it.vfsfitvnm.vimusic.ui.components.BottomSheetState
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.MusicBars
import it.vfsfitvnm.vimusic.ui.components.themed.ConfirmationDialog
import it.vfsfitvnm.vimusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderIconButton
import it.vfsfitvnm.vimusic.ui.components.themed.IconButton
import it.vfsfitvnm.vimusic.ui.components.themed.InputTextDialog
import it.vfsfitvnm.vimusic.ui.components.themed.PlaylistsItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.QueuedMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.SelectorDialog
import it.vfsfitvnm.vimusic.ui.items.SongItem
import it.vfsfitvnm.vimusic.ui.items.SongItemPlaceholder
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.onOverlay
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.BehindMotionSwipe
import it.vfsfitvnm.vimusic.utils.DisposableListener
import it.vfsfitvnm.vimusic.utils.LeftAction
import it.vfsfitvnm.vimusic.utils.RightActions
import it.vfsfitvnm.vimusic.utils.UiTypeKey
import it.vfsfitvnm.vimusic.utils.addNext
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.downloadedStateMedia
import it.vfsfitvnm.vimusic.utils.enqueue
import it.vfsfitvnm.vimusic.utils.getDownloadState
import it.vfsfitvnm.vimusic.utils.isSwipeToActionEnabledKey
import it.vfsfitvnm.vimusic.utils.manageDownload
import it.vfsfitvnm.vimusic.utils.medium
import it.vfsfitvnm.vimusic.utils.queueLoopEnabledKey
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.reorderInQueueEnabledKey
import it.vfsfitvnm.vimusic.utils.shouldBePlaying
import it.vfsfitvnm.vimusic.utils.showButtonPlayerArrowKey
import it.vfsfitvnm.vimusic.utils.shuffleQueue
import it.vfsfitvnm.vimusic.utils.smoothScrollToTop
import it.vfsfitvnm.vimusic.utils.toast
import it.vfsfitvnm.vimusic.utils.windows
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date


@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@androidx.media3.common.util.UnstableApi
@Composable
fun Queue(
    backgroundColorProvider: () -> Color,
    layoutState: BottomSheetState,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
    shape: RoundedCornerShape = RoundedCornerShape(
        topStart = 12.dp,
        topEnd = 12.dp
    )
) {
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
    //val uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)
    val windowInsets = WindowInsets.systemBars

    val horizontalBottomPaddingValues = windowInsets
        .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom).asPaddingValues()
    //val bottomPaddingValues = windowInsets
    //    .only(WindowInsetsSides.Bottom).asPaddingValues()

    val context = LocalContext.current
    val showButtonPlayerArrow by rememberPreference(showButtonPlayerArrowKey, false)

    BottomSheet(
        state = layoutState,
        disableVerticalDrag = showButtonPlayerArrow,
        modifier = modifier,
        collapsedContent = {
                Box(
                    modifier = Modifier
                        //.clip(shape)
                        .drawBehind { drawRect(backgroundColorProvider()) }
                        .fillMaxSize()
                        .padding(horizontalBottomPaddingValues)
                ) {
                    if (!showButtonPlayerArrow)
                        Image(
                            painter = painterResource(R.drawable.horizontal_bold_line_rounded),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.text),
                            modifier = Modifier
                                .absoluteOffset(0.dp, -10.dp)
                                .align(Alignment.TopCenter)
                                .size(30.dp)
                        )

                    content()
                }

        }
    ) {
        val binder = LocalPlayerServiceBinder.current

        binder?.player ?: return@BottomSheet

        val player = binder.player

        var queueLoopEnabled by rememberPreference(queueLoopEnabledKey, defaultValue = true)

        val menuState = LocalMenuState.current

        val thumbnailSizeDp = Dimensions.thumbnails.song
        val thumbnailSizePx = thumbnailSizeDp.px

        var mediaItemIndex by remember {
            mutableStateOf(if (player.mediaItemCount == 0) -1 else player.currentMediaItemIndex)
        }

        var windows by remember {
            mutableStateOf(player.currentTimeline.windows)
        }

        var shouldBePlaying by remember {
            mutableStateOf(binder.player.shouldBePlaying)
        }

        player.DisposableListener {
            object : Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    mediaItemIndex = player.currentMediaItemIndex
                        //if (player.mediaItemCount == 0) -1 else player.currentMediaItemIndex
                }

                override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                    windows = timeline.windows
                    mediaItemIndex = player.currentMediaItemIndex
                        //if (player.mediaItemCount == 0) -1 else player.currentMediaItemIndex
                }

                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    shouldBePlaying = binder.player.shouldBePlaying
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    shouldBePlaying = binder.player.shouldBePlaying
                }
            }
        }

        val reorderingState = rememberReorderingState(
            lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = mediaItemIndex),
            key = windows,
            onDragEnd = player::moveMediaItem,
            extraItemCount = 0
        )

        val rippleIndication = rememberRipple(bounded = false)

        val musicBarsTransition = updateTransition(targetState = mediaItemIndex, label = "")

        var isReorderDisabled by rememberPreference(reorderInQueueEnabledKey, defaultValue = true)

        var downloadState by remember {
            mutableStateOf(Download.STATE_STOPPED)
        }

        var listMediaItems = remember {
            mutableListOf<MediaItem>()
        }
        var listMediaItemsIndex = remember {
            mutableListOf<Int>()
        }

        var selectQueueItems by remember {
            mutableStateOf(false)
        }

        var showSelectTypeClearQueue by remember {
            mutableStateOf(false)
        }
        var position by remember {
            mutableIntStateOf(0)
        }

        var showConfirmDeleteAllDialog by remember {
            mutableStateOf(false)
        }

        if (showConfirmDeleteAllDialog) {
            ConfirmationDialog(
                text = "Do you really want to clean queue?",
                onDismiss = { showConfirmDeleteAllDialog = false },
                onConfirm = {
                    showConfirmDeleteAllDialog = false
                    val mediacount = binder.player.mediaItemCount - 1
                    for (i in mediacount.downTo(0)) {
                        if (i == mediaItemIndex) null else binder.player.removeMediaItem(i)
                    }
                    listMediaItems.clear()
                    listMediaItemsIndex.clear()
                }
            )
        }

        var plistName by remember {
            mutableStateOf("")
        }

        val exportLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
                if (uri == null) return@rememberLauncherForActivityResult

                context.applicationContext.contentResolver.openOutputStream(uri)
                    ?.use { outputStream ->
                        csvWriter().open(outputStream){
                            writeRow("PlaylistBrowseId", "PlaylistName", "MediaId", "Title", "Artists", "Duration", "ThumbnailUrl")
                            if (listMediaItems.isEmpty()) {
                                windows.forEach {
                                    writeRow(
                                        "",
                                        plistName,
                                        it.mediaItem.mediaId,
                                        it.mediaItem.mediaMetadata.title,
                                        it.mediaItem.mediaMetadata.artist,
                                        "",
                                        it.mediaItem.mediaMetadata.artworkUri
                                    )
                                }
                            } else {
                                listMediaItems.forEach {
                                    writeRow(
                                        "",
                                        plistName,
                                        it.mediaId,
                                        it.mediaMetadata.title,
                                        it.mediaMetadata.artist,
                                        "",
                                        it.mediaMetadata.artworkUri
                                    )
                                }
                            }
                        }
                    }

            }

        var isExporting by rememberSaveable {
            mutableStateOf(false)
        }

        if (isExporting) {
            InputTextDialog(
                onDismiss = {
                    isExporting = false
                },
                title = stringResource(R.string.enter_the_playlist_name),
                value = plistName,
                placeholder = stringResource(R.string.enter_the_playlist_name),
                setValue = { text ->
                    plistName = text
                    try {
                        @SuppressLint("SimpleDateFormat")
                        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")
                        exportLauncher.launch("RMPlaylist_${text.take(20)}_${dateFormat.format(
                            Date()
                        )}")
                    } catch (e: ActivityNotFoundException) {
                        context.toast("Couldn't find an application to create documents")
                    }
                }
            )
        }

        val isSwipeToActionEnabled by rememberPreference(isSwipeToActionEnabledKey, true)


        Column {
            Box(
                modifier = Modifier
                    .background(colorPalette.background1)
                    .weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .drawBehind { drawRect(backgroundColorProvider()) }
                        .fillMaxSize()
                        .padding(horizontalBottomPaddingValues)

                ) {
                    if (!showButtonPlayerArrow)
                        Image(
                            painter = painterResource(R.drawable.horizontal_bold_line_rounded),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.text),
                            modifier = Modifier
                                .absoluteOffset(0.dp, -10.dp)
                                .align(Alignment.TopCenter)
                                .size(30.dp)
                        )
                }

                LazyColumn(
                    state = reorderingState.lazyListState,
                    contentPadding = windowInsets
                        .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                        .asPaddingValues(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .nestedScroll(layoutState.preUpPostDownNestedScrollConnection)

                ) {
                    items(
                        items = windows,
                        key = { it.uid.hashCode() }
                    ) { window ->
                        val currentItem by rememberUpdatedState(window)
                        BehindMotionSwipe(
                            content = {
                                var deltaX by remember { mutableStateOf(0f) }
                                val isPlayingThisMediaItem =
                                    mediaItemIndex == window.firstPeriodIndex
                                //val currentItem by rememberUpdatedState(window)
                                val isLocal by remember { derivedStateOf { window.mediaItem.isLocal } }
                                downloadState = getDownloadState(window.mediaItem.mediaId)
                                val isDownloaded =
                                    if (!isLocal) downloadedStateMedia(window.mediaItem.mediaId) else true
                                SongItem(
                                    song = window.mediaItem,
                                    isDownloaded = isDownloaded,
                                    onDownloadClick = {
                                        binder.cache.removeResource(window.mediaItem.mediaId)
                                        if (!isLocal)
                                            manageDownload(
                                                context = context,
                                                songId = window.mediaItem.mediaId,
                                                songTitle = window.mediaItem.mediaMetadata.title.toString(),
                                                downloadState = isDownloaded
                                            )
                                    },
                                    downloadState = downloadState,
                                    thumbnailSizePx = thumbnailSizePx,
                                    thumbnailSizeDp = thumbnailSizeDp,
                                    onThumbnailContent = {
                                        musicBarsTransition.AnimatedVisibility(
                                            visible = { it == window.firstPeriodIndex },
                                            enter = fadeIn(tween(800)),
                                            exit = fadeOut(tween(800)),
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .background(
                                                        color = Color.Black.copy(alpha = 0.25f),
                                                        shape = thumbnailShape
                                                    )
                                                    .size(Dimensions.thumbnails.song)
                                            ) {
                                                if (shouldBePlaying) {
                                                    MusicBars(
                                                        color = colorPalette.onOverlay,
                                                        modifier = Modifier
                                                            .height(24.dp)
                                                    )
                                                } else {
                                                    Image(
                                                        painter = painterResource(R.drawable.play),
                                                        contentDescription = null,
                                                        colorFilter = ColorFilter.tint(colorPalette.onOverlay),
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    trailingContent = {

                                        val checkedState = remember { mutableStateOf(false) }
                                        if (selectQueueItems)
                                            Checkbox(
                                                checked = checkedState.value,
                                                onCheckedChange = {
                                                    checkedState.value = it
                                                    if (it) {
                                                        listMediaItems.add(window.mediaItem)
                                                        listMediaItemsIndex.add(window.firstPeriodIndex)
                                                    } else
                                                    {
                                                        listMediaItems.remove(window.mediaItem)
                                                        listMediaItemsIndex.remove(window.firstPeriodIndex)
                                                    }
                                                },
                                                colors = colors(
                                                    checkedColor = colorPalette.accent,
                                                    uncheckedColor = colorPalette.text
                                                ),
                                                modifier = Modifier
                                                    .scale(0.7f)
                                            )
                                        else checkedState.value = false

                                        if (!isReorderDisabled) {
                                            IconButton(
                                                icon = R.drawable.reorder,
                                                color = colorPalette.textDisabled,
                                                indication = rippleIndication,
                                                onClick = {},
                                                modifier = Modifier
                                                    .reorder(
                                                        reorderingState = reorderingState,
                                                        index = window.firstPeriodIndex
                                                    )
                                                    .size(18.dp)
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .combinedClickable(
                                            onLongClick = {
                                                menuState.display {
                                                    QueuedMediaItemMenu(
                                                        mediaItem = window.mediaItem,
                                                        indexInQueue = if (isPlayingThisMediaItem) null else window.firstPeriodIndex,
                                                        onDismiss = menuState::hide,
                                                        onDownload = {
                                                            manageDownload(
                                                                context = context,
                                                                songId = window.mediaItem.mediaId,
                                                                songTitle = window.mediaItem.mediaMetadata.title.toString(),
                                                                downloadState = isDownloaded
                                                            )
                                                        }

                                                    )
                                                }
                                            },
                                            onClick = {
                                                if (isPlayingThisMediaItem) {
                                                    if (shouldBePlaying) {
                                                        player.pause()
                                                    } else {
                                                        player.play()
                                                    }
                                                } else {
                                                    player.seekToDefaultPosition(window.firstPeriodIndex)
                                                    player.playWhenReady = true
                                                }
                                            }
                                        )
                                        /*
                                        .pointerInput(Unit) {

                                            detectHorizontalDragGestures(
                                                onHorizontalDrag = { change, dragAmount ->
                                                    deltaX = dragAmount
                                                },

                                                onDragEnd = {
                                                    if (!isReorderDisabled && !isSwipeToActionEnabled)
                                                        player.removeMediaItem(window.firstPeriodIndex)
                                                }

                                            )

                                        }
                                         */
                                        .draggedItem(
                                            reorderingState = reorderingState,
                                            index = window.firstPeriodIndex
                                        )
                                        .background(color = colorPalette.background0)

                                )
                            },
                            leftActionsContent = {
                                if (!reorderingState.isDragging)
                                    LeftAction(
                                        icon = R.drawable.share_social,
                                        backgroundColor = Color.Transparent, //colorPalette.background4,
                                        onClick = {
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                type = "text/plain"
                                                putExtra(
                                                    Intent.EXTRA_TEXT,
                                                    "https://music.youtube.com/watch?v=${window.mediaItem.mediaId}"
                                                )
                                            }

                                            context.startActivity(Intent.createChooser(sendIntent, null))
                                        }
                                    )
                            },
                            rightActionsContent = {
                                if (!reorderingState.isDragging) {
                                    //val currentItem by rememberUpdatedState(window)
                                    var likedAt by remember {
                                        mutableStateOf<Long?>(null)
                                    }
                                    LaunchedEffect(Unit, window.mediaItem.mediaId) {
                                        Database.likedAt(window.mediaItem.mediaId).collect { likedAt = it }
                                    }

                                    RightActions(
                                        iconAction1 = if (likedAt == null) R.drawable.heart_outline else R.drawable.heart,
                                        backgroundColorAction1 = Color.Transparent, //colorPalette.background4,
                                        onClickAction1 = {
                                            query {
                                                if (Database.like(
                                                        window.mediaItem.mediaId,
                                                        if (likedAt == null) System.currentTimeMillis() else null
                                                    ) == 0
                                                ) {
                                                    Database.insert(window.mediaItem, Song::toggleLike)
                                                }
                                            }
                                        },
                                        iconAction2 = R.drawable.trash,
                                        backgroundColorAction2 = Color.Transparent, //colorPalette.iconButtonPlayer,
                                        onClickAction2 = {
                                                player.removeMediaItem(currentItem.firstPeriodIndex)
                                        }
                                    )

                                }
                            },
                            onHorizontalSwipeWhenActionDisabled = {
                                if (!isReorderDisabled && !isSwipeToActionEnabled)
                                    player.removeMediaItem(currentItem.firstPeriodIndex)
                                else context.toast(context.resources.getString(R.string.locked))
                            }
                        )
                    }

                    item {
                        if (binder.isLoadingRadio) {
                            Column(
                                modifier = Modifier
                                    .shimmer()
                            ) {
                                repeat(3) { index ->
                                    SongItemPlaceholder(
                                        thumbnailSizeDp = thumbnailSizeDp,
                                        modifier = Modifier
                                            .alpha(1f - index * 0.125f)
                                            .fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }

                /*
                if(uiType == UiType.ViMusic)
                FloatingActionsContainerWithScrollToTop(
                    lazyListState = reorderingState.lazyListState,
                    iconId = R.drawable.shuffle,
                    visible = !reorderingState.isDragging,
                    windowInsets = windowInsets.only(WindowInsetsSides.Horizontal),
                    onClick = {
                        reorderingState.coroutineScope.launch {
                            reorderingState.lazyListState.smoothScrollToTop()
                        }.invokeOnCompletion {
                            player.shuffleQueue()
                        }
                    }
                )
                */

                //FloatingActionsContainerWithScrollToTop(lazyListState = reorderingState.lazyListState)

            }


            Box(
                modifier = Modifier
                    //.clip(shape)
                    .clickable(onClick = layoutState::collapseSoft)
                    .background(colorPalette.background1)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(horizontalBottomPaddingValues)
                    .height(60.dp) //bottom bar queue
            ) {
                if (!showButtonPlayerArrow)
                    Image(
                        painter = painterResource(R.drawable.horizontal_bold_line_rounded),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette.text),
                        modifier = Modifier
                            .absoluteOffset(0.dp, -10.dp)
                            .align(Alignment.TopCenter)
                            .size(30.dp)
                    )


            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .align(Alignment.CenterStart)

            ) {

                 BasicText(
                    text = "${binder.player.mediaItemCount} " + stringResource(R.string.songs), //+ " " + stringResource(R.string.on_queue),
                    style = typography.xxs.medium,
                )

            }


                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(horizontal = 4.dp)
                       // .fillMaxHeight()

                ) {

                    IconButton(
                        icon = if (isReorderDisabled) R.drawable.locked else R.drawable.unlocked,
                        color = colorPalette.text,
                        onClick = { isReorderDisabled = !isReorderDisabled },
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(24.dp)
                    )

                    Spacer(
                        modifier = Modifier
                            .width(12.dp)
                    )
                    IconButton(
                        icon = R.drawable.repeat,
                        color = if (queueLoopEnabled) colorPalette.text else colorPalette.textDisabled,
                        onClick = { queueLoopEnabled = !queueLoopEnabled },
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(24.dp)
                    )

                    Spacer(
                        modifier = Modifier
                            .width(12.dp)
                    )

                    IconButton(
                        icon = R.drawable.shuffle,
                        color = colorPalette.text,
                        enabled = !reorderingState.isDragging,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(24.dp),
                        onClick = {
                            reorderingState.coroutineScope.launch {
                                reorderingState.lazyListState.smoothScrollToTop()
                            }.invokeOnCompletion {
                                player.shuffleQueue()
                            }
                        }
                    )

                    Spacer(
                        modifier = Modifier
                            .width(12.dp)
                    )
                    HeaderIconButton(
                        icon = R.drawable.ellipsis_horizontal,
                        color = if (windows.isNotEmpty() == true) colorPalette.text else colorPalette.textDisabled,
                        enabled = windows.isNotEmpty() == true,
                        modifier = Modifier
                            .padding(end = 4.dp),
                        onClick = {
                            menuState.display {
                                PlaylistsItemMenu(
                                    onDismiss = menuState::hide,
                                    onSelect = { selectQueueItems = true },
                                    onUncheck = {
                                        selectQueueItems = false
                                        listMediaItems.clear()
                                        listMediaItemsIndex.clear()
                                    },
                                    onDelete = {
                                        if (listMediaItemsIndex.isNotEmpty())
                                            //showSelectTypeClearQueue = true else
                                            {
                                            val mediacount = listMediaItemsIndex.size - 1
                                            listMediaItemsIndex.sort()
                                            for (i in mediacount.downTo(0)) {
                                                //if (i == mediaItemIndex) null else
                                                binder.player.removeMediaItem(listMediaItemsIndex[i])
                                            }
                                            listMediaItemsIndex.clear()
                                            listMediaItems.clear()
                                            selectQueueItems = false
                                        } else {
                                            showConfirmDeleteAllDialog = true
                                        }
                                    },
                                    onAddToPlaylist = { playlistPreview ->
                                        position =
                                            playlistPreview.songCount.minus(1) ?: 0
                                        //Log.d("mediaItem", " maxPos in Playlist $it ${position}")
                                        if (position > 0) position++ else position = 0
                                        //Log.d("mediaItem", "next initial pos ${position}")
                                        if (listMediaItems.isEmpty()) {
                                            windows.forEachIndexed { index, song ->
                                                transaction {
                                                    Database.insert(song.mediaItem)
                                                    Database.insert(
                                                        SongPlaylistMap(
                                                            songId = song.mediaItem.mediaId,
                                                            playlistId = playlistPreview.playlist.id,
                                                            position = position + index
                                                        )
                                                    )
                                                }
                                                //Log.d("mediaItemPos", "added position ${position + index}")
                                            }
                                        } else {
                                            listMediaItems.forEachIndexed { index, song ->
                                                //Log.d("mediaItemMaxPos", position.toString())
                                                transaction {
                                                    Database.insert(song)
                                                    Database.insert(
                                                        SongPlaylistMap(
                                                            songId = song.mediaId,
                                                            playlistId = playlistPreview.playlist.id,
                                                            position = position + index
                                                        )
                                                    )
                                                }
                                                //Log.d("mediaItemPos", "add position $position")
                                            }
                                            listMediaItems.clear()
                                            listMediaItemsIndex.clear()
                                            selectQueueItems = false
                                        }
                                    },
                                    onExport = {
                                        isExporting = true
                                    }
                                )
                            }
                        }
                    )


                    if (showButtonPlayerArrow) {
                        Spacer(
                            modifier = Modifier
                                .width(12.dp)
                        )
                        IconButton(
                            icon = R.drawable.chevron_down,
                            color = colorPalette.text,
                            onClick = { layoutState.collapseSoft() },
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(24.dp)
                        )
                    }


                }
            }
        }
        FloatingActionsContainerWithScrollToTop(lazyListState = reorderingState.lazyListState)
    }
}
