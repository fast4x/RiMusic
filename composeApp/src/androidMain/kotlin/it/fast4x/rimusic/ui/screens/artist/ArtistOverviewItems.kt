package it.fast4x.rimusic.ui.screens.artist

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
import coil.compose.AsyncImage
import it.fast4x.compose.persist.persist
import it.fast4x.innertube.Innertube
import it.fast4x.innertube.YtMusic
import it.fast4x.innertube.models.BrowseEndpoint
import it.fast4x.innertube.models.bodies.BrowseBody
import it.fast4x.innertube.requests.ArtistItemsPage
import it.fast4x.innertube.requests.ArtistPage
import it.fast4x.innertube.requests.ArtistSection
import it.fast4x.innertube.requests.itemsPage
import it.fast4x.innertube.utils.from
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.LocalPlayerAwareWindowInsets
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.models.Artist
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.ShimmerHost
import it.fast4x.rimusic.ui.components.themed.AutoResizeText
import it.fast4x.rimusic.ui.components.themed.FontSizeRange
import it.fast4x.rimusic.ui.components.themed.HeaderIconButton
import it.fast4x.rimusic.ui.components.themed.LayoutWithAdaptiveThumbnail
import it.fast4x.rimusic.ui.components.themed.MultiFloatingActionsContainer
import it.fast4x.rimusic.ui.components.themed.SecondaryTextButton
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.ui.components.themed.TextPlaceholder
import it.fast4x.rimusic.ui.components.themed.Title
import it.fast4x.rimusic.ui.items.AlbumItem
import it.fast4x.rimusic.ui.items.AlbumItemPlaceholder
import it.fast4x.rimusic.ui.items.PlaylistItem
import it.fast4x.rimusic.ui.items.SongItem
import it.fast4x.rimusic.ui.items.SongItemPlaceholder
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.px
import it.fast4x.rimusic.utils.align
import it.fast4x.rimusic.utils.asMediaItem
import it.fast4x.rimusic.utils.color
import it.fast4x.rimusic.utils.conditional
import it.fast4x.rimusic.utils.fadingEdge
import it.fast4x.rimusic.utils.forcePlay
import it.fast4x.rimusic.utils.getHttpClient
import it.fast4x.rimusic.utils.isLandscape
import it.fast4x.rimusic.utils.languageDestination
import it.fast4x.rimusic.utils.medium
import it.fast4x.rimusic.utils.parentalControlEnabledKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.resize
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.showFloatingIconKey
import me.bush.translator.Translator
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.isVideoEnabled
import it.fast4x.rimusic.typography
import it.fast4x.rimusic.ui.components.SwipeablePlaylistItem
import it.fast4x.rimusic.ui.components.themed.NonQueuedMediaItemMenu
import it.fast4x.rimusic.ui.components.themed.NowPlayingSongIndicator
import it.fast4x.rimusic.ui.components.themed.TitleMiniSection
import it.fast4x.rimusic.ui.components.themed.TitleSection
import it.fast4x.rimusic.ui.items.ArtistItem
import it.fast4x.rimusic.ui.items.VideoItem
import it.fast4x.rimusic.utils.addNext
import it.fast4x.rimusic.utils.enqueue
import it.fast4x.rimusic.utils.getDownloadState
import it.fast4x.rimusic.utils.isDownloadedSong
import it.fast4x.rimusic.utils.isNowPlaying
import it.fast4x.rimusic.utils.manageDownload
import it.fast4x.rimusic.utils.playVideo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@UnstableApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun ArtistOverviewItems(
    navController: NavController,
    browseId: String,
    params: String? = null,
    artistName: String? = null,
    sectionName: String? = null,
    disableScrollingText: Boolean,
    onDismiss: () -> Unit
) {
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val windowInsets = LocalPlayerAwareWindowInsets.current

    val songThumbnailSizeDp = Dimensions.thumbnails.song
    val songThumbnailSizePx = songThumbnailSizeDp.px
    val albumThumbnailSizeDp = 108.dp
    val albumThumbnailSizePx = albumThumbnailSizeDp.px
    val artistThumbnailSizeDp = 92.dp
    val artistThumbnailSizePx = artistThumbnailSizeDp.px
    val playlistThumbnailSizeDp = 108.dp
    val playlistThumbnailSizePx = playlistThumbnailSizeDp.px

    val endPaddingValues = windowInsets.only(WindowInsetsSides.End).asPaddingValues()

    val sectionTextModifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 24.dp, bottom = 8.dp)

    val scrollState = rememberScrollState()

    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }

    val context = LocalContext.current

    var showConfirmDeleteDownloadDialog by remember {
        mutableStateOf(false)
    }

    var showConfirmDownloadAllDialog by remember {
        mutableStateOf(false)
    }

    var translateEnabled by remember {
        mutableStateOf(false)
    }

    val translator = Translator(getHttpClient())
    val languageDestination = languageDestination()
    val listMediaItems = remember { mutableListOf<MediaItem>() }

    //var artist by persist<Artist?>("artist/${artistSection?.moreEndpoint?.browseId}/items")

    val hapticFeedback = LocalHapticFeedback.current
    val parentalControlEnabled by rememberPreference(parentalControlEnabledKey, false)

    var artistItemsPage by persist<ArtistItemsPage?>("artist/${browseId}/artistPage")

    val thumbnailSizeDp = Dimensions.thumbnails.album //+ 24.dp
    val thumbnailSizePx = thumbnailSizeDp.px

    LaunchedEffect(Unit) {
        artistItemsPage = YtMusic.getArtistItemsPage(
            BrowseEndpoint(
                browseId = browseId,
                params = params
            )
        ).getOrNull()

        println("ArtistOverviewItems artistItemsPage: $artistItemsPage")
    }

    if (artistItemsPage == null) return

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

        if (artistItemsPage?.items?.firstOrNull() is Innertube.SongItem) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = WindowInsets.systemBars.asPaddingValues()
            ) {
                item {
                    Title(
                        title = artistName ?: "",
                        modifier = sectionTextModifier,
                        icon = R.drawable.chevron_down,
                        onClick = onDismiss
                    )
                    TitleSection(
                        title = sectionName ?: "",
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .padding(horizontal = 16.dp)
                    )
                }
                items(artistItemsPage?.items!!.distinctBy { it.key }) { item ->
                    when (item) {
                        is Innertube.SongItem -> {
                            if (parentalControlEnabled && item.explicit) return@items

                            downloadState = getDownloadState(item.asMediaItem.mediaId)
                            val isDownloaded = isDownloadedSong(item.asMediaItem.mediaId)

                            SwipeablePlaylistItem(
                                mediaItem = item.asMediaItem,
                                onPlayNext = {
                                    binder?.player?.addNext(item.asMediaItem)
                                },
                                onDownload = {
                                    binder?.cache?.removeResource(item.asMediaItem.mediaId)
                                    CoroutineScope(Dispatchers.IO).launch {
                                        Database.resetContentLength( item.asMediaItem.mediaId )
                                    }

                                    manageDownload(
                                        context = context,
                                        mediaItem = item.asMediaItem,
                                        downloadState = isDownloaded
                                    )
                                },
                                onEnqueue = {
                                    binder?.player?.enqueue(item.asMediaItem)
                                }
                            ) {
                                listMediaItems.add(item.asMediaItem)
                                var forceRecompose by remember { mutableStateOf(false) }
                                SongItem(
                                    song = item,
                                    onDownloadClick = {
                                        binder?.cache?.removeResource(item.asMediaItem.mediaId)
                                        CoroutineScope(Dispatchers.IO).launch {
                                            Database.deleteFormat( item.asMediaItem.mediaId )
                                        }

                                        manageDownload(
                                            context = context,
                                            mediaItem = item.asMediaItem,
                                            downloadState = isDownloaded
                                        )
                                    },
                                    thumbnailContent = {
                                        NowPlayingSongIndicator(item.asMediaItem.mediaId, binder?.player)
                                    },
                                    downloadState = getDownloadState(item.asMediaItem.mediaId),
                                    thumbnailSizeDp = songThumbnailSizeDp,
                                    thumbnailSizePx = songThumbnailSizePx,
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
                                                        mediaItem = item.asMediaItem,
                                                        disableScrollingText = disableScrollingText
                                                    )
                                                };
                                                hapticFeedback.performHapticFeedback(
                                                    HapticFeedbackType.LongPress
                                                )
                                            },
                                            onClick = {
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    withContext(Dispatchers.Main) {
                                                        binder?.stopRadio()
                                                        binder?.player?.forcePlay(item.asMediaItem)
                                                        binder?.player?.addMediaItems(
                                                            artistItemsPage!!.items
                                                                .map{it as Innertube.SongItem}
                                                                .map { it.asMediaItem }
                                                                .filterNot { it.mediaId == item.key }
                                                                //.toMutableList()

                                                        )
                                                    }
                                                }

                                            }
                                        ),
                                    disableScrollingText = disableScrollingText,
                                    isNowPlaying = binder?.player?.isNowPlaying(item.key) ?: false,
                                    forceRecompose = forceRecompose
                                )
                            }
                        }
                        else -> {}
//                        is Innertube.AlbumItem -> {
//                            AlbumItem(
//                                album = item,
//                                thumbnailSizePx = thumbnailSizePx,
//                                thumbnailSizeDp = thumbnailSizeDp,
//                                alternative = false,
//                                yearCentered = false,
//                                showAuthors = true,
//                                modifier = Modifier.clickable(onClick = {
//                                    navController.navigate(route = "${NavRoutes.album.name}/${item.key}")
//                                }),
//                                disableScrollingText = disableScrollingText
//                            )
//                        }
//                        is Innertube.PlaylistItem -> {
//                            PlaylistItem(
//                                playlist = item,
//                                alternative = false,
//                                thumbnailSizePx = playlistThumbnailSizePx,
//                                thumbnailSizeDp = playlistThumbnailSizeDp,
//                                disableScrollingText = disableScrollingText,
//                                modifier = Modifier.clickable(onClick = {
//                                    navController.navigate("${NavRoutes.playlist.name}/${item.key}")
//                                })
//                            )
//                        }
//                        is Innertube.VideoItem -> {
//                            VideoItem(
//                                video = item,
//                                thumbnailHeightDp = playlistThumbnailSizeDp,
//                                thumbnailWidthDp = playlistThumbnailSizeDp,
//                                disableScrollingText = disableScrollingText,
//                                modifier = Modifier.clickable(onClick = {
//                                    binder?.stopRadio()
//                                    if (isVideoEnabled())
//                                        binder?.player?.playVideo(item.asMediaItem)
//                                    else
//                                        binder?.player?.forcePlay(item.asMediaItem)
//                                })
//                            )
//                        }
//                        is Innertube.ArtistItem -> {
//                            ArtistItem(
//                                artist = item,
//                                thumbnailSizePx = artistThumbnailSizePx,
//                                thumbnailSizeDp = artistThumbnailSizeDp,
//                                disableScrollingText = disableScrollingText,
//                                modifier = Modifier.clickable(onClick = {
//                                    navController.navigate("${NavRoutes.artist.name}/${item.key}")
//                                })
//                            )
//                        }

                    }



                }

            }
        } else {
            LazyVerticalGrid(
                state = rememberLazyGridState(),
                columns = GridCells.Adaptive(Dimensions.thumbnails.album + 24.dp),
                modifier = Modifier
                    .background(colorPalette().background0)
                    .fillMaxSize(),
                contentPadding = WindowInsets.systemBars.asPaddingValues()
            ) {

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column{
                        Title(
                            title = artistName ?: "",
                            modifier = sectionTextModifier,
                            icon = R.drawable.chevron_down,
                            onClick = onDismiss
                        )
                        TitleSection(
                            title = sectionName ?: "",
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .padding(horizontal = 16.dp)
                        )
                    }

                }
                items(
                    items = artistItemsPage?.items!!) { item ->
                    when (item) {
//                        is Innertube.SongItem -> {
////                            if (parentalControlEnabled && item.explicit) return@items
////
////                            downloadState = getDownloadState(item.asMediaItem.mediaId)
////                            val isDownloaded = isDownloadedSong(item.asMediaItem.mediaId)
////
////                            SwipeablePlaylistItem(
////                                mediaItem = item.asMediaItem,
////                                onPlayNext = {
////                                    binder?.player?.addNext(item.asMediaItem)
////                                },
////                                onDownload = {
////                                    binder?.cache?.removeResource(item.asMediaItem.mediaId)
////                                    CoroutineScope(Dispatchers.IO).launch {
////                                        Database.resetContentLength( item.asMediaItem.mediaId )
////                                    }
////
////                                    manageDownload(
////                                        context = context,
////                                        mediaItem = item.asMediaItem,
////                                        downloadState = isDownloaded
////                                    )
////                                },
////                                onEnqueue = {
////                                    binder?.player?.enqueue(item.asMediaItem)
////                                }
////                            ) {
////                                listMediaItems.add(item.asMediaItem)
////                                var forceRecompose by remember { mutableStateOf(false) }
////                                SongItem(
////                                    song = item,
////                                    onDownloadClick = {
////                                        binder?.cache?.removeResource(item.asMediaItem.mediaId)
////                                        CoroutineScope(Dispatchers.IO).launch {
////                                            Database.deleteFormat( item.asMediaItem.mediaId )
////                                        }
////
////                                        manageDownload(
////                                            context = context,
////                                            mediaItem = item.asMediaItem,
////                                            downloadState = isDownloaded
////                                        )
////                                    },
////                                    thumbnailContent = {
////                                        NowPlayingSongIndicator(item.asMediaItem.mediaId, binder?.player)
////                                    },
////                                    downloadState = downloadState,
////                                    thumbnailSizeDp = songThumbnailSizeDp,
////                                    thumbnailSizePx = songThumbnailSizePx,
////                                    modifier = Modifier
////                                        .combinedClickable(
////                                            onLongClick = {
////                                                menuState.display {
////                                                    NonQueuedMediaItemMenu(
////                                                        navController = navController,
////                                                        onDismiss = {
////                                                            menuState.hide()
////                                                            forceRecompose = true
////                                                        },
////                                                        mediaItem = item.asMediaItem,
////                                                        disableScrollingText = disableScrollingText
////                                                    )
////                                                };
////                                                hapticFeedback.performHapticFeedback(
////                                                    HapticFeedbackType.LongPress
////                                                )
////                                            },
////                                            onClick = {
////                                                CoroutineScope(Dispatchers.IO).launch {
////                                                    withContext(Dispatchers.Main) {
////                                                        binder?.stopRadio()
////                                                        binder?.player?.forcePlay(item.asMediaItem)
////                                                        binder?.player?.addMediaItems(
////                                                            artistItemsPage!!.items
////                                                                .map{it as Innertube.SongItem}
////                                                                .map { it.asMediaItem }
////                                                                .filterNot { it.mediaId == item.key }
////                                                            //.toMutableList()
////
////                                                        )
////                                                    }
////                                                }
////
////                                            }
////                                        ),
////                                    disableScrollingText = disableScrollingText,
////                                    isNowPlaying = binder?.player?.isNowPlaying(item.key) ?: false,
////                                    forceRecompose = forceRecompose
////                                )
////                            }
//                        }
                        is Innertube.AlbumItem -> {
                            AlbumItem(
                                album = item,
                                thumbnailSizePx = thumbnailSizePx,
                                thumbnailSizeDp = thumbnailSizeDp,
                                alternative = true,
                                yearCentered = true,
                                showAuthors = true,
                                modifier = Modifier.clickable(onClick = {
                                    navController.navigate(route = "${NavRoutes.album.name}/${item.key}")
                                }),
                                disableScrollingText = disableScrollingText
                            )
                        }
                        is Innertube.PlaylistItem -> {
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
                        is Innertube.ArtistItem -> {
                            ArtistItem(
                                artist = item,
                                alternative = true,
                                thumbnailSizePx = artistThumbnailSizePx,
                                thumbnailSizeDp = artistThumbnailSizeDp,
                                disableScrollingText = disableScrollingText,
                                modifier = Modifier.clickable(onClick = {
                                    navController.navigate("${NavRoutes.artist.name}/${item.key}")
                                })
                            )
                        }
                        else -> {}
                    }
                }
            }
        }

    }

}
