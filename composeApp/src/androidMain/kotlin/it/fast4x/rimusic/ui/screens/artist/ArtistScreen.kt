package it.fast4x.rimusic.ui.screens.artist

import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
import com.valentinilk.shimmer.shimmer
import it.fast4x.compose.persist.persist
import it.fast4x.environment.Environment
import it.fast4x.environment.models.bodies.BrowseBody
import it.fast4x.environment.models.bodies.ContinuationBody
import it.fast4x.environment.requests.artistPage
import it.fast4x.environment.requests.itemsPage
import it.fast4x.environment.utils.from
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.ThumbnailRoundness
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.models.Artist
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.Scaffold
import it.fast4x.rimusic.ui.components.SwipeablePlaylistItem
import it.fast4x.rimusic.ui.components.themed.Header
import it.fast4x.rimusic.ui.components.themed.HeaderIconButton
import it.fast4x.rimusic.ui.components.themed.HeaderPlaceholder
import it.fast4x.rimusic.ui.components.themed.NonQueuedMediaItemMenu
import it.fast4x.rimusic.ui.components.themed.NowPlayingSongIndicator
import it.fast4x.rimusic.ui.components.themed.SecondaryTextButton
import it.fast4x.rimusic.ui.components.themed.adaptiveThumbnailContent
import it.fast4x.rimusic.ui.items.AlbumItem
import it.fast4x.rimusic.ui.items.AlbumItemPlaceholder
import it.fast4x.rimusic.ui.items.SongItem
import it.fast4x.rimusic.ui.items.SongItemPlaceholder
import it.fast4x.rimusic.ui.screens.searchresult.ItemsPage
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.px
import it.fast4x.rimusic.utils.addNext
import it.fast4x.rimusic.utils.asMediaItem
import it.fast4x.rimusic.utils.completed
import it.fast4x.rimusic.utils.disableScrollingTextKey
import it.fast4x.rimusic.utils.enqueue
import it.fast4x.rimusic.utils.forcePlay
import it.fast4x.rimusic.utils.getDownloadState
import it.fast4x.rimusic.utils.isDownloadedSong
import it.fast4x.rimusic.utils.isNowPlaying
import it.fast4x.rimusic.utils.manageDownload
import it.fast4x.rimusic.utils.parentalControlEnabledKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.thumbnailRoundnessKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import it.fast4x.rimusic.colorPalette

@ExperimentalMaterialApi
@ExperimentalTextApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun ArtistScreen(
    navController: NavController,
    browseId: String,
    miniPlayer: @Composable () -> Unit = {},
) {
    val saveableStateHolder = rememberSaveableStateHolder()

    //var tabIndex by rememberPreference(artistScreenTabIndexKey, defaultValue = 0)

    val binder = LocalPlayerServiceBinder.current

    var tabIndex by rememberSaveable {
        mutableStateOf(0)
    }

    //PersistMapCleanup(tagPrefix = "artist/$browseId/")

    var artist by persist<Artist?>("artist/$browseId/artist")

    var artistPage by persist<Environment.ArtistInfoPage?>("artist/$browseId/artistPage")

    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }
    val context = LocalContext.current

    val thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )
    var changeShape by remember {
        mutableStateOf(false)
    }
    val hapticFeedback = LocalHapticFeedback.current
    val parentalControlEnabled by rememberPreference(parentalControlEnabledKey, false)

    val disableScrollingText by rememberPreference(disableScrollingTextKey, false)

    LaunchedEffect(Unit) {
        Database
            .artist(browseId)
            .combine(snapshotFlow { tabIndex }.map { it != 4 }) { artist, mustFetch -> artist to mustFetch }
            .distinctUntilChanged()
            .collect { (currentArtist, mustFetch) ->
                artist = currentArtist

                if (artistPage == null && (currentArtist?.timestamp == null || mustFetch)) {
                    withContext(Dispatchers.IO) {
                        Environment.artistPage(BrowseBody(browseId = browseId))
                            ?.onSuccess { currentArtistPage ->
                                artistPage = currentArtistPage

                                Database.upsert(
                                    Artist(
                                        id = browseId,
                                        name = currentArtistPage.name,
                                        thumbnailUrl = currentArtistPage.thumbnail?.url,
                                        timestamp = System.currentTimeMillis(),
                                        bookmarkedAt = currentArtist?.bookmarkedAt
                                    )
                                )
                            }
                    }
                }
            }
    }

    val listMediaItems = remember { mutableListOf<MediaItem>() }

            val thumbnailContent =
                adaptiveThumbnailContent(
                    artist?.timestamp == null,
                    artist?.thumbnailUrl,
                    //CircleShape
                    onClick = { changeShape = !changeShape },
                    shape = if (changeShape) CircleShape else thumbnailRoundness.shape(),
                )

            val headerContent: @Composable (textButton: (@Composable () -> Unit)?) -> Unit =
                { textButton ->
                    if (artist?.timestamp == null) {
                        HeaderPlaceholder(
                            modifier = Modifier
                                .shimmer()
                        )
                    } else {
                        Header(title = artist?.name ?: "Unknown", actionsContent = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .padding(top = 50.dp)
                                        .padding(horizontal = 12.dp)
                                ) {
                                    textButton?.invoke()

                                    Spacer(
                                        modifier = Modifier
                                            .weight(0.2f)
                                    )

                                    SecondaryTextButton(
                                        text = if (artist?.bookmarkedAt == null) stringResource(R.string.follow) else stringResource(
                                            R.string.following
                                        ),
                                        onClick = {
                                            val bookmarkedAt =
                                                if (artist?.bookmarkedAt == null) System.currentTimeMillis() else null

                                            Database.asyncTransaction {
                                                artist?.copy( bookmarkedAt = bookmarkedAt )
                                                      ?.let( ::update )
                                            }
                                        },
                                        alternative = artist?.bookmarkedAt == null
                                    )

                                    HeaderIconButton(
                                        icon = R.drawable.share_social,
                                        color = colorPalette().text,
                                        onClick = {
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                type = "text/plain"
                                                putExtra(
                                                    Intent.EXTRA_TEXT,
                                                    "https://music.youtube.com/channel/$browseId"
                                                )
                                            }

                                            context.startActivity(
                                                Intent.createChooser(
                                                    sendIntent,
                                                    null
                                                )
                                            )
                                        }
                                    )
                                }
                            },
                            disableScrollingText = disableScrollingText)
                    }
                }

            Scaffold(
                navController = navController,
                miniPlayer = miniPlayer,
                topIconButtonId = R.drawable.chevron_back,
                onTopIconButtonClick = {},
                showButton1 = UiType.RiMusic.isNotCurrent(),
                topIconButton2Id = R.drawable.chevron_back,
                onTopIconButton2Click = {},
                showButton2 = false,
                tabIndex = tabIndex,
                onHomeClick = {
                    navController.navigate(NavRoutes.home.name)
                },
                onTabChanged = { tabIndex = it },
                tabColumnContent = { Item ->
                    Item(0, stringResource(R.string.overview), R.drawable.artist)
                    Item(1, stringResource(R.string.songs), R.drawable.musical_notes)
                    Item(2, stringResource(R.string.albums), R.drawable.album)
                    Item(3, stringResource(R.string.singles), R.drawable.disc)
                    Item(4, stringResource(R.string.library), R.drawable.library)
                },
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> {
                            ArtistOverview(
                                navController = navController,
                                browseId = browseId,
                                youtubeArtistPage = artistPage,
                                thumbnailContent = thumbnailContent,
                                headerContent = headerContent,
                                onAlbumClick = {
                                    navController.navigate(route = "${NavRoutes.album.name}/$it")
                                },
                                onPlaylistClick = {
                                    navController.navigate(route = "${NavRoutes.playlist.name}/$it")
                                },
                                onViewAllSongsClick = { tabIndex = 1 },
                                onViewAllAlbumsClick = { tabIndex = 2 },
                                onViewAllSinglesClick = { tabIndex = 3 },
                                onSearchClick = {
                                    navController.navigate(NavRoutes.search.name)
                                },
                                onSettingsClick = {
                                    navController.navigate(NavRoutes.settings.name)
                                },
                                disableScrollingText = disableScrollingText
                            )
                        }

                        1 -> {
                            val menuState = LocalMenuState.current
                            val thumbnailSizeDp = Dimensions.thumbnails.song
                            val thumbnailSizePx = thumbnailSizeDp.px
                            //val listMediaItems = remember { mutableListOf<MediaItem>() }
                            ItemsPage(
                                tag = "artist/$browseId/songs",
                                headerContent = headerContent,
                                itemsPageProvider = artistPage?.let {
                                    {
                                        artistPage
                                            ?.songsEndpoint
                                            ?.takeIf { it.browseId != null }
                                            ?.let { endpoint ->
                                                Environment.itemsPage(
                                                    body = BrowseBody(
                                                        browseId = endpoint.browseId!!,
                                                        params = endpoint.params
                                                    ),
                                                    fromMusicResponsiveListItemRenderer = Environment.SongItem::from,
                                                )?.completed()
                                            }
                                        ?: Result.success( // is this section ever reached now?
                                            Environment.ItemsPage(
                                                items = artistPage?.songs,
                                                continuation = null
                                            )
                                        )
                                    }
                                },
                                itemContent = { song ->
                                    if (parentalControlEnabled && song.explicit) return@ItemsPage

                                    downloadState = getDownloadState(song.asMediaItem.mediaId)
                                    val isDownloaded = isDownloadedSong(song.asMediaItem.mediaId)

                                    SwipeablePlaylistItem(
                                        mediaItem = song.asMediaItem,
                                        onPlayNext = {
                                            binder?.player?.addNext(song.asMediaItem)
                                        },
                                        onDownload = {
                                            binder?.cache?.removeResource(song.asMediaItem.mediaId)
                                            CoroutineScope(Dispatchers.IO).launch {
                                                Database.resetContentLength( song.asMediaItem.mediaId )
                                            }

                                            manageDownload(
                                                context = context,
                                                mediaItem = song.asMediaItem,
                                                downloadState = isDownloaded
                                            )
                                        },
                                        onEnqueue = {
                                            binder?.player?.enqueue(song.asMediaItem)
                                        }
                                    ) {
                                        listMediaItems.add(song.asMediaItem)
                                        var forceRecompose by remember { mutableStateOf(false) }
                                        SongItem(
                                            song = song,
                                            onDownloadClick = {
                                                binder?.cache?.removeResource(song.asMediaItem.mediaId)
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    Database.deleteFormat( song.asMediaItem.mediaId )
                                                }

                                                manageDownload(
                                                    context = context,
                                                    mediaItem = song.asMediaItem,
                                                    downloadState = isDownloaded
                                                )
                                            },
                                            thumbnailContent = {
                                                NowPlayingSongIndicator(song.asMediaItem.mediaId, binder?.player)
                                            },
                                            downloadState = downloadState,
                                            thumbnailSizeDp = thumbnailSizeDp,
                                            thumbnailSizePx = thumbnailSizePx,
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
                                                                disableScrollingText = disableScrollingText
                                                            )
                                                        };
                                                        hapticFeedback.performHapticFeedback(
                                                            HapticFeedbackType.LongPress
                                                        )
                                                    },
                                                    onClick = {
                                                        CoroutineScope(Dispatchers.IO).launch {
                                                            artistPage
                                                                ?.songsEndpoint
                                                                ?.takeIf { it.browseId != null }
                                                                ?.let { endpoint ->
                                                                    Environment.itemsPage(
                                                                        body = BrowseBody(
                                                                            browseId = endpoint.browseId!!,
                                                                            params = endpoint.params,
                                                                        ),
                                                                        fromMusicResponsiveListItemRenderer = Environment.SongItem::from,
                                                                    )
                                                                }
                                                                ?.getOrNull()
                                                                ?.items
                                                                ?.map { it.asMediaItem }
                                                                ?.let {
                                                                    withContext(Dispatchers.Main) {
                                                                        binder?.stopRadio()
                                                                        binder?.player?.forcePlay(song.asMediaItem)
                                                                        binder?.player?.addMediaItems(
                                                                            it.filterNot { it.mediaId == song.key }
                                                                        )
                                                                    }
                                                                    /*
                                                                    withContext(Dispatchers.Main) {
                                                                        binder?.player?.forcePlayFromBeginning(
                                                                            it
                                                                        )
                                                                    }
                                                                     */
                                                                }
                                                        }

                                                    }
                                                ),
                                            disableScrollingText = disableScrollingText,
                                            isNowPlaying = binder?.player?.isNowPlaying(song.key) ?: false,
                                            forceRecompose = forceRecompose
                                        )
                                    }
                                },
                                itemPlaceholderContent = {
                                    SongItemPlaceholder(thumbnailSizeDp = thumbnailSizeDp)
                                }
                            )
                        }

                        2 -> {
                            val thumbnailSizeDp = 108.dp
                            val thumbnailSizePx = thumbnailSizeDp.px

                            ItemsPage(
                                tag = "artist/$browseId/albums",
                                headerContent = headerContent,
                                emptyItemsText = stringResource(R.string.artist_no_release_album),
                                itemsPageProvider = artistPage?.let {
                                    ({ continuation ->
                                        continuation?.let {
                                            Environment.itemsPage(
                                                body = ContinuationBody(continuation = continuation),
                                                fromMusicTwoRowItemRenderer = Environment.AlbumItem::from,
                                            )
                                        } ?: artistPage
                                            ?.albumsEndpoint
                                            ?.takeIf { it.browseId != null }
                                            ?.let { endpoint ->
                                                Environment.itemsPage(
                                                    body = BrowseBody(
                                                        browseId = endpoint.browseId!!,
                                                        params = endpoint.params,
                                                    ),
                                                    fromMusicTwoRowItemRenderer = Environment.AlbumItem::from,
                                                )
                                            }
                                        ?: Result.success(
                                            Environment.ItemsPage(
                                                items = artistPage?.albums,
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
                                            .clickable(onClick = {
                                                //albumRoute(album.key)
                                                navController.navigate(route = "${NavRoutes.album.name}/${album.key}")
                                            }),
                                        yearCentered = false,
                                        disableScrollingText = disableScrollingText
                                    )
                                },
                                itemPlaceholderContent = {
                                    AlbumItemPlaceholder(thumbnailSizeDp = thumbnailSizeDp)
                                }
                            )
                        }

                        3 -> {
                            val thumbnailSizeDp = 108.dp
                            val thumbnailSizePx = thumbnailSizeDp.px

                            ItemsPage(
                                tag = "artist/$browseId/singles",
                                headerContent = headerContent,
                                emptyItemsText = stringResource(R.string.artist_no_release_single),
                                itemsPageProvider = artistPage?.let {
                                    ({ continuation ->
                                        continuation?.let {
                                            Environment.itemsPage(
                                                body = ContinuationBody(continuation = continuation),
                                                fromMusicTwoRowItemRenderer = Environment.AlbumItem::from,
                                            )
                                        } ?: artistPage
                                            ?.singlesEndpoint
                                            ?.takeIf { it.browseId != null }
                                            ?.let { endpoint ->
                                                Environment.itemsPage(
                                                    body = BrowseBody(
                                                        browseId = endpoint.browseId!!,
                                                        params = endpoint.params,
                                                    ),
                                                    fromMusicTwoRowItemRenderer = Environment.AlbumItem::from,
                                                )
                                            }
                                        ?: Result.success(
                                            Environment.ItemsPage(
                                                items = artistPage?.singles,
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
                                            .clickable(onClick = {
                                                //albumRoute(album.key)
                                                navController.navigate(route = "${NavRoutes.album.name}/${album.key}")
                                            }),
                                        yearCentered = false,
                                        disableScrollingText = disableScrollingText
                                    )
                                },
                                itemPlaceholderContent = {
                                    AlbumItemPlaceholder(thumbnailSizeDp = thumbnailSizeDp)
                                }
                            )
                        }

                        4 -> {
                            ArtistLocalSongs(
                                navController = navController,
                                browseId = browseId,
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
                        }
                    }
                }
            }

}
