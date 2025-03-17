package it.fast4x.rimusic.ui.screens.artist

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import it.fast4x.compose.persist.persist
import it.fast4x.compose.persist.persistList
import it.fast4x.environment.Environment
import it.fast4x.environment.EnvironmentExt
import it.fast4x.environment.models.BrowseEndpoint
import it.fast4x.environment.requests.ArtistItemsPage
import it.fast4x.environment.utils.completed
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.LocalPlayerAwareWindowInsets
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.appContext
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.themed.HeaderIconButton
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.ui.components.themed.Title
import it.fast4x.rimusic.ui.items.AlbumItem
import it.fast4x.rimusic.ui.items.PlaylistItem
import it.fast4x.rimusic.ui.items.SongItem
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.px
import it.fast4x.rimusic.utils.asMediaItem
import it.fast4x.rimusic.utils.forcePlay
import org.dailyislam.android.utilities.getHttpClient
import it.fast4x.rimusic.utils.languageDestination
import it.fast4x.rimusic.utils.parentalControlEnabledKey
import it.fast4x.rimusic.utils.rememberPreference
import me.bush.translator.Translator
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.enums.MaxSongs
import it.fast4x.rimusic.enums.PopupType
import it.fast4x.rimusic.isVideoEnabled
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.models.Album
import it.fast4x.rimusic.ui.components.SwipeablePlaylistItem
import it.fast4x.rimusic.ui.components.themed.AddToPlaylistArtistSongs
import it.fast4x.rimusic.ui.components.themed.ConfirmationDialog
import it.fast4x.rimusic.ui.components.themed.NonQueuedMediaItemMenu
import it.fast4x.rimusic.ui.components.themed.NowPlayingSongIndicator
import it.fast4x.rimusic.ui.components.themed.TitleSection
import it.fast4x.rimusic.ui.items.ArtistItem
import it.fast4x.rimusic.ui.items.VideoItem
import it.fast4x.rimusic.ui.screens.settings.isYouTubeSyncEnabled
import it.fast4x.rimusic.utils.addNext
import it.fast4x.rimusic.utils.addToYtLikedSongs
import it.fast4x.rimusic.utils.asSong
import it.fast4x.rimusic.utils.enqueue
import it.fast4x.rimusic.utils.forcePlayAtIndex
import it.fast4x.rimusic.utils.forcePlayFromBeginning
import it.fast4x.rimusic.utils.formatAsDuration
import it.fast4x.rimusic.utils.getDownloadState
import it.fast4x.rimusic.utils.isDownloadedSong
import it.fast4x.rimusic.utils.isExplicit
import org.dailyislam.android.utilities.isNetworkConnected
import it.fast4x.rimusic.utils.isNowPlaying
import it.fast4x.rimusic.utils.manageDownload
import it.fast4x.rimusic.utils.maxSongsInQueueKey
import it.fast4x.rimusic.utils.playVideo
import it.fast4x.rimusic.utils.setLikeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
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

    var showYoutubeLikeConfirmDialog by remember {
        mutableStateOf(false)
    }

    var notLikedSongs by persistList<MediaItem>("")
    var totalMinutesToLike by remember { mutableStateOf("") }

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
    val maxSongsInQueue by rememberPreference(maxSongsInQueueKey, MaxSongs.`500`)
    var forceRecompose by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        artistItemsPage = EnvironmentExt.getArtistItemsPage(
            BrowseEndpoint(
                browseId = browseId,
                params = params
            )
        ).completed().getOrNull()

        println("ArtistOverviewItems artistItemsPage size: ${artistItemsPage?.items}")
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

        if (artistItemsPage?.items?.firstOrNull() is Environment.SongItem) {
            val artistSongs = artistItemsPage!!.items
                .map{it as Environment.SongItem}
                .map { it.asMediaItem }

            if (showConfirmDownloadAllDialog) {
                ConfirmationDialog(
                    text = stringResource(R.string.do_you_really_want_to_download_all),
                    onDismiss = { showConfirmDownloadAllDialog = false },
                    onConfirm = {
                        showConfirmDownloadAllDialog = false
                        downloadState = Download.STATE_DOWNLOADING
                        Database.asyncTransaction {
                        if (artistSongs.any { getLikedAt(it.mediaId) != -1L }) {
                            artistSongs.filter { getLikedAt(it.mediaId) != -1L }.forEach {
                                binder?.cache?.removeResource(it.mediaId)
                                Database.insert(
                                    Song(
                                        id = it.mediaId,
                                        title = it.mediaMetadata.title.toString(),
                                        artistsText = it.mediaMetadata.artist.toString(),
                                        thumbnailUrl = it.asSong.thumbnailUrl,
                                        durationText = null
                                    )
                                )
                                manageDownload(
                                    context = context,
                                    mediaItem = it,
                                    downloadState = false
                                )
                            }
                        }
                        }
                    }
                )
            }

            if (showConfirmDeleteDownloadDialog) {
                ConfirmationDialog(
                    text = stringResource(R.string.do_you_really_want_to_delete_download),
                    onDismiss = { showConfirmDeleteDownloadDialog = false },
                    onConfirm = {
                        showConfirmDeleteDownloadDialog = false
                        downloadState = Download.STATE_DOWNLOADING
                        if (artistSongs.isNotEmpty()) {
                            artistSongs.forEach {
                                binder?.cache?.removeResource(it.mediaId)
                                manageDownload(
                                    context = context,
                                    mediaItem = it,
                                    downloadState = true
                                )
                            }
                        }
                    }
                )
            }

            if (showYoutubeLikeConfirmDialog) {
                Database.asyncTransaction {
                    notLikedSongs = artistSongs.filter { getLikedAt(it.mediaId) in listOf(-1L,null)}
                }
                totalMinutesToLike = formatAsDuration(notLikedSongs.size.toLong()*1000)
                ConfirmationDialog(
                    text = "$totalMinutesToLike "+stringResource(R.string.do_you_really_want_to_like_all),
                    onDismiss = { showYoutubeLikeConfirmDialog = false },
                    onConfirm = {
                        showYoutubeLikeConfirmDialog = false
                        CoroutineScope(Dispatchers.IO).launch {
                            addToYtLikedSongs(notLikedSongs)
                        }
                    }
                )
            }

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
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .padding(vertical = 10.dp)
                            .fillMaxWidth()){
                        HeaderIconButton(
                            icon = R.drawable.shuffle,
                            //enabled = artistSongs.any { it.mediaMetadata.artworkUri.toString() != "" && it.song.likedAt != -1L },
                            color = if (artistSongs.any { it.asSong.thumbnailUrl != "" }) colorPalette().text else colorPalette().textDisabled,
                            onClick = {},
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {
                                        coroutineScope.launch(Dispatchers.IO) {
                                            if (artistSongs.any { Database.getLikedAt(it.mediaId) != -1L }) {
                                                artistSongs.filter { Database.getLikedAt(it.mediaId) != -1L }
                                                    .let { songs ->
                                                        if (songs.isNotEmpty()) {
                                                            val itemsLimited =
                                                                if (songs.size > maxSongsInQueue.number) songs.shuffled()
                                                                    .take(maxSongsInQueue.number.toInt()) else songs
                                                            withContext(Dispatchers.Main){
                                                                binder?.stopRadio()
                                                                binder?.player?.forcePlayFromBeginning(
                                                                    itemsLimited.shuffled()
                                                                )
                                                            }
                                                        }
                                                    }
                                            } else {
                                                SmartMessage(
                                                    context.resources.getString(R.string.disliked_this_collection),
                                                    type = PopupType.Error,
                                                    context = context
                                                )
                                            }
                                        }
                                    },
                                    onLongClick = {
                                        SmartMessage(context.resources.getString(R.string.info_shuffle), context = context)
                                    }
                                )
                        )
                        HeaderIconButton(
                            icon = R.drawable.enqueue,
                            //enabled = artistSongs.any { it.mediaMetadata.artworkUri.toString() != "" && it.song.likedAt != -1L },
                            color = if (artistSongs.any { it.asSong.thumbnailUrl != "" }) colorPalette().text else colorPalette().textDisabled,
                            onClick = {},
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {
                                        coroutineScope.launch(Dispatchers.IO) {
                                            if (artistSongs.any { Database.getLikedAt(it.mediaId) != -1L }) {
                                                val filteredArtistSongs = artistSongs.filter {Database.getLikedAt(it.mediaId) != -1L}
                                                withContext(Dispatchers.Main) {
                                                    binder?.player?.enqueue(filteredArtistSongs, context)
                                                }
                                            } else {
                                                SmartMessage(context.resources.getString(R.string.disliked_this_collection),type = PopupType.Error, context = context)
                                            }
                                        }
                                    },
                                    onLongClick = {
                                        SmartMessage(context.resources.getString(R.string.info_enqueue_songs), context = context)
                                    }
                                )
                        )
                        HeaderIconButton(
                            icon = R.drawable.play_skip_forward,
                            //enabled = artistSongs.any { it.mediaMetadata.artworkUri.toString() != "" && it.song.likedAt != -1L },
                            color = if (artistSongs.any { it.asSong.thumbnailUrl != "" }) colorPalette().text else colorPalette().textDisabled,
                            onClick = {},
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {
                                        coroutineScope.launch(Dispatchers.IO) {
                                            if (artistSongs.any { Database.getLikedAt(it.mediaId) != -1L }) {
                                                val filteredArtistSongs = artistSongs.filter {Database.getLikedAt(it.mediaId) != -1L}
                                                withContext(Dispatchers.Main) {
                                                    binder?.player?.addNext(filteredArtistSongs, context)
                                                }
                                            } else {
                                                SmartMessage(context.resources.getString(R.string.disliked_this_collection),type = PopupType.Error, context = context)
                                            }
                                        }
                                    },
                                    onLongClick = {
                                        SmartMessage(context.resources.getString(R.string.play_next), context = context)
                                    }
                                )
                        )
                        HeaderIconButton(
                            icon = R.drawable.downloaded,
                            //enabled = playlistSongs.any { it.song.likedAt != -1L },
                            color = if (artistSongs.any { it.asSong.thumbnailUrl != "" }) colorPalette().text else colorPalette().textDisabled,
                            onClick = {},
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {
                                        showConfirmDownloadAllDialog = true
                                    },
                                    onLongClick = {
                                        SmartMessage(context.resources.getString(R.string.info_download_all_songs), context = context)
                                    }
                                )
                        )
                        HeaderIconButton(
                            icon = R.drawable.download,
                            //enabled = playlistSongs.any { it.song.likedAt != -1L },
                            color = if (artistSongs.any { it.asSong.thumbnailUrl != "" }) colorPalette().text else colorPalette().textDisabled,
                            onClick = {},
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {
                                        if (artistSongs.any { it.asSong.thumbnailUrl != "" }) {
                                            showConfirmDeleteDownloadDialog = true
                                        } else {
                                            SmartMessage(context.resources.getString(R.string.disliked_this_collection),type = PopupType.Error, context = context)
                                        }
                                    },
                                    onLongClick = {
                                        SmartMessage(context.resources.getString(R.string.info_remove_all_downloaded_songs), context = context)
                                    }
                                )
                        )
                        HeaderIconButton(
                            icon = R.drawable.add_in_playlist,
                            color = colorPalette().text,
                            onClick = {},
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .combinedClickable(
                                    onClick = {
                                        menuState.display {
                                            AddToPlaylistArtistSongs(
                                                navController = navController,
                                                onDismiss = {
                                                    menuState.hide()
                                                    forceRecompose = true
                                                },
                                                mediaItems = artistSongs,
                                                onClosePlayer = {
                                                    onDismiss()
                                                },
                                            )
                                        }
                                    },
                                    onLongClick = {
                                        SmartMessage(
                                            context.resources.getString(R.string.info_add_in_playlist),
                                            context = context
                                        )
                                    }
                                )
                        )
                        HeaderIconButton(
                            icon = R.drawable.heart,
                            enabled = artistSongs.isNotEmpty(),
                            color = colorPalette().text,
                            onClick = {},
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .combinedClickable(
                                    onClick = {
                                        if (!isNetworkConnected(appContext()) && isYouTubeSyncEnabled()) {
                                            SmartMessage(appContext().resources.getString(R.string.no_connection), context = appContext(), type = PopupType.Error)
                                        } else if (!isYouTubeSyncEnabled()){
                                            artistSongs.forEachIndexed { _, song ->
                                                Database.asyncTransaction {
                                                    if (like(song.mediaId, setLikeState(song.asSong.likedAt)) == 0
                                                    ) {
                                                        insert(song, Song::toggleLike)
                                                    }
                                                }
                                            }
                                            SmartMessage(
                                                context.resources.getString(R.string.done),
                                                context = context
                                            )
                                        } else {
                                            showYoutubeLikeConfirmDialog = true
                                        }
                                    },
                                    onLongClick = {
                                        SmartMessage(
                                            context.resources.getString(R.string.add_to_favorites),
                                            context = context
                                        )
                                    }
                                )
                        )
                    }
                }
                items(artistSongs) { item ->

                    println("ArtistOverviewItems item: ${item}")

                    if (parentalControlEnabled && item.isExplicit) return@items

                    downloadState = getDownloadState(item.mediaId)
                    val isDownloaded = isDownloadedSong(item.mediaId)

                    SwipeablePlaylistItem(
                        mediaItem = item,
                        onPlayNext = {
                            binder?.player?.addNext(item)
                        },
                        onDownload = {
                            binder?.cache?.removeResource(item.mediaId)
                            CoroutineScope(Dispatchers.IO).launch {
                                Database.resetContentLength( item.mediaId )
                            }

                            manageDownload(
                                context = context,
                                mediaItem = item,
                                downloadState = isDownloaded
                            )
                        },
                        onEnqueue = {
                            binder?.player?.enqueue(item)
                        }
                    ) {
                        SongItem(
                            song = item,
                            onDownloadClick = {
                                binder?.cache?.removeResource(item.mediaId)
                                CoroutineScope(Dispatchers.IO).launch {
                                    Database.deleteFormat( item.mediaId )
                                }

                                manageDownload(
                                    context = context,
                                    mediaItem = item,
                                    downloadState = isDownloaded
                                )
                            },
                            onThumbnailContent = {
                                NowPlayingSongIndicator(item.mediaId, binder?.player)
                            },
                            downloadState = getDownloadState(item.mediaId),
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
                                                onInfo = {
                                                    navController.navigate("${NavRoutes.videoOrSongInfo.name}/${item.mediaId}")
                                                },
                                                mediaItem = item,
                                                disableScrollingText = disableScrollingText
                                            )
                                        };
                                        hapticFeedback.performHapticFeedback(
                                            HapticFeedbackType.LongPress
                                        )
                                    },
                                    onClick = {
                                        coroutineScope.launch(Dispatchers.IO) {
                                            val filteredArtistSongs = artistSongs.filter {Database.getLikedAt(it.mediaId) != -1L}
                                            if (item in filteredArtistSongs){
                                                withContext(Dispatchers.Main) {
                                                    binder?.player?.forcePlayAtIndex(
                                                        filteredArtistSongs,
                                                        filteredArtistSongs.indexOf(item)
                                                    )
                                                }
                                            } else {
                                                SmartMessage(context.resources.getString(R.string.disliked_this_song),type = PopupType.Error, context = context)
                                            }

                                        }
                                    }
                                ),
                            disableScrollingText = disableScrollingText,
                            isNowPlaying = binder?.player?.isNowPlaying(item.mediaId) ?: false,
                            forceRecompose = forceRecompose
                        )
                    }
                    /*else -> {}
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
//                        }*/

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
                        is Environment.AlbumItem -> {
                            var albumById by remember { mutableStateOf<Album?>(null) }
                            LaunchedEffect(item) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    albumById = Database.album(item.key).firstOrNull()
                                }
                            }
                            AlbumItem(
                                album = item,
                                thumbnailSizePx = thumbnailSizePx,
                                thumbnailSizeDp = thumbnailSizeDp,
                                alternative = true,
                                yearCentered = true,
                                showAuthors = true,
                                isYoutubeAlbum = albumById?.isYoutubeAlbum == true,
                                modifier = Modifier.clickable(onClick = {
                                    navController.navigate(route = "${NavRoutes.album.name}/${item.key}")
                                }),
                                disableScrollingText = disableScrollingText
                            )
                        }
                        is Environment.PlaylistItem -> {
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
                        is Environment.ArtistItem -> {
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
