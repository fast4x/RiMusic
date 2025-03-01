package it.fast4x.rimusic.ui.screens

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.cleanString
import it.fast4x.rimusic.enums.BuiltInPlaylist
import it.fast4x.rimusic.enums.DeviceLists
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.StatisticsType
import it.fast4x.rimusic.enums.ThumbnailRoundness
import it.fast4x.rimusic.enums.TransitionEffect
import it.fast4x.rimusic.extensions.games.pacman.Pacman
import it.fast4x.rimusic.extensions.games.snake.SnakeGame
import it.fast4x.rimusic.models.Mood
import it.fast4x.rimusic.models.SearchQuery
import it.fast4x.rimusic.ui.components.CustomModalBottomSheet
import it.fast4x.rimusic.ui.screens.album.AlbumScreen
import it.fast4x.rimusic.ui.screens.artist.ArtistOverviewItems
import it.fast4x.rimusic.ui.screens.artist.ArtistScreen
import it.fast4x.rimusic.ui.screens.artist.ArtistScreenModern
import it.fast4x.rimusic.ui.screens.builtinplaylist.BuiltInPlaylistScreen
import it.fast4x.rimusic.ui.screens.history.HistoryScreen
import it.fast4x.rimusic.ui.screens.home.HomeScreen
import it.fast4x.rimusic.ui.screens.localplaylist.LocalPlaylistScreen
import it.fast4x.rimusic.ui.screens.mood.MoodScreen
import it.fast4x.rimusic.ui.screens.mood.MoodsPageScreen
import it.fast4x.rimusic.ui.screens.newreleases.NewreleasesScreen
import it.fast4x.rimusic.ui.screens.ondevice.DeviceListSongsScreen
import it.fast4x.rimusic.ui.screens.player.Player
import it.fast4x.rimusic.ui.screens.player.Queue
import it.fast4x.rimusic.ui.screens.playlist.PlaylistScreen
import it.fast4x.rimusic.ui.screens.podcast.PodcastScreen
import it.fast4x.rimusic.ui.screens.search.SearchScreen
import it.fast4x.rimusic.ui.screens.searchresult.SearchResultScreen
import it.fast4x.rimusic.ui.screens.settings.SettingsScreen
import it.fast4x.rimusic.ui.screens.statistics.StatisticsScreen
import it.fast4x.rimusic.utils.ShowVideoOrSongInfo
import it.fast4x.rimusic.utils.clearPreference
import it.fast4x.rimusic.utils.homeScreenTabIndexKey
import it.fast4x.rimusic.utils.pauseSearchHistoryKey
import it.fast4x.rimusic.utils.preferences
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.thumbnailRoundnessKey
import it.fast4x.rimusic.utils.transitionEffectKey

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class,
    ExperimentalMaterialApi::class, ExperimentalTextApi::class, ExperimentalComposeUiApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun AppNavigation(
    navController: NavHostController,
    miniPlayer: @Composable () -> Unit = {},
    openTabFromShortcut: Int
) {
    val transitionEffect by rememberPreference(transitionEffectKey, TransitionEffect.Scale)

    @Composable
    fun customScaffold(content: @Composable () -> Unit) {
        Scaffold(
            bottomBar = {  }
        ) { paddingValues ->
            Surface(
                modifier = Modifier.padding(paddingValues),
                content = content
            )
        }
    }

    @Composable
    fun modalBottomSheetPage(content: @Composable () -> Unit) {
        var showSheet by rememberSaveable { mutableStateOf(true) }
        val thumbnailRoundness by rememberPreference(
            thumbnailRoundnessKey,
            ThumbnailRoundness.Heavy
        )

        CustomModalBottomSheet(
            showSheet = showSheet,
            onDismissRequest = {
                if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED)
                    navController.popBackStack()
            },
            containerColor = Color.Transparent,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = {
                Surface(
                    modifier = Modifier.padding(vertical = 0.dp),
                    color = Color.Transparent,
                    //shape = thumbnailShape
                ) {}
            },
            shape = thumbnailRoundness.shape()
        ) {
            content()
        }
    }

    // Clearing homeScreenTabIndex in opening app.
    val context = LocalContext.current
    clearPreference(context, homeScreenTabIndexKey)

    NavHost(
        navController = navController,
        startDestination = NavRoutes.home.name,
        enterTransition = {
            when (transitionEffect) {
                TransitionEffect.None -> EnterTransition.None
                TransitionEffect.Expand -> expandIn(animationSpec = tween(350, easing = LinearOutSlowInEasing), expandFrom = Alignment.TopStart)
                TransitionEffect.Fade -> fadeIn(animationSpec = tween(350))
                TransitionEffect.Scale -> scaleIn(animationSpec = tween(350))
                TransitionEffect.SlideVertical -> slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up)
                TransitionEffect.SlideHorizontal -> slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left)
            }
        },
        exitTransition = {
            when (transitionEffect) {
                TransitionEffect.None -> ExitTransition.None
                TransitionEffect.Expand -> shrinkOut(animationSpec = tween(350, easing = FastOutSlowInEasing),shrinkTowards = Alignment.TopStart)
                TransitionEffect.Fade -> fadeOut(animationSpec = tween(350))
                TransitionEffect.Scale -> scaleOut(animationSpec = tween(350))
                TransitionEffect.SlideVertical -> slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down)
                TransitionEffect.SlideHorizontal -> slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right)
            }
        },
        popEnterTransition = {
            when (transitionEffect) {
                TransitionEffect.None -> EnterTransition.None
                TransitionEffect.Expand -> expandIn(animationSpec = tween(350, easing = LinearOutSlowInEasing), expandFrom = Alignment.TopStart)
                TransitionEffect.Fade -> fadeIn(animationSpec = tween(350))
                TransitionEffect.Scale -> scaleIn(animationSpec = tween(350))
                TransitionEffect.SlideVertical -> slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up)
                TransitionEffect.SlideHorizontal -> slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left)
            }
        },
        popExitTransition = {
            when (transitionEffect) {
                TransitionEffect.None -> ExitTransition.None
                TransitionEffect.Expand -> shrinkOut(animationSpec = tween(350, easing = FastOutSlowInEasing),shrinkTowards = Alignment.TopStart)
                TransitionEffect.Fade -> fadeOut(animationSpec = tween(350))
                TransitionEffect.Scale -> scaleOut(animationSpec = tween(350))
                TransitionEffect.SlideVertical -> slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down)
                TransitionEffect.SlideHorizontal -> slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right)
            }
        }
    ) {
        val navigateToAlbum =
            { browseId: String -> navController.navigate(route = "${NavRoutes.album.name}/$browseId") }
        val navigateToArtist =
            { browseId: String -> navController.navigate("${NavRoutes.artist.name}/$browseId") }
        val navigateToPlaylist =
            { browseId: String -> navController.navigate("${NavRoutes.playlist.name}/$browseId") }
        val pop = {
            if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) navController.popBackStack()
        }



        composable(route = NavRoutes.home.name) {
            HomeScreen(
                navController = navController,
                onPlaylistUrl = navigateToPlaylist,
                miniPlayer = miniPlayer,
                openTabFromShortcut = openTabFromShortcut
            )
        }

        composable(route = NavRoutes.gamePacman.name) {
            modalBottomSheetPage {
                Pacman()
            }

        }

        composable(route = NavRoutes.gameSnake.name) {
            modalBottomSheetPage {
                SnakeGame()
            }

        }

        composable(
            route = "${NavRoutes.videoOrSongInfo.name}/{id}",
            arguments = listOf(
                navArgument(
                    name = "id",
                    builder = { type = NavType.StringType }
                )
            )
        ) { navBackStackEntry ->
            val id = navBackStackEntry.arguments?.getString("id") ?: ""
            modalBottomSheetPage {
                ShowVideoOrSongInfo(id)
            }
        }

        composable(route = NavRoutes.queue.name) {
            modalBottomSheetPage {
                Queue(
                    navController = navController,
                    onDismiss = {},
                    onDiscoverClick = {}
                )
            }
        }

        composable(route = NavRoutes.player.name) {
            modalBottomSheetPage {
                Player(
                    navController = navController,
                    onDismiss = {}
                )
            }
        }

        composable(
            route = "${NavRoutes.artist.name}/{id}",
            arguments = listOf(
                navArgument(
                    name = "id",
                    builder = { type = NavType.StringType }
                )
            )
        ) { navBackStackEntry ->
            val id = navBackStackEntry.arguments?.getString("id") ?: ""
            ArtistScreenModern(
                navController = navController,
                browseId = id,
                miniPlayer = miniPlayer,
            )
        }



        composable(
            route = "${NavRoutes.album.name}/{id}",
            arguments = listOf(
                navArgument(
                    name = "id",
                    builder = { type = NavType.StringType }
                )
            )
        ) { navBackStackEntry ->
            val id = navBackStackEntry.arguments?.getString("id") ?: ""
            AlbumScreen(
                navController = navController,
                browseId = id,
                miniPlayer = miniPlayer,
            )
        }

        composable(
            route = "${NavRoutes.playlist.name}/{id}",
            arguments = listOf(
                navArgument(
                    name = "id",
                    builder = { type = NavType.StringType }
                )
            )
        ) { navBackStackEntry ->
            val id = navBackStackEntry.arguments?.getString("id") ?: ""
            PlaylistScreen(
                navController = navController,
                browseId = id,
                params = null,
                miniPlayer = miniPlayer,
            )
        }

        composable(
            route = "${NavRoutes.podcast.name}/{id}",
            arguments = listOf(
                navArgument(
                    name = "id",
                    builder = { type = NavType.StringType }
                )
            )
        ) { navBackStackEntry ->
            val id = navBackStackEntry.arguments?.getString("id") ?: ""
            PodcastScreen(
                navController = navController,
                browseId = id,
                params = null,
                miniPlayer = miniPlayer,
            )
        }

        composable(route = NavRoutes.settings.name) {
            SettingsScreen(
                navController = navController,
                miniPlayer = miniPlayer,
            )
        }

        composable(route = NavRoutes.statistics.name) {
            StatisticsScreen(
                navController = navController,
                statisticsType = StatisticsType.Today,
                miniPlayer = miniPlayer,
            )
        }

        composable(route = NavRoutes.history.name) {
            HistoryScreen(
                navController = navController,
                miniPlayer = miniPlayer,

                )
        }

        /*
        composable(
            route = "settingsPage/{index}",
            arguments = listOf(
                navArgument(
                    name = "index",
                    builder = { type = NavType.IntType }
                )
            )
        ) { navBackStackEntry ->
            val index = navBackStackEntry.arguments?.getInt("index") ?: 0

            PlayerScaffold {
                SettingsPage(
                    section = SettingsSection.entries[index],
                    pop = popDestination
                )
            }
        }
         */

        composable(
            route = "${NavRoutes.search.name}?text={text}",
            arguments = listOf(
                navArgument(
                    name = "text",
                    builder = {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            )
        ) { navBackStackEntry ->
            val context = LocalContext.current
            val text = navBackStackEntry.arguments?.getString("text") ?: ""

            SearchScreen(
                navController = navController,
                miniPlayer = miniPlayer,
                initialTextInput = text,
                onViewPlaylist = {},
                //pop = popDestination,
                onSearch = { query ->
                    println("onSearch: $query")
                    navController.navigate(
                        route = "${NavRoutes.searchResults.name}/${
                            cleanString(
                                query
                            )
                        }"
                    )

                    if (!context.preferences.getBoolean(pauseSearchHistoryKey, false)) {
                        Database.asyncTransaction {
                            insert(SearchQuery(query = query))
                        }
                    }
                },

                )
        }

        composable(
            route = "${NavRoutes.searchResults.name}/{query}",
            arguments = listOf(
                navArgument(
                    name = "query",
                    builder = { type = NavType.StringType }
                )
            )
        ) { navBackStackEntry ->
            val query = navBackStackEntry.arguments?.getString("query") ?: ""

            SearchResultScreen(
                navController = navController,
                miniPlayer = miniPlayer,
                query = query,
                onSearchAgain = {}
            )
        }

        composable(
            route = "${NavRoutes.builtInPlaylist.name}/{index}",
            arguments = listOf(
                navArgument(
                    name = "index",
                    builder = { type = NavType.IntType }
                )
            )
        ) { navBackStackEntry ->
            val index = navBackStackEntry.arguments?.getInt("index") ?: 0

            BuiltInPlaylistScreen(
                navController = navController,
                builtInPlaylist = BuiltInPlaylist.entries[index],
                miniPlayer = miniPlayer,
            )
        }

        composable(
            route = "${NavRoutes.localPlaylist.name}/{id}",
            arguments = listOf(
                navArgument(
                    name = "id",
                    builder = { type = NavType.LongType }
                )
            )
        ) { navBackStackEntry ->
            val id = navBackStackEntry.arguments?.getLong("id") ?: 0L

            LocalPlaylistScreen(
                navController = navController,
                playlistId = id,
                miniPlayer = miniPlayer
            )
        }

        composable(
            route = NavRoutes.mood.name,
        ) { navBackStackEntry ->
            val mood: Mood? = navController.previousBackStackEntry?.savedStateHandle?.get("mood")
            if (mood != null) {
                MoodScreen(
                    navController = navController,
                    mood = mood,
                    miniPlayer = miniPlayer,
                )
            }
        }

        composable(
            route = NavRoutes.moodsPage.name
        ) { navBackStackEntry ->
            /*
            SimpleScaffold(navController = navController) {
                MoodsPage(
                    navController = navController
                )
            }
             */
            MoodsPageScreen(
                navController = navController
            )

        }

        composable(
            route = NavRoutes.onDevice.name
        ) { navBackStackEntry ->
            DeviceListSongsScreen(
                navController = navController,
                deviceLists = DeviceLists.LocalSongs,
                miniPlayer = miniPlayer,
            )
        }

        composable(
            route = NavRoutes.newAlbums.name
        ) { navBackStackEntry ->
            NewreleasesScreen(
                navController = navController,
                miniPlayer = miniPlayer,
            )
        }

        composable(
            "searchScreenRoute/{query}"
        ) { backStackEntry ->
            val context = LocalContext.current
            val query = backStackEntry.arguments?.getString("query")?: ""
            SearchScreen(
                navController = navController,
                miniPlayer = miniPlayer,
                initialTextInput = query ,
                onViewPlaylist = {},
                onSearch = { newQuery ->
                    navController.navigate(route = "${NavRoutes.searchResults.name}/${cleanString(newQuery)}")

                    if (!context.preferences.getBoolean(pauseSearchHistoryKey, false)) {
                        Database.asyncTransaction {
                            insert(SearchQuery(query = newQuery))
                        }
                    }
                },
            )
        }
    }
}