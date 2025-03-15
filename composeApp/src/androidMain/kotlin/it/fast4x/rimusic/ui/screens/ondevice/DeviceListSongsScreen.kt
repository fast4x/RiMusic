package it.fast4x.rimusic.ui.screens.ondevice

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import it.fast4x.compose.persist.PersistMapCleanup
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.BuiltInPlaylist
import it.fast4x.rimusic.enums.DeviceLists
import it.fast4x.rimusic.enums.MaxTopPlaylistItems
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.ui.screens.builtinplaylist.BuiltInPlaylistSongs
import it.fast4x.rimusic.utils.MaxTopPlaylistItemsKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.showCachedPlaylistKey
import it.fast4x.rimusic.utils.showDownloadedPlaylistKey
import it.fast4x.rimusic.utils.showFavoritesPlaylistKey
import it.fast4x.rimusic.utils.showMyTopPlaylistKey
import it.fast4x.rimusic.utils.showOnDevicePlaylistKey
import it.fast4x.rimusic.ui.components.Skeleton
import it.fast4x.rimusic.utils.showDislikedPlaylistKey

@ExperimentalMaterialApi
@ExperimentalTextApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun DeviceListSongsScreen(
    navController: NavController,
    deviceLists: DeviceLists,
    miniPlayer: @Composable () -> Unit = {},
) {
    val saveableStateHolder = rememberSaveableStateHolder()

    val (tabIndex, onTabIndexChanged) = rememberSaveable {
        mutableStateOf(when (deviceLists) {
            DeviceLists.LocalSongs -> 4
        })
    }

    val maxTopPlaylistItems by rememberPreference(
        MaxTopPlaylistItemsKey,
        MaxTopPlaylistItems.`10`
    )

    val showFavoritesPlaylist by rememberPreference(showFavoritesPlaylistKey, true)
    val showDislikedPlaylist by rememberPreference(showDislikedPlaylistKey, false)
    val showCachedPlaylist by rememberPreference(showCachedPlaylistKey, true)
    val showMyTopPlaylist by rememberPreference(showMyTopPlaylistKey, true)
    val showDownloadedPlaylist by rememberPreference(showDownloadedPlaylistKey, true)
    val showOnDevicePlaylist by rememberPreference(showOnDevicePlaylistKey, true)

    PersistMapCleanup(tagPrefix = "${deviceLists.name}/")

            Skeleton(
                navController,
                tabIndex,
                onTabIndexChanged,
                miniPlayer,
                navBarContent = { item ->
                    if(showFavoritesPlaylist)
                        item(0, stringResource(R.string.favorites), R.drawable.heart)
                    if(showCachedPlaylist)
                        item(1, stringResource(R.string.cached), R.drawable.sync)
                    if(showDownloadedPlaylist)
                        item(2, stringResource(R.string.downloaded), R.drawable.downloaded)
                    if(showMyTopPlaylist)
                        item(3, stringResource(R.string.my_playlist_top)  + " ${maxTopPlaylistItems.number}" , R.drawable.trending)
                    if(showOnDevicePlaylist)
                        item(4, stringResource(R.string.on_device), R.drawable.musical_notes)
                    if(showDislikedPlaylist)
                        item(5, stringResource(R.string.disliked), R.drawable.heart_dislike)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    val builtInPlaylist: BuiltInPlaylist =
                        when( currentTabIndex ) {
                            0 -> BuiltInPlaylist.Favorites
                            1 -> BuiltInPlaylist.Offline
                            2 -> BuiltInPlaylist.Downloaded
                            3 -> BuiltInPlaylist.Top
                            4 -> BuiltInPlaylist.OnDevice
                            else -> BuiltInPlaylist.Disliked
                        }

                    if( builtInPlaylist == BuiltInPlaylist.OnDevice )
                        DeviceListSongs(
                            navController = navController,
                            deviceLists = DeviceLists.LocalSongs,
                            onSearchClick = { navController.navigate(NavRoutes.search.name) }
                        )
                    else
                        BuiltInPlaylistSongs(
                            navController = navController,
                            builtInPlaylist = builtInPlaylist,
                            onSearchClick = { navController.navigate(NavRoutes.search.name) }
                        )
                }
            }
}
