package it.fast4x.rimusic.ui.screens.localplaylist


import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
import com.github.doyaaaaaken.kotlincsv.client.KotlinCsvExperimental
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import it.fast4x.compose.persist.persist
import it.fast4x.compose.persist.persistList
import it.fast4x.compose.reordering.draggedItem
import it.fast4x.compose.reordering.rememberReorderingState
import it.fast4x.compose.reordering.reorder
import it.fast4x.innertube.Innertube
import it.fast4x.innertube.YtMusic
import it.fast4x.innertube.models.bodies.NextBody
import it.fast4x.innertube.requests.relatedSongs
import it.fast4x.innertube.utils.completed
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.Database.Companion.songAlbumInfo
import it.fast4x.rimusic.Database.Companion.songArtistInfo
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.MaxSongs
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.enums.PlaylistSongSortBy
import it.fast4x.rimusic.enums.PopupType
import it.fast4x.rimusic.enums.RecommendationsNumber
import it.fast4x.rimusic.enums.SortOrder
import it.fast4x.rimusic.enums.ThumbnailRoundness
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.models.Playlist
import it.fast4x.rimusic.models.PlaylistPreview
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.models.SongPlaylistMap
import it.fast4x.rimusic.service.isLocal
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.SwipeableQueueItem
import it.fast4x.rimusic.ui.components.themed.ConfirmationDialog
import it.fast4x.rimusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import it.fast4x.rimusic.ui.components.themed.HeaderIconButton
import it.fast4x.rimusic.ui.components.themed.HeaderWithIcon
import it.fast4x.rimusic.ui.components.themed.IconButton
import it.fast4x.rimusic.ui.components.themed.IconInfo
import it.fast4x.rimusic.ui.components.themed.InPlaylistMediaItemMenu
import it.fast4x.rimusic.ui.components.themed.InputTextDialog
import it.fast4x.rimusic.ui.components.themed.Playlist
import it.fast4x.rimusic.ui.components.themed.PlaylistsItemMenu
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.ui.components.themed.SortMenu
import it.fast4x.rimusic.ui.items.SongItem
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.LocalAppearance
import it.fast4x.rimusic.ui.styling.favoritesIcon
import it.fast4x.rimusic.ui.styling.onOverlay
import it.fast4x.rimusic.ui.styling.overlay
import it.fast4x.rimusic.ui.styling.px
import it.fast4x.rimusic.utils.UiTypeKey
import it.fast4x.rimusic.utils.addNext
import it.fast4x.rimusic.utils.addToPipedPlaylist
import it.fast4x.rimusic.utils.asMediaItem
import it.fast4x.rimusic.utils.center
import it.fast4x.rimusic.utils.color
import it.fast4x.rimusic.utils.deletePipedPlaylist
import it.fast4x.rimusic.utils.durationTextToMillis
import it.fast4x.rimusic.utils.enqueue
import it.fast4x.rimusic.utils.forcePlay
import it.fast4x.rimusic.utils.forcePlayAtIndex
import it.fast4x.rimusic.utils.forcePlayFromBeginning
import it.fast4x.rimusic.utils.formatAsTime
import it.fast4x.rimusic.utils.getDownloadState
import it.fast4x.rimusic.utils.getPipedSession
import it.fast4x.rimusic.utils.isLandscape
import it.fast4x.rimusic.utils.isPipedEnabledKey
import it.fast4x.rimusic.utils.isRecommendationEnabledKey
import it.fast4x.rimusic.utils.manageDownload
import it.fast4x.rimusic.utils.maxSongsInQueueKey
import it.fast4x.rimusic.utils.navigationBarPositionKey
import it.fast4x.rimusic.utils.playlistSongSortByKey
import it.fast4x.rimusic.utils.recommendationsNumberKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.removeFromPipedPlaylist
import it.fast4x.rimusic.utils.renamePipedPlaylist
import it.fast4x.rimusic.utils.reorderInQueueEnabledKey
import it.fast4x.rimusic.utils.secondary
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.showFloatingIconKey
import it.fast4x.rimusic.utils.songSortOrderKey
import it.fast4x.rimusic.utils.syncSongsInPipedPlaylist
import it.fast4x.rimusic.utils.thumbnailRoundnessKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID
import it.fast4x.rimusic.cleanPrefix
import it.fast4x.rimusic.MONTHLY_PREFIX
import it.fast4x.rimusic.PINNED_PREFIX
import it.fast4x.rimusic.PIPED_PREFIX
import it.fast4x.rimusic.YTP_PREFIX
import it.fast4x.rimusic.appContext
import it.fast4x.rimusic.enums.PlaylistSongsTypeFilter
import it.fast4x.rimusic.ui.components.themed.NowPlayingSongIndicator
import it.fast4x.rimusic.ui.screens.settings.isYouTubeSyncEnabled
import it.fast4x.rimusic.utils.checkFileExists
import it.fast4x.rimusic.utils.deleteFileIfExists
import it.fast4x.rimusic.utils.disableScrollingTextKey
import it.fast4x.rimusic.utils.isDownloadedSong
import it.fast4x.rimusic.utils.isNowPlaying
import it.fast4x.rimusic.utils.saveImageToInternalStorage
import kotlinx.coroutines.CoroutineScope
import it.fast4x.rimusic.models.SongEntity
import it.fast4x.rimusic.service.LOCAL_KEY_PREFIX
import it.fast4x.rimusic.service.MyDownloadHelper
import it.fast4x.rimusic.ui.components.themed.FilterMenu
import it.fast4x.rimusic.ui.components.themed.InProgressDialog
import it.fast4x.rimusic.ui.components.themed.SongMatchingDialog
import it.fast4x.rimusic.utils.addToYtLikedSongs
import it.fast4x.rimusic.utils.addToYtPlaylist
import it.fast4x.rimusic.utils.asSong
import it.fast4x.rimusic.utils.formatAsDuration
import it.fast4x.rimusic.utils.getAlbumVersionFromVideo
import it.fast4x.rimusic.utils.isExplicit
import it.fast4x.rimusic.utils.isNetworkConnected
import it.fast4x.rimusic.utils.mediaItemToggleLike
import it.fast4x.rimusic.utils.playlistSongsTypeFilterKey
import it.fast4x.rimusic.utils.removeYTSongFromPlaylist
import it.fast4x.rimusic.utils.thumbnail
import it.fast4x.rimusic.utils.updateLocalPlaylist
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay


@KotlinCsvExperimental
@ExperimentalMaterialApi
@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun LocalPlaylistSongs(
    navController: NavController,
    playlistId: Long,
    onDelete: () -> Unit,
) {
    val context = LocalContext.current
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val uiType by rememberPreference(UiTypeKey, UiType.RiMusic)

    var playlistAllSongs by persistList<SongEntity>("localPlaylist/$playlistId/songs")
    var songsInTheToPlaylist by persistList<SongEntity>("")
    var downloadedPlaylistSongs by persistList<SongEntity>("localPlaylist/$playlistId/songs")
    var cachedPlaylistSongs by persistList<SongEntity>("localPlaylist/$playlistId/songs")
    var playlistSongs by persistList<SongEntity>("localPlaylist/$playlistId/songs")
    var playlistSongsSortByPosition by persistList<SongEntity>("localPlaylist/$playlistId/songs")
    var playlistPreview by persist<PlaylistPreview?>("localPlaylist/playlist")
    val thumbnailUrl = remember { mutableStateOf("") }


    var sortBy by rememberPreference(playlistSongSortByKey, PlaylistSongSortBy.Title)
    var sortOrder by rememberPreference(songSortOrderKey, SortOrder.Descending)

    var filter: String? by rememberSaveable { mutableStateOf(null) }

    val disableScrollingText by rememberPreference(disableScrollingTextKey, false)
    var playlistSongsTypeFilter by rememberPreference(playlistSongsTypeFilterKey, PlaylistSongsTypeFilter.All)

    LaunchedEffect(Unit, filter, sortOrder, sortBy) {
        Database.songsPlaylist(playlistId, sortBy, sortOrder).filterNotNull()
            .collect { playlistAllSongs = it }
    }

    Database.asyncTransaction {
        val downloads = MyDownloadHelper.downloads.value
        CoroutineScope(Dispatchers.IO).launch {
            downloadedPlaylistSongs = playlistAllSongs.filter { song -> downloads[song.song.id]?.state == Download.STATE_COMPLETED }
            cachedPlaylistSongs = playlistAllSongs.filter { song -> song.contentLength?.let { binder?.cache?.isCached(song.song.id, 0, song.contentLength) } ?: false
            }
        }
    }

    LaunchedEffect(Unit, playlistAllSongs, filter, playlistSongsTypeFilter) {
        when (playlistSongsTypeFilter) {
            PlaylistSongsTypeFilter.All -> {playlistSongs = playlistAllSongs}
            PlaylistSongsTypeFilter.Local -> {
                playlistSongs = playlistAllSongs.filter { it.asMediaItem.isLocal }
            }

            PlaylistSongsTypeFilter.OnlineSongs -> {
                playlistSongs =
                    playlistAllSongs.filter { it.song.thumbnailUrl?.startsWith("https://lh3.googleusercontent.com") == true }
            }

            PlaylistSongsTypeFilter.Videos -> {
                playlistSongs =
                    playlistAllSongs.filter { it.song.thumbnailUrl?.startsWith("https://i.ytimg.com/") == true }
            }

            PlaylistSongsTypeFilter.Unmatched -> {
                playlistSongs =
                    playlistAllSongs.filter { it.song.thumbnailUrl == "" && !it.asMediaItem.isLocal }
            }

            PlaylistSongsTypeFilter.Favorites -> {
                playlistSongs =
                    playlistAllSongs.filter { it.song.likedAt !in listOf(-1L,null) }
            }

            PlaylistSongsTypeFilter.Explicit -> {
                playlistSongs =
                    playlistAllSongs.filter { it.asMediaItem.isExplicit }
            }

            PlaylistSongsTypeFilter.Downloaded -> {
                playlistSongs = downloadedPlaylistSongs
            }

            PlaylistSongsTypeFilter.Cached -> {
                playlistSongs = cachedPlaylistSongs
            }
        }
    }

    LaunchedEffect(Unit) {
        Database.songsPlaylist(playlistId, PlaylistSongSortBy.Position, SortOrder.Ascending).filterNotNull()
            .collect { playlistSongsSortByPosition = it }
    }

    LaunchedEffect(Unit) {
        Database.singlePlaylistPreview(playlistId).collect { playlistPreview = it }
    }

    LaunchedEffect( playlistPreview?.playlist?.name ) {
        val thumbnailName = "thumbnail/playlist_${playlistId}"
        val presentThumbnailUrl: String? = checkFileExists(context, thumbnailName)
        if (presentThumbnailUrl != null) {
            thumbnailUrl.value = presentThumbnailUrl
        }
    }

    //**** SMART RECOMMENDATION
    val recommendationsNumber by rememberPreference(
        recommendationsNumberKey,
        RecommendationsNumber.`5`
    )
    var isRecommendationEnabled by rememberPreference(isRecommendationEnabledKey, false)
    var relatedSongsRecommendationResult by persist<Result<Innertube.RelatedSongs?>?>(tag = "home/relatedSongsResult")
    var songBaseRecommendation by persist<SongEntity?>("home/songBaseRecommendation")
    var positionsRecommendationList = arrayListOf<Int>()
    var songMatchingDialogEnable by remember { mutableStateOf(false) }
    var matchingSongEntity by remember { mutableStateOf(SongEntity(
        Song(
        id = "",
        title = "",
        durationText = null,
        thumbnailUrl = null
                )
            )
        )
    }

    if (isRecommendationEnabled) {
        LaunchedEffect(Unit, isRecommendationEnabled) {
            Database.songsPlaylist(playlistId, sortBy, sortOrder).distinctUntilChanged()
                .collect { songs ->
                    val song = songs.firstOrNull()
                    if (relatedSongsRecommendationResult == null || songBaseRecommendation?.song?.id != song?.song?.id) {
                        relatedSongsRecommendationResult =
                            Innertube.relatedSongs(NextBody(videoId = (song?.song?.id ?: "HZnNt9nnEhw")))
                    }
                    songBaseRecommendation = song
                }
        }
        //relatedSongsRecommendationResult?.getOrNull()?.songs?.toString()?.let { Log.d("mediaItem", "related  $it") }
        //Log.d("mediaItem","related size "+relatedSongsRecommendationResult?.getOrNull()?.songs?.size.toString())
        //val numRelated = relatedSongsResult?.getOrNull()?.songs?.size ?: 0
        //val relatedMax = playlistSongs.size
        if (relatedSongsRecommendationResult != null) {
            for (index in 0..recommendationsNumber.number) {
                positionsRecommendationList.add((0..playlistSongs.size).random())
            }
        }
        //Log.d("mediaItem","positionsList "+positionsRecommendationList.toString())
        //**** SMART RECOMMENDATION
    }

    var filterCharSequence: CharSequence
    filterCharSequence = filter.toString()

    if (!filter.isNullOrBlank())
        playlistSongs =
            playlistSongs.filter { songItem ->
                songItem.song.title.contains(
                    filterCharSequence,
                    true
                ) ?: false
                        || songItem.song.artistsText?.contains(
                    filterCharSequence,
                    true
                ) ?: false
                        || songItem.albumTitle?.contains(
                    filterCharSequence,
                    true
                ) ?: false
            }

    var searching by rememberSaveable { mutableStateOf(false) }

    var totalPlayTimes = 0L
    playlistSongs.forEach {
        totalPlayTimes += it.song.durationText?.let { it1 ->
            durationTextToMillis(it1)
        }?.toLong() ?: 0
    }


    val thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )

    val sortOrderIconRotation by animateFloatAsState(
        targetValue = if (sortOrder == SortOrder.Ascending) 0f else 180f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing), label = ""
    )

    val lazyListState = rememberLazyListState()

    val reorderingState = rememberReorderingState(
        lazyListState = lazyListState,
        key = playlistSongs,
        onDragEnd = { fromIndex, toIndex ->
            //Log.d("mediaItem","reoder playlist $playlistId, from $fromIndex, to $toIndex")
            Database.asyncTransaction {
                Database.move(playlistId, fromIndex, toIndex)
            }
        },
        extraItemCount = 1
    )


    var isDeleting by rememberSaveable {
        mutableStateOf(false)
    }

    val isPipedEnabled by rememberPreference(isPipedEnabledKey, false)
    val coroutineScope = rememberCoroutineScope()
    val pipedSession = getPipedSession()

    if (songMatchingDialogEnable){

        SongMatchingDialog(
            songToRematch = matchingSongEntity.song,
            playlistId = playlistId,
            position = playlistSongsSortByPosition.indexOf(matchingSongEntity),
            playlist = playlistPreview?.playlist,
            onDismiss = {songMatchingDialogEnable = false}
        )
    }

    if (isDeleting) {
        ConfirmationDialog(
            text = stringResource(R.string.delete_playlist),
            onDismiss = { isDeleting = false },
            onConfirm = {
                CoroutineScope(Dispatchers.IO).launch {
                    if (isYouTubeSyncEnabled() && playlistPreview?.playlist?.isYoutubePlaylist == true) {
                        if (playlistPreview?.playlist?.isEditable == true) {
                            playlistPreview?.playlist?.browseId?.let {YtMusic.deletePlaylist(cleanPrefix(it))
                            }
                        } else {
                            playlistPreview?.playlist?.browseId?.let {YtMusic.removelikePlaylistOrAlbum(cleanPrefix(it))}
                        }
                    }
                    Database.asyncTransaction {
                        playlistPreview?.playlist?.let(Database::delete)
                    }
                }

                if (playlistPreview?.playlist?.name?.startsWith(PIPED_PREFIX) == true && isPipedEnabled && pipedSession.token.isNotEmpty())
                    deletePipedPlaylist(
                        context = context,
                        coroutineScope = coroutineScope,
                        pipedSession = pipedSession.toApiSession(),
                        id = UUID.fromString(cleanPrefix(playlistPreview?.playlist?.browseId ?: ""))
                    )


                //onDelete()
                navController.popBackStack()
            }
        )
    }

    var isRenumbering by rememberSaveable {
        mutableStateOf(false)
    }
    if (isRenumbering) {
        ConfirmationDialog(
            text = stringResource(R.string.do_you_really_want_to_renumbering_positions_in_this_playlist),
            onDismiss = { isRenumbering = false },
            onConfirm = {
                Database.asyncTransaction {
                    playlistSongs.forEachIndexed { index, song ->
                        playlistPreview?.playlist?.let {
                            Database.updateSongPosition(it.id, song.song.id, index)
                        }
                    }
                }

            }
        )
    }
    fun sync() {
        SmartMessage(
            message = context.resources.getString(R.string.syncing),
            durationLong = true,
            context = context,
        )
        playlistPreview?.let { playlistPreview ->
            if (!playlistPreview.playlist.name.startsWith(
                    PIPED_PREFIX,
                    0,
                    true
                )
            ) {
                Database.asyncTransaction {
                    runBlocking(Dispatchers.IO) {
                        withContext(Dispatchers.IO) {
                            playlistPreview.playlist.browseId?.let {
                                YtMusic.getPlaylist(
                                    playlistId = cleanPrefix(it)
                                ).completed()
                            }
                        }
                    }?.getOrNull()?.let { remotePlaylist ->
                        val playlistIdChecked =
                            if (remotePlaylist.playlist.key.startsWith("VL")) remotePlaylist.playlist.key.substringAfter("VL") else remotePlaylist.playlist.key

                        Database.clearPlaylist(playlistId)

                        remotePlaylist.songs
                            .map(Innertube.SongItem::asMediaItem)
                            .onEach(Database::insert)
                            .mapIndexed { position, mediaItem ->
                                SongPlaylistMap(
                                    songId = mediaItem.mediaId,
                                    playlistId = playlistId,
                                    position = position,
                                    setVideoId = mediaItem.mediaMetadata.extras?.getString("setVideoId"),
                                ).default()
                            }.let(Database::insertSongPlaylistMaps)
                            .also { SmartMessage(context.resources.getString(R.string.done), context = context) }
                    }
                }
            } else {
                syncSongsInPipedPlaylist(
                    context = context,
                    coroutineScope = coroutineScope,
                    pipedSession = pipedSession.toApiSession(),
                    idPipedPlaylist = UUID.fromString(
                        cleanPrefix(playlistPreview.playlist.browseId ?: "")
                    ),
                    playlistId = playlistPreview.playlist.id

                )
            }
        }
    }

    val shouldSync = remember(playlistPreview?.playlist?.name) {
        playlistPreview?.playlist?.name?.startsWith(YTP_PREFIX) == true
    } || remember(playlistPreview?.playlist?.browseId) {
        playlistPreview?.playlist?.browseId?.startsWith("RDTMAK5uy") == true
    }

    LaunchedEffect(shouldSync) {
        if (!shouldSync || !isNetworkConnected(context)) {
            return@LaunchedEffect
        }
        coroutineScope.launch {
            sync()
            Database.asyncTransaction {
                updatePlaylistName(cleanPrefix(playlistPreview!!.playlist.name), playlistId)
            }
        }
    }

    var isReorderDisabled by rememberPreference(reorderInQueueEnabledKey, defaultValue = true)

    val playlistThumbnailSizeDp = Dimensions.thumbnails.playlist
    val playlistThumbnailSizePx = playlistThumbnailSizeDp.px

    val thumbnailSizeDp = Dimensions.thumbnails.song
    val thumbnailSizePx = thumbnailSizeDp.px

    val rippleIndication = ripple(bounded = false)

    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }


    val uriHandler = LocalUriHandler.current

    var showConfirmDeleteDownloadDialog by remember {
        mutableStateOf(false)
    }

    var showConfirmDownloadAllDialog by remember {
        mutableStateOf(false)
    }

    var showConfirmMatchAllDialog by remember {
        mutableStateOf(false)
    }

    var showYoutubeLikeConfirmDialog by remember {
        mutableStateOf(false)
    }

    var totalMinutesToLike by remember { mutableStateOf("") }

    var songItemsToLike = remember { mutableStateListOf<MediaItem>() }

    var scrollToNowPlaying by remember {
        mutableStateOf(false)
    }

    var nowPlayingItem by remember {
        mutableStateOf(-1)
    }

    /*
    var showSortTypeSelectDialog by remember {
        mutableStateOf(false)
    }
     */
    /*
        var showAddPlaylistSelectDialog by remember {
            mutableStateOf(false)
        }
        var isCreatingNewPlaylist by rememberSaveable {
            mutableStateOf(false)
        }
        var showPlaylistSelectDialog by remember {
            mutableStateOf(false)
        }
        */
    var listMediaItems = remember {
        mutableListOf<MediaItem>()
    }

    var selectItems by remember {
        mutableStateOf(false)
    }

    var plistId by remember {
        mutableStateOf(0L)
    }
    var plistName by remember {
        mutableStateOf(playlistPreview?.playlist?.name)
    }
    /*
    val playlistPreviews by remember {
        Database.playlistPreviews(PlaylistSortBy.Name, SortOrder.Ascending)
    }.collectAsState(initial = emptyList(), context = Dispatchers.IO)
     */

    var position by remember {
        mutableIntStateOf(0)
    }

    val exportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            coroutineScope.launch (Dispatchers.IO){
                context.applicationContext.contentResolver.openOutputStream(uri)
                    ?.use { outputStream ->
                        csvWriter().open(outputStream) {
                            writeRow(
                                "PlaylistBrowseId",
                                "PlaylistName",
                                "MediaId",
                                "Title",
                                "Artists",
                                "Duration",
                                "ThumbnailUrl",
                                "AlbumId",
                                "AlbumTitle",
                                "ArtistIds"
                            )
                            if (listMediaItems.isEmpty()) {
                                playlistSongs.forEach {
                                    val artistInfos = Database.songArtistInfo(it.asMediaItem.mediaId)
                                    val albumInfo = Database.songAlbumInfo(it.asMediaItem.mediaId)
                                    writeRow(
                                        playlistPreview?.playlist?.browseId,
                                        plistName,
                                        it.song.id,
                                        it.song.title,
                                        artistInfos.joinToString(",") { it.name ?: "" },
                                        it.song.durationText,
                                        it.song.thumbnailUrl,
                                        albumInfo?.id,
                                        albumInfo?.name,
                                        artistInfos.joinToString(",") { it.id }
                                    )
                                }
                            } else {
                                listMediaItems.forEach {
                                    val artistInfos = Database.songArtistInfo(it.mediaId)
                                    val albumInfo = Database.songAlbumInfo(it.mediaId)
                                    writeRow(
                                        playlistPreview?.playlist?.browseId,
                                        plistName,
                                        it.mediaId,
                                        it.mediaMetadata.title,
                                        artistInfos.joinToString(",") { it.name ?: "" },
                                        it.asSong.durationText,
                                        it.mediaMetadata.artworkUri,
                                        albumInfo?.id,
                                        albumInfo?.name,
                                        artistInfos.joinToString(",") { it.id }
                                    )
                                }
                            }
                        }
                    }
                }
        }

    val importLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            context.applicationContext.contentResolver.openInputStream(uri)
                ?.use { inputStream ->
                    csvReader().open(inputStream) {
                        readAllWithHeaderAsSequence().forEachIndexed { index, row: Map<String, String> ->

                            Database.asyncTransaction {
                                plistId = row["PlaylistName"]?.let {
                                    Database.playlistExistByName(
                                        it
                                    )
                                } ?: 0L

                                if (plistId == 0L) {
                                    plistId = row["PlaylistName"]?.let {
                                        Database.insert(
                                            Playlist(
                                                name = it,
                                                browseId = row["PlaylistBrowseId"]
                                            )
                                        )
                                    }!!
                                } else {
                                    /**/
                                    if (row["MediaId"] != null && row["Title"] != null) {
                                        val song =
                                            row["MediaId"]?.let {
                                                row["Title"]?.let { it1 ->
                                                    Song(
                                                        id = it,
                                                        title = it1,
                                                        artistsText = row["Artists"],
                                                        durationText = row["Duration"],
                                                        thumbnailUrl = row["ThumbnailUrl"]
                                                    )
                                                }
                                            }
                                        Database.asyncTransaction {
                                            if (song != null) {
                                                Database.insert(song)
                                                Database.insert(
                                                    SongPlaylistMap(
                                                        songId = song.id,
                                                        playlistId = plistId,
                                                        position = index
                                                    ).default()
                                                )
                                            }
                                        }


                                    }
                                    /**/
                                }
                            }

                        }
                    }

                }
        }

    var isRenaming by rememberSaveable {
        mutableStateOf(false)
    }
    var isExporting by rememberSaveable {
        mutableStateOf(false)
    }

    if (isRenaming || isExporting) {
        InputTextDialog(
            onDismiss = {
                isRenaming = false
                isExporting = false
            },
            title = stringResource(R.string.enter_the_playlist_name),
            value = playlistPreview?.playlist?.name?.let { cleanPrefix(it) } ?: "",
            placeholder = stringResource(R.string.enter_the_playlist_name),
            setValue = { text ->
                if (isRenaming) {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (isYouTubeSyncEnabled() && (playlistPreview?.playlist?.isEditable == true)) {
                            println("Innertube YtMusic try to rename Playlist with browseId: ${playlistPreview?.playlist?.browseId}, name: $text")
                            playlistPreview?.playlist?.browseId?.let {
                                println("Innertube YtMusic renamePlaylist with id: $it, name: $text")
                                YtMusic.renamePlaylist(cleanPrefix(it), text)
                            }
                        }
                        Database.asyncTransaction {
                            playlistPreview?.playlist?.copy(name = text)?.let(Database::update)
                        }
                    }


                    if (playlistPreview?.playlist?.name?.startsWith(PIPED_PREFIX) == true && isPipedEnabled && pipedSession.token.isNotEmpty())
                        renamePipedPlaylist(
                            context = context,
                            coroutineScope = coroutineScope,
                            pipedSession = pipedSession.toApiSession(),
                            id = UUID.fromString(cleanPrefix(playlistPreview?.playlist?.browseId ?: "")),
                            name = text
                        )

                }
                if (isExporting) {
                    plistName = text
                    try {
                        @SuppressLint("SimpleDateFormat")
                        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")
                        exportLauncher.launch("RMPlaylist_${text.take(20)}_${dateFormat.format(Date())}")
                    } catch (e: ActivityNotFoundException) {
                        SmartMessage(
                            context.resources.getString(R.string.info_not_find_app_create_doc),
                            type = PopupType.Warning, context = context
                        )
                    }
                }

            }
        )
    }

    val navigationBarPosition by rememberPreference(
        navigationBarPositionKey,
        NavigationBarPosition.Bottom
    )
    val maxSongsInQueue by rememberPreference(maxSongsInQueueKey, MaxSongs.`500`)

    val playlistNotMonthlyType =
        playlistPreview?.playlist?.name?.startsWith(MONTHLY_PREFIX, 0, true) == false
    val playlistNotPipedType =
        playlistPreview?.playlist?.name?.startsWith(PIPED_PREFIX, 0, true) == false
    val hapticFeedback = LocalHapticFeedback.current
    val unmatchedSongsCount = playlistSongs.filter { it.song.thumbnailUrl == "" }.size

    val editThumbnailLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            val thumbnailName = "playlist_${playlistPreview?.playlist?.id}"
            val permaUri = saveImageToInternalStorage(context, uri, "thumbnail", thumbnailName)
            thumbnailUrl.value = permaUri.toString()
        } else {
            SmartMessage(context.resources.getString(R.string.thumbnail_not_selected), context = context)
        }
    }
    fun openEditThumbnailPicker() {
        editThumbnailLauncher.launch("image/*")
    }

    fun resetThumbnail() {
        if(thumbnailUrl.value == ""){
            SmartMessage(context.resources.getString(R.string.no_thumbnail_present), context = context)
            return
        }
        val thumbnailName = "thumbnail/playlist_${playlistPreview?.playlist?.id}"
        val retVal = deleteFileIfExists(context, thumbnailName)
        if(retVal == true){
            SmartMessage(context.resources.getString(R.string.removed_thumbnail), context = context)
            thumbnailUrl.value = ""
        } else {
            SmartMessage(context.resources.getString(R.string.failed_to_remove_thumbnail), context = context)
        }
    }

    var getAlbumVersion by remember { mutableStateOf(false) }
    var showGetAlbumVersionDialogue by remember { mutableStateOf(false) }
    var showGetAlbumVersionDialogueExt by remember { mutableStateOf(false) }
    var totalSongsToMatch by remember { mutableIntStateOf(0) }
    var songsMatched by remember { mutableIntStateOf(0) }

    if (showGetAlbumVersionDialogue){
        InProgressDialog(
            total = totalSongsToMatch,
            done = songsMatched,
            text = stringResource(R.string.matching_songs)
        )
    }

    if (showGetAlbumVersionDialogueExt){
        InProgressDialog(
            total = totalSongsToMatch,
            done = songsMatched,
            text = stringResource(R.string.matching_songs),
            onDismiss = {showGetAlbumVersionDialogueExt = false}
        )
    }

    if (showYoutubeLikeConfirmDialog) {
        songItemsToLike.clear()
        if (listMediaItems.isEmpty()) {
            playlistSongs.forEachIndexed { index, song ->
                if (song.song.likedAt in listOf(-1L,null)) {
                    songItemsToLike.add(song.asMediaItem)
                }
            }
        } else {
            Database.asyncTransaction {
                listMediaItems.forEachIndexed { index, song ->
                    if (Database.getLikedAt(song.mediaId) in listOf(-1L,null)) {
                        songItemsToLike.add(song)
                    }
                }
            }
        }
        totalMinutesToLike = formatAsDuration(((songItemsToLike).size*1000).toLong())
        ConfirmationDialog(
            text = "$totalMinutesToLike "+stringResource(R.string.do_you_really_want_to_like_all),
            onDismiss = { showYoutubeLikeConfirmDialog = false },
            onConfirm = {
                showYoutubeLikeConfirmDialog = false
                CoroutineScope(Dispatchers.IO).launch {
                    addToYtLikedSongs(songItemsToLike)
                }
            }
        )
    }


    if (playlistSongsSortByPosition.any{songEntity -> songEntity.song.id == (cleanPrefix(songEntity.song.title)+songEntity.song.artistsText).filter{it.isLetterOrDigit()}}){
        showGetAlbumVersionDialogueExt = true
            LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                totalSongsToMatch = playlistSongsSortByPosition
                    .filter{songEntity -> songEntity.song.id == (cleanPrefix(songEntity.song.title)+songEntity.song.artistsText).filter{it.isLetterOrDigit()}}.size
                songsMatched = 0

                val jobs = mutableListOf<Job>()
                playlistSongsSortByPosition.forEachIndexed { index, video ->
                    if (video.song.id == (cleanPrefix(video.song.title)+video.song.artistsText).filter{it.isLetterOrDigit()}){
                        jobs.add(coroutineScope.launch(Dispatchers.IO) {
                            getAlbumVersionFromVideo(
                                song = video.song,
                                playlistId = playlistId,
                                position = index,
                                playlist = playlistPreview?.playlist
                            )
                          }
                        )
                    }
                }
                while(jobs.isNotEmpty()){
                    val oldSize = jobs.size
                    jobs.removeIf{it.isCompleted}
                    songsMatched += oldSize - jobs.size
                    delay(10)
                }
                showGetAlbumVersionDialogueExt = false
                getAlbumVersion = false
            }
        }
    }

    LaunchedEffect(getAlbumVersion) {
        withContext(Dispatchers.IO) {
            totalSongsToMatch = playlistSongsSortByPosition
                .filter {(it.song.thumbnailUrl?.startsWith("https://lh3.googleusercontent.com") == false) && !(it.song.id.startsWith(LOCAL_KEY_PREFIX))}.size
            songsMatched = 0

            val jobs = mutableListOf<Job>()
            playlistSongsSortByPosition.forEachIndexed { index, video ->
                if ((video.song.thumbnailUrl?.startsWith("https://lh3.googleusercontent.com") == false) && !(video.song.id.startsWith(LOCAL_KEY_PREFIX))) {
                    jobs.add(coroutineScope.launch(Dispatchers.IO) {
                        getAlbumVersionFromVideo(
                            song = video.song,
                            playlistId = playlistId,
                            position = index,
                            playlist = playlistPreview?.playlist
                        )
                      }
                    )
                }
            }
            while(jobs.isNotEmpty()){
                val oldSize = jobs.size
                jobs.removeIf{it.isCompleted}
                songsMatched += oldSize - jobs.size
                delay(10)
            }

            showGetAlbumVersionDialogue = false
            getAlbumVersion = false
        }
    }

    var playlistUpdateDialog by remember { mutableStateOf(false) }
    var songsUpdated by remember { mutableIntStateOf(0) }
    var totalSongsToUpdate by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit,playlistUpdateDialog){
        Database.asyncTransaction {
            totalSongsToUpdate = playlistAllSongs.filter { it.song.thumbnailUrl?.startsWith("https://lh3.googleusercontent.com/") == true && ((songAlbumInfo(it.asMediaItem.mediaId)?.id == null) || songArtistInfo(it.asMediaItem.mediaId).isEmpty()) }.size
        }
    }

    if (playlistUpdateDialog){
        InProgressDialog(
            total = totalSongsToUpdate,
            done = songsUpdated,
            text = stringResource(R.string.updating_playlist)
        )
    }

    LaunchedEffect(playlistUpdateDialog) {
        withContext(Dispatchers.IO) {
            songsUpdated = 0
            val jobs = mutableListOf<Job>()
            playlistAllSongs.filter { it.song.thumbnailUrl?.startsWith("https://lh3.googleusercontent.com/") == true && ((songAlbumInfo(it.asMediaItem.mediaId)?.id == null) || songArtistInfo(it.asMediaItem.mediaId).isEmpty()) }.forEach { song ->
                jobs.add(coroutineScope.launch(Dispatchers.IO) {
                    updateLocalPlaylist(song.song)
                }
                )
            }
            while(jobs.isNotEmpty()){
                val oldSize = jobs.size
                jobs.removeIf{it.isCompleted}
                songsUpdated += oldSize - jobs.size
                delay(10)
            }
            playlistUpdateDialog = false
        }
    }


    println("LocalPlaylistSongs playlist browseId ${playlistPreview?.playlist?.browseId}")

    Box(
        modifier = Modifier
            .background(colorPalette.background0)
            //.fillMaxSize()
            .fillMaxHeight()
            .fillMaxWidth(
                if (navigationBarPosition == NavigationBarPosition.Left ||
                    navigationBarPosition == NavigationBarPosition.Top ||
                    navigationBarPosition == NavigationBarPosition.Bottom
                ) 1f
                else Dimensions.contentWidthRightBar
            )
    ) {
        LazyColumn(
            state = reorderingState.lazyListState,
            //contentPadding = LocalPlayerAwareWindowInsets.current
            //    .only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
            modifier = Modifier
                .background(colorPalette.background0)
                .fillMaxSize()
        ) {
            item(
                key = "header",
                contentType = 0
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {

                    HeaderWithIcon(
                        title = playlistPreview?.playlist?.name?.let { name ->
                            cleanPrefix(name)
                        } ?: "Unknown",
                        iconId = R.drawable.playlist,
                        enabled = true,
                        showIcon = false,
                        modifier = Modifier
                            .padding(bottom = 8.dp),
                        onClick = {}
                    )

                }

                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        //.background(colorPalette.background4)
                        .fillMaxSize(0.99F)
                        .background(
                            color = colorPalette.background1,
                            shape = thumbnailRoundness.shape()
                        )
                ) {

                    playlistPreview?.let {
                        Playlist(
                            playlist = it,
                            thumbnailSizeDp = playlistThumbnailSizeDp,
                            thumbnailSizePx = playlistThumbnailSizePx,
                            alternative = true,
                            showName = false,
                            modifier = Modifier
                                .padding(top = 14.dp),
                            disableScrollingText = disableScrollingText,
                            thumbnailUrl = if (thumbnailUrl.value == "") null else thumbnailUrl.value
                        )
                    }


                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            //.fillMaxHeight()
                            .padding(end = 10.dp)
                            .fillMaxWidth(if (isLandscape) 0.90f else 0.80f)
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))
                        IconInfo(
                            title = playlistSongs.size.toString(),
                            icon = painterResource(R.drawable.musical_notes)
                        )
                        if (unmatchedSongsCount > 0) {
                            Spacer(modifier = Modifier.height(5.dp))
                            IconInfo(
                                title = "($unmatchedSongsCount)",
                                icon = painterResource(R.drawable.alert)
                            )
                        }
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

                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HeaderIconButton(
                            icon = R.drawable.smart_shuffle,
                            enabled = true,
                            color = if (isRecommendationEnabled) colorPalette.text else colorPalette.textDisabled,
                            onClick = {},
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {
                                        isRecommendationEnabled = !isRecommendationEnabled
                                    },
                                    onLongClick = {
                                        SmartMessage(context.resources.getString(R.string.info_smart_recommendation), context = context)
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        HeaderIconButton(
                            icon = R.drawable.shuffle,
                            enabled = playlistSongs.any { it.song.thumbnailUrl != "" && it.song.likedAt != -1L },
                            color = if (playlistSongs.any { it.song.thumbnailUrl != "" && it.song.likedAt != -1L }) colorPalette.text else colorPalette.textDisabled,
                            onClick = {},
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {
                                        if (playlistSongs.any { it.song.thumbnailUrl != "" && it.song.likedAt != -1L }) {
                                            playlistSongs.filter { it.song.thumbnailUrl != "" && it.song.likedAt != -1L }
                                                .let { songs ->
                                                    if (songs.isNotEmpty()) {
                                                        val itemsLimited =
                                                            if (songs.size > maxSongsInQueue.number) songs.shuffled()
                                                                .take(maxSongsInQueue.number.toInt()) else songs
                                                        binder?.stopRadio()
                                                        binder?.player?.forcePlayFromBeginning(
                                                            itemsLimited.shuffled()
                                                                .map(SongEntity::asMediaItem)
                                                        )
                                                    }
                                                }
                                        } else {
                                            SmartMessage(context.resources.getString(R.string.disliked_this_collection),type = PopupType.Error, context = context)
                                        }
                                    },
                                    onLongClick = {
                                        SmartMessage(context.resources.getString(R.string.info_shuffle), context = context)
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        HeaderIconButton(
                            modifier = Modifier.padding(horizontal = 5.dp),
                            onClick = { searching = !searching },
                            icon = R.drawable.search_circle,
                            color = colorPalette.text,
                            iconSize = 24.dp
                        )
                    }


                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth()
                ) {

                    HeaderIconButton(
                        icon = R.drawable.pin,
                        enabled = playlistSongs.isNotEmpty(),
                        color = if (playlistPreview?.playlist?.name?.startsWith(PINNED_PREFIX,0,true) == true)
                            colorPalette.text else colorPalette.textDisabled,
                        onClick = {},
                        modifier = Modifier
                            .combinedClickable(
                                onClick = {
                                    Database.asyncTransaction {
                                        if (playlistPreview?.playlist?.name?.startsWith(PINNED_PREFIX,0,true) == true)
                                            Database.unPinPlaylist(playlistId) else
                                            Database.pinPlaylist(playlistId)
                                    }
                                },
                                onLongClick = {
                                    SmartMessage(context.resources.getString(R.string.info_pin_unpin_playlist), context = context)
                                }
                            )
                    )

                    if (sortBy == PlaylistSongSortBy.Position && sortOrder == SortOrder.Ascending)
                        HeaderIconButton(
                            icon = if (isReorderDisabled) R.drawable.locked else R.drawable.unlocked,
                            enabled = playlistSongs.isNotEmpty() == true,
                            color = if (playlistSongs.isNotEmpty() == true) colorPalette.text else colorPalette.textDisabled,
                            onClick = {},
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {
                                        if (sortBy == PlaylistSongSortBy.Position && sortOrder == SortOrder.Ascending) {
                                            isReorderDisabled = !isReorderDisabled
                                        } else {
                                            SmartMessage(
                                                context.resources.getString(R.string.info_reorder_is_possible_only_in_ascending_sort),
                                                type = PopupType.Warning, context = context
                                            )
                                        }
                                    },
                                    onLongClick = {
                                        SmartMessage(context.resources.getString(R.string.info_lock_unlock_reorder_songs), context = context)
                                    }
                                )
                        )

                    HeaderIconButton(
                        icon = R.drawable.downloaded,
                        enabled = playlistSongs.any { it.song.likedAt != -1L },
                        color = if (playlistSongs.any { it.song.likedAt != -1L }) colorPalette.text else colorPalette.textDisabled,
                        onClick = {},
                        modifier = Modifier
                            .combinedClickable(
                                onClick = {
                                    if (playlistSongs.any { it.song.likedAt != -1L }) {
                                        showConfirmDownloadAllDialog = true
                                    } else {
                                        SmartMessage(context.resources.getString(R.string.disliked_this_collection),type = PopupType.Error, context = context)
                                    }
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
                                isRecommendationEnabled = false
                                downloadState = Download.STATE_DOWNLOADING
                                if (listMediaItems.isEmpty()) {
                                    if (playlistSongs.any { it.song.likedAt != -1L }) {
                                        playlistSongs.filter { it.song.likedAt != -1L }.forEach {
                                            binder?.cache?.removeResource(it.asMediaItem.mediaId)
                                            Database.asyncTransaction {
                                                Database.insert(
                                                    Song(
                                                        id = it.asMediaItem.mediaId,
                                                        title = it.asMediaItem.mediaMetadata.title.toString(),
                                                        artistsText = it.asMediaItem.mediaMetadata.artist.toString(),
                                                        thumbnailUrl = it.song.thumbnailUrl,
                                                        durationText = null
                                                    )
                                                )
                                            }
                                            manageDownload(
                                                context = context,
                                                mediaItem = it.asMediaItem,
                                                downloadState = false
                                            )
                                        }
                                    }
                                } else {
                                    listMediaItems.forEach {
                                        binder?.cache?.removeResource(it.mediaId)
                                        manageDownload(
                                            context = context,
                                            mediaItem = it,
                                            downloadState = true
                                        )
                                    }
                                    selectItems = false
                                }
                            }
                        )
                    }

                    if (showConfirmMatchAllDialog) {
                        ConfirmationDialog(
                            text = stringResource(R.string.do_you_really_want_to_match_all),
                            onDismiss = { showConfirmMatchAllDialog = false },
                            onConfirm = {
                                getAlbumVersion = true
                                showGetAlbumVersionDialogue = true
                                showConfirmMatchAllDialog = false
                            }
                        )
                    }

                    HeaderIconButton(
                        icon = R.drawable.download,
                        enabled = playlistSongs.isNotEmpty(),
                        color = if (playlistSongs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                        onClick = {},
                        modifier = Modifier
                            .combinedClickable(
                                onClick = {
                                    showConfirmDeleteDownloadDialog = true
                                },
                                onLongClick = {
                                    SmartMessage(context.resources.getString(R.string.info_remove_all_downloaded_songs), context = context)
                                }
                            )
                    )



                    if ((playlistPreview?.playlist?.isYoutubePlaylist) == false) {
                        HeaderIconButton(
                            icon = R.drawable.random,
                            enabled = playlistSongs.any {
                                (it.song.thumbnailUrl?.startsWith("https://lh3.googleusercontent.com") == false) && !(it.song.id.startsWith(
                                    LOCAL_KEY_PREFIX
                                ))
                            },
                            color = if (playlistSongs.any {
                                    (it.song.thumbnailUrl?.startsWith("https://lh3.googleusercontent.com") == false) && !(it.song.id.startsWith(
                                        LOCAL_KEY_PREFIX
                                    ))
                                }) colorPalette.text else colorPalette.textDisabled,
                            onClick = {},
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {
                                        if (!isNetworkConnected(context) && playlistPreview?.playlist?.isYoutubePlaylist == true && (playlistPreview?.playlist?.isEditable == true) && isYouTubeSyncEnabled()){
                                            SmartMessage(context.resources.getString(R.string.no_connection), context = context, type = PopupType.Error)
                                        } else if (playlistSongs.any {
                                                (it.song.thumbnailUrl?.startsWith("https://lh3.googleusercontent.com") == false) && !(it.song.id.startsWith(
                                                    LOCAL_KEY_PREFIX
                                                ))
                                            }) {
                                            showConfirmMatchAllDialog = true
                                        } else {
                                            SmartMessage(
                                                context.resources.getString(R.string.no_videos_found),
                                                context = context
                                            )
                                        }
                                    },
                                    onLongClick = {
                                        SmartMessage(
                                            context.resources.getString(R.string.get_album_version),
                                            context = context
                                        )
                                    }
                                )
                        )
                    }

                    if (showConfirmDeleteDownloadDialog) {
                        ConfirmationDialog(
                            text = stringResource(R.string.do_you_really_want_to_delete_download),
                            onDismiss = { showConfirmDeleteDownloadDialog = false },
                            onConfirm = {
                                showConfirmDeleteDownloadDialog = false
                                downloadState = Download.STATE_DOWNLOADING
                                if (listMediaItems.isEmpty()) {
                                    if (playlistSongs.isNotEmpty() == true)
                                        playlistSongs.forEach {
                                            binder?.cache?.removeResource(it.asMediaItem.mediaId)
                                            manageDownload(
                                                context = context,
                                                mediaItem = it.asMediaItem,
                                                downloadState = true
                                            )
                                        }
                                } else {
                                    listMediaItems.forEach {
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

                    /*
                    HeaderIconButton(
                        icon = R.drawable.enqueue,
                        enabled = playlistSongs.isNotEmpty(),
                        color = if (playlistSongs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                        onClick = {
                            playlistSongs
                                .map(Song::asMediaItem)
                                .let { mediaItems ->
                                    binder?.player?.enqueue(mediaItems)
                                }
                        }
                    )
                     */

                    /*
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
                        enabled = playlistSongs.isNotEmpty() == true,
                        color = if (playlistSongs.isNotEmpty() == true) colorPalette.text else colorPalette.textDisabled,
                        onClick = {
                            playlistSongs.let { songs ->
                                if (songs.isNotEmpty()) {
                                    val itemsLimited = if (songs.size > maxSongsInQueue.number)  songs.shuffled().take(maxSongsInQueue.number.toInt()) else songs
                                    binder?.stopRadio()
                                    binder?.player?.forcePlayFromBeginning(
                                        itemsLimited.shuffled().map(Song::asMediaItem)
                                    )
                                }
                            }
                        }
                    )
                    */

                    HeaderIconButton(
                        icon = R.drawable.update,
                        color = colorPalette.text,
                        onClick = {},
                        modifier = Modifier
                            .combinedClickable(
                                onClick = {playlistUpdateDialog = true},
                                onLongClick = {
                                    SmartMessage(
                                        context.resources.getString(R.string.updating_playlist_message),
                                        context = context
                                    )
                                }
                            )
                    )


                    HeaderIconButton(
                        icon = R.drawable.ellipsis_horizontal,
                        color = colorPalette.text, //if (playlistWithSongs?.songs?.isNotEmpty() == true) colorPalette.text else colorPalette.textDisabled,
                        enabled = true, //playlistWithSongs?.songs?.isNotEmpty() == true,
                        modifier = Modifier
                            .padding(end = 4.dp),
                        onClick = {
                            menuState.display {
                                playlistPreview?.let { playlistPreview ->
                                    PlaylistsItemMenu(
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
                                        playlist = playlistPreview,
                                        onEnqueue = {
                                            if (listMediaItems.isEmpty()) {
                                                if (playlistSongs.any { it.song.thumbnailUrl != "" && it.song.likedAt != -1L }) {
                                                    binder?.player?.enqueue(playlistSongs.filter { it.song.thumbnailUrl != "" && it.song.likedAt != -1L }.map(SongEntity::asMediaItem),context)
                                                } else {
                                                    SmartMessage(context.resources.getString(R.string.disliked_this_collection),type = PopupType.Error, context = context)
                                                }
                                            } else {
                                                binder?.player?.enqueue(listMediaItems, context)
                                                listMediaItems.clear()
                                                selectItems = false
                                            }
                                        },
                                        onPlayNext = {
                                            if (listMediaItems.isEmpty()) {
                                                if (playlistSongs.any { it.song.thumbnailUrl != "" && it.song.likedAt != -1L }) {
                                                    binder?.player?.addNext(playlistSongs.filter { it.song.thumbnailUrl != "" && it.song.likedAt != -1L }.map(SongEntity::asMediaItem),context)
                                                } else {
                                                    SmartMessage(context.resources.getString(R.string.disliked_this_collection),type = PopupType.Error, context = context)
                                                }
                                            } else {
                                                binder?.player?.addNext(listMediaItems, context)
                                                listMediaItems.clear()
                                                selectItems = false
                                            }
                                        },
                                        showOnSyncronize = !playlistPreview.playlist.browseId.isNullOrBlank(),
                                        showLinkUnlink = isNetworkConnected(context) && (!playlistPreview.playlist.browseId.isNullOrBlank()),
                                        /*
                                        onSyncronize = {
                                            if (!playlistPreview.playlist.name.startsWith(
                                                    PIPED_PREFIX,
                                                    0,
                                                    true
                                                )
                                            ) {
                                                transaction {
                                                    runBlocking(Dispatchers.IO) {
                                                        withContext(Dispatchers.IO) {
                                                            Innertube.playlistPage(
                                                                BrowseBody(
                                                                    browseId = playlistPreview.playlist.browseId
                                                                        ?: ""
                                                                )
                                                            )
                                                                ?.completed()
                                                        }
                                                    }?.getOrNull()?.let { remotePlaylist ->
                                                        Database.clearPlaylist(playlistId)

                                                        remotePlaylist.songsPage
                                                            ?.items
                                                            ?.map(Innertube.SongItem::asMediaItem)
                                                            ?.onEach(Database::insert)
                                                            ?.mapIndexed { position, mediaItem ->
                                                                SongPlaylistMap(
                                                                    songId = mediaItem.mediaId,
                                                                    playlistId = playlistId,
                                                                    position = position
                                                                )
                                                            }?.let(Database::insertSongPlaylistMaps)
                                                    }
                                                }
                                                //SmartToast(context.resources.getString(R.string.done))
                                                SmartMessage(context.resources.getString(R.string.done), context = context)
                                            } else {
                                                syncSongsInPipedPlaylist(
                                                    context = context,
                                                    coroutineScope = coroutineScope,
                                                    pipedSession = pipedSession.toApiSession(),
                                                    idPipedPlaylist = UUID.fromString(
                                                        playlistPreview.playlist.browseId
                                                    ),
                                                    playlistId = playlistPreview.playlist.id

                                                )
                                                //SmartToast(context.resources.getString(R.string.done))
                                                SmartMessage(context.resources.getString(R.string.done), context = context)
                                            }
                                        },
                                        */
                                        onSyncronize = {sync()},
                                        onLinkUnlink = {
                                            if (!isNetworkConnected(context) && playlistPreview.playlist.isYoutubePlaylist && playlistPreview.playlist.isEditable && isYouTubeSyncEnabled()){
                                                SmartMessage(context.resources.getString(R.string.no_connection), context = context, type = PopupType.Error)
                                            } else if (playlistPreview.playlist.isYoutubePlaylist){
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    if (playlistPreview.playlist.isEditable) {
                                                        playlistPreview.playlist.browseId.let {YtMusic.deletePlaylist(it ?: "")}
                                                    } else {
                                                        playlistPreview.playlist.browseId.let {YtMusic.removelikePlaylistOrAlbum(it ?: "")}
                                                    }
                                                    Database.update(
                                                        playlistPreview.playlist.copy(
                                                            browseId = null,
                                                            isYoutubePlaylist = false,
                                                            isEditable = false
                                                        )
                                                    )
                                                }
                                            } else {
                                                CoroutineScope(Dispatchers.IO).launch {
                                                Database.update(
                                                    playlistPreview.playlist.copy(
                                                        browseId = null
                                                    )
                                                )
                                                }
                                            }
                                        },
                                        onRename = {
                                            if (!isNetworkConnected(context) && playlistPreview.playlist.isYoutubePlaylist && (playlistPreview.playlist.isEditable) && isYouTubeSyncEnabled()){
                                                SmartMessage(context.resources.getString(R.string.no_connection), context = context, type = PopupType.Error)
                                            } else if (playlistPreview.playlist.isEditable && playlistNotMonthlyType){
                                                isRenaming = true
                                            }
                                            else SmartMessage(context.resources.getString(R.string.info_cannot_rename_a_monthly_or_piped_playlist), context = context)
                                        },
                                        onAddToPlaylist = { toPlaylistPreview ->
                                            position = toPlaylistPreview.songCount.minus(1)
                                            //Log.d("mediaItem", " maxPos in Playlist $it ${position}")
                                            if (position > 0) position++ else position = 0
                                            //Log.d("mediaItem", "next initial pos ${position}")
                                            if (listMediaItems.isEmpty()) {
                                                val filteredPLSongs = playlistSongs.filterNot {it.asMediaItem.mediaId.startsWith(LOCAL_KEY_PREFIX) || it.song.thumbnailUrl == ""}
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    songsInTheToPlaylist = withContext(Dispatchers.IO) { Database.sortSongsPlaylistByPositionNoFlow(toPlaylistPreview.playlist.id) }
                                                    var distinctSongs = filteredPLSongs.filterNot { it in songsInTheToPlaylist }

                                                    if ((distinctSongs.size + toPlaylistPreview.songCount) > 5000 && toPlaylistPreview.playlist.isYoutubePlaylist && isYouTubeSyncEnabled()){
                                                        SmartMessage(context.resources.getString(R.string.yt_playlist_limited), context = context, type = PopupType.Error)
                                                    } else if (!isYouTubeSyncEnabled() || !toPlaylistPreview.playlist.isYoutubePlaylist) {
                                                        playlistSongs.forEachIndexed { index, song ->
                                                            Database.asyncTransaction {
                                                                Database.insert(song.asMediaItem)
                                                                Database.insert(
                                                                    SongPlaylistMap(
                                                                        songId = song.asMediaItem.mediaId,
                                                                        playlistId = toPlaylistPreview.playlist.id,
                                                                        position = position + index
                                                                    ).default()
                                                                )
                                                            }
                                                        }
                                                    } else {
                                                        CoroutineScope(Dispatchers.IO).launch {
                                                            if (playlistPreview.playlist.isYoutubePlaylist) {
                                                                YtMusic.addPlaylistToPlaylist(
                                                                    cleanPrefix(toPlaylistPreview.playlist.browseId ?: ""),
                                                                    cleanPrefix(playlistPreview.playlist.browseId ?: "")
                                                                ).onSuccess {
                                                                    playlistSongs.forEachIndexed { index, song ->
                                                                        Database.asyncTransaction {
                                                                            Database.insert(song.asMediaItem)
                                                                            Database.insert(
                                                                                SongPlaylistMap(
                                                                                    songId = song.asMediaItem.mediaId,
                                                                                    playlistId = toPlaylistPreview.playlist.id,
                                                                                    position = position + index
                                                                                ).default()
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                            } else if (distinctSongs.isNotEmpty()) {
                                                                addToYtPlaylist(toPlaylistPreview.playlist.id,
                                                                    position,
                                                                    toPlaylistPreview.playlist.browseId ?: "",
                                                                    distinctSongs.map { it.asMediaItem })
                                                            }
                                                        }
                                                    }
                                                    if (toPlaylistPreview.playlist.name.startsWith(
                                                            PIPED_PREFIX
                                                        ) && isPipedEnabled && pipedSession.token.isNotEmpty()
                                                    )
                                                        addToPipedPlaylist(
                                                            context = context,
                                                            coroutineScope = coroutineScope,
                                                            pipedSession = pipedSession.toApiSession(),
                                                            id = UUID.fromString(cleanPrefix(toPlaylistPreview.playlist.browseId ?: "")),
                                                            videos = playlistSongs.map { it.asMediaItem.mediaId }
                                                                .toList()
                                                        )
                                                }
                                            }
                                            else {
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    val filteredListMediaItems = listMediaItems.filterNot {it.mediaId.startsWith(LOCAL_KEY_PREFIX) || it.mediaMetadata.artworkUri.toString() == ""}
                                                    songsInTheToPlaylist = withContext(Dispatchers.IO){ Database.sortSongsPlaylistByPositionNoFlow(toPlaylistPreview.playlist.id) }

                                                    val distinctSongs = filteredListMediaItems.filter { item -> item !in songsInTheToPlaylist.map { it.asMediaItem } }
                                                    if ((distinctSongs.size + toPlaylistPreview.songCount) > 5000 && toPlaylistPreview.playlist.isYoutubePlaylist && isYouTubeSyncEnabled()){
                                                        SmartMessage(context.resources.getString(R.string.yt_playlist_limited), context = context, type = PopupType.Error)
                                                    } else if (!isYouTubeSyncEnabled() || !toPlaylistPreview.playlist.isYoutubePlaylist) {
                                                        listMediaItems.forEachIndexed { index, song ->
                                                            Database.asyncTransaction {
                                                                Database.insert(song)
                                                                Database.insert(
                                                                    SongPlaylistMap(
                                                                        songId = song.mediaId,
                                                                        playlistId = toPlaylistPreview.playlist.id,
                                                                        position = position + index
                                                                    ).default()
                                                                )
                                                            }
                                                        }
                                                    } else if (distinctSongs.isNotEmpty()){
                                                        CoroutineScope(Dispatchers.IO).launch {
                                                            addToYtPlaylist(toPlaylistPreview.playlist.id,
                                                                position,
                                                                toPlaylistPreview.playlist.browseId ?: "",
                                                                distinctSongs)
                                                        }
                                                    }
                                                    println("pipedInfo mediaitemmenu uuid ${playlistPreview.playlist.browseId}")

                                                    if (toPlaylistPreview.playlist.name.startsWith(
                                                            PIPED_PREFIX
                                                        ) && isPipedEnabled && pipedSession.token.isNotEmpty()
                                                    )
                                                        addToPipedPlaylist(
                                                            context = context,
                                                            coroutineScope = coroutineScope,
                                                            pipedSession = pipedSession.toApiSession(),
                                                            id = UUID.fromString(cleanPrefix(toPlaylistPreview.playlist.browseId ?: "")),
                                                            videos = listMediaItems.map { it.mediaId }
                                                                .toList()
                                                        )
                                                    listMediaItems.clear()
                                                    selectItems = false
                                                }
                                            }
                                        },
                                        onAddToPreferites = {
                                            if (!isNetworkConnected(appContext()) && isYouTubeSyncEnabled()) {
                                                SmartMessage(appContext().resources.getString(R.string.no_connection), context = appContext(), type = PopupType.Error)
                                            } else if (!isYouTubeSyncEnabled()){
                                                if (listMediaItems.isEmpty()) {
                                                    playlistSongs.forEachIndexed { index, song ->
                                                        if (song.song.likedAt in listOf(-1L,null)
                                                        ) {
                                                            mediaItemToggleLike(song.asMediaItem)
                                                        }
                                                    }
                                                } else {
                                                    Database.asyncTransaction {
                                                        listMediaItems.forEachIndexed { index, song ->
                                                            if (Database.getLikedAt(song.mediaId) !in listOf(-1L,null)
                                                            ) {
                                                                mediaItemToggleLike(song)
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                showYoutubeLikeConfirmDialog = true
                                            }
                                        },
                                        onRenumberPositions = {
                                            if (playlistNotMonthlyType)
                                                isRenumbering = true
                                            else
                                            /*
                                            SmartToast(context.resources.getString(R.string.info_cannot_renumbering_a_monthly_playlist))
                                             */
                                                SmartMessage(context.resources.getString(R.string.info_cannot_renumbering_a_monthly_playlist), context = context)
                                        },
                                        onDelete = {
                                            if (!isNetworkConnected(context) && playlistPreview.playlist.isYoutubePlaylist && isYouTubeSyncEnabled()){
                                                SmartMessage(context.resources.getString(R.string.no_connection), context = context, type = PopupType.Error)
                                            } else isDeleting = true
                                        },
                                        showonListenToYT = !playlistPreview.playlist.browseId.isNullOrBlank(),
                                        onListenToYT = {
                                            binder?.player?.pause()
                                            uriHandler.openUri(
                                                "https://youtube.com/playlist?list=${
                                                    playlistPreview.playlist.browseId?.let { 
                                                        cleanPrefix(it).removePrefix("VL")
                                                    }
                                                }"
                                            )
                                        },
                                        onExport = {
                                            isExporting = true
                                        },
                                        onEditThumbnail = {
                                            openEditThumbnailPicker()
                                        },
                                        onResetThumbnail = {
                                            resetThumbnail()
                                        },
                                        onGoToPlaylist = {
                                            navController.navigate("${NavRoutes.localPlaylist.name}/$it")
                                        },
                                        disableScrollingText = disableScrollingText,
                                        /*
                                        onImport = {
                                            try {
                                                importLauncher.launch(
                                                    arrayOf(
                                                        "text/csv",
                                                        "text/txt"
                                                    )
                                                )
                                            } catch (e: ActivityNotFoundException) {
                                                context.toast("Couldn't find an application to open documents")
                                            }
                                        }
                                        */
                                    )
                                }

                            }
                        }
                    )
                    //}
                }

                Spacer(modifier = Modifier.height(10.dp))

                /*        */
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth()
                ) {

                    HeaderIconButton(
                        icon = R.drawable.arrow_up,
                        color = colorPalette.text,
                        onClick = { sortOrder = !sortOrder },
                        modifier = Modifier
                            .graphicsLayer { rotationZ = sortOrderIconRotation }
                    )

                    BasicText(
                        text = when (sortBy) {
                            PlaylistSongSortBy.Album -> stringResource(R.string.sort_album)
                            PlaylistSongSortBy.AlbumYear -> stringResource(R.string.sort_album_year)
                            PlaylistSongSortBy.Position -> stringResource(R.string.sort_position)
                            PlaylistSongSortBy.Title -> stringResource(R.string.sort_title)
                            PlaylistSongSortBy.DatePlayed -> stringResource(R.string.sort_date_played)
                            PlaylistSongSortBy.DateLiked -> stringResource(R.string.sort_date_liked)
                            PlaylistSongSortBy.Artist -> stringResource(R.string.sort_artist)
                            PlaylistSongSortBy.ArtistAndAlbum -> "${stringResource(R.string.sort_artist)}, ${
                                stringResource(
                                    R.string.sort_album
                                )
                            }"

                            PlaylistSongSortBy.PlayTime -> stringResource(R.string.sort_listening_time)
                            PlaylistSongSortBy.Duration -> stringResource(R.string.sort_duration)
                            PlaylistSongSortBy.DateAdded -> stringResource(R.string.sort_date_added)
                            PlaylistSongSortBy.RelativePlayTime -> stringResource(R.string.sort_relative_listening_time)
                        },
                        style = typography.xs.semiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .clickable {
                                menuState.display {
                                    SortMenu(
                                        title = stringResource(R.string.sorting_order),
                                        onDismiss = menuState::hide,
                                        onTitle = { sortBy = PlaylistSongSortBy.Title },
                                        onAlbum = { sortBy = PlaylistSongSortBy.Album },
                                        onAlbumYear = { sortBy = PlaylistSongSortBy.AlbumYear },
                                        onDatePlayed = { sortBy = PlaylistSongSortBy.DatePlayed },
                                        onDateLiked = { sortBy = PlaylistSongSortBy.DateLiked },
                                        onPosition = { sortBy = PlaylistSongSortBy.Position },
                                        onArtist = { sortBy = PlaylistSongSortBy.Artist },
                                        onArtistAndAlbum = {
                                            sortBy = PlaylistSongSortBy.ArtistAndAlbum
                                        },
                                        onPlayTime = { sortBy = PlaylistSongSortBy.PlayTime },
                                        onRelativePlayTime = {
                                            sortBy = PlaylistSongSortBy.RelativePlayTime
                                        },
                                        onDuration = { sortBy = PlaylistSongSortBy.Duration },
                                        onDateAdded = { sortBy = PlaylistSongSortBy.DateAdded },
                                    )
                                }

                            }
                    )
                    HeaderIconButton(
                        icon = R.drawable.playlist,
                        color = colorPalette.text,
                        onClick = {},
                        modifier = Modifier
                            .offset(0.dp, 2.5.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {}
                            )
                    )

                    BasicText(
                        text = when (playlistSongsTypeFilter) {
                            PlaylistSongsTypeFilter.All -> stringResource(R.string.all)
                            PlaylistSongsTypeFilter.OnlineSongs -> stringResource(R.string.online_songs)
                            PlaylistSongsTypeFilter.Videos -> stringResource(R.string.videos)
                            PlaylistSongsTypeFilter.Local -> stringResource(R.string.on_device)
                            PlaylistSongsTypeFilter.Favorites -> stringResource(R.string.favorites)
                            PlaylistSongsTypeFilter.Unmatched -> stringResource(R.string.unmatched)
                            PlaylistSongsTypeFilter.Downloaded -> stringResource(R.string.downloaded)
                            PlaylistSongsTypeFilter.Cached -> stringResource(R.string.cached)
                            PlaylistSongsTypeFilter.Explicit -> stringResource(R.string.explicit)
                        },
                        style = typography.xs.semiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .offset(0.dp, 1.5.dp)
                            .clickable {
                                menuState.display {
                                    FilterMenu(
                                        title = stringResource(R.string.filter_by),
                                        onDismiss = menuState::hide,
                                        onAll = {
                                            playlistSongsTypeFilter = PlaylistSongsTypeFilter.All
                                        },
                                        onOnlineSongs = {
                                            playlistSongsTypeFilter =
                                                PlaylistSongsTypeFilter.OnlineSongs
                                        },
                                        onFavorites = {
                                            playlistSongsTypeFilter =
                                                PlaylistSongsTypeFilter.Favorites
                                        },
                                        onVideos = {
                                            playlistSongsTypeFilter = PlaylistSongsTypeFilter.Videos
                                        },
                                        onLocal = {
                                            playlistSongsTypeFilter = PlaylistSongsTypeFilter.Local
                                        },
                                        onUnmatched = {
                                            playlistSongsTypeFilter =
                                                PlaylistSongsTypeFilter.Unmatched
                                        },
                                        onDownloaded = {
                                            playlistSongsTypeFilter =
                                                PlaylistSongsTypeFilter.Downloaded
                                        },
                                        onCached = {
                                            playlistSongsTypeFilter = PlaylistSongsTypeFilter.Cached
                                        },
                                        onExplicit = {
                                            playlistSongsTypeFilter =
                                                PlaylistSongsTypeFilter.Explicit
                                        },
                                    )
                                }

                            }
                    )

                    Row(
                        horizontalArrangement = Arrangement.End, //Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        HeaderIconButton(
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .combinedClickable(
                                    onClick = {
                                        nowPlayingItem = -1
                                        scrollToNowPlaying = false
                                        playlistSongs
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
                            enabled = playlistSongs.isNotEmpty(),
                            color = if (playlistSongs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                            onClick = {}
                        )
                        LaunchedEffect(scrollToNowPlaying) {
                            if (scrollToNowPlaying)
                                lazyListState.scrollToItem(nowPlayingItem, 1)
                            scrollToNowPlaying = false
                        }
                        /*
                        HeaderIconButton(
                            modifier = Modifier.padding(horizontal = 5.dp),
                            onClick = { searching = !searching },
                            icon = R.drawable.search_circle,
                            color = colorPalette.text,
                            iconSize = 24.dp
                        )
                         */
                    }

                }


                Row(
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
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 10.dp)
                                ) {
                                    IconButton(
                                        onClick = {},
                                        icon = R.drawable.search,
                                        color = colorPalette.favoritesIcon,
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
                                            style = typography.xs.semiBold.secondary.copy(color = colorPalette.textDisabled)
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
                items = playlistSongs ?: emptyList(),
                key = { _, song -> song.song.id },
                contentType = { _, song -> song },
            ) { index, song ->

                if (index in positionsRecommendationList.distinct()) {
                    val songRecommended =
                        relatedSongsRecommendationResult?.getOrNull()?.songs?.shuffled()
                            ?.lastOrNull()
                    songRecommended?.asMediaItem?.let {
                        SongItem(
                            song = it,
                            isRecommended = true,
                            thumbnailSizeDp = thumbnailSizeDp,
                            thumbnailSizePx = thumbnailSizePx,
                            onDownloadClick = {},
                            downloadState = Download.STATE_STOPPED,
                            trailingContent = {},
                            onThumbnailContent = {},
                            modifier = Modifier
                                .clickable {
                                    binder?.stopRadio()
                                    binder?.player?.forcePlay(it)
                                },
                            disableScrollingText = disableScrollingText,
                            isNowPlaying = binder?.player?.isNowPlaying(it.mediaId) ?: false

                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .draggedItem(
                            reorderingState = reorderingState,
                            index = index
                        )
                        .zIndex(10f)
                ) {
                    val isLocal by remember { derivedStateOf { song.asMediaItem.isLocal } }
                    downloadState = getDownloadState(song.asMediaItem.mediaId)
                    val isDownloaded =
                        if (!isLocal) isDownloadedSong(song.asMediaItem.mediaId) else true
                    val checkedState = rememberSaveable { mutableStateOf(false) }
                    val positionInPlaylist: Int = index
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .zIndex(10f)
                            .align(Alignment.TopEnd)
                            .offset(x = -15.dp)

                    ) {
                        if (!isReorderDisabled && sortBy == PlaylistSongSortBy.Position && sortOrder == SortOrder.Ascending) {
                            IconButton(
                                icon = R.drawable.reorder,
                                color = colorPalette.textDisabled,
                                indication = rippleIndication,
                                onClick = {},
                                modifier = Modifier
                                    .reorder(
                                        reorderingState = reorderingState,
                                        index = index
                                    )
                            )
                        }
                    }

                    SwipeableQueueItem(
                        mediaItem = song.asMediaItem,
                        onRemoveFromQueue = {
                            if (!isNetworkConnected(context) && playlistPreview?.playlist?.isYoutubePlaylist == true && (playlistPreview?.playlist?.isEditable == true) && isYouTubeSyncEnabled()){
                                SmartMessage(context.resources.getString(R.string.no_connection), context = context, type = PopupType.Error)
                            } else if (playlistPreview?.playlist?.isEditable == true) {
                                if (isYouTubeSyncEnabled() && playlistPreview?.playlist?.isYoutubePlaylist == true && playlistPreview?.playlist?.isEditable == true) {
                                    Database.asyncTransaction {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            if (removeYTSongFromPlaylist(song.asMediaItem.mediaId,playlistPreview?.playlist?.browseId ?: "",playlistId)) {
                                                deleteSongFromPlaylist(song.asMediaItem.mediaId,playlistId)
                                            }
                                        }
                                    }
                                } else {
                                    Database.asyncTransaction {
                                        deleteSongFromPlaylist(song.asMediaItem.mediaId,playlistId)
                                    }
                                }

                                if (playlistPreview?.playlist?.name?.startsWith(PIPED_PREFIX) == true && isPipedEnabled && pipedSession.token.isNotEmpty()) {
                                    removeFromPipedPlaylist(
                                        context = context,
                                        coroutineScope = coroutineScope,
                                        pipedSession = pipedSession.toApiSession(),
                                        id = UUID.fromString(playlistPreview?.playlist?.browseId),
                                        positionInPlaylist
                                    )
                                }
                                coroutineScope.launch {
                                    SmartMessage(
                                        context.resources.getString(R.string.deleted) + " \"" + song.asMediaItem.mediaMetadata.title.toString() + " - " + song.asMediaItem.mediaMetadata.artist.toString() + "\" ",
                                        type = PopupType.Warning,
                                        context = context,
                                        durationLong = true
                                    )
                                }
                            } else {
                                SmartMessage(
                                    context.resources.getString(R.string.cannot_delete_from_online_playlists),type = PopupType.Warning, context = context)
                            }
                        },
                        onPlayNext = {
                            binder?.player?.addNext(song.asMediaItem)
                        }
                    ) {
                        var forceRecompose by remember { mutableStateOf(false) }
                        SongItem(
                            song = song.song,
                            onDownloadClick = {
                                binder?.cache?.removeResource(song.asMediaItem.mediaId)
                                Database.asyncTransaction {
                                    Database.insert(
                                        Song(
                                            id = song.asMediaItem.mediaId,
                                            title = song.asMediaItem.mediaMetadata.title.toString(),
                                            artistsText = song.asMediaItem.mediaMetadata.artist.toString(),
                                            thumbnailUrl = song.song.thumbnailUrl,
                                            durationText = null
                                        )
                                    )
                                }

                                if (!isLocal) {
                                    manageDownload(
                                        context = context,
                                        mediaItem = song.asMediaItem,
                                        downloadState = isDownloaded
                                    )
                                }
                                //if (isDownloaded) listDownloadedMedia.dropWhile { it.asMediaItem.mediaId == song.asMediaItem.mediaId } else listDownloadedMedia.add(song)
                                //Log.d("mediaItem", "manageDownload click isDownloaded ${isDownloaded} listDownloadedMedia ${listDownloadedMedia.distinct().size}")
                            },
                            downloadState = getDownloadState(song.asMediaItem.mediaId),
                            thumbnailSizePx = thumbnailSizePx,
                            thumbnailSizeDp = thumbnailSizeDp,
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
                                            checkedColor = colorPalette.accent,
                                            uncheckedColor = colorPalette.text
                                        ),
                                        modifier = Modifier
                                            .scale(0.7f)
                                    )
                                else checkedState.value = false

                                /*
                                if (!isReorderDisabled && sortBy == PlaylistSongSortBy.Position && sortOrder == SortOrder.Ascending) {
                                    IconButton(
                                        icon = R.drawable.reorder,
                                        color = colorPalette.textDisabled,
                                        indication = rippleIndication,
                                        onClick = {},
                                        modifier = Modifier
                                            .reorder(
                                                reorderingState = reorderingState,
                                                index = index
                                            )
                                            .size(18.dp)
                                    )
                                }
                                */
                            },
                            onThumbnailContent = {
                                if (sortBy == PlaylistSongSortBy.PlayTime) {
                                    BasicText(
                                        text = song.song.formattedTotalPlayTime,
                                        style = typography.xxs.semiBold.center.color(colorPalette.onOverlay),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        colorPalette.overlay
                                                    )
                                                ),
                                                shape = thumbnailShape
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .align(Alignment.BottomCenter)
                                    )
                                }
                                if (sortBy == PlaylistSongSortBy.RelativePlayTime) {
                                    BasicText(
                                        text = "${song.relativePlayTime().toLong()}",
                                        style = typography.xxs.semiBold.center.color(colorPalette.onOverlay),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        colorPalette.overlay
                                                    )
                                                ),
                                                shape = thumbnailShape
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .align(Alignment.BottomCenter)
                                    )
                                }

                                /*
                                if (sortBy == PlaylistSongSortBy.Position)
                                    BasicText(
                                        text = (index + 1).toString(),
                                        style = typography.m.semiBold.center.color(colorPalette.onOverlay),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        colorPalette.overlay
                                                    )
                                                ),
                                                shape = thumbnailShape
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .align(Alignment.Center)
                                    )
                                 */

                                if (nowPlayingItem > -1)
                                    NowPlayingSongIndicator(song.asMediaItem.mediaId, binder?.player)
                            },
                            modifier = Modifier
                                .combinedClickable(
                                    onLongClick = {
                                        menuState.display {
                                            InPlaylistMediaItemMenu(
                                                onMatchingSong = {
                                                    if (!isNetworkConnected(context) && playlistPreview?.playlist?.isYoutubePlaylist == true && (playlistPreview?.playlist?.isEditable == true) && isYouTubeSyncEnabled()){
                                                        SmartMessage(context.resources.getString(R.string.no_connection), context = context, type = PopupType.Error)
                                                    } else if ((playlistPreview?.playlist?.isYoutubePlaylist) == false){
                                                        songMatchingDialogEnable = true
                                                        matchingSongEntity = song
                                                    } else {
                                                        SmartMessage(
                                                            context.resources.getString(R.string.cannot_delete_from_online_playlists),type = PopupType.Warning, context = context)
                                                        }
                                                },
                                                navController = navController,
                                                playlist = playlistPreview,
                                                playlistId = playlistId,
                                                positionInPlaylist = index,
                                                song = song.song,
                                                onDismiss = menuState::hide,
                                                disableScrollingText = disableScrollingText,
                                            )
                                        }
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    onClick = {
                                        if (!selectItems) {
                                            if (song.song.thumbnailUrl == "") {
                                                songMatchingDialogEnable = true
                                                matchingSongEntity = song
                                            } else if (song.song.likedAt != -1L) {
                                                searching = false
                                                filter = null
                                                playlistSongs.filter { it.song.thumbnailUrl != "" && it.song.likedAt != -1L }
                                                    .map(SongEntity::asMediaItem)
                                                    .let { mediaItems ->
                                                        binder?.stopRadio()
                                                        binder?.player?.forcePlayAtIndex(
                                                            mediaItems,
                                                            mediaItems.indexOf(song.asMediaItem)
                                                        )
                                                    }
                                            } else {
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    SmartMessage(
                                                        context.resources.getString(R.string.disliked_this_song),
                                                        type = PopupType.Error,
                                                        context = context
                                                    )
                                                }
                                            }
                                        } else checkedState.value = !checkedState.value
                                    }
                                )
                                .draggedItem(reorderingState = reorderingState, index = index)
                                .background(color = colorPalette.background0),
                            disableScrollingText = disableScrollingText,
                            isNowPlaying = binder?.player?.isNowPlaying(song.song.id) ?: false
                        )
                    }
                }

            }

            item(
                key = "footer",
                contentType = 0,
            ) {
                Spacer(modifier = Modifier.height(Dimensions.bottomSpacer))
            }
        }

        FloatingActionsContainerWithScrollToTop(lazyListState = lazyListState)

        val showFloatingIcon by rememberPreference(showFloatingIconKey, false)
        if (uiType == UiType.ViMusic || showFloatingIcon)
            FloatingActionsContainerWithScrollToTop(
                lazyListState = lazyListState,
                iconId = R.drawable.shuffle,
                visible = !reorderingState.isDragging,
                onClick = {
                    if (playlistSongs.any { it.song.thumbnailUrl != "" && it.song.likedAt != -1L }) {
                        playlistSongs.filter { it.song.thumbnailUrl != "" && it.song.likedAt != -1L }.let { songs ->
                            if (songs.isNotEmpty()) {
                                binder?.stopRadio()
                                binder?.player?.forcePlayFromBeginning(
                                    songs.shuffled().map(SongEntity::asMediaItem)
                                )
                            }
                        }
                    } else {
                        SmartMessage(context.resources.getString(R.string.disliked_this_collection),type = PopupType.Error, context = context)
                    }
                }
            )


    }
}


