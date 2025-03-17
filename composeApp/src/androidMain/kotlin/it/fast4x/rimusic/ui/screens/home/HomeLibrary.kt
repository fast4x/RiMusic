package it.fast4x.rimusic.ui.screens.home

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import it.fast4x.compose.persist.persistList
import it.fast4x.environment.EnvironmentExt
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.MONTHLY_PREFIX
import it.fast4x.rimusic.PINNED_PREFIX
import it.fast4x.rimusic.PIPED_PREFIX
import it.fast4x.rimusic.R
import it.fast4x.rimusic.YTP_PREFIX
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.enums.PlaylistSortBy
import it.fast4x.rimusic.enums.PlaylistsType
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.models.Playlist
import it.fast4x.rimusic.models.PlaylistPreview
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.models.SongPlaylistMap
import it.fast4x.rimusic.ui.components.ButtonsRow
import it.fast4x.rimusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import it.fast4x.rimusic.ui.components.themed.HeaderInfo
import it.fast4x.rimusic.ui.components.themed.MultiFloatingActionsContainer
import it.fast4x.rimusic.ui.items.PlaylistItem
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.utils.CheckMonthlyPlaylist
import it.fast4x.rimusic.utils.ImportPipedPlaylists
import it.fast4x.rimusic.utils.asMediaItem
import it.fast4x.rimusic.utils.createPipedPlaylist
import it.fast4x.rimusic.utils.disableScrollingTextKey
import it.fast4x.rimusic.utils.enableCreateMonthlyPlaylistsKey
import it.fast4x.rimusic.utils.getPipedSession
import it.fast4x.rimusic.utils.isPipedEnabledKey
import it.fast4x.rimusic.utils.playlistSortByKey
import it.fast4x.rimusic.utils.playlistSortOrderKey
import it.fast4x.rimusic.utils.playlistTypeKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.showFloatingIconKey
import it.fast4x.rimusic.utils.showMonthlyPlaylistsKey
import it.fast4x.rimusic.utils.showPinnedPlaylistsKey
import it.fast4x.rimusic.utils.showPipedPlaylistsKey
import kotlinx.coroutines.flow.map
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.enums.ViewType
import it.fast4x.rimusic.getViewType
import it.fast4x.rimusic.models.SongAlbumMap
import it.fast4x.rimusic.models.SongArtistMap
import it.fast4x.rimusic.ui.components.PullToRefreshBox
import it.fast4x.rimusic.ui.components.themed.IDialog
import it.fast4x.rimusic.ui.components.themed.Search
import it.fast4x.rimusic.ui.components.navigation.header.TabToolBar
import it.fast4x.rimusic.ui.components.tab.ImportSongsFromCSV
import it.fast4x.rimusic.ui.components.tab.ItemSize
import it.fast4x.rimusic.ui.components.tab.Sort
import it.fast4x.rimusic.ui.components.tab.TabHeader
import it.fast4x.rimusic.ui.components.tab.toolbar.Descriptive
import it.fast4x.rimusic.ui.components.tab.toolbar.MenuIcon
import it.fast4x.rimusic.ui.components.tab.toolbar.SongsShuffle
import it.fast4x.rimusic.ui.screens.settings.isYouTubeSyncEnabled
import it.fast4x.rimusic.utils.importYTMPrivatePlaylists
import it.fast4x.rimusic.utils.Preference.HOME_LIBRARY_ITEM_SIZE
import it.fast4x.rimusic.utils.autoSyncToolbutton
import it.fast4x.rimusic.utils.autosyncKey
import it.fast4x.rimusic.utils.viewTypeToolbutton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@ExperimentalMaterial3Api
@UnstableApi
@ExperimentalMaterialApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun HomeLibrary(
    onPlaylistClick: (Playlist) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    // Essentials
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lazyGridState = rememberLazyGridState()

    // Non-vital
    val pipedSession = getPipedSession()
    var plistId by remember { mutableLongStateOf( 0L ) }
    var playlistType by rememberPreference(playlistTypeKey, PlaylistsType.Playlist)
    val isPipedEnabled by rememberPreference(isPipedEnabledKey, false)
    val disableScrollingText by rememberPreference(disableScrollingTextKey, false)

    var items by persistList<PlaylistPreview>("home/playlists")

    var itemsOnDisplay by persistList<PlaylistPreview>("home/playlists/on_display")

    // Dialog states
    val newPlaylistToggleState = remember { mutableStateOf( false ) }

    val search = Search.init()

    val sort = Sort.init(
        playlistSortOrderKey,
        PlaylistSortBy.entries,
        rememberPreference(playlistSortByKey, PlaylistSortBy.DateAdded)
    )

    val itemSize = ItemSize.init( HOME_LIBRARY_ITEM_SIZE )

    val shuffle = SongsShuffle.init {
        when( playlistType ) {
            PlaylistsType.Playlist -> Database.songsInAllPlaylists()
            PlaylistsType.PinnedPlaylist -> Database.songsInAllPinnedPlaylists()
            PlaylistsType.MonthlyPlaylist -> Database.songsInAllMonthlyPlaylists()
            PlaylistsType.PipedPlaylist -> Database.songsInAllPipedPlaylists()
            PlaylistsType.YTPlaylist -> Database.songsInAllYTPrivatePlaylists()
        }.map { it.map( Song::asMediaItem ) }
    }
    //<editor-fold desc="New playlist dialog">
    val newPlaylistDialog = object: IDialog, Descriptive, MenuIcon {

        override val messageId: Int = R.string.create_new_playlist
        override val iconId: Int = R.drawable.add_in_playlist
        override val dialogTitle: String
            @Composable
            get() = stringResource( R.string.enter_the_playlist_name )
        override val menuIconTitle: String
            @Composable
            get() = stringResource( messageId )

        override var isActive: Boolean = newPlaylistToggleState.value
            set(value) {
                newPlaylistToggleState.value = value
                field = value
            }

        override var value: String = ""

        override fun onShortClick() = super.onShortClick()

        override fun onSet(newValue: String) {
            if (isYouTubeSyncEnabled()) {
                CoroutineScope(Dispatchers.IO).launch {
                    EnvironmentExt.createPlaylist(newValue).getOrNull()
                        .also {
                            println("Innertube YtMusic createPlaylist: $it")
                            Database.asyncTransaction {
                                insert(Playlist(name = newValue, browseId = it, isYoutubePlaylist = true, isEditable = true))
                            }
                        }

                }
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    Database.asyncTransaction {
                        insert(Playlist(name = newValue))
                    }
                }
            }

            if ( isPipedEnabled && pipedSession.token.isNotEmpty() )
                createPipedPlaylist(
                    context = context,
                    coroutineScope = coroutineScope,
                    pipedSession = pipedSession.toApiSession(),
                    name = newValue
                )



            onDismiss()
        }

    }
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss")
    var time by remember {mutableStateOf("")}
    val formattedDate = currentDateTime.format(formatter)
    //</editor-fold>
    val importPlaylistDialog = ImportSongsFromCSV.init(
        beforeTransaction = { _, row ->
            time = formattedDate
            val playlistName = row["PlaylistName"] ?: "New Playlist $time"
            plistId = playlistName.let {
                Database.playlistExistByName( it )
            }

            if (plistId == 0L)
                plistId = playlistName.let {
                    Database.insert( Playlist( plistId, it, row["PlaylistBrowseId"] ) )
                }
        },
        afterTransaction = { index, song, album, artists ->
            if (song.id.isBlank()) return@init

            Database.insert(song)
            Database.insert(
                SongPlaylistMap(
                    songId = song.id,
                    playlistId = plistId,
                    position = index
                ).default()
            )

            if(album.id !=""){
                Database.insert(
                    album,
                    SongAlbumMap(
                        songId = song.id,
                        albumId = album.id,
                        position = null
                    )
                )
            }
            if(artists.isNotEmpty()){
                Database.insert(
                    artists,
                    artists.map{ artist->
                        SongArtistMap(
                            songId = song.id,
                            artistId = artist.id
                        )
                    }
                )
            }
        }
    )
    val sync = autoSyncToolbutton(R.string.autosync)

    val doAutoSync by rememberPreference(autosyncKey, false)
    var justSynced by rememberSaveable { mutableStateOf(!doAutoSync) }

    val viewType = viewTypeToolbutton(R.string.viewType)

    var refreshing by remember { mutableStateOf(false) }
    val refreshScope = rememberCoroutineScope()

    fun refresh() {
        if (refreshing) return
        refreshScope.launch(Dispatchers.IO) {
            refreshing = true
            justSynced = false
            delay(500)
            refreshing = false
        }
    }

    // START: Import YTM private playlists
    LaunchedEffect(justSynced, doAutoSync) {
        if ((!justSynced) && importYTMPrivatePlaylists())
            justSynced = true
    }

    // START: Import Piped playlists
    if (isPipedEnabled)
        ImportPipedPlaylists()

    LaunchedEffect( sort.sortBy, sort.sortOrder ) {
        Database.playlistPreviews(sort.sortBy, sort.sortOrder).collect { items = it }
    }
    LaunchedEffect( items, search.input ) {
        val scrollIndex = lazyGridState.firstVisibleItemIndex
        val scrollOffset = lazyGridState.firstVisibleItemScrollOffset

        itemsOnDisplay = items.filter {
            it.playlist.name.contains( search.input, true )
        }

        lazyGridState.scrollToItem( scrollIndex, scrollOffset )
    }

    // START: Additional playlists
    val showPinnedPlaylists by rememberPreference(showPinnedPlaylistsKey, true)
    val showMonthlyPlaylists by rememberPreference(showMonthlyPlaylistsKey, true)
    val showPipedPlaylists by rememberPreference(showPipedPlaylistsKey, true)

    val buttonsList = mutableListOf(PlaylistsType.Playlist to stringResource(R.string.playlists))
    buttonsList += PlaylistsType.YTPlaylist to stringResource(R.string.yt_playlists)
    if (showPipedPlaylists) buttonsList +=
        PlaylistsType.PipedPlaylist to stringResource(R.string.piped_playlists)
    if (showPinnedPlaylists) buttonsList +=
        PlaylistsType.PinnedPlaylist to stringResource(R.string.pinned_playlists)
    if (showMonthlyPlaylists) buttonsList +=
        PlaylistsType.MonthlyPlaylist to stringResource(R.string.monthly_playlists)
    // END - Additional playlists


    // START - New playlist
    newPlaylistDialog.Render()
    // END - New playlist

    // START - Monthly playlist
    val enableCreateMonthlyPlaylists by rememberPreference(enableCreateMonthlyPlaylistsKey, true)
    if (enableCreateMonthlyPlaylists)
        CheckMonthlyPlaylist()
    // END - Monthly playlist

    PullToRefreshBox(
        refreshing = refreshing,
        onRefresh = { refresh() }
    ) {
        Box(
            modifier = Modifier
                .background(colorPalette().background0)
                //.fillMaxSize()
                .fillMaxHeight()
                .fillMaxWidth(
                    if (NavigationBarPosition.Right.isCurrent())
                        Dimensions.contentWidthRightBar
                    else
                        1f
                )
        ) {
            Column( Modifier.fillMaxSize() ) {
                // Sticky tab's title
                TabHeader( R.string.playlists ) {
                    HeaderInfo( items.size.toString(), R.drawable.playlist )
                }

                // Sticky tab's tool bar
                TabToolBar.Buttons( sort, sync, search, shuffle, newPlaylistDialog, importPlaylistDialog, itemSize, viewType )

                // Sticky search bar
                search.SearchBar( this )

                if (getViewType() == ViewType.List) {
                    LazyColumn(
                        state = rememberLazyListState(),
                        modifier = Modifier

                    ) {
                        item(
                            key = "separator",
                            contentType = 0
                        ) {
                            ButtonsRow(
                                chips = buttonsList,
                                currentValue = playlistType,
                                onValueUpdate = { playlistType = it },
                                modifier = Modifier.padding(start = 12.dp, end = 12.dp)
                            )
                        }

                        val listPrefix =
                            when (playlistType) {
                                PlaylistsType.Playlist -> ""    // Matches everything
                                PlaylistsType.PinnedPlaylist -> PINNED_PREFIX
                                PlaylistsType.MonthlyPlaylist -> MONTHLY_PREFIX
                                PlaylistsType.PipedPlaylist -> PIPED_PREFIX
                                PlaylistsType.YTPlaylist -> YTP_PREFIX
                            }
                        val condition: (PlaylistPreview) -> Boolean = {
                            if (playlistType == PlaylistsType.YTPlaylist) {
                                it.playlist.isYoutubePlaylist
                            } else it.playlist.name.startsWith(listPrefix, true)
                        }
                        items(
                            items = itemsOnDisplay.filter(condition),
                            key = { it.playlist.id }
                        ) { preview ->
                            PlaylistItem(
                                playlist = preview,
                                thumbnailSizeDp = itemSize.size.dp,
                                thumbnailSizePx = itemSize.size.px,
                                homepage = true,
                                iconSize = itemSize.size.dp,
                                alternative = false,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .animateItem(fadeInSpec = null, fadeOutSpec = null)
                                    .clickable(onClick = {
                                        search.onItemSelected()
                                        onPlaylistClick(preview.playlist)
                                    }),
                                disableScrollingText = disableScrollingText,
                                isYoutubePlaylist = preview.playlist.isYoutubePlaylist,
                                isEditable = preview.playlist.isEditable
                            )
                        }

                        item(
                            key = "footer",
                            contentType = 0
                        ) {
                            Spacer(modifier = Modifier.height(Dimensions.bottomSpacer))
                        }

                    }
                } else {
                    LazyVerticalGrid(
                        state = lazyGridState,
                        columns = GridCells.Adaptive(itemSize.size.dp),
                        modifier = Modifier
                            .background(colorPalette().background0)
                    ) {
                        item(
                            key = "separator",
                            contentType = 0,
                            span = { GridItemSpan(maxLineSpan) }) {
                            ButtonsRow(
                                chips = buttonsList,
                                currentValue = playlistType,
                                onValueUpdate = { playlistType = it },
                                modifier = Modifier.padding(start = 12.dp, end = 12.dp)
                            )
                        }

                        val listPrefix =
                            when (playlistType) {
                                PlaylistsType.Playlist -> ""    // Matches everything
                                PlaylistsType.PinnedPlaylist -> PINNED_PREFIX
                                PlaylistsType.MonthlyPlaylist -> MONTHLY_PREFIX
                                PlaylistsType.PipedPlaylist -> PIPED_PREFIX
                                PlaylistsType.YTPlaylist -> YTP_PREFIX
                            }
                        val condition: (PlaylistPreview) -> Boolean = {
                            if (playlistType == PlaylistsType.YTPlaylist) {
                                it.playlist.isYoutubePlaylist
                            } else it.playlist.name.startsWith(listPrefix, true)
                        }
                        items(
                            items = itemsOnDisplay.filter(condition),
                            key = { it.playlist.id }
                        ) { preview ->
                            PlaylistItem(
                                playlist = preview,
                                thumbnailSizeDp = itemSize.size.dp,
                                thumbnailSizePx = itemSize.size.px,
                                alternative = true,
                                homepage = true,
                                iconSize = itemSize.size.dp,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .animateItem(fadeInSpec = null, fadeOutSpec = null)
                                    .clickable(onClick = {
                                        search.onItemSelected()
                                        onPlaylistClick(preview.playlist)
                                    }),
                                disableScrollingText = disableScrollingText,
                                isYoutubePlaylist = preview.playlist.isYoutubePlaylist,
                                isEditable = preview.playlist.isEditable
                            )
                        }

                        item(
                            key = "footer",
                            contentType = 0,
                            span = { GridItemSpan(maxLineSpan) }
                        ) {
                            Spacer(modifier = Modifier.height(Dimensions.bottomSpacer))
                        }

                    }
                }

            }

            FloatingActionsContainerWithScrollToTop(lazyGridState = lazyGridState)

            val showFloatingIcon by rememberPreference(showFloatingIconKey, false)
            if (UiType.ViMusic.isCurrent() && showFloatingIcon)
                MultiFloatingActionsContainer(
                    iconId = R.drawable.search,
                    onClick = onSearchClick,
                    onClickSettings = onSettingsClick,
                    onClickSearch = onSearchClick
                )
        }
    }
}