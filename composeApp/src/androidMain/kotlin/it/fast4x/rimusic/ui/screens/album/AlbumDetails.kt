package it.fast4x.rimusic.ui.screens.album

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
import it.fast4x.compose.persist.persistList
import it.fast4x.environment.Environment
import it.fast4x.environment.EnvironmentExt
import it.fast4x.environment.models.NavigationEndpoint
import it.fast4x.environment.requests.AlbumPage
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.EXPLICIT_PREFIX
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.MODIFIED_PREFIX
import it.fast4x.rimusic.R
import it.fast4x.rimusic.appContext
import it.fast4x.rimusic.cleanPrefix
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.models.Album
import it.fast4x.rimusic.models.Info
import it.fast4x.rimusic.models.Playlist
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.models.SongPlaylistMap
import it.fast4x.rimusic.service.modern.isLocal
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.ShimmerHost
import it.fast4x.rimusic.ui.components.SwipeablePlaylistItem
import it.fast4x.rimusic.ui.components.themed.AlbumsItemMenu
import it.fast4x.rimusic.ui.components.themed.AutoResizeText
import it.fast4x.rimusic.ui.components.themed.ConfirmationDialog
import it.fast4x.rimusic.ui.components.themed.FontSizeRange
import it.fast4x.rimusic.ui.components.themed.HeaderIconButton
import it.fast4x.rimusic.ui.components.themed.IconButton
import it.fast4x.rimusic.ui.components.themed.InputTextDialog
import it.fast4x.rimusic.ui.components.themed.ItemsList
import it.fast4x.rimusic.ui.components.themed.LayoutWithAdaptiveThumbnail
import it.fast4x.rimusic.ui.components.themed.MultiFloatingActionsContainer
import it.fast4x.rimusic.ui.components.themed.NonQueuedMediaItemMenu
import it.fast4x.rimusic.ui.components.themed.NowPlayingSongIndicator
import it.fast4x.rimusic.ui.components.themed.SelectorDialog
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.ui.items.AlbumItem
import it.fast4x.rimusic.ui.items.AlbumItemPlaceholder
import it.fast4x.rimusic.ui.items.SongItem
import it.fast4x.rimusic.ui.items.SongItemPlaceholder
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.px
import it.fast4x.rimusic.utils.addNext
import it.fast4x.rimusic.utils.align
import it.fast4x.rimusic.utils.asMediaItem
import it.fast4x.rimusic.utils.center
import it.fast4x.rimusic.utils.color
import it.fast4x.rimusic.utils.conditional
import it.fast4x.rimusic.utils.disableScrollingTextKey
import it.fast4x.rimusic.utils.durationTextToMillis
import it.fast4x.rimusic.utils.enqueue
import it.fast4x.rimusic.utils.fadingEdge
import it.fast4x.rimusic.utils.forcePlayAtIndex
import it.fast4x.rimusic.utils.forcePlayFromBeginning
import it.fast4x.rimusic.utils.formatAsTime
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
import it.fast4x.rimusic.enums.PopupType
import it.fast4x.rimusic.models.SongAlbumMap
import it.fast4x.rimusic.service.MyDownloadHelper
import it.fast4x.rimusic.typography
import it.fast4x.rimusic.ui.screens.settings.isYouTubeSyncEnabled
import it.fast4x.rimusic.utils.addToYtLikedSongs
import it.fast4x.rimusic.utils.addToYtPlaylist
import it.fast4x.rimusic.utils.isNetworkConnected
import it.fast4x.rimusic.utils.mediaItemToggleLike
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@UnstableApi
@Composable
fun AlbumDetails(
    navController: NavController,
    browseId: String,
    albumPage: AlbumPage?,
    headerContent: @Composable (textButton: (@Composable () -> Unit)?) -> Unit,
    thumbnailContent: @Composable () -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {

    if (albumPage == null) return

    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val context = LocalContext.current

    var songs by persistList<Song>("album/$browseId/songs")
    var album by persist<Album?>("album/$browseId")
    //val albumPage by persist<Innertube.PlaylistOrAlbumPage?>("album/$browseId/albumPage")
    val parentalControlEnabled by rememberPreference(parentalControlEnabledKey, false)
    val disableScrollingText by rememberPreference(disableScrollingTextKey, false)
    var startSync by remember{ mutableStateOf(false) }
    var songPlaylist by remember {
        mutableIntStateOf(0)
    }
    var playlistsList by remember { mutableStateOf<List<Database.PlayListIdPosition>?>(null) }
    var songExists by remember { mutableStateOf(false) }
    var likedAt by remember {
        mutableStateOf<Long?>(null)
    }
    var playTime by remember {
        mutableStateOf<Long?>(null)
    }

    data class AlbumSongsState(
        val song : Song,
        val likedAt : Long? = null,
        val playtime : Long? = null,
        val songExists : Boolean = false,
        val playlistsList : List<Database.PlayListIdPosition>? = emptyList(),
    )

    LaunchedEffect(startSync) {
        if(!startSync || !isNetworkConnected(context)) {
            startSync = false
            return@LaunchedEffect
        }
        withContext(Dispatchers.IO) {
            Database.asyncTransaction {
                val albumSongsStateList = mutableListOf<AlbumSongsState>()
                songs.forEach { song ->
                    songPlaylist = Database.songUsedInPlaylists(song.id)
                    if (songPlaylist > 0) songExists = true
                    playlistsList = Database.playlistsUsedForSong(song.id)
                    likedAt = song.likedAt
                    playTime = song.totalPlayTimeMs
                    binder?.cache?.removeResource(song.id)
                    binder?.downloadCache?.removeResource(song.id)
                    val songState = AlbumSongsState(song, likedAt, playTime, songExists, playlistsList)
                    albumSongsStateList.add(songState)
                    Database.delete(song)
                }

                Database.upsert(
                    Album(
                        id = browseId,
                        title = if (album?.title?.startsWith(MODIFIED_PREFIX) == true) album?.title else albumPage?.album?.title,
                        thumbnailUrl = if (album?.thumbnailUrl?.startsWith(MODIFIED_PREFIX) == true) album?.thumbnailUrl else albumPage?.album?.thumbnail?.url,
                        year = albumPage?.album?.year,
                        authorsText = if (album?.authorsText?.startsWith(MODIFIED_PREFIX) == true) album?.authorsText else albumPage?.album?.authors
                            ?.joinToString("") { it.name ?: "" },
                        shareUrl = albumPage?.url,
                        timestamp = System.currentTimeMillis(),
                        bookmarkedAt = album?.bookmarkedAt,
                        isYoutubeAlbum = album?.isYoutubeAlbum == true
                    ),
                    albumPage
                        ?.songs?.distinct()
                        ?.map(Environment.SongItem::asMediaItem)
                        ?.onEach(Database::insert)
                        ?.mapIndexed { position, mediaItem ->
                            SongAlbumMap(
                                songId = mediaItem.mediaId,
                                albumId = browseId,
                                position = position
                            )
                        } ?: emptyList()
                )

                albumSongsStateList.forEach { albumSongsState ->
                    if ((albumSongsState.songExists || albumSongsState.likedAt != null || albumSongsState.playtime != null)
                        && songExist(albumSongsState.song.id) == 0) {
                        insert(albumSongsState.song)
                    }
                    if (albumSongsState.songExists) {
                        albumSongsState.playlistsList?.forEach { item ->
                            insert(
                                SongPlaylistMap(
                                    songId = albumSongsState.song.id,
                                    playlistId = item.playlistId,
                                    position = item.position
                                ).default()
                            )
                        }
                    }
                    if (albumSongsState.likedAt != null) {
                        Database.like(albumSongsState.song.id, albumSongsState.likedAt)
                    }
                    Database.incrementTotalPlayTimeMs(
                        albumSongsState.song.id,
                        albumSongsState.playtime ?: 0
                    )
                }
                startSync = false
            }
        }
    }

    LaunchedEffect(Unit) {
        Database.albumSongs(browseId).collect {
            songs = if (parentalControlEnabled)
                it.filter { !it.title.startsWith(EXPLICIT_PREFIX) } else it
        }
    }

    LaunchedEffect(Unit) {
        Database.album(browseId).collect { album = it }
    }

    /*
    val playlistPreviews by remember {
        Database.playlistPreviews(PlaylistSortBy.Name, SortOrder.Ascending)
    }.collectAsState(initial = emptyList(), context = Dispatchers.IO)

    var showPlaylistSelectDialog by remember {
        mutableStateOf(false)
    }
     */

    var showConfirmDeleteDownloadDialog by remember {
        mutableStateOf(false)
    }

    var showConfirmDownloadAllDialog by remember {
        mutableStateOf(false)
    }

    val thumbnailSizeDp = Dimensions.thumbnails.song
    val thumbnailAlbumSizeDp = Dimensions.thumbnails.album

    val thumbnailAlbumSizePx = thumbnailAlbumSizeDp.px

    val lazyListState = rememberLazyListState()

    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }

    var listMediaItems = remember {
        mutableListOf<MediaItem>()
    }

    var selectItems by remember {
        mutableStateOf(false)
    }

    var showSelectDialog by remember {
        mutableStateOf(false)
    }

    /*
    var showAddPlaylistSelectDialog by remember {
        mutableStateOf(false)
    }
     */

    var showSelectCustomizeAlbumDialog by remember {
        mutableStateOf(false)
    }
    var showDialogChangeAlbumTitle by remember {
        mutableStateOf(false)
    }
    var showDialogChangeAlbumAuthors by remember {
        mutableStateOf(false)
    }
    var showDialogChangeAlbumCover by remember {
        mutableStateOf(false)
    }
    var isCreatingNewPlaylist by rememberSaveable {
        mutableStateOf(false)
    }
    var totalPlayTimes = 0L
    songs.forEach {
        totalPlayTimes += it.durationText?.let { it1 ->
            durationTextToMillis(it1)
        }?.toLong() ?: 0
    }
    var position by remember {
        mutableIntStateOf(0)
    }

    var scrollToNowPlaying by remember {
        mutableStateOf(false)
    }

    var nowPlayingItem by remember {
        mutableStateOf(-1)
    }
    val hapticFeedback = LocalHapticFeedback.current

    if (showDialogChangeAlbumTitle)
        InputTextDialog(
            onDismiss = { showDialogChangeAlbumTitle = false },
            title = stringResource(R.string.update_title),
            value = album?.title.toString(),
            placeholder = stringResource(R.string.title),
            setValue = {
                if (it.isNotEmpty()) {
                    Database.asyncTransaction {
                        updateAlbumTitle(browseId, it)
                    }
                }
            },
            prefix = MODIFIED_PREFIX
        )
    if (showDialogChangeAlbumAuthors)
        InputTextDialog(
            onDismiss = { showDialogChangeAlbumAuthors = false },
            title = stringResource(R.string.update_authors),
            value = album?.authorsText.toString(),
            placeholder = stringResource(R.string.authors),
            setValue = {
                if (it.isNotEmpty()) {
                    Database.asyncTransaction {
                        updateAlbumAuthors(browseId, it)
                    }
                    //context.toast("Album Saved $it")
                }
            },
            prefix = MODIFIED_PREFIX
        )

    if (showDialogChangeAlbumCover)
        InputTextDialog(
            onDismiss = { showDialogChangeAlbumCover = false },
            title = stringResource(R.string.update_cover),
            value = album?.thumbnailUrl.toString(),
            placeholder = stringResource(R.string.cover),
            setValue = {
                if (it.isNotEmpty()) {
                    Database.asyncTransaction {
                        updateAlbumCover(browseId, it)
                    }
                    //context.toast("Album Saved $it")
                }
            },
            prefix = MODIFIED_PREFIX
        )

    if (isCreatingNewPlaylist)
        InputTextDialog(
            onDismiss = { isCreatingNewPlaylist = false },
            title = stringResource(R.string.new_playlist),
            value = "",
            placeholder = stringResource(R.string.new_playlist),
            setValue = {
                if (it.isNotEmpty()) {
                    Database.asyncTransaction {
                        insert(Playlist(name = it))
                    }
                }
            }
        )

    if (showConfirmDeleteDownloadDialog) {
        ConfirmationDialog(
            text = stringResource(R.string.do_you_really_want_to_delete_download),
            onDismiss = { showConfirmDeleteDownloadDialog = false },
            onConfirm = {
                showConfirmDeleteDownloadDialog = false
                downloadState = Download.STATE_DOWNLOADING
                if (listMediaItems.isEmpty()) {
                    if (songs.isNotEmpty() == true)
                        songs.forEach {
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
                } else {
                    runCatching {
                        listMediaItems.forEach {
                            binder?.cache?.removeResource(it.mediaId)
                            CoroutineScope(Dispatchers.IO).launch {
                                Database.deleteFormat( it.mediaId )
                            }
                            manageDownload(
                                context = context,
                                mediaItem = it,
                                downloadState = true
                            )
                            //listMediaItems.clear()
                            selectItems = false
                        }
                    }.onFailure {
                        Timber.e("Failed listMediaItems in AlbumDetailsModern ${it.stackTraceToString()}")
                    }
                }
            }
        )
    }

    if (showConfirmDownloadAllDialog) {
        ConfirmationDialog(
            text = stringResource(R.string.do_you_really_want_to_download_all),
            onDismiss = { showConfirmDownloadAllDialog = false },
            onConfirm = {
                showConfirmDownloadAllDialog = false
                downloadState = Download.STATE_DOWNLOADING
                if (listMediaItems.isEmpty()) {
                    if (songs.filter { it.likedAt != -1L }.isNotEmpty()){
                        songs.filter { it.likedAt != -1L }.forEach {
                            binder?.cache?.removeResource(it.asMediaItem.mediaId)
                            CoroutineScope(Dispatchers.IO).launch {
                                Database.deleteFormat(it.asMediaItem.mediaId)
                            }
                            manageDownload(
                                context = context,
                                mediaItem = it.asMediaItem,
                                downloadState = false
                            )
                        }
                    }
                } else {
                    runCatching {
                        listMediaItems.forEach {
                            binder?.cache?.removeResource(it.mediaId)
                            CoroutineScope(Dispatchers.IO).launch {
                                Database.deleteFormat( it.mediaId )
                            }
                            manageDownload(
                                context = context,
                                mediaItem = it,
                                downloadState = false
                            )
                            //listMediaItems.clear()
                            selectItems = false
                        }
                    }.onFailure {
                        Timber.e("Failed listMediaItems 1 in AlbumDetailsModern ${it.stackTraceToString()}")
                    }
                }
            }
        )
    }

    if (showSelectDialog)
        SelectorDialog(
            title = stringResource(R.string.enqueue),
            onDismiss = { showSelectDialog = false },
            values = listOf(
                Info("a", stringResource(R.string.enqueue_all)),
                Info("s", stringResource(R.string.enqueue_selected))
            ),
            onValueSelected = {
                if (it == "a") {
                    binder?.player?.enqueue(songs.map(Song::asMediaItem))
                } else selectItems = true

                showSelectDialog = false
            }
        )

    LaunchedEffect(scrollToNowPlaying) {
        if (scrollToNowPlaying)
            lazyListState.scrollToItem(nowPlayingItem, 1)
        scrollToNowPlaying = false
    }

    val sectionTextModifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 24.dp, bottom = 8.dp)

    var translateEnabled by remember {
        mutableStateOf(false)
    }

    val translator = Translator(getHttpClient())
    val languageDestination = languageDestination()

    LayoutWithAdaptiveThumbnail(thumbnailContent = thumbnailContent) {
        Box(
            modifier = Modifier
                .background(
                    colorPalette().background0
                )
                //.fillMaxSize()
                .fillMaxHeight()
                //.fillMaxWidth(if (navigationBarPosition == NavigationBarPosition.Left) 1f else contentWidth)
                .fillMaxWidth()
        ) {

            LazyColumn(
                state = lazyListState,
                //contentPadding = LocalPlayerAwareWindowInsets.current
                //    .only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
                modifier = Modifier
                    .background(
                        colorPalette().background0
                    )
                    .fillMaxSize()
            ) {
                item(
                    key = "header"
                ) {

                    val modifierArt = Modifier.fillMaxWidth()

                    Box(
                        modifier = modifierArt
                    ) {
                        if (album != null) {
                            if (!isLandscape)
                                Box {
                                    AsyncImage(
                                        model = album?.thumbnailUrl?.resize(1200, 1200),
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
                                    if (album?.isYoutubeAlbum == true){
                                        Image(
                                            painter = painterResource(R.drawable.ytmusic),
                                            colorFilter = ColorFilter.tint(Color.Red.copy(0.75f).compositeOver(Color.White)),
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
                                text = cleanPrefix(album?.title ?: ""),
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
                                    .conditional(!disableScrollingText) { basicMarquee(iterations = Int.MAX_VALUE) }
                                //.padding(bottom = 20.dp)
                            )

                            HeaderIconButton(
                                icon = R.drawable.share_social,
                                color = colorPalette().text,
                                iconSize = 24.dp,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 5.dp, end = 5.dp),
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
                                    BasicText(
                                        text = stringResource(R.string.info_wait_it_may_take_a_few_minutes),
                                        style = typography().xs.medium,
                                        maxLines = 1,
                                        modifier = Modifier
                                        //.padding(top = 10.dp)

                                    )
                                }
                            }
                        }
                    }

                }

                if (album?.year != null && songs.isNotEmpty())
                    item(
                        key = "infoAlbum"
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                //.padding(top = 10.dp)
                                .fillMaxWidth()
                        ) {
                            BasicText(
                                text = "${album?.year} - " + songs.size.toString() + " "
                                        + stringResource(R.string.songs)
                                        + " - " + formatAsTime(totalPlayTimes),
                                style = typography().xs.medium,
                                maxLines = 1
                            )
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
                        //headerContent {
                        HeaderIconButton(
                            icon = if (album?.bookmarkedAt == null) {
                                R.drawable.bookmark_outline
                            } else {
                                R.drawable.bookmark
                            },
                            color = colorPalette()
.accent,
                            modifier = Modifier
                                .padding(horizontal = 25.dp)
                                .combinedClickable(
                                    onClick = {
                                        if (isYouTubeSyncEnabled() && !isNetworkConnected(context)){
                                            SmartMessage(context.resources.getString(R.string.no_connection), context = context, type = PopupType.Error)
                                        } else {
                                            val bookmarkedAt =
                                                if (album?.bookmarkedAt == null) System.currentTimeMillis() else null

                                            Database.asyncTransaction {
                                                album
                                                    ?.copy(bookmarkedAt = bookmarkedAt)
                                                    ?.let(::update)
                                            }

                                            if (bookmarkedAt != null) {
                                                MyDownloadHelper.autoDownloadWhenAlbumBookmarked(
                                                    context,
                                                    songs.map { it.asMediaItem })
                                            }

                                            if (isYouTubeSyncEnabled())
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    if (bookmarkedAt == null)
                                                        albumPage?.album?.playlistId.let {
                                                            if (it != null) {
                                                                EnvironmentExt.removelikePlaylistOrAlbum(it)
                                                                Database.asyncTransaction {
                                                                    update(album!!.copy(isYoutubeAlbum = false))
                                                                }
                                                            }
                                                        }
                                                    else
                                                        albumPage?.album?.playlistId.let {
                                                            if (it != null) {
                                                                EnvironmentExt.likePlaylistOrAlbum(it)
                                                                if (album != null) {
                                                                    Database.asyncTransaction {
                                                                        update(album!!.copy(isYoutubeAlbum = true))
                                                                    }
                                                                }
                                                            }
                                                        }
                                                }
                                        }
                                    },
                                    onLongClick = {
                                        SmartMessage(
                                            context.resources.getString(R.string.info_bookmark_album),
                                            context = context
                                        )
                                    }
                                ),
                            onClick = {}
                        )
                        HeaderIconButton(
                            icon = R.drawable.downloaded,
                            color = if (songs.any { it.likedAt != -1L }) colorPalette().text else colorPalette().textDisabled,
                            onClick = {},
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .combinedClickable(
                                    onClick = {
                                        if (songs.any { it.likedAt != -1L }) {
                                            showConfirmDownloadAllDialog = true
                                        } else {
                                            SmartMessage(context.resources.getString(R.string.disliked_this_collection),type = PopupType.Error, context = context)
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
                            color = colorPalette()
.text,
                            onClick = {},
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .combinedClickable(
                                    onClick = {
                                        showConfirmDeleteDownloadDialog = true
                                    },
                                    onLongClick = {
                                        SmartMessage(
                                            context.resources.getString(R.string.info_remove_all_downloaded_songs),
                                            context = context
                                        )
                                    }
                                )
                        )


                        /*
                    HeaderIconButton(
                        icon = R.drawable.enqueue,
                        enabled = songs.isNotEmpty(),
                        color = if (songs.isNotEmpty()) colorPalette()
.text else colorPalette()
.textDisabled,
                        onClick = {
                            if (!selectItems)
                            showSelectDialog = true else {
                                binder?.player?.enqueue(listMediaItems)
                                listMediaItems.clear()
                                selectItems = false
                            }

                        }
                    )
                     */



                        HeaderIconButton(
                            icon = R.drawable.shuffle,
                            enabled = songs.any { it.likedAt != -1L },
                            color = if (songs.any { it.likedAt != -1L }) colorPalette().text else colorPalette().textDisabled,
                            onClick = {},
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .combinedClickable(
                                    onClick = {
                                        if (songs.any { it.likedAt != -1L }) {
                                            binder?.stopRadio()
                                            binder?.player?.forcePlayFromBeginning(
                                                songs.filter { it.likedAt != -1L }
                                                    .shuffled()
                                                    .map(Song::asMediaItem)
                                            )
                                        } else {
                                            SmartMessage(context.resources.getString(R.string.disliked_this_collection),type = PopupType.Error, context = context)
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
                            enabled = true,
                            color = if (songs.any { it.likedAt != -1L }) colorPalette().text else colorPalette().textDisabled,
                            onClick = {},
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .combinedClickable(
                                    onClick = {
                                        if (songs.any { it.likedAt != -1L }) {
                                            binder?.stopRadio()
                                            binder?.player?.forcePlayFromBeginning(songs.filter { it.likedAt != -1L }.map(Song::asMediaItem))
                                            binder?.setupRadio(NavigationEndpoint.Endpoint.Watch(videoId = songs.first { it.likedAt != -1L }.id))
                                        } else {
                                            SmartMessage(context.resources.getString(R.string.disliked_this_collection),type = PopupType.Error, context = context)
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
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .combinedClickable(
                                    onClick = {
                                        nowPlayingItem = -1
                                        scrollToNowPlaying = false
                                        songs
                                            .forEachIndexed { index, song ->
                                                if (song.asMediaItem.mediaId == binder?.player?.currentMediaItem?.mediaId)
                                                    nowPlayingItem = index
                                            }

                                        if (nowPlayingItem > -1)
                                            scrollToNowPlaying = true
                                    },
                                    onLongClick = {
                                        SmartMessage(
                                            context.resources.getString(R.string.info_find_the_song_that_is_playing),
                                            context = context
                                        )
                                    }
                                ),
                            icon = R.drawable.locate,
                            enabled = songs.isNotEmpty(),
                            color = if (songs.isNotEmpty()) colorPalette()
.text else colorPalette()
.textDisabled,
                            onClick = {}


                        )

                        HeaderIconButton(
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .combinedClickable(
                                    onClick = {startSync = true},
                                    onLongClick = {
                                        SmartMessage(
                                            context.resources.getString(R.string.update_album),
                                            context = context
                                        )
                                    }
                                ),
                            icon = R.drawable.update,
                            enabled = songs.isNotEmpty(),
                            color = if (songs.isNotEmpty()) colorPalette()
                                .text else colorPalette()
                                .textDisabled,
                            onClick = {}
                        )


                        HeaderIconButton(
                            modifier = Modifier
                                .padding(horizontal = 5.dp),
                            icon = R.drawable.ellipsis_horizontal,
                            enabled = songs.isNotEmpty(),
                            color = if (songs.isNotEmpty()) colorPalette()
.text else colorPalette()
.textDisabled,
                            onClick = {
                                menuState.display {
                                    album?.let { it ->
                                        AlbumsItemMenu(
                                            navController = navController,
                                            onDismiss = menuState::hide,
                                            onSelectUnselect = {
                                                selectItems = !selectItems
                                                if (!selectItems) {
                                                    listMediaItems.clear()
                                                }
                                            },
                                            /*
                                        onSelect = { selectItems = true },
                                        onUncheck = {
                                            selectItems = false
                                            listMediaItems.clear()
                                        },
                                         */
                                            onChangeAlbumTitle = {
                                                if (album?.isYoutubeAlbum == true){
                                                    SmartMessage(context.resources.getString(R.string.cant_rename_Saved_albums),type = PopupType.Error, context = context)
                                                } else
                                                showDialogChangeAlbumTitle = true
                                            },
                                            onChangeAlbumAuthors = {
                                                showDialogChangeAlbumAuthors = true
                                            },
                                            onChangeAlbumCover = {
                                                showDialogChangeAlbumCover = true
                                            },
                                            onPlayNext = {
                                                if (listMediaItems.isEmpty()) {
                                                    if (songs.any { it.likedAt != -1L }) {
                                                        binder?.player?.addNext(
                                                            songs.filter { it.likedAt != -1L }
                                                                .map(Song::asMediaItem),
                                                            context
                                                        )
                                                    } else {
                                                        SmartMessage(context.resources.getString(R.string.disliked_this_collection),type = PopupType.Error, context = context)
                                                    }
                                                } else {
                                                    binder?.player?.addNext(listMediaItems, context)
                                                    listMediaItems.clear()
                                                    selectItems = false
                                                }
                                            },
                                            onEnqueue = {
                                                if (listMediaItems.isEmpty()) {
                                                    if (songs.any { it.likedAt != -1L }) {
                                                        binder?.player?.enqueue(
                                                            songs.filter { it.likedAt != -1L }
                                                                .map(Song::asMediaItem),context
                                                        )
                                                    } else {
                                                        SmartMessage(context.resources.getString(R.string.disliked_this_collection),type = PopupType.Error, context = context)
                                                    }
                                                } else {
                                                    binder?.player?.enqueue(listMediaItems, context)
                                                    listMediaItems.clear()
                                                    selectItems = false
                                                }
                                            },
                                            album = it,
                                            onAddToPlaylist = { playlistPreview ->
                                                position =
                                                    playlistPreview.songCount.minus(1) ?: 0
                                                //Log.d("mediaItem", " maxPos in Playlist $it ${position}")
                                                if (position > 0) position++ else position =
                                                    0
                                                //Log.d("mediaItem", "next initial pos ${position}")
                                                if (listMediaItems.isEmpty()) {
                                                    if (!isYouTubeSyncEnabled() || !playlistPreview.playlist.isYoutubePlaylist) {
                                                        songs.forEachIndexed { index, song ->
                                                            Database.asyncTransaction {
                                                                insert(song.asMediaItem)
                                                                insert(
                                                                    SongPlaylistMap(
                                                                        songId = song.asMediaItem.mediaId,
                                                                        playlistId = playlistPreview.playlist.id,
                                                                        position = position + index
                                                                    ).default()
                                                                )
                                                            }
                                                        }
                                                    } else {
                                                        CoroutineScope(Dispatchers.IO).launch {
                                                            EnvironmentExt.addPlaylistToPlaylist(
                                                                cleanPrefix(playlistPreview.playlist.browseId ?: ""),
                                                                cleanPrefix(albumPage?.album?.playlistId ?: "")
                                                            ).onSuccess {
                                                                songs.forEachIndexed { index, song ->
                                                                    Database.asyncTransaction {
                                                                        insert(song.asMediaItem)
                                                                        insert(
                                                                            SongPlaylistMap(
                                                                                songId = song.asMediaItem.mediaId,
                                                                                playlistId = playlistPreview.playlist.id,
                                                                                position = position + index
                                                                            ).default()
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    if (!isYouTubeSyncEnabled() || !playlistPreview.playlist.isYoutubePlaylist) {
                                                        listMediaItems.forEachIndexed { index, song ->
                                                            //Log.d("mediaItemMaxPos", position.toString())
                                                            Database.asyncTransaction {
                                                                insert(song)
                                                                insert(
                                                                    SongPlaylistMap(
                                                                        songId = song.mediaId,
                                                                        playlistId = playlistPreview.playlist.id,
                                                                        position = position + index
                                                                    ).default()
                                                                )
                                                            }
                                                        }
                                                    } else {
                                                        CoroutineScope(Dispatchers.IO).launch {
                                                            addToYtPlaylist(
                                                                playlistPreview.playlist.id,
                                                                position,
                                                                cleanPrefix(playlistPreview.playlist.browseId ?: ""),
                                                                listMediaItems
                                                            )
                                                        }
                                                    }
                                                    listMediaItems.clear()
                                                    selectItems = false
                                                }
                                            },
                                            onAddToFavourites = {
                                                if (!isNetworkConnected(appContext()) && isYouTubeSyncEnabled()) {
                                                    SmartMessage(appContext().resources.getString(R.string.no_connection), context = appContext(), type = PopupType.Error)
                                                } else if (!isYouTubeSyncEnabled()){
                                                    songs.forEach { song ->
                                                        val likedAt: Long? = song.likedAt
                                                        if (likedAt == null) {
                                                            mediaItemToggleLike(song.asMediaItem)
                                                        }
                                                    }

                                                } else {
                                                    val totalSongsToLike = songs.filter {
                                                        it.likedAt in listOf(-1L,null)
                                                    }
                                                    CoroutineScope(Dispatchers.IO).launch {
                                                        addToYtLikedSongs(totalSongsToLike.map { it.asMediaItem })
                                                    }
                                                }
                                            },
                                            onGoToPlaylist = {
                                                navController.navigate("${NavRoutes.localPlaylist.name}/$it")
                                            },
                                            disableScrollingText = disableScrollingText
                                        )
                                    }
                                }

                            }
                        )

                    }
                }

                albumPage?.description?.let { description ->
                    item(
                        key = "albumInfo"
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

                item(
                    key = "songsTitle"
                ) {
                    BasicText(
                        text = stringResource(R.string.songs),
                        style = typography().m.semiBold.align(TextAlign.Start),
                        modifier = sectionTextModifier
                            .fillMaxWidth()
                    )
                }
                itemsIndexed(
                    items = songs,
                    key = { _, song -> song.id }
                ) { index, song ->
                    val isLocal by remember { derivedStateOf { song.asMediaItem.isLocal } }
                    downloadState = getDownloadState(song.asMediaItem.mediaId)
                    val isDownloaded =
                        if (!isLocal) isDownloadedSong(song.asMediaItem.mediaId) else true

                    SwipeablePlaylistItem(
                        mediaItem = song.asMediaItem,
                        onPlayNext = {
                            binder?.player?.addNext(song.asMediaItem)
                        },
                        onDownload = {
                            binder?.cache?.removeResource(song.asMediaItem.mediaId)
                            Database.asyncTransaction {
                                resetContentLength( song.asMediaItem.mediaId )
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
                        val checkedState = rememberSaveable { mutableStateOf(false) }
                        var forceRecompose by remember { mutableStateOf(false) }
                        SongItem(
                            mediaItem = song.asMediaItem,
                            downloadState = getDownloadState(song.asMediaItem.mediaId),
                            onDownloadClick = {
                                binder?.cache?.removeResource(song.asMediaItem.mediaId)
                                Database.asyncTransaction {
                                    deleteFormat( song.asMediaItem.mediaId )
                                }

                                if (!isLocal)
                                    manageDownload(
                                        context = context,
                                        mediaItem = song.asMediaItem,
                                        downloadState = isDownloaded
                                    )
                            },
                            thumbnailSizeDp = thumbnailSizeDp,
                            thumbnailContent = {
                                /*
                            AsyncImage(
                                model = song.thumbnailUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .clip(LocalAppearance.current.thumbnailShape)
                                    .fillMaxSize()
                            )
                             */
                                BasicText(
                                    text = "${index + 1}",
                                    style = typography().s.semiBold.center.color(
                                        colorPalette()
.textDisabled),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .width(thumbnailSizeDp)
                                        .align(Alignment.Center)
                                )


                                    NowPlayingSongIndicator(song.asMediaItem.mediaId, binder?.player)
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
                                                onInfo = {
                                                    navController.navigate("${NavRoutes.videoOrSongInfo.name}/${song.id}")
                                                },
                                                mediaItem = song.asMediaItem,
                                                disableScrollingText = disableScrollingText
                                            )
                                        }
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    onClick = {
                                        if (!selectItems) {
                                            if (song.likedAt != -1L) {
                                                binder?.stopRadio()
                                                binder?.player?.forcePlayAtIndex(
                                                    songs.filter { it.likedAt != -1L }.map(Song::asMediaItem),
                                                    songs.filter { it.likedAt != -1L }.map(Song::asMediaItem).indexOf(song.asMediaItem)
                                                )
                                            } else {
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    SmartMessage(context.resources.getString(R.string.disliked_this_song),type = PopupType.Error, context = context)
                                                }
                                            }
                                        } else checkedState.value = !checkedState.value
                                    }
                                ),
                            trailingContent = {
                                if (selectItems)
                                    Checkbox(
                                        checked = checkedState.value,
                                        onCheckedChange = {
                                            checkedState.value = it
                                            if (it) listMediaItems.add(song.asMediaItem) else
                                                listMediaItems.remove(song.asMediaItem)
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = colorPalette()
.accent,
                                            uncheckedColor = colorPalette()
.text
                                        ),
                                        modifier = Modifier
                                            .scale(0.7f)
                                    )
                                else checkedState.value = false
                            },
                            //mediaId = song.asMediaItem.mediaId
                            disableScrollingText = disableScrollingText,
                            isNowPlaying = binder?.player?.isNowPlaying(song.id) ?: false,
                            forceRecompose = forceRecompose
                        )
                    }
                }

                item(key = "alternateVersionsTitle") {
                    BasicText(
                        text = stringResource(R.string.album_alternative_versions),
                        style = typography().m.semiBold,
                        maxLines = 1,
                        modifier = Modifier
                            .padding(all = 16.dp)

                    )

                }

                item(key = "alternateVersions") {
                    ItemsList(
                        tag = "album/$browseId/alternatives",
                        headerContent = {},
                        initialPlaceholderCount = 1,
                        continuationPlaceholderCount = 1,
                        emptyItemsText = stringResource(R.string.album_no_alternative_version),
                        itemsPageProvider = albumPage?.let {
                            ({
                                Result.success(
                                    Environment.ItemsPage(
                                        items = albumPage.otherVersions,
                                        continuation = null
                                    )
                                )
                            })
                        } ?: { Result.success(Environment.ItemsPage(items = emptyList(), continuation = null)) },
                        itemContent = { album ->
                            AlbumItem(
                                alternative = true,
                                album = album,
                                thumbnailSizePx = thumbnailAlbumSizePx,
                                thumbnailSizeDp = thumbnailAlbumSizeDp,
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

                            /**********/

                    /**********/
                }

                item(key = "bottom") {
                    Spacer(modifier = Modifier.height(Dimensions.bottomSpacer))
                }

                if (songs.isEmpty()) {
                    item(key = "loading") {
                        ShimmerHost(
                            modifier = Modifier
                                .fillParentMaxSize()
                        ) {
                            repeat(1) {
                                AlbumItemPlaceholder(thumbnailSizeDp = Dimensions.thumbnails.album)
                            }
                            repeat(4) {
                                SongItemPlaceholder(thumbnailSizeDp = Dimensions.thumbnails.song)
                            }
                        }
                    }
                }


            }


            val showFloatingIcon by rememberPreference(showFloatingIconKey, false)
            if ( UiType.ViMusic.isCurrent() && showFloatingIcon )
                MultiFloatingActionsContainer(
                    iconId = R.drawable.shuffle,
                    onClick = {
                        if (songs.any { it.likedAt != -1L }) {
                            binder?.stopRadio()
                            binder?.player?.forcePlayFromBeginning(
                                songs.filter { it.likedAt != -1L }
                                    .shuffled()
                                    .map(Song::asMediaItem)
                            )
                        } else {
                            SmartMessage(context.resources.getString(R.string.disliked_this_collection),type = PopupType.Error, context = context)
                        }
                    },
                    onClickSettings = onSettingsClick,
                    onClickSearch = onSearchClick
                )

            /*
            FloatingActionsContainerWithScrollToTop(
                lazyListState = lazyListState,
                iconId = R.drawable.shuffle,
                onClick = {
                    if (songs.isNotEmpty()) {
                        binder?.stopRadio()
                        binder?.player?.forcePlayFromBeginning(
                            songs.shuffled().map(Song::asMediaItem)
                        )
                    }
                }
            )

             */


        }


    }


}
