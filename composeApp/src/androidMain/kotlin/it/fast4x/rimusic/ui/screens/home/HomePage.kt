package it.fast4x.rimusic.ui.screens.home

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
import it.fast4x.compose.persist.persist
import it.fast4x.environment.Environment
import it.fast4x.environment.EnvironmentExt
import it.fast4x.environment.requests.HomePage
import it.fast4x.rimusic.LocalPlayerAwareWindowInsets
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.Countries
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.PullToRefreshBox
import it.fast4x.rimusic.ui.components.themed.HeaderWithIcon
import it.fast4x.rimusic.ui.components.themed.MultiFloatingActionsContainer
import it.fast4x.rimusic.ui.items.AlbumItem
import it.fast4x.rimusic.ui.items.ArtistItem
import it.fast4x.rimusic.ui.items.PlaylistItem
import it.fast4x.rimusic.ui.items.SongItem
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.px
import it.fast4x.rimusic.utils.WelcomeMessage
import it.fast4x.rimusic.utils.disableScrollingTextKey
import it.fast4x.rimusic.utils.isLandscape
import it.fast4x.rimusic.utils.loadedDataKey
import it.fast4x.rimusic.utils.parentalControlEnabledKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.selectedCountryCodeKey
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.showFloatingIconKey
import it.fast4x.rimusic.utils.showSearchTabKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.typography
import it.fast4x.rimusic.ui.items.VideoItem
import it.fast4x.rimusic.utils.asMediaItem
import it.fast4x.rimusic.utils.forcePlay
import timber.log.Timber


@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalMaterialApi
@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun HomePage(
    navController: NavController,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onMoodClick: (mood: Environment.Mood.Item) -> Unit,
) {
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val windowInsets = LocalPlayerAwareWindowInsets.current


    var homePageResult by persist<Result<HomePage?>>("home/homePage")

    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }

    val context = LocalContext.current
    val refreshScope = rememberCoroutineScope()

    var selectedCountryCode by rememberPreference(selectedCountryCodeKey, Countries.ZZ)

    val parentalControlEnabled by rememberPreference(parentalControlEnabledKey, false)

    //var loadedData by rememberSaveable { mutableStateOf(false) }
    var loadedData by rememberPreference(loadedDataKey, false)

    suspend fun loadData() {

        homePageResult = EnvironmentExt.getHomePage()

        if (loadedData) return

        runCatching {
            refreshScope.launch(Dispatchers.IO) {

            }

        }.onFailure {
            Timber.e("Failed loadData in HomePage ${it.stackTraceToString()}")
            println("Failed loadData in HomePage ${it.stackTraceToString()}")
            loadedData = false
        }.onSuccess {
            Timber.d("Success loadData in HomePage")
            println("Success loadData in HomePage")
            loadedData = true
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    var refreshing by remember { mutableStateOf(false) }

    fun refresh() {
        if (refreshing) return
        loadedData = false
        refreshScope.launch(Dispatchers.IO) {
            refreshing = true
            loadData()
            delay(500)
            refreshing = false
        }
    }

    val songThumbnailSizeDp = Dimensions.thumbnails.song
    val songThumbnailSizePx = songThumbnailSizeDp.px
    val albumThumbnailSizeDp = 108.dp
    val albumThumbnailSizePx = albumThumbnailSizeDp.px
    val artistThumbnailSizeDp = 92.dp
    val artistThumbnailSizePx = artistThumbnailSizeDp.px
    val playlistThumbnailSizeDp = 108.dp
    val playlistThumbnailSizePx = playlistThumbnailSizeDp.px

    val scrollState = rememberScrollState()
    val quickPicksLazyGridState = rememberLazyGridState()
    val moodAngGenresLazyGridState = rememberLazyGridState()
    val chartsPageSongLazyGridState = rememberLazyGridState()
    val chartsPageArtistLazyGridState = rememberLazyGridState()

    val endPaddingValues = windowInsets.only(WindowInsetsSides.End).asPaddingValues()

    val sectionTextModifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 24.dp, bottom = 8.dp)
        .padding(endPaddingValues)

    val hapticFeedback = LocalHapticFeedback.current

    val disableScrollingText by rememberPreference(disableScrollingTextKey, false)
    val showSearchTab by rememberPreference(showSearchTabKey, false)

    PullToRefreshBox(
        refreshing = refreshing,
        onRefresh = { refresh() }
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth(
                    if (NavigationBarPosition.Right.isCurrent())
                        Dimensions.contentWidthRightBar
                    else
                        1f
                )

        ) {
            val quickPicksLazyGridItemWidthFactor =
                if (isLandscape && maxWidth * 0.475f >= 320.dp) {
                    0.475f
                } else {
                    0.9f
                }
            val itemInHorizontalGridWidth = maxWidth * quickPicksLazyGridItemWidthFactor

            val moodItemWidthFactor =
                if (isLandscape && maxWidth * 0.475f >= 320.dp) 0.475f else 0.9f
            val itemWidth = maxWidth * moodItemWidthFactor

            Column(
                modifier = Modifier
                    .background(colorPalette().background0)
                    .fillMaxHeight()
                    .verticalScroll(scrollState)
            ) {

                if (UiType.ViMusic.isCurrent())
                    HeaderWithIcon(
                        title = "Home",
                        iconId = R.drawable.search,
                        enabled = true,
                        showIcon = !showSearchTab,
                        modifier = Modifier,
                        onClick = onSearchClick
                    )

                WelcomeMessage()


                //******* HOMEPAGE *************
                homePageResult?.getOrNull()?.sections?.forEach {
                    println("homePage() in HomeYouTubeMusic sections: ${it.title} ${it.items.size}")
                    BasicText(
                        text = it.title,
                        style = typography().l.semiBold,
                        modifier = sectionTextModifier
                    )
                    LazyRow(contentPadding = endPaddingValues) {
                        items(it.items) { item ->
                            when (item) {
                                is Environment.SongItem -> {
                                    println("Innertube homePage SongItem: ${item.info?.name}")
                                    SongItem(
                                        song = item,
                                        thumbnailSizePx = songThumbnailSizePx,
                                        thumbnailSizeDp = songThumbnailSizeDp,
                                        onDownloadClick = {},
                                        downloadState = Download.STATE_STOPPED,
                                        disableScrollingText = disableScrollingText,
                                        isNowPlaying = false,
                                        modifier = Modifier.clickable(onClick = {
                                            binder?.player?.forcePlay(item.asMediaItem)
                                        })
                                    )
                                }

                                is Environment.AlbumItem -> {
                                    println("Innertube homePage AlbumItem: ${item.info?.name}")
                                    AlbumItem(
                                        album = item,
                                        alternative = true,
                                        thumbnailSizePx = albumThumbnailSizePx,
                                        thumbnailSizeDp = albumThumbnailSizeDp,
                                        disableScrollingText = disableScrollingText,
                                        modifier = Modifier.clickable(onClick = {
                                            navController.navigate("${NavRoutes.album.name}/${item.key}")
                                        })

                                    )
                                }
                                is Environment.ArtistItem -> {
                                    println("Innertube homePage ArtistItem: ${item.info?.name}")
                                    ArtistItem(
                                        artist = item,
                                        thumbnailSizePx = artistThumbnailSizePx,
                                        thumbnailSizeDp = artistThumbnailSizeDp,
                                        disableScrollingText = disableScrollingText,
                                        modifier = Modifier.clickable(onClick = {
                                            navController.navigate("${NavRoutes.artist.name}/${item.key}")
                                        })
                                    )
                                }
                                is Environment.PlaylistItem -> {
                                    println("Innertube homePage PlaylistItem: ${item.info?.name}")
                                    PlaylistItem(
                                        playlist = item,
                                        alternative = true,
                                        thumbnailSizePx = playlistThumbnailSizePx,
                                        thumbnailSizeDp = playlistThumbnailSizeDp,
                                        disableScrollingText = disableScrollingText,
                                        modifier = Modifier.clickable(onClick = {
                                            navController.navigate("${NavRoutes.playlist.name}/${item.key}")
                                        })
                                    )
                                }
                                is Environment.VideoItem -> {
                                    println("Innertube homePage VideoItem: ${item.info?.name}")
                                    VideoItem(
                                        video = item,
                                        thumbnailHeightDp = playlistThumbnailSizeDp,
                                        thumbnailWidthDp = playlistThumbnailSizeDp,
                                        disableScrollingText = disableScrollingText
                                    )
                                }

                                null -> {}
                            }

                        }
                    }
                }
                //********************






            }


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


