package it.vfsfitvnm.vimusic.ui.screens.player

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.audiofx.AudioEffect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import it.vfsfitvnm.innertube.models.NavigationEndpoint
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.ColorPaletteName
import it.vfsfitvnm.vimusic.enums.PauseBetweenSongs
import it.vfsfitvnm.vimusic.enums.PlayerPlayButtonType
import it.vfsfitvnm.vimusic.enums.PlayerThumbnailSize
import it.vfsfitvnm.vimusic.enums.PlayerTimelineType
import it.vfsfitvnm.vimusic.enums.UiType
import it.vfsfitvnm.vimusic.models.Info
import it.vfsfitvnm.vimusic.models.Song
import it.vfsfitvnm.vimusic.models.ui.UiMedia
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.service.PlayerService
import it.vfsfitvnm.vimusic.ui.components.SeekBar
import it.vfsfitvnm.vimusic.ui.components.SeekBarCustom
import it.vfsfitvnm.vimusic.ui.components.SeekBarWaved
import it.vfsfitvnm.vimusic.ui.components.themed.BaseMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.IconButton
import it.vfsfitvnm.vimusic.ui.components.themed.PlaybackParamsDialog
import it.vfsfitvnm.vimusic.ui.components.themed.ScrollText
import it.vfsfitvnm.vimusic.ui.components.themed.SelectorDialog
import it.vfsfitvnm.vimusic.ui.screens.albumRoute
import it.vfsfitvnm.vimusic.ui.screens.artistRoute
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.collapsedPlayerProgressBar
import it.vfsfitvnm.vimusic.ui.styling.favoritesIcon
import it.vfsfitvnm.vimusic.utils.UiTypeKey
import it.vfsfitvnm.vimusic.utils.bold
import it.vfsfitvnm.vimusic.utils.colorPaletteNameKey
import it.vfsfitvnm.vimusic.utils.disableScrollingTextKey
import it.vfsfitvnm.vimusic.utils.downloadedStateMedia
import it.vfsfitvnm.vimusic.utils.effectRotationKey
import it.vfsfitvnm.vimusic.utils.forceSeekToNext
import it.vfsfitvnm.vimusic.utils.forceSeekToPrevious
import it.vfsfitvnm.vimusic.utils.formatAsDuration
import it.vfsfitvnm.vimusic.utils.isCompositionLaunched
import it.vfsfitvnm.vimusic.utils.isGradientBackgroundEnabledKey
import it.vfsfitvnm.vimusic.utils.pauseBetweenSongsKey
import it.vfsfitvnm.vimusic.utils.playbackSpeedKey
import it.vfsfitvnm.vimusic.utils.playerPlayButtonTypeKey
import it.vfsfitvnm.vimusic.utils.playerThumbnailSizeKey
import it.vfsfitvnm.vimusic.utils.playerTimelineTypeKey
import it.vfsfitvnm.vimusic.utils.positionAndDurationState
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.seamlessPlay
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.vimusic.utils.toast
import it.vfsfitvnm.vimusic.utils.trackLoopEnabledKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@UnstableApi
@Composable
fun Controls(
    media: UiMedia,
    mediaId: String,
    title: String?,
    artist: String?,
    artistIds: List<Info>?,
    albumId: String?,
    shouldBePlaying: Boolean,
    position: Long,
    duration: Long,
    modifier: Modifier = Modifier
) {
    val (colorPalette, typography) = LocalAppearance.current
    val colorPaletteName by rememberPreference(colorPaletteNameKey, ColorPaletteName.ModernBlack)

    val binder = LocalPlayerServiceBinder.current
    binder?.player ?: return

    val uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)

    var trackLoopEnabled by rememberPreference(trackLoopEnabledKey, defaultValue = false)

    var scrubbingPosition by remember(mediaId) {
        mutableStateOf<Long?>(null)
    }

    val onGoToArtist = artistRoute::global
    val onGoToAlbum = albumRoute::global


    var likedAt by rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    /*
    var nextmediaItemIndex = binder.player.nextMediaItemIndex ?: -1
    var nextmediaItemtitle = ""


    if (nextmediaItemIndex.toShort() > -1)
        nextmediaItemtitle = binder.player.getMediaItemAt(nextmediaItemIndex).mediaMetadata.title.toString()
    */

    var isRotated by rememberSaveable { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isRotated) 360F else 0f,
        animationSpec = tween(durationMillis = 200), label = ""
    )
    var effectRotationEnabled by rememberPreference(effectRotationKey, true)
    var disableScrollingText by rememberPreference(disableScrollingTextKey, false)
    var playerTimelineType by rememberPreference(playerTimelineTypeKey, PlayerTimelineType.Default)
    var playerPlayButtonType by rememberPreference(playerPlayButtonTypeKey, PlayerPlayButtonType.Rectangular)

    val scope = rememberCoroutineScope()
    val animatedPosition = remember { Animatable(position.toFloat()) }
    var isSeeking by remember { mutableStateOf(false) }


    val compositionLaunched = isCompositionLaunched()
    LaunchedEffect(mediaId) {
        if (compositionLaunched) animatedPosition.animateTo(0f)
    }
    LaunchedEffect(position) {
        if (!isSeeking && !animatedPosition.isRunning)
            animatedPosition.animateTo(position.toFloat(), tween(
                durationMillis = 1000,
                easing = LinearEasing
            ))
    }
    //val durationVisible by remember(isSeeking) { derivedStateOf { isSeeking } }



    LaunchedEffect(mediaId) {
        Database.likedAt(mediaId).distinctUntilChanged().collect { likedAt = it }
    }

    var isDownloaded by rememberSaveable {
        mutableStateOf<Boolean>(false)
    }

    isDownloaded = downloadedStateMedia(mediaId)

    //val menuState = LocalMenuState.current

    val shouldBePlayingTransition = updateTransition(shouldBePlaying, label = "shouldBePlaying")

    val playPauseRoundness by shouldBePlayingTransition.animateDp(
        transitionSpec = { tween(durationMillis = 100, easing = LinearEasing) },
        label = "playPauseRoundness",
        targetValueByState = { if (it) 32.dp else 16.dp }
    )

    var showSelectDialog by remember { mutableStateOf(false) }

    val playerThumbnailSize by rememberPreference(playerThumbnailSizeKey, PlayerThumbnailSize.Medium)
    val isGradientBackgroundEnabled by rememberPreference(isGradientBackgroundEnabledKey, false)

    /*
    var windows by remember {
        mutableStateOf(binder.player.currentTimeline.windows)
    }
    var queuedSongs by remember {
        mutableStateOf<List<Song>>(emptyList())
    }
    LaunchedEffect(mediaId, windows) {
        Database.getSongsList(
            windows.map {
                it.mediaItem.mediaId
            }
        ).collect{ queuedSongs = it}
    }

    var totalPlayTimes = 0L
    queuedSongs.forEach {
        totalPlayTimes += it.durationText?.let { it1 ->
            durationTextToMillis(it1)
        }?.toLong() ?: 0
    }
     */

    /*
    var showLyrics by rememberSaveable {
        mutableStateOf(false)
    }
     */

    val pauseBetweenSongs  by rememberPreference(pauseBetweenSongsKey,   PauseBetweenSongs.`0`)

    var playbackSpeed  by rememberPreference(playbackSpeedKey,   1f)
    var showSpeedPlayerDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showSpeedPlayerDialog) {
        PlaybackParamsDialog(
            onDismiss = { showSpeedPlayerDialog = false },
            speedValue = { playbackSpeed = it },
            pitchValue = {}
        )
    }

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = playerThumbnailSize.size.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .fillMaxWidth()
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (uiType != UiType.ViMusic) Arrangement.Start else Arrangement.Center,
                modifier = Modifier.fillMaxWidth(if (uiType != UiType.ViMusic) 0.90f else 1f)
            ) {
                if (uiType != UiType.ViMusic) {

                    IconButton(
                        icon = if (albumId == null) R.drawable.alert_circle_not_filled else R.drawable.album,
                        color = if (albumId == null) colorPalette.textDisabled else colorPalette.text,
                        enabled = albumId != null,
                        onClick = {
                            if (albumId != null) onGoToAlbum(albumId)
                        },
                        modifier = Modifier
                            .size(26.dp)
                    )

                    Spacer(
                        modifier = Modifier
                            .width(8.dp)
                    )
                }

                if (disableScrollingText == false) {
                    ScrollText(
                        text = title ?: "",
                        style = TextStyle(
                            color = if (albumId == null) colorPalette.textDisabled else colorPalette.text,
                            fontStyle = typography.l.bold.fontStyle,
                            fontSize = typography.l.bold.fontSize
                        ),
                        onClick = {
                            //if (albumId != null) onGoToAlbum(albumId)
                        },

                    )
                } else {
                    BasicText(
                        text = title ?: "",
                        style = TextStyle(
                            color = if (albumId == null) colorPalette.textDisabled else colorPalette.text,
                            fontStyle = typography.l.bold.fontStyle,
                            fontSize = typography.l.bold.fontSize
                        ),
                        maxLines = 1,
                        modifier = Modifier
                            .clickable {
                                //if (albumId != null) onGoToAlbum(albumId)
                            }
                    )
                }
            }

            if (uiType != UiType.ViMusic) {
                IconButton(
                    //color = if (likedAt == null) colorPalette.textDisabled else colorPalette.text,
                    color = colorPalette.favoritesIcon,
                    //icon = R.drawable.heart,
                    icon = if (likedAt == null) R.drawable.heart_outline else R.drawable.heart,
                    onClick = {
                        val currentMediaItem = binder.player.currentMediaItem
                        query {
                            if (Database.like(
                                    mediaId,
                                    if (likedAt == null) System.currentTimeMillis() else null
                                ) == 0
                            ) {
                                currentMediaItem
                                    ?.takeIf { it.mediaId == mediaId }
                                    ?.let {
                                        Database.insert(currentMediaItem, Song::toggleLike)
                                    }
                            }
                        }
                        if (effectRotationEnabled) isRotated = !isRotated
                    },
                    modifier = Modifier
                        .size(24.dp)
                )

                /*
                IconButton(
                    icon = R.drawable.ellipsis_vertical,
                    color = colorPalette.text,
                    onClick = {
                        menuState.display {
                            binder.player.currentMediaItem?.let {
                                PlayerMenu(
                                    onDismiss = menuState::hide,
                                    mediaItem = it,
                                    binder = binder
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        //.padding(horizontal = 15.dp)
                        .size(24.dp)
                )
                 */
            }

        }



        Spacer(
            modifier = Modifier
                .height(10.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (uiType != UiType.ViMusic) Arrangement.Start else Arrangement.Center,
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .fillMaxWidth()
        ) {


            if (showSelectDialog)
                SelectorDialog(
                    title = stringResource(R.string.artists),
                    onDismiss = { showSelectDialog = false },
                    values = artistIds,
                    onValueSelected = {
                        onGoToArtist(it)
                        showSelectDialog = false
                    }
                )


            if (uiType != UiType.ViMusic) {
                IconButton(
                    icon = if (artistIds?.isEmpty() == true) R.drawable.alert_circle_not_filled else R.drawable.artists,
                    color = if (artistIds?.isEmpty() == true) colorPalette.textDisabled else colorPalette.text,
                    onClick = {
                        if (artistIds?.isNotEmpty() == true && artistIds.size > 1)
                            showSelectDialog = true
                        if (artistIds?.isNotEmpty() == true && artistIds.size == 1)
                            onGoToArtist( artistIds[0].id )
                    },
                    modifier = Modifier
                        .size(24.dp)
                        .padding(start = 2.dp)
                )

            Spacer(
                modifier = Modifier
                    .width(12.dp)
            )
        }

            if (disableScrollingText == false) {
            ScrollText(
                text = artist ?: "",
                style = TextStyle(
                    color = if (artistIds?.isEmpty() == true) colorPalette.textDisabled else colorPalette.text,
                    fontStyle = typography.s.bold.fontStyle,
                    fontSize = typography.s.bold.fontSize
                ),
                onClick = {
                    if (artistIds?.isNotEmpty() == true && artistIds.size > 1)
                        showSelectDialog = true
                    if (artistIds?.isNotEmpty() == true && artistIds.size == 1)
                        onGoToArtist( artistIds[0].id )
                }
            ) } else {
        BasicText(
            text = artist ?: "",
            style = TextStyle(
                color = if (artistIds?.isEmpty() == true) colorPalette.textDisabled else colorPalette.text,
                fontStyle = typography.s.bold.fontStyle,
                fontSize = typography.s.bold.fontSize
            ),
            maxLines = 1,
            modifier = Modifier
                .clickable {
                    if (artistIds?.isNotEmpty() == true && artistIds.size > 1)
                        showSelectDialog = true
                    if (artistIds?.isNotEmpty() == true && artistIds.size == 1)
                        onGoToArtist( artistIds[0].id )
                }
        )}

        }


        Spacer(
            modifier = Modifier
                .height(20.dp)
        )


        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .fillMaxWidth()
        ) {

            if (playerTimelineType != PlayerTimelineType.Default && playerTimelineType != PlayerTimelineType.Wavy)
                SeekBarCustom(
                    type = playerTimelineType,
                    value = scrubbingPosition ?: position,
                    minimumValue = 0,
                    maximumValue = duration,
                    onDragStart = {
                        scrubbingPosition = it
                    },
                    onDrag = { delta ->
                        scrubbingPosition = if (duration != C.TIME_UNSET) {
                            scrubbingPosition?.plus(delta)?.coerceIn(0, duration)
                        } else {
                            null
                        }
                    },
                    onDragEnd = {
                        scrubbingPosition?.let(binder.player::seekTo)
                        scrubbingPosition = null
                    },
                    color = colorPalette.collapsedPlayerProgressBar,
                    backgroundColor = colorPalette.textSecondary,
                    shape = RoundedCornerShape(8.dp)
                )

            if (playerTimelineType == PlayerTimelineType.Default)
                SeekBar(
                    value = scrubbingPosition ?: position,
                    minimumValue = 0,
                    maximumValue = duration,
                    onDragStart = {
                        scrubbingPosition = it
                    },
                    onDrag = { delta ->
                        scrubbingPosition = if (duration != C.TIME_UNSET) {
                            scrubbingPosition?.plus(delta)?.coerceIn(0, duration)
                        } else {
                            null
                        }
                    },
                    onDragEnd = {
                        scrubbingPosition?.let(binder.player::seekTo)
                        scrubbingPosition = null
                    },
                    color = colorPalette.collapsedPlayerProgressBar,
                    backgroundColor = colorPalette.textSecondary,
                    shape = RoundedCornerShape(8.dp),
                )




            if (playerTimelineType == PlayerTimelineType.Wavy) {
                SeekBarWaved(
                    position = { animatedPosition.value },
                    range = 0f..media.duration.toFloat(),
                    onSeekStarted = {
                        scrubbingPosition = it.toLong()

                        //isSeeking = true
                        scope.launch {
                            animatedPosition.animateTo(it)
                        }

                    },
                    onSeek = { delta ->
                        scrubbingPosition = if (duration != C.TIME_UNSET) {
                            scrubbingPosition?.plus(delta)?.coerceIn(0F, duration.toFloat())
                                ?.toLong()
                        } else {
                            null
                        }

                        if (media.duration != C.TIME_UNSET) {
                            //isSeeking = true
                            scope.launch {
                                animatedPosition.snapTo(
                                    animatedPosition.value.plus(delta)
                                        .coerceIn(0f, media.duration.toFloat())
                                )
                            }
                        }

                    },
                    onSeekFinished = {
                        scrubbingPosition?.let(binder.player::seekTo)
                        scrubbingPosition = null
                        /*
                    isSeeking = false
                    animatedPosition.let {
                        binder.player.seekTo(it.targetValue.toLong())
                    }
                     */
                    },
                    color = colorPalette.collapsedPlayerProgressBar,
                    isActive = binder.player.isPlaying,
                    backgroundColor = colorPalette.textSecondary,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Spacer(
            modifier = Modifier
                .height(8.dp)
        )


        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .fillMaxWidth()
        ) {
            BasicText(
                text = formatAsDuration(scrubbingPosition ?: position),
                style = typography.xxs.semiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (duration != C.TIME_UNSET) {
                val positionAndDuration = binder.player.positionAndDurationState()
                var timeRemaining by remember { mutableIntStateOf(0) }
                timeRemaining = positionAndDuration.value.second.toInt() - positionAndDuration.value.first.toInt()
                var paused by remember { mutableStateOf(false) }

                if (pauseBetweenSongs != PauseBetweenSongs.`0`)
                    LaunchedEffect(timeRemaining){
                        if (
                            //formatAsDuration(timeRemaining.toLong()) == "0:00"
                            timeRemaining.toLong() < 500
                            )
                        {
                            paused = true
                            binder.player.pause()
                            delay(pauseBetweenSongs.number)
                            //binder.player.seekTo(position+2000)
                            binder.player.play()
                            paused = false
                        }
                    }

                    if (!paused) {

                        BasicText(
                            text = "-${formatAsDuration(timeRemaining.toLong())}",
                            style = typography.xxs.semiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                        )

                        /*
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                        ) {
                            Image(
                                painter = painterResource(R.drawable.time),
                                colorFilter = ColorFilter.tint(colorPalette.accent),
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(horizontal = 5.dp),
                                contentDescription = "Background Image",
                                contentScale = ContentScale.Fit
                            )
                            BasicText(
                                text = " ${formatAsTime(totalPlayTimes)}",
                                style = typography.xxs.semiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                         */

                    } else {
                        Image(
                            painter = painterResource(R.drawable.pause),
                            colorFilter = ColorFilter.tint(colorPalette.accent),
                            modifier = Modifier
                                .size(20.dp),
                            contentDescription = "Background Image",
                            contentScale = ContentScale.Fit
                        )
                    }

                /*
                BasicText(
                    text = "-${formatAsDuration(timeRemaining.toLong())} / ${formatAsDuration(duration)}",
                    style = typography.xxs.semiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                 */

                BasicText(
                    text = formatAsDuration(duration),
                    style = typography.xxs.semiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

            }
        }

        Spacer(
            modifier = Modifier
                .weight(0.5f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
        ) {

            if (uiType != UiType.RiMusic)
                IconButton(
                    color = colorPalette.favoritesIcon,
                    icon = if (likedAt == null) R.drawable.heart_outline else R.drawable.heart,
                    onClick = {
                        val currentMediaItem = binder.player.currentMediaItem
                        query {
                            if (Database.like(
                                    mediaId,
                                    if (likedAt == null) System.currentTimeMillis() else null
                                ) == 0
                            ) {
                                currentMediaItem
                                    ?.takeIf { it.mediaId == mediaId }
                                    ?.let {
                                        Database.insert(currentMediaItem, Song::toggleLike)
                                    }
                            }
                        }
                        if (effectRotationEnabled) isRotated = !isRotated
                    },
                    modifier = Modifier
                        .padding(10.dp)
                        .size(26.dp)
                )

            Image(
                painter = painterResource(R.drawable.play_skip_back),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorPalette.collapsedPlayerProgressBar),
                modifier = Modifier
                    .combinedClickable(
                        indication = rememberRipple(bounded = false),
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {
                            //binder.player.forceSeekToPrevious()
                            binder.player.seekToPrevious()
                            if (effectRotationEnabled) isRotated = !isRotated
                        },
                        onLongClick = {
                            binder.player.seekTo(position - 5000)
                        }
                    )
                    .rotate(rotationAngle)
                    .padding(10.dp)
                    .size(26.dp)

            )

            Box(
                modifier = Modifier
                    .combinedClickable(
                        indication = rememberRipple(bounded = false),
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {
                            if (shouldBePlaying) {
                                binder.player.pause()
                            } else {
                                if (binder.player.playbackState == Player.STATE_IDLE) {
                                    binder.player.prepare()
                                }
                                binder.player.play()
                            }
                            if (effectRotationEnabled) isRotated = !isRotated
                        },
                        onLongClick = {
                            showSpeedPlayerDialog = true
                        }
                    )
                    .clip(RoundedCornerShape(playPauseRoundness))
                    .background(
                        when (colorPaletteName) {
                            ColorPaletteName.Dynamic, ColorPaletteName.Default -> {
                                when (playerPlayButtonType) {
                                    PlayerPlayButtonType.CircularRibbed -> {
                                        if (isGradientBackgroundEnabled) colorPalette.background2
                                        else colorPalette.background1
                                    }

                                    PlayerPlayButtonType.Disabled -> colorPalette.background1
                                    else -> {
                                        if (isGradientBackgroundEnabled) colorPalette.background1
                                        else colorPalette.background2
                                    }
                                }
                            }

                            ColorPaletteName.PureBlack, ColorPaletteName.ModernBlack ->
                                if (playerPlayButtonType == PlayerPlayButtonType.CircularRibbed)
                                    colorPalette.background1 else
                                    if (playerPlayButtonType != PlayerPlayButtonType.Disabled)
                                        colorPalette.background4 else colorPalette.background0
                        }
                    )
                    .width(playerPlayButtonType.width.dp)
                    .height(playerPlayButtonType.height.dp)
                    //.width(if (uiType != UiType.RiMusic) PlayerPlayButtonType.Default.width.dp else playerPlayButtonType.width.dp)
                    //.height(if (uiType != UiType.RiMusic) PlayerPlayButtonType.Default.height.dp else playerPlayButtonType.height.dp)
            ) {
                //if (uiType == UiType.RiMusic && playerPlayButtonType == PlayerPlayButtonType.CircularRibbed)
                if (playerPlayButtonType == PlayerPlayButtonType.CircularRibbed)
                Image(
                    painter = painterResource(R.drawable.a13shape),
                    colorFilter = ColorFilter.tint(
                        when (colorPaletteName) {
                            ColorPaletteName.PureBlack, ColorPaletteName.ModernBlack -> colorPalette.background4
                            else -> if (isGradientBackgroundEnabled) colorPalette.background1
                            else colorPalette.background2
                        }
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(rotationAngle),
                    contentDescription = "Background Image",
                    contentScale = ContentScale.Fit
                )

                Image(
                    painter = painterResource(if (shouldBePlaying) R.drawable.pause else R.drawable.play),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorPalette.collapsedPlayerProgressBar), //if (playerPlayButtonType == PlayerPlayButtonType.CircularRibbed) ColorFilter.tint(colorPalette.iconButtonPlayer) else ColorFilter.tint(colorPalette.text),
                    modifier = Modifier
                        .rotate(rotationAngle)
                        .align(Alignment.Center)
                        .size(30.dp)
                )

                if ("%.1fx".format(playbackSpeed) != "1,0x")
                    Box(
                        modifier = Modifier
                        .align(Alignment.BottomCenter)

                    ) {
                        BasicText(
                            text = "%.1fx".format(playbackSpeed),
                            style = TextStyle(
                                color = colorPalette.collapsedPlayerProgressBar,
                                fontStyle = typography.xxxs.semiBold.fontStyle,
                                fontSize = typography.xxxs.semiBold.fontSize
                            ),
                            maxLines = 1,
                            modifier = Modifier
                                .padding(bottom = if (playerPlayButtonType != PlayerPlayButtonType.CircularRibbed) 5.dp else 15.dp)
                        )
                    }
            }


            Image(
                painter = painterResource(R.drawable.play_skip_forward),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorPalette.collapsedPlayerProgressBar),
                modifier = Modifier
                    .combinedClickable(
                        indication = rememberRipple(bounded = false),
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {
                            binder.player.forceSeekToNext()
                            if (effectRotationEnabled) isRotated = !isRotated
                        },
                        onLongClick = {
                            binder.player.seekTo(position + 5000)
                        }
                    )
                    .rotate(rotationAngle)
                    .padding(10.dp)
                    .size(26.dp)

            )


            if (uiType != UiType.RiMusic)
            IconButton(
                icon = R.drawable.repeat,
                color = if (trackLoopEnabled) colorPalette.iconButtonPlayer else colorPalette.textDisabled,
                onClick = {
                    trackLoopEnabled = !trackLoopEnabled
                },
                modifier = Modifier
                    .padding(10.dp)
                    .size(26.dp)
            )


        }

        Spacer(
            modifier = Modifier
                .weight(0.8f)
        )

        }

}
@ExperimentalTextApi
@ExperimentalAnimationApi
@UnstableApi
@Composable
private fun PlayerMenu(
    binder: PlayerService.Binder,
    mediaItem: MediaItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    BaseMediaItemMenu(
        mediaItem = mediaItem,
        onStartRadio = {
            binder.stopRadio()
            binder.player.seamlessPlay(mediaItem)
            binder.setupRadio(NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId))
        },
        onGoToEqualizer = {
            try {
                activityResultLauncher.launch(
                    Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                        putExtra(AudioEffect.EXTRA_AUDIO_SESSION, binder.player.audioSessionId)
                        putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                        putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                    }
                )
            } catch (e: ActivityNotFoundException) {
                context.toast("Couldn't find an application to equalize audio")
            }
        },
        onShowSleepTimer = {},
        onDismiss = onDismiss
    )
}

@Composable
private fun Duration(
    position: Float,
    duration: Long,
) {
    val typography = LocalAppearance.current.typography
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        BasicText(
            text = formatAsDuration(position.toLong()),
            style = typography.xxs.semiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (duration != C.TIME_UNSET) {
            BasicText(
                text = formatAsDuration(duration),
                style = typography.xxs.semiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}