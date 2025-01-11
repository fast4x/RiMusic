package it.fast4x.rimusic.ui.components.themed

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
import it.fast4x.compose.persist.persistList
import it.fast4x.innertube.YtMusic
import it.fast4x.innertube.models.NavigationEndpoint
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.MODIFIED_PREFIX
import it.fast4x.rimusic.MONTHLY_PREFIX
import it.fast4x.rimusic.PINNED_PREFIX
import it.fast4x.rimusic.PIPED_PREFIX
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.PlaylistSortBy
import it.fast4x.rimusic.enums.SortOrder
import it.fast4x.rimusic.models.Artist
import it.fast4x.rimusic.models.Info
import it.fast4x.rimusic.models.Playlist
import it.fast4x.rimusic.models.SongPlaylistMap
import it.fast4x.rimusic.service.isLocal
import it.fast4x.rimusic.ui.items.SongItem
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.favoritesIcon
import it.fast4x.rimusic.ui.styling.px
import it.fast4x.rimusic.utils.addNext
import it.fast4x.rimusic.utils.enqueue
import it.fast4x.rimusic.utils.forcePlay
import it.fast4x.rimusic.utils.formatAsDuration
import it.fast4x.rimusic.utils.getDownloadState
import it.fast4x.rimusic.utils.isDownloadedSong
import it.fast4x.rimusic.utils.manageDownload
import it.fast4x.rimusic.utils.mediaItemToggleLike
import it.fast4x.rimusic.utils.playlistSortByKey
import it.fast4x.rimusic.utils.playlistSortOrderKey
import it.fast4x.rimusic.utils.positionAndDurationState
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.thumbnail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.context
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.service.MyDownloadHelper
import it.fast4x.rimusic.typography
import it.fast4x.rimusic.ui.screens.settings.isYouTubeSyncEnabled
import it.fast4x.rimusic.utils.getLikeState
import it.fast4x.rimusic.utils.setDisLikeState

@OptIn(UnstableApi::class)
@Composable
fun NonQueuedMediaItemGridMenu(
    navController: NavController,
    onDismiss: () -> Unit,
    mediaItem: MediaItem,
    modifier: Modifier = Modifier,
    onRemoveFromPlaylist: (() -> Unit)? = null,
    onHideFromDatabase: (() -> Unit)? = null,
    onRemoveFromQuickPicks: (() -> Unit)? = null,
    onDownload: (() -> Unit)? = null,
    disableScrollingText: Boolean
) {
    val binder = LocalPlayerServiceBinder.current
    val context = LocalContext.current

    BaseMediaItemGridMenu(
        navController = navController,
        mediaItem = mediaItem,
        onDismiss = onDismiss,
        onStartRadio = {
            binder?.stopRadio()
            binder?.player?.forcePlay(mediaItem)
            binder?.setupRadio(
                NavigationEndpoint.Endpoint.Watch(
                    videoId = mediaItem.mediaId,
                    playlistId = mediaItem.mediaMetadata.extras?.getString("playlistId")
                )
            )
        },
        onPlayNext = { binder?.player?.addNext(mediaItem, context) },
        onEnqueue = { binder?.player?.enqueue(mediaItem, context) },
        onDownload = onDownload,
        onRemoveFromPlaylist = onRemoveFromPlaylist,
        onHideFromDatabase = onHideFromDatabase,
        onRemoveFromQuickPicks = onRemoveFromQuickPicks,
        modifier = modifier,
        disableScrollingText = disableScrollingText
    )
}

@Composable
fun BaseMediaItemGridMenu(
    navController: NavController,
    onDismiss: () -> Unit,
    mediaItem: MediaItem,
    modifier: Modifier = Modifier,
    onGoToEqualizer: (() -> Unit)? = null,
    onShowSleepTimer: (() -> Unit)? = null,
    onStartRadio: (() -> Unit)? = null,
    onPlayNext: (() -> Unit)? = null,
    onEnqueue: (() -> Unit)? = null,
    onDownload: (() -> Unit)? = null,
    onRemoveFromQueue: (() -> Unit)? = null,
    onRemoveFromPlaylist: (() -> Unit)? = null,
    onHideFromDatabase: (() -> Unit)? = null,
    onDeleteFromDatabase: (() -> Unit)? = null,
    onRemoveFromQuickPicks: (() -> Unit)? = null,
    onClosePlayer: (() -> Unit)? = null,
    onGoToPlaylist: ((Long) -> Unit)? = null,
    onAddToPreferites: (() -> Unit)? = null,
    disableScrollingText: Boolean
) {
    //val context = LocalContext.current

    MediaItemGridMenu(
        navController = navController,
        mediaItem = mediaItem,
        onDismiss = onDismiss,
        onGoToEqualizer = onGoToEqualizer,
        onShowSleepTimer = onShowSleepTimer,
        onStartRadio = onStartRadio,
        onPlayNext = onPlayNext,
        onEnqueue = onEnqueue,
        onDownload = onDownload,
        onAddToPreferites = onAddToPreferites,
        onAddToPlaylist = { playlist, position ->
            Database.asyncTransaction {
                insert(mediaItem)
                insert(
                    SongPlaylistMap(
                        songId = mediaItem.mediaId,
                        playlistId = insert(playlist).takeIf { it != -1L } ?: playlist.id,
                        position = position
                    )
                )
            }
            if(isYouTubeSyncEnabled())
                CoroutineScope(Dispatchers.IO).launch {
                    playlist.browseId?.let { YtMusic.addToPlaylist(it, mediaItem.mediaId) }
                }
        },
        onHideFromDatabase = onHideFromDatabase,
        onDeleteFromDatabase = onDeleteFromDatabase,
        onRemoveFromPlaylist = onRemoveFromPlaylist,
        onRemoveFromQueue = onRemoveFromQueue,
        onGoToAlbum =   {
            navController.navigate(route = "${NavRoutes.album.name}/${it}")
            if (onClosePlayer != null) {
                onClosePlayer()
            }
        }, //albumRoute::global,
        onGoToArtist = {
            navController.navigate(route = "${NavRoutes.artist.name}/${it}")
            if (onClosePlayer != null) {
                onClosePlayer()
            }
        },
        /*
        onShare = {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_TEXT,
                    "https://music.youtube.com/watch?v=${mediaItem.mediaId}"
                )
            }

            context.startActivity(Intent.createChooser(sendIntent, null))
        },
         */
        onRemoveFromQuickPicks = onRemoveFromQuickPicks,
        onGoToPlaylist = {
            navController.navigate(route = "${NavRoutes.localPlaylist.name}/$it")
        },
        modifier = modifier,
        disableScrollingText = disableScrollingText
    )
}

@Composable
fun MiniMediaItemGridMenu(
    navController: NavController,
    onDismiss: () -> Unit,
    mediaItem: MediaItem,
    onGoToPlaylist: ((Long) -> Unit)? = null,
    onAddToPreferites: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    disableScrollingText: Boolean
) {

    MediaItemGridMenu(
        navController = navController,
        mediaItem = mediaItem,
        onDismiss = onDismiss,
        onAddToPlaylist = { playlist, position ->
            Database.asyncTransaction {
                insert(mediaItem)
                insert(
                    SongPlaylistMap(
                        songId = mediaItem.mediaId,
                        playlistId = insert(playlist).takeIf { it != -1L } ?: playlist.id,
                        position = position
                    )
                )
            }

            if(isYouTubeSyncEnabled())
                CoroutineScope(Dispatchers.IO).launch {
                    playlist.browseId?.let { YtMusic.addToPlaylist(it, mediaItem.mediaId) }
                }

            onDismiss()
        },
        onGoToPlaylist = {
            navController.navigate(route = "${NavRoutes.localPlaylist.name}/$it")
            if (onGoToPlaylist != null) {
                onGoToPlaylist(it)
            }
        },
        onAddToPreferites = onAddToPreferites,
        modifier = modifier,
        disableScrollingText = disableScrollingText
    )
}

@kotlin.OptIn(ExperimentalTextApi::class)
@OptIn(UnstableApi::class)
@Composable
fun MediaItemGridMenu (
    navController: NavController,
    onDismiss: () -> Unit,
    mediaItem: MediaItem,
    modifier: Modifier = Modifier,
    onGoToEqualizer: (() -> Unit)? = null,
    onShowSleepTimer: (() -> Unit)? = null,
    onStartRadio: (() -> Unit)? = null,
    onPlayNext: (() -> Unit)? = null,
    onEnqueue: (() -> Unit)? = null,
    onDownload: (() -> Unit)? = null,
    onHideFromDatabase: (() -> Unit)? = null,
    onDeleteFromDatabase: (() -> Unit)? = null,
    onRemoveFromQueue: (() -> Unit)? = null,
    onRemoveFromPlaylist: (() -> Unit)? = null,
    onAddToPreferites: (() -> Unit)?,
    onAddToPlaylist: ((Playlist, Int) -> Unit)? = null,
    onGoToAlbum: ((String) -> Unit)? = null,
    onGoToArtist: ((String) -> Unit)? = null,
    onRemoveFromQuickPicks: (() -> Unit)? = null,
    onGoToPlaylist: ((Long) -> Unit)?,
    disableScrollingText: Boolean
) {
    val binder = LocalPlayerServiceBinder.current
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    val isLocal by remember { derivedStateOf { mediaItem.isLocal } }

    var likedAt by remember {
        mutableStateOf<Long?>(null)
    }
    LaunchedEffect(Unit, mediaItem.mediaId) {
        Database.likedAt(mediaItem.mediaId).collect {
            likedAt = it
        }
    }

    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }

    downloadState = getDownloadState(mediaItem.mediaId)
    val isDownloaded = if (!isLocal) isDownloadedSong(mediaItem.mediaId) else true
    val thumbnailSizeDp = Dimensions.thumbnails.song + 20.dp
    val thumbnailSizePx = thumbnailSizeDp.px
    val thumbnailArtistSizeDp = Dimensions.thumbnails.song + 10.dp

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

    var artistsList by persistList<Artist?>("home/artists")
    var artistIds = remember { mutableListOf("") }

    LaunchedEffect(Unit, mediaItem.mediaId) {
        withContext(Dispatchers.IO) {
            if (albumInfo == null)
                albumInfo = Database.songAlbumInfo(mediaItem.mediaId)
            if (artistsInfo == null)
                artistsInfo = Database.songArtistInfo(mediaItem.mediaId)

            artistsInfo?.forEach { info ->
                if (info.id.isNotEmpty()) artistIds.add(info.id)
            }
            Database.getArtistsList(artistIds).collect { artistsList = it }
        }
    }


    var showSelectDialogListenOn by remember {
        mutableStateOf(false)
    }

    if (showSelectDialogListenOn)
        SelectorDialog(
            title = stringResource(R.string.listen_on),
            onDismiss = { showSelectDialogListenOn = false },
            values = listOf(
                Info(
                    "https://youtube.com/watch?v=${mediaItem.mediaId}",
                    stringResource(R.string.listen_on_youtube)
                ),
                Info(
                    "https://music.youtube.com/watch?v=${mediaItem.mediaId}",
                    stringResource(R.string.listen_on_youtube_music)
                ),
                Info(
                    "https://piped.kavin.rocks/watch?v=${mediaItem.mediaId}&playerAutoPlay=true",
                    stringResource(R.string.listen_on_piped)
                ),
                Info(
                    "https://yewtu.be/watch?v=${mediaItem.mediaId}&autoplay=1",
                    stringResource(R.string.listen_on_invidious)
                )
            ),
            onValueSelected = {
                binder?.player?.pause()
                showSelectDialogListenOn = false
                uriHandler.openUri(it)
            }
        )

    var isViewingPlaylists by remember {
        mutableStateOf(false)
    }

    val height by remember {
        mutableStateOf(0.dp)
    }

    val topContent = @Composable {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(end = 12.dp)
        ) {
            SongItem(
                mediaItem = mediaItem,
                thumbnailUrl = mediaItem.mediaMetadata.artworkUri.thumbnail(thumbnailSizePx)
                    ?.toString(),
                onDownloadClick = {
                    binder?.cache?.removeResource(mediaItem.mediaId)
                    CoroutineScope(Dispatchers.IO).launch {
                        Database.deleteFormat( mediaItem.mediaId )
                    }
                    if (!isLocal)
                        manageDownload(
                            context = context,
                            mediaItem = mediaItem,
                            downloadState = isDownloaded
                        )
                },
                downloadState = downloadState,
                thumbnailSizeDp = thumbnailSizeDp,
                modifier = Modifier
                    .weight(1f),
                disableScrollingText = disableScrollingText
            )


            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    icon = getLikeState(mediaItem.mediaId),
                    color = colorPalette().favoritesIcon,
                    onClick = {
                        mediaItemToggleLike(mediaItem)
                        Database.asyncQuery {
                            if(songliked(mediaItem.mediaId) != 0){
                                MyDownloadHelper.autoDownloadWhenLiked(context(), mediaItem)
                            }
                        }
                    },
                    onLongClick = {
                        Database.asyncTransaction {
                            if (like(mediaItem.mediaId, setDisLikeState(likedAt)) == 0) {
                                insert(mediaItem, Song::toggleDislike)
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(all = 4.dp)
                        .size(24.dp)
                )

                if (!isLocal)
                    IconButton(
                        icon = R.drawable.share_social,
                        color = colorPalette().text,
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "https://music.youtube.com/watch?v=${mediaItem.mediaId}"
                                )
                            }

                            context.startActivity(Intent.createChooser(sendIntent, null))
                        },
                        modifier = Modifier
                            .padding(all = 4.dp)
                            .size(24.dp)
                    )


            }

        }
    }

    var showCircularSlider by remember {
        mutableStateOf(false)
    }
    var isShowingSleepTimerDialog by remember {
        mutableStateOf(false)
    }

    val sleepTimerMillisLeft by (binder?.sleepTimerMillisLeft
        ?: flowOf(null))
        .collectAsState(initial = null)

    val positionAndDuration = binder?.player?.positionAndDurationState()

    var timeRemaining by remember { mutableIntStateOf(0) }

    if (positionAndDuration != null) {
        timeRemaining = positionAndDuration.value.second.toInt() - positionAndDuration.value.first.toInt()
    }

    //val timeToStop = System.currentTimeMillis()

    if (isShowingSleepTimerDialog) {
        if (sleepTimerMillisLeft != null) {
            ConfirmationDialog(
                text = stringResource(R.string.stop_sleep_timer),
                cancelText = stringResource(R.string.no),
                confirmText = stringResource(R.string.stop),
                onDismiss = { isShowingSleepTimerDialog = false },
                onConfirm = {
                    binder?.cancelSleepTimer()
                    onDismiss()
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
                    style = typography().s.semiBold,
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
                                .background(colorPalette().background0)
                        ) {
                            BasicText(
                                text = "-",
                                style = typography().xs.semiBold
                            )
                        }

                        Box(contentAlignment = Alignment.Center) {
                            BasicText(
                                text = stringResource(
                                    R.string.left,
                                    formatAsDuration(amount * 5 * 60 * 1000L)
                                ),
                                style = typography().s.semiBold,
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
                                .background(colorPalette().background0)
                        ) {
                            BasicText(
                                text = "+",
                                style = typography().xs.semiBold
                            )
                        }

                    } else {
                        CircularSlider(
                            stroke = 40f,
                            thumbColor = colorPalette().accent,
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
                        color = colorPalette().text
                    )
                    IconButton(
                        onClick = { isShowingSleepTimerDialog = false },
                        icon = R.drawable.close,
                        color = colorPalette().text
                    )
                    IconButton(
                        enabled = amount > 0,
                        onClick = {
                            binder?.startSleepTimer(amount * 5 * 60 * 1000L)
                            isShowingSleepTimerDialog = false
                        },
                        icon = R.drawable.checkmark,
                        color = colorPalette().accent
                    )
                }
            }
        }
    }

    var showDialogChangeSongTitle by remember {
        mutableStateOf(false)
    }

    var songSaved by remember {
        mutableStateOf(0)
    }
    LaunchedEffect(Unit, mediaItem.mediaId) {
        withContext(Dispatchers.IO) {
            songSaved = Database.songExist(mediaItem.mediaId)
        }
    }

    if (showDialogChangeSongTitle)
        InputTextDialog(
            onDismiss = { showDialogChangeSongTitle = false },
            title = stringResource(R.string.update_title),
            value = mediaItem.mediaMetadata.title.toString(),
            placeholder = stringResource(R.string.title),
            setValue = {
                if (it.isNotEmpty()) {
                    Database.asyncTransaction {
                        updateSongTitle(mediaItem.mediaId, it)
                    }
                }
            },
            prefix = MODIFIED_PREFIX
        )

    AnimatedContent(
        targetState = isViewingPlaylists,
        transitionSpec = {
            val animationSpec = tween<IntOffset>(400)
            val slideDirection = if (targetState) AnimatedContentTransitionScope.SlideDirection.Left
            else AnimatedContentTransitionScope.SlideDirection.Right

            slideIntoContainer(slideDirection, animationSpec) togetherWith
                    slideOutOfContainer(slideDirection, animationSpec)
        }, label = ""
    ) { currentIsViewingPlaylists ->
        if (currentIsViewingPlaylists) {
            val sortBy by rememberPreference(playlistSortByKey, PlaylistSortBy.DateAdded)
            val sortOrder by rememberPreference(playlistSortOrderKey, SortOrder.Descending)
            val playlistPreviews by remember {
                Database.playlistPreviews(sortBy, sortOrder)
            }.collectAsState(initial = emptyList(), context = Dispatchers.IO)

            val playlistIds by remember {
                Database.getPlaylistsWithSong(mediaItem.mediaId)
            }.collectAsState(initial = emptyList(), context = Dispatchers.IO)

            val pinnedPlaylists = playlistPreviews.filter {
                it.playlist.name.startsWith(PINNED_PREFIX, 0, true)
            }

            val unpinnedPlaylists = playlistPreviews.filter {
                !it.playlist.name.startsWith(PINNED_PREFIX, 0, true) &&
                !it.playlist.name.startsWith(MONTHLY_PREFIX, 0, true) //&&
                //!it.playlist.name.startsWith(PIPED_PREFIX, 0, true)
            }

            var isCreatingNewPlaylist by rememberSaveable {
                mutableStateOf(false)
            }

            if (isCreatingNewPlaylist && onAddToPlaylist != null) {
                InputTextDialog(
                    onDismiss = { isCreatingNewPlaylist = false },
                    title = stringResource(R.string.enter_the_playlist_name),
                    value = "",
                    placeholder = stringResource(R.string.enter_the_playlist_name),
                    setValue = { text ->
                        onDismiss()
                        onAddToPlaylist(Playlist(name = text), 0)
                    }
                )
            }

            BackHandler {
                isViewingPlaylists = false
            }

            Menu(
                modifier = modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                ) {
                    IconButton(
                        onClick = { isViewingPlaylists = false },
                        icon = R.drawable.chevron_back,
                        color = colorPalette().textSecondary,
                        modifier = Modifier
                            .padding(all = 4.dp)
                            .size(20.dp)
                    )

                    if (onAddToPlaylist != null) {
                        SecondaryTextButton(
                            text = stringResource(R.string.new_playlist),
                            onClick = { isCreatingNewPlaylist = true },
                            alternative = true
                        )
                    }
                }

                if (pinnedPlaylists.isNotEmpty()) {
                    BasicText(
                        text = stringResource(R.string.pinned_playlists),
                        style = typography().m.semiBold,
                        modifier = modifier.padding(start = 20.dp, top = 5.dp)
                    )

                    onAddToPlaylist?.let { onAddToPlaylist ->
                        pinnedPlaylists.forEach { playlistPreview ->
                            MenuEntry(
                                icon = if (playlistIds.contains(playlistPreview.playlist.id)) R.drawable.checkmark else R.drawable.add_in_playlist,
                                text = playlistPreview.playlist.name.substringAfter(PINNED_PREFIX),
                                secondaryText = "${playlistPreview.songCount} " + stringResource(R.string.songs),
                                onClick = {
                                    onDismiss()
                                    onAddToPlaylist(
                                        playlistPreview.playlist,
                                        playlistPreview.songCount
                                    )
                                },
                                trailingContent = {
                                    IconButton(
                                        icon = R.drawable.open,
                                        color = colorPalette().text,
                                        onClick = {
                                            if (onGoToPlaylist != null) {
                                                onGoToPlaylist(playlistPreview.playlist.id)
                                                onDismiss()
                                            }
                                            navController.navigate(route = "${NavRoutes.localPlaylist.name}/${playlistPreview.playlist.id}")
                                        },
                                        modifier = Modifier
                                            .size(24.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                if (unpinnedPlaylists.isNotEmpty()) {
                    BasicText(
                        text = stringResource(R.string.playlists),
                        style = typography().m.semiBold,
                        modifier = modifier.padding(start = 20.dp, top = 5.dp)
                    )

                    onAddToPlaylist?.let { onAddToPlaylist ->
                        unpinnedPlaylists.forEach { playlistPreview ->
                            MenuEntry(
                                icon = if (playlistIds.contains(playlistPreview.playlist.id)) R.drawable.checkmark else R.drawable.add_in_playlist,
                                text = playlistPreview.playlist.name,
                                secondaryText = "${playlistPreview.songCount} " + stringResource(R.string.songs),
                                onClick = {
                                    onDismiss()
                                    onAddToPlaylist(
                                        playlistPreview.playlist,
                                        playlistPreview.songCount
                                    )
                                },
                                trailingContent = {
                                    if (playlistPreview.playlist.name.startsWith(PIPED_PREFIX, 0, true))
                                        Image(
                                            painter = painterResource(R.drawable.piped_logo),
                                            contentDescription = null,
                                            colorFilter = ColorFilter.tint(colorPalette().red),
                                            modifier = Modifier
                                                .size(18.dp)
                                        )

                                    IconButton(
                                        icon = R.drawable.open,
                                        color = colorPalette().text,
                                        onClick = {
                                            if (onGoToPlaylist != null) {
                                                onGoToPlaylist(playlistPreview.playlist.id)
                                                onDismiss()
                                            }
                                            navController.navigate(route = "${NavRoutes.localPlaylist.name}/${playlistPreview.playlist.id}")
                                        },
                                        modifier = Modifier
                                            .size(24.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        } else {
            val colorPalette = colorPalette()

            GridMenu(
                contentPadding = PaddingValues(
                    start = 8.dp,
                    top = 8.dp,
                    end = 8.dp,
                    bottom = 8.dp + WindowInsets.systemBars.asPaddingValues()
                        .calculateBottomPadding()
                ),
                topContent = {
                    topContent()
                }
            ) {

                if (!isLocal && songSaved > 0) {
                    GridMenuItem(
                        icon = R.drawable.title_edit,
                        title = R.string.update_title,
                        colorIcon = colorPalette.text,
                        colorText = colorPalette.text,
                        onClick = {
                            showDialogChangeSongTitle = true
                        }
                    )
                }

                if (!isLocal) onStartRadio?.let { onStartRadio ->
                    GridMenuItem(
                        icon = R.drawable.radio,
                        title = R.string.start_radio,
                        colorIcon = colorPalette.text,
                        colorText = colorPalette.text,
                        onClick = {
                            onDismiss()
                            onStartRadio()
                        }
                    )
                }
                onPlayNext?.let { onPlayNext ->
                    GridMenuItem(
                        icon = R.drawable.play_skip_forward,
                        title = R.string.play_next,
                        colorIcon = colorPalette.text,
                        colorText = colorPalette.text,
                        onClick = {
                            onDismiss()
                            onPlayNext()
                        }
                    )
                }

                onEnqueue?.let { onEnqueue ->
                    GridMenuItem(
                        icon = R.drawable.enqueue,
                        title = R.string.enqueue,
                        colorIcon = colorPalette.text,
                        colorText = colorPalette.text,
                        onClick = {
                            onDismiss()
                            onEnqueue()
                        }
                    )
                }


                onDownload?.let { onDownload ->
                    GridMenuItem(
                        icon = if (!isDownloaded) R.drawable.download else R.drawable.downloaded,
                        title = if (!isDownloaded) R.string.download else R.string.downloaded,
                        colorIcon = colorPalette.text,
                        colorText = colorPalette.text,
                        onClick = {
                            onDismiss()
                            onDownload()
                        }
                    )
                }

                onGoToEqualizer?.let { onGoToEqualizer ->
                    GridMenuItem(
                        icon = R.drawable.equalizer,
                        title = R.string.equalizer,
                        colorIcon = colorPalette.text,
                        colorText = colorPalette.text,
                        onClick = {
                            onDismiss()
                            onGoToEqualizer()
                        }
                    )
                }


                GridMenuItem(
                    icon = R.drawable.sleep,
                    title = R.string.sleep_timer,
                    titleString = sleepTimerMillisLeft?.let {
                        formatAsDuration(it)
                    } ?: "",
                    colorIcon = colorPalette.text,
                    colorText = colorPalette.text,
                    onClick = {
                        isShowingSleepTimerDialog = true
                    }
                )

                if (onAddToPreferites != null)
                    GridMenuItem(
                        icon = R.drawable.heart,
                        title = R.string.add_to_favorites,
                        colorIcon = colorPalette.text,
                        colorText = colorPalette.text,
                        onClick = onAddToPreferites
                    )

                onAddToPlaylist?.let { onAddToPlaylist ->
                    GridMenuItem(
                        icon = R.drawable.add_in_playlist,
                        title = R.string.add_to_playlist,
                        colorIcon = colorPalette.text,
                        colorText = colorPalette.text,
                        onClick = {
                            isViewingPlaylists = true
                        }
                    )
                }

                if (!isLocal)
                    onGoToAlbum?.let { onGoToAlbum ->
                        albumInfo?.let { (albumId) ->
                            GridMenuItem(
                                icon = R.drawable.album,
                                title = R.string.go_to_album,
                                colorIcon = colorPalette.text,
                                colorText = colorPalette.text,
                                onClick = {
                                    onDismiss()
                                    onGoToAlbum(albumId)
                                }
                            )
                        }
                }

                if (!isLocal)
                    onGoToArtist?.let { onGoToArtist ->
                        artistsInfo?.forEach { (authorId, authorName) ->
                            GridMenuItem(
                                icon = R.drawable.artists,
                                title = R.string.more_of,
                                titleString = authorName ?: "",
                                colorIcon = colorPalette.text,
                                colorText = colorPalette.text,
                                onClick = {
                                    onDismiss()
                                    onGoToArtist(authorId)
                                }
                            )
                        }
                    }

                if (!isLocal)
                    GridMenuItem(
                        icon = R.drawable.play,
                        title = R.string.listen_on,
                        colorIcon = colorPalette.text,
                        colorText = colorPalette.text,
                        onClick = {
                            showSelectDialogListenOn = true
                        }
                    )

                onRemoveFromQueue?.let { onRemoveFromQueue ->
                    GridMenuItem(
                        icon = R.drawable.trash,
                        title = R.string.remove_from_queue,
                        colorIcon = colorPalette.text,
                        colorText = colorPalette.text,
                        onClick = {
                            onDismiss()
                            onRemoveFromQueue()
                        }
                    )
                }

                onRemoveFromPlaylist?.let { onRemoveFromPlaylist ->
                    GridMenuItem(
                        icon = R.drawable.trash,
                        title = R.string.remove_from_playlist,
                        colorIcon = colorPalette.text,
                        colorText = colorPalette.text,
                        onClick = {
                            onDismiss()
                            onRemoveFromPlaylist()
                        }
                    )
                }

                if (!isLocal) onHideFromDatabase?.let { onHideFromDatabase ->
                    GridMenuItem(
                        icon = R.drawable.update,
                        title = R.string.update,
                        colorIcon = colorPalette.text,
                        colorText = colorPalette.text,
                        onClick = {
                            //onDismiss()
                            onHideFromDatabase()
                        }
                    )
                }

                onDeleteFromDatabase?.let { onDeleteFromDatabase ->
                    GridMenuItem(
                        icon = R.drawable.trash,
                        title = R.string.delete,
                        colorIcon = colorPalette.text,
                        colorText = colorPalette.text,
                        onClick = {
                            //onDismiss()
                            onDeleteFromDatabase()
                        }
                    )
                }

                if (!isLocal) onRemoveFromQuickPicks?.let { onRemoveFromQuickPicks ->
                    GridMenuItem(
                        icon = R.drawable.trash,
                        title = R.string.hide_from_quick_picks,
                        colorIcon = colorPalette.text,
                        colorText = colorPalette.text,
                        onClick = {
                            onDismiss()
                            onRemoveFromQuickPicks()
                        }
                    )
                }


            }

        }
    }
}

