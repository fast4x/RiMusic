package it.fast4x.rimusic.ui.screens.album

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.valentinilk.shimmer.shimmer
import it.fast4x.compose.persist.PersistMapCleanup
import it.fast4x.compose.persist.persist
import it.fast4x.environment.Environment
import it.fast4x.environment.EnvironmentExt
import it.fast4x.environment.requests.AlbumPage
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.MODIFIED_PREFIX
import it.fast4x.rimusic.R
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.PlayerPosition
import it.fast4x.rimusic.enums.ThumbnailRoundness
import it.fast4x.rimusic.enums.TransitionEffect
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.models.Album
import it.fast4x.rimusic.models.SongAlbumMap
import it.fast4x.rimusic.ui.components.navigation.header.AppHeader
import it.fast4x.rimusic.ui.components.themed.Header
import it.fast4x.rimusic.ui.components.themed.HeaderIconButton
import it.fast4x.rimusic.ui.components.themed.HeaderPlaceholder
import it.fast4x.rimusic.ui.components.themed.adaptiveThumbnailContent
import it.fast4x.rimusic.ui.items.AlbumItem
import it.fast4x.rimusic.ui.items.AlbumItemPlaceholder
import it.fast4x.rimusic.ui.screens.searchresult.ItemsPage
import it.fast4x.rimusic.ui.styling.px
import it.fast4x.rimusic.utils.asMediaItem
import it.fast4x.rimusic.utils.disableScrollingTextKey
import it.fast4x.rimusic.utils.playerPositionKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.thumbnailRoundnessKey
import it.fast4x.rimusic.utils.transitionEffectKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber


@ExperimentalMaterialApi
@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation", "SimpleDateFormat")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun AlbumScreen(
    navController: NavController,
    browseId: String,
    modifier: Modifier = Modifier,
    miniPlayer: @Composable () -> Unit = {}
) {

    //val uriHandler = LocalUriHandler.current
    val saveableStateHolder = rememberSaveableStateHolder()

    var tabIndex by rememberSaveable {
        mutableStateOf(0)
    }
    val thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )
    var changeShape by remember {
        mutableStateOf(false)
    }

    var album by persist<Album?>("album/$browseId/album")
    //var albumPage by persist<Innertube.PlaylistOrAlbumPage?>("album/$browseId/albumPage")
    var albumPage by persist<AlbumPage?>("album/$browseId/albumPage")

    val disableScrollingText by rememberPreference(disableScrollingTextKey, false)

    PersistMapCleanup(tagPrefix = "album/$browseId/")

    LaunchedEffect(Unit) {
        Database
            .album(browseId).collect { currentAlbum ->
                println("AlbumScreen collect ${currentAlbum?.title}")
                album = currentAlbum
                CoroutineScope(Dispatchers.IO).launch {
                    if (albumPage == null)
                        EnvironmentExt.getAlbum(browseId)
                            .onSuccess { currentAlbumPage ->
                                albumPage = currentAlbumPage

                                println("AlbumScreen otherVersion ${currentAlbumPage.otherVersions}")
                                Database.upsert(
                                    Album(
                                        id = browseId,
                                        title = album?.title ?: currentAlbumPage.album.title,
                                        thumbnailUrl = if (album?.thumbnailUrl?.startsWith(
                                                MODIFIED_PREFIX
                                            ) == true
                                        ) album?.thumbnailUrl else currentAlbumPage.album.thumbnail?.url,
                                        year = currentAlbumPage.album.year,
                                        authorsText = if (album?.authorsText?.startsWith(
                                                MODIFIED_PREFIX
                                            ) == true
                                        ) album?.authorsText else currentAlbumPage.album.authors
                                            ?.joinToString(", ") { it.name ?: "" },
                                        shareUrl = currentAlbumPage.url,
                                        timestamp = System.currentTimeMillis(),
                                        bookmarkedAt = album?.bookmarkedAt,
                                        isYoutubeAlbum = album?.isYoutubeAlbum == true
                                    ),
                                    currentAlbumPage
                                        .songs.distinct()
                                        .map(Environment.SongItem::asMediaItem)
                                        .onEach(Database::insert)
                                        .mapIndexed { position, mediaItem ->
                                            SongAlbumMap(
                                                songId = mediaItem.mediaId,
                                                albumId = browseId,
                                                position = position
                                            )
                                        }
                                )
                            }
                            .onFailure {
                                Timber.e("AlbumScreen error ${it.stackTraceToString()}")
//                            if (it.message?.contains("NOT_FOUND") == true) {
//                                // This album no longer exists in YouTube Music
//                                Database.asyncTransaction {
//                                    album?.let(::delete)
//                                }
//                            }
                            }
                }
            }

    }

//    LaunchedEffect(Unit) {
//        Database
//            .album(browseId)
//            .combine(snapshotFlow { tabIndex }) { album, tabIndex -> album to tabIndex }
//            .collect { (currentAlbum,
//                          // tabIndex
//            ) ->
//                album = currentAlbum
//
//                if (albumPage == null
//                    //&& (currentAlbum?.timestamp == null || tabIndex == 1)
//                    ) {
//
//                    withContext(Dispatchers.IO) {
//                        Innertube.albumPage(BrowseBody(browseId = browseId))
//                            ?.onSuccess { currentAlbumPage ->
//                                albumPage = currentAlbumPage
//
//                                println("mediaItem albumScreen ${currentAlbumPage.songsPage}")
//                                Database.upsert(
//                                    Album(
//                                        id = browseId,
//                                        title = if (album?.title?.startsWith(MODIFIED_PREFIX) == true) album?.title else currentAlbumPage?.title,
//                                        thumbnailUrl = if (album?.thumbnailUrl?.startsWith(MODIFIED_PREFIX) == true) album?.thumbnailUrl else currentAlbumPage?.thumbnail?.url,
//                                        year = currentAlbumPage?.year,
//                                        authorsText = if (album?.authorsText?.startsWith(MODIFIED_PREFIX) == true) album?.authorsText else currentAlbumPage?.authors
//                                            ?.joinToString("") { it.name ?: "" },
//                                        shareUrl = currentAlbumPage.url,
//                                        timestamp = System.currentTimeMillis(),
//                                        bookmarkedAt = album?.bookmarkedAt
//                                    ),
//                                    currentAlbumPage
//                                        .songsPage
//                                        ?.items?.distinct()
//                                        ?.map(Innertube.SongItem::asMediaItem)
//                                        ?.onEach(Database::insert)
//                                        ?.mapIndexed { position, mediaItem ->
//                                            SongAlbumMap(
//                                                songId = mediaItem.mediaId,
//                                                albumId = browseId,
//                                                position = position
//                                            )
//                                        } ?: emptyList()
//                                )
//                            }
//
//                            ?.onFailure {
//                                println("mediaItem error albumScreen ${it.message}")
//                            }
//
//                    }
//
//                }
//            }
//    }

    /*
    LaunchedEffect(Unit ) {
        withContext(Dispatchers.IO) {
            Innertube.albumPage(BrowseBody(browseId = browseId))
                ?.onSuccess { currentAlbumPage ->
                    albumPage = currentAlbumPage
                }
            //println("mediaItem home albumscreen albumPage des ${albumPage?.description} albumPage ${albumPage?.otherVersions?.size}")
            //println("mediaItem home albumscreen albumPage songPage ${albumPage?.songsPage}")
        }
    }

     */


    val headerContent: @Composable (textButton: (@Composable () -> Unit)?) -> Unit =
        { textButton ->
            if (album?.timestamp == null) {
                HeaderPlaceholder(
                    modifier = Modifier
                        .shimmer()
                )
            } else {
                val context = LocalContext.current

                Header(
                    //title = album?.title ?: "Unknown"
                    title = "",
                    modifier = Modifier.padding(horizontal = 12.dp),
                    actionsContent = {
                        textButton?.invoke()


                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                        )

                        HeaderIconButton(
                            icon = if (album?.bookmarkedAt == null) {
                                R.drawable.bookmark_outline
                            } else {
                                R.drawable.bookmark
                            },
                            color = colorPalette().accent,
                            onClick = {
                                val bookmarkedAt =
                                    if (album?.bookmarkedAt == null) System.currentTimeMillis() else null

                                Database.asyncTransaction {
                                    album?.copy(bookmarkedAt = bookmarkedAt)
                                        ?.let(::update)
                                }
                            }
                        )

                        HeaderIconButton(
                            icon = R.drawable.share_social,
                            color = colorPalette().text,
                            onClick = {
                                album?.shareUrl?.let { url ->
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, url)
                                    }

                                    context.startActivity(
                                        Intent.createChooser(
                                            sendIntent,
                                            null
                                        )
                                    )
                                }
                            }
                        )
                    },
                    disableScrollingText = disableScrollingText
                )
            }
        }

    val thumbnailContent =
        adaptiveThumbnailContent(
            album?.timestamp == null,
            album?.thumbnailUrl,
            showIcon = false, //albumPage?.otherVersions?.isNotEmpty(),
            onOtherVersionAvailable = {
                //println("mediaItem Click other version")
            },
            //shape = thumbnailRoundness.shape()
            onClick = { changeShape = !changeShape },
            shape = if (changeShape) CircleShape else thumbnailRoundness.shape(),
        )

    val transitionEffect by rememberPreference(transitionEffectKey, TransitionEffect.Scale)
    val playerPosition by rememberPreference(playerPositionKey, PlayerPosition.Bottom)

    androidx.compose.material3.Scaffold(
        modifier = Modifier,
        containerColor = colorPalette().background0,
        topBar = {
            if (UiType.RiMusic.isCurrent())
                AppHeader(navController).Draw()
        }
    ) {
        //**
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {

            Row(
                modifier = Modifier
                    .background(colorPalette().background0)
                    .fillMaxSize()
            ) {
                val topPadding = if (UiType.ViMusic.isCurrent()) 30.dp else 0.dp

                AnimatedContent(
                    targetState = 0,
                    transitionSpec = {
                        when (transitionEffect) {
                            TransitionEffect.None -> EnterTransition.None togetherWith ExitTransition.None
                            TransitionEffect.Expand -> expandIn(
                                animationSpec = tween(
                                    350,
                                    easing = LinearOutSlowInEasing
                                ), expandFrom = Alignment.BottomStart
                            ).togetherWith(
                                shrinkOut(
                                    animationSpec = tween(
                                        350,
                                        easing = FastOutSlowInEasing
                                    ), shrinkTowards = Alignment.CenterStart
                                )
                            )

                            TransitionEffect.Fade -> fadeIn(animationSpec = tween(350)).togetherWith(
                                fadeOut(animationSpec = tween(350))
                            )

                            TransitionEffect.Scale -> scaleIn(animationSpec = tween(350)).togetherWith(
                                scaleOut(animationSpec = tween(350))
                            )

                            TransitionEffect.SlideHorizontal, TransitionEffect.SlideVertical -> {
                                val slideDirection = when (targetState > initialState) {
                                    true -> {
                                        if (transitionEffect == TransitionEffect.SlideHorizontal)
                                            AnimatedContentTransitionScope.SlideDirection.Left
                                        else AnimatedContentTransitionScope.SlideDirection.Up
                                    }

                                    false -> {
                                        if (transitionEffect == TransitionEffect.SlideHorizontal)
                                            AnimatedContentTransitionScope.SlideDirection.Right
                                        else AnimatedContentTransitionScope.SlideDirection.Down
                                    }
                                }

                                val animationSpec = spring(
                                    dampingRatio = 0.9f,
                                    stiffness = Spring.StiffnessLow,
                                    visibilityThreshold = IntOffset.VisibilityThreshold
                                )

                                slideIntoContainer(
                                    slideDirection,
                                    animationSpec
                                ) togetherWith
                                        slideOutOfContainer(slideDirection, animationSpec)
                            }
                        }
                    },
                    label = "",
                    modifier = Modifier
                        //.fillMaxWidth()
                        .fillMaxHeight()
                        .padding(top = topPadding)
                ) { currentTabIndex ->
                    saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                        when (currentTabIndex) {
                            0 -> AlbumDetails(
                                navController = navController,
                                browseId = browseId,
                                albumPage = albumPage,
                                headerContent = headerContent,
                                thumbnailContent = thumbnailContent,
                                onSearchClick = {
                                    //searchRoute("")
                                    navController.navigate(NavRoutes.search.name)
                                },
                                onSettingsClick = {
                                    //settingsRoute()
                                    navController.navigate(NavRoutes.settings.name)
                                }
                            )

                            1 -> {
                                val thumbnailSizeDp = 108.dp
                                val thumbnailSizePx = thumbnailSizeDp.px

                                ItemsPage(
                                    tag = "album/$browseId/alternatives",
                                    headerContent = headerContent,
                                    initialPlaceholderCount = 1,
                                    continuationPlaceholderCount = 1,
                                    emptyItemsText = stringResource(R.string.album_no_alternative_version),
                                    itemsPageProvider = albumPage?.let {
                                        ({
                                            Result.success(
                                                Environment.ItemsPage(
                                                    items = albumPage?.otherVersions,
                                                    continuation = null
                                                )
                                            )
                                        })
                                    },
                                    itemContent = { album ->
                                        AlbumItem(
                                            album = album,
                                            thumbnailSizePx = thumbnailSizePx,
                                            thumbnailSizeDp = thumbnailSizeDp,
                                            modifier = Modifier
                                                .clickable {
                                                    //albumRoute(album.key)
                                                    navController.navigate(route = "${NavRoutes.album.name}/${album.key}")
                                                },
                                            disableScrollingText = disableScrollingText
                                        )
                                    },
                                    itemPlaceholderContent = {
                                        AlbumItemPlaceholder(thumbnailSizeDp = thumbnailSizeDp)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            //**
            Box(
                modifier = modifier
                    .padding(vertical = 5.dp)
                    .align(
                        if (playerPosition == PlayerPosition.Top) Alignment.TopCenter else Alignment.BottomCenter
                    )
            ) {
                miniPlayer.invoke()
            }
        }
    }

}
