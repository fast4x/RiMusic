package it.vfsfitvnm.vimusic.ui.screens.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import it.vfsfitvnm.compose.persist.PersistMapCleanup
import it.vfsfitvnm.compose.routing.RouteHandler
import it.vfsfitvnm.compose.routing.defaultStacking
import it.vfsfitvnm.compose.routing.defaultStill
import it.vfsfitvnm.compose.routing.defaultUnstacking
import it.vfsfitvnm.compose.routing.isStacking
import it.vfsfitvnm.compose.routing.isUnknown
import it.vfsfitvnm.compose.routing.isUnstacking
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.CheckUpdateState
import it.vfsfitvnm.vimusic.enums.HomeScreenTabs
import it.vfsfitvnm.vimusic.enums.StatisticsType
import it.vfsfitvnm.vimusic.models.SearchQuery
import it.vfsfitvnm.vimusic.models.toUiMood
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.ui.components.rememberBottomSheetState
import it.vfsfitvnm.vimusic.ui.components.themed.ConfirmationDialog
import it.vfsfitvnm.vimusic.ui.components.themed.Scaffold
import it.vfsfitvnm.vimusic.ui.screens.albumRoute
import it.vfsfitvnm.vimusic.ui.screens.artistRoute
import it.vfsfitvnm.vimusic.ui.screens.builtInPlaylistRoute
import it.vfsfitvnm.vimusic.ui.screens.builtinplaylist.BuiltInPlaylistScreen
import it.vfsfitvnm.vimusic.ui.screens.deviceListSongRoute
import it.vfsfitvnm.vimusic.ui.screens.globalRoutes
import it.vfsfitvnm.vimusic.ui.screens.homeRoute
import it.vfsfitvnm.vimusic.ui.screens.localPlaylistRoute
import it.vfsfitvnm.vimusic.ui.screens.localplaylist.LocalPlaylistScreen
import it.vfsfitvnm.vimusic.ui.screens.moodRoute
import it.vfsfitvnm.vimusic.ui.screens.playlist.PlaylistScreen
import it.vfsfitvnm.vimusic.ui.screens.playlistRoute
import it.vfsfitvnm.vimusic.ui.screens.search.SearchScreen
import it.vfsfitvnm.vimusic.ui.screens.search.SearchSheet
import it.vfsfitvnm.vimusic.ui.screens.searchResultRoute
import it.vfsfitvnm.vimusic.ui.screens.searchRoute
import it.vfsfitvnm.vimusic.ui.screens.searchTypeRoute
import it.vfsfitvnm.vimusic.ui.screens.searchresult.SearchResultScreen
import it.vfsfitvnm.vimusic.ui.screens.settings.SettingsScreen
import it.vfsfitvnm.vimusic.ui.screens.settingsRoute
import it.vfsfitvnm.vimusic.ui.screens.statisticsTypeRoute
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.utils.CheckAvailableNewVersion
import it.vfsfitvnm.vimusic.utils.checkUpdateStateKey
import it.vfsfitvnm.vimusic.utils.getEnum
import it.vfsfitvnm.vimusic.utils.homeScreenTabIndexKey
import it.vfsfitvnm.vimusic.utils.indexNavigationTabKey
import it.vfsfitvnm.vimusic.utils.isInvincibilityEnabledKey
import it.vfsfitvnm.vimusic.utils.pauseSearchHistoryKey
import it.vfsfitvnm.vimusic.utils.preferences
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.showSearchTabKey

const val PINNED_PREFIX = "pinned:"

@ExperimentalMaterialApi
@ExperimentalTextApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun HomeScreen(
    onPlaylistUrl: (String) -> Unit,
    openTabFromShortcut: Int
) {
    var showNewversionDialog by remember {
        mutableStateOf(true)
    }

    var checkUpdateState by rememberPreference(checkUpdateStateKey, CheckUpdateState.Disabled)

    val saveableStateHolder = rememberSaveableStateHolder()

    val preferences = LocalContext.current.preferences
    val showSearchTab by rememberPreference(showSearchTabKey, false)

    PersistMapCleanup("home/")

    RouteHandler(
        listenToGlobalEmitter = true,
        transitionSpec = {
            when {
                isStacking -> defaultStacking
                isUnstacking -> defaultUnstacking
                isUnknown -> when {
                    initialState.route == searchRoute && targetState.route == searchResultRoute -> defaultStacking
                    initialState.route == searchResultRoute && targetState.route == searchRoute -> defaultUnstacking
                    else -> defaultStill
                }

                else -> defaultStill
            }
        }
    ) {
        globalRoutes()

        settingsRoute {
            SettingsScreen()
        }

        localPlaylistRoute { playlistId ->
            LocalPlaylistScreen(
                playlistId = playlistId ?: error("playlistId cannot be null")
            )
        }

        builtInPlaylistRoute { builtInPlaylist ->
            BuiltInPlaylistScreen(
                builtInPlaylist = builtInPlaylist
            )
        }

        playlistRoute { browseId, params ->
            PlaylistScreen(
                browseId = browseId ?: "",
                params = params
            )
        }

        searchResultRoute { query ->
            SearchResultScreen(
                query = query,
                onSearchAgain = {
                    searchRoute(query)
                }
            )
        }

        searchRoute { initialTextInput ->
            val context = LocalContext.current

            SearchScreen(
                initialTextInput = initialTextInput,
                onSearch = { query ->
                    pop()
                    searchResultRoute(query)

                    if (!context.preferences.getBoolean(pauseSearchHistoryKey, false)) {
                        query {
                            Database.insert(SearchQuery(query = query))
                        }
                    }
                },
                onViewPlaylist = onPlaylistUrl,
                onDismiss = { homeRoute::global }
            )
        }

        host {

            var (tabIndex, onTabChanged) =
                when (openTabFromShortcut) {
                    -1 -> when (preferences.getEnum(indexNavigationTabKey, HomeScreenTabs.Default)) {
                            HomeScreenTabs.Default -> rememberPreference(homeScreenTabIndexKey,
                            HomeScreenTabs.QuickPics.index)
                          else -> remember {
                                mutableStateOf(preferences.getEnum(indexNavigationTabKey, HomeScreenTabs.QuickPics).index)
                          }
                        }
                    else -> remember { mutableStateOf(openTabFromShortcut) }
                }

            /*
            var (tabIndex, onTabChanged) =
                if (preferences.getEnum(indexNavigationTabKey, HomeScreenTabs.Default) == HomeScreenTabs.Default)
                    rememberPreference(
                        homeScreenTabIndexKey,
                        HomeScreenTabs.QuickPics.index
                    ) else
                    remember {
                        mutableStateOf(preferences.getEnum(indexNavigationTabKey, HomeScreenTabs.QuickPics).index)
                    }

             */

            Scaffold(
                topIconButtonId = R.drawable.settings,
                onTopIconButtonClick = { settingsRoute() },
                topIconButton2Id = R.drawable.stats_chart,
                onTopIconButton2Click = { statisticsTypeRoute(StatisticsType.Today) },
                showButton2 = true,
                showBottomButton = showSearchTab,
                onBottomIconButtonClick = { searchRoute("") },
                tabIndex = tabIndex,
                onTabChanged = onTabChanged,
                tabColumnContent = { Item ->
                    Item(0, stringResource(R.string.quick_picks), R.drawable.sparkles)
                    Item(1, stringResource(R.string.songs), R.drawable.musical_notes)
                    Item(2, stringResource(R.string.artists), R.drawable.artists)
                    Item(3, stringResource(R.string.albums), R.drawable.album)
                    Item(4, stringResource(R.string.library), R.drawable.library)
                    //Item(5, stringResource(R.string.discovery), R.drawable.megaphone)
                    //if (showSearchTab)
                    //Item(6, stringResource(R.string.search), R.drawable.search)
                    //Item(6, "Equalizer", R.drawable.musical_notes)
                    //Item(6, "Settings", R.drawable.equalizer)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> QuickPicks(
                            onAlbumClick = { albumRoute(it) },
                            onArtistClick = { artistRoute(it) },
                            onPlaylistClick = { playlistRoute(it) },
                            onSearchClick = { searchRoute("") },
                            onMoodClick = { mood -> moodRoute(mood.toUiMood()) },
                        )

                        1 -> HomeSongs(
                            onSearchClick = { searchRoute("") }
                        )

                        2 -> HomeArtistList(
                            onArtistClick = { artistRoute(it.id) },
                            onSearchClick = { searchRoute("") }
                        )

                        3 -> HomeAlbums(
                            onAlbumClick = { albumRoute(it.id) },
                            onSearchClick = { searchRoute("") }
                        )

                        4 -> HomePlaylists(
                            onBuiltInPlaylist = { builtInPlaylistRoute(it) },
                            onPlaylistClick = { localPlaylistRoute(it.id) },
                            onSearchClick = { searchRoute("") },
                            onDeviceListSongsClick = { deviceListSongRoute("") }
                        )

                        5 -> HomeDiscovery(
                            onMoodClick = { mood -> moodRoute(mood.toUiMood()) },
                            onNewReleaseAlbumClick = { albumRoute(it) },
                            onSearchClick = { searchRoute("") }
                        )

                        //6 -> HomeEqualizer( )
                        /*
                        5 -> HomeStatistics(
                            onStatisticsType = { statisticsTypeRoute(it)},
                            onBuiltInPlaylist = { builtInPlaylistRoute(it) },
                            onPlaylistClick = { localPlaylistRoute(it.id) },
                            onSearchClick = { searchRoute("") }
                        )
                         */

                        /*
                        6 -> HomeSearch(
                            onSearchType = { searchTypeRoute(it) }
                        )
                         */
                    }
                }
            }
        }
    }

    if (showNewversionDialog && checkUpdateState == CheckUpdateState.Enabled)
        CheckAvailableNewVersion(
            onDismiss = { showNewversionDialog = false }
        )

    if (checkUpdateState == CheckUpdateState.Ask)
        ConfirmationDialog(
            text = stringResource(R.string.check_at_github_for_updates) + "\n\n" +
                    stringResource(R.string.when_an_update_is_available_you_will_be_asked_if_you_want_to_install_info) + "\n\n" +
                    stringResource(R.string.but_these_updates_would_not_go_through) + "\n\n" +
                    stringResource(R.string.you_can_still_turn_it_on_or_off_from_the_settings),
            confirmText = stringResource(R.string.enable),
            cancelText = stringResource(R.string.don_t_enable),
            cancelBackgroundPrimary = true,
            onCancel = { checkUpdateState = CheckUpdateState.Disabled },
            onDismiss = { checkUpdateState = CheckUpdateState.Disabled },
            onConfirm = { checkUpdateState = CheckUpdateState.Enabled },
        )
    
}
