package it.fast4x.rimusic.ui.screens.searchresult

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
import it.fast4x.compose.persist.persist
import it.fast4x.innertube.Innertube
import it.fast4x.innertube.models.bodies.BrowseBody
import it.fast4x.innertube.models.bodies.ContinuationBody
import it.fast4x.innertube.models.bodies.SearchBody
import it.fast4x.innertube.requests.albumPage
import it.fast4x.innertube.requests.searchPage
import it.fast4x.innertube.utils.from
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.models.Album
import it.fast4x.rimusic.models.Artist
import it.fast4x.rimusic.models.Playlist
import it.fast4x.rimusic.models.SongAlbumMap
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.Skeleton
import it.fast4x.rimusic.ui.components.SwipeableAlbumItem
import it.fast4x.rimusic.ui.components.SwipeablePlaylistItem
import it.fast4x.rimusic.ui.components.themed.NonQueuedMediaItemMenu
import it.fast4x.rimusic.ui.components.themed.NowPlayingSongIndicator
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.ui.components.themed.Title
import it.fast4x.rimusic.ui.items.AlbumItem
import it.fast4x.rimusic.ui.items.AlbumItemPlaceholder
import it.fast4x.rimusic.ui.items.ArtistItem
import it.fast4x.rimusic.ui.items.ArtistItemPlaceholder
import it.fast4x.rimusic.ui.items.PlaylistItem
import it.fast4x.rimusic.ui.items.PlaylistItemPlaceholder
import it.fast4x.rimusic.ui.items.SongItem
import it.fast4x.rimusic.ui.items.SongItemPlaceholder
import it.fast4x.rimusic.ui.items.VideoItem
import it.fast4x.rimusic.ui.items.VideoItemPlaceholder
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.px
import it.fast4x.rimusic.utils.addNext
import it.fast4x.rimusic.utils.asMediaItem
import it.fast4x.rimusic.utils.disableScrollingTextKey
import it.fast4x.rimusic.utils.enqueue
import it.fast4x.rimusic.utils.forcePlay
import it.fast4x.rimusic.utils.getDownloadState
import it.fast4x.rimusic.utils.isDownloadedSong
import it.fast4x.rimusic.utils.isNowPlaying
import it.fast4x.rimusic.utils.manageDownload
import it.fast4x.rimusic.utils.parentalControlEnabledKey
import it.fast4x.rimusic.utils.playVideo
import it.fast4x.rimusic.utils.preferences
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.searchResultScreenTabIndexKey
import it.fast4x.rimusic.utils.showButtonPlayerVideoKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@ExperimentalMaterialApi
@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun SearchResultScreen(
    navController: NavController,
    miniPlayer: @Composable () -> Unit = {},
    query: String,
    onSearchAgain: () -> Unit
) {
    val context = LocalContext.current
    val binder = LocalPlayerServiceBinder.current
    val saveableStateHolder = rememberSaveableStateHolder()
    val (tabIndex, onTabIndexChanges) = rememberPreference(searchResultScreenTabIndexKey, 0)


    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }
    val hapticFeedback = LocalHapticFeedback.current

    val isVideoEnabled = LocalContext.current.preferences.getBoolean(showButtonPlayerVideoKey, false)
    val parentalControlEnabled by rememberPreference(parentalControlEnabledKey, false)

    val disableScrollingText by rememberPreference(disableScrollingTextKey, false)

    //PersistMapCleanup(tagPrefix = "searchResults/$query/")

            val headerContent: @Composable (textButton: (@Composable () -> Unit)?) -> Unit = {
                Title(
                    title = stringResource(R.string.search_results_for),
                    verticalPadding = 4.dp
                )
                Title(
                    title = query,
                    icon = R.drawable.pencil,
                    onClick = {
                        /*
                                context.persistMap?.keys?.removeAll {
                                   it.startsWith("searchResults/$query/")
                                }
                                onSearchAgain()
                                */
                        navController.navigate("searchScreenRoute/${query}")
                    },
                    verticalPadding = 4.dp
                )
                Spacer(modifier = Modifier.height(12.dp))
                /*
                Header(
                    title = query,
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectTapGestures {
                                /*
                                context.persistMap?.keys?.removeAll {
                                   it.startsWith("searchResults/$query/")
                                }
                                onSearchAgain()
                                */
                                navController.navigate("searchScreenRoute/${query}")
                            }
                        }
                )
                 */
            }

            val emptyItemsText = stringResource(R.string.no_results_found)

            Skeleton(
                navController,
                tabIndex,
                onTabIndexChanges,
                miniPlayer,
                navBarContent = { item ->
                    item(0, stringResource(R.string.songs), R.drawable.musical_notes)
                    item(1, stringResource(R.string.albums), R.drawable.album)
                    item(2, stringResource(R.string.artists), R.drawable.artist)
                    item(3, stringResource(R.string.videos), R.drawable.video)
                    item(4, stringResource(R.string.playlists), R.drawable.playlist)
                    item(5, stringResource(R.string.featured), R.drawable.featured_playlist)
                    item(6, stringResource(R.string.podcasts), R.drawable.podcast)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(currentTabIndex) {
                    when ( currentTabIndex ) {
                        0 -> {
                            val localBinder = LocalPlayerServiceBinder.current
                            val menuState = LocalMenuState.current
                            val thumbnailSizeDp = Dimensions.thumbnails.song
                            val thumbnailSizePx = thumbnailSizeDp.px

                            ItemsPage(
                                tag = "searchResults/$query/songs",
                                itemsPageProvider = { continuation ->
                                    if (continuation == null) {
                                        Innertube.searchPage(
                                            body = SearchBody(
                                                query = query,
                                                params = Innertube.SearchFilter.Song.value
                                            ),
                                            fromMusicShelfRendererContent = Innertube.SongItem.Companion::from
                                        )
                                    } else {
                                        Innertube.searchPage(
                                            body = ContinuationBody(continuation = continuation),
                                            fromMusicShelfRendererContent = Innertube.SongItem.Companion::from
                                        )
                                    }
                                },
                                emptyItemsText = emptyItemsText,
                                headerContent = headerContent,
                                itemContent = { song ->
                                    //Log.d("mediaItem",song.toString())
                                    if (parentalControlEnabled && song.explicit)
                                        return@ItemsPage

                                    downloadState = getDownloadState(song.asMediaItem.mediaId)
                                    val isDownloaded =
                                        isDownloadedSong(song.asMediaItem.mediaId)

                                    SwipeablePlaylistItem(
                                        mediaItem = song.asMediaItem,
                                        onPlayNext = {
                                            localBinder?.player?.addNext(song.asMediaItem)
                                        },
                                        onDownload = {
                                            localBinder?.cache?.removeResource(song.asMediaItem.mediaId)
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
                                            localBinder?.player?.enqueue(song.asMediaItem)
                                        }
                                    ) {
                                        var forceRecompose by remember { mutableStateOf(false) }
                                        SongItem(
                                            song = song,
                                            onDownloadClick = {
                                                localBinder?.cache?.removeResource(song.asMediaItem.mediaId)
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
                                            downloadState = getDownloadState(song.asMediaItem.mediaId),
                                            thumbnailSizePx = thumbnailSizePx,
                                            thumbnailSizeDp = thumbnailSizeDp,
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
                                                        localBinder?.stopRadio()
                                                        localBinder?.player?.forcePlay(song.asMediaItem)
                                                        forceRecompose = true
                                                        localBinder?.setupRadio(song.info?.endpoint)
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

                        1 -> {
                            val thumbnailSizeDp = 108.dp
                            val thumbnailSizePx = thumbnailSizeDp.px

                            ItemsPage(
                                tag = "searchResults/$query/albums",
                                itemsPageProvider = { continuation ->
                                    if (continuation == null) {
                                        Innertube.searchPage(
                                            body = SearchBody(
                                                query = query,
                                                params = Innertube.SearchFilter.Album.value
                                            ),
                                            fromMusicShelfRendererContent = Innertube.AlbumItem::from
                                        )
                                    } else {
                                        Innertube.searchPage(
                                            body = ContinuationBody(continuation = continuation),
                                            fromMusicShelfRendererContent = Innertube.AlbumItem::from
                                        )
                                    }
                                },
                                emptyItemsText = emptyItemsText,
                                headerContent = headerContent,
                                itemContent = { album ->
                                    var albumPage by persist<Innertube.PlaylistOrAlbumPage?>("album/${album.key}/albumPage")
                                    SwipeableAlbumItem(
                                        albumItem = album,
                                        onPlayNext = {
                                            CoroutineScope(Dispatchers.IO).launch {
                                                Database
                                                    .album(album.key)
                                                    .combine(snapshotFlow { currentTabIndex }) { album, tabIndex -> album to tabIndex }
                                                    .collect {
                                                        if (albumPage == null)
                                                            withContext(Dispatchers.IO) {
                                                                Innertube.albumPage(
                                                                    BrowseBody(
                                                                        browseId = album.key
                                                                    )
                                                                )
                                                                    ?.onSuccess { currentAlbumPage ->
                                                                        albumPage =
                                                                            currentAlbumPage

                                                                        println("mediaItem success home album songsPage ${currentAlbumPage.songsPage} description ${currentAlbumPage.description} year ${currentAlbumPage.year}")

                                                                        albumPage
                                                                            ?.songsPage
                                                                            ?.items
                                                                            ?.map(
                                                                                Innertube.SongItem::asMediaItem
                                                                            )
                                                                            ?.let { it1 ->
                                                                                withContext(Dispatchers.Main) {
                                                                                    binder?.player?.addNext(
                                                                                        it1,
                                                                                        context
                                                                                    )
                                                                                }
                                                                            }
                                                                        println("mediaItem success add in queue album songsPage ${albumPage
                                                                            ?.songsPage
                                                                            ?.items?.size}")

                                                                    }
                                                                    ?.onFailure {
                                                                        println("mediaItem error searchResultScreen album ${it.stackTraceToString()}")
                                                                    }

                                                            }

                                                        //}
                                                    }

                                            }

                                        },
                                        onEnqueue = {
                                            CoroutineScope(Dispatchers.IO).launch {
                                                Database
                                                    .album(album.key)
                                                    .combine(snapshotFlow { currentTabIndex }) { album, tabIndex -> album to tabIndex }
                                                    .collect {
                                                        if (albumPage == null)
                                                            withContext(Dispatchers.IO) {
                                                                Innertube.albumPage(
                                                                    BrowseBody(
                                                                        browseId = album.key
                                                                    )
                                                                )
                                                                    ?.onSuccess { currentAlbumPage ->
                                                                        albumPage =
                                                                            currentAlbumPage

                                                                        println("mediaItem success home album songsPage ${currentAlbumPage.songsPage} description ${currentAlbumPage.description} year ${currentAlbumPage.year}")

                                                                        albumPage
                                                                            ?.songsPage
                                                                            ?.items
                                                                            ?.map(
                                                                                Innertube.SongItem::asMediaItem
                                                                            )
                                                                            ?.let { it1 ->
                                                                                withContext(Dispatchers.Main) {
                                                                                    binder?.player?.enqueue(
                                                                                        it1,
                                                                                        context
                                                                                    )
                                                                                }
                                                                            }
                                                                        println("mediaItem success add in queue album songsPage ${albumPage
                                                                            ?.songsPage
                                                                            ?.items?.size}")

                                                                    }
                                                                    ?.onFailure {
                                                                        println("mediaItem error searchResultScreen album ${it.stackTraceToString()}")
                                                                    }

                                                            }

                                                        //}
                                                    }

                                            }

                                        },
                                        onBookmark = {
                                            CoroutineScope(Dispatchers.IO).launch {
                                                Database
                                                    .album(album.key)
                                                    .combine(snapshotFlow { currentTabIndex }) { album, tabIndex -> album to tabIndex }
                                                    .collect {
                                                        if (albumPage == null)
                                                            withContext(Dispatchers.IO) {
                                                                Innertube.albumPage(
                                                                    BrowseBody(
                                                                        browseId = album.key
                                                                    )
                                                                )
                                                                    ?.onSuccess { currentAlbumPage ->
                                                                        albumPage =
                                                                            currentAlbumPage

                                                                        println("mediaItem success home album songsPage ${currentAlbumPage.songsPage} description ${currentAlbumPage.description} year ${currentAlbumPage.year}")

                                                                        Database.upsert(
                                                                            Album(
                                                                                id = album.key,
                                                                                title = currentAlbumPage.title,
                                                                                thumbnailUrl = currentAlbumPage.thumbnail?.url,
                                                                                year = currentAlbumPage.year,
                                                                                authorsText = currentAlbumPage.authors
                                                                                    ?.joinToString(
                                                                                        ""
                                                                                    ) {
                                                                                        it.name
                                                                                            ?: ""
                                                                                    },
                                                                                shareUrl = currentAlbumPage.url,
                                                                                timestamp = System.currentTimeMillis(),
                                                                                bookmarkedAt = System.currentTimeMillis()
                                                                            ),
                                                                            currentAlbumPage
                                                                                .songsPage
                                                                                ?.items
                                                                                ?.map(
                                                                                    Innertube.SongItem::asMediaItem
                                                                                )
                                                                                ?.onEach(
                                                                                    Database::insert
                                                                                )
                                                                                ?.mapIndexed { position, mediaItem ->
                                                                                    SongAlbumMap(
                                                                                        songId = mediaItem.mediaId,
                                                                                        albumId = album.key,
                                                                                        position = position
                                                                                    )
                                                                                }
                                                                                ?: emptyList()
                                                                        )

                                                                    }
                                                                    ?.onFailure {
                                                                        println("mediaItem error searchResultScreen album ${it.stackTraceToString()}")
                                                                    }

                                                            }
                                                    }
                                            }
                                        }
                                    ) {
                                        var albumById by remember { mutableStateOf<Album?>(null) }
                                        LaunchedEffect(album) {
                                            CoroutineScope(Dispatchers.IO).launch {
                                                albumById = Database.album(album.key).firstOrNull()
                                            }
                                        }
                                        AlbumItem(
                                            yearCentered = false,
                                            album = album,
                                            thumbnailSizePx = thumbnailSizePx,
                                            thumbnailSizeDp = thumbnailSizeDp,
                                            isYoutubeAlbum = albumById?.isYoutubeAlbum == true,
                                            modifier = Modifier
                                                .combinedClickable(
                                                    onClick = {
                                                        navController.navigate("${NavRoutes.album.name}/${album.key}")
                                                    },
                                                    onLongClick = {}

                                                ),
                                            disableScrollingText = disableScrollingText
                                        )
                                    }
                                },
                                itemPlaceholderContent = {
                                    AlbumItemPlaceholder(thumbnailSizeDp = thumbnailSizeDp)
                                }
                            )
                        }

                        2 -> {
                            val thumbnailSizeDp = 64.dp
                            val thumbnailSizePx = thumbnailSizeDp.px

                            ItemsPage(
                                tag = "searchResults/$query/artists",
                                itemsPageProvider = { continuation ->
                                    if (continuation == null) {
                                        Innertube.searchPage(
                                            body = SearchBody(
                                                query = query,
                                                params = Innertube.SearchFilter.Artist.value
                                            ),
                                            fromMusicShelfRendererContent = Innertube.ArtistItem::from
                                        )
                                    } else {
                                        Innertube.searchPage(
                                            body = ContinuationBody(continuation = continuation),
                                            fromMusicShelfRendererContent = Innertube.ArtistItem::from
                                        )
                                    }
                                },
                                emptyItemsText = emptyItemsText,
                                headerContent = headerContent,
                                itemContent = { artist ->
                                    var artistById by remember { mutableStateOf<Artist?>(null) }
                                    LaunchedEffect(artist) {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            artistById = Database.artist(artist.key).firstOrNull()
                                        }
                                    }
                                    ArtistItem(
                                        artist = artist,
                                        thumbnailSizePx = thumbnailSizePx,
                                        thumbnailSizeDp = thumbnailSizeDp,
                                        isYoutubeArtist = artistById?.isYoutubeArtist == true,
                                        modifier = Modifier
                                            .clickable(onClick = {
                                                navController.navigate("${NavRoutes.artist.name}/${artist.key}")
                                            }),
                                        disableScrollingText = disableScrollingText,
                                        smallThumbnail = true
                                    )
                                },
                                itemPlaceholderContent = {
                                    ArtistItemPlaceholder(thumbnailSizeDp = thumbnailSizeDp)
                                }
                            )
                        }

                        3 -> {
                            val localBinder = LocalPlayerServiceBinder.current
                            val menuState = LocalMenuState.current
                            val thumbnailHeightDp = 72.dp
                            val thumbnailWidthDp = 128.dp

                            ItemsPage(
                                tag = "searchResults/$query/videos",
                                itemsPageProvider = { continuation ->
                                    if (continuation == null) {
                                        Innertube.searchPage(
                                            body = SearchBody(
                                                query = query,
                                                params = Innertube.SearchFilter.Video.value
                                            ),
                                            fromMusicShelfRendererContent = Innertube.VideoItem::from
                                        )
                                    } else {
                                        Innertube.searchPage(
                                            body = ContinuationBody(continuation = continuation),
                                            fromMusicShelfRendererContent = Innertube.VideoItem::from
                                        )
                                    }
                                },
                                emptyItemsText = emptyItemsText,
                                headerContent = headerContent,
                                itemContent = { video ->
                                    SwipeablePlaylistItem(
                                        mediaItem = video.asMediaItem,
                                        onPlayNext = {
                                            localBinder?.player?.addNext(video.asMediaItem)
                                        },
                                        onDownload = {
                                            val message = context.resources.getString(R.string.downloading_videos_not_supported)

                                            SmartMessage(
                                                message,
                                                durationLong = false,
                                                context = context
                                            )
                                        },
                                        onEnqueue = {
                                            localBinder?.player?.enqueue(video.asMediaItem)
                                        }
                                    ) {
                                        VideoItem(
                                            video = video,
                                            thumbnailWidthDp = thumbnailWidthDp,
                                            thumbnailHeightDp = thumbnailHeightDp,
                                            modifier = Modifier
                                                .combinedClickable(
                                                    onLongClick = {
                                                        menuState.display {
                                                            NonQueuedMediaItemMenu(
                                                                navController = navController,
                                                                mediaItem = video.asMediaItem,
                                                                onDismiss = menuState::hide,
                                                                disableScrollingText = disableScrollingText
                                                            )
                                                        };
                                                        hapticFeedback.performHapticFeedback(
                                                            HapticFeedbackType.LongPress
                                                        )
                                                    },
                                                    onClick = {
                                                        localBinder?.stopRadio()
                                                        if (isVideoEnabled)
                                                            localBinder?.player?.playVideo(video.asMediaItem)
                                                        else
                                                            localBinder?.player?.forcePlay(video.asMediaItem)
                                                        //binder?.setupRadio(video.info?.endpoint)
                                                    }
                                                ),
                                            disableScrollingText = disableScrollingText
                                        )
                                    }
                                },
                                itemPlaceholderContent = {
                                    VideoItemPlaceholder(
                                        thumbnailHeightDp = thumbnailHeightDp,
                                        thumbnailWidthDp = thumbnailWidthDp
                                    )
                                }
                            )
                        }

                        4, 5 -> {
                            val thumbnailSizeDp = Dimensions.thumbnails.playlist
                            val thumbnailSizePx = thumbnailSizeDp.px
                            //val thumbnailSizeDp = 108.dp
                            //val thumbnailSizePx = thumbnailSizeDp.px

                            ItemsPage(
                                tag = "searchResults/$query/${
                                    when (currentTabIndex) {
                                        4 -> "playlists"
                                        else -> "featured"
                                    }
                                }",
                                itemsPageProvider = { continuation ->
                                    if (continuation == null) {
                                        val filter = when (currentTabIndex) {
                                            4 -> Innertube.SearchFilter.CommunityPlaylist
                                            else -> Innertube.SearchFilter.FeaturedPlaylist
                                        }

                                        Innertube.searchPage(
                                            body = SearchBody(query = query, params = filter.value),
                                            fromMusicShelfRendererContent = Innertube.PlaylistItem::from
                                        )
                                    } else {
                                        Innertube.searchPage(
                                            body = ContinuationBody(continuation = continuation),
                                            fromMusicShelfRendererContent = Innertube.PlaylistItem::from
                                        )
                                    }
                                },
                                emptyItemsText = emptyItemsText,
                                headerContent = headerContent,
                                itemContent = { playlist ->
                                    var playlistById by remember { mutableStateOf<Playlist?>(null) }
                                    LaunchedEffect(playlist) {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            playlistById = Database.playlist(playlist.key.substringAfter("VL")).firstOrNull()
                                        }
                                    }
                                    PlaylistItem(
                                        playlist = playlist,
                                        thumbnailSizePx = thumbnailSizePx,
                                        thumbnailSizeDp = thumbnailSizeDp,
                                        showSongsCount = false,
                                        isYoutubePlaylist = playlistById?.isYoutubePlaylist == true,
                                        modifier = Modifier
                                            .clickable(onClick = {
                                                //playlistRoute(playlist.key)
                                                navController.navigate("${NavRoutes.playlist.name}/${playlist.key}")
                                            }),
                                        disableScrollingText = disableScrollingText
                                    )
                                },
                                itemPlaceholderContent = {
                                    PlaylistItemPlaceholder(thumbnailSizeDp = thumbnailSizeDp)
                                }
                            )
                        }

                        6 -> {
                            val thumbnailSizeDp = Dimensions.thumbnails.playlist
                            val thumbnailSizePx = thumbnailSizeDp.px
                            //val thumbnailSizeDp = 108.dp
                            //val thumbnailSizePx = thumbnailSizeDp.px

                            ItemsPage(
                                tag = "searchResults/$query/podcasts",
                                itemsPageProvider = { continuation ->
                                    if (continuation == null) {
                                        val filter = Innertube.SearchFilter.Podcast

                                        Innertube.searchPage(
                                            body = SearchBody(query = query, params = filter.value),
                                            fromMusicShelfRendererContent = Innertube.PlaylistItem::from
                                        )
                                    } else {
                                        Innertube.searchPage(
                                            body = ContinuationBody(continuation = continuation),
                                            fromMusicShelfRendererContent = Innertube.PlaylistItem::from
                                        )
                                    }
                                },
                                emptyItemsText = emptyItemsText,
                                headerContent = headerContent,
                                itemContent = { playlist ->
                                    PlaylistItem(
                                        playlist = playlist,
                                        thumbnailSizePx = thumbnailSizePx,
                                        thumbnailSizeDp = thumbnailSizeDp,
                                        showSongsCount = false,
                                        modifier = Modifier
                                            .clickable(onClick = {
                                                //playlistRoute(playlist.key)
                                                println("mediaItem searchResultScreen playlist key ${playlist.key}")
                                                navController.navigate("${NavRoutes.podcast.name}/${playlist.key}")
                                            }),
                                        disableScrollingText = disableScrollingText
                                    )
                                },
                                itemPlaceholderContent = {
                                    PlaylistItemPlaceholder(thumbnailSizeDp = thumbnailSizeDp)
                                }
                            )
                        }
                    }
                }
            }
}
