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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyRow
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
import it.fast4x.environment.Environment
import it.fast4x.environment.models.bodies.BrowseBody
import it.fast4x.environment.requests.itemsPage
import it.fast4x.environment.utils.from
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
import it.fast4x.rimusic.ui.components.SwipeablePlaylistItem
import it.fast4x.rimusic.ui.components.themed.AutoResizeText
import it.fast4x.rimusic.ui.components.themed.ConfirmationDialog
import it.fast4x.rimusic.ui.components.themed.FontSizeRange
import it.fast4x.rimusic.ui.components.themed.HeaderIconButton
import it.fast4x.rimusic.ui.components.themed.IconButton
import it.fast4x.rimusic.ui.components.themed.LayoutWithAdaptiveThumbnail
import it.fast4x.rimusic.ui.components.themed.MultiFloatingActionsContainer
import it.fast4x.rimusic.ui.components.themed.NonQueuedMediaItemMenu
import it.fast4x.rimusic.ui.components.themed.SecondaryTextButton
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.ui.components.themed.TextPlaceholder
import it.fast4x.rimusic.ui.components.themed.Title
import it.fast4x.rimusic.ui.components.themed.Title2Actions
import it.fast4x.rimusic.ui.items.AlbumItem
import it.fast4x.rimusic.ui.items.AlbumItemPlaceholder
import it.fast4x.rimusic.ui.items.PlaylistItem
import it.fast4x.rimusic.ui.items.SongItem
import it.fast4x.rimusic.ui.items.SongItemPlaceholder
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.px
import it.fast4x.rimusic.utils.addNext
import it.fast4x.rimusic.utils.align
import it.fast4x.rimusic.utils.asMediaItem
import it.fast4x.rimusic.utils.color
import it.fast4x.rimusic.utils.conditional
import it.fast4x.rimusic.utils.enqueue
import it.fast4x.rimusic.utils.fadingEdge
import it.fast4x.rimusic.utils.forcePlay
import it.fast4x.rimusic.utils.getDownloadState
import it.fast4x.rimusic.utils.getHttpClient
import it.fast4x.rimusic.utils.isDownloadedSong
import it.fast4x.rimusic.utils.isLandscape
import it.fast4x.rimusic.utils.isNowPlaying
import it.fast4x.rimusic.utils.languageDestination
import it.fast4x.rimusic.utils.manageDownload
import it.fast4x.rimusic.utils.medium
import it.fast4x.rimusic.utils.parentalControlEnabledKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.resize
import it.fast4x.rimusic.utils.secondary
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.showFloatingIconKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.bush.translator.Language
import me.bush.translator.Translator
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.typography
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@UnstableApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun ArtistOverview(
    navController: NavController,
    browseId: String?,
    youtubeArtistPage: Environment.ArtistInfoPage?,
    onViewAllSongsClick: () -> Unit,
    onViewAllAlbumsClick: () -> Unit,
    onViewAllSinglesClick: () -> Unit,
    onAlbumClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    thumbnailContent: @Composable () -> Unit,
    headerContent: @Composable (textButton: (@Composable () -> Unit)?) -> Unit,
    disableScrollingText: Boolean
) {
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val windowInsets = LocalPlayerAwareWindowInsets.current

    val songThumbnailSizeDp = Dimensions.thumbnails.song
    val songThumbnailSizePx = songThumbnailSizeDp.px
    val albumThumbnailSizeDp = 108.dp
    val albumThumbnailSizePx = albumThumbnailSizeDp.px

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

    var artist by persist<Artist?>("artist/$browseId/artist")
    val hapticFeedback = LocalHapticFeedback.current
    val parentalControlEnabled by rememberPreference(parentalControlEnabledKey, false)

    LaunchedEffect(Unit) {
        if (browseId != null) {
            Database.artist(browseId).collect { artist = it }
        }
    }

    LayoutWithAdaptiveThumbnail(thumbnailContent = thumbnailContent) {
        Box(
            modifier = Modifier
                .background(colorPalette().background0)
                //.fillMaxSize()
                .fillMaxHeight()
                .fillMaxWidth(
                    if( NavigationBarPosition.Right.isCurrent() )
                        Dimensions.contentWidthRightBar
                    else
                        1f
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(colorPalette().background0)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    /*
                    .padding(
                        windowInsets
                            .only(WindowInsetsSides.Vertical)
                            .asPaddingValues()
                    )
                     */
            ) {

                /*val modifierArt = if (isLandscape) Modifier.fillMaxWidth() else Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3)*/

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    if (youtubeArtistPage != null) {
                        if(!isLandscape)
                            AsyncImage(
                                model = youtubeArtistPage?.thumbnail?.url?.resize(1200, 1200),
                                contentDescription = "loading...",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.Center)
                                    .fadingEdge(
                                        top = WindowInsets.systemBars
                                            .asPaddingValues()
                                            .calculateTopPadding() + Dimensions.fadeSpacingTop,
                                        bottom = Dimensions.fadeSpacingBottom
                                    )
                            )

                        AutoResizeText(
                            text = youtubeArtistPage.name.toString(),
                            style = typography().l.semiBold,
                            fontSizeRange = FontSizeRange(32.sp, 38.sp),
                            fontWeight = typography().l.semiBold.fontWeight,
                            fontFamily = typography().l.semiBold.fontFamily,
                            color = typography().l.semiBold.color,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 30.dp)
                                .conditional(!disableScrollingText) { basicMarquee(iterations = Int.MAX_VALUE)}
                                //.padding(bottom = 5.dp)
                        )

                        /*
                        youtubeArtistPage.subscriberCountText?.let {
                            BasicText(
                                text = String.format(
                                    stringResource(R.string.artist_subscribers),
                                    it
                                ),
                                style = typography().xs.semiBold,
                                maxLines = 1,
                                modifier = Modifier
                                    //.padding(top = 10.dp)
                                    .align(Alignment.BottomCenter)
                            )
                        }
                         */
                        HeaderIconButton(
                            icon = R.drawable.share_social,
                            color = colorPalette().text,
                            iconSize = 24.dp,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 5.dp, end = 5.dp),
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "https://music.youtube.com/channel/$browseId"
                                    )
                                }

                                context.startActivity(Intent.createChooser(sendIntent, null))
                            }
                        )

                    } else {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(4f / 3)
                        ) {
                            ShimmerHost {
                                AlbumItemPlaceholder(
                                    thumbnailSizeDp = 200.dp,
                                    alternative = true
                                )
                            }
                        }
                    }
                }

                youtubeArtistPage?.subscriberCountText?.let {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        BasicText(
                            text = String.format(
                                stringResource(R.string.artist_subscribers),
                                it
                            ),
                            style = typography().xs.semiBold,
                            maxLines = 1
                        )
                    }
                }


                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .fillMaxWidth()
                ) {
                    SecondaryTextButton(
                        text = if (artist?.bookmarkedAt == null) stringResource(R.string.follow) else stringResource(
                            R.string.following
                        ),
                        onClick = {
                            val bookmarkedAt =
                                if (artist?.bookmarkedAt == null) System.currentTimeMillis() else null
                            //CoroutineScope(Dispatchers.IO).launch {
                            Database.asyncTransaction {
                                    artist?.copy(bookmarkedAt = bookmarkedAt)
                                          ?.let( ::update )
                                }
                            //}
                        },
                        alternative = artist?.bookmarkedAt == null,
                        modifier = Modifier.padding(end = 30.dp)
                    )

                    HeaderIconButton(
                        icon = R.drawable.downloaded,
                        color = colorPalette().text,
                        onClick = {},
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                            .combinedClickable(
                                onClick = {
                                    showConfirmDownloadAllDialog = true
                                },
                                onLongClick = {
                                    SmartMessage(context.resources.getString(R.string.info_download_all_songs), context = context)
                                }
                            )
                    )

                    if (showConfirmDownloadAllDialog) {
                        ConfirmationDialog(
                            text = stringResource(R.string.do_you_really_want_to_download_all),
                            onDismiss = { showConfirmDownloadAllDialog = false },
                            onConfirm = {
                                showConfirmDownloadAllDialog = false
                                downloadState = Download.STATE_DOWNLOADING
                                if (youtubeArtistPage?.songs?.isNotEmpty() == true)
                                    youtubeArtistPage.songs?.forEach {
                                        binder?.cache?.removeResource(it.asMediaItem.mediaId)
                                        CoroutineScope(Dispatchers.IO).launch {
                                            Database.deleteFormat( it.asMediaItem.mediaId )
                                        }
                                        manageDownload(
                                            context = context,
                                            mediaItem = it.asMediaItem,
                                            downloadState = false
                                        )
                                    }
                            }
                        )
                    }

                    HeaderIconButton(
                        icon = R.drawable.download,
                        color = colorPalette().text,
                        onClick = {},
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                            .combinedClickable(
                                onClick = {
                                    showConfirmDeleteDownloadDialog = true
                                },
                                onLongClick = {
                                    SmartMessage(context.resources.getString(R.string.info_remove_all_downloaded_songs), context = context)
                                }
                            )
                    )

                    if (showConfirmDeleteDownloadDialog) {
                        ConfirmationDialog(
                            text = stringResource(R.string.do_you_really_want_to_delete_download),
                            onDismiss = { showConfirmDeleteDownloadDialog = false },
                            onConfirm = {
                                showConfirmDeleteDownloadDialog = false
                                downloadState = Download.STATE_DOWNLOADING
                                if (youtubeArtistPage?.songs?.isNotEmpty() == true)
                                    youtubeArtistPage.songs?.forEach {
                                        binder?.cache?.removeResource(it.asMediaItem.mediaId)
                                        CoroutineScope(Dispatchers.IO).launch {
                                            Database.deleteFormat( it.asMediaItem.mediaId )
                                        }
                                        manageDownload(
                                            context = context,
                                            mediaItem = it.asMediaItem,
                                            downloadState = true
                                        )
                                    }
                            }
                        )
                    }

                    youtubeArtistPage?.shuffleEndpoint?.let { endpoint ->
                        HeaderIconButton(
                            icon = R.drawable.shuffle,
                            enabled = true,
                            color = colorPalette().text,
                            onClick = {},
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .combinedClickable(
                                    onClick = {
                                        binder?.stopRadio()
                                        binder?.playRadio(endpoint)
                                    },
                                    onLongClick = {
                                        SmartMessage(context.resources.getString(R.string.info_shuffle), context = context)
                                    }
                                )
                        )
                    }

                    youtubeArtistPage?.radioEndpoint?.let { endpoint ->
                        HeaderIconButton(
                            icon = R.drawable.radio,
                            enabled = true,
                            color = colorPalette().text,
                            onClick = {},
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .combinedClickable(
                                    onClick = {
                                        binder?.stopRadio()
                                        binder?.playRadio(endpoint)
                                    },
                                    onLongClick = {
                                        SmartMessage(context.resources.getString(R.string.info_start_radio), context = context)
                                    }
                                )
                        )
                    }
                    youtubeArtistPage?.songs?.let { songs ->
                        HeaderIconButton(
                            icon = R.drawable.enqueue,
                            enabled = true,
                            color = colorPalette().text,
                            onClick = {},
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .combinedClickable(
                                    onClick = {
                                        binder?.player?.enqueue(
                                            songs.map(Environment.SongItem::asMediaItem),
                                            context
                                        )
                                    },
                                    onLongClick = {
                                        SmartMessage(context.resources.getString(R.string.info_enqueue_songs), context = context)
                                    }
                                )
                        )
                    }
                }

                if (youtubeArtistPage != null) {

                    youtubeArtistPage.songs?.let { allSongs ->

                        val songs = if (parentalControlEnabled)
                            allSongs.filter { !it.explicit } else allSongs
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(endPaddingValues)
                        ) {
                            Title(
                                title = stringResource(R.string.songs),
                                onClick = {
                                    //if (youtubeArtistPage.songsEndpoint?.browseId != null) {
                                        onViewAllSongsClick()
                                    //} else SmartToast(context.resources.getString(R.string.info_no_songs_yet))
                                },
                                //modifier = Modifier.fillMaxWidth(0.7f)
                            )

                        }

                        songs.forEachIndexed { index, song ->
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
                                    downloadState = downloadState,
                                    thumbnailSizeDp = songThumbnailSizeDp,
                                    thumbnailSizePx = songThumbnailSizePx,
                                    modifier = Modifier
                                        .combinedClickable(
                                            onLongClick = {
                                                menuState.display {
                                                    NonQueuedMediaItemMenu(
                                                        navController = navController,
                                                        onDismiss = menuState::hide,
                                                        mediaItem = song.asMediaItem,
                                                        disableScrollingText = disableScrollingText
                                                    )
                                                }
                                                hapticFeedback.performHapticFeedback(
                                                    HapticFeedbackType.LongPress
                                                )
                                            },
                                            onClick = {
                                                binder?.stopRadio()
                                                binder?.player?.forcePlay(song.asMediaItem)

                                                CoroutineScope(Dispatchers.IO).launch {
                                                    youtubeArtistPage
                                                        .songsEndpoint
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
                                                                binder?.player?.addMediaItems(
                                                                    it.filterNot { it.mediaId == song.key }
                                                                )
                                                            }
                                                        }
                                                }

                                            }
                                        )
                                        .padding(endPaddingValues),
                                    disableScrollingText = disableScrollingText,
                                    isNowPlaying = binder?.player?.isNowPlaying(song.key) ?: false
                                )
                            }
                        }
                    }

                    youtubeArtistPage.playlists?.let { playlists ->
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(endPaddingValues)
                        ) {
                            Title(
                                title = stringResource(R.string.playlists),
                                onClick = {
                                    //if (youtubeArtistPage.albumsEndpoint?.browseId != null) {
                                    //onViewAllAlbumsClick()
                                    //} else SmartToast(context.resources.getString(R.string.info_no_albums_yet))
                                }
                            )
                        }

                        LazyRow(
                            contentPadding = endPaddingValues,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            items(
                                items = playlists,
                                key = Environment.PlaylistItem::key
                            ) { playlist ->
                                PlaylistItem(
                                    playlist = playlist,
                                    thumbnailSizePx = albumThumbnailSizePx,
                                    thumbnailSizeDp = albumThumbnailSizeDp,
                                    alternative = true,
                                    modifier = Modifier
                                        .clickable(onClick = { onPlaylistClick(playlist.key) }),
                                    disableScrollingText = disableScrollingText
                                )
                            }
                        }
                    }

                    youtubeArtistPage.albums?.let { albums ->
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(endPaddingValues)
                        ) {
                            Title2Actions(
                                title = stringResource(R.string.albums),
                                onClick1 = {
                                    //if (youtubeArtistPage.albumsEndpoint?.browseId != null) {
                                        onViewAllAlbumsClick()
                                    //} else SmartToast(context.resources.getString(R.string.info_no_albums_yet))
                                },
                                icon2 = R.drawable.dice,
                                onClick2 = {
                                    if (albums.isEmpty()) return@Title2Actions
                                    val albumId = albums.get(
                                        if (albums.size > 1)
                                            Random(System.currentTimeMillis()).nextInt(0, albums.size-1)
                                        else 0
                                    ).key
                                    navController.navigate(route = "${NavRoutes.album.name}/${albumId}")
                                }
                            )
                            /*
                            BasicText(
                                text = stringResource(R.string.albums),
                                style = typography().m.semiBold,
                                modifier = sectionTextModifier
                            )

                            youtubeArtistPage.albumsEndpoint?.let {
                                BasicText(
                                    text = stringResource(R.string.view_all),
                                    style = typography().xs.secondary,
                                    modifier = sectionTextModifier
                                        .clickable(onClick = onViewAllAlbumsClick),
                                )
                            }
                             */
                        }

                        LazyRow(
                            contentPadding = endPaddingValues,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            items(
                                items = albums,
                                key = Environment.AlbumItem::key
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

                    youtubeArtistPage.singles?.let { singles ->
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(endPaddingValues)
                        ) {
                            Title(
                                title = stringResource(R.string.singles),
                                onClick = {
                                    //if (youtubeArtistPage.singlesEndpoint?.browseId != null) {
                                        onViewAllSinglesClick()
                                    //} else SmartToast(context.resources.getString(R.string.info_no_singles_yet))
                                }
                            )
                            /*
                            BasicText(
                                text = stringResource(R.string.singles),
                                style = typography().m.semiBold,
                                modifier = sectionTextModifier
                            )

                            youtubeArtistPage.singlesEndpoint?.let {
                                BasicText(
                                    text = stringResource(R.string.view_all),
                                    style = typography().xs.secondary,
                                    modifier = sectionTextModifier
                                        .clickable(onClick = onViewAllSinglesClick),
                                )
                            }
                             */
                        }

                        LazyRow(
                            contentPadding = endPaddingValues,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            items(
                                items = singles,
                                key = Environment.AlbumItem::key
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

                    youtubeArtistPage.description?.let { description ->
                        val attributionsIndex = description.lastIndexOf("\n\nFrom Wikipedia")

                        BasicText(
                            text = stringResource(R.string.information),
                            style = typography().m.semiBold.align(TextAlign.Start),
                            modifier = sectionTextModifier
                                .fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier
                                //.padding(top = 16.dp)
                                .padding(vertical = 16.dp, horizontal = 8.dp)
                                //.padding(endPaddingValues)
                                //.padding(end = Dimensions.bottomSpacer)
                        ) {
                            IconButton(
                                icon = R.drawable.translate,
                                color = if (translateEnabled == true) colorPalette().text else colorPalette().textDisabled,
                                enabled = true,
                                onClick = {},
                                modifier = Modifier
                                    .padding(all = 8.dp)
                                    .size(18.dp)
                                    .combinedClickable(
                                        onClick = {
                                            translateEnabled = !translateEnabled
                                        },
                                        onLongClick = {
                                            SmartMessage(context.resources.getString(R.string.info_translation), context = context)
                                        }
                                    )
                            )
                            BasicText(
                                text = "“",
                                style = typography().xxl.semiBold,
                                modifier = Modifier
                                    .offset(y = (-8).dp)
                                    .align(Alignment.Top)
                            )

                            var translatedText by remember { mutableStateOf("") }
                            val nonTranslatedText by remember { mutableStateOf(
                                    if (attributionsIndex == -1) {
                                        description
                                    } else {
                                        description.substring(0, attributionsIndex)
                                    }
                                )
                            }


                            if (translateEnabled == true) {
                                LaunchedEffect(Unit) {
                                    val result = withContext(Dispatchers.IO) {
                                        try {
                                            translator.translate(
                                                nonTranslatedText,
                                                languageDestination,
                                                Language.AUTO
                                            ).translatedText
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                    translatedText =
                                        if (result.toString() == "kotlin.Unit") "" else result.toString()
                                }
                            } else translatedText = nonTranslatedText

                            BasicText(
                                text = translatedText,
                                style = typography().xxs.secondary.align(TextAlign.Justify),
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .weight(1f)
                            )

                            BasicText(
                                text = "„",
                                style = typography().xxl.semiBold,
                                modifier = Modifier
                                    .offset(y = 4.dp)
                                    .align(Alignment.Bottom)
                            )
                        }

                        if (attributionsIndex != -1) {
                            BasicText(
                                text = stringResource(R.string.from_wikipedia_cca),
                                style = typography().xxs.color(colorPalette().textDisabled).align(TextAlign.Start),
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 16.dp)
                                //.padding(endPaddingValues)
                            )
                        }

                    }
                } else {
                    ShimmerHost {
                        TextPlaceholder(modifier = sectionTextModifier)

                        repeat(5) {
                            SongItemPlaceholder(
                                thumbnailSizeDp = songThumbnailSizeDp,
                            )
                        }

                        BasicText(
                            text = stringResource(R.string.info_wait_it_may_take_a_few_minutes),
                            style = typography().xxs.medium,
                            maxLines = 1
                        )

                        repeat(2) {
                            TextPlaceholder(modifier = sectionTextModifier)

                            Row {
                                repeat(2) {
                                    AlbumItemPlaceholder(
                                        thumbnailSizeDp = albumThumbnailSizeDp,
                                        alternative = true
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.bottomSpacer))

            }

            val showFloatingIcon by rememberPreference(showFloatingIconKey, false)
            if( UiType.ViMusic.isCurrent() && showFloatingIcon )
                youtubeArtistPage?.radioEndpoint?.let { endpoint ->

                    MultiFloatingActionsContainer(
                        iconId = R.drawable.radio,
                        onClick = {
                            binder?.stopRadio()
                            binder?.playRadio(endpoint)
                        },
                        onClickSettings = onSettingsClick,
                        onClickSearch = onSearchClick
                    )
                    /*

                   FloatingActionsContainerWithScrollToTop(
                       scrollState = scrollState,
                       iconId = R.drawable.radio,
                       onClick = {
                           binder?.stopRadio()
                           binder?.playRadio(endpoint)
                       }
                   )
                    */
                }


        }
    }
}
