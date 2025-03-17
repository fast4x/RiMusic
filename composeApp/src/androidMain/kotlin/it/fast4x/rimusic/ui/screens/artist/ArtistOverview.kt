package it.fast4x.rimusic.ui.screens.artist

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
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
import it.fast4x.environment.EnvironmentExt
import it.fast4x.environment.models.BrowseEndpoint
import it.fast4x.environment.requests.ArtistPage
import it.fast4x.environment.requests.ArtistSection
import it.fast4x.environment.utils.completed
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.LocalPlayerAwareWindowInsets
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.cleanPrefix
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.enums.PopupType
import it.fast4x.rimusic.enums.ThumbnailRoundness
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.models.Album
import it.fast4x.rimusic.models.Artist
import it.fast4x.rimusic.models.Playlist
import it.fast4x.rimusic.thumbnailShape
import it.fast4x.rimusic.typography
import it.fast4x.rimusic.ui.components.CustomModalBottomSheet
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.SwipeablePlaylistItem
import it.fast4x.rimusic.ui.components.themed.AutoResizeText
import it.fast4x.rimusic.ui.components.themed.FontSizeRange
import it.fast4x.rimusic.ui.components.themed.HeaderIconButton
import it.fast4x.rimusic.ui.components.themed.MultiFloatingActionsContainer
import it.fast4x.rimusic.ui.components.themed.NonQueuedMediaItemMenu
import it.fast4x.rimusic.ui.components.themed.SecondaryTextButton
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.ui.components.themed.Title
import it.fast4x.rimusic.ui.components.themed.Title2Actions
import it.fast4x.rimusic.ui.items.AlbumItem
import it.fast4x.rimusic.ui.items.ArtistItem
import it.fast4x.rimusic.ui.items.PlaylistItem
import it.fast4x.rimusic.ui.items.SongItem
import it.fast4x.rimusic.ui.items.VideoItem
import it.fast4x.rimusic.ui.screens.settings.isYouTubeSyncEnabled
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
import it.fast4x.rimusic.utils.isDownloadedSong
import it.fast4x.rimusic.utils.isLandscape
import org.dailyislam.android.utilities.isNetworkConnected
import it.fast4x.rimusic.utils.manageDownload
import it.fast4x.rimusic.utils.parentalControlEnabledKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.resize
import it.fast4x.rimusic.utils.secondary
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.showFloatingIconKey
import it.fast4x.rimusic.utils.thumbnailRoundnessKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    artistPage: ArtistPage?,
    onItemsPageClick: (ArtistSection) -> Unit,
    disableScrollingText: Boolean
) {

    if (browseId == null || artistPage == null) return

    val binder = LocalPlayerServiceBinder.current
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

    val thumbnailRoundness by rememberPreference(thumbnailRoundnessKey, ThumbnailRoundness.Heavy)

    val sectionTextModifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 24.dp, bottom = 8.dp)

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

    val listMediaItems = remember { mutableListOf<MediaItem>() }

    var artist by persist<Artist?>("artist/$browseId/artist")

    var itemsBrowseId by remember { mutableStateOf("") }
    var itemsParams by remember { mutableStateOf("") }
    var itemsSectionName by remember { mutableStateOf("") }
    var showArtistItems by rememberSaveable { mutableStateOf(false) }
    var songsBrowseId by remember { mutableStateOf("") }
    var songsParams by remember { mutableStateOf("") }

    val hapticFeedback = LocalHapticFeedback.current
    val parentalControlEnabled by rememberPreference(parentalControlEnabledKey, false)
    val menuState = LocalMenuState.current

    var readMore by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Database.artist(browseId).collect { artist = it }
    }


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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {

            item {
                val modifierArt = Modifier.fillMaxWidth()

                Box(
                    modifier = modifierArt
                ) {
                    //if (artistPage != null) {
                    if (!isLandscape)
                        Box {
                            AsyncImage(
                                model = artistPage.artist.thumbnail?.url?.resize(
                                    1200,
                                    1200
                                ),
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
                            if (artist?.isYoutubeArtist == true) {
                                Image(
                                    painter = painterResource(R.drawable.ytmusic),
                                    colorFilter = ColorFilter.tint(
                                        Color.Red.copy(0.75f).compositeOver(Color.White)
                                    ),
                                    modifier = Modifier
                                        .size(40.dp)
                                        .padding(all = 5.dp)
                                        .offset(10.dp,10.dp),
                                    contentDescription = "Background Image",
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }

                    AutoResizeText(
                        text = artistPage.artist.info?.name ?: "",
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
                            .conditional(!disableScrollingText) {
                                basicMarquee(
                                    iterations = Int.MAX_VALUE
                                )
                            }

                    )


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

                            context.startActivity(
                                Intent.createChooser(
                                    sendIntent,
                                    null
                                )
                            )
                        }
                    )

                }

                artistPage.subscribers?.let {
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
                            if (isYouTubeSyncEnabled() && !isNetworkConnected(context)){
                                SmartMessage(context.resources.getString(R.string.no_connection), context = context, type = PopupType.Error)
                            } else {
                                val bookmarkedAt =
                                    if (artist?.bookmarkedAt == null) System.currentTimeMillis() else null

                                Database.asyncTransaction {
                                    artist?.copy(bookmarkedAt = bookmarkedAt)
                                        ?.let(::update)
                                }
                                if (isYouTubeSyncEnabled())
                                    CoroutineScope(Dispatchers.IO).launch {
                                        if (bookmarkedAt == null)
                                            artistPage.artist.channelId.let {
                                                if (it != null) {
                                                    EnvironmentExt.unsubscribeChannel(it)
                                                    if (artist != null) {
                                                        Database.update(artist!!.copy(isYoutubeArtist = false))
                                                    }
                                                }
                                            }
                                        else
                                            artistPage.artist.channelId.let {
                                                if (it != null) {
                                                    EnvironmentExt.subscribeChannel(it)
                                                    if (artist != null) {
                                                        Database.update(artist!!.copy(isYoutubeArtist = true))
                                                    }
                                                }
                                            }
                                    }
                            }

                        },
                        alternative = artist?.bookmarkedAt == null,
                        modifier = Modifier.padding(end = 30.dp)
                    )

//                    HeaderIconButton(
//                        icon = R.drawable.downloaded,
//                        color = colorPalette().text,
//                        onClick = {},
//                        modifier = Modifier
//                            .padding(horizontal = 5.dp)
//                            .combinedClickable(
//                                onClick = {
//                                    showConfirmDownloadAllDialog = true
//                                },
//                                onLongClick = {
//                                    SmartMessage(context.resources.getString(R.string.info_download_all_songs), context = context)
//                                }
//                            )
//                    )

//                    if (showConfirmDownloadAllDialog) {
//                        ConfirmationDialog(
//                            text = stringResource(R.string.do_you_really_want_to_download_all),
//                            onDismiss = { showConfirmDownloadAllDialog = false },
//                            onConfirm = {
//                                showConfirmDownloadAllDialog = false
//                                downloadState = Download.STATE_DOWNLOADING
//                                if (youtubeArtistPage?.songs?.isNotEmpty() == true)
//                                    youtubeArtistPage.songs?.forEach {
//                                        binder?.cache?.removeResource(it.asMediaItem.mediaId)
//                                        CoroutineScope(Dispatchers.IO).launch {
//                                            Database.deleteFormat( it.asMediaItem.mediaId )
//                                        }
//                                        manageDownload(
//                                            context = context,
//                                            mediaItem = it.asMediaItem,
//                                            downloadState = false
//                                        )
//                                    }
//                            }
//                        )
//                    }

//                    HeaderIconButton(
//                        icon = R.drawable.download,
//                        color = colorPalette().text,
//                        onClick = {},
//                        modifier = Modifier
//                            .padding(horizontal = 5.dp)
//                            .combinedClickable(
//                                onClick = {
//                                    showConfirmDeleteDownloadDialog = true
//                                },
//                                onLongClick = {
//                                    SmartMessage(context.resources.getString(R.string.info_remove_all_downloaded_songs), context = context)
//                                }
//                            )
//                    )
//
//                    if (showConfirmDeleteDownloadDialog) {
//                        ConfirmationDialog(
//                            text = stringResource(R.string.do_you_really_want_to_delete_download),
//                            onDismiss = { showConfirmDeleteDownloadDialog = false },
//                            onConfirm = {
//                                showConfirmDeleteDownloadDialog = false
//                                downloadState = Download.STATE_DOWNLOADING
//                                if (youtubeArtistPage?.songs?.isNotEmpty() == true)
//                                    youtubeArtistPage.songs?.forEach {
//                                        binder?.cache?.removeResource(it.asMediaItem.mediaId)
//                                        CoroutineScope(Dispatchers.IO).launch {
//                                            Database.deleteFormat( it.asMediaItem.mediaId )
//                                        }
//                                        manageDownload(
//                                            context = context,
//                                            mediaItem = it.asMediaItem,
//                                            downloadState = true
//                                        )
//                                    }
//                            }
//                        )
//                    }

                    artistPage.shuffleEndpoint?.let { endpoint ->
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
                                        SmartMessage(
                                            context.resources.getString(R.string.info_shuffle),
                                            context = context
                                        )
                                    }
                                )
                        )
                    }

                    artistPage.radioEndpoint?.let { endpoint ->
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
                                        SmartMessage(
                                            context.resources.getString(R.string.info_start_radio),
                                            context = context
                                        )
                                    }
                                )
                        )
                    }

                }
            }

            item {
                artistPage.description?.let { description ->
                    val attributionsIndex = description.lastIndexOf("\n\nFrom Wikipedia")

                    Title(
                        title = stringResource(R.string.information),
                        icon = if (readMore) R.drawable.chevron_up else R.drawable.chevron_down,
                        onClick = {
                            readMore = !readMore
                        }
                    )

                    Row(
                        modifier = Modifier
                            .padding(vertical = 16.dp, horizontal = 8.dp)
                    ) {
                        BasicText(
                            text = "“",
                            style = typography().xxl.semiBold,
                            modifier = Modifier
                                .offset(y = (-8).dp)
                                .align(Alignment.Top)
                        )

                        if (!readMore)
                            BasicText(
                                text = description.substring(0,
                                    if (description.length >= 100) 100 else description.length
                                ).plus("..."),
                                style = typography().xxs.secondary.align(TextAlign.Justify),
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .weight(1f)
                                    .clickable {
                                        readMore = !readMore
                                    }
                            )

                        if (readMore)
                            BasicText(
                                text = if (attributionsIndex == -1) {
                                    description
                                } else {
                                    description.substring(0, attributionsIndex)
                                },
                                style = typography().xxs.secondary.align(TextAlign.Justify),
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .weight(1f)
                                    .clickable {
                                        readMore = !readMore
                                    }
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
                            style = typography().xxs.color(colorPalette().textDisabled)
                                .align(TextAlign.Start),
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp)
                        )
                    }

                }
            }

            artistPage.sections.forEach() { it ->
                //println("ArtistOverviewModern title: ${it.title} browseId: ${it.moreEndpoint?.browseId} params: ${it.moreEndpoint?.params}")
                item {
                    if (it.items.firstOrNull() is Environment.SongItem) {
                        Title(
                            title = it.title,
                            enableClick = it.moreEndpoint?.browseId != null,
                            onClick = {
                                //println("ArtistOverviewModern onClick: browseId: ${it.moreEndpoint?.browseId} params: ${it.moreEndpoint?.params}")
                                if (it.moreEndpoint?.browseId != null) {
                                    itemsBrowseId = it.moreEndpoint!!.browseId!!
                                    itemsParams = it.moreEndpoint!!.params.toString()
                                    itemsSectionName = it.title
                                    showArtistItems = true
                                }

                            },
                        )
                    } else {
                        Title2Actions(
                            title = it.title,
                            enableClick = it.moreEndpoint?.browseId != null,
                            onClick1 = {
                                //println("ArtistOverviewModern onClick: browseId: ${it.moreEndpoint?.browseId} params: ${it.moreEndpoint?.params}")
                                if (it.moreEndpoint?.browseId != null) {
                                    itemsBrowseId = it.moreEndpoint!!.browseId!!
                                    itemsParams = it.moreEndpoint!!.params.toString()
                                    itemsSectionName = it.title
                                    showArtistItems = true
                                }

                            },
                            icon2 = R.drawable.dice,
                            onClick2 = {
                                if (it.items.isEmpty()) return@Title2Actions
                                val idItem = it.items.get(
                                    if (it.items.size > 1)
                                        Random(System.currentTimeMillis()).nextInt(0, it.items.size-1)
                                    else 0
                                ).key
                                navController.navigate(route = "${NavRoutes.album.name}/${idItem}")
                            }
                        )
                    }
                }
                if (it.items.firstOrNull() is Environment.SongItem) {
                    items(it.items) { item ->
                        when (item) {
                            is Environment.SongItem -> {
                                if (parentalControlEnabled && item.explicit) return@items

                                downloadState = getDownloadState(item.asMediaItem.mediaId)
                                val isDownloaded = isDownloadedSong(item.asMediaItem.mediaId)
                                println("Innertube artistmodern SongItem: ${item.info?.name}")
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
                                    var forceRecompose by remember { mutableStateOf(false) }
                                    SongItem(
                                        song = item,
                                        thumbnailSizePx = songThumbnailSizePx,
                                        thumbnailSizeDp = songThumbnailSizeDp,
                                        onDownloadClick = {},
                                        downloadState = Download.STATE_STOPPED,
                                        disableScrollingText = disableScrollingText,
                                        isNowPlaying = false,
                                        forceRecompose = forceRecompose,
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
                                                                navController.navigate("${NavRoutes.videoOrSongInfo.name}/${item.key}")
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
                                                    binder?.stopRadio()
                                                    CoroutineScope(Dispatchers.IO).launch {
                                                        artistPage.sections.firstOrNull{sec -> sec.items.firstOrNull() is Environment.SongItem}.let {
                                                            songsBrowseId = it?.moreEndpoint?.browseId.toString()
                                                            songsParams = it?.moreEndpoint?.params.toString()
                                                        }
                                                        if (songsBrowseId.isNotEmpty())
                                                            BrowseEndpoint(
                                                                browseId = songsBrowseId,
                                                                params = songsParams
                                                            ).let { endpoint ->
                                                                val artistSongs = EnvironmentExt.getArtistItemsPage(endpoint)
                                                                    .completed()
                                                                    .getOrNull()
                                                                    ?.items
                                                                    ?.map{ it as Environment.SongItem }
                                                                    ?.map { it.asMediaItem }

                                                                    val filteredArtistSongs = artistSongs
                                                                    ?.filter { it.mediaId != Database.songDisliked(it.mediaId) }

                                                                    //if (artistSongs?.contains(item.asMediaItem) == false){
                                                                        withContext(Dispatchers.Main) {
                                                                            binder?.player?.forcePlay(item.asMediaItem)
                                                                            if (filteredArtistSongs != null) {
                                                                                binder?.player?.addMediaItems(filteredArtistSongs.filterNot { it.mediaId == item.key })
                                                                            }
                                                                        }
    //                                                                } else {
    //                                                                    SmartMessage(context.resources.getString(R.string.disliked_this_song),type = PopupType.Error, context = context)
    //                                                                }

                                                            }
                                                    }
                                                }
                                            )
                                    )
                                }
                            }

                            else -> {}
                        }
                    }
                } else {
                    item {
                        LazyRow(contentPadding = endPaddingValues) {
                            items(it.items) { item ->
                                when (item) {
                                    is Environment.SongItem -> {}

                                    is Environment.AlbumItem -> {
                                        println("Innertube artistmodern AlbumItem: ${item.info?.name}")
                                        var albumById by remember { mutableStateOf<Album?>(null) }
                                        LaunchedEffect(item) {
                                            CoroutineScope(Dispatchers.IO).launch {
                                                albumById = Database.album(item.key).firstOrNull()
                                            }
                                        }
                                        AlbumItem(
                                            album = item,
                                            alternative = true,
                                            thumbnailSizePx = albumThumbnailSizePx,
                                            thumbnailSizeDp = albumThumbnailSizeDp,
                                            disableScrollingText = disableScrollingText,
                                            isYoutubeAlbum = albumById?.isYoutubeAlbum == true,
                                            modifier = Modifier.clickable(onClick = {
                                                navController.navigate("${NavRoutes.album.name}/${item.key}")
                                            })

                                        )
                                    }

                                    is Environment.ArtistItem -> {
                                        println("Innertube v ArtistItem: ${item.info?.name}")
                                        var artistById by remember { mutableStateOf<Artist?>(null) }
                                        LaunchedEffect(item) {
                                            CoroutineScope(Dispatchers.IO).launch {
                                                artistById = Database.artist(item.key).firstOrNull()
                                            }
                                        }
                                        ArtistItem(
                                            artist = item,
                                            thumbnailSizePx = artistThumbnailSizePx,
                                            thumbnailSizeDp = artistThumbnailSizeDp,
                                            disableScrollingText = disableScrollingText,
                                            isYoutubeArtist = artistById?.isYoutubeArtist == true,
                                            modifier = Modifier.clickable(onClick = {
                                                navController.navigate("${NavRoutes.artist.name}/${item.key}")
                                            })
                                        )
                                    }

                                    is Environment.PlaylistItem -> {
                                        println("Innertube v PlaylistItem: ${item.info?.name}")
                                        var playlistById by remember { mutableStateOf<Playlist?>(null) }
                                        LaunchedEffect(item) {
                                            CoroutineScope(Dispatchers.IO).launch {
                                                playlistById = Database.playlist(item.key.substringAfter("VL")).firstOrNull()
                                            }
                                        }
                                        PlaylistItem(
                                            playlist = item,
                                            alternative = true,
                                            thumbnailSizePx = playlistThumbnailSizePx,
                                            thumbnailSizeDp = playlistThumbnailSizeDp,
                                            disableScrollingText = disableScrollingText,
                                            isYoutubePlaylist = playlistById?.isYoutubePlaylist == true,
                                            modifier = Modifier.clickable(onClick = {
                                                navController.navigate("${NavRoutes.playlist.name}/${item.key}")
                                            })
                                        )
                                    }

                                    is Environment.VideoItem -> {
                                        println("Innertube v VideoItem: ${item.info?.name}")
                                        VideoItem(
                                            video = item,
                                            thumbnailHeightDp = playlistThumbnailSizeDp,
                                            thumbnailWidthDp = playlistThumbnailSizeDp,
                                            disableScrollingText = disableScrollingText
                                        )
                                    }
                                }

                            }
                        }
                    }
                }
            }

            item(key = "bottom") {
                Spacer(modifier = Modifier.height(Dimensions.bottomSpacer))
            }

        }

        val showFloatingIcon by rememberPreference(showFloatingIconKey, false)
        if (UiType.ViMusic.isCurrent() && showFloatingIcon)
            artistPage?.radioEndpoint?.let { endpoint ->

                MultiFloatingActionsContainer(
                    iconId = R.drawable.radio,
                    onClick = {
                        binder?.stopRadio()
                        binder?.playRadio(endpoint)
                    },
                    onClickSettings = { navController.navigate(NavRoutes.search.name) },
                    onClickSearch = { navController.navigate(NavRoutes.settings.name) }
                )

            }

        println("ArtistOverviewModern showArtistItems: $showArtistItems itemsBrowseId: $itemsBrowseId itemsParams: $itemsParams")
        CustomModalBottomSheet(
            showSheet = showArtistItems,
            onDismissRequest = { showArtistItems = false },
            containerColor = colorPalette().background2,
            contentColor = colorPalette().background2,
            modifier = Modifier
                .fillMaxWidth(),
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = {
                Surface(
                    modifier = Modifier.padding(vertical = 0.dp),
                    color = colorPalette().background0,
                    shape = thumbnailShape()
                ) {}
            },
            shape = thumbnailRoundness.shape()
        ) {
            ArtistOverviewItems(
                navController,
                artistName = cleanPrefix(artist?.name ?: ""),
                sectionName = itemsSectionName,
                browseId = itemsBrowseId,
                params = itemsParams,
                disableScrollingText = false,
                onDismiss = { showArtistItems = false }
            )
        }


    }

}
