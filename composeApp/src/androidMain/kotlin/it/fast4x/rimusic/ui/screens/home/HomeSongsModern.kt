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
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
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
import it.fast4x.rimusic.enums.BuiltInPlaylist
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
import it.fast4x.rimusic.enums.TopPlaylistPeriod
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.models.Folder
import it.fast4x.rimusic.models.OnDeviceSong
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.models.SongEntity
import it.fast4x.rimusic.models.SongPlaylistMap
import it.fast4x.rimusic.query
import it.fast4x.rimusic.service.DownloadUtil
import it.fast4x.rimusic.service.LOCAL_KEY_PREFIX
import it.fast4x.rimusic.service.isLocal
import it.fast4x.rimusic.transaction
import it.fast4x.rimusic.ui.components.ButtonsRow
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.SwipeablePlaylistItem
import it.fast4x.rimusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import it.fast4x.rimusic.ui.components.themed.FolderItemMenu
import it.fast4x.rimusic.ui.components.themed.HeaderInfo
import it.fast4x.rimusic.ui.components.themed.InHistoryMediaItemMenu
import it.fast4x.rimusic.ui.components.themed.MultiFloatingActionsContainer
import it.fast4x.rimusic.ui.components.themed.NowPlayingShow
import it.fast4x.rimusic.ui.components.themed.PeriodMenu
import it.fast4x.rimusic.ui.components.themed.PlaylistsItemMenu
import it.fast4x.rimusic.ui.components.themed.SecondaryTextButton
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.ui.items.FolderItem
import it.fast4x.rimusic.ui.items.SongItem
import it.fast4x.rimusic.ui.screens.ondevice.musicFilesAsFlow
import it.fast4x.rimusic.ui.styling.Dimensions
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
import it.fast4x.rimusic.utils.downloadedStateMedia
import it.fast4x.rimusic.utils.durationTextToMillis
import it.fast4x.rimusic.utils.enqueue
import it.fast4x.rimusic.utils.excludeSongsWithDurationLimitKey
import it.fast4x.rimusic.utils.forcePlayAtIndex
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
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.showCachedPlaylistKey
import it.fast4x.rimusic.utils.showDownloadedPlaylistKey
import it.fast4x.rimusic.utils.showFavoritesPlaylistKey
import it.fast4x.rimusic.utils.showFloatingIconKey
import it.fast4x.rimusic.utils.showFoldersOnDeviceKey
import it.fast4x.rimusic.utils.showMyTopPlaylistKey
import it.fast4x.rimusic.utils.showOnDevicePlaylistKey
import it.fast4x.rimusic.utils.songSortByKey
import it.fast4x.rimusic.utils.songSortOrderKey
import it.fast4x.rimusic.utils.topPlaylistPeriodKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.knighthat.colorPalette
import me.knighthat.component.header.TabToolBar
import me.knighthat.component.tab.TabHeader
import me.knighthat.component.tab.toolbar.ConfirmationDialog
import me.knighthat.component.tab.toolbar.Dialog
import me.knighthat.component.tab.toolbar.InputDialog
import me.knighthat.component.tab.toolbar.Search
import me.knighthat.component.tab.toolbar.SongsShuffle
import me.knighthat.component.tab.toolbar.Sort
import me.knighthat.thumbnailShape
import me.knighthat.typography
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Optional
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration


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
    // Essentials
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val thumbnailSizeDp = Dimensions.thumbnails.song
    val thumbnailSizePx = thumbnailSizeDp.px

    val parentalControlEnabled by rememberPreference(parentalControlEnabledKey, false)

    var items by persistList<SongEntity>("home/songs")
    var listMediaItems = remember { mutableListOf<MediaItem>() }

    //var songsWithAlbum by persistList<SongWithAlbum>("home/songsWithAlbum")

    /*
    var filterDownloaded by remember {
        mutableStateOf(false)
    }
     */

    var builtInPlaylist by rememberPreference(
        builtInPlaylistKey,
        BuiltInPlaylist.Favorites
    )

    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }

    val context = LocalContext.current

    var showHiddenSongs by remember {
        mutableStateOf(0)
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

    var songs: List<SongEntity> = emptyList()
    var folders: List<Folder> = emptyList()

    var filteredSongs = songs
    val maxSongsInQueue  by rememberPreference(maxSongsInQueueKey, MaxSongs.`500`)

    // Non-vital
    var plistName by remember { mutableStateOf( "" ) }
    var selectItems by remember { mutableStateOf( false ) }

    // Search states
    val searching = rememberSaveable { mutableStateOf(false) }
    val isSearchInputFocused = rememberSaveable { mutableStateOf( false ) }
    val filter = rememberSaveable { mutableStateOf( "" ) }
    // Sort states
    val deviceSongSortState = rememberPreference(onDeviceSongSortByKey, OnDeviceSongSortBy.DateAdded)
    val deviceFolderSortState = rememberPreference(onDeviceFolderSortByKey, OnDeviceFolderSortBy.Title)
    val songSortState = rememberPreference(songSortByKey, SongSortBy.DateAdded)
    val sortOrderState = rememberPreference(songSortOrderKey, SortOrder.Descending)
    // Dialog states
    val exportToggleState = rememberSaveable { mutableStateOf( false ) }
    val downloadAllToggleState = rememberSaveable { mutableStateOf( false ) }
    val deleteDownloadsToggleState = rememberSaveable { mutableStateOf( false ) }
    val deleteSongToggleState = rememberSaveable { mutableStateOf( false ) }
    val hideSongToggleState = rememberSaveable { mutableStateOf( false ) }

    val search = remember {
        object: Search {
            override val visibleState = searching
            override val focusState = isSearchInputFocused
            override val inputState = filter
        }
    }
    val songSort = remember {
        object: Sort<SongSortBy> {
            override val menuState = menuState
            override val sortOrderState = sortOrderState
            override val sortByEnum = SongSortBy.entries
            override val sortByState = songSortState
        }
    }
    val deviceSongSort = remember {
        object: Sort<OnDeviceSongSortBy> {
            override val menuState = menuState
            override val sortOrderState = sortOrderState
            override val sortByEnum = OnDeviceSongSortBy.entries
            override val sortByState = deviceSongSortState
        }
    }
    val deviceFolderSort = remember {
        object: Sort<OnDeviceFolderSortBy> {
            override val menuState = menuState
            override val sortOrderState = sortOrderState
            override val sortByEnum = OnDeviceFolderSortBy.entries
            override val sortByState = deviceFolderSortState
        }
    }
    val shuffle = remember {
        object: SongsShuffle {
            override val binder = binder
            override val context = context
            override val dispatcher = Dispatchers.Main

            override fun query(): Flow<List<Song>?> {
                if ( builtInPlaylist == BuiltInPlaylist.OnDevice )
                    items = filteredSongs

                return  flowOf(items.map(SongEntity::song))
            }
        }
    }
    // START - Import songs
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        context.applicationContext
               .contentResolver
               .openInputStream(uri)
               ?.use { inputStream ->
                    csvReader().open(inputStream) {
                        readAllWithHeaderAsSequence().forEachIndexed { index, row: Map<String, String> ->
                            println("mediaItem index song ${index}")
                            transaction {
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
                                    transaction {
                                        if (song != null) {
                                            Database.upsert(song)
                                            Database.like(
                                                song.id,
                                                System.currentTimeMillis()
                                            )
                                        }
                                    }


                                }
                            }

                        }
                    }
               }
    }
    // END - Import songs
    val importDialog = remember {
        object: Dialog {
            override val context = context
            override val toggleState = mutableStateOf( false )
            override val iconId = R.drawable.resource_import
            override val titleId = -1       // Unused
            override val messageId = R.string.import_favorites

            @Composable
            override fun Render() {}

            override fun onShortClick() {
                try {
                    importLauncher.launch( arrayOf( "text/*" ) )
                } catch (_: ActivityNotFoundException) {
                    SmartMessage(
                        context.resources.getString(R.string.info_not_find_app_open_doc),
                        type = PopupType.Warning, context = context
                    )
                }
            }
        }
    }
    // START - Export songs
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        context.applicationContext
               .contentResolver
               .openOutputStream(uri)
               ?.use { outputStream ->
                    csvWriter().open(outputStream){
                        writeRow("PlaylistBrowseId", "PlaylistName", "MediaId", "Title", "Artists", "Duration", "ThumbnailUrl")
                        if (listMediaItems.isEmpty()) {
                            items.forEach {
                                writeRow(
                                    "",
                                    plistName,
                                    it.song.id,
                                    it.song.title,
                                    it.song.artistsText,
                                    it.song.durationText,
                                    it.song.thumbnailUrl
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
    // END - Export songs
    val exportDialog = remember {
        object: InputDialog {
            override val context = context
            override val toggleState = exportToggleState
            override val iconId = -1
            override val titleId = R.string.enter_the_playlist_name
            override val messageId = -1
            override val defValue: String
                get() = context.resources.getString(
                    when( builtInPlaylist ){
                        BuiltInPlaylist.All -> R.string.songs
                        BuiltInPlaylist.Favorites -> R.string.favorites
                        BuiltInPlaylist.Offline -> R.string.cached
                        BuiltInPlaylist.Downloaded -> R.string.downloaded
                        BuiltInPlaylist.Top -> R.string.playlist_top
                        BuiltInPlaylist.OnDevice -> R.string.on_device
                    }
                )

            override fun onSet(newValue: String) {
                try {
                    val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault() )
                    val fullName = "RMPlaylist_${newValue.take(20)}_${dateFormat.format(Date())}"
                    exportLauncher.launch( fullName )
                } catch (e: ActivityNotFoundException) {
                    SmartMessage("Couldn't find an application to create documents",
                        type = PopupType.Warning, context = context)
                }

                onDismiss()
            }
        }
    }
    val downloadAllDialog = remember {
        object: ConfirmationDialog {
            override val context = context
            override val toggleState = downloadAllToggleState
            override val iconId = R.drawable.downloaded
            override val titleId = R.string.do_you_really_want_to_download_all
            override val messageId = R.string.info_download_all_songs

            override fun onConfirm() {
                downloadState = Download.STATE_DOWNLOADING

                val toBeDownloaded = mutableListOf<MediaItem>()
                if( listMediaItems.isNotEmpty() )
                    toBeDownloaded.addAll( listMediaItems )
                else if( items.isNotEmpty() ) {
                    items.forEach {
                        toBeDownloaded.add( it.asMediaItem )

                        query {
                            Database.insert(
                                Song(
                                    id = it.asMediaItem.mediaId,
                                    title = it.asMediaItem.mediaMetadata.title.toString(),
                                    artistsText = it.asMediaItem.mediaMetadata.artist.toString(),
                                    thumbnailUrl = it.song.thumbnailUrl,
                                    durationText = null
                                )
                            )
                        }
                    }

                    selectItems = false
                }

                // If no element was added because either condition above is true
                // then the block below will not be executed
                toBeDownloaded.forEach {
                    binder?.cache?.removeResource(it.mediaId)

                    manageDownload(
                        context = context,
                        songId = it.mediaId,
                        songTitle = it.mediaMetadata.title.toString(),
                        downloadState = true
                    )
                }

                onDismiss()
            }
        }
    }
    val deleteDownloadsDialog = remember {
        object: ConfirmationDialog {
            override val context = context
            override val toggleState = deleteDownloadsToggleState
            override val iconId = R.drawable.download
            override val titleId = R.string.do_you_really_want_to_delete_download
            override val messageId = R.string.info_remove_all_downloaded_songs

            override fun onConfirm() {
                downloadState = Download.STATE_DOWNLOADING

                val toBeDeleted: List<MediaItem> =
                    if( listMediaItems.isNotEmpty() )
                        listMediaItems
                    else if( items.isNotEmpty() )
                        items.map( SongEntity::asMediaItem )
                    else
                        emptyList()

                toBeDeleted.forEach {
                    binder?.cache?.removeResource(it.mediaId)

                    manageDownload(
                        context = context,
                        songId = it.mediaId,
                        songTitle = it.mediaMetadata.title.toString(),
                        downloadState = true
                    )
                }

                selectItems = false
                onDismiss()
            }
        }
    }
    val deleteSongDialog = remember {
        object: ConfirmationDialog {
            override val context = context
            override val toggleState = deleteSongToggleState
            override val iconId = -1
            override val titleId = R.string.delete_song
            override val messageId = -1

            var song: Optional<SongEntity> = Optional.empty()

            override fun onDismiss() {
                // Always override current value with empty Optional
                // to prevent unwanted outcomes
                song = Optional.empty()
                super.onDismiss()
            }

            override fun onConfirm() {
                song.ifPresent {
                    query {
                        menuState.hide()
                        binder?.cache?.removeResource(it.song.id)
                        binder?.downloadCache?.removeResource(it.song.id)
                        Database.delete(it.song)
                        Database.deleteSongFromPlaylists(it.song.id)
                    }
                    SmartMessage(context.resources.getString(R.string.deleted), context = context)
                }

                onDismiss()
            }
        }
    }
    val hideSongDialog = remember {
        object: ConfirmationDialog {
            override val context = context
            override val toggleState = hideSongToggleState
            override val iconId = -1
            override val titleId = R.string.hidesong
            override val messageId = -1

            var song: Optional<SongEntity> = Optional.empty()

            override fun onDismiss() {
                // Always override current value with empty Optional
                // to prevent unwanted outcomes
                song = Optional.empty()
                super.onDismiss()
            }

            override fun onConfirm() {
                song.ifPresent {
                    query {
                        menuState.hide()
                        binder?.cache?.removeResource(it.song.id)
                        binder?.downloadCache?.removeResource(it.song.id)
                        Database.incrementTotalPlayTimeMs(
                            it.song.id,
                            -it.song.totalPlayTimeMs
                        )
                    }
                }

                onDismiss()
            }
        }
    }

    // Search mutable
    var isSearchBarVisible by search.visibleState
    var isSearchBarFocused by search.focusState
    var searchInput by search.inputState
    // Sort mutable
    val sortOrder by sortOrderState
    val sortBy by songSort.sortByState
    val sortByOnDevice by deviceSongSort.sortByState

    val defaultFolder by rememberPreference(defaultFolderKey, "/")

    var songsDevice by remember(sortBy, sortOrder) {
        mutableStateOf<List<OnDeviceSong>>(emptyList())
    }

    var filteredFolders = folders
    var currentFolder: Folder? = null;
    var currentFolderPath by remember {
        mutableStateOf(defaultFolder)
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

    when (builtInPlaylist) {
        BuiltInPlaylist.All -> {
            LaunchedEffect(sortBy, sortOrder, searchInput, showHiddenSongs, includeLocalSongs) {
                //Database.songs(sortBy, sortOrder, showHiddenSongs).collect { items = it }
                Database.songs(sortBy, sortOrder, showHiddenSongs).collect { items = it }

            }
        }
        BuiltInPlaylist.Downloaded, BuiltInPlaylist.Favorites, BuiltInPlaylist.Offline, BuiltInPlaylist.Top -> {

            LaunchedEffect(Unit, builtInPlaylist, sortBy, sortOrder, searchInput, topPlaylistPeriod) {

                var songFlow: Flow<List<SongEntity>> = flowOf()
                var dispatcher = Dispatchers.Default
                var filterCondition: (SongEntity) -> Boolean = { true }

                when( builtInPlaylist ) {
                    BuiltInPlaylist.Favorites -> {

                        songFlow = Database.songsFavorites(sortBy, sortOrder)
                        filterCondition = { true }
                    }
                    BuiltInPlaylist.Offline -> {

                        songFlow = Database.songsOffline( sortBy, sortOrder )
                        dispatcher = Dispatchers.IO
                        filterCondition = { song ->
                            song.contentLength?.let {
                                binder?.cache?.isCached(song.song.id, 0, song.contentLength)
                            } ?: false
                        }
                    }
                    BuiltInPlaylist.Downloaded -> {

                        val downloads = DownloadUtil.downloads.value

                        songFlow = Database.listAllSongsAsFlow()
                        dispatcher = Dispatchers.IO
                        filterCondition = { song ->
                            downloads[song.song.id]?.state == Download.STATE_COMPLETED
                        }
                    }
                    BuiltInPlaylist.Top -> {

                        songFlow =
                            if (topPlaylistPeriod.duration == Duration.INFINITE)
                                Database.songsEntityByPlayTimeWithLimitDesc(limit = maxTopPlaylistItems.number.toInt())
                            else
                                Database.trendingSongEntity(
                                    limit = maxTopPlaylistItems.number.toInt(),
                                    period = topPlaylistPeriod.duration.inWholeMilliseconds
                                )

                        filterCondition = { songs ->
                            if (excludeSongWithDurationLimit == DurationInMinutes.Disabled)
                                true
                            else
                                songs.song.durationText?.let {
                                    durationTextToMillis(it)
                                }!! < excludeSongWithDurationLimit.minutesInMilliSeconds
                        }
                    }
                    else -> {}
                }

                songFlow.flowOn( dispatcher )
                        .map { it.filter( filterCondition ) }
                        .collect {
                            items = if( autoShuffle ) it.shuffled() else it
                        }
            }
        }
        BuiltInPlaylist.OnDevice -> {
            items = emptyList()
            LaunchedEffect(sortByOnDevice, sortOrder) {
                if (hasPermission)
                    context.musicFilesAsFlow(sortByOnDevice, sortOrder, context)
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
                deviceFolderSort.sortByState.value,
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
                    SongSortBy.Title -> items = items.sortedBy { it.song.title }
                    SongSortBy.PlayTime -> items = items.sortedBy { it.song.totalPlayTimeMs }
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
                    SongSortBy.Title -> items = items.sortedByDescending { it.song.title }
                    SongSortBy.PlayTime -> items = items.sortedByDescending { it.song.totalPlayTimeMs }
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

    if( searchInput.isNotBlank() )
        if( builtInPlaylist == BuiltInPlaylist.OnDevice ) {
            filteredSongs = songs.filter {
                it.song.title.contains( searchInput, true )
                        || it.song.artistsText?.contains( searchInput, true ) ?: false
                        || it.albumTitle?.contains( searchInput, true ) ?: false
            }

            filteredFolders = folders.filter {
                it.name.contains( searchInput, true )
            }
        } else
            items = items.filter {
                it.song.title.contains( searchInput, true )
                        || it.song.artistsText?.contains( searchInput, true ) ?: false
                        || it.albumTitle?.contains( searchInput, true ) ?: false
            }
    /******** */

    val sortOrderIconRotation by animateFloatAsState(
        targetValue = if (sortOrder == SortOrder.Ascending) 0f else 180f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing), label = ""
    )

    val lazyListState = rememberLazyListState()

    var position by remember {
        mutableIntStateOf(0)
    }

    val queueLimit by remember { mutableStateOf(QueueSelection.END_OF_QUEUE_WINDOWED) }

    exportDialog.Render()
    downloadAllDialog.Render()
    deleteDownloadsDialog.Render()
    deleteSongDialog.Render()
    hideSongDialog.Render()

    Box(
        modifier = Modifier
            .background(colorPalette().background0)
            //.fillMaxSize()
            .fillMaxHeight()
            //.fillMaxWidth(if (navigationBarPosition == NavigationBarPosition.Left) 1f else Dimensions.contentWidthRightBar)
            .fillMaxWidth(
                if (NavigationBarPosition.Right.isCurrent())
                    Dimensions.contentWidthRightBar
                else
                    1f
            )
    ) {
        Column( Modifier.fillMaxSize() ) {
            // Sticky tab's title
            TabHeader( R.string.songs ) {
                val size =
                    if( builtInPlaylist == BuiltInPlaylist.OnDevice )
                        filteredSongs.size
                    else
                        items.size
                HeaderInfo( size.toString(), R.drawable.musical_notes )
            }

            // Sticky tab's tool bar
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(vertical = 4.dp)
                    .fillMaxWidth()
            ) {

                when( builtInPlaylist ) {
                    BuiltInPlaylist.Top -> {
                        TabToolBar.Icon(iconId = R.drawable.stat) {
                            menuState.display {
                                PeriodMenu(
                                    onDismiss = {
                                        topPlaylistPeriod = it
                                        menuState.hide()
                                    }
                                )
                            }
                        }
                    }
                    BuiltInPlaylist.OnDevice -> {
                        if( showFolders )
                            deviceFolderSort.ToolBarButton()
                        else
                            deviceSongSort.ToolBarButton()
                    }
                    else -> songSort.ToolBarButton()
                }

                search.ToolBarButton()

                TabToolBar.Icon(
                    iconId = R.drawable.locate,
                    tint = if (songs.isNotEmpty()) colorPalette().text else colorPalette().textDisabled,
                    enabled = songs.isNotEmpty(),
                    onShortClick = {
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
                )

                LaunchedEffect(scrollToNowPlaying) {
                    if (scrollToNowPlaying)
                        lazyListState.scrollToItem(nowPlayingItem, 1)
                    scrollToNowPlaying = false
                }

                if (builtInPlaylist == BuiltInPlaylist.Favorites)
                    downloadAllDialog.ToolBarButton()

                if (builtInPlaylist == BuiltInPlaylist.Favorites || builtInPlaylist == BuiltInPlaylist.Downloaded)
                    deleteDownloadsDialog.Render()

                TabToolBar.Toggleable(
                    onIconId = R.drawable.eye,
                    offIconId = R.drawable.eye_off,
                    toggleCondition = showHiddenSongs != 0,
                    onShortClick = {
                        showHiddenSongs = if (showHiddenSongs == 0) -1 else 0
                    },
                    onLongClick = {
                        SmartMessage(
                            context.resources.getString(R.string.info_show_hide_hidden_songs),
                            context = context
                        )
                    }
                )

                shuffle.ToolBarButton()

                if (builtInPlaylist == BuiltInPlaylist.Favorites)
                    TabToolBar.Icon(
                        iconId = R.drawable.random,
                        tint = if (autoShuffle) colorPalette().text else colorPalette().textDisabled,
                        onShortClick = { autoShuffle = !autoShuffle },
                        // TODO: Add string to language pack
                        onLongClick = { SmartMessage( "Random sorting", context = context) }
                    )
                else
                    importDialog.ToolBarButton()

                TabToolBar.Icon( R.drawable.ellipsis_horizontal ) {
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
                                    binder?.player?.addNext(
                                        items.map(SongEntity::asMediaItem),
                                        context
                                    )
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
                                    binder?.player?.enqueue(
                                        items.map(SongEntity::asMediaItem),
                                        context
                                    )
                                } else {
                                    binder?.player?.enqueue(listMediaItems, context)
                                    listMediaItems.clear()
                                    selectItems = false
                                }
                            },
                            onAddToPreferites = {
                                if (listMediaItems.isNotEmpty()) {
                                    listMediaItems.map {
                                        transaction {
                                            Database.like(
                                                it.mediaId,
                                                System.currentTimeMillis()
                                            )
                                        }
                                    }
                                } else {
                                    items.map {
                                        transaction {
                                            Database.like(
                                                it.asMediaItem.mediaId,
                                                System.currentTimeMillis()
                                            )
                                        }
                                    }
                                }
                            },
                            onAddToPlaylist = { playlistPreview ->
                                if (builtInPlaylist == BuiltInPlaylist.OnDevice) items =
                                    filteredSongs
                                position =
                                    playlistPreview.songCount.minus(1) ?: 0
                                if (position > 0) position++ else position = 0

                                items.forEachIndexed { index, song ->
                                    runCatching {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            Database.insert(song.song.asMediaItem)
                                            Database.insert(
                                                SongPlaylistMap(
                                                    songId = song.song.asMediaItem.mediaId,
                                                    playlistId = playlistPreview.playlist.id,
                                                    position = position + index
                                                )
                                            )
                                        }
                                    }.onFailure {
                                        Timber.e("Failed addToPlaylist in HomeSongsModern ${it.stackTraceToString()}")
                                        println("Failed addToPlaylist in HomeSongsModern ${it.stackTraceToString()}")
                                    }
                                }
                                CoroutineScope(Dispatchers.Main).launch {
                                    SmartMessage(
                                        context.resources.getString(R.string.done),
                                        type = PopupType.Success, context = context
                                    )
                                }
                            },
                            onExport = { exportToggleState.value = true }
                        )
                    }
                }
            }

            // Sticky search bar
            search.SearchBar( this )

            LazyColumn(
                state = lazyListState,
            ) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .padding(vertical = 4.dp)
                            .fillMaxWidth()
                    ) {
                        ButtonsRow(
                            chips = buttonsList,
                            currentValue = builtInPlaylist,
                            onValueUpdate = { builtInPlaylist = it },
                            modifier = Modifier.padding(end = 12.dp)
                        )
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
                                                            thumbnailSizeDp = thumbnailSizeDp
                                                        )
                                                    };
                                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                },
                                                onClick = {
                                                    currentFolderPath += folder.name + "/"

                                                    if (isSearchBarVisible)
                                                        if (searchInput.isBlank())
                                                            isSearchBarVisible = false
                                                        else
                                                            isSearchBarFocused = false
                                                }
                                            ),
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
                            SwipeablePlaylistItem(
                                mediaItem = song.asMediaItem,
                                onSwipeToRight = {
                                    binder?.player?.addNext(song.asMediaItem)
                                }
                            ) {
                                SongItem(
                                    song = song.song,
                                    isDownloaded = true,
                                    onDownloadClick = {
                                        // not necessary
                                    },
                                    downloadState = Download.STATE_COMPLETED,
                                    thumbnailSizeDp = thumbnailSizeDp,
                                    thumbnailSizePx = thumbnailSizePx,
                                    onThumbnailContent = {
                                        if (nowPlayingItem > -1)
                                            NowPlayingShow(song.asMediaItem.mediaId)
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
                                                        onDismiss = menuState::hide
                                                    )
                                                }
                                                hapticFeedback.performHapticFeedback(
                                                    HapticFeedbackType.LongPress
                                                )
                                            },
                                            onClick = {
                                                if (isSearchBarVisible)
                                                    if (searchInput.isBlank())
                                                        isSearchBarVisible = false
                                                    else
                                                        isSearchBarFocused = false

                                                if (!selectItems) {
                                                    binder?.stopRadio()
                                                    binder?.player?.forcePlayAtIndex(
                                                        filteredSongs.map(SongEntity::asMediaItem),
                                                        index
                                                    )
                                                }
                                            }
                                        )
                                        .animateItem(
                                            fadeInSpec = null,
                                            fadeOutSpec = null
                                        )
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

                        SwipeablePlaylistItem(
                            mediaItem = song.song.asMediaItem,
                            onSwipeToRight = {
                                binder?.player?.addNext(song.song.asMediaItem)
                            }
                        ) {
                            val isLocal by remember { derivedStateOf { song.song.asMediaItem.isLocal } }
                            downloadState = getDownloadState(song.song.asMediaItem.mediaId)
                            val isDownloaded =
                                if (!isLocal) downloadedStateMedia(song.song.asMediaItem.mediaId) else true
                            val checkedState = rememberSaveable { mutableStateOf(false) }
                            SongItem(
                                song = song.song,
                                isDownloaded = isDownloaded,
                                onDownloadClick = {
                                    binder?.cache?.removeResource(song.song.asMediaItem.mediaId)
                                    query {
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
                                            songId = song.song.id,
                                            songTitle = song.song.title,
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

                                    if (nowPlayingItem > -1)
                                        NowPlayingShow(song.song.asMediaItem.mediaId)

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
                                                    onDismiss = menuState::hide,
                                                    onHideFromDatabase = {
                                                        hideSongDialog.song = Optional.of( song )
                                                        hideSongToggleState.value = true
                                                    },
                                                    onDeleteFromDatabase = {
                                                        deleteSongDialog.song = Optional.of( song )
                                                        deleteSongToggleState.value = true
                                                    }
                                                )
                                            }
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        },
                                        onClick = {
                                            isSearchBarVisible = false
                                            searchInput = ""

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
                                                itemsLimited.map(SongEntity::asMediaItem),
                                                playIndex
                                            )
                                        }
                                    )
                                    .animateItemPlacement()
                            )
                        }
                    }
                }

                item(key = "bottom") {
                    Spacer(modifier = Modifier.height(Dimensions.bottomSpacer))
                }
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
