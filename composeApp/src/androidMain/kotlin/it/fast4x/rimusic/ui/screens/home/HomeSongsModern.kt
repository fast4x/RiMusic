package it.fast4x.rimusic.ui.screens.home


import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import it.fast4x.compose.persist.persistList
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.EXPLICIT_PREFIX
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.appContext
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.enums.BuiltInPlaylist
import it.fast4x.rimusic.enums.CacheType
import it.fast4x.rimusic.enums.DurationInMinutes
import it.fast4x.rimusic.enums.MaxSongs
import it.fast4x.rimusic.enums.MaxTopPlaylistItems
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.enums.OnDeviceFolderSortBy
import it.fast4x.rimusic.enums.OnDeviceSongSortBy
import it.fast4x.rimusic.enums.PopupType
import it.fast4x.rimusic.enums.QueueSelection
import it.fast4x.rimusic.enums.SongSortBy
import it.fast4x.rimusic.enums.SortOrder
import it.fast4x.rimusic.enums.ThumbnailRoundness
import it.fast4x.rimusic.enums.TopPlaylistPeriod
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.models.Folder
import it.fast4x.rimusic.models.OnDeviceSong
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.models.SongEntity
import it.fast4x.rimusic.models.SongPlaylistMap
import it.fast4x.rimusic.service.LOCAL_KEY_PREFIX
import it.fast4x.rimusic.service.MyDownloadHelper
import it.fast4x.rimusic.service.modern.isLocal
import it.fast4x.rimusic.thumbnailShape
import it.fast4x.rimusic.typography
import it.fast4x.rimusic.ui.components.ButtonsRow
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.SwipeablePlaylistItem
import it.fast4x.rimusic.ui.components.themed.ConfirmationDialog
import it.fast4x.rimusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import it.fast4x.rimusic.ui.components.themed.FolderItemMenu
import it.fast4x.rimusic.ui.components.themed.HeaderIconButton
import it.fast4x.rimusic.ui.components.themed.HeaderInfo
import it.fast4x.rimusic.ui.components.themed.HeaderWithIcon
import it.fast4x.rimusic.ui.components.themed.IconButton
import it.fast4x.rimusic.ui.components.themed.InHistoryMediaItemMenu
import it.fast4x.rimusic.ui.components.themed.InputTextDialog
import it.fast4x.rimusic.ui.components.themed.MultiFloatingActionsContainer
import it.fast4x.rimusic.ui.components.themed.NowPlayingSongIndicator
import it.fast4x.rimusic.ui.components.themed.PeriodMenu
import it.fast4x.rimusic.ui.components.themed.PlaylistsItemMenu
import it.fast4x.rimusic.ui.components.themed.SecondaryTextButton
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.ui.components.themed.SortMenu
import it.fast4x.rimusic.ui.components.themed.TitleSection
import it.fast4x.rimusic.ui.items.FolderItem
import it.fast4x.rimusic.ui.items.SongItem
import it.fast4x.rimusic.ui.screens.ondevice.musicFilesAsFlow
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.favoritesIcon
import it.fast4x.rimusic.ui.styling.onOverlay
import it.fast4x.rimusic.ui.styling.overlay
import it.fast4x.rimusic.ui.styling.px
import it.fast4x.rimusic.utils.MaxTopPlaylistItemsKey
import it.fast4x.rimusic.utils.OnDeviceOrganize
import it.fast4x.rimusic.utils.addNext
import it.fast4x.rimusic.utils.asMediaItem
import it.fast4x.rimusic.utils.autoShuffleKey
import it.fast4x.rimusic.utils.builtInPlaylistKey
import it.fast4x.rimusic.utils.center
import it.fast4x.rimusic.utils.color
import it.fast4x.rimusic.utils.defaultFolderKey
import it.fast4x.rimusic.utils.disableScrollingTextKey
import it.fast4x.rimusic.utils.durationTextToMillis
import it.fast4x.rimusic.utils.enqueue
import it.fast4x.rimusic.utils.excludeSongsWithDurationLimitKey
import it.fast4x.rimusic.utils.forcePlayAtIndex
import it.fast4x.rimusic.utils.forcePlayFromBeginning
import it.fast4x.rimusic.utils.getDownloadState
import it.fast4x.rimusic.utils.hasPermission
import it.fast4x.rimusic.utils.includeLocalSongsKey
import it.fast4x.rimusic.utils.isCompositionLaunched
import it.fast4x.rimusic.utils.manageDownload
import it.fast4x.rimusic.utils.maxSongsInQueueKey
import it.fast4x.rimusic.utils.onDeviceFolderSortByKey
import it.fast4x.rimusic.utils.onDeviceSongSortByKey
import it.fast4x.rimusic.utils.parentalControlEnabledKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.secondary
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.showCachedPlaylistKey
import it.fast4x.rimusic.utils.showDownloadedPlaylistKey
import it.fast4x.rimusic.utils.showFavoritesPlaylistKey
import it.fast4x.rimusic.utils.showFloatingIconKey
import it.fast4x.rimusic.utils.showFoldersOnDeviceKey
import it.fast4x.rimusic.utils.showMyTopPlaylistKey
import it.fast4x.rimusic.utils.showOnDevicePlaylistKey
import it.fast4x.rimusic.utils.showSearchTabKey
import it.fast4x.rimusic.utils.songSortByKey
import it.fast4x.rimusic.utils.songSortOrderKey
import it.fast4x.rimusic.utils.thumbnailRoundnessKey
import it.fast4x.rimusic.utils.topPlaylistPeriodKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration
import it.fast4x.rimusic.ui.components.themed.CacheSpaceIndicator
import it.fast4x.rimusic.ui.components.themed.InProgressDialog
import it.fast4x.rimusic.ui.screens.settings.isYouTubeSyncEnabled
import it.fast4x.rimusic.utils.addToYtLikedSongs
import it.fast4x.rimusic.utils.addToYtPlaylist
import it.fast4x.rimusic.utils.asSong
import it.fast4x.rimusic.utils.formatAsDuration
import it.fast4x.rimusic.utils.isDownloadedSong
import it.fast4x.rimusic.utils.isNetworkConnected
import it.fast4x.rimusic.utils.isNowPlaying
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess


@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun HomeSongsModern(
    navController: NavController,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val thumbnailSizeDp = Dimensions.thumbnails.song
    val thumbnailSizePx = thumbnailSizeDp.px

    var sortBy by rememberPreference(songSortByKey, SongSortBy.DateAdded)
    var sortOrder by rememberPreference(songSortOrderKey, SortOrder.Descending)
    val parentalControlEnabled by rememberPreference(parentalControlEnabledKey, false)

    var items by persistList<SongEntity>("home/songs")
    var itemsAll by persistList<SongEntity>("")

    //var songsWithAlbum by persistList<SongWithAlbum>("home/songsWithAlbum")

    /*
    var filterDownloaded by remember {
        mutableStateOf(false)
    }
     */

    var filter: String? by rememberSaveable { mutableStateOf(null) }
    var builtInPlaylist by rememberPreference(
        builtInPlaylistKey,
        BuiltInPlaylist.Favorites
    )

    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }

    val context = LocalContext.current

    var thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )

    var showHiddenSongs by remember {
        mutableStateOf(0)
    }

    LaunchedEffect(Unit) {
        Database.listAllSongsAsFlow().collect { itemsAll = it }
    }

    var includeLocalSongs by rememberPreference(includeLocalSongsKey, true)
    var autoShuffle by rememberPreference(autoShuffleKey, false)

    val maxTopPlaylistItems by rememberPreference(
        MaxTopPlaylistItemsKey,
        MaxTopPlaylistItems.`10`
    )
    var topPlaylistPeriod by rememberPreference(topPlaylistPeriodKey, TopPlaylistPeriod.PastWeek)

    var scrollToNowPlaying by remember {
        mutableStateOf(false)
    }

    var nowPlayingItem by remember {
        mutableStateOf(-1)
    }


    /************ OnDeviceDev */
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
    val backButtonFolder = Folder(stringResource(R.string.back))
    val showFolders by rememberPreference(showFoldersOnDeviceKey, true)

    var sortByOnDevice by rememberPreference(onDeviceSongSortByKey, OnDeviceSongSortBy.DateAdded)
    var sortByFolderOnDevice by rememberPreference(onDeviceFolderSortByKey, OnDeviceFolderSortBy.Title)
    var sortOrderOnDevice by rememberPreference(songSortOrderKey, SortOrder.Descending)

    val defaultFolder by rememberPreference(defaultFolderKey, "/")

    var songsDevice by remember(sortBy, sortOrder) {
        mutableStateOf<List<OnDeviceSong>>(emptyList())
    }
    var songs: List<SongEntity> = emptyList()
    var folders: List<Folder> = emptyList()

    var filteredSongs = songs

    var filteredFolders = folders
    var currentFolder: Folder? = null;
    var currentFolderPath by remember {
        mutableStateOf(defaultFolder)
    }

    var deleteProgressDialog by remember {
        mutableStateOf(false)
    }

    var checkCheck by remember {
        mutableStateOf(false)
    }

    var totalSongsToDelete by remember { mutableIntStateOf(0) }

    var songsDeleted by remember { mutableIntStateOf(0) }

    val importLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            //requestPermission(activity, "Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED")

            context.applicationContext.contentResolver.openInputStream(uri)
                ?.use { inputStream ->
                    csvReader().open(inputStream) {
                        readAllWithHeaderAsSequence().forEachIndexed { index, row: Map<String, String> ->
                            println("mediaItem index song ${index}")
                            Database.asyncTransaction {
                                /**/
                                if (row["MediaId"] != null && row["Title"] != null) {
                                    val song =
                                        row["MediaId"]?.let {
                                            row["Title"]?.let { it1 ->
                                                Song(
                                                    id = it,
                                                    title = it1,
                                                    artistsText = row["Artists"],
                                                    durationText = row["Duration"],
                                                    thumbnailUrl = row["ThumbnailUrl"],
                                                    totalPlayTimeMs = 1L
                                                )
                                            }
                                        }

                                        if (song != null) {
                                            Database.upsert(song)
                                            Database.like(
                                                song.id,
                                                System.currentTimeMillis()
                                            )
                                        }



                                }
                                /**/

                            }

                        }
                    }
                }
        }

    /************ */

    val showFavoritesPlaylist by rememberPreference(showFavoritesPlaylistKey, true)
    val showCachedPlaylist by rememberPreference(showCachedPlaylistKey, true)
    val showMyTopPlaylist by rememberPreference(showMyTopPlaylistKey, true)
    val showDownloadedPlaylist by rememberPreference(showDownloadedPlaylistKey, true)
    val showOnDevicePlaylist by rememberPreference(showOnDevicePlaylistKey, true)

    var buttonsList = listOf(BuiltInPlaylist.All to stringResource(R.string.all))
    if (showFavoritesPlaylist) buttonsList +=
        BuiltInPlaylist.Favorites to stringResource(R.string.favorites)
    if (showCachedPlaylist) buttonsList +=
        BuiltInPlaylist.Offline to stringResource(R.string.cached)
    if (showDownloadedPlaylist) buttonsList +=
        BuiltInPlaylist.Downloaded to stringResource(R.string.downloaded)
    if (showMyTopPlaylist) buttonsList +=
        BuiltInPlaylist.Top to String.format(stringResource(R.string.my_playlist_top),maxTopPlaylistItems.number)
    if (showOnDevicePlaylist) buttonsList +=
        BuiltInPlaylist.OnDevice to stringResource(R.string.on_device)

    val excludeSongWithDurationLimit by rememberPreference(excludeSongsWithDurationLimitKey, DurationInMinutes.Disabled)
    val hapticFeedback = LocalHapticFeedback.current

    val disableScrollingText by rememberPreference(disableScrollingTextKey, false)

    when (builtInPlaylist) {
        BuiltInPlaylist.All -> {
            LaunchedEffect(sortBy, sortOrder, filter, showHiddenSongs, includeLocalSongs) {
                //Database.songs(sortBy, sortOrder, showHiddenSongs).collect { items = it }
                Database.songs(sortBy, sortOrder, showHiddenSongs).collect { items = it }

            }
        }
        BuiltInPlaylist.Downloaded, BuiltInPlaylist.Favorites, BuiltInPlaylist.Offline, BuiltInPlaylist.Top -> {

            LaunchedEffect(Unit, builtInPlaylist, sortBy, sortOrder, filter, topPlaylistPeriod) {

                if (builtInPlaylist == BuiltInPlaylist.Downloaded) {
                    /*
                    val downloads = DownloadUtil.downloads.value
                    Database.listAllSongsAsFlow()
                        .combine(
                            Database
                                .songsOffline(sortBy, sortOrder)
                        ){ a, b ->
                            a.filter { song ->
                                downloads[song.song.id]?.state == Download.STATE_COMPLETED
                            }.union(
                                b.filter { binder?.isCached(it) ?: false } //.map { it.song }
                            )
                        }
                        .collect {
                            items = it.toList()
                        }

                     */


                    val downloads = MyDownloadHelper.downloads.value
                    Database.listAllSongsAsFlow()
                        .flowOn(Dispatchers.IO)
                        .map {
                            it.filter { song ->
                                binder?.downloadCache?.keys?.contains(song.song.id) == true
                                        && downloads[song.song.id]?.state == Download.STATE_COMPLETED
                            }
//                            .filter { song ->
//                                downloads[song.song.id]?.state == Download.STATE_COMPLETED
//                            }
                        }
                        .collect {
                            items = it
                        }
                }

                if (builtInPlaylist == BuiltInPlaylist.Favorites) {
                    Database.songsFavorites(sortBy, sortOrder)
                        .collect {
                            items =
                                if (autoShuffle)
                                    it.shuffled()
                                else it
                        }
                }


                if (builtInPlaylist == BuiltInPlaylist.Offline) {
                    Database
                        .songsOffline(sortBy, sortOrder)
                        .flowOn(Dispatchers.IO)
                        .map { songs ->
                            songs.filter { song ->
                                song.contentLength?.let {
                                    binder?.cache?.isCached(song.song.id, 0, song.contentLength)
                                } ?: false
                            }
                        }
                        .collect {
                            items = it
                        }
                }

                if (builtInPlaylist == BuiltInPlaylist.Top) {

                    if (topPlaylistPeriod.duration == Duration.INFINITE) {
                        Database
                            .songsEntityByPlayTimeWithLimitDesc(limit = maxTopPlaylistItems.number.toInt())
                            .collect {
                                items = it.filter { item ->
                                    if (excludeSongWithDurationLimit == DurationInMinutes.Disabled)
                                        true
                                    else
                                        item.song.durationText?.let { it1 ->
                                            durationTextToMillis(it1)
                                        }!! < excludeSongWithDurationLimit.minutesInMilliSeconds
                                }
                            }
                    } else {
                        Database
                            .trendingSongEntity(
                                limit = maxTopPlaylistItems.number.toInt(),
                                period = topPlaylistPeriod.duration.inWholeMilliseconds
                            )
                            .collect {
                                items = it.filter { item ->
                                    if (excludeSongWithDurationLimit == DurationInMinutes.Disabled)
                                        true
                                    else
                                        item.song.durationText?.let { it1 ->
                                            durationTextToMillis(it1)
                                        }!! < excludeSongWithDurationLimit.minutesInMilliSeconds
                                }
                            }
                    }
                }

            }
        }
        BuiltInPlaylist.OnDevice -> {
            items = emptyList()
            LaunchedEffect(sortByOnDevice, sortOrderOnDevice) {
                if (hasPermission)
                    context.musicFilesAsFlow(sortByOnDevice, sortOrderOnDevice, context)
                        .collect { songsDevice = it.distinctBy { song -> song.id } }
            }
        }
    }

    //println("mediaItem SongEntity: ${items.size} filter ${filter} $items")

    /********** OnDeviceDev */
    if (builtInPlaylist == BuiltInPlaylist.OnDevice) {
        if (showFolders) {
            val organized = OnDeviceOrganize.organizeSongsIntoFolders(songsDevice)
            currentFolder = OnDeviceOrganize.getFolderByPath(organized, currentFolderPath)
            songs = OnDeviceOrganize.sortSongs(
                sortOrder,
                sortByFolderOnDevice,
                currentFolder?.songs?.map { it.toSongEntity() } ?: emptyList())
            filteredSongs = songs
            folders = currentFolder?.subFolders?.toList() ?: emptyList()
            filteredFolders = folders
        } else {
            songs = songsDevice.map { it.toSongEntity() }
            filteredSongs = songs
        }
    }
    /********** */

    if (!includeLocalSongs && builtInPlaylist == BuiltInPlaylist.All)
        items = items
            .filter {
                !it.song.id.startsWith(LOCAL_KEY_PREFIX)
            }

    if (builtInPlaylist == BuiltInPlaylist.Downloaded) {
        when (sortOrder) {
            SortOrder.Ascending -> {
                when (sortBy) {
                    SongSortBy.Title, SongSortBy.AlbumName -> items = items.sortedBy { it.song.title }
                    SongSortBy.PlayTime -> items = items.sortedBy { it.song.totalPlayTimeMs }
                    SongSortBy.RelativePlayTime -> items = items.sortedBy { it.relativePlayTime() }
                    SongSortBy.Duration -> items = items.sortedBy { it.song.durationText }
                    SongSortBy.Artist -> items = items.sortedBy { it.song.artistsText }
                    SongSortBy.DatePlayed -> {}
                    SongSortBy.DateLiked -> items = items.sortedBy { it.song.likedAt }
                    SongSortBy.DateAdded -> {}
                    SongSortBy.AlbumName -> items = items.sortedBy { it.albumTitle }
                }
            }
            SortOrder.Descending -> {
                when (sortBy) {
                    SongSortBy.Title, SongSortBy.AlbumName -> items = items.sortedByDescending { it.song.title }
                    SongSortBy.PlayTime -> items = items.sortedByDescending { it.song.totalPlayTimeMs }
                    SongSortBy.RelativePlayTime -> items = items.sortedByDescending { it.relativePlayTime() }
                    SongSortBy.Duration -> items = items.sortedByDescending { it.song.durationText }
                    SongSortBy.Artist -> items = items.sortedByDescending { it.song.artistsText }
                    SongSortBy.DatePlayed -> {}
                    SongSortBy.DateLiked -> items = items.sortedByDescending { it.song.likedAt }
                    SongSortBy.DateAdded -> {}
                    SongSortBy.AlbumName -> items = items.sortedByDescending { it.albumTitle }
                }
            }
        }

    }

    var filterCharSequence: CharSequence
    filterCharSequence = filter.toString()
    /******** OnDeviceDev */
    if (builtInPlaylist == BuiltInPlaylist.OnDevice) {
        if (!filter.isNullOrBlank())
            filteredSongs = songs
                .filter {
                    it.song.title.contains(filterCharSequence,true) ?: false
                            || it.song.artistsText?.contains(filterCharSequence,true) ?: false
                            || it.albumTitle?.contains(filterCharSequence,true) ?: false
                }
        if (!filter.isNullOrBlank())
            filteredFolders = folders
                .filter {
                    it.name.contains(filterCharSequence,true)
                }
    } else {
        if (!filter.isNullOrBlank())
            items = items
                .filter {
                    it.song.title.contains(filterCharSequence,true) ?: false
                            || it.song.artistsText?.contains(filterCharSequence,true) ?: false
                            || it.albumTitle?.contains(filterCharSequence,true) ?: false
                }
    }
    /******** */

    var searching by rememberSaveable { mutableStateOf(false) }

    val sortOrderIconRotation by animateFloatAsState(
        targetValue = if (sortOrder == SortOrder.Ascending) 0f else 180f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing), label = ""
    )

    val lazyListState = rememberLazyListState()

    val showSearchTab by rememberPreference(showSearchTabKey, false)
    val maxSongsInQueue  by rememberPreference(maxSongsInQueueKey, MaxSongs.`500`)

    var listMediaItems = remember {
        mutableListOf<MediaItem>()
    }

    var selectItems by remember {
        mutableStateOf(false)
    }

    var position by remember {
        mutableIntStateOf(0)
    }

    var plistName by remember {
        mutableStateOf("")
    }

    val coroutineScope = rememberCoroutineScope()

    val exportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            coroutineScope.launch (Dispatchers.IO){
                context.applicationContext.contentResolver.openOutputStream(uri)
                    ?.use { outputStream ->
                        csvWriter().open(outputStream) {
                            writeRow(
                                "PlaylistBrowseId",
                                "PlaylistName",
                                "MediaId",
                                "Title",
                                "Artists",
                                "Duration",
                                "ThumbnailUrl",
                                "AlbumId",
                                "AlbumTitle",
                                "ArtistIds"
                            )
                            if (listMediaItems.isEmpty()) {
                                items.forEach {
                                    val artistInfos = Database.songArtistInfo(it.asMediaItem.mediaId)
                                    val albumInfo = Database.songAlbumInfo(it.asMediaItem.mediaId)
                                    writeRow(
                                        "",
                                        plistName,
                                        it.song.id,
                                        it.song.title,
                                        artistInfos.joinToString(",") { it.name ?: "" },
                                        it.song.durationText,
                                        it.song.thumbnailUrl,
                                        albumInfo?.id,
                                        albumInfo?.name,
                                        artistInfos.joinToString(",") { it.id }
                                    )
                                }
                            } else {
                                listMediaItems.forEach {
                                    val artistInfos = Database.songArtistInfo(it.mediaId)
                                    val albumInfo = Database.songAlbumInfo(it.mediaId)
                                    writeRow(
                                        "",
                                        plistName,
                                        it.mediaId,
                                        it.mediaMetadata.title,
                                        artistInfos.joinToString(",") { it.name ?: "" },
                                        it.asSong.durationText,
                                        it.mediaMetadata.artworkUri,
                                        albumInfo?.id,
                                        albumInfo?.name,
                                        artistInfos.joinToString(",") { it.id }
                                    )
                                }
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
            value = when (builtInPlaylist) {
                BuiltInPlaylist.All -> context.resources.getString(R.string.songs)
                BuiltInPlaylist.OnDevice -> context.resources.getString(R.string.on_device)
                BuiltInPlaylist.Favorites -> context.resources.getString(R.string.favorites)
                BuiltInPlaylist.Downloaded -> context.resources.getString(R.string.downloaded)
                BuiltInPlaylist.Offline -> context.resources.getString(R.string.cached)
                BuiltInPlaylist.Top -> context.resources.getString(R.string.playlist_top)
            },
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
                    SmartMessage("Couldn't find an application to create documents",
                        type = PopupType.Warning, context = context)
                }
            }
        )
    }

    var showConfirmDeleteDownloadDialog by remember {
        mutableStateOf(false)
    }
    var showConfirmDownloadAllDialog by remember {
        mutableStateOf(false)
    }

    var showRiMusicLikeYoutubeLikeConfirmDialog by remember {
        mutableStateOf(false)
    }

    var showYoutubeLikeConfirmDialog by remember {
        mutableStateOf(false)
    }

    var totalMinutesToLike by remember { mutableStateOf("") }

    val queueLimit by remember { mutableStateOf(QueueSelection.END_OF_QUEUE_WINDOWED) }
    var songItemsToLike = remember { mutableStateListOf<MediaItem>() }

    Box(
        modifier = Modifier
            .background(colorPalette().background0)
            //.fillMaxSize()
            .fillMaxHeight()
            //.fillMaxWidth(if (navigationBarPosition == NavigationBarPosition.Left) 1f else Dimensions.contentWidthRightBar)
            .fillMaxWidth(
                if( NavigationBarPosition.Right.isCurrent() )
                    Dimensions.contentWidthRightBar
                else
                    1f
            )
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier

        ) {

            stickyHeader {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorPalette().background0)
                ) {
                    if ( UiType.ViMusic.isCurrent() )
                        HeaderWithIcon(
                            title = stringResource(R.string.songs),
                            iconId = R.drawable.search,
                            enabled = true,
                            showIcon = !showSearchTab,
                            modifier = Modifier,
                            onClick = onSearchClick
                        )

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .padding(vertical = 4.dp)
                            .fillMaxWidth()
                    ) {
                        if ( UiType.RiMusic.isCurrent() )
                            TitleSection(title = stringResource(R.string.songs))

                        HeaderInfo(
                            title = if (builtInPlaylist == BuiltInPlaylist.OnDevice) "${filteredSongs.size}" else "${items.size}",
                            iconId = R.drawable.musical_notes
                        )
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .padding(vertical = 4.dp)
                            .fillMaxWidth()
                    ) {
                        Column {
                            ButtonsRow(
                                chips = buttonsList,
                                currentValue = builtInPlaylist,
                                onValueUpdate = {
                                    builtInPlaylist = it
                                },
                                modifier = Modifier.padding(end = 12.dp)
                            )

                            when (builtInPlaylist) {
                                BuiltInPlaylist.Downloaded, BuiltInPlaylist.Offline -> {
                                    CacheSpaceIndicator(
                                        cacheType = when (builtInPlaylist) {
                                            BuiltInPlaylist.Downloaded -> CacheType.DownloadedSongs
                                            BuiltInPlaylist.Offline -> CacheType.CachedSongs
                                            else -> CacheType.CachedSongs
                                        }
                                    )
                                }
                                else -> {}
                            }

                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .padding(vertical = 4.dp)
                            .fillMaxWidth()
                    ) {

                        if (builtInPlaylist != BuiltInPlaylist.Top) {
                            HeaderIconButton(
                                icon = R.drawable.arrow_up,
                                color = colorPalette().text,
                                onClick = {},
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .graphicsLayer {
                                        rotationZ =
                                            sortOrderIconRotation
                                    }
                                    .combinedClickable(
                                        onClick = {
                                            if (builtInPlaylist != BuiltInPlaylist.OnDevice)
                                                sortOrder = !sortOrder
                                            else sortOrderOnDevice = !sortOrderOnDevice
                                        },
                                        onLongClick = {
                                            menuState.display {
                                                when (builtInPlaylist) {
                                                    BuiltInPlaylist.OnDevice -> {
                                                        if (!showFolders)
                                                            SortMenu(
                                                                title = stringResource(R.string.sorting_order),
                                                                onDismiss = menuState::hide,
                                                                onTitle = {
                                                                    sortByOnDevice =
                                                                        OnDeviceSongSortBy.Title
                                                                },
                                                                onDateAdded = {
                                                                    sortByOnDevice =
                                                                        OnDeviceSongSortBy.DateAdded
                                                                },
                                                                onArtist = {
                                                                    sortByOnDevice =
                                                                        OnDeviceSongSortBy.Artist
                                                                },
                                                                onAlbum = {
                                                                    sortByOnDevice =
                                                                        OnDeviceSongSortBy.Album
                                                                },
                                                            )
                                                        else
                                                            SortMenu(
                                                                title = stringResource(R.string.sorting_order),
                                                                onDismiss = menuState::hide,
                                                                onTitle = {
                                                                    sortByFolderOnDevice =
                                                                        OnDeviceFolderSortBy.Title
                                                                },
                                                                onArtist = {
                                                                    sortByFolderOnDevice =
                                                                        OnDeviceFolderSortBy.Artist
                                                                },
                                                                onDuration = {
                                                                    sortByFolderOnDevice =
                                                                        OnDeviceFolderSortBy.Duration
                                                                },
                                                            )
                                                    }

                                                    else -> {
                                                        SortMenu(
                                                            title = stringResource(R.string.sorting_order),
                                                            onDismiss = menuState::hide,
                                                            onTitle = { sortBy = SongSortBy.Title },
                                                            onDatePlayed = {
                                                                sortBy = SongSortBy.DatePlayed
                                                            },
                                                            onDateAdded = {
                                                                sortBy = SongSortBy.DateAdded
                                                            },
                                                            onPlayTime = {
                                                                sortBy = SongSortBy.PlayTime
                                                            },
                                                            onRelativePlayTime = {
                                                                sortBy = SongSortBy.RelativePlayTime
                                                            },
                                                            onDateLiked = {
                                                                sortBy = SongSortBy.DateLiked
                                                            },
                                                            onArtist = {
                                                                sortBy = SongSortBy.Artist
                                                            },
                                                            onDuration = {
                                                                sortBy = SongSortBy.Duration
                                                            },
                                                            onAlbum = {
                                                                sortBy = SongSortBy.AlbumName
                                                            },
                                                        )
                                                    }
                                                }

                                            }
                                        }
                                    )
                            )
                        }
                        if (builtInPlaylist == BuiltInPlaylist.Top) {
                            HeaderIconButton(
                                icon = R.drawable.stat,
                                color = colorPalette().text,
                                onClick = {},
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .clickable(
                                        onClick = {
                                            menuState.display {
                                                PeriodMenu(
                                                    onDismiss = {
                                                        topPlaylistPeriod = it
                                                        menuState.hide()
                                                    }
                                                )
                                            }
                                        }
                                    )
                            )
                        }

                        HeaderIconButton(
                            onClick = { searching = !searching },
                            icon = R.drawable.search_circle,
                            color = colorPalette().text,
                            iconSize = 24.dp,
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                        )

                        HeaderIconButton(
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .combinedClickable(
                                    onClick = {
                                        nowPlayingItem = -1
                                        scrollToNowPlaying = false
                                        items
                                            .forEachIndexed { index, song ->
                                                if (song.song.asMediaItem.mediaId == binder?.player?.currentMediaItem?.mediaId)
                                                    nowPlayingItem = index
                                            }

                                        if (nowPlayingItem > -1)
                                            scrollToNowPlaying = true
                                    },
                                    onLongClick = {
                                        SmartMessage(
                                            context.resources.getString(R.string.info_find_the_song_that_is_playing),
                                            context = context
                                        )
                                    }
                                ),
                            icon = R.drawable.locate,
                            enabled = songs.isNotEmpty(),
                            color = colorPalette().text,
                            onClick = {}
                        )
                        LaunchedEffect(scrollToNowPlaying) {
                            if (scrollToNowPlaying)
                                lazyListState.scrollToItem(nowPlayingItem, 1)
                            scrollToNowPlaying = false
                        }

                        if (builtInPlaylist == BuiltInPlaylist.Favorites) {
                            HeaderIconButton(
                                icon = R.drawable.downloaded,
                                enabled = (items.any { it.song.likedAt != -1L }),
                                color = if (items.any { it.song.likedAt != -1L }) colorPalette().text else colorPalette().text,
                                onClick = {},
                                modifier = Modifier
                                    .combinedClickable(
                                        onClick = {
                                            if (items.any { it.song.likedAt != -1L }) {
                                                showConfirmDownloadAllDialog = true
                                            } else {
                                                SmartMessage(context.resources.getString(R.string.disliked_this_collection),type = PopupType.Error, context = context)
                                            }
                                        },
                                        onLongClick = {
                                            SmartMessage(
                                                context.resources.getString(R.string.info_download_all_songs),
                                                context = context
                                            )
                                        }
                                    )
                            )
                        }

                        if (showConfirmDownloadAllDialog) {
                            ConfirmationDialog(
                                text = stringResource(R.string.do_you_really_want_to_download_all),
                                onDismiss = { showConfirmDownloadAllDialog = false },
                                onConfirm = {
                                    showConfirmDownloadAllDialog = false
                                    //isRecommendationEnabled = false
                                    downloadState = Download.STATE_DOWNLOADING
                                    if (listMediaItems.isEmpty()) {
                                        if (items.any { it.song.likedAt != -1L }) {
                                            items.filter { it.song.likedAt != -1L }.forEach {
                                                binder?.cache?.removeResource(it.song.asMediaItem.mediaId)
                                                manageDownload(
                                                    context = context,
                                                    mediaItem = it.song.asMediaItem,
                                                    downloadState = false
                                                )
                                            }
                                        }
                                    } else {
                                        listMediaItems.forEach {
                                            binder?.cache?.removeResource(it.mediaId)
                                            manageDownload(
                                                context = context,
                                                mediaItem = it,
                                                downloadState = false
                                            )

                                        }
                                        selectItems = false
                                    }
                                }
                            )
                        }

                        if (showRiMusicLikeYoutubeLikeConfirmDialog) {
                            Database.asyncTransaction {
                            totalMinutesToLike = formatAsDuration((if (listMediaItems.isNotEmpty()) (listMediaItems.filter { Database.getLikedAt(it.mediaId) !in listOf(-1L,null)}).size
                                else (items.filter { Database.getLikedAt(it.asMediaItem.mediaId) !in listOf(-1L,null) }).size)*1000.toLong())
                                }
                            ConfirmationDialog(
                                text = "$totalMinutesToLike "+stringResource(R.string.do_you_really_want_to_like_all_rimusictoytmusic),
                                onDismiss = { showRiMusicLikeYoutubeLikeConfirmDialog = false },
                                onConfirm = {
                                    showRiMusicLikeYoutubeLikeConfirmDialog = false

                                    if (listMediaItems.isNotEmpty()) {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            addToYtLikedSongs(listMediaItems.filter { Database.getLikedAt(it.mediaId) !in listOf(-1L,null) }.map { it })
                                        }
                                    } else {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            addToYtLikedSongs(items.filter { Database.getLikedAt(it.asMediaItem.mediaId) !in listOf(-1L,null) }.map { it.asMediaItem })
                                        }
                                    }
                                }
                            )
                        }

                        if (showYoutubeLikeConfirmDialog) {
                            songItemsToLike.clear()
                            if (listMediaItems.isEmpty()) {
                                items.forEachIndexed { index, song ->
                                    if (song.song.likedAt in listOf(-1L,null)) {
                                        songItemsToLike.add(song.asMediaItem)
                                    }
                                }
                            } else {
                                Database.asyncTransaction {
                                    listMediaItems.forEachIndexed { index, song ->
                                        if (Database.getLikedAt(song.mediaId) in listOf(-1L,null)) {
                                            songItemsToLike.add(song)
                                        }
                                    }
                                }
                            }
                            totalMinutesToLike = formatAsDuration(((songItemsToLike).size*1000).toLong())
                            ConfirmationDialog(
                                text = "$totalMinutesToLike "+stringResource(R.string.do_you_really_want_to_like_all),
                                onDismiss = { showYoutubeLikeConfirmDialog = false },
                                onConfirm = {
                                    showYoutubeLikeConfirmDialog = false
                                    CoroutineScope(Dispatchers.IO).launch {
                                        addToYtLikedSongs(songItemsToLike)
                                    }
                                }
                            )
                        }

                        if (deleteProgressDialog){
                            InProgressDialog(total = totalSongsToDelete, done = songsDeleted, text = stringResource(R.string.delete_in_process))
                        }
                        LaunchedEffect(checkCheck) {
                            if(!checkCheck) {
                                return@LaunchedEffect
                            }
                            SmartMessage(
                                context.resources.getString(R.string.please_wait),
                                type = PopupType.Info, context = context
                            )
                            deleteProgressDialog = true
                            withContext(Dispatchers.IO) {
                                Database.asyncTransaction {
                                    totalSongsToDelete = (itemsAll.filter {
                                        !it.song.id.startsWith(LOCAL_KEY_PREFIX)
                                                && it.song.likedAt == null
                                                && songUsedInPlaylists(it.song.id) == 0
                                                && albumBookmarked(
                                            songAlbumInfo(it.song.id)?.id ?: "" ) == 0
                                    }).size
                                    if (totalSongsToDelete == 0) {
                                        SmartMessage(
                                            context.resources.getString(R.string.nothing_to_delete),
                                            type = PopupType.Info, context = context
                                        )
                                        deleteProgressDialog = false
                                    } else {
                                        songsDeleted = 0
                                        itemsAll.filter {!it.song.id.startsWith(LOCAL_KEY_PREFIX)}.forEach { song ->
                                            Database.asyncTransaction {
                                                if ((song.song.likedAt == null) && (Database.songUsedInPlaylists(song.song.id) == 0) && (Database.albumBookmarked(Database.songAlbumInfo(song.song.id)?.id?: "") == 0)) {
                                                    binder?.cache?.removeResource(song.song.id)
                                                    binder?.downloadCache?.removeResource(song.song.id)
                                                    Database.delete(song.song)
                                                    songsDeleted++
                                                    if (songsDeleted == totalSongsToDelete) {
                                                        deleteProgressDialog = false
                                                        exitProcess(0)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    checkCheck = false
                                }
                            }
                        }

                        if (builtInPlaylist == BuiltInPlaylist.Favorites || builtInPlaylist == BuiltInPlaylist.Downloaded) {
                            HeaderIconButton(
                                icon = R.drawable.download,
                                enabled = songs.isNotEmpty(),
                                color = colorPalette().text,
                                onClick = {},
                                modifier = Modifier
                                    .combinedClickable(
                                        onClick = {
                                            showConfirmDeleteDownloadDialog = true
                                        },
                                        onLongClick = {
                                            SmartMessage(
                                                context.resources.getString(R.string.info_remove_all_downloaded_songs),
                                                context = context
                                            )
                                        }
                                    )
                            )

                            if (showConfirmDeleteDownloadDialog) {
                                ConfirmationDialog(
                                    text = stringResource(R.string.do_you_really_want_to_delete_download),
                                    onDismiss = { showConfirmDeleteDownloadDialog = false },
                                    onConfirm = {
                                        showConfirmDeleteDownloadDialog = false
                                        downloadState = Download.STATE_DOWNLOADING
                                        if (listMediaItems.isEmpty()) {
                                            if (items.isNotEmpty() == true)
                                                items.forEach {
                                                    binder?.cache?.removeResource(it.song.asMediaItem.mediaId)
                                                    manageDownload(
                                                        context = context,
                                                        mediaItem = it.song.asMediaItem,
                                                        downloadState = true
                                                    )
                                                }
                                        } else {
                                            listMediaItems.forEach {
                                                binder?.cache?.removeResource(it.mediaId)
                                                manageDownload(
                                                    context = context,
                                                    mediaItem = it,
                                                    downloadState = true
                                                )
                                            }
                                            selectItems = false
                                        }
                                    }
                                )
                            }
                        }

                        if (builtInPlaylist == BuiltInPlaylist.All)
                            HeaderIconButton(
                                onClick = {},
                                icon = if (showHiddenSongs == 0) R.drawable.eye_off else R.drawable.eye,
                                color = colorPalette().text,
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .combinedClickable(
                                        onClick = {
                                            showHiddenSongs = if (showHiddenSongs == 0) -1 else 0
                                        },
                                        onLongClick = {
                                            SmartMessage(
                                                context.resources.getString(R.string.info_show_hide_hidden_songs),
                                                context = context
                                            )
                                        }
                                    )
                            )

                        HeaderIconButton(
                            icon = R.drawable.shuffle,
                            enabled = items.any { it.song.likedAt != -1L },
                            color = if (items.any { it.song.likedAt != -1L }) colorPalette().text else colorPalette().textDisabled,
                            onClick = {},
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .combinedClickable(
                                    onClick = {
                                        if (builtInPlaylist == BuiltInPlaylist.OnDevice) items =
                                            filteredSongs
                                        if (items.filter { it.song.likedAt != -1L }.isNotEmpty()) {
                                            val itemsLimited =
                                                if (items.filter { it.song.likedAt != -1L }.size > maxSongsInQueue.number) items.filter { it.song.likedAt != -1L }
                                                    .shuffled()
                                                    .take(maxSongsInQueue.number.toInt()) else items.filter { it.song.likedAt != -1L }
                                            binder?.stopRadio()
                                            binder?.player?.forcePlayFromBeginning(
                                                itemsLimited
                                                    .shuffled()
                                                    .map(SongEntity::asMediaItem)
                                            )
                                        } else {
                                            SmartMessage(context.resources.getString(R.string.disliked_this_collection),type = PopupType.Error, context = context)
                                        }
                                    },
                                    onLongClick = {
                                        SmartMessage(
                                            context.resources.getString(R.string.info_shuffle),
                                            context = context
                                        )
                                    }
                                )
                        )

                        if (builtInPlaylist == BuiltInPlaylist.Favorites)
                            HeaderIconButton(
                                icon = R.drawable.random,
                                enabled = true,
                                color = if (autoShuffle) colorPalette().text else colorPalette().textDisabled,
                                onClick = {},
                                modifier = Modifier
                                    .combinedClickable(
                                        onClick = {
                                            autoShuffle = !autoShuffle
                                        },
                                        onLongClick = {
                                            SmartMessage("Random sorting", context = context)
                                        }
                                    )
                            )

                        if (builtInPlaylist != BuiltInPlaylist.Favorites)
                            HeaderIconButton(
                                icon = R.drawable.resource_import,
                                color = colorPalette().text,
                                //iconSize = 22.dp,
                                onClick = {},
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .combinedClickable(
                                        onClick = {
                                            try {
                                                importLauncher.launch(
                                                    arrayOf(
                                                        "text/*"
                                                    )
                                                )
                                            } catch (e: ActivityNotFoundException) {
                                                SmartMessage(
                                                    context.resources.getString(R.string.info_not_find_app_open_doc),
                                                    type = PopupType.Warning, context = context
                                                )
                                            }
                                        },
                                        onLongClick = {
                                            SmartMessage(
                                                context.resources.getString(R.string.import_favorites),
                                                context = context
                                            )
                                        }
                                    )
                            )

                        HeaderIconButton(
                            icon = R.drawable.ellipsis_horizontal,
                            color = colorPalette().text,
                            onClick = {
                                menuState.display {
                                    PlaylistsItemMenu(
                                        navController = navController,
                                        modifier = Modifier.fillMaxHeight(0.4f),
                                        onDismiss = menuState::hide,
                                        onSelectUnselect = {
                                            selectItems = !selectItems
                                            if (!selectItems) {
                                                listMediaItems.clear()
                                            }
                                        },
                                        onPlayNext = {
                                            if (builtInPlaylist == BuiltInPlaylist.OnDevice) items =
                                                filteredSongs
                                            if (listMediaItems.isEmpty()) {
                                                if (items.any { it.song.likedAt != -1L }) {
                                                    binder?.player?.addNext(
                                                        items.filter { it.song.likedAt != -1L }
                                                            .map(SongEntity::asMediaItem),
                                                        context
                                                    )
                                                } else {
                                                    SmartMessage(context.resources.getString(R.string.disliked_this_collection),type = PopupType.Error, context = context)
                                                }
                                            } else {
                                                binder?.player?.addNext(listMediaItems, context)
                                                listMediaItems.clear()
                                                selectItems = false
                                            }
                                        },
                                        onEnqueue = {
                                            if (builtInPlaylist == BuiltInPlaylist.OnDevice) items =
                                                filteredSongs
                                            if (listMediaItems.isEmpty()) {
                                                if (items.any { it.song.likedAt != -1L }) {
                                                    binder?.player?.enqueue(
                                                        items.filter { it.song.likedAt != -1L }
                                                            .map(SongEntity::asMediaItem),
                                                        context
                                                    )
                                                } else {
                                                    SmartMessage(context.resources.getString(R.string.disliked_this_collection),type = PopupType.Error, context = context)
                                                }
                                            } else {
                                                binder?.player?.enqueue(listMediaItems, context)
                                                listMediaItems.clear()
                                                selectItems = false
                                            }
                                        },
                                        onAddToPreferites = {
                                            if (!isNetworkConnected(appContext()) && isYouTubeSyncEnabled()) {
                                                SmartMessage(appContext().resources.getString(R.string.no_connection), context = appContext(), type = PopupType.Error)
                                            } else if (!isYouTubeSyncEnabled()){
                                                if (listMediaItems.isNotEmpty()) {
                                                    Database.asyncTransaction {
                                                        listMediaItems.filter{getLikedAt(it.mediaId) in listOf(-1L,null)}.map {
                                                            Database.like(
                                                                it.mediaId,
                                                                System.currentTimeMillis()
                                                            )
                                                        }
                                                    }
                                                } else {
                                                    Database.asyncTransaction {
                                                        items.filter {
                                                            getLikedAt(it.asMediaItem.mediaId) in listOf(-1L,null)
                                                        }.map {
                                                            Database.like(
                                                                it.asMediaItem.mediaId,
                                                                System.currentTimeMillis()
                                                            )
                                                        }
                                                    }
                                                }
                                            } else {
                                                showYoutubeLikeConfirmDialog = true
                                            }
                                        },
                                        showonAddToPreferitesYoutube = isYouTubeSyncEnabled(),
                                        onAddToPreferitesYoutube = {
                                            if (!isNetworkConnected(appContext())) {
                                                SmartMessage(appContext().resources.getString(R.string.no_connection), context = appContext(), type = PopupType.Error)
                                            } else {
                                                showRiMusicLikeYoutubeLikeConfirmDialog = true
                                            }
                                        },
                                        onAddToPlaylist = { playlistPreview ->
                                            if (builtInPlaylist == BuiltInPlaylist.OnDevice) items =
                                                filteredSongs
                                            position =
                                                playlistPreview.songCount.minus(1) ?: 0
                                            if (position > 0) position++ else position = 0

                                            val filteredItems = items.filterNot {it.asMediaItem.mediaId.startsWith(LOCAL_KEY_PREFIX) || it.song.thumbnailUrl == ""}
                                            if ((filteredItems.size + playlistPreview.songCount) > 5000 && playlistPreview.playlist.isYoutubePlaylist && isYouTubeSyncEnabled()){
                                                SmartMessage(context.resources.getString(R.string.yt_playlist_limited), context = context, type = PopupType.Error)
                                            } else if (!isYouTubeSyncEnabled() || !playlistPreview.playlist.isYoutubePlaylist) {
                                                items.forEachIndexed { index, song ->
                                                    runCatching {
                                                        CoroutineScope(Dispatchers.IO).launch {
                                                            Database.insert(song.song.asMediaItem)
                                                            Database.insert(
                                                                SongPlaylistMap(
                                                                    songId = song.song.asMediaItem.mediaId,
                                                                    playlistId = playlistPreview.playlist.id,
                                                                    position = position + index
                                                                ).default()
                                                            )
                                                        }
                                                    }.onFailure {
                                                        Timber.e("Failed addToPlaylist in HomeSongsModern ${it.stackTraceToString()}")
                                                        println("Failed addToPlaylist in HomeSongsModern ${it.stackTraceToString()}")
                                                    }
                                                }
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    SmartMessage(context.resources.getString(R.string.done), type = PopupType.Success, context = context)
                                                }
                                            } else {
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    addToYtPlaylist(playlistPreview.playlist.id,
                                                        position,
                                                        playlistPreview.playlist.browseId ?: "",
                                                        filteredItems.map { it.asMediaItem })
                                                }
                                            }
                                        },
                                        onDeleteSongsNotInLibrary = {
                                            checkCheck = true
                                        },
                                        onExport = {
                                            isExporting = true
                                        },
                                        disableScrollingText = disableScrollingText
                                    )
                                }
                            },
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                        )

                    }

                    /*        */

                    AnimatedVisibility(visible = searching) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier
                                //.requiredHeight(30.dp)
                                .padding(all = 10.dp)
                                .fillMaxWidth()
                        ) {
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
                    /*        */
                    //}



                }
            }

            if (builtInPlaylist == BuiltInPlaylist.OnDevice) {
                if (!hasPermission) {
                    item(
                        key = "OnDeviceSongsPermission"
                    ) {
                        LaunchedEffect(Unit, relaunchPermission) { launcher.launch(permission) }

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(
                                2.dp,
                                Alignment.CenterVertically
                            ),
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
                                            setData(
                                                Uri.fromParts(
                                                    "package",
                                                    context.packageName,
                                                    null
                                                )
                                            )
                                        }
                                    )
                                }
                            )

                        }

                    }
                } else {
                    if (showFolders) {
                        if (currentFolderPath != "/") {

                            item {
                                BackHandler(onBack = {
                                    currentFolderPath = currentFolderPath.removeSuffix("/").substringBeforeLast("/") + "/"
                                })
                            }

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
                                items = filteredFolders.distinctBy { it.fullPath },
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
                                                    FolderItemMenu(
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
                                                };
                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
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
                        items = filteredSongs.distinctBy { it.song.id },
                        //key = { index, _ -> Random.nextLong().toString() },
                        //contentType = { _, song -> song },
                        //) { index, song ->
                        key = { _, song -> song.song.id }
                    ) { index, song ->
                        val isLocal by remember { derivedStateOf { song.asMediaItem.isLocal } }
                        val isDownloaded = isLocal || isDownloadedSong( song.asMediaItem.mediaId )
                        SwipeablePlaylistItem(
                            mediaItem = song.asMediaItem,
                            onPlayNext = {
                                binder?.player?.addNext(song.asMediaItem)
                            }
                        ) {
                            var forceRecompose by remember { mutableStateOf(false) }

                            SongItem(
                                song = song.song,
                                onDownloadClick = {
                                    if( builtInPlaylist != BuiltInPlaylist.OnDevice ) {
                                        binder?.cache?.removeResource(song.song.asMediaItem.mediaId)
                                        CoroutineScope(Dispatchers.IO).launch {
                                            Database.resetContentLength( song.asMediaItem.mediaId )
                                        }
                                        if (!isLocal)
                                            manageDownload(
                                                context = context,
                                                mediaItem = song.song.asMediaItem,
                                                downloadState = isDownloaded
                                            )
                                    }
                                },
                                downloadState = Download.STATE_COMPLETED,
                                thumbnailSizeDp = thumbnailSizeDp,
                                thumbnailSizePx = thumbnailSizePx,
                                onThumbnailContent = {
                                    if ( sortBy == SongSortBy.PlayTime || builtInPlaylist == BuiltInPlaylist.Top ) {
                                        var text = song.song.formattedTotalPlayTime
                                        var typography = typography().xxs
                                        var alignment = Alignment.BottomCenter

                                        if( builtInPlaylist == BuiltInPlaylist.Top ) {
                                            text = (index + 1).toString()
                                            typography = typography().m
                                            alignment = Alignment.Center
                                        }

                                        BasicText(
                                            text = text,
                                            style = typography.semiBold.center.color(colorPalette().onOverlay),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                .align(alignment)
                                                .background(
                                                    brush = Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color.Transparent,
                                                            colorPalette().overlay
                                                        )
                                                    ),
                                                    shape = thumbnailShape()
                                                )
                                        )
                                    }

                                    NowPlayingSongIndicator(song.asMediaItem.mediaId, binder?.player)
                                },
                                trailingContent = {
                                    val checkedState = rememberSaveable { mutableStateOf(false) }
                                    if (selectItems)
                                        androidx.compose.material3.Checkbox(
                                            checked = checkedState.value,
                                            onCheckedChange = {
                                                checkedState.value = it
                                                if (it) listMediaItems.add(song.asMediaItem) else
                                                    listMediaItems.remove(song.asMediaItem)
                                            },
                                            colors = androidx.compose.material3.CheckboxDefaults.colors(
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
                                                InHistoryMediaItemMenu(
                                                    navController = navController,
                                                    song = song.song,
                                                    onDismiss = {
                                                        menuState.hide()
                                                        forceRecompose = true
                                                    },
                                                    onInfo = {
                                                        navController.navigate("${NavRoutes.videoOrSongInfo.name}/${song.song.id}")
                                                    },
                                                    disableScrollingText = disableScrollingText
                                                )
                                            }
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
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
                }
            }

            if (builtInPlaylist != BuiltInPlaylist.OnDevice) {
                itemsIndexed(
                    items = if (parentalControlEnabled)
                        items.filter { !it.song.title.startsWith(EXPLICIT_PREFIX) }.distinctBy { it.song.id }
                    else items.distinctBy { it.song.id },
                    key = { _, song -> song.song.id },
                    //contentType = { _, song -> song },
                ) { index, song ->

                    var isHiding by remember {
                        mutableStateOf(false)
                    }

                    var isDeleting by remember {
                        mutableStateOf(false)
                    }

                    if (isHiding) {
                        ConfirmationDialog(
                            text = stringResource(R.string.update_song),
                            onDismiss = { isHiding = false },
                            onConfirm = {
                                Database.asyncTransaction {
                                    menuState.hide()
                                    binder?.cache?.removeResource(song.song.id)
                                    binder?.downloadCache?.removeResource(song.song.id)
                                    Database.incrementTotalPlayTimeMs(
                                        song.song.id,
                                        -song.song.totalPlayTimeMs
                                    )
                                }
                            }
                        )
                    }

                    if (isDeleting) {
                        ConfirmationDialog(
                            text = stringResource(R.string.delete_song),
                            onDismiss = { isDeleting = false },
                            onConfirm = {
                                Database.asyncTransaction {
                                    menuState.hide()
                                    binder?.cache?.removeResource(song.song.id)
                                    binder?.downloadCache?.removeResource(song.song.id)
                                    Database.delete(song.song)
                                    Database.deleteSongFromPlaylists(song.song.id)
                                }
                                SmartMessage(context.resources.getString(R.string.deleted), context = context)
                            }
                        )
                    }

                    SwipeablePlaylistItem(
                        mediaItem = song.song.asMediaItem,
                        onPlayNext = {
                            binder?.player?.addNext(song.song.asMediaItem)
                        }
                    ) {
                        var forceRecompose by remember { mutableStateOf(false) }
                        val isLocal by remember { derivedStateOf { song.song.asMediaItem.isLocal } }
                        downloadState = getDownloadState(song.song.asMediaItem.mediaId)
                        val isDownloaded = if (!isLocal) isDownloadedSong(song.song.asMediaItem.mediaId) else true
                        val checkedState = rememberSaveable { mutableStateOf(false) }
                        SongItem(
                            song = song.song,
                            onDownloadClick = {
                                binder?.cache?.removeResource(song.song.asMediaItem.mediaId)
                                Database.asyncTransaction {
                                    Database.insert(
                                        Song(
                                            id = song.song.asMediaItem.mediaId,
                                            title = song.song.asMediaItem.mediaMetadata.title.toString(),
                                            artistsText = song.song.asMediaItem.mediaMetadata.artist.toString(),
                                            thumbnailUrl = song.song.thumbnailUrl,
                                            durationText = null
                                        )
                                    )
                                }
                                if (!isLocal)
                                    manageDownload(
                                        context = context,
                                        mediaItem = song.song.asMediaItem,
                                        downloadState = isDownloaded
                                    )
                            },
                            downloadState = downloadState,
                            thumbnailSizePx = thumbnailSizePx,
                            thumbnailSizeDp = thumbnailSizeDp,
                            onThumbnailContent = {
                                if (sortBy == SongSortBy.PlayTime) {
                                    BasicText(
                                        text = song.song.formattedTotalPlayTime,
                                        style = typography().xxs.semiBold.center.color(colorPalette().onOverlay),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        colorPalette().overlay
                                                    )
                                                ),
                                                shape = thumbnailShape()
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .align(Alignment.BottomCenter)
                                    )
                                }
                                if (sortBy == SongSortBy.RelativePlayTime){
                                    BasicText(
                                        text = "${song.relativePlayTime().toLong()}",
                                        style = typography().xxs.semiBold.center.color(colorPalette().onOverlay),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        colorPalette().overlay
                                                    )
                                                ),
                                                shape = thumbnailShape()
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .align(Alignment.BottomCenter)
                                    )
                                }

                                if (nowPlayingItem > -1)
                                    NowPlayingSongIndicator(song.song.asMediaItem.mediaId, binder?.player)

                                if (builtInPlaylist == BuiltInPlaylist.Top)
                                    BasicText(
                                        text = (index + 1).toString(),
                                        style = typography().m.semiBold.center.color(colorPalette().onOverlay),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        colorPalette().overlay
                                                    )
                                                ),
                                                shape = thumbnailShape()
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .align(Alignment.Center)
                                    )
                            },
                            trailingContent = {
                                if (selectItems)
                                    Checkbox(
                                        checked = checkedState.value,
                                        onCheckedChange = {
                                            checkedState.value = it
                                            if (it) listMediaItems.add(song.song.asMediaItem) else
                                                listMediaItems.remove(song.song.asMediaItem)
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
                                            InHistoryMediaItemMenu(
                                                navController = navController,
                                                song = song.song,
                                                onDismiss = {
                                                    menuState.hide()
                                                    forceRecompose = true
                                                },
                                                onInfo = {
                                                    navController.navigate("${NavRoutes.videoOrSongInfo.name}/${song.song.id}")
                                                },
                                                onHideFromDatabase = { isHiding = true },
                                                onDeleteFromDatabase = { isDeleting = true },
                                                disableScrollingText = disableScrollingText,
                                            )
                                        }
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    onClick = {
                                        if (song.song.likedAt != -1L) {
                                            searching = false
                                            filter = null

                                            val maxSongs = maxSongsInQueue.number.toInt()
                                            val itemsRange: IntRange
                                            val playIndex: Int
                                            if (items.size < maxSongsInQueue.number) {
                                                itemsRange = items.indices
                                                playIndex = index
                                            } else {
                                                when (queueLimit) {
                                                    QueueSelection.START_OF_QUEUE -> {
                                                        // tries to guarantee maxSongs many songs
                                                        // window starting from index with maxSongs songs (if possible)
                                                        itemsRange = index..<min(
                                                            index + maxSongs,
                                                            items.size
                                                        )

                                                        // index is located at the first position
                                                        playIndex = 0
                                                    }

                                                    QueueSelection.CENTERED -> {
                                                        // tries to guarantee >= maxSongs/2 many songs
                                                        // window with +- maxSongs/2 songs (if possible) around index
                                                        val minIndex = max(0, index - maxSongs / 2)
                                                        val maxIndex =
                                                            min(index + maxSongs / 2, items.size)
                                                        itemsRange = minIndex..<maxIndex

                                                        // index is located at "center"
                                                        playIndex = index - minIndex
                                                    }

                                                    QueueSelection.END_OF_QUEUE -> {
                                                        // tries to guarantee maxSongs many songs
                                                        // window with maxSongs songs (if possible) ending at index
                                                        val minIndex = max(0, index - maxSongs + 1)
                                                        val maxIndex = min(index, items.size)
                                                        itemsRange = minIndex..maxIndex

                                                        // index is located at end
                                                        playIndex = index - minIndex
                                                    }

                                                    QueueSelection.END_OF_QUEUE_WINDOWED -> {
                                                        // tries to guarantee maxSongs many songs,
                                                        // similar to original implementation in it's valid range
                                                        // window with maxSongs songs (if possible) before index
                                                        val minIndex = max(0, index - maxSongs + 1)
                                                        val maxIndex =
                                                            min(minIndex + maxSongs, items.size)
                                                        itemsRange = minIndex..<maxIndex

                                                        // index is located at "end"
                                                        playIndex = index - minIndex
                                                    }
                                                }
                                            }
                                            val itemsLimited = items.slice(itemsRange)
                                            binder?.stopRadio()
                                            binder?.player?.forcePlayAtIndex(
                                                itemsLimited.filter { it.song.likedAt != -1L }.map(SongEntity::asMediaItem),
                                                itemsLimited.filter { it.song.likedAt != -1L }.map(SongEntity::asMediaItem).indexOf(song.asMediaItem)
                                            )
                                        } else {
                                            CoroutineScope(Dispatchers.Main).launch {
                                                SmartMessage(context.resources.getString(R.string.disliked_this_song),type = PopupType.Error, context = context)
                                            }
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
            }

            item(key = "bottom") {
                Spacer(modifier = Modifier.height(Dimensions.bottomSpacer))
            }

        }

        FloatingActionsContainerWithScrollToTop(lazyListState = lazyListState)

        val showFloatingIcon by rememberPreference(showFloatingIconKey, false)
        if( UiType.ViMusic.isCurrent() && showFloatingIcon )
            MultiFloatingActionsContainer(
                iconId = R.drawable.search,
                onClick = onSearchClick,
                onClickSettings = onSettingsClick,
                onClickSearch = onSearchClick
            )





    }
}
