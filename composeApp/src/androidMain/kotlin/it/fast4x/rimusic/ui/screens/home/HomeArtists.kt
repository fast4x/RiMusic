package it.fast4x.rimusic.ui.screens.home


import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import it.fast4x.compose.persist.persistList
import it.fast4x.environment.EnvironmentExt
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.ArtistSortBy
import it.fast4x.rimusic.enums.ArtistsType
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.models.Artist
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.ui.components.ButtonsRow
import it.fast4x.rimusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import it.fast4x.rimusic.ui.components.themed.HeaderInfo
import it.fast4x.rimusic.ui.components.themed.MultiFloatingActionsContainer
import it.fast4x.rimusic.ui.items.ArtistItem
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.utils.artistSortByKey
import it.fast4x.rimusic.utils.artistSortOrderKey
import it.fast4x.rimusic.utils.artistTypeKey
import it.fast4x.rimusic.utils.asMediaItem
import it.fast4x.rimusic.utils.disableScrollingTextKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.showFloatingIconKey
import kotlinx.coroutines.flow.map
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.enums.FilterBy
import it.fast4x.rimusic.enums.ViewType
import it.fast4x.rimusic.getViewType
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.PullToRefreshBox
import it.fast4x.rimusic.ui.components.themed.Search
import it.fast4x.rimusic.ui.components.navigation.header.TabToolBar
import it.fast4x.rimusic.ui.components.tab.ItemSize
import it.fast4x.rimusic.ui.components.tab.Sort
import it.fast4x.rimusic.ui.components.tab.TabHeader
import it.fast4x.rimusic.ui.components.tab.toolbar.Randomizer
import it.fast4x.rimusic.ui.components.tab.toolbar.SongsShuffle
import it.fast4x.rimusic.ui.components.themed.FilterMenu
import it.fast4x.rimusic.ui.components.themed.HeaderIconButton
import it.fast4x.rimusic.ui.screens.settings.isYouTubeSyncEnabled
import it.fast4x.rimusic.ui.styling.LocalAppearance
import it.fast4x.rimusic.utils.Preference.HOME_ARTIST_ITEM_SIZE
import it.fast4x.rimusic.utils.autoSyncToolbutton
import it.fast4x.rimusic.utils.autosyncKey
import it.fast4x.rimusic.utils.filterByKey
import it.fast4x.rimusic.utils.importYTMSubscribedChannels
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.viewTypeToolbutton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@ExperimentalMaterial3Api
@UnstableApi
@ExperimentalMaterialApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun HomeArtists(
    onArtistClick: (Artist) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    // Essentials
    val lazyGridState = rememberLazyGridState()

    var items by persistList<Artist>( "")
    var itemsToFilter by persistList<Artist>( "home/artists" )

    var itemsOnDisplay by persistList<Artist>( "home/artists/on_display" )

    val disableScrollingText by rememberPreference(disableScrollingTextKey, false)

    val search = Search.init()

    val sort = Sort.init(
        artistSortOrderKey,
        ArtistSortBy.entries,
        rememberPreference(artistSortByKey, ArtistSortBy.DateAdded)
    )

    val itemSize = ItemSize.init( HOME_ARTIST_ITEM_SIZE )

    val randomizer = object: Randomizer<Artist> {
        override fun getItems(): List<Artist> = itemsOnDisplay
        override fun onClick(index: Int) = onArtistClick(itemsOnDisplay[index])

    }
    val shuffle = SongsShuffle.init {
        Database.songsInAllFollowedArtists().map{ it.map( Song::asMediaItem ) }
    }

    var artistType by rememberPreference(artistTypeKey, ArtistsType.Favorites )
    val buttonsList = ArtistsType.entries.map { it to it.textName }

    var filterBy by rememberPreference(filterByKey, FilterBy.All)
    val (colorPalette, typography) = LocalAppearance.current
    val menuState = LocalMenuState.current
    val coroutineScope = rememberCoroutineScope()

    if (!isYouTubeSyncEnabled()) {
        filterBy = FilterBy.All
    }

    LaunchedEffect( Unit, sort.sortBy, sort.sortOrder, artistType ) {
        when( artistType ) {
            ArtistsType.Favorites -> Database.artists( sort.sortBy, sort.sortOrder ).collect { itemsToFilter = it }
            ArtistsType.Library -> Database.artistsInLibrary( sort.sortBy, sort.sortOrder ).collect { itemsToFilter = it }
            //ArtistsType.All -> Database.artistsWithSongsSaved( sort.sortBy, sort.sortOrder ).collect { items = it }
        }

    }
    LaunchedEffect( Unit, itemsToFilter, filterBy ) {
        items = when(filterBy) {
            FilterBy.All -> itemsToFilter
            FilterBy.YoutubeLibrary -> itemsToFilter.filter { it.isYoutubeArtist }
            FilterBy.Local -> itemsToFilter.filterNot { it.isYoutubeArtist }
        }

    }
    LaunchedEffect( items, search.input ) {
        val scrollIndex = lazyGridState.firstVisibleItemIndex
        val scrollOffset = lazyGridState.firstVisibleItemScrollOffset

        itemsOnDisplay = items.filter {
            it.name?.contains( search.input, true ) ?: false
        }

        lazyGridState.scrollToItem( scrollIndex, scrollOffset )
    }
    if (items.any{it.thumbnailUrl == null}) {
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                items.filter { it.thumbnailUrl == null }.forEach { artist ->
                    coroutineScope.launch(Dispatchers.IO) {
                        val artistThumbnail = EnvironmentExt.getArtistPage(artist.id).getOrNull()?.artist?.thumbnail?.url
                        Database.asyncTransaction {
                            update(artist.copy(thumbnailUrl = artistThumbnail))
                        }
                    }
                }
            }
        }
    }

    val sync = autoSyncToolbutton(R.string.autosync_channels)

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

    // START: Import YTM subscribed channels
    LaunchedEffect(justSynced, doAutoSync) {
        if (!justSynced && importYTMSubscribedChannels())
                justSynced = true
    }

    PullToRefreshBox(
        refreshing = refreshing,
        onRefresh = { refresh() }
    ) {
        Box (
            modifier = Modifier
                .background(colorPalette().background0)
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
                TabHeader( R.string.artists ) {
                    HeaderInfo(items.size.toString(), R.drawable.artists)
                }

                // Sticky tab's tool bar
                TabToolBar.Buttons( sort, sync, search, randomizer, shuffle, itemSize, viewType )

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        //.padding(vertical = 4.dp)
                        .padding(bottom = 8.dp)
                        .fillMaxWidth()
                ) {
                    Box {
                        ButtonsRow(
                            chips = buttonsList,
                            currentValue = artistType,
                            onValueUpdate = { artistType = it },
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        if (isYouTubeSyncEnabled()) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                            ) {
                                BasicText(
                                    text = when (filterBy) {
                                        FilterBy.All -> stringResource(R.string.all)
                                        FilterBy.Local -> stringResource(R.string.on_device)
                                        FilterBy.YoutubeLibrary -> stringResource(R.string.ytm_library)
                                    },
                                    style = typography.xs.semiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .padding(end = 5.dp)
                                        .clickable {
                                            menuState.display {
                                                FilterMenu(
                                                    title = stringResource(R.string.filter_by),
                                                    onDismiss = menuState::hide,
                                                    onAll = { filterBy = FilterBy.All },
                                                    onYoutubeLibrary = {
                                                        filterBy = FilterBy.YoutubeLibrary
                                                    },
                                                    onLocal = { filterBy = FilterBy.Local }
                                                )
                                            }

                                        }
                                )
                                HeaderIconButton(
                                    icon = R.drawable.playlist,
                                    color = colorPalette.text,
                                    onClick = {},
                                    modifier = Modifier
                                        .offset(0.dp, 2.5.dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = {}
                                        )
                                )
                            }
                        }
                    }
                }

                // Sticky search bar
                search.SearchBar( this )

                if (getViewType() == ViewType.List) {
                    LazyColumn(
                        state = rememberLazyListState(),
                        modifier = Modifier
                    ) {
                        items(items = itemsOnDisplay, key = Artist::id) { artist ->
                            ArtistItem(
                                artist = artist,
                                thumbnailSizeDp = itemSize.size.dp,
                                thumbnailSizePx = itemSize.size.px,
                                homePage = true,
                                iconSize = itemSize.size.dp,
                                alternative = false,
                                modifier = Modifier
                                    .animateItem(
                                        fadeInSpec = null,
                                        fadeOutSpec = null
                                    )
                                    .clickable(onClick = {
                                        search.onItemSelected()
                                        onArtistClick(artist)
                                    }),
                                disableScrollingText = disableScrollingText,
                                isYoutubeArtist = artist.isYoutubeArtist
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        state = lazyGridState,
                        columns = GridCells.Adaptive(itemSize.size.dp),
                        modifier = Modifier
                            .background(colorPalette().background0)
                            .fillMaxSize(),
                        contentPadding = PaddingValues(bottom = Dimensions.bottomSpacer)
                    ) {
                        items(items = itemsOnDisplay, key = Artist::id) { artist ->
                            ArtistItem(
                                artist = artist,
                                thumbnailSizeDp = itemSize.size.dp,
                                thumbnailSizePx = itemSize.size.px,
                                homePage = true,
                                iconSize = itemSize.size.dp,
                                alternative = true,
                                modifier = Modifier
                                    .animateItem(
                                        fadeInSpec = null,
                                        fadeOutSpec = null
                                    )
                                    .clickable(onClick = {
                                        search.onItemSelected()
                                        onArtistClick(artist)
                                    }),
                                disableScrollingText = disableScrollingText,
                                isYoutubeArtist = artist.isYoutubeArtist
                            )
                        }
                    }
                }
            }

            FloatingActionsContainerWithScrollToTop(lazyGridState = lazyGridState)

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
}
