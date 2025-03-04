package it.fast4x.rimusic.ui.screens.playlist

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
import coil.compose.AsyncImage
import it.fast4x.compose.persist.persist
import it.fast4x.compose.persist.persistList
import it.fast4x.environment.Environment
import it.fast4x.environment.EnvironmentExt
import it.fast4x.environment.models.NavigationEndpoint
import it.fast4x.environment.requests.PlaylistPage
import it.fast4x.environment.utils.completed
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.Database.Companion.like
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.appContext
import it.fast4x.rimusic.cleanPrefix
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.enums.PopupType
import it.fast4x.rimusic.enums.ThumbnailRoundness
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.models.Playlist
import it.fast4x.rimusic.models.SongPlaylistMap
import it.fast4x.rimusic.service.isLocal
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.ShimmerHost
import it.fast4x.rimusic.ui.components.SwipeablePlaylistItem
import it.fast4x.rimusic.ui.components.themed.AutoResizeText
import it.fast4x.rimusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import it.fast4x.rimusic.ui.components.themed.FontSizeRange
import it.fast4x.rimusic.ui.components.themed.HeaderIconButton
import it.fast4x.rimusic.ui.components.themed.IconButton
import it.fast4x.rimusic.ui.components.themed.InputTextDialog
import it.fast4x.rimusic.ui.components.themed.LayoutWithAdaptiveThumbnail
import it.fast4x.rimusic.ui.components.themed.NonQueuedMediaItemMenu
import it.fast4x.rimusic.ui.components.themed.PlaylistsItemMenu
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.ui.components.themed.adaptiveThumbnailContent
import it.fast4x.rimusic.ui.items.AlbumItemPlaceholder
import it.fast4x.rimusic.ui.items.SongItem
import it.fast4x.rimusic.ui.items.SongItemPlaceholder
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.favoritesIcon
import it.fast4x.rimusic.ui.styling.px
import it.fast4x.rimusic.utils.addNext
import it.fast4x.rimusic.utils.asMediaItem
import it.fast4x.rimusic.utils.asSong
import it.fast4x.rimusic.utils.disableScrollingTextKey
import it.fast4x.rimusic.utils.durationTextToMillis
import it.fast4x.rimusic.utils.enqueue
import it.fast4x.rimusic.utils.fadingEdge
import it.fast4x.rimusic.utils.forcePlayAtIndex
import it.fast4x.rimusic.utils.forcePlayFromBeginning
import it.fast4x.rimusic.utils.formatAsTime
import it.fast4x.rimusic.utils.getDownloadState
import it.fast4x.rimusic.utils.isDownloadedSong
import it.fast4x.rimusic.utils.isLandscape
import it.fast4x.rimusic.utils.isNowPlaying
import it.fast4x.rimusic.utils.manageDownload
import it.fast4x.rimusic.utils.medium
import it.fast4x.rimusic.utils.parentalControlEnabledKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.resize
import it.fast4x.rimusic.utils.secondary
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.showFloatingIconKey
import it.fast4x.rimusic.utils.thumbnailRoundnessKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.typography
import it.fast4x.rimusic.ui.components.themed.ConfirmationDialog
import it.fast4x.rimusic.ui.screens.settings.isYouTubeSyncEnabled
import it.fast4x.rimusic.utils.addToYtLikedSongs
import it.fast4x.rimusic.utils.align
import it.fast4x.rimusic.utils.color
import it.fast4x.rimusic.utils.formatAsDuration
import it.fast4x.rimusic.utils.getHttpClient
import it.fast4x.rimusic.utils.isNetworkConnected
import it.fast4x.rimusic.utils.languageDestination
import it.fast4x.rimusic.utils.setLikeState
import kotlinx.coroutines.flow.filterNotNull
import me.bush.translator.Language
import me.bush.translator.Translator
import timber.log.Timber


@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun PlaylistSongList(
    navController: NavController,
    browseId: String,
) {
    val binder = LocalPlayerServiceBinder.current
    val context = LocalContext.current
    val menuState = LocalMenuState.current

    var playlistPage by persist<PlaylistPage?>("playlist/$browseId/playlistPage")
    var playlistSongs by persistList<Environment.SongItem>("playlist/$browseId/songs")

    var filter: String? by rememberSaveable { mutableStateOf(null) }
    val hapticFeedback = LocalHapticFeedback.current
    val parentalControlEnabled by rememberPreference(parentalControlEnabledKey, false)
    val disableScrollingText by rememberPreference(disableScrollingTextKey, false)
    var isLiked by remember {
        mutableStateOf(0)
    }

    var saveCheck by remember { mutableStateOf(false) }

    val sectionTextModifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 24.dp, bottom = 8.dp)

    var translateEnabled by remember {
        mutableStateOf(false)
    }

    val translator = Translator(getHttpClient())
    val languageDestination = languageDestination()

    var localPlaylist by remember { mutableStateOf<Playlist?>(null) }

    LaunchedEffect(saveCheck) {
        Database.asyncTransaction {
            localPlaylist = Database.playlistWithBrowseId(browseId.substringAfter("VL"))
        }
    }

    @Composable
    fun checkLike(mediaId : String, song: Environment. SongItem) : Boolean {
        LaunchedEffect(Unit, mediaId) {
            withContext(Dispatchers.IO) {
                isLiked = like( mediaId, setLikeState(song.asSong.likedAt))
            }
        }
        return true
    }

    LaunchedEffect(Unit, browseId) {
        EnvironmentExt.getPlaylist(browseId).completed()
            .onSuccess {
                playlistPage = it
                playlistSongs = it.songs
                playlistSongs = if (parentalControlEnabled) it.songs.filter { !it.explicit } else
                    playlistPage?.songs ?: emptyList()
            }.onFailure {
                println("PlaylistSongList error: ${it.stackTraceToString()}")
            }

        println("PlaylistSongList browseId: ${browseId}")
        println("PlaylistSongList playlistSongs: ${playlistSongs.size}")
    }

    var filterCharSequence: CharSequence
    filterCharSequence = filter.toString()
    //Log.d("mediaItemFilter", "<${filter}>  <${filterCharSequence}>")
    if (!filter.isNullOrBlank()) {
        playlistPage?.songs =
            playlistPage?.songs?.filter { songItem ->
                songItem.asMediaItem.mediaMetadata.title?.contains(
                    filterCharSequence,
                    true
                ) ?: false
                        || songItem.asMediaItem.mediaMetadata.artist?.contains(
                    filterCharSequence,
                    true
                ) ?: false
                        || songItem.asMediaItem.mediaMetadata.albumTitle?.contains(
                    filterCharSequence,
                    true
                ) ?: false
            }!!
    } else playlistPage?.songs = playlistSongs

    var playlistNotLikedSongs by persistList<Environment.SongItem>("")

    var searching by rememberSaveable { mutableStateOf(false) }

    val songThumbnailSizeDp = Dimensions.thumbnails.song
    val songThumbnailSizePx = songThumbnailSizeDp.px

    var isImportingPlaylist by rememberSaveable {
        mutableStateOf(false)
    }

    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }

    var thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )

    var showYoutubeLikeConfirmDialog by remember {
        mutableStateOf(false)
    }
    var totalMinutesToLike by remember { mutableStateOf("") }

    if (showYoutubeLikeConfirmDialog) {
        Database.asyncTransaction {
            playlistNotLikedSongs = playlistSongs.filter { getLikedAt(it.asMediaItem.mediaId) in listOf(-1L,null)}
        }
        totalMinutesToLike = formatAsDuration(playlistNotLikedSongs.size.toLong()*1000)
        ConfirmationDialog(
            text = "$totalMinutesToLike "+stringResource(R.string.do_you_really_want_to_like_all),
            onDismiss = { showYoutubeLikeConfirmDialog = false },
            onConfirm = {
                showYoutubeLikeConfirmDialog = false
                CoroutineScope(Dispatchers.IO).launch {
                    addToYtLikedSongs(playlistNotLikedSongs.map {it.asMediaItem})
                }
            }
        )
    }

    var totalPlayTimes = 0L
    playlistPage?.songs?.forEach {
        totalPlayTimes += it.durationText?.let { it1 ->
            durationTextToMillis(it1) }?.toLong() ?: 0
    }

    var dislikedSongs by persistList<String>("")

    LaunchedEffect(Unit) {
        Database.dislikedSongsById().filterNotNull()
            .collect { dislikedSongs = it }
    }

    if (isImportingPlaylist) {
        InputTextDialog(
            onDismiss = { isImportingPlaylist = false },
            title = stringResource(R.string.enter_the_playlist_name),
            value = playlistPage?.playlist?.title ?: "",
            placeholder = "https://........",
            setValue = { text ->
                Database.asyncTransaction {
                    val playlistId = insert(Playlist(name = text, browseId = browseId))

                    playlistPage?.songs
                                ?.map(Environment.SongItem::asMediaItem)
                                ?.onEach( ::insert )
                                ?.mapIndexed { index, mediaItem ->
                                    SongPlaylistMap(
                                        songId = mediaItem.mediaId,
                                        playlistId = playlistId,
                                        position = index
                                    ).default()
                                }
                                ?.let( ::insertSongPlaylistMaps )
                }
                SmartMessage(context.resources.getString(R.string.done), PopupType.Success, context = context)
            }
        )
    }

    var position by remember {
        mutableIntStateOf(0)
    }

    val thumbnailContent = adaptiveThumbnailContent(playlistPage == null, playlistPage?.playlist?.thumbnail?.url)

    val lazyListState = rememberLazyListState()

    val coroutineScope = rememberCoroutineScope()

    LayoutWithAdaptiveThumbnail(thumbnailContent = thumbnailContent) {
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
                state = lazyListState,
                //contentPadding = LocalPlayerAwareWindowInsets.current
                //.only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
                modifier = Modifier
                    .background(colorPalette().background0)
                    .fillMaxSize()
            ) {

                item(
                    key = "header"
                ) {

                    val modifierArt = Modifier.fillMaxWidth()

                    Box(
                        modifier = modifierArt
                    ) {
                        if (playlistPage != null) {
                            if(!isLandscape)
                                Box {
                                    AsyncImage(
                                        model = playlistPage!!.playlist.thumbnail?.url?.resize(
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
                                }
                                if (localPlaylist?.isYoutubePlaylist == true) {
                                   Image(
                                        painter = painterResource(R.drawable.ytmusic),
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(
                                        Color.Red.copy(0.75f).compositeOver(Color.White)
                                        ),
                                        modifier = Modifier
                                           .size(40.dp)
                                           .offset(5.dp,5.dp)
                                   )
                                }

                            AutoResizeText(
                                text = playlistPage?.playlist?.title ?: "",
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
                                    .padding(bottom = 20.dp)
                            )

                            BasicText(
                                text = playlistPage!!.songs?.size.toString() + " "
                                        + stringResource(R.string.songs)
                                        + " - " + formatAsTime(totalPlayTimes),
                                style = typography().xs.medium,
                                maxLines = 1,
                                modifier = Modifier
                                    //.padding(top = 10.dp)
                                    .align(Alignment.BottomCenter)
                            )


                            HeaderIconButton(
                                icon = R.drawable.share_social,
                                color = colorPalette().text,
                                iconSize = 24.dp,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 5.dp, end = 5.dp),
                                onClick = {
                                    (playlistPage?.playlist?.thumbnail?.url ?: "https://music.youtube.com/playlist?list=${browseId.removePrefix("VL")}").let { url ->
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, url)
                                        }

                                        context.startActivity(Intent.createChooser(sendIntent, null))
                                    }
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

                }

                item(
                    key = "actions",
                    contentType = 0
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .fillMaxWidth()
                    ) {

                        //if (!isLandscape) thumbnailContent()

                        if (playlistPage != null) {

                            //actionsContent()

                            HeaderIconButton(
                                onClick = { searching = !searching },
                                icon = R.drawable.search_circle,
                                color = colorPalette().text,
                                iconSize = 24.dp,
                                modifier = Modifier
                                    .padding(horizontal = 5.dp)
                            )

                            HeaderIconButton(
                                icon = R.drawable.downloaded,
                                color = colorPalette().text,
                                onClick = {},
                                modifier = Modifier
                                    .padding(horizontal = 5.dp)
                                    .combinedClickable(
                                        onClick = {
                                            if (playlistPage?.songs?.any { it.asMediaItem.mediaId !in dislikedSongs } == true) {
                                                downloadState = Download.STATE_DOWNLOADING
                                                if (playlistPage?.songs?.any { it.asMediaItem.mediaId !in dislikedSongs } == true)
                                                    playlistPage?.songs?.filter { it.asMediaItem.mediaId !in dislikedSongs }
                                                        ?.forEach {
                                                            binder?.cache?.removeResource(it.asMediaItem.mediaId)
                                                            CoroutineScope(Dispatchers.IO).launch {
                                                                Database.deleteFormat(it.asMediaItem.mediaId)
                                                            }
                                                            manageDownload(
                                                                context = context,
                                                                mediaItem = it.asMediaItem,
                                                                downloadState = false
                                                            )
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
                                            SmartMessage(
                                                context.resources.getString(R.string.info_download_all_songs),
                                                context = context
                                            )
                                        }
                                    )
                            )

                            HeaderIconButton(
                                icon = R.drawable.download,
                                color = colorPalette().text,
                                onClick = {},
                                modifier = Modifier
                                    .padding(horizontal = 5.dp)
                                    .combinedClickable(
                                        onClick = {
                                            if (playlistPage?.songs?.any { it.asMediaItem.mediaId !in dislikedSongs } == true) {
                                                downloadState = Download.STATE_DOWNLOADING
                                                if (playlistPage?.songs?.isNotEmpty() == true)
                                                    playlistPage?.songs?.forEach {
                                                        binder?.cache?.removeResource(it.asMediaItem.mediaId)
                                                        CoroutineScope(Dispatchers.IO).launch {
                                                            Database.deleteFormat(it.asMediaItem.mediaId)
                                                        }
                                                        manageDownload(
                                                            context = context,
                                                            mediaItem = it.asMediaItem,
                                                            downloadState = true
                                                        )
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
                                            SmartMessage(
                                                context.resources.getString(R.string.info_remove_all_downloaded_songs),
                                                context = context
                                            )
                                        }
                                    )
                            )



                            HeaderIconButton(
                                icon = R.drawable.enqueue,
                                enabled = playlistPage?.songs?.any { it.asMediaItem.mediaId !in dislikedSongs } == true,
                                color =  if (playlistPage?.songs?.any { it.asMediaItem.mediaId !in dislikedSongs } == true) colorPalette().text else colorPalette().textDisabled,
                                onClick = {},
                                modifier = Modifier
                                    .padding(horizontal = 5.dp)
                                    .combinedClickable(
                                        onClick = {
                                            if (playlistPage?.songs?.any { it.asMediaItem.mediaId !in dislikedSongs } == true) {
                                                playlistPage?.songs?.filter { it.asMediaItem.mediaId !in dislikedSongs }
                                                    ?.map(Environment.SongItem::asMediaItem)
                                                    ?.let { mediaItems ->
                                                        binder?.player?.enqueue(mediaItems, context)
                                                    }
                                            } else {
                                                SmartMessage(
                                                    context.resources.getString(R.string.disliked_this_collection),
                                                    type = PopupType.Error,
                                                    context = context
                                                )
                                            }
                                        },
                                        onLongClick = {
                                            SmartMessage(
                                                context.resources.getString(R.string.info_enqueue_songs),
                                                context = context
                                            )
                                        }
                                    )
                            )

                            HeaderIconButton(
                                icon = R.drawable.shuffle,
                                enabled = playlistPage?.songs?.any { it.asMediaItem.mediaId !in dislikedSongs } == true,
                                color = if (playlistPage?.songs?.any { it.asMediaItem.mediaId !in dislikedSongs } == true) colorPalette().text else colorPalette().textDisabled,
                                onClick = {},
                                modifier = Modifier
                                    .padding(horizontal = 5.dp)
                                    .combinedClickable(
                                        onClick = {
                                            if (playlistPage?.songs?.any { it.asMediaItem.mediaId !in dislikedSongs } == true) {
                                                binder?.stopRadio()
                                                playlistPage?.songs?.filter { it.asMediaItem.mediaId !in dislikedSongs }
                                                    ?.shuffled()
                                                    ?.map(Environment.SongItem::asMediaItem)
                                                    ?.let {
                                                        binder?.player?.forcePlayFromBeginning(
                                                            it
                                                        )
                                                    }
                                            } else {
                                                SmartMessage(
                                                    context.resources.getString(R.string.disliked_this_collection),
                                                    type = PopupType.Error,
                                                    context = context
                                                )
                                            }
                                        },
                                        onLongClick = {
                                            SmartMessage(
                                                context.resources.getString(R.string.info_shuffle),
                                                context = context
                                            )
                                        }
                                    )
                            )

                            HeaderIconButton(
                                icon = R.drawable.radio,
                                enabled = playlistPage?.songs?.any { it.asMediaItem.mediaId !in dislikedSongs } == true,
                                color = if (playlistPage?.songs?.any { it.asMediaItem.mediaId !in dislikedSongs } == true) colorPalette().text else colorPalette().textDisabled,
                                onClick = {},
                                modifier = Modifier
                                    .padding(horizontal = 5.dp)
                                    .combinedClickable(
                                        onClick = {
                                            if (binder != null) {
                                                if (playlistPage?.songs?.any { it.asMediaItem.mediaId !in dislikedSongs } == true) {
                                                    binder.stopRadio()
                                                    binder.playRadio(
                                                        NavigationEndpoint.Endpoint.Watch(videoId =
                                                        if (binder.player.currentMediaItem?.mediaId != null)
                                                            binder.player.currentMediaItem?.mediaId
                                                        else playlistPage?.songs?.first { it.asMediaItem.mediaId !in dislikedSongs }?.asMediaItem?.mediaId
                                                        )
                                                    )
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
                                            SmartMessage(
                                                context.resources.getString(R.string.info_start_radio),
                                                context = context
                                            )
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
                                                PlaylistsItemMenu(
                                                    navController = navController,
                                                    modifier = Modifier.fillMaxHeight(0.4f),
                                                    onDismiss = menuState::hide,
                                                    onImportOnlinePlaylist = {
                                                        isImportingPlaylist = true
                                                    },

                                                    onAddToPlaylist = { playlistPreview ->
                                                        position =
                                                            playlistPreview.songCount.minus(1) ?: 0
                                                        if (position > 0) position++ else position =
                                                            0

                                                        val playlistSize = playlistPage?.songs?.size ?: 0

                                                        if ((playlistSize + playlistPreview.songCount) > 5000 && playlistPreview.playlist.isYoutubePlaylist && isYouTubeSyncEnabled()){
                                                            SmartMessage(context.resources.getString(R.string.yt_playlist_limited), context = context, type = PopupType.Error)
                                                        } else if (!isYouTubeSyncEnabled() || !playlistPreview.playlist.isYoutubePlaylist) {
                                                            playlistPage!!.songs.forEachIndexed { index, song ->
                                                                runCatching {
                                                                    coroutineScope.launch(Dispatchers.IO) {
                                                                        Database.insert(song.asSong)
                                                                        Database.insert(
                                                                            SongPlaylistMap(
                                                                                songId = song.asMediaItem.mediaId,
                                                                                playlistId = playlistPreview.playlist.id,
                                                                                position = position + index
                                                                            ).default()
                                                                        )
                                                                    }
                                                                }.onFailure {
                                                                    Timber.e("Failed onAddToPlaylist in PlaylistSongListModern  ${it.stackTraceToString()}")
                                                                }
                                                              }
                                                        } else {
                                                            CoroutineScope(Dispatchers.IO).launch {
                                                                EnvironmentExt.addPlaylistToPlaylist(
                                                                    cleanPrefix(playlistPreview.playlist.browseId ?: ""),
                                                                    browseId.substringAfter("VL")

                                                                    )
                                                                }
                                                        }
                                                        CoroutineScope(Dispatchers.Main).launch {
                                                            SmartMessage(context.resources.getString(
                                                                        R.string.done
                                                                    ), type = PopupType.Success, context = context)
                                                        }
                                                    },
                                                    onGoToPlaylist = {
                                                        navController.navigate("${NavRoutes.localPlaylist.name}/$it")
                                                    },
                                                    disableScrollingText = disableScrollingText
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
                                enabled = playlistPage?.songs?.isNotEmpty() == true,
                                color = colorPalette().text,
                                onClick = {},
                                modifier = Modifier
                                    .padding(horizontal = 5.dp)
                                    .combinedClickable(
                                        onClick = {
                                            if (!isNetworkConnected(appContext()) && isYouTubeSyncEnabled()) {
                                                SmartMessage(appContext().resources.getString(R.string.no_connection), context = appContext(), type = PopupType.Error)
                                            } else if (!isYouTubeSyncEnabled()){
                                                Database.asyncTransaction {
                                                    playlistPage!!.songs.filter { getLikedAt(it.asMediaItem.mediaId) in listOf(-1L,null) }.forEachIndexed { _, song ->
                                                        Database.asyncTransaction {
                                                            if (like(song.asMediaItem.mediaId, setLikeState(song.asSong.likedAt)) == 0
                                                            ) {
                                                                insert(song.asMediaItem, Song::toggleLike)
                                                            }
                                                        }
                                                    }
                                                    SmartMessage(
                                                        context.resources.getString(R.string.done),
                                                        context = context
                                                    )
                                                }
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
                            if (isYouTubeSyncEnabled()) {
                                HeaderIconButton(
                                    icon = if (localPlaylist?.isYoutubePlaylist == true) R.drawable.bookmark else R.drawable.bookmark_outline,
                                    color = colorPalette().text,
                                    onClick = {},
                                    modifier = Modifier
                                        .padding(horizontal = 5.dp)
                                        .combinedClickable(
                                            onClick = {
                                                if (isNetworkConnected(context)) {
                                                    if (localPlaylist?.isYoutubePlaylist == true) {
                                                        CoroutineScope(Dispatchers.IO).launch {
                                                            EnvironmentExt.removelikePlaylistOrAlbum(
                                                                browseId.substringAfter("VL")
                                                            )
                                                        }
                                                        Database.asyncTransaction {
                                                            Database.playlistWithBrowseId(
                                                                browseId.substringAfter(
                                                                    "VL"
                                                                )
                                                            )
                                                                ?.let { delete(it) }
                                                        }
                                                    } else {
                                                        CoroutineScope(Dispatchers.IO).launch {
                                                            EnvironmentExt.likePlaylistOrAlbum(
                                                                browseId.substringAfter(
                                                                    "VL"
                                                                )
                                                            )
                                                        }
                                                        Database.asyncTransaction {
                                                            val playlistId = insert(
                                                                Playlist(
                                                                    name = (playlistPage?.playlist?.title ?: ""),
                                                                    browseId = browseId.substringAfter("VL"),
                                                                    isYoutubePlaylist = true,
                                                                    isEditable = false
                                                                )
                                                            )

                                                            playlistPage?.songs
                                                                ?.map(Environment.SongItem::asMediaItem)
                                                                ?.onEach(::insert)
                                                                ?.mapIndexed { index, mediaItem ->
                                                                    SongPlaylistMap(
                                                                        songId = mediaItem.mediaId,
                                                                        playlistId = playlistId,
                                                                        position = index
                                                                    ).default()
                                                                }
                                                                ?.let(::insertSongPlaylistMaps)
                                                        }
                                                    }
                                                    SmartMessage(
                                                        context.resources.getString(R.string.done),
                                                        context = context
                                                    )
                                                    saveCheck = !saveCheck
                                                } else {
                                                    SmartMessage(context.resources.getString(R.string.no_connection), context = context, type = PopupType.Error)
                                                }
                                            },
                                            onLongClick = {
                                                SmartMessage(
                                                    context.resources.getString(R.string.save_youtube_library),
                                                    context = context
                                                )
                                            }
                                        )
                                )
                            }


                            /*
                            HeaderIconButton(
                                icon = R.drawable.share_social,
                                color = colorPalette().text,
                                onClick = {
                                    (playlistPage?.url ?: "https://music.youtube.com/playlist?list=${browseId.removePrefix("VL")}").let { url ->
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, url)
                                        }

                                        context.startActivity(Intent.createChooser(sendIntent, null))
                                    }
                                }
                            )
                             */

                        } else {
                            BasicText(
                                text = stringResource(R.string.info_wait_it_may_take_a_few_minutes),
                                style = typography().xxs.medium,
                                maxLines = 1
                            )
                        }
                    }
                    Row (
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier
                            .padding(all = 10.dp)
                            .fillMaxWidth()
                    ) {
                        AnimatedVisibility(visible = searching) {
                            val focusRequester = remember { FocusRequester() }
                            val focusManager = LocalFocusManager.current
                            val keyboardController = LocalSoftwareKeyboardController.current

                            LaunchedEffect(searching) {
                                focusRequester.requestFocus()
                            }

                            BasicTextField(
                                value = filter ?: "",
                                onValueChange = { filter = it },
                                textStyle = typography().xs.semiBold,
                                singleLine = true,
                                maxLines = 1,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    if (filter.isNullOrBlank()) filter = ""
                                    focusManager.clearFocus()
                                }),
                                cursorBrush = SolidColor(colorPalette().text),
                                decorationBox = { innerTextField ->
                                    Box(
                                        contentAlignment = Alignment.CenterStart,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 10.dp)
                                    ) {
                                        IconButton(
                                            onClick = {},
                                            icon = R.drawable.search,
                                            color = colorPalette().favoritesIcon,
                                            modifier = Modifier
                                                .align(Alignment.CenterStart)
                                                .size(16.dp)
                                        )
                                    }
                                    Box(
                                        contentAlignment = Alignment.CenterStart,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 30.dp)
                                    ) {
                                        androidx.compose.animation.AnimatedVisibility(
                                            visible = filter?.isEmpty() ?: true,
                                            enter = fadeIn(tween(100)),
                                            exit = fadeOut(tween(100)),
                                        ) {
                                            BasicText(
                                                text = stringResource(R.string.search),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                style = typography().xs.semiBold.secondary.copy(color = colorPalette().textDisabled)
                                            )
                                        }

                                        innerTextField()
                                    }
                                },
                                modifier = Modifier
                                    .height(30.dp)
                                    .fillMaxWidth()
                                    .background(
                                        colorPalette().background4,
                                        shape = thumbnailRoundness.shape()
                                    )
                                    .focusRequester(focusRequester)
                                    .onFocusChanged {
                                        if (!it.hasFocus) {
                                            keyboardController?.hide()
                                            if (filter?.isBlank() == true) {
                                                filter = null
                                                searching = false
                                            }
                                        }
                                    }
                            )
                        }
                    }
                }

                playlistPage?.description?.let { description ->
                    item(
                        key = "playlistInfo"
                    ) {

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
                                color = if (translateEnabled == true) colorPalette()
                                    .text else colorPalette()
                                    .textDisabled,
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
                                            SmartMessage(
                                                context.resources.getString(R.string.info_translation),
                                                context = context
                                            )
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
                            val nonTranslatedText by remember {
                                mutableStateOf(
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
                                style = typography().xxs.color(
                                    colorPalette()
                                        .textDisabled).align(
                                    TextAlign.Start
                                ),
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 16.dp)
                                //.padding(endPaddingValues)
                            )
                        }

                    }
                }

                itemsIndexed(items = playlistPage?.songs ?: emptyList()) { index, song ->

                    val isLocal by remember { derivedStateOf { song.asMediaItem.isLocal } }
                    downloadState = getDownloadState(song.asMediaItem.mediaId)
                    val isDownloaded = if (!isLocal) isDownloadedSong(song.asMediaItem.mediaId) else true

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

                            if (!isLocal)
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
                            downloadState = getDownloadState(song.asMediaItem.mediaId),
                            thumbnailSizePx = songThumbnailSizePx,
                            thumbnailSizeDp = songThumbnailSizeDp,
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
                                                    navController.navigate("${NavRoutes.videoOrSongInfo.name}/${song.key}")
                                                },
                                                mediaItem = song.asMediaItem,
                                                disableScrollingText = disableScrollingText
                                            )
                                        };
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    onClick = {
                                        if (song.asMediaItem.mediaId !in dislikedSongs) {
                                            searching = false
                                            filter = null
                                            playlistPage?.songs?.filter { it.asMediaItem.mediaId !in dislikedSongs }
                                                ?.map(Environment.SongItem::asMediaItem)
                                                ?.let { mediaItems ->
                                                    binder?.stopRadio()
                                                    binder?.player?.forcePlayAtIndex(
                                                        mediaItems,
                                                        mediaItems.indexOf(song.asMediaItem)
                                                    )
                                                }
                                        } else {SmartMessage(context.resources.getString(R.string.disliked_this_song),type = PopupType.Error, context = context)}
                                    }
                                ),
                            disableScrollingText = disableScrollingText,
                            isNowPlaying = binder?.player?.isNowPlaying(song.key) ?: false,
                            forceRecompose = forceRecompose
                        )
                    }
                }

                item(
                    key = "footer",
                    contentType = 0,
                ) {
                    Spacer(modifier = Modifier.height(Dimensions.bottomSpacer))
                }

                if (playlistPage == null) {
                    item(key = "loading") {
                        ShimmerHost(
                            modifier = Modifier
                                .fillParentMaxSize()
                        ) {
                            repeat(4) {
                                SongItemPlaceholder(thumbnailSizeDp = songThumbnailSizeDp)
                            }
                        }
                    }
                }
            }

            val showFloatingIcon by rememberPreference(showFloatingIconKey, false)
            if( UiType.ViMusic.isCurrent() && showFloatingIcon )
            FloatingActionsContainerWithScrollToTop(
                lazyListState = lazyListState,
                iconId = R.drawable.shuffle,
                onClick = {
                    if (playlistPage?.songs?.any { it.asMediaItem.mediaId !in dislikedSongs } == true) {
                        binder?.stopRadio()
                        playlistPage?.songs?.filter{ it.asMediaItem.mediaId !in dislikedSongs }?.shuffled()?.map(Environment.SongItem::asMediaItem)
                            ?.let {
                                binder?.player?.forcePlayFromBeginning(
                                    it
                                )
                            }
                    } else {
                        SmartMessage(context.resources.getString(R.string.disliked_this_collection),type = PopupType.Error, context = context)
                    }
                }
            )


        }
    }
}
