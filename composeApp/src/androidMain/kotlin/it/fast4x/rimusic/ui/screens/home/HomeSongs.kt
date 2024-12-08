package it.fast4x.rimusic.ui.screens.home

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
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
import it.fast4x.rimusic.enums.QueueSelection
import it.fast4x.rimusic.enums.SongSortBy
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.models.Folder
import it.fast4x.rimusic.models.OnDeviceSong
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.models.SongEntity
import it.fast4x.rimusic.service.LOCAL_KEY_PREFIX
import it.fast4x.rimusic.service.MyDownloadHelper
import it.fast4x.rimusic.service.isLocal
import it.fast4x.rimusic.ui.components.ButtonsRow
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.SwipeablePlaylistItem
import it.fast4x.rimusic.ui.components.themed.ConfirmationDialog
import it.fast4x.rimusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import it.fast4x.rimusic.ui.components.themed.FolderItemMenu
import it.fast4x.rimusic.ui.components.themed.HeaderInfo
import it.fast4x.rimusic.ui.components.themed.InHistoryMediaItemMenu
import it.fast4x.rimusic.ui.components.themed.MultiFloatingActionsContainer
import it.fast4x.rimusic.ui.components.themed.NowPlayingSongIndicator
import it.fast4x.rimusic.ui.components.themed.SecondaryTextButton
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.ui.items.FolderItem
import it.fast4x.rimusic.ui.items.SongItem
import it.fast4x.rimusic.ui.items.SongItemPlaceholder
import it.fast4x.rimusic.ui.screens.ondevice.musicFilesAsFlow
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.onOverlay
import it.fast4x.rimusic.ui.styling.overlay
import it.fast4x.rimusic.ui.styling.px
import it.fast4x.rimusic.utils.MaxTopPlaylistItemsKey
import it.fast4x.rimusic.utils.OnDeviceOrganize
import it.fast4x.rimusic.utils.addNext
import it.fast4x.rimusic.utils.asMediaItem
import it.fast4x.rimusic.utils.builtInPlaylistKey
import it.fast4x.rimusic.utils.center
import it.fast4x.rimusic.utils.color
import it.fast4x.rimusic.utils.defaultFolderKey
import it.fast4x.rimusic.utils.disableScrollingTextKey
import it.fast4x.rimusic.utils.durationTextToMillis
import it.fast4x.rimusic.utils.enqueue
import it.fast4x.rimusic.utils.excludeSongsWithDurationLimitKey
import it.fast4x.rimusic.utils.forcePlayAtIndex
import it.fast4x.rimusic.utils.getDownloadState
import it.fast4x.rimusic.utils.hasPermission
import it.fast4x.rimusic.utils.includeLocalSongsKey
import it.fast4x.rimusic.utils.isCompositionLaunched
import it.fast4x.rimusic.utils.isDownloadedSong
import it.fast4x.rimusic.utils.isNowPlaying
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.knighthat.appContext
import me.knighthat.colorPalette
import me.knighthat.component.Enqueue
import me.knighthat.component.ItemSelector
import me.knighthat.component.LikeSongs
import me.knighthat.component.PlayNext
import me.knighthat.component.PlaylistsMenu
import me.knighthat.component.Search
import me.knighthat.component.header.TabToolBar
import me.knighthat.component.screen.HiddenSongs
import me.knighthat.component.screen.PeriodSelector
import me.knighthat.component.screen.randomSort
import me.knighthat.component.tab.DelSongDialog
import me.knighthat.component.tab.DeleteHiddenSongsDialog
import me.knighthat.component.tab.ExportSongsToCSVDialog
import me.knighthat.component.tab.HideSongDialog
import me.knighthat.component.tab.ImportSongsFromCSV
import me.knighthat.component.tab.LocateComponent
import me.knighthat.component.tab.Sort
import me.knighthat.component.tab.TabHeader
import me.knighthat.component.tab.toolbar.Button
import me.knighthat.component.tab.toolbar.DelAllDownloadedDialog
import me.knighthat.component.tab.toolbar.DownloadAllDialog
import me.knighthat.component.tab.toolbar.SongsShuffle
import me.knighthat.thumbnailShape
import me.knighthat.typography
import timber.log.Timber
import java.util.Optional
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration


@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalTextApi::class
)
@UnstableApi
@Composable
fun HomeSongs(
    navController: NavController,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    // Essentials
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val thumbnailSizeDp = Dimensions.thumbnails.song
    val thumbnailSizePx = thumbnailSizeDp.px
    val lazyListState = rememberLazyListState()

    var items by persistList<SongEntity>( "home/songs" )
    var itemsOnDisplay by persistList<SongEntity>( "home/songs/on_display" )
    // List should be cleared when tab changed
    val selectedItems = remember { mutableListOf<SongEntity>() }

    fun getMediaItems() = selectedItems.ifEmpty { itemsOnDisplay }.map( SongEntity::asMediaItem )

    var isLoading by remember { mutableStateOf( false ) }

    val parentalControlEnabled by rememberPreference(parentalControlEnabledKey, false)
    val disableScrollingText by rememberPreference(disableScrollingTextKey, false)

    var builtInPlaylist by rememberPreference(
        builtInPlaylistKey,
        BuiltInPlaylist.Favorites
    )

    val context = LocalContext.current

    var includeLocalSongs by rememberPreference(includeLocalSongsKey, true)

    val maxTopPlaylistItems by rememberPreference(
        MaxTopPlaylistItemsKey,
        MaxTopPlaylistItems.`10`
    )

    /************ OnDeviceDev */
    val permission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
    else Manifest.permission.READ_EXTERNAL_STORAGE

    var relaunchPermission by remember { mutableStateOf(false) }

    var hasPermission by remember(isCompositionLaunched()) {
        mutableStateOf(context.applicationContext.hasPermission(permission))
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { hasPermission = it }
    )
    val backButtonFolder = Folder(stringResource(R.string.back))
    val showFolders by rememberPreference(showFoldersOnDeviceKey, true)

    var folders: List<Folder> = emptyList()

    val maxSongsInQueue  by rememberPreference(maxSongsInQueueKey, MaxSongs.`500`)

    // Non-vital
    val playlistNameState = remember { mutableStateOf( "" ) }

    // Update playlistNameState's value based on current builtInPlaylist
    LaunchedEffect( builtInPlaylist ) {
        playlistNameState.value =
            when (builtInPlaylist) {
                BuiltInPlaylist.All -> context.resources.getString(R.string.songs)
                BuiltInPlaylist.OnDevice -> context.resources.getString(R.string.on_device)
                BuiltInPlaylist.Favorites -> context.resources.getString(R.string.favorites)
                BuiltInPlaylist.Downloaded -> context.resources.getString(R.string.downloaded)
                BuiltInPlaylist.Offline -> context.resources.getString(R.string.cached)
                BuiltInPlaylist.Top -> context.resources.getString(R.string.playlist_top)
            }
    }

    val search = Search.init()

    val songSort = Sort.init(
        songSortOrderKey,
        SongSortBy.entries,
        rememberPreference(songSortByKey, SongSortBy.DateAdded)
    )
    val onDeviceSort = Sort.init(
        songSortOrderKey,
        OnDeviceSongSortBy.entries,
        rememberPreference(onDeviceSongSortByKey, OnDeviceSongSortBy.DateAdded)
    )
    val deviceFolderSort = Sort.init(
        songSortOrderKey,
        OnDeviceFolderSortBy.entries,
        rememberPreference(onDeviceFolderSortByKey, OnDeviceFolderSortBy.Title)
    )
    val shuffle = SongsShuffle.init{ flowOf( getMediaItems() ) }
    val import = ImportSongsFromCSV.init(
        afterTransaction = { _, song ->
            Database.upsert( song )
            Database.like( song.id, System.currentTimeMillis() )
        }
    )
    val exportDialog = ExportSongsToCSVDialog.init( playlistNameState, ::getMediaItems )
    val downloadAllDialog = DownloadAllDialog.init( ::getMediaItems )
    val deleteDownloadsDialog = DelAllDownloadedDialog.init( ::getMediaItems )
    val deleteSongDialog =  DelSongDialog.init()
    val hideSongDialog = HideSongDialog.init()
    val deleteHiddenSongs = DeleteHiddenSongsDialog.init()

    val locator = LocateComponent.init( lazyListState, ::getMediaItems )

    val randomSorter = randomSort()

    val hiddenSongs = HiddenSongs.init()

    val topPlaylists = PeriodSelector.init()

    //<editor-fold desc="Menu">
    val itemSelector = ItemSelector.init()
    LaunchedEffect( itemSelector.isActive ) {
        // Clears itemsOnDisplay when check boxes are disabled
        if( !itemSelector.isActive ) selectedItems.clear()
    }

    val playNext = PlayNext {
        getMediaItems().let {
            binder?.player?.addNext( it, appContext() )

            // Turn of selector clears the selected list
            itemSelector.isActive = false
        }
    }
    val enqueue = Enqueue {
        getMediaItems().let {
            binder?.player?.enqueue( it, appContext() )

            // Turn of selector clears the selected list
            itemSelector.isActive = false
        }
    }

    val addToFavorite = LikeSongs( ::getMediaItems )

    val addToPlaylist = PlaylistsMenu.init(
        navController,
        { getMediaItems() },
        { throwable, preview ->
            Timber.e( "Failed to add songs to playlist ${preview.playlist.name} on HomeSongs" )
            throwable.printStackTrace()
        },
        {
            // Turn of selector clears the selected list
            itemSelector.isActive = false
        }
    )
    //</editor-fold>

    val defaultFolder by rememberPreference(defaultFolderKey, "/")

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
        BuiltInPlaylist.Top to stringResource(R.string.my_playlist_top,maxTopPlaylistItems.number)
    if (showOnDevicePlaylist) buttonsList +=
        BuiltInPlaylist.OnDevice to stringResource(R.string.on_device)

    val excludeSongWithDurationLimit by rememberPreference(excludeSongsWithDurationLimitKey, DurationInMinutes.Disabled)
    val hapticFeedback = LocalHapticFeedback.current

    // This phrase loads all songs across types into [itemsOffShelve]
    // No filtration applied to this stage, only sort
    LaunchedEffect( builtInPlaylist, songSort.sortBy, songSort.sortOrder, hiddenSongs.isShown() ) {
        if( builtInPlaylist == BuiltInPlaylist.OnDevice ) return@LaunchedEffect

        // This variable will be set to false after filtration stage is completed
        isLoading = true

        when( builtInPlaylist ) {
            BuiltInPlaylist.All, BuiltInPlaylist.Downloaded -> {
                // I personally think [hiddenSongs.isShown()] should be default regardless
                val showHidden = if( builtInPlaylist == BuiltInPlaylist.All ) hiddenSongs.isShown() else 0

                Database.listAllSongs( songSort.sortBy, songSort.sortOrder, showHidden )
            }
            BuiltInPlaylist.Favorites -> Database.listFavoriteSongs( songSort.sortBy, songSort.sortOrder )
            BuiltInPlaylist.Offline -> Database.listOfflineSongs( songSort.sortBy, songSort.sortOrder )
            BuiltInPlaylist.Top -> {
                if (topPlaylists.period.duration == Duration.INFINITE)
                    Database.songsEntityByPlayTimeWithLimitDesc(limit = maxTopPlaylistItems.number.toInt())
                else
                    Database.trendingSongEntity(
                        limit = maxTopPlaylistItems.number.toInt(),
                        period = topPlaylists.period.duration.inWholeMilliseconds
                    )
            }
            BuiltInPlaylist.OnDevice -> flowOf()

        }.flowOn( Dispatchers.IO ).distinctUntilChanged().collect {
             /*
                 When [builtInPlaylist] goes from [BuiltInPlaylist.All] to [BuiltInPlaylist.Downloaded]
                 or vice versa, the list refuses to update because new list and [items] contain
                 the same items.
                 To counter this, we need to manually clear the list and update it
                 with a new one (with a little delay in between to prevent race condition)
             */
            if( it.containsAll( items ) ) {
                items = emptyList()
                delay( 100 )
            }

            items = it
        }
    }

    var songsDevice by remember {
        mutableStateOf(emptyList<OnDeviceSong>())
    }
    LaunchedEffect( builtInPlaylist, onDeviceSort.sortBy, onDeviceSort.sortOrder, hasPermission ) {
        if( builtInPlaylist != BuiltInPlaylist.OnDevice ) return@LaunchedEffect

        // This variable will be set to false after filtration stage is completed
        isLoading = true

        // [context] remains unchanged (because of **val**) during the lifecycle of this Composable
        context.musicFilesAsFlow( onDeviceSort.sortBy, onDeviceSort.sortOrder, context )
               .collect {
                   songsDevice = it.distinctBy( OnDeviceSong::id )
               }
    }
    if( builtInPlaylist == BuiltInPlaylist.OnDevice )
        if (showFolders) {
            with( OnDeviceOrganize ) {
                val organized = organizeSongsIntoFolders( songsDevice )
                currentFolder = getFolderByPath( organized, currentFolderPath )

                items = sortSongs(
                    deviceFolderSort.sortOrder,
                    deviceFolderSort.sortBy,
                    currentFolder?.songs
                        ?.map( OnDeviceSong::toSongEntity )
                        ?: emptyList()
                )

                folders = currentFolder?.subFolders?.toList() ?: emptyList()
                filteredFolders = folders
            }
        } else
            items = songsDevice.map( OnDeviceSong::toSongEntity )
    // This phrase will filter out songs depends on search inputs, and natural filter
    // parameters, such as get downloaded songs when [BuiltInPlaylist.Offline] is set.
    val naturalFilter: (SongEntity) -> Boolean =
        when( builtInPlaylist ) {
            BuiltInPlaylist.All -> { song ->
                !includeLocalSongs || !song.song.id.startsWith(LOCAL_KEY_PREFIX)
            }

            BuiltInPlaylist.Offline -> { song ->
                song.contentLength?.let {
                    binder?.cache?.isCached(song.song.id, 0, song.contentLength)
                } ?: false
            }

            BuiltInPlaylist.Downloaded -> { song ->
                val downloads = MyDownloadHelper.downloads.value
                downloads[song.song.id]?.state == Download.STATE_COMPLETED
            }

            BuiltInPlaylist.Top -> { songs ->
                if (excludeSongWithDurationLimit == DurationInMinutes.Disabled)
                    true
                else {
                    println("HomeSongs durationTextToMillis: ${songs.song.durationText?.let {
                        durationTextToMillis(
                            it
                        )
                    }}")

                    try {
                        songs.song.durationText?.let {
                            durationTextToMillis(it)
                        }!! < excludeSongWithDurationLimit.minutesInMilliSeconds
                    } catch (e: Exception) {
                        false
                    }

                }
            }

            else -> { _ -> true }
        }
    LaunchedEffect( items, search.input ) {
        // Don't set [isLoading] to true here, it'll make searching look weird

        itemsOnDisplay = withContext( Dispatchers.Default ) {
            items.distinctBy { it.song.id }
                .filter( naturalFilter )
                 .filter {
                     // Without cleaning, user can search explicit songs with "e:"
                     // I kinda want this to be a feature, but it seems unnecessary
                     val containsTitle = it.song.cleanTitle().contains(search.input, true)
                     val containsArtist = it.song.artistsText?.contains(search.input, true) ?: false
                     val containsAlbum = it.albumTitle?.contains(search.input, true) ?: false
                     val isExplicit = parentalControlEnabled && it.song.title.startsWith(EXPLICIT_PREFIX)

                     containsTitle || containsArtist || containsAlbum || isExplicit
                 }
        }

        /*
            [LazyListState] will try to keep the visible song at the top
            after search input has changed. This creates a weird effect
            that fools user to believe search results haven't change.

            To prevent it, always scroll the list to the top
         */
        lazyListState.scrollToItem( 0 )

        isLoading = false
    }
    // Filter folder on the side
    LaunchedEffect( builtInPlaylist, folders, search.input ) {
        filteredFolders = folders.filter {
            builtInPlaylist == BuiltInPlaylist.OnDevice && it.name.contains( search.input, true )
        }
    }

    val queueLimit by remember { mutableStateOf(QueueSelection.END_OF_QUEUE_WINDOWED) }

    exportDialog.Render()
    downloadAllDialog.Render()
    deleteDownloadsDialog.Render()
    deleteSongDialog.Render()
    hideSongDialog.Render()
    deleteHiddenSongs.Render()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var songRif by remember { mutableStateOf(Song(
        id = "",
        title = "",
        durationText = null,
        thumbnailUrl = null
    )) }
    if (showDeleteDialog) {
        ConfirmationDialog(
            text = stringResource(R.string.delete_song),
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                Database.asyncTransaction {
                    deleteSongFromPlaylists(songRif.id)
                    deleteFormat(songRif.id)
                    delete(songRif)
                }
                SmartMessage(
                    message = appContext().resources.getString(R.string.deleted),
                    context = appContext()
                )
                menuState.hide()
                showDeleteDialog = false
            }

        )
    }

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
                HeaderInfo( itemsOnDisplay.size.toString(), R.drawable.musical_notes )
            }

            // Sticky tab's tool bar
            TabToolBar.Buttons(
                mutableListOf<Button>().apply {
                    this.add(
                        when( builtInPlaylist ) {
                            BuiltInPlaylist.Top -> topPlaylists
                            BuiltInPlaylist.OnDevice -> {
                                if( showFolders )
                                    deviceFolderSort
                                else
                                    onDeviceSort
                            }
                            else -> songSort
                        }
                    )
                    this.add( search )
                    this.add( locator )
                    this.add( downloadAllDialog )
                    this.add( deleteDownloadsDialog )
                    //this.add( deleteSongDialog )
                    if (builtInPlaylist == BuiltInPlaylist.All)
                        this.add( hiddenSongs )
                    this.add( shuffle )
                    if (builtInPlaylist == BuiltInPlaylist.Favorites)
                        this.add( randomSorter )
                    this.add( itemSelector )
                    this.add( playNext )
                    this.add( enqueue )
                    this.add( addToFavorite )
                    this.add( addToPlaylist )
                    this.add( exportDialog )
                    this.add( import )
                    this.add( deleteHiddenSongs )
                }
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    //.padding(vertical = 4.dp)
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
            ) {
                ButtonsRow(
                    chips = buttonsList,
                    currentValue = builtInPlaylist,
                    onValueUpdate = { builtInPlaylist = it },
                    modifier = Modifier.padding(end = 12.dp)
                )
            }


            // Sticky search bar
            search.SearchBar( this )

            LazyColumn(
                state = lazyListState,
                contentPadding = PaddingValues( start = 8.dp, bottom = Dimensions.bottomSpacer ),
                userScrollEnabled = !isLoading // Effectively disable scroll (drag gesture) while loading
            ) {

                /*
                    On slower phones, having to load a large database will create a
                    subtle feeling of the app is not responding. This component
                    creates fake song card that notifies user that songs are loading.
                 */
                if( isLoading ) {
                    items(
                        count = 20,
                        key = { it }
                    ) { SongItemPlaceholder( thumbnailSizeDp ) }

                    return@LazyColumn
                }

                if( builtInPlaylist == BuiltInPlaylist.OnDevice && !hasPermission ) {
                    item( "OnDeviceSongsPermission" ) {
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
                                modifier = Modifier.fillMaxWidth( 0.75f ),
                                style = typography().xs.semiBold
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            SecondaryTextButton(
                                text = stringResource(R.string.open_permission_settings),
                                onClick = {
                                    context.startActivity(
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", context.packageName, null)
                                        }
                                    )
                                }
                            )

                        }
                    }
                    return@LazyColumn       // Return early to prevent other components from loading
                }

                if( builtInPlaylist == BuiltInPlaylist.OnDevice && showFolders ) {
                    if (currentFolder == null) {
                        item {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.CenterHorizontally)
                            ) {
                                BasicText(
                                    text = stringResource(R.string.folder_was_not_found),
                                    style = typography().xs.semiBold
                                )
                            }
                        }
                        return@LazyColumn   // Return early to prevent other components from loading
                    }

                    // Renders back button
                    item {
                        if (currentFolderPath == "/") return@item

                        fun back() {
                            currentFolderPath = currentFolderPath.removeSuffix("/").substringBeforeLast("/") + "/"
                        }

                        FolderItem(
                            folder = backButtonFolder,
                            thumbnailSizeDp = thumbnailSizeDp,
                            icon = R.drawable.chevron_back,
                            modifier = Modifier
                                .combinedClickable( onClick = ::back ),
                            disableScrollingText = disableScrollingText
                        )

                        BackHandler( onBack = ::back )
                    }

                    // Renders folders
                    items(
                        items = filteredFolders.distinctBy( Folder::fullPath ),
                        key = Folder::fullPath
                    ) {folder ->

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
                                        search.onItemSelected()
                                    }
                                ),
                            disableScrollingText = disableScrollingText
                        )
                    }
                }

                itemsIndexed(
                    items = itemsOnDisplay,
                    key = { _, song -> song.song.id }
                ) {index, song ->
                    val mediaItem = song.asMediaItem

                    SwipeablePlaylistItem(
                        mediaItem = mediaItem,
                        onSwipeToRight = { binder?.player?.addNext( mediaItem ) }
                    ) {
                        downloadAllDialog.state = getDownloadState( mediaItem.mediaId )

                        val isLocal by remember { derivedStateOf { mediaItem.isLocal } }
                        val isDownloaded = isLocal || isDownloadedSong( mediaItem.mediaId )
                        var forceRecompose by remember { mutableStateOf(false) }
                        SongItem(
                            song = song.song,
                            onDownloadClick = {
                                // Only allow action(s) on songs other than [BuiltInPlaylist.OnDevice]
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
                                if ( songSort.sortBy == SongSortBy.PlayTime || builtInPlaylist == BuiltInPlaylist.Top ) {
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
                                // It must watch for [selectedItems.size] for changes
                                // Otherwise, state will stay the same
                                val checkedState = remember( selectedItems.size ) {
                                    mutableStateOf( song in selectedItems )
                                }

                                if( itemSelector.isActive )
                                    Checkbox(
                                        checked = checkedState.value,
                                        onCheckedChange = {
                                            checkedState.value = it
                                            if ( it )
                                                selectedItems.add( song )
                                            else
                                                selectedItems.remove( song )
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = colorPalette().accent,
                                            uncheckedColor = colorPalette().text
                                        ),
                                        modifier = Modifier.scale( 0.7f )
                                    )
                            },
                            modifier = Modifier
                                .combinedClickable(
                                    onLongClick = {
                                        val hideAction =
                                            if (builtInPlaylist != BuiltInPlaylist.OnDevice) {
                                                {
                                                    hideSongDialog.song = Optional.of(song)
                                                    hideSongDialog.onShortClick()
                                                }
                                            } else null


                                        menuState.display {
                                            InHistoryMediaItemMenu(
                                                navController = navController,
                                                song = song.song,
                                                onDismiss = {
                                                    menuState.hide()
                                                    forceRecompose = true
                                                },
                                                onHideFromDatabase = hideAction,
                                                onDeleteFromDatabase = {
                                                    songRif = song.song
                                                    showDeleteDialog = true
                                                },
                                                disableScrollingText = disableScrollingText
                                            )
                                        }
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    onClick = {
                                        search.onItemSelected()

                                        val maxSongs = maxSongsInQueue.number.toInt()
                                        val itemsRange: IntRange
                                        val playIndex: Int
                                        if (itemsOnDisplay.size < maxSongsInQueue.number) {
                                            itemsRange = itemsOnDisplay.indices
                                            playIndex = index
                                        } else {
                                            when (queueLimit) {
                                                QueueSelection.START_OF_QUEUE -> {
                                                    // tries to guarantee maxSongs many songs
                                                    // window starting from index with maxSongs songs (if possible)
                                                    itemsRange = index..<min(
                                                        index + maxSongs,
                                                        itemsOnDisplay.size
                                                    )

                                                    // index is located at the first position
                                                    playIndex = 0
                                                }

                                                QueueSelection.CENTERED -> {
                                                    // tries to guarantee >= maxSongs/2 many songs
                                                    // window with +- maxSongs/2 songs (if possible) around index
                                                    val minIndex = max(0, index - maxSongs / 2)
                                                    val maxIndex =
                                                        min(
                                                            index + maxSongs / 2,
                                                            itemsOnDisplay.size
                                                        )
                                                    itemsRange = minIndex..<maxIndex

                                                    // index is located at "center"
                                                    playIndex = index - minIndex
                                                }

                                                QueueSelection.END_OF_QUEUE -> {
                                                    // tries to guarantee maxSongs many songs
                                                    // window with maxSongs songs (if possible) ending at index
                                                    val minIndex = max(0, index - maxSongs + 1)
                                                    val maxIndex = min(index, itemsOnDisplay.size)
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
                                                        min(
                                                            minIndex + maxSongs,
                                                            itemsOnDisplay.size
                                                        )
                                                    itemsRange = minIndex..<maxIndex

                                                    // index is located at "end"
                                                    playIndex = index - minIndex
                                                }
                                            }
                                        }
                                        val itemsLimited = itemsOnDisplay.slice(itemsRange)
                                        binder?.stopRadio()
                                        binder?.player?.forcePlayAtIndex(
                                            itemsLimited.map(SongEntity::asMediaItem),
                                            playIndex
                                        )
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
