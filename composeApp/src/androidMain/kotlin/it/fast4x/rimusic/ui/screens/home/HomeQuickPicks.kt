package it.fast4x.rimusic.ui.screens.home

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
import it.fast4x.compose.persist.persist
import it.fast4x.compose.persist.persistList
import it.fast4x.innertube.Innertube
import it.fast4x.innertube.YtMusic
import it.fast4x.innertube.models.NavigationEndpoint
import it.fast4x.innertube.models.bodies.NextBody
import it.fast4x.innertube.requests.HomePage
import it.fast4x.innertube.requests.chartsPageComplete
import it.fast4x.innertube.requests.discoverPage
import it.fast4x.innertube.requests.relatedPage
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.EXPLICIT_PREFIX
import it.fast4x.rimusic.LocalPlayerAwareWindowInsets
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.Countries
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.enums.PlayEventsType
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.models.Artist
import it.fast4x.rimusic.models.PlaylistPreview
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.service.MyDownloadHelper
import it.fast4x.rimusic.service.isLocal
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.PullToRefreshBox
import it.fast4x.rimusic.ui.components.themed.HeaderWithIcon
import it.fast4x.rimusic.ui.components.themed.Loader
import it.fast4x.rimusic.ui.components.themed.Menu
import it.fast4x.rimusic.ui.components.themed.MenuEntry
import it.fast4x.rimusic.ui.components.themed.MultiFloatingActionsContainer
import it.fast4x.rimusic.ui.components.themed.NonQueuedMediaItemMenu
import it.fast4x.rimusic.ui.components.themed.Title
import it.fast4x.rimusic.ui.components.themed.Title2Actions
import it.fast4x.rimusic.ui.items.AlbumItem
import it.fast4x.rimusic.ui.items.ArtistItem
import it.fast4x.rimusic.ui.items.PlaylistItem
import it.fast4x.rimusic.ui.items.SongItem
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.px
import it.fast4x.rimusic.utils.WelcomeMessage
import it.fast4x.rimusic.utils.asMediaItem
import it.fast4x.rimusic.utils.asSong
import it.fast4x.rimusic.utils.bold
import it.fast4x.rimusic.utils.center
import it.fast4x.rimusic.utils.color
import it.fast4x.rimusic.utils.disableScrollingTextKey
import it.fast4x.rimusic.utils.forcePlay
import it.fast4x.rimusic.utils.getDownloadState
import it.fast4x.rimusic.utils.isDownloadedSong
import it.fast4x.rimusic.utils.isLandscape
import it.fast4x.rimusic.utils.isNowPlaying
import it.fast4x.rimusic.utils.loadedDataKey
import it.fast4x.rimusic.utils.manageDownload
import it.fast4x.rimusic.utils.parentalControlEnabledKey
import it.fast4x.rimusic.utils.playEventsTypeKey
import it.fast4x.rimusic.utils.quickPicsDiscoverPageKey
import it.fast4x.rimusic.utils.quickPicsRelatedPageKey
import it.fast4x.rimusic.utils.quickPicsTrendingSongKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.secondary
import it.fast4x.rimusic.utils.selectedCountryCodeKey
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.showChartsKey
import it.fast4x.rimusic.utils.showFloatingIconKey
import it.fast4x.rimusic.utils.showMonthlyPlaylistInQuickPicksKey
import it.fast4x.rimusic.utils.showMoodsAndGenresKey
import it.fast4x.rimusic.utils.showNewAlbumsArtistsKey
import it.fast4x.rimusic.utils.showNewAlbumsKey
import it.fast4x.rimusic.utils.showPlaylistMightLikeKey
import it.fast4x.rimusic.utils.showRelatedAlbumsKey
import it.fast4x.rimusic.utils.showSearchTabKey
import it.fast4x.rimusic.utils.showSimilarArtistsKey
import it.fast4x.rimusic.utils.showTipsKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.isVideoEnabled
import it.fast4x.rimusic.typography
import it.fast4x.rimusic.ui.components.ShimmerHost
import it.fast4x.rimusic.ui.components.themed.TextPlaceholder
import it.fast4x.rimusic.ui.components.themed.TitleMiniSection
import it.fast4x.rimusic.ui.components.themed.TitleSection
import it.fast4x.rimusic.ui.items.AlbumItemPlaceholder
import it.fast4x.rimusic.ui.items.PlaylistItemPlaceholder
import it.fast4x.rimusic.ui.items.SongItemPlaceholder
import it.fast4x.rimusic.ui.items.VideoItem
import it.fast4x.rimusic.ui.screens.settings.isYouTubeLoggedIn
import it.fast4x.rimusic.utils.playVideo
import it.fast4x.rimusic.utils.quickPicsHomePageKey
import timber.log.Timber
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days


@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalMaterialApi
@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun HomeQuickPicks(
    navController: NavController,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onMoodClick: (mood: Innertube.Mood.Item) -> Unit,
    onSettingsClick: () -> Unit
) {
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val windowInsets = LocalPlayerAwareWindowInsets.current
    var playEventType by rememberPreference(playEventsTypeKey, PlayEventsType.MostPlayed)

    var trending by persist<Song?>("home/trending")
    val trendingInit by persist<Song?>(tag = "home/trending")
    var trendingPreference by rememberPreference(quickPicsTrendingSongKey, trendingInit)

    var relatedPageResult by persist<Result<Innertube.RelatedPage?>?>(tag = "home/relatedPageResult")
    var relatedInit by persist<Innertube.RelatedPage?>(tag = "home/relatedPage")
    var relatedPreference by rememberPreference(quickPicsRelatedPageKey, relatedInit)

    var discoverPageResult by persist<Result<Innertube.DiscoverPage?>>("home/discoveryAlbums")
    var discoverPageInit by persist<Innertube.DiscoverPage>("home/discoveryAlbums")
    var discoverPagePreference by rememberPreference(quickPicsDiscoverPageKey, discoverPageInit)

    var homePageResult by persist<Result<HomePage?>>("home/homePage")
    var homePageInit by persist<HomePage?>("home/homePage")
    var homePagePreference by rememberPreference(quickPicsHomePageKey, homePageInit)

    var chartsPageResult by persist<Result<Innertube.ChartsPage?>>("home/chartsPage")
    var chartsPageInit by persist<Innertube.ChartsPage>("home/chartsPage")
//    var chartsPagePreference by rememberPreference(quickPicsChartsPageKey, chartsPageInit)



    var preferitesArtists by persistList<Artist>("home/artists")

    var localMonthlyPlaylists by persistList<PlaylistPreview>("home/monthlyPlaylists")
    LaunchedEffect(Unit) {
        Database.monthlyPlaylistsPreview("").collect { localMonthlyPlaylists = it }
    }

    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }

    val context = LocalContext.current


    val showRelatedAlbums by rememberPreference(showRelatedAlbumsKey, true)
    val showSimilarArtists by rememberPreference(showSimilarArtistsKey, true)
    val showNewAlbumsArtists by rememberPreference(showNewAlbumsArtistsKey, true)
    val showPlaylistMightLike by rememberPreference(showPlaylistMightLikeKey, true)
    val showMoodsAndGenres by rememberPreference(showMoodsAndGenresKey, true)
    val showNewAlbums by rememberPreference(showNewAlbumsKey, true)
    val showMonthlyPlaylistInQuickPicks by rememberPreference(
        showMonthlyPlaylistInQuickPicksKey,
        true
    )
    val showTips by rememberPreference(showTipsKey, true)
    val showCharts by rememberPreference(showChartsKey, true)

    val refreshScope = rememberCoroutineScope()
    val now = System.currentTimeMillis()
    val last50Year: Duration = 18250.days
    val from = last50Year.inWholeMilliseconds

    var selectedCountryCode by rememberPreference(selectedCountryCodeKey, Countries.ZZ)

    val parentalControlEnabled by rememberPreference(parentalControlEnabledKey, false)

    //var loadedData by rememberSaveable { mutableStateOf(false) }
    var loadedData by rememberPreference(loadedDataKey, false)

    suspend fun loadData() {

        //Used to refresh chart when country change
        if (showCharts)
            chartsPageResult =
                Innertube.chartsPageComplete(countryCode = selectedCountryCode.name)

        if (loadedData) return

        runCatching {
            refreshScope.launch(Dispatchers.IO) {
                when (playEventType) {
                    PlayEventsType.MostPlayed ->
                        Database.songsMostPlayedByPeriod(from, now, 1).distinctUntilChanged()
                            .collect { songs ->
                                val song = songs.firstOrNull()
                                if (relatedPageResult == null || trending?.id != song?.id) {
                                    relatedPageResult = Innertube.relatedPage(
                                        NextBody(
                                            videoId = (song?.id ?: "HZnNt9nnEhw")
                                        )
                                    )
                                }
                                trending = song
                            }

                    PlayEventsType.LastPlayed, PlayEventsType.CasualPlayed -> {
                        val numSongs = if (playEventType == PlayEventsType.LastPlayed) 3 else 100
                        Database.lastPlayed(numSongs).distinctUntilChanged().collect { songs ->
                            val song =
                                if (playEventType == PlayEventsType.LastPlayed) songs.firstOrNull()
                                else songs.shuffled().firstOrNull()
                            if (relatedPageResult == null || trending?.id != song?.id) {
                                relatedPageResult =
                                    Innertube.relatedPage(
                                        NextBody(
                                            videoId = (song?.id ?: "HZnNt9nnEhw")
                                        )
                                    )
                            }
                            trending = song
                        }
                    }

                }
            }

            if (showNewAlbums || showNewAlbumsArtists || showMoodsAndGenres) {
                discoverPageResult = Innertube.discoverPage()
            }

            if (isYouTubeLoggedIn())
                homePageResult = YtMusic.getHomePage()

        }.onFailure {
            Timber.e("Failed loadData in QuickPicsModern ${it.stackTraceToString()}")
            println("Failed loadData in QuickPicsModern ${it.stackTraceToString()}")
            loadedData = false
        }.onSuccess {
            Timber.d("Success loadData in QuickPicsModern")
            println("Success loadData in QuickPicsModern")
            loadedData = true
        }
    }

    LaunchedEffect(Unit, playEventType, selectedCountryCode) {
        loadData()
    }

    var refreshing by remember { mutableStateOf(false) }

    fun refresh() {
        if (refreshing) return
        loadedData = false
        relatedPageResult = null
        relatedInit = null
        trending = null
        refreshScope.launch(Dispatchers.IO) {
            refreshing = true
            loadData()
            delay(500)
            refreshing = false
        }
    }


    LaunchedEffect(Unit) {
        Database.preferitesArtistsByName().collect { preferitesArtists = it }
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

    val showSearchTab by rememberPreference(showSearchTabKey, false)

    val downloadedSongs = remember {
        MyDownloadHelper.downloads.value.filter {
            it.value.state == Download.STATE_COMPLETED
        }.keys.toList()
    }
    val cachedSongs = remember {
        binder?.cache?.keys?.toMutableList()
    }
    cachedSongs?.addAll(downloadedSongs)

    val hapticFeedback = LocalHapticFeedback.current

    val disableScrollingText by rememberPreference(disableScrollingTextKey, false)

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

                /*   Load data from url or from saved preference   */
                if (trendingPreference != null) {
                    when (loadedData) {
                        true -> trending = trendingPreference
                        else -> trendingPreference = trending
                    }
                } else trendingPreference = trending

                if (relatedPreference != null) {
                    when (loadedData) {
                        true -> {
                            relatedPageResult = Result.success(relatedPreference)
                            relatedInit = relatedPageResult?.getOrNull()
                        }
                        else -> {
                            relatedInit = relatedPageResult?.getOrNull()
                            relatedPreference = relatedInit
                        }
                    }
                } else {
                    relatedInit = relatedPageResult?.getOrNull()
                    relatedPreference = relatedInit
                }

                if (discoverPagePreference != null) {
                    when (loadedData) {
                        true -> {
                            discoverPageResult = Result.success(discoverPagePreference)
                            discoverPageInit = discoverPageResult?.getOrNull()
                        }
                        else -> {
                            discoverPageInit = discoverPageResult?.getOrNull()
                            discoverPagePreference = discoverPageInit
                        }

                    }
                } else {
                    discoverPageInit = discoverPageResult?.getOrNull()
                    discoverPagePreference = discoverPageInit
                }

                // Not saved/cached to preference
                chartsPageInit = chartsPageResult?.getOrNull()

                if (homePagePreference != null) {
                    when (loadedData) {
                        true -> {
                            homePageResult = Result.success(homePagePreference)
                            homePageInit = homePageResult?.getOrNull()
                        }
                        else -> {
                            homePageInit = homePageResult?.getOrNull()
                            homePagePreference = homePageInit
                        }

                    }
                } else {
                    homePageInit = homePageResult?.getOrNull()
                    homePagePreference = homePageInit
                }

                /*   Load data from url or from saved preference   */


                if (UiType.ViMusic.isCurrent())
                    HeaderWithIcon(
                        title = if (!isYouTubeLoggedIn()) stringResource(R.string.quick_picks)
                        else stringResource(R.string.home),
                        iconId = R.drawable.search,
                        enabled = true,
                        showIcon = !showSearchTab,
                        modifier = Modifier,
                        onClick = onSearchClick
                    )

                WelcomeMessage()

                if (showTips) {
                    Title2Actions(
                        title = stringResource(R.string.tips),
                        onClick1 = {
                            menuState.display {
                                Menu {
                                    MenuEntry(
                                        icon = R.drawable.chevron_up,
                                        text = stringResource(R.string.by_most_played_song),
                                        onClick = {
                                            playEventType = PlayEventsType.MostPlayed
                                            menuState.hide()
                                        }
                                    )
                                    MenuEntry(
                                        icon = R.drawable.chevron_down,
                                        text = stringResource(R.string.by_last_played_song),
                                        onClick = {
                                            playEventType = PlayEventsType.LastPlayed
                                            menuState.hide()
                                        }
                                    )
                                    MenuEntry(
                                        icon = R.drawable.random,
                                        text = stringResource(R.string.by_casual_played_song),
                                        onClick = {
                                            playEventType = PlayEventsType.CasualPlayed
                                            menuState.hide()
                                        }
                                    )
                                }
                            }
                        },
                        icon2 = R.drawable.play,
                        onClick2 = {
                            binder?.stopRadio()
                            trending?.let { binder?.player?.forcePlay(it.asMediaItem) }
                            binder?.player?.addMediaItems(relatedInit?.songs?.map { it.asMediaItem }
                                ?: emptyList())
                        }

                        //modifier = Modifier.fillMaxWidth(0.7f)
                    )

                    BasicText(
                        text = when (playEventType) {
                            PlayEventsType.MostPlayed -> stringResource(R.string.by_most_played_song)
                            PlayEventsType.LastPlayed -> stringResource(R.string.by_last_played_song)
                            PlayEventsType.CasualPlayed -> stringResource(R.string.by_casual_played_song)
                        },
                        style = typography().xxs.secondary,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 8.dp)
                    )




                    LazyHorizontalGrid(
                        state = quickPicksLazyGridState,
                        rows = GridCells.Fixed(if (relatedInit != null) 3 else 1),
                        flingBehavior = ScrollableDefaults.flingBehavior(),
                        contentPadding = endPaddingValues,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (relatedInit != null) Dimensions.itemsVerticalPadding * 3 * 9 else Dimensions.itemsVerticalPadding * 9)
                        //.height((songThumbnailSizeDp + Dimensions.itemsVerticalPadding * 2) * 4)
                    ) {
                        trending?.let { song ->
                            item {
                                val isLocal by remember { derivedStateOf { song.asMediaItem.isLocal } }
                                downloadState = getDownloadState(song.asMediaItem.mediaId)
                                val isDownloaded =
                                    if (!isLocal) isDownloadedSong(song.asMediaItem.mediaId) else true
                                var forceRecompose by remember { mutableStateOf(false) }
                                SongItem(
                                    song = song,
                                    onDownloadClick = {
                                        binder?.cache?.removeResource(song.asMediaItem.mediaId)
                                        CoroutineScope(Dispatchers.IO).launch {
                                            Database.deleteFormat( song.asMediaItem.mediaId )
                                        }


                                        if (!isLocal)
                                            manageDownload(
                                                context = context,
                                                mediaItem = song.asMediaItem,
                                                downloadState = isDownloaded
                                            )

                                    },
                                    downloadState = downloadState,
                                    thumbnailSizePx = songThumbnailSizePx,
                                    thumbnailSizeDp = songThumbnailSizeDp,
                                    trailingContent = {
                                        Image(
                                            painter = painterResource(R.drawable.star),
                                            contentDescription = null,
                                            colorFilter = ColorFilter.tint(colorPalette().accent),
                                            modifier = Modifier
                                                .size(16.dp)
                                        )
                                    },
                                    modifier = Modifier
                                        .combinedClickable(
                                            onLongClick = {
                                                menuState.display {
                                                    NonQueuedMediaItemMenu(
                                                        navController = navController,
                                                        onDismiss = {
                                                            menuState.hide()
                                                            forceRecompose = true
                                                        },
                                                        mediaItem = song.asMediaItem,
                                                        onRemoveFromQuickPicks = {
                                                            Database.asyncTransaction {
                                                                clearEventsFor(song.id)
                                                            }
                                                        },

                                                        onDownload = {
                                                            binder?.cache?.removeResource(song.asMediaItem.mediaId)
                                                            CoroutineScope(Dispatchers.IO).launch {
                                                                Database.deleteFormat(song.asMediaItem.mediaId)
                                                            }
                                                            manageDownload(
                                                                context = context,
                                                                mediaItem = song.asMediaItem,
                                                                downloadState = isDownloaded
                                                            )
                                                        },
                                                        disableScrollingText = disableScrollingText
                                                    )
                                                }
                                                hapticFeedback.performHapticFeedback(
                                                    HapticFeedbackType.LongPress
                                                )
                                            },
                                            onClick = {
                                                val mediaItem = song.asMediaItem
                                                binder?.stopRadio()
                                                binder?.player?.forcePlay(mediaItem)
                                                binder?.setupRadio(
                                                    NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId)
                                                )
                                            }
                                        )
                                        .animateItem(
                                            fadeInSpec = null,
                                            fadeOutSpec = null
                                        )
                                        .width(itemInHorizontalGridWidth),
                                    disableScrollingText = disableScrollingText,
                                    isNowPlaying = binder?.player?.isNowPlaying(song.id) ?: false,
                                    forceRecompose = forceRecompose
                                )
                            }
                        }

                        if (relatedInit != null) {
                            items(
                                items = relatedInit?.songs?.distinctBy { it.key }?.filter {
                                    if (cachedSongs != null) {
                                        cachedSongs.indexOf(it.asMediaItem.mediaId) < 0
                                    } else true
                                }
                                    ?.dropLast(if (trending == null) 0 else 1)
                                    ?: emptyList(),
                                key = Innertube.SongItem::key
                            ) { song ->
                                val isLocal by remember { derivedStateOf { song.asMediaItem.isLocal } }
                                downloadState = getDownloadState(song.asMediaItem.mediaId)
                                val isDownloaded =
                                    if (!isLocal) isDownloadedSong(song.asMediaItem.mediaId) else true
                                var forceRecompose by remember { mutableStateOf(false) }
                                SongItem(
                                    song = song,
                                    onDownloadClick = {
                                        binder?.cache?.removeResource(song.asMediaItem.mediaId)
                                        CoroutineScope(Dispatchers.IO).launch {
                                            Database.deleteFormat( song.asMediaItem.mediaId )
                                        }
                                        if (!isLocal)
                                            manageDownload(
                                                context = context,
                                                mediaItem = song.asMediaItem,
                                                downloadState = isDownloaded
                                            )

                                    },
                                    downloadState = downloadState,
                                    thumbnailSizePx = songThumbnailSizePx,
                                    thumbnailSizeDp = songThumbnailSizeDp,
                                    modifier = Modifier
                                        .animateItem(
                                            fadeInSpec = null,
                                            fadeOutSpec = null
                                        )
                                        .width(itemInHorizontalGridWidth)
                                        .combinedClickable(
                                            onLongClick = {
                                                menuState.display {
                                                    NonQueuedMediaItemMenu(
                                                        navController = navController,
                                                        onDismiss = {
                                                            menuState.hide()
                                                            forceRecompose = true
                                                        },
                                                        mediaItem = song.asMediaItem,
                                                        onDownload = {
                                                            binder?.cache?.removeResource(song.asMediaItem.mediaId)
                                                            CoroutineScope(Dispatchers.IO).launch {
                                                                Database.deleteFormat(song.asMediaItem.mediaId)
                                                            }
                                                            manageDownload(
                                                                context = context,
                                                                mediaItem = song.asMediaItem,
                                                                downloadState = isDownloaded
                                                            )
                                                        },
                                                        disableScrollingText = disableScrollingText
                                                    )
                                                }
                                                hapticFeedback.performHapticFeedback(
                                                    HapticFeedbackType.LongPress
                                                )
                                            },
                                            onClick = {
                                                val mediaItem = song.asMediaItem
                                                binder?.stopRadio()
                                                binder?.player?.forcePlay(mediaItem)
                                                binder?.setupRadio(
                                                    NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId)
                                                )
                                            }
                                        ),
                                    disableScrollingText = disableScrollingText,
                                    isNowPlaying = binder?.player?.isNowPlaying(song.key) ?: false,
                                    forceRecompose = forceRecompose
                                )
                            }
                        }
                    }

                    if (relatedInit == null) Loader()

                }


                discoverPageInit?.let { page ->

                    var newReleaseAlbumsFiltered by persistList<Innertube.AlbumItem>("discovery/newalbumsartist")
                    page.newReleaseAlbums.forEach { album ->
                        preferitesArtists.forEach { artist ->
                            if (artist.name == album.authors?.first()?.name) {
                                newReleaseAlbumsFiltered += album
                            }
                        }
                    }

                    if (showNewAlbumsArtists)
                        if (newReleaseAlbumsFiltered.isNotEmpty() && preferitesArtists.isNotEmpty()) {

                            BasicText(
                                text = stringResource(R.string.new_albums_of_your_artists),
                                style = typography().l.semiBold,
                                modifier = sectionTextModifier
                            )

                            LazyRow(contentPadding = endPaddingValues) {
                                items(
                                    items = newReleaseAlbumsFiltered.distinctBy { it.key },
                                    key = { it.key }) {
                                    AlbumItem(
                                        album = it,
                                        thumbnailSizePx = albumThumbnailSizePx,
                                        thumbnailSizeDp = albumThumbnailSizeDp,
                                        alternative = true,
                                        modifier = Modifier.clickable(onClick = {
                                            onAlbumClick(it.key)
                                        }),
                                        disableScrollingText = disableScrollingText
                                    )
                                }
                            }

                        }

                    if (showNewAlbums) {
                        Title(
                            title = stringResource(R.string.new_albums),
                            onClick = { navController.navigate(NavRoutes.newAlbums.name) },
                            //modifier = Modifier.fillMaxWidth(0.7f)
                        )

                        LazyRow(contentPadding = endPaddingValues) {
                            items(
                                items = page.newReleaseAlbums.distinctBy { it.key },
                                key = { it.key }) {
                                AlbumItem(
                                    album = it,
                                    thumbnailSizePx = albumThumbnailSizePx,
                                    thumbnailSizeDp = albumThumbnailSizeDp,
                                    alternative = true,
                                    modifier = Modifier.clickable(onClick = {
                                        onAlbumClick(it.key)
                                    }),
                                    disableScrollingText = disableScrollingText
                                )
                            }
                        }
                    }
                }

                if (showRelatedAlbums)
                    relatedInit?.albums?.let { albums ->
                        BasicText(
                            text = stringResource(R.string.related_albums),
                            style = typography().l.semiBold,
                            modifier = sectionTextModifier
                        )

                        LazyRow(contentPadding = endPaddingValues) {
                            items(
                                items = albums.distinctBy { it.key },
                                key = Innertube.AlbumItem::key
                            ) { album ->
                                AlbumItem(
                                    album = album,
                                    thumbnailSizePx = albumThumbnailSizePx,
                                    thumbnailSizeDp = albumThumbnailSizeDp,
                                    alternative = true,
                                    modifier = Modifier
                                        .clickable(onClick = { onAlbumClick(album.key) }),
                                    disableScrollingText = disableScrollingText
                                )
                            }
                        }
                    }

                if (showSimilarArtists)
                    relatedInit?.artists?.let { artists ->
                        BasicText(
                            text = stringResource(R.string.similar_artists),
                            style = typography().l.semiBold,
                            modifier = sectionTextModifier
                        )

                        LazyRow(contentPadding = endPaddingValues) {
                            items(
                                items = artists.distinctBy { it.key },
                                key = Innertube.ArtistItem::key,
                            ) { artist ->
                                ArtistItem(
                                    artist = artist,
                                    thumbnailSizePx = artistThumbnailSizePx,
                                    thumbnailSizeDp = artistThumbnailSizeDp,
                                    alternative = true,
                                    modifier = Modifier
                                        .clickable(onClick = { onArtistClick(artist.key) }),
                                    disableScrollingText = disableScrollingText
                                )
                            }
                        }
                    }

                if (showPlaylistMightLike)
                    relatedInit?.playlists?.let { playlists ->
                        BasicText(
                            text = stringResource(R.string.playlists_you_might_like),
                            style = typography().l.semiBold,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(top = 24.dp, bottom = 8.dp)
                        )

                        LazyRow(contentPadding = endPaddingValues) {
                            items(
                                items = playlists.distinctBy { it.key },
                                key = Innertube.PlaylistItem::key,
                            ) { playlist ->
                                PlaylistItem(
                                    playlist = playlist,
                                    thumbnailSizePx = playlistThumbnailSizePx,
                                    thumbnailSizeDp = playlistThumbnailSizeDp,
                                    alternative = true,
                                    showSongsCount = false,
                                    modifier = Modifier
                                        .clickable(onClick = { onPlaylistClick(playlist.key) }),
                                    disableScrollingText = disableScrollingText
                                )
                            }
                        }
                    }



                if (showMoodsAndGenres)
                    discoverPageInit?.let { page ->

                        if (page.moods.isNotEmpty()) {

                            Title(
                                title = stringResource(R.string.moods_and_genres),
                                onClick = { navController.navigate(NavRoutes.moodsPage.name) },
                                //modifier = Modifier.fillMaxWidth(0.7f)
                            )

                            LazyHorizontalGrid(
                                state = moodAngGenresLazyGridState,
                                rows = GridCells.Fixed(4),
                                flingBehavior = ScrollableDefaults.flingBehavior(),
                                //flingBehavior = rememberSnapFlingBehavior(snapLayoutInfoProvider),
                                contentPadding = endPaddingValues,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    //.height((thumbnailSizeDp + Dimensions.itemsVerticalPadding * 8) * 8)
                                    .height(Dimensions.itemsVerticalPadding * 4 * 8)
                            ) {
                                items(
                                    items = page.moods.sortedBy { it.title },
                                    key = { it.endpoint.params ?: it.title }
                                ) {
                                    MoodItemColored(
                                        mood = it,
                                        onClick = { it.endpoint.browseId?.let { _ -> onMoodClick(it) } },
                                        modifier = Modifier
                                            //.width(itemWidth)
                                            .padding(4.dp)
                                    )
                                }
                            }

                        }
                    }

                if (showMonthlyPlaylistInQuickPicks)
                    localMonthlyPlaylists.let { playlists ->
                        if (playlists.isNotEmpty()) {
                            BasicText(
                                text = stringResource(R.string.monthly_playlists),
                                style = typography().l.semiBold,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 24.dp, bottom = 8.dp)
                            )

                            LazyRow(contentPadding = endPaddingValues) {
                                items(
                                    items = playlists.distinctBy { it.playlist.id },
                                    key = { it.playlist.id }
                                ) { playlist ->
                                    PlaylistItem(
                                        playlist = playlist,
                                        thumbnailSizeDp = playlistThumbnailSizeDp,
                                        thumbnailSizePx = playlistThumbnailSizePx,
                                        alternative = true,
                                        modifier = Modifier
                                            .animateItem(
                                                fadeInSpec = null,
                                                fadeOutSpec = null
                                            )
                                            .fillMaxSize()
                                            .clickable(onClick = { navController.navigate(route = "${NavRoutes.localPlaylist.name}/${playlist.playlist.id}") }),
                                        disableScrollingText = disableScrollingText,
                                        isYoutubePlaylist = playlist.playlist.isYoutubePlaylist,
                                        isEditable = playlist.playlist.isEditable
                                    )
                                }
                            }
                        }
                    }

                if (showCharts) {

                    chartsPageInit?.let { page ->

                        Title(
                            title = "${stringResource(R.string.charts)} (${selectedCountryCode.countryName})",
                            onClick = {
                                menuState.display {
                                    Menu {
                                        Countries.entries.forEach { country ->
                                            MenuEntry(
                                                icon = R.drawable.arrow_right,
                                                text = country.countryName,
                                                onClick = {
                                                    selectedCountryCode = country
                                                    menuState.hide()
                                                }
                                            )
                                        }
                                    }
                                }
                            },
                        )

                        page.playlists?.let { playlists ->
                            /*
                           BasicText(
                               text = stringResource(R.string.playlists),
                               style = typography().l.semiBold,
                               modifier = Modifier
                                   .padding(horizontal = 16.dp)
                                   .padding(top = 24.dp, bottom = 8.dp)
                           )
                             */

                            LazyRow(contentPadding = endPaddingValues) {
                                items(
                                    items = playlists.distinctBy { it.key },
                                    key = Innertube.PlaylistItem::key,
                                ) { playlist ->
                                    PlaylistItem(
                                        playlist = playlist,
                                        thumbnailSizePx = playlistThumbnailSizePx,
                                        thumbnailSizeDp = playlistThumbnailSizeDp,
                                        alternative = true,
                                        showSongsCount = false,
                                        modifier = Modifier
                                            .clickable(onClick = { onPlaylistClick(playlist.key) }),
                                        disableScrollingText = disableScrollingText
                                    )
                                }
                            }
                        }

                        page.songs?.let { songs ->
                            if (songs.isNotEmpty()) {
                                BasicText(
                                    text = stringResource(R.string.chart_top_songs),
                                    style = typography().l.semiBold,
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .padding(top = 24.dp, bottom = 8.dp)
                                )


                                LazyHorizontalGrid(
                                    rows = GridCells.Fixed(2),
                                    modifier = Modifier
                                        .height(130.dp)
                                        .fillMaxWidth(),
                                    state = chartsPageSongLazyGridState,
                                    flingBehavior = ScrollableDefaults.flingBehavior(),
                                ) {
                                    itemsIndexed(
                                        items = if (parentalControlEnabled)
                                            songs.filter {
                                                !it.asSong.title.startsWith(
                                                    EXPLICIT_PREFIX
                                                )
                                            }.distinctBy { it.key }
                                        else songs.distinctBy { it.key },
                                        key = { _, song -> song.key }
                                    ) { index, song ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(start = 16.dp)
                                        ) {
                                            BasicText(
                                                text = "${index + 1}",
                                                style = typography().l.bold.center.color(
                                                    colorPalette().text
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            SongItem(
                                                song = song,
                                                onDownloadClick = {},
                                                downloadState = Download.STATE_STOPPED,
                                                thumbnailSizePx = songThumbnailSizePx,
                                                thumbnailSizeDp = songThumbnailSizeDp,
                                                modifier = Modifier
                                                    .clickable(onClick = {
                                                        val mediaItem = song.asMediaItem
                                                        binder?.stopRadio()
                                                        binder?.player?.forcePlay(mediaItem)
                                                        binder?.player?.addMediaItems(songs.map { it.asMediaItem })
                                                    })
                                                    .width(itemWidth),
                                                disableScrollingText = disableScrollingText,
                                                isNowPlaying = binder?.player?.isNowPlaying(song.key) ?: false
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        page.artists?.let { artists ->
                            if (artists.isNotEmpty()) {
                                BasicText(
                                    text = stringResource(R.string.chart_top_artists),
                                    style = typography().l.semiBold,
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .padding(top = 24.dp, bottom = 8.dp)
                                )


                                LazyHorizontalGrid(
                                    rows = GridCells.Fixed(2),
                                    modifier = Modifier
                                        .height(130.dp)
                                        .fillMaxWidth(),
                                    state = chartsPageArtistLazyGridState,
                                    flingBehavior = ScrollableDefaults.flingBehavior(),
                                ) {
                                    itemsIndexed(
                                        items = artists.distinctBy { it.key },
                                        key = { _, artist -> artist.key }
                                    ) { index, artist ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(start = 16.dp)
                                        ) {
                                            BasicText(
                                                text = "${index + 1}",
                                                style = typography().l.bold.center.color(
                                                    colorPalette().text
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            ArtistItem(
                                                artist = artist,
                                                thumbnailSizePx = songThumbnailSizePx,
                                                thumbnailSizeDp = songThumbnailSizeDp,
                                                alternative = false,
                                                modifier = Modifier
                                                    .width(200.dp)
                                                    .clickable(onClick = { onArtistClick(artist.key) }),
                                                disableScrollingText = disableScrollingText
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }


                homePageInit?.let { page ->

                    page.sections.forEach {
                        if (it.items.isEmpty() || it.items.firstOrNull()?.key == null) return@forEach
                        println("homePage() in HomeYouTubeMusic sections: ${it.title} ${it.items.size}")
                        println("homePage() in HomeYouTubeMusic sections items: ${it.items}")

                        TitleMiniSection(it.label ?: "", modifier = Modifier.padding(horizontal = 16.dp).padding(top = 14.dp, bottom = 4.dp))
                        
                        BasicText(
                            text = it.title,
                            style = typography().l.semiBold.color(colorPalette().text),
                            modifier = Modifier.padding(horizontal = 16.dp).padding(vertical = 4.dp)
                        )
                        LazyRow(contentPadding = endPaddingValues) {
                            items(it.items) { item ->
                                when (item) {
                                    is Innertube.SongItem -> {
                                        println("Innertube homePage SongItem: ${item.info?.name}")
                                        SongItem(
                                            song = item,
                                            thumbnailSizePx = albumThumbnailSizePx,
                                            thumbnailSizeDp = albumThumbnailSizeDp,
                                            onDownloadClick = {},
                                            downloadState = Download.STATE_STOPPED,
                                            disableScrollingText = disableScrollingText,
                                            isNowPlaying = false,
                                            modifier = Modifier.clickable(onClick = {
                                                binder?.player?.forcePlay(item.asMediaItem)
                                            })
                                        )
                                    }

                                    is Innertube.AlbumItem -> {
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

                                    is Innertube.ArtistItem -> {
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

                                    is Innertube.PlaylistItem -> {
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

                                    is Innertube.VideoItem -> {
                                        println("Innertube homePage VideoItem: ${item.info?.name}")
                                        VideoItem(
                                            video = item,
                                            thumbnailHeightDp = playlistThumbnailSizeDp,
                                            thumbnailWidthDp = playlistThumbnailSizeDp,
                                            disableScrollingText = disableScrollingText,
                                            modifier = Modifier.clickable(onClick = {
                                                binder?.stopRadio()
                                                if (isVideoEnabled())
                                                    binder?.player?.playVideo(item.asMediaItem)
                                                else
                                                    binder?.player?.forcePlay(item.asMediaItem)
                                            })
                                        )
                                    }

                                    null -> {}
                                }

                            }
                        }
                    }
                } ?: if (!isYouTubeLoggedIn()) BasicText(
                    text = "Log in to your YTM account for more content",
                    style = typography().xs.center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(vertical = 32.dp)
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate(NavRoutes.settings.name)
                        }
                ) else {
                    ShimmerHost {
                        repeat(3) {
                            SongItemPlaceholder(
                                thumbnailSizeDp = songThumbnailSizeDp,
                            )
                        }

                        TextPlaceholder(modifier = sectionTextModifier)

                        Row {
                            repeat(2) {
                                AlbumItemPlaceholder(
                                    thumbnailSizeDp = albumThumbnailSizeDp,
                                    alternative = true
                                )
                            }
                        }

                        TextPlaceholder(modifier = sectionTextModifier)

                        Row {
                            repeat(2) {
                                PlaylistItemPlaceholder(
                                    thumbnailSizeDp = albumThumbnailSizeDp,
                                    alternative = true
                                )
                            }
                        }
                    }
                }






                Spacer(modifier = Modifier.height(Dimensions.bottomSpacer))


                //} ?:

                relatedPageResult?.exceptionOrNull()?.let {
                    BasicText(
                        text = stringResource(R.string.page_not_been_loaded),
                        style = typography().s.secondary.center,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(all = 16.dp)
                    )
                }

                /*
                if (related == null)
                    ShimmerHost {
                        repeat(3) {
                            SongItemPlaceholder(
                                thumbnailSizeDp = songThumbnailSizeDp,
                            )
                        }

                        TextPlaceholder(modifier = sectionTextModifier)

                        Row {
                            repeat(2) {
                                AlbumItemPlaceholder(
                                    thumbnailSizeDp = albumThumbnailSizeDp,
                                    alternative = true
                                )
                            }
                        }

                        TextPlaceholder(modifier = sectionTextModifier)

                        Row {
                            repeat(2) {
                                ArtistItemPlaceholder(
                                    thumbnailSizeDp = albumThumbnailSizeDp,
                                    alternative = true
                                )
                            }
                        }

                        TextPlaceholder(modifier = sectionTextModifier)

                        Row {
                            repeat(2) {
                                PlaylistItemPlaceholder(
                                    thumbnailSizeDp = albumThumbnailSizeDp,
                                    alternative = true
                                )
                            }
                        }
                    }
                 */


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


