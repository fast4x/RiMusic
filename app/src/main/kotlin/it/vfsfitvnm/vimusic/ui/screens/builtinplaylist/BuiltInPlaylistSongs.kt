package it.vfsfitvnm.vimusic.ui.screens.builtinplaylist

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import coil.compose.AsyncImage
import it.vfsfitvnm.compose.persist.persist
import it.vfsfitvnm.compose.persist.persistList
import it.vfsfitvnm.compose.reordering.reorder
import it.vfsfitvnm.innertube.Innertube
import it.vfsfitvnm.innertube.models.bodies.NextBody
import it.vfsfitvnm.innertube.requests.relatedSongs
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerAwareWindowInsets
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.AlbumSortBy
import it.vfsfitvnm.vimusic.enums.BuiltInPlaylist
import it.vfsfitvnm.vimusic.enums.PlaylistSongSortBy
import it.vfsfitvnm.vimusic.enums.RecommendationsNumber
import it.vfsfitvnm.vimusic.enums.SongSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.models.Song
import it.vfsfitvnm.vimusic.models.SongPlaylistMap
import it.vfsfitvnm.vimusic.models.SongWithContentLength
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.service.DownloadUtil
import it.vfsfitvnm.vimusic.service.isLocal
import it.vfsfitvnm.vimusic.transaction
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.themed.ConfirmationDialog
import it.vfsfitvnm.vimusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderIconButton
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderWithIcon
import it.vfsfitvnm.vimusic.ui.components.themed.IconInfo
import it.vfsfitvnm.vimusic.ui.components.themed.InHistoryMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.NonQueuedMediaItemMenu
import it.vfsfitvnm.vimusic.ui.items.PlaylistItem
import it.vfsfitvnm.vimusic.ui.items.SongItem
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.onOverlay
import it.vfsfitvnm.vimusic.ui.styling.overlay
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.center
import it.vfsfitvnm.vimusic.utils.color
import it.vfsfitvnm.vimusic.utils.downloadedStateMedia
import it.vfsfitvnm.vimusic.utils.durationTextToMillis
import it.vfsfitvnm.vimusic.utils.enqueue
import it.vfsfitvnm.vimusic.utils.forcePlay
import it.vfsfitvnm.vimusic.utils.forcePlayAtIndex
import it.vfsfitvnm.vimusic.utils.forcePlayFromBeginning
import it.vfsfitvnm.vimusic.utils.formatAsTime
import it.vfsfitvnm.vimusic.utils.getDownloadState
import it.vfsfitvnm.vimusic.utils.isRecommendationEnabledKey
import it.vfsfitvnm.vimusic.utils.manageDownload
import it.vfsfitvnm.vimusic.utils.recommendationsNumberKey
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.vimusic.utils.songSortByKey
import it.vfsfitvnm.vimusic.utils.songSortOrderKey
import it.vfsfitvnm.vimusic.utils.thumbnail
import it.vfsfitvnm.vimusic.utils.thumbnailRoundnessKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import it.vfsfitvnm.vimusic.ui.components.themed.NowPlayingShow
import it.vfsfitvnm.vimusic.ui.components.themed.PlaylistsItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.SortMenu
import it.vfsfitvnm.vimusic.ui.styling.favoritesIcon

@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation", "StateFlowValueCalledInComposition")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun BuiltInPlaylistSongs(
    builtInPlaylist: BuiltInPlaylist,
    onSearchClick: () -> Unit
) {
    val context = LocalContext.current
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current

    var songs by persistList<Song>("${builtInPlaylist.name}/songs")

    var sortBy by rememberPreference(songSortByKey, SongSortBy.DateAdded)
    var sortOrder by rememberPreference(songSortOrderKey, SortOrder.Descending)

    var filter: String? by rememberSaveable { mutableStateOf(null) }

    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }

    var showConfirmDeleteDownloadDialog by remember {
        mutableStateOf(false)
    }
    var showConfirmDownloadAllDialog by remember {
        mutableStateOf(false)
    }



     LaunchedEffect(Unit, sortBy, sortOrder, filter) {
         when (builtInPlaylist) {

             BuiltInPlaylist.Downloaded -> {
                 DownloadUtil.getDownloadManager(context)
                 DownloadUtil.getDownloads()
                 DownloadUtil.downloads.value.keys.toList().let { Database.getSongsList(it) }
             }

             BuiltInPlaylist.Favorites -> Database
                 .songsFavorites(sortBy, sortOrder)

             BuiltInPlaylist.Offline -> Database
                 .songsOffline(sortBy, sortOrder)
                 .flowOn(Dispatchers.IO)
                 .map { songs ->
                     songs.filter { song ->
                         song.contentLength?.let {
                             binder?.cache?.isCached(song.song.id, 0, song.contentLength)
                         } ?: false
                     }.map(SongWithContentLength::song)
                 }
         }.collect { songs = it }

    }

    val thumbnailSizeDp = Dimensions.thumbnails.song
    val thumbnailSizePx = thumbnailSizeDp.px
    val playlistThumbnailSizeDp = Dimensions.thumbnails.playlist
    val playlistThumbnailSizePx = playlistThumbnailSizeDp.px
    val thumbnails = songs
        .takeWhile { it.thumbnailUrl?.isNotEmpty() ?: false }
        .take(4)
        .map { it.thumbnailUrl.thumbnail(playlistThumbnailSizePx / 2) }


    var filterCharSequence: CharSequence
    filterCharSequence = filter.toString()
    //Log.d("mediaItemFilter", "<${filter}>  <${filterCharSequence}>")
    if (!filter.isNullOrBlank())
    songs = songs
        .filter {
            it.title?.contains(filterCharSequence,true) ?: false
            || it.artistsText?.contains(filterCharSequence,true) ?: false
        }

    var searching by rememberSaveable { mutableStateOf(false) }

    val sortOrderIconRotation by animateFloatAsState(
        targetValue = if (sortOrder == SortOrder.Ascending) 0f else 180f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing), label = ""
    )

    var thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )

    val lazyListState = rememberLazyListState()

    var totalPlayTimes = 0L
    songs.forEach {
        totalPlayTimes += it.durationText?.let { it1 ->
            durationTextToMillis(it1)
        }?.toLong() ?: 0
    }

    //**** SMART RECOMMENDATION
    val recommendationsNumber by rememberPreference(recommendationsNumberKey,   RecommendationsNumber.`5`)
    var isRecommendationEnabled by rememberPreference(isRecommendationEnabledKey, false)
    var relatedSongsRecommendationResult by persist<Result<Innertube.RelatedSongs?>?>(tag = "home/relatedSongsResult")
    var songBaseRecommendation by persist<Song?>("home/songBaseRecommendation")
    var positionsRecommendationList = arrayListOf<Int>()
    if (isRecommendationEnabled) {
        LaunchedEffect(Unit,isRecommendationEnabled) {
                val song = songs.shuffled().firstOrNull()
                if (relatedSongsRecommendationResult == null || songBaseRecommendation?.id != song?.id) {
                    relatedSongsRecommendationResult =
                        Innertube.relatedSongs(NextBody(videoId = (song?.id ?: "HZnNt9nnEhw")))
                }
        }


        if (relatedSongsRecommendationResult != null) {
            for (index in 0..recommendationsNumber.number) {
                positionsRecommendationList.add((0..songs.size).random())
            }
        }
        //Log.d("mediaItem","positionsList "+positionsRecommendationList.toString())
        //**** SMART RECOMMENDATION
    }

    var scrollToNowPlaying by remember {
        mutableStateOf(false)
    }

    var nowPlayingItem by remember {
        mutableStateOf(-1)
    }

    var listMediaItems = remember {
        mutableListOf<MediaItem>()
    }

    var selectItems by remember {
        mutableStateOf(false)
    }

    var position by remember {
        mutableIntStateOf(0)
    }


    /*
    var showSortTypeSelectDialog by remember {
        mutableStateOf(false)
    }
     */



    Box {
        LazyColumn(
            state = lazyListState,
            contentPadding = LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
            modifier = Modifier
                .background(colorPalette.background0)
                .fillMaxSize()
        ) {
            item(
                key = "header",
                contentType = 0
            ) {

                HeaderWithIcon(
                    title = when (builtInPlaylist) {
                        BuiltInPlaylist.Favorites -> stringResource(R.string.favorites)
                        BuiltInPlaylist.Downloaded -> stringResource(R.string.downloaded)
                        BuiltInPlaylist.Offline -> stringResource(R.string.cached)
                    },
                    iconId = R.drawable.search,
                    enabled = true,
                    showIcon = true,
                    modifier = Modifier,
                    onClick = onSearchClick
                )

                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        //.background(colorPalette.background4)
                        .fillMaxSize(0.99F)
                        .background(
                            color = colorPalette.background4,
                            shape = thumbnailRoundness.shape()
                        )
                ) {

                    if (songs.isEmpty())
                    PlaylistItem(
                        icon = when (builtInPlaylist) {
                            BuiltInPlaylist.Favorites -> R.drawable.heart
                            BuiltInPlaylist.Downloaded -> R.drawable.downloaded
                            BuiltInPlaylist.Offline -> R.drawable.sync
                        },
                        colorTint = colorPalette.favoritesIcon,
                        name = when (builtInPlaylist) {
                            BuiltInPlaylist.Favorites -> stringResource(R.string.favorites)
                            BuiltInPlaylist.Downloaded -> stringResource(R.string.downloaded)
                            BuiltInPlaylist.Offline -> stringResource(R.string.cached)
                        },
                        songCount = null,
                        thumbnailSizeDp = playlistThumbnailSizeDp,
                        alternative = false,
                        modifier = Modifier
                            .padding(top = 14.dp)
                    )

                    if (songs.isNotEmpty())
                    PlaylistItem(
                        thumbnailContent = {
                            if (thumbnails.toSet().size == 1) {
                                AsyncImage(
                                    model = thumbnails.first().thumbnail(playlistThumbnailSizePx),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = it
                                )
                            } else {
                                Box(
                                    modifier = it
                                        .fillMaxSize()
                                ) {
                                    listOf(
                                        Alignment.TopStart,
                                        Alignment.TopEnd,
                                        Alignment.BottomStart,
                                        Alignment.BottomEnd
                                    ).forEachIndexed { index, alignment ->
                                        AsyncImage(
                                            model = thumbnails.getOrNull(index),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .align(alignment)
                                                .size(playlistThumbnailSizeDp / 2)
                                        )
                                    }
                                }
                            }
                        },
                        songCount = null,
                        name = "",
                        channelName = null,
                        thumbnailSizeDp = playlistThumbnailSizeDp,
                        alternative = true,
                        showName = false,
                        modifier = Modifier
                            .padding(top = 14.dp)
                    )


                    Column (
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .fillMaxHeight()
                        //.border(BorderStroke(1.dp, Color.White))
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))
                        IconInfo(
                            title = songs.size.toString(),
                            icon = painterResource(R.drawable.musical_notes)
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        IconInfo(
                            title = formatAsTime(totalPlayTimes),
                            icon = painterResource(R.drawable.time)
                        )
                        if (isRecommendationEnabled) {
                            Spacer(modifier = Modifier.height(5.dp))
                            IconInfo(
                                title = positionsRecommendationList.distinct().size.toString(),
                                icon = painterResource(R.drawable.smart_shuffle)
                            )
                        }
                        Spacer(modifier = Modifier.height(30.dp))
                    }



                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween, //Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth()
                ) {

                    if (builtInPlaylist == BuiltInPlaylist.Favorites) {
                        HeaderIconButton(
                            icon = R.drawable.downloaded,
                            enabled = songs.isNotEmpty(),
                            color = if (songs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                            onClick = {
                                showConfirmDownloadAllDialog = true
                            }
                        )
                    }

                    if (showConfirmDownloadAllDialog) {
                        ConfirmationDialog(
                            text = stringResource(R.string.do_you_really_want_to_download_all),
                            onDismiss = { showConfirmDownloadAllDialog = false },
                            onConfirm = {
                                showConfirmDownloadAllDialog = false
                                isRecommendationEnabled = false
                                downloadState = Download.STATE_DOWNLOADING
                                if (songs.isNotEmpty() == true)
                                    songs.forEach {
                                        binder?.cache?.removeResource(it.asMediaItem.mediaId)
                                        manageDownload(
                                            context = context,
                                            songId = it.asMediaItem.mediaId,
                                            songTitle = it.asMediaItem.mediaMetadata.title.toString(),
                                            downloadState = false
                                        )
                                    }
                            }
                        )
                    }

                    if (builtInPlaylist == BuiltInPlaylist.Favorites || builtInPlaylist == BuiltInPlaylist.Downloaded) {
                        HeaderIconButton(
                            icon = R.drawable.download,
                            enabled = songs.isNotEmpty(),
                            color = if (songs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                            onClick = {
                                showConfirmDeleteDownloadDialog = true
                            }
                        )

                        if (showConfirmDeleteDownloadDialog) {
                            ConfirmationDialog(
                                text = stringResource(R.string.do_you_really_want_to_delete_download),
                                onDismiss = { showConfirmDeleteDownloadDialog = false },
                                onConfirm = {
                                    showConfirmDeleteDownloadDialog = false
                                    downloadState = Download.STATE_DOWNLOADING
                                    if (songs.isNotEmpty() == true)
                                        songs.forEach {
                                            binder?.cache?.removeResource(it.asMediaItem.mediaId)
                                            manageDownload(
                                                context = context,
                                                songId = it.asMediaItem.mediaId,
                                                songTitle = it.asMediaItem.mediaMetadata.title.toString(),
                                                downloadState = true
                                            )
                                        }
                                }
                            )
                        }

                    }

                    /*
                    HeaderIconButton(
                        icon = R.drawable.enqueue,
                        enabled = songs.isNotEmpty(),
                        color = if (songs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                        onClick = {
                            binder?.player?.enqueue(songs.map(Song::asMediaItem))
                        }
                    )
                     */

                    HeaderIconButton(
                        icon = R.drawable.smart_shuffle,
                        enabled = true,
                        color = if (isRecommendationEnabled) colorPalette.text else colorPalette.textDisabled,
                        onClick = {
                            isRecommendationEnabled = !isRecommendationEnabled
                        }
                    )

                    HeaderIconButton(
                        icon = R.drawable.shuffle,
                        enabled = songs.isNotEmpty(),
                        color = if (songs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                        onClick = {
                            if (songs.isNotEmpty()) {
                                binder?.stopRadio()
                                binder?.player?.forcePlayFromBeginning(
                                    songs.shuffled().map(Song::asMediaItem)
                                )
                            }
                        }
                    )

                    HeaderIconButton(
                        icon = R.drawable.ellipsis_horizontal,
                        color = if (songs.isNotEmpty() == true) colorPalette.text else colorPalette.textDisabled,
                        enabled = songs.isNotEmpty() == true,
                        modifier = Modifier
                            .padding(end = 4.dp),
                        onClick = {
                            menuState.display {
                                PlaylistsItemMenu(
                                    onDismiss = menuState::hide,
                                    onSelect = { selectItems = true },
                                    onUncheck = {
                                        selectItems = false
                                        listMediaItems.clear()
                                    },
                                    onEnqueue = {
                                        if (listMediaItems.isEmpty()) {
                                            binder?.player?.enqueue(songs.map(Song::asMediaItem))
                                        } else {
                                            binder?.player?.enqueue(listMediaItems)
                                            listMediaItems.clear()
                                            selectItems = false
                                        }
                                    },
                                    onAddToPlaylist = { playlistPreview ->
                                        position =
                                            playlistPreview.songCount.minus(1) ?: 0
                                        //Log.d("mediaItem", " maxPos in Playlist $it ${position}")
                                        if (position > 0) position++ else position = 0
                                        //Log.d("mediaItem", "next initial pos ${position}")
                                        if (listMediaItems.isEmpty()) {
                                            songs.forEachIndexed { index, song ->
                                                transaction {
                                                    Database.insert(song.asMediaItem)
                                                    Database.insert(
                                                        SongPlaylistMap(
                                                            songId = song.asMediaItem.mediaId,
                                                            playlistId = playlistPreview.playlist.id,
                                                            position = position + index
                                                        )
                                                    )
                                                }
                                                //Log.d("mediaItemPos", "added position ${position + index}")
                                            }
                                        } else {
                                            listMediaItems.forEachIndexed { index, song ->
                                                //Log.d("mediaItemMaxPos", position.toString())
                                                transaction {
                                                    Database.insert(song)
                                                    Database.insert(
                                                        SongPlaylistMap(
                                                            songId = song.mediaId,
                                                            playlistId = playlistPreview.playlist.id,
                                                            position = position + index
                                                        )
                                                    )
                                                }
                                                //Log.d("mediaItemPos", "add position $position")
                                            }
                                            listMediaItems.clear()
                                            selectItems = false
                                        }
                                    },
                                )
                            }
                        }
                    )


                }

                Spacer(modifier = Modifier.height(10.dp))

                Row (
                    horizontalArrangement = Arrangement.SpaceBetween, //Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth()
                ) {

                    Row (
                        horizontalArrangement = Arrangement.SpaceBetween, //Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth(0.50f)
                    ) {
                        HeaderIconButton(
                            onClick = { searching = !searching },
                            icon = R.drawable.search_circle,
                            color = colorPalette.text,
                            iconSize = 24.dp
                        )
                        HeaderIconButton(
                            icon = R.drawable.locate,
                            enabled = songs.isNotEmpty(),
                            color = if (songs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
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
                            }
                        )
                        LaunchedEffect(scrollToNowPlaying) {
                            if (scrollToNowPlaying)
                                lazyListState.scrollToItem(nowPlayingItem, 1)
                            scrollToNowPlaying = false
                        }
                    }

                    Spacer(
                        modifier = Modifier
                            .width(30.dp)
                    )

                    if (builtInPlaylist != BuiltInPlaylist.Downloaded) {

                        BasicText(
                            text = when (sortBy) {
                                SongSortBy.Title -> stringResource(R.string.sort_title)
                                SongSortBy.DatePlayed -> stringResource(R.string.sort_date_played)
                                SongSortBy.PlayTime -> stringResource(R.string.sort_listening_time)
                                SongSortBy.DateAdded -> stringResource(R.string.sort_date_added)
                            },
                            style = typography.xs.semiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .clickable {
                                    menuState.display{
                                        SortMenu(
                                            title = stringResource(R.string.sorting_order),
                                            onDismiss = menuState::hide,
                                            onTitle = { sortBy = SongSortBy.Title },
                                            onDatePlayed = { sortBy = SongSortBy.DatePlayed },
                                            onDateAdded = { sortBy = SongSortBy.DateAdded },
                                            onPlayTime = { sortBy = SongSortBy.PlayTime },
                                        )
                                    }
                                    //showSortTypeSelectDialog = true
                                }
                        )
                        /*
                        if (showSortTypeSelectDialog)
                            ValueSelectorDialog(
                                onDismiss = { showSortTypeSelectDialog = false },
                                title = stringResource(R.string.sorting_order),
                                selectedValue = sortBy,
                                values = enumValues<SongSortBy>().toList(),
                                onValueSelected = { sortBy = it },
                                valueText = {
                                    when (it) {
                                        SongSortBy.Title -> stringResource(R.string.sort_title)
                                        SongSortBy.DatePlayed -> stringResource(R.string.sort_date_played)
                                        SongSortBy.PlayTime -> stringResource(R.string.sort_listening_time)
                                        SongSortBy.DateAdded -> stringResource(R.string.sort_date_added)
                                    }
                                }
                            )

                         */

                        /*
                        if (builtInPlaylist == BuiltInPlaylist.Favorites)
                            HeaderIconButton(
                                icon = R.drawable.up_right_arrow,
                                color = if (sortBy == SongSortBy.DatePlayed) colorPalette.text else colorPalette.textDisabled,
                                onClick = { sortBy = SongSortBy.DatePlayed }
                            )

                        HeaderIconButton(
                            icon = R.drawable.trending,
                            color = if (sortBy == SongSortBy.PlayTime) colorPalette.text else colorPalette.textDisabled,
                            onClick = { sortBy = SongSortBy.PlayTime }
                        )

                        HeaderIconButton(
                            icon = R.drawable.text,
                            color = if (sortBy == SongSortBy.Title) colorPalette.text else colorPalette.textDisabled,
                            onClick = { sortBy = SongSortBy.Title }
                        )

                        HeaderIconButton(
                            icon = R.drawable.time,
                            color = if (sortBy == SongSortBy.DateAdded) colorPalette.text else colorPalette.textDisabled,
                            onClick = { sortBy = SongSortBy.DateAdded }
                        )
                         */

                        /*
                        Spacer(
                            modifier = Modifier
                                .width(2.dp)
                        )
                           */
                        HeaderIconButton(
                            icon = R.drawable.arrow_up,
                            color = colorPalette.text,
                            onClick = { sortOrder = !sortOrder },
                            modifier = Modifier
                                .graphicsLayer { rotationZ = sortOrderIconRotation }
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
                            textStyle = typography.xs.semiBold,
                            singleLine = true,
                            maxLines = 1,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (filter.isNullOrBlank()) filter = ""
                                focusManager.clearFocus()
                            }),
                            cursorBrush = SolidColor(colorPalette.text),
                            decorationBox = { innerTextField ->
                                Box(
                                    contentAlignment = Alignment.CenterStart,
                                    modifier = Modifier.weight(1f)
                                        .padding(horizontal = 10.dp)
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
                                            style = typography.xs.semiBold.secondary.copy(color = colorPalette.textDisabled),
                                        )
                                    }

                                    innerTextField()
                                }
                            },
                            modifier = Modifier
                                .height(30.dp)
                                .fillMaxWidth()
                                .background(
                                    colorPalette.background4,
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

            itemsIndexed(
                items = songs,
                key = { _, song -> song.id },
                contentType = { _, song -> song },
            ) { index, song ->

                if (index in positionsRecommendationList.distinct()) {
                    val songRecommended = relatedSongsRecommendationResult?.getOrNull()?.songs?.shuffled()
                        ?.lastOrNull()
                    val duration = songRecommended?.durationText
                    songRecommended?.asMediaItem?.let {
                        SongItem(
                            song = it,
                            duration = duration,
                            isRecommended = true,
                            thumbnailSizeDp = thumbnailSizeDp,
                            thumbnailSizePx = thumbnailSizePx,
                            isDownloaded = false,
                            onDownloadClick = {},
                            downloadState = Download.STATE_STOPPED,
                            trailingContent = {},
                            onThumbnailContent = {},
                            modifier = Modifier
                                .clickable {
                                    binder?.stopRadio()
                                    binder?.player?.forcePlay(it)
                                }

                        )
                    }
                }

                val isLocal by remember { derivedStateOf { song.asMediaItem.isLocal } }
                downloadState = getDownloadState(song.asMediaItem.mediaId)
                val isDownloaded = if (!isLocal) downloadedStateMedia(song.asMediaItem.mediaId) else true
                SongItem(
                    song = song,
                    isDownloaded = isDownloaded,
                    onDownloadClick = {
                        binder?.cache?.removeResource(song.asMediaItem.mediaId)
                        query {
                            Database.insert(
                                Song(
                                    id = song.asMediaItem.mediaId,
                                    title = song.asMediaItem.mediaMetadata.title.toString(),
                                    artistsText = song.asMediaItem.mediaMetadata.artist.toString(),
                                    thumbnailUrl = song.thumbnailUrl,
                                    durationText = null
                                )
                            )
                        }

                        if (!isLocal)
                        manageDownload(
                            context = context,
                            songId = song.asMediaItem.mediaId,
                            songTitle = song.asMediaItem.mediaMetadata.title.toString(),
                            downloadState = isDownloaded
                        )
                    },
                    downloadState = downloadState,
                    thumbnailSizeDp = thumbnailSizeDp,
                    thumbnailSizePx = thumbnailSizePx,
                    onThumbnailContent = {
                        if (sortBy == SongSortBy.PlayTime) {
                            BasicText(
                                text = song.formattedTotalPlayTime,
                                style = typography.xxs.semiBold.center.color(colorPalette.onOverlay),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, colorPalette.overlay)
                                        ),
                                        shape = thumbnailShape
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .align(Alignment.BottomCenter)
                            )
                        }
                        if (nowPlayingItem > -1)
                            NowPlayingShow(song.asMediaItem.mediaId)
                    },
                    trailingContent = {
                        val checkedState = remember { mutableStateOf(false) }
                        if (selectItems)
                            Checkbox(
                                checked = checkedState.value,
                                onCheckedChange = {
                                    checkedState.value = it
                                    if (it) listMediaItems.add(song.asMediaItem) else
                                        listMediaItems.remove(song.asMediaItem)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = colorPalette.accent,
                                    uncheckedColor = colorPalette.text
                                )
                            )
                    },
                    modifier = Modifier
                        .combinedClickable(
                            onLongClick = {
                                menuState.display {
                                    when (builtInPlaylist) {
                                        BuiltInPlaylist.Favorites, BuiltInPlaylist.Downloaded -> NonQueuedMediaItemMenu(
                                            mediaItem = song.asMediaItem,
                                            onDismiss = menuState::hide
                                        )

                                        BuiltInPlaylist.Offline -> InHistoryMediaItemMenu(
                                            song = song,
                                            onDismiss = menuState::hide
                                        )
                                    }
                                }
                            },
                            onClick = {
                                binder?.stopRadio()
                                binder?.player?.forcePlayAtIndex(
                                    songs.map(Song::asMediaItem),
                                    index
                                )
                            }
                        )
                        .animateItemPlacement()
                )
            }

            }

        FloatingActionsContainerWithScrollToTop(lazyListState = lazyListState)

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
