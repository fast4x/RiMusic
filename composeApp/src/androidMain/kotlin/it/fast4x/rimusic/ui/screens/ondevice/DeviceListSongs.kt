package it.fast4x.rimusic.ui.screens.ondevice

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
import coil.compose.AsyncImage
import it.fast4x.innertube.YtMusic
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.cleanPrefix
import it.fast4x.rimusic.enums.DeviceLists
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.enums.OnDeviceFolderSortBy
import it.fast4x.rimusic.enums.OnDeviceSongSortBy
import it.fast4x.rimusic.enums.SortOrder
import it.fast4x.rimusic.enums.ThumbnailRoundness
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.models.Folder
import it.fast4x.rimusic.models.OnDeviceSong
import it.fast4x.rimusic.models.SongEntity
import it.fast4x.rimusic.models.SongPlaylistMap
import it.fast4x.rimusic.service.LOCAL_KEY_PREFIX
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import it.fast4x.rimusic.ui.components.themed.FolderItemMenu
import it.fast4x.rimusic.ui.components.themed.HeaderIconButton
import it.fast4x.rimusic.ui.components.themed.HeaderWithIcon
import it.fast4x.rimusic.ui.components.themed.IconButton
import it.fast4x.rimusic.ui.components.themed.IconInfo
import it.fast4x.rimusic.ui.components.themed.InHistoryMediaItemMenu
import it.fast4x.rimusic.ui.components.themed.NowPlayingSongIndicator
import it.fast4x.rimusic.ui.components.themed.PlaylistsItemMenu
import it.fast4x.rimusic.ui.components.themed.SecondaryTextButton
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.ui.components.themed.SortMenu
import it.fast4x.rimusic.ui.items.FolderItem
import it.fast4x.rimusic.ui.items.PlaylistItem
import it.fast4x.rimusic.ui.items.SongItem
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.favoritesIcon
import it.fast4x.rimusic.ui.styling.px
import it.fast4x.rimusic.utils.OnDeviceBlacklist
import it.fast4x.rimusic.utils.OnDeviceOrganize
import it.fast4x.rimusic.utils.addNext
import it.fast4x.rimusic.utils.asMediaItem
import it.fast4x.rimusic.utils.defaultFolderKey
import it.fast4x.rimusic.utils.disableScrollingTextKey
import it.fast4x.rimusic.utils.durationTextToMillis
import it.fast4x.rimusic.utils.enqueue
import it.fast4x.rimusic.utils.forcePlayAtIndex
import it.fast4x.rimusic.utils.forcePlayFromBeginning
import it.fast4x.rimusic.utils.formatAsTime
import it.fast4x.rimusic.utils.hasPermission
import it.fast4x.rimusic.utils.isAtLeastAndroid10
import it.fast4x.rimusic.utils.isAtLeastAndroid11
import it.fast4x.rimusic.utils.isCompositionLaunched
import it.fast4x.rimusic.utils.isNowPlaying
import it.fast4x.rimusic.utils.onDeviceFolderSortByKey
import it.fast4x.rimusic.utils.onDeviceSongSortByKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.secondary
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.showFoldersOnDeviceKey
import it.fast4x.rimusic.utils.showSearchTabKey
import it.fast4x.rimusic.utils.songSortOrderKey
import it.fast4x.rimusic.utils.thumbnail
import it.fast4x.rimusic.utils.thumbnailRoundnessKey
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.typography
import it.fast4x.rimusic.ui.screens.settings.isYouTubeSyncEnabled
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun DeviceListSongs(
    navController: NavController,
    deviceLists: DeviceLists,
    onSearchClick: () -> Unit
) {
    val context = LocalContext.current
    val permission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
    else Manifest.permission.READ_EXTERNAL_STORAGE

    var relaunchPermission by remember {
        mutableStateOf(false)
    }

    var hasPermission by remember(isCompositionLaunched()) {
        mutableStateOf(context.applicationContext.hasPermission(permission))
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { hasPermission = it }
    )


    if (!hasPermission) {

        LaunchedEffect(Unit, relaunchPermission) { launcher.launch(permission) }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BasicText(
                text = stringResource(R.string.media_permission_required_please_grant),
                modifier = Modifier.fillMaxWidth(0.75f),
                style = typography().xs.semiBold
            )
            /*
            Spacer(modifier = Modifier.height(12.dp))
            SecondaryTextButton(
                text = stringResource(R.string.grant_permission),
                onClick = {
                    relaunchPermission = !relaunchPermission
                }
            )
             */
            Spacer(modifier = Modifier.height(20.dp))
            SecondaryTextButton(
                text = stringResource(R.string.open_permission_settings),
                onClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            setData(Uri.fromParts("package", context.packageName, null))
                        }
                    )
                }
            )

        }

    }
    else {

        val backButtonFolder = Folder(stringResource(R.string.back))
        val binder = LocalPlayerServiceBinder.current
        val menuState = LocalMenuState.current
        val showFolders by rememberPreference(showFoldersOnDeviceKey, true)

        var sortBy by rememberPreference(onDeviceSongSortByKey, OnDeviceSongSortBy.DateAdded)
        var sortByFolder by rememberPreference(onDeviceFolderSortByKey, OnDeviceFolderSortBy.Title)
        var sortOrder by rememberPreference(songSortOrderKey, SortOrder.Descending)

        val defaultFolder by rememberPreference(defaultFolderKey, "/")

        val disableScrollingText by rememberPreference(disableScrollingTextKey, false)

        var songsDevice by remember(sortBy, sortOrder) {
            mutableStateOf<List<OnDeviceSong>>(emptyList())
        }
        LaunchedEffect(sortBy, sortOrder) {
            if (hasPermission)
                context.musicFilesAsFlow(sortBy, sortOrder, context).collect { songsDevice = it.distinctBy { song -> song.id } }
        }

        var songs: List<SongEntity> = emptyList()
        var folders: List<Folder> = emptyList()
        var filteredSongs = songs
        var filteredFolders = folders
        var currentFolder: Folder? = null;
        var currentFolderPath by remember {
            mutableStateOf(defaultFolder)
        }

        if (showFolders) {
            val organized = OnDeviceOrganize.organizeSongsIntoFolders(songsDevice)
            currentFolder = OnDeviceOrganize.getFolderByPath(organized, currentFolderPath)
            songs = OnDeviceOrganize.sortSongs(sortOrder, sortByFolder, currentFolder?.songs?.map { it.toSongEntity() } ?: emptyList())
            filteredSongs = songs
            folders = currentFolder?.subFolders?.toList() ?: emptyList()
            filteredFolders = folders
        }
        else {
            songs = songsDevice.map { it.toSongEntity() }
            filteredSongs = songs
        }

        var filter: String? by rememberSaveable { mutableStateOf(null) }

        var filterCharSequence: CharSequence
        filterCharSequence = filter.toString()
        //Log.d("mediaItemFilter", "<${filter}>  <${filterCharSequence}>")
        if (!filter.isNullOrBlank())
            filteredSongs = songs
                .filter {
                    it.song.title.contains(filterCharSequence,true) ?: false
                            || it.song.artistsText?.contains(filterCharSequence,true) ?: false
                }
        if (!filter.isNullOrBlank())
            filteredFolders = folders
                .filter {
                    it.name.contains(filterCharSequence,true)
                }

        var searching by rememberSaveable { mutableStateOf(false) }

        val thumbnailSizeDp = Dimensions.thumbnails.song
        val thumbnailSize = thumbnailSizeDp.px

        val sortOrderIconRotation by animateFloatAsState(
            targetValue = if (sortOrder == SortOrder.Ascending) 0f else 180f,
            animationSpec = tween(durationMillis = 400, easing = LinearEasing), label = ""
        )

        var thumbnailRoundness by rememberPreference(
            thumbnailRoundnessKey,
            ThumbnailRoundness.Heavy
        )

        val lazyListState = rememberLazyListState()

        var totalPlayTimes = 0L
        filteredSongs.forEach {
            totalPlayTimes += it.song.durationText?.let { it1 ->
                durationTextToMillis(it1)
            }?.toLong() ?: 0
        }

        val playlistThumbnailSizeDp = Dimensions.thumbnails.playlist
        val playlistThumbnailSizePx = playlistThumbnailSizeDp.px

        val thumbnails = songs
            .takeWhile { it.song.thumbnailUrl?.isNotEmpty() ?: false }
            .take(4)
            .map { it.song.thumbnailUrl.thumbnail(playlistThumbnailSizePx / 2) }

        var listMediaItems = remember {
            mutableListOf<MediaItem>()
        }

        var selectItems by remember {
            mutableStateOf(false)
        }

        var position by remember {
            mutableIntStateOf(0)
        }
        var scrollToNowPlaying by remember {
            mutableStateOf(false)
        }

        var nowPlayingItem by remember {
            mutableStateOf(-1)
        }

        val showSearchTab by rememberPreference(showSearchTabKey, false)

        Box(
            modifier = Modifier
                .background(colorPalette().background0)
                //.fillMaxSize()
                .fillMaxHeight()
                .fillMaxWidth(
                    if( NavigationBarPosition.Right.isCurrent() )
                        Dimensions.contentWidthRightBar
                    else
                        1f
                )
        ) {
        LazyColumn(
            state = lazyListState,
            //contentPadding = LocalPlayerAwareWindowInsets.current
            //    .only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
            modifier = Modifier
                .background(colorPalette().background0)
                .fillMaxSize()
        ) {
            item(
                key = "header",
                contentType = 0
            ) {

                HeaderWithIcon(
                    title = stringResource(R.string.on_device),
                    iconId = R.drawable.search,
                    enabled = true,
                    showIcon = !showSearchTab,
                    modifier = Modifier,
                    onClick = onSearchClick
                )

                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        //.background(colorPalette().background4)
                        .fillMaxSize(0.99F)
                        .background(
                            color = colorPalette().background1,
                            shape = thumbnailRoundness.shape()
                        )
                ) {
                    if (filteredSongs.isEmpty())
                        PlaylistItem(
                            icon = R.drawable.musical_notes,
                            colorTint = colorPalette().favoritesIcon,
                            name = stringResource(R.string.on_device),
                            songCount = null,
                            thumbnailSizeDp = playlistThumbnailSizeDp,
                            alternative = false,
                            modifier = Modifier
                                .padding(top = 14.dp),
                            disableScrollingText = disableScrollingText
                        )

                    if (filteredSongs.isNotEmpty())
                        PlaylistItem(
                            thumbnailContent = {
                                if (thumbnails.size == 1) {
                                    AsyncImage(
                                        model = thumbnails.first().thumbnail(playlistThumbnailSizePx),
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
                                                    .size(playlistThumbnailSizeDp / 2)
                                            )
                                        }
                                    }
                                }
                            },
                            songCount = null,
                            name = "",
                            channelName = null,
                            thumbnailSizeDp = playlistThumbnailSizeDp,
                            alternative = true,
                            showName = false,
                            modifier = Modifier
                                .padding(top = 14.dp),
                            disableScrollingText = disableScrollingText
                        )


                    Column (
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            //.fillMaxHeight()
                            .fillMaxWidth(0.7f)
                        //.border(BorderStroke(1.dp, Color.White))
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))
                        IconInfo(
                            title = filteredSongs.size.toString(),
                            icon = painterResource(R.drawable.musical_notes)
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        IconInfo(
                            title = formatAsTime(totalPlayTimes),
                            icon = painterResource(R.drawable.time)
                        )
                        Spacer(modifier = Modifier.height(30.dp))
                    }

                    Column (
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                        //.fillMaxHeight()
                        //.border(BorderStroke(1.dp, Color.White))
                    ) {
                        HeaderIconButton(
                            icon = R.drawable.shuffle,
                            enabled = songs.isNotEmpty(),
                            color = if (songs.isNotEmpty()) colorPalette().text else colorPalette().textDisabled,
                            onClick = {},
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {
                                        if (filteredSongs.isNotEmpty()) {
                                            binder?.stopRadio()
                                            binder?.player?.forcePlayFromBeginning(
                                                songs.shuffled().map(SongEntity::asMediaItem)
                                            )
                                        }
                                    },
                                    onLongClick = {
                                        SmartMessage(context.resources.getString(R.string.info_shuffle), context = context)
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        HeaderIconButton(
                            modifier = Modifier.padding(horizontal = 5.dp),
                            onClick = { searching = !searching },
                            icon = R.drawable.search_circle,
                            color = colorPalette().text,
                            iconSize = 24.dp
                        )
                    }


                }

                Spacer(modifier = Modifier.height(10.dp))

                Row (
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ){

                    HeaderIconButton(
                        icon = R.drawable.arrow_up,
                        color = colorPalette().text,
                        onClick = { sortOrder = !sortOrder },
                        modifier = Modifier
                            .graphicsLayer { rotationZ = sortOrderIconRotation }
                    )

                    if (!showFolders) {
                        BasicText(
                            text = when (sortBy) {
                                OnDeviceSongSortBy.Title -> stringResource(R.string.sort_title)
                                OnDeviceSongSortBy.DateAdded -> stringResource(R.string.sort_date_added)
                                OnDeviceSongSortBy.Artist -> stringResource(R.string.sort_artist)
                                OnDeviceSongSortBy.Duration -> stringResource(R.string.sort_duration)
                                OnDeviceSongSortBy.Album -> stringResource(R.string.sort_album)

                            },
                            style = typography().xs.semiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .clickable {
                                    menuState.display{
                                        SortMenu(
                                            title = stringResource(R.string.sorting_order),
                                            onDismiss = menuState::hide,
                                            onTitle = { sortBy = OnDeviceSongSortBy.Title },
                                            onDateAdded = { sortBy = OnDeviceSongSortBy.DateAdded },
                                            onArtist = { sortBy = OnDeviceSongSortBy.Artist },
                                            onAlbum = { sortBy = OnDeviceSongSortBy.Album },
                                        )
                                    }

                                }
                        )
                    } else {
                        BasicText(
                            text = when (sortByFolder) {
                                OnDeviceFolderSortBy.Title -> stringResource(R.string.sort_title)
                                OnDeviceFolderSortBy.Artist -> stringResource(R.string.sort_artist)
                                OnDeviceFolderSortBy.Duration -> stringResource(R.string.sort_duration)
                            },
                            style = typography().xs.semiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .clickable {
                                    menuState.display{
                                        SortMenu(
                                            title = stringResource(R.string.sorting_order),
                                            onDismiss = menuState::hide,
                                            onTitle = { sortByFolder = OnDeviceFolderSortBy.Title },
                                            onArtist = { sortByFolder = OnDeviceFolderSortBy.Artist },
                                            onDuration = { sortByFolder = OnDeviceFolderSortBy.Duration },
                                        )
                                    }

                                }
                        )
                    }


                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                    )

                    HeaderIconButton(
                        icon = R.drawable.locate,
                        enabled = filteredSongs.isNotEmpty(),
                        color = if (filteredSongs.isNotEmpty()) colorPalette().text else colorPalette().textDisabled,
                        onClick = {},
                        modifier = Modifier
                            .combinedClickable(
                                onClick = {
                                    nowPlayingItem = -1
                                    scrollToNowPlaying = false
                                    filteredSongs
                                        .forEachIndexed{ index, song ->
                                            if (song.asMediaItem.mediaId == binder?.player?.currentMediaItem?.mediaId)
                                                nowPlayingItem = index
                                        }

                                    if (nowPlayingItem > -1)
                                        scrollToNowPlaying = true
                                },
                                onLongClick = {
                                    SmartMessage(context.resources.getString(R.string.info_find_the_song_that_is_playing), context = context)
                                }
                            )
                    )
                    LaunchedEffect(scrollToNowPlaying) {
                        if (scrollToNowPlaying)
                            lazyListState.scrollToItem(nowPlayingItem,1)
                        scrollToNowPlaying = false
                    }
                    /*
                    HeaderIconButton(
                        onClick = { searching = !searching },
                        icon = R.drawable.search_circle,
                        color = colorPalette().text,
                        iconSize = 24.dp
                    )

                     */
                    /*
                    HeaderIconButton(
                        icon = R.drawable.enqueue,
                        enabled = filteredSongs.isNotEmpty(),
                        color = if (filteredSongs.isNotEmpty()) colorPalette().text else colorPalette().textDisabled,
                        onClick = {
                            binder?.player?.enqueue(filteredSongs.map(Song::asMediaItem))
                        }
                    )
                     */
                    /*
                    HeaderIconButton(
                        icon = R.drawable.shuffle,
                        enabled = filteredSongs.isNotEmpty(),
                        color = if (filteredSongs.isNotEmpty()) colorPalette().text else colorPalette().textDisabled,
                        onClick = {},
                        modifier = Modifier
                            .combinedClickable(
                                onClick = {
                                    if (filteredSongs.isNotEmpty()) {
                                        binder?.stopRadio()
                                        binder?.player?.forcePlayFromBeginning(
                                            songs.shuffled().map(Song::asMediaItem)
                                        )
                                    }
                                },
                                onLongClick = {
                                    SmartToast(context.resources.getString(R.string.info_shuffle))
                                }
                            )
                    )
                     */

                    HeaderIconButton(
                        icon = R.drawable.ellipsis_horizontal,
                        color = if (filteredSongs.isNotEmpty() == true) colorPalette().text else colorPalette().textDisabled,
                        enabled = filteredSongs.isNotEmpty() == true,
                        modifier = Modifier
                            .padding(end = 4.dp),
                        onClick = {
                            menuState.display {
                                PlaylistsItemMenu(
                                    navController = navController,
                                    onDismiss = menuState::hide,
                                    onSelectUnselect = {
                                        selectItems = !selectItems
                                        if (!selectItems) {
                                            selectItems = false
                                            listMediaItems.clear()
                                        }
                                    },
                                    /*
                                    onSelect = { selectItems = true },
                                    onUncheck = {
                                        selectItems = false
                                        listMediaItems.clear()
                                    },
                                     */
                                    onPlayNext = {
                                        if (listMediaItems.isEmpty()) {
                                            binder?.player?.addNext(filteredSongs.map(SongEntity::asMediaItem), context)
                                        } else {
                                            binder?.player?.addNext(listMediaItems, context)
                                            listMediaItems.clear()
                                            selectItems = false
                                        }
                                    },
                                    onEnqueue = {
                                        if (listMediaItems.isEmpty()) {
                                            binder?.player?.enqueue(filteredSongs.map(SongEntity::asMediaItem), context)
                                        } else {
                                            binder?.player?.enqueue(listMediaItems, context)
                                            listMediaItems.clear()
                                            selectItems = false
                                        }
                                    },
                                    onAddToPlaylist = { playlistPreview ->
                                        position =
                                            playlistPreview.songCount.minus(1) ?: 0
                                        //Log.d("mediaItem", " maxPos in Playlist $it ${position}")
                                        if (position > 0) position++ else position = 0
                                        //Log.d("mediaItem", "next initial pos ${position}")
                                        if (listMediaItems.isEmpty()) {
                                            filteredSongs.forEachIndexed { index, song ->
                                                Database.asyncTransaction {
                                                    insert(song.asMediaItem)
                                                    insert(
                                                        SongPlaylistMap(
                                                            songId = song.asMediaItem.mediaId,
                                                            playlistId = playlistPreview.playlist.id,
                                                            position = position + index
                                                        ).default()
                                                    )
                                                }
                                            }
                                        } else {
                                            listMediaItems.forEachIndexed { index, song ->
                                                //Log.d("mediaItemMaxPos", position.toString())
                                                Database.asyncTransaction {
                                                    insert(song)
                                                    insert(
                                                        SongPlaylistMap(
                                                            songId = song.mediaId,
                                                            playlistId = playlistPreview.playlist.id,
                                                            position = position + index
                                                        ).default()
                                                    )
                                                }
                                            }
                                            listMediaItems.clear()
                                            selectItems = false
                                        }
                                    },
                                    onGoToPlaylist = {
                                        navController.navigate("${NavRoutes.localPlaylist.name}/$it")
                                    },
                                    disableScrollingText = disableScrollingText
                                )
                            }
                        }
                    )

                }

                Row (
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                        .padding(all = 10.dp)
                        .fillMaxWidth()
                ) {
                    AnimatedVisibility(visible = searching) {
                        val focusRequester = remember { FocusRequester() }
                        val focusManager = LocalFocusManager.current
                        val keyboardController = LocalSoftwareKeyboardController.current

                        LaunchedEffect(searching) {
                            focusRequester.requestFocus()
                        }

                        BasicTextField(
                            value = filter ?: "",
                            onValueChange = { filter = it },
                            textStyle = typography().xs.semiBold,
                            singleLine = true,
                            maxLines = 1,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (filter.isNullOrBlank()) filter = ""
                                focusManager.clearFocus()
                            }),
                            cursorBrush = SolidColor(colorPalette().text),
                            decorationBox = { innerTextField ->
                                Box(
                                    contentAlignment = Alignment.CenterStart,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 10.dp)
                                ) {
                                    IconButton(
                                        onClick = {},
                                        icon = R.drawable.search,
                                        color = colorPalette().favoritesIcon,
                                        modifier = Modifier
                                            .align(Alignment.CenterStart)
                                            .size(16.dp)
                                    )
                                }
                                Box(
                                    contentAlignment = Alignment.CenterStart,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 30.dp)
                                ) {
                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = filter?.isEmpty() ?: true,
                                        enter = fadeIn(tween(100)),
                                        exit = fadeOut(tween(100)),
                                    ) {
                                        BasicText(
                                            text = stringResource(R.string.search),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = typography().xs.semiBold.secondary.copy(color = colorPalette().textDisabled)
                                        )
                                    }

                                    innerTextField()
                                }
                            },
                            modifier = Modifier
                                .height(30.dp)
                                .fillMaxWidth()
                                .background(
                                    colorPalette().background4,
                                    shape = thumbnailRoundness.shape()
                                )
                                .focusRequester(focusRequester)
                                .onFocusChanged {
                                    if (!it.hasFocus) {
                                        keyboardController?.hide()
                                        if (filter?.isBlank() == true) {
                                            filter = null
                                            searching = false
                                        }
                                    }
                                }
                        )
                    }
                }

            }

            if (showFolders) {
                if (currentFolderPath != "/") {
                    itemsIndexed(items = listOf(backButtonFolder)) { index, folderItem ->
                        FolderItem(
                            folder = folderItem,
                            thumbnailSizeDp = thumbnailSizeDp,
                            icon = R.drawable.chevron_back,
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {
                                        currentFolderPath = currentFolderPath.removeSuffix("/").substringBeforeLast("/") + "/"
                                    }
                                ),
                            disableScrollingText = disableScrollingText
                        )
                    }
                }
                if (currentFolder != null) {
                    itemsIndexed(
                        items = filteredFolders,
                        key = { _, folder -> folder.fullPath },
                        contentType = { _, folder -> folder }
                    ) { index, folder ->
                        FolderItem(
                            folder = folder,
                            thumbnailSizeDp = thumbnailSizeDp,
                            modifier = Modifier
                                .combinedClickable(
                                    onLongClick = {
                                        menuState.display {
                                            when (deviceLists) {
                                                DeviceLists.LocalSongs -> FolderItemMenu(
                                                    folder = folder,
                                                    onDismiss = menuState::hide,
                                                    onEnqueue = {
                                                        val allSongs = folder.getAllSongs()
                                                            .map { it.toSong().asMediaItem }
                                                        binder?.player?.enqueue(allSongs, context)
                                                    },
                                                    thumbnailSizeDp = thumbnailSizeDp,
                                                    disableScrollingText = disableScrollingText
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        currentFolderPath += folder.name + "/"
                                    }
                                ),
                            disableScrollingText = disableScrollingText
                        )
                    }
                } else {
                    item {
                        BasicText(
                            text = stringResource(R.string.folder_was_not_found),
                            style = typography().xs.semiBold
                        )
                    }
                }
            }

            itemsIndexed(
                items = filteredSongs,
                key = { index, _ -> Random.nextLong().toString() },
                contentType = { _, song -> song },
            ) { index, song ->

                var forceRecompose by remember { mutableStateOf(false) }
                SongItem(
                    song = song.song,
                    onDownloadClick = {
                        // not necessary
                    },
                    downloadState = Download.STATE_COMPLETED,
                    thumbnailSizeDp = thumbnailSizeDp,
                    thumbnailSizePx = thumbnailSize,
                    onThumbnailContent = {
                            NowPlayingSongIndicator(song.asMediaItem.mediaId, binder?.player)
                    },
                    trailingContent = {
                        val checkedState = rememberSaveable { mutableStateOf(false) }
                        if (selectItems)
                            Checkbox(
                                checked = checkedState.value,
                                onCheckedChange = {
                                    checkedState.value = it
                                    if (it) listMediaItems.add(song.asMediaItem) else
                                        listMediaItems.remove(song.asMediaItem)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = colorPalette().accent,
                                    uncheckedColor = colorPalette().text
                                ),
                                modifier = Modifier
                                    .scale(0.7f)

                            )
                        else checkedState.value = false
                    },
                    modifier = Modifier
                        .combinedClickable(
                            onLongClick = {
                                menuState.display {
                                    when (deviceLists) {
                                        DeviceLists.LocalSongs -> InHistoryMediaItemMenu(
                                            navController = navController,
                                            song = song.song,
                                            onDismiss = {
                                                menuState.hide()
                                                forceRecompose = true
                                            },
                                            disableScrollingText = disableScrollingText
                                        )
                                    }
                                }
                            },
                            onClick = {
                                if (!selectItems) {
                                    searching = false
                                    filter = null
                                    binder?.stopRadio()
                                    binder?.player?.forcePlayAtIndex(
                                        filteredSongs.map(SongEntity::asMediaItem),
                                        index
                                    )
                                }
                            }
                        )
                        .animateItem(),
                    disableScrollingText = disableScrollingText,
                    isNowPlaying = binder?.player?.isNowPlaying(song.song.id) ?: false,
                    forceRecompose = forceRecompose
                )
            }
        }

            if( UiType.ViMusic.isCurrent() )
            FloatingActionsContainerWithScrollToTop(
                lazyListState = lazyListState,
                iconId = R.drawable.shuffle,
                onClick = {
                    if (filteredSongs.isNotEmpty()) {
                        binder?.stopRadio()
                        binder?.player?.forcePlayFromBeginning(
                            filteredSongs.shuffled().map(SongEntity::asMediaItem)
                        )
                    }
                }
            )
        }

    }




}

private val mediaScope = CoroutineScope(Dispatchers.IO + CoroutineName("MediaStore worker"))
fun Context.musicFilesAsFlow(sortBy: OnDeviceSongSortBy, order: SortOrder, context: Context): StateFlow<List<OnDeviceSong>> = flow {
    var version: String? = null

    while (currentCoroutineContext().isActive) {
        val newVersion = MediaStore.getVersion(applicationContext)
        if (version != newVersion) {
            version = newVersion
            val collection =
                if (isAtLeastAndroid10) MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

            var projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
                if (isAtLeastAndroid10) {
                    MediaStore.Audio.Media.RELATIVE_PATH
                } else {
                    MediaStore.Audio.Media.DATA
                },
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.IS_MUSIC,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.DATE_MODIFIED
            )

            if (isAtLeastAndroid11)
                projection += MediaStore.Audio.Media.BITRATE

            projection += MediaStore.Audio.Media.SIZE

            val sortOrderSQL = when (order) {
                SortOrder.Ascending -> "ASC"
                SortOrder.Descending -> "DESC"
            }

            val sortBySQL = when (sortBy) {
                OnDeviceSongSortBy.Title -> "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE $sortOrderSQL"
                OnDeviceSongSortBy.DateAdded -> "${MediaStore.Audio.Media.DATE_ADDED} $sortOrderSQL"
                OnDeviceSongSortBy.Artist -> "${MediaStore.Audio.Media.ARTIST} COLLATE NOCASE $sortOrderSQL"
                OnDeviceSongSortBy.Duration -> "${MediaStore.Audio.Media.DURATION} COLLATE NOCASE $sortOrderSQL"
                OnDeviceSongSortBy.Album -> "${MediaStore.Audio.Media.ALBUM} COLLATE NOCASE $sortOrderSQL"
            }

            val albumUriBase = Uri.parse("content://media/external/audio/albumart")

            contentResolver.query(collection, projection, null, null, sortBySQL)
                ?.use { cursor ->
                    val idIdx = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                    Timber.i(" DeviceListSongs colums idIdx $idIdx")
                    val nameIdx = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                    Timber.i(" DeviceListSongs colums nameIdx $nameIdx")
                    val durationIdx = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                    Timber.i(" DeviceListSongs colums durationIdx $durationIdx")
                    val artistIdx = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                    Timber.i(" DeviceListSongs colums artistIdx $artistIdx")
                    val albumIdIdx = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                    Timber.i(" DeviceListSongs colums albumIdIdx $albumIdIdx")
                    val relativePathIdx = if (isAtLeastAndroid10) {
                        cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH)
                    } else {
                        cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                    }
                    Timber.i(" DeviceListSongs colums relativePathIdx $relativePathIdx")
                    val titleIdx = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                    Timber.i(" DeviceListSongs colums titleIdx $titleIdx")
                    val isMusicIdx = cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)
                    Timber.i(" DeviceListSongs colums isMusicIdx $isMusicIdx")
                    val mimeTypeIdx = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
                    Timber.i(" DeviceListSongs colums mimeTypeIdx $mimeTypeIdx")
                    val bitrateIdx = if (isAtLeastAndroid11) cursor.getColumnIndex(MediaStore.Audio.Media.BITRATE) else -1
                    Timber.i(" DeviceListSongs colums bitrateIdx $bitrateIdx")
                    val fileSizeIdx = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
                    Timber.i(" DeviceListSongs colums fileSizeIdx $fileSizeIdx")
                    val dateModifiedIdx = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)
                    Timber.i(" DeviceListSongs colums dateModifiedIdx $dateModifiedIdx")


                    val blacklist = OnDeviceBlacklist(context = context)

                    Timber.i(" DeviceListSongs SDK ${Build.VERSION.SDK_INT} initialize columns complete")

                    buildList {
                        while (cursor.moveToNext()) {
                            if (cursor.getInt(isMusicIdx) == 0) continue
                            val id = cursor.getLong(idIdx)
                            val name = cursor.getString(nameIdx).substringBeforeLast(".")
                            val trackName = cursor.getString(titleIdx)
                            Timber.i(" DeviceListSongs trackName $trackName loaded")
                            val duration = cursor.getInt(durationIdx)
                            if (duration == 0) continue
                            val artist = cursor.getString(artistIdx)
                            val albumId = cursor.getLong(albumIdIdx)

                            val mimeType = cursor.getString(mimeTypeIdx)
                            val bitrate = if (isAtLeastAndroid11) cursor.getInt(bitrateIdx) else 0
                            val fileSize = cursor.getInt(fileSizeIdx)
                            val dateModified = cursor.getLong(dateModifiedIdx)

                            val relativePath = if (isAtLeastAndroid10) {
                                cursor.getString(relativePathIdx)
                            } else {
                                cursor.getString(relativePathIdx).substringBeforeLast("/")
                            }
                            val exclude = blacklist.contains(relativePath)

                            if (!exclude) {
                                runCatching {
                                    val albumUri = ContentUris.withAppendedId(albumUriBase, albumId)
                                    val durationText =
                                        duration.milliseconds.toComponents { minutes, seconds, _ ->
                                            "$minutes:${seconds.toString().padStart(2, '0')}"
                                        }
                                    val song = OnDeviceSong(
                                        id = "$LOCAL_KEY_PREFIX$id",
                                        title = trackName ?: name,
                                        artistsText = artist,
                                        durationText = durationText,
                                        thumbnailUrl = albumUri.toString(),
                                        relativePath = relativePath
                                    )
                                    Database.insert(
                                        song.toSong()
                                    )
                                    
                                    Database.insert(
                                        it.fast4x.rimusic.models.Format(
                                            songId = song.id,
                                            itag = 0,
                                            mimeType = mimeType,
                                            bitrate = bitrate.toLong(),
                                            contentLength = fileSize.toLong(),
                                            lastModified = dateModified
                                        )
                                    )

                                    add(
                                        song
                                    )
                                }.onFailure {
                                    Timber.e("DeviceListSongs addSong error ${it.stackTraceToString()}")
                                }
                            }
                        }
                    }
                }?.let {
                    runCatching {
                        emit(it)
                    }.onFailure {
                        Timber.e("DeviceListSongs emit error ${it.stackTraceToString()}")
                    }
                }
        }
        runCatching {
            delay(5.seconds)
        }
    }
}.distinctUntilChanged()
    .stateIn(mediaScope, SharingStarted.Eagerly, listOf())
