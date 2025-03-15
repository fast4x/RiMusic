package it.fast4x.rimusic.ui.screens.player.components.controls

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.R
import it.fast4x.rimusic.appContext
import it.fast4x.rimusic.cleanPrefix
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.context
import it.fast4x.rimusic.enums.ButtonState
import it.fast4x.rimusic.enums.ColorPaletteMode
import it.fast4x.rimusic.enums.ColorPaletteName
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.PlayerBackgroundColors
import it.fast4x.rimusic.enums.PlayerControlsType
import it.fast4x.rimusic.enums.PlayerPlayButtonType
import it.fast4x.rimusic.enums.PopupType
import it.fast4x.rimusic.enums.QueueLoopType
import it.fast4x.rimusic.models.Info
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.models.ui.UiMedia
import it.fast4x.rimusic.service.MyDownloadHelper
import it.fast4x.rimusic.service.modern.PlayerServiceModern
import it.fast4x.rimusic.typography
import it.fast4x.rimusic.ui.components.themed.IconButton
import it.fast4x.rimusic.ui.components.themed.SelectorArtistsDialog
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.ui.screens.player.bounceClick
import it.fast4x.rimusic.ui.screens.settings.isYouTubeSyncEnabled
import it.fast4x.rimusic.ui.styling.favoritesIcon
import it.fast4x.rimusic.utils.HorizontalfadingEdge2
import it.fast4x.rimusic.utils.addToYtLikedSong
import it.fast4x.rimusic.utils.bold
import it.fast4x.rimusic.utils.buttonStateKey
import it.fast4x.rimusic.utils.colorPaletteModeKey
import it.fast4x.rimusic.utils.colorPaletteNameKey
import it.fast4x.rimusic.utils.conditional
import it.fast4x.rimusic.utils.effectRotationKey
import it.fast4x.rimusic.utils.getIconQueueLoopState
import it.fast4x.rimusic.utils.getLikeState
import it.fast4x.rimusic.utils.getUnlikedIcon
import org.dailyislam.android.utilities.isNetworkConnected
import it.fast4x.rimusic.utils.jumpPreviousKey
import it.fast4x.rimusic.utils.playNext
import it.fast4x.rimusic.utils.playPrevious
import it.fast4x.rimusic.utils.playerBackgroundColorsKey
import it.fast4x.rimusic.utils.playerControlsTypeKey
import it.fast4x.rimusic.utils.queueLoopTypeKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.setLikeState
import it.fast4x.rimusic.utils.setQueueLoopState
import it.fast4x.rimusic.utils.showthumbnailKey
import it.fast4x.rimusic.utils.textCopyToClipboard
import it.fast4x.rimusic.utils.textoutlineKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@UnstableApi
@ExperimentalFoundationApi
@Composable
fun InfoAlbumAndArtistEssential(
    binder: PlayerServiceModern.Binder,
    navController: NavController,
    albumId: String?,
    media: UiMedia,
    mediaId: String,
    title: String?,
    likedAt: Long?,
    artistIds: List<Info>?,
    artist: String?,
    isExplicit: Boolean,
    onCollapse: () -> Unit,
    disableScrollingText: Boolean = false
) {
    val playerControlsType by rememberPreference(playerControlsTypeKey, PlayerControlsType.Essential)
    val colorPaletteMode by rememberPreference(colorPaletteModeKey, ColorPaletteMode.Dark)
    var effectRotationEnabled by rememberPreference(effectRotationKey, true)
    var showthumbnail by rememberPreference(showthumbnailKey, true)
    var isRotated by rememberSaveable { mutableStateOf(false) }
    var showSelectDialog by remember { mutableStateOf(false) }
    var textoutline by rememberPreference(textoutlineKey, false)
    val buttonState by rememberPreference(buttonStateKey, ButtonState.Idle)
    val playerBackgroundColors by rememberPreference(playerBackgroundColorsKey,PlayerBackgroundColors.BlurredCoverColor)
    var likeButtonWidth by remember{ mutableStateOf(0.dp) }
    val currentMediaItem = binder.player.currentMediaItem

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .fillMaxWidth()
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {


            var modifierTitle = Modifier
                .combinedClickable (
                    indication = ripple(bounded = true),
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        if (albumId != null) {
                            navController.navigate(route = "${NavRoutes.album.name}/${albumId}")
                            onCollapse()
                        }
                    },
                    onLongClick = {
                        textCopyToClipboard(cleanPrefix(title ?: ""), context = appContext())
                    }
                )

            if (!disableScrollingText) modifierTitle = modifierTitle.basicMarquee()

            /*if (isExplicit || playerControlsType == PlayerControlsType.Modern ) {
                Box(
                    modifier = Modifier.weight(0.1f)
                ) {
                    if (isExplicit) {
                        IconButton(
                            icon = R.drawable.explicit,
                            color = colorPalette().text,
                            enabled = true,
                            onClick = {},
                            modifier = Modifier
                                .size(18.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
            }*/

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .conditional(!disableScrollingText){HorizontalfadingEdge2(0.025f)},
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = modifierTitle
                    .conditional(!disableScrollingText) {padding(horizontal = maxWidth * 0.025f)}
                    .conditional(playerControlsType == PlayerControlsType.Modern){padding(start = likeButtonWidth)}
                ) {
                    if (isExplicit) {
                        IconButton(
                            icon = R.drawable.explicit,
                            color = colorPalette().text,
                            enabled = true,
                            onClick = {},
                            modifier = Modifier
                                .size(18.dp)
                        )
                        Spacer(modifier = Modifier
                               .width(5.dp)
                        )
                    }
                    Box {
                        BasicText(
                            text = cleanPrefix(title ?: ""),
                            style = TextStyle(
                                textAlign = TextAlign.Center,
                                color = if (albumId == null)
                                /*if (showthumbnail) colorPalette().textDisabled else if (colorPaletteMode == ColorPaletteMode.Light) colorPalette().textDisabled.copy(0.5f).compositeOver(Color.Black) else colorPalette().textDisabled.copy(0.35f).compositeOver(Color.White)
                                else colorPalette().text,*/
                                    if (colorPaletteMode == ColorPaletteMode.Light || (colorPaletteMode == ColorPaletteMode.System && (!isSystemInDarkTheme()))) colorPalette().textDisabled.copy(
                                        0.35f
                                    )
                                        .compositeOver(Color.Black) else colorPalette().textDisabled.copy(
                                        0.35f
                                    ).compositeOver(Color.White)
                                else colorPalette().text,
                                fontStyle = typography().l.bold.fontStyle,
                                fontWeight = typography().l.bold.fontWeight,
                                fontSize = typography().l.bold.fontSize,
                                fontFamily = typography().l.bold.fontFamily
                            ),
                            maxLines = 1,
                        )
                        BasicText(
                            text = cleanPrefix(title ?: ""),
                            style = TextStyle(
                                drawStyle = Stroke(width = 1.5f, join = StrokeJoin.Round),
                                textAlign = TextAlign.Center,
                                color = if (!textoutline) Color.Transparent else if (colorPaletteMode == ColorPaletteMode.Light || (colorPaletteMode == ColorPaletteMode.System && (!isSystemInDarkTheme()))) Color.White.copy(
                                    0.5f
                                )
                                else Color.Black,
                                fontStyle = typography().l.bold.fontStyle,
                                fontWeight = typography().l.bold.fontWeight,
                                fontSize = typography().l.bold.fontSize,
                                fontFamily = typography().l.bold.fontFamily
                            ),
                            maxLines = 1,
                        )
                    }
                }
            }

            //}
            if (playerControlsType == PlayerControlsType.Modern){
                BoxWithConstraints(
                    modifier = Modifier.weight(0.1f)
                ) {
                    likeButtonWidth = maxWidth
                    IconButton(
                        color = colorPalette().favoritesIcon,
                        icon = getLikeState(mediaId),
                        onClick = {
                            if (!isNetworkConnected(appContext()) && isYouTubeSyncEnabled()) {
                                SmartMessage(appContext().resources.getString(R.string.no_connection), context = appContext(), type = PopupType.Error)
                            } else if (!isYouTubeSyncEnabled()){
                                Database.asyncTransaction {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        if (like(mediaId, setLikeState(likedAt)) == 0) {
                                            currentMediaItem
                                                ?.takeIf { it.mediaId == mediaId }
                                                ?.let {
                                                    insert(currentMediaItem, Song::toggleLike)
                                                }
                                        }
                                    }
                                }
                            } else {
                                CoroutineScope(Dispatchers.IO).launch {
                                    if (currentMediaItem != null) {
                                        addToYtLikedSong(currentMediaItem)
                                    }
                                }
                            }
                            if (currentMediaItem != null) {
                                MyDownloadHelper.autoDownloadWhenLiked(
                                    context(),
                                    currentMediaItem
                                )
                            }
                            if (effectRotationEnabled) isRotated = !isRotated
                        },
                        modifier = Modifier
                            .padding(start = 5.dp)
                            .size(24.dp)
                    )
                    if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) {
                        Icon(
                            painter = painterResource(id = getUnlikedIcon()),
                            tint = colorPalette().text,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(start = 5.dp)
                                .size(24.dp)
                        )
                    }
                }
            }
        }

    }


    Spacer(
        modifier = Modifier
            .height(10.dp)
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .fillMaxWidth()
    ) {

        if (showSelectDialog)
            SelectorArtistsDialog(
                title = stringResource(R.string.artists),
                onDismiss = { showSelectDialog = false },
                values = artistIds,
                onValueSelected = {
                    navController.navigate(route = "${NavRoutes.artist.name}/${it}")
                    showSelectDialog = false
                    onCollapse()
                }
            )


        var modifierArtist = Modifier
            .combinedClickable (
                indication = ripple(bounded = true),
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    if (artistIds?.isNotEmpty() == true && artistIds.size > 1)
                        showSelectDialog = true
                    if (artistIds?.isNotEmpty() == true && artistIds.size == 1) {
                        navController.navigate(route = "${NavRoutes.artist.name}/${artistIds[0].id}")
                        onCollapse()
                    }
                },
                onLongClick = {
                    textCopyToClipboard(artist ?: "", context = appContext())
                }
            )

        if (!disableScrollingText) modifierArtist = modifierArtist.basicMarquee()

        BoxWithConstraints(
            modifier = Modifier
                .conditional(!disableScrollingText){HorizontalfadingEdge2(0.025f)},
            contentAlignment = Alignment.Center
        ) {
            BasicText(
                text = artist ?: "",
                style = TextStyle(
                    textAlign = TextAlign.Center,
                    color = if (artistIds?.isEmpty() == true)
                        /*if (showthumbnail) colorPalette().textDisabled else if (colorPaletteMode == ColorPaletteMode.Light) colorPalette().textDisabled.copy(0.5f).compositeOver(Color.Black) else colorPalette().textDisabled.copy(0.35f).compositeOver(Color.White)
                            else colorPalette().text,*/
                        if (colorPaletteMode == ColorPaletteMode.Light || (colorPaletteMode == ColorPaletteMode.System && (!isSystemInDarkTheme()))) colorPalette().textDisabled.copy(0.35f).compositeOver(Color.Black) else colorPalette().textDisabled.copy(0.35f).compositeOver(Color.White)
                        else colorPalette().text,
                    fontStyle = typography().m.bold.fontStyle,
                    fontSize = typography().m.bold.fontSize,
                    //fontWeight = typography().m.bold.fontWeight,
                    fontFamily = typography().m.bold.fontFamily
                ),
                maxLines = 1,
                modifier = modifierArtist
                    .conditional(!disableScrollingText){padding(horizontal = maxWidth*0.025f)}

            )
            BasicText(
                text = artist ?: "",
                style = TextStyle(
                    drawStyle = Stroke(width = 1.5f, join = StrokeJoin.Round),
                    textAlign = TextAlign.Center,
                    color = if (!textoutline) Color.Transparent else if (colorPaletteMode == ColorPaletteMode.Light || (colorPaletteMode == ColorPaletteMode.System && (!isSystemInDarkTheme()))) Color.White.copy(0.5f)
                    else Color.Black,
                    fontStyle = typography().m.bold.fontStyle,
                    fontSize = typography().m.bold.fontSize,
                    //fontWeight = typography().m.bold.fontWeight,
                    fontFamily = typography().m.bold.fontFamily
                ),
                maxLines = 1,
                modifier = modifierArtist
                    .conditional(!disableScrollingText){padding(horizontal = maxWidth*0.025f)}

            )
        }

    }

}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ControlsEssential(
    binder: PlayerServiceModern.Binder,
    position: Long,
    playbackSpeed: Float,
    shouldBePlaying: Boolean,
    likedAt: Long?,
    mediaId: String,
    playerPlayButtonType: PlayerPlayButtonType,
    isGradientBackgroundEnabled: Boolean,
    onShowSpeedPlayerDialog: () -> Unit,
) {
    val colorPaletteName by rememberPreference(colorPaletteNameKey, ColorPaletteName.Dynamic)
    val colorPaletteMode by rememberPreference(colorPaletteModeKey, ColorPaletteMode.Dark)
    var effectRotationEnabled by rememberPreference(effectRotationKey, true)
    var isRotated by rememberSaveable { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isRotated) 360F else 0f,
        animationSpec = tween(durationMillis = 200), label = ""
    )
    val shouldBePlayingTransition = updateTransition(shouldBePlaying, label = "shouldBePlaying")
    val playPauseRoundness by shouldBePlayingTransition.animateDp(
        transitionSpec = { tween(durationMillis = 100, easing = LinearEasing) },
        label = "playPauseRoundness",
        targetValueByState = { if (it) 32.dp else 16.dp }
    )

    var queueLoopType by rememberPreference(queueLoopTypeKey, defaultValue = QueueLoopType.Default)
    val playerBackgroundColors by rememberPreference(playerBackgroundColorsKey,PlayerBackgroundColors.BlurredCoverColor)
    var jumpPrevious by rememberPreference(jumpPreviousKey,"3")
    val currentMediaItem = binder.player.currentMediaItem
    var lightTheme = colorPaletteMode == ColorPaletteMode.Light || (colorPaletteMode == ColorPaletteMode.System && (!isSystemInDarkTheme()))

    Box {
        IconButton(
            color = colorPalette().favoritesIcon,
            icon = getLikeState(mediaId),
            onClick = {
                if (!isNetworkConnected(appContext()) && isYouTubeSyncEnabled()) {
                    SmartMessage(appContext().resources.getString(R.string.no_connection), context = appContext(), type = PopupType.Error)
                } else if (!isYouTubeSyncEnabled()){
                    Database.asyncTransaction {
                        CoroutineScope(Dispatchers.IO).launch {
                            if (like(mediaId, setLikeState(likedAt)) == 0) {
                                currentMediaItem
                                    ?.takeIf { it.mediaId == mediaId }
                                    ?.let {
                                        insert(currentMediaItem, Song::toggleLike)
                                    }
                            }
                        }
                    }
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (currentMediaItem != null) {
                            addToYtLikedSong(currentMediaItem)
                        }
                    }
                }
                if (currentMediaItem != null) {
                    MyDownloadHelper.autoDownloadWhenLiked(
                        context(),
                        currentMediaItem
                    )
                }
                if (effectRotationEnabled) isRotated = !isRotated
            },
            modifier = Modifier
                //.padding(10.dp)
                .size(26.dp)
        )
        if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) {
            Icon(
                painter = painterResource(id = getUnlikedIcon()),
                tint = colorPalette().text,
                contentDescription = null,
                modifier = Modifier
                    //.padding(10.dp)
                    .size(26.dp)
            )
        }

    }

    Image(
        painter = painterResource(R.drawable.play_skip_back),
        contentDescription = null,
        colorFilter = ColorFilter.tint(colorPalette().text),
        modifier = Modifier
            .combinedClickable(
                indication = ripple(bounded = false),
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    if (jumpPrevious == "") jumpPrevious = "0"
                    if(!binder.player.hasPreviousMediaItem() || (jumpPrevious != "0" && binder.player.currentPosition > jumpPrevious.toInt()*1000)){
                        binder.player.seekTo(0)
                    }
                    else binder.player.playPrevious()
                    if (effectRotationEnabled) isRotated = !isRotated
                },
                onLongClick = {}
            )
            .rotate(rotationAngle)
            .padding(10.dp)
            .size(26.dp)

    )



    Box(
        modifier = Modifier
            .combinedClickable(
                indication = ripple(bounded = false),
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    if (shouldBePlaying) {
                        //binder.player.pause()
                        binder.callPause({} )
                    } else {
                        /*
                        if (binder.player.playbackState == Player.STATE_IDLE) {
                            binder.player.prepare()
                        }
                         */
                        binder.player.play()
                    }
                    if (effectRotationEnabled) isRotated = !isRotated
                },
                onLongClick = onShowSpeedPlayerDialog
            )
            .bounceClick()
            .clip(RoundedCornerShape(playPauseRoundness))
            .background(
                when (playerPlayButtonType) {
                    PlayerPlayButtonType.CircularRibbed, PlayerPlayButtonType.Disabled -> Color.Transparent
                    else -> {
                        if (playerBackgroundColors == PlayerBackgroundColors.AnimatedGradient)
                            if (lightTheme) Color.White else Color.Black
                        else colorPalette().accent
                    }
                }
            )
            .width(playerPlayButtonType.width.dp)
            .height(playerPlayButtonType.height.dp)

    ) {

        if (playerPlayButtonType == PlayerPlayButtonType.CircularRibbed)
            Image(
                painter = painterResource(R.drawable.a13shape),
                colorFilter = ColorFilter.tint(
                    if (playerBackgroundColors == PlayerBackgroundColors.AnimatedGradient)
                        if (lightTheme) Color.White else Color.Black
                    else colorPalette().accent
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(rotationAngle)
                    .bounceClick(),
                contentDescription = "Background Image",
                contentScale = ContentScale.Fit
            )

        Image(
            painter = painterResource(if (shouldBePlaying) R.drawable.pause else R.drawable.play),
            contentDescription = null,
            colorFilter = ColorFilter.tint(if (playerPlayButtonType == PlayerPlayButtonType.Disabled || playerBackgroundColors == PlayerBackgroundColors.AnimatedGradient) colorPalette().accent else colorPalette().text),
            modifier = Modifier
                .rotate(rotationAngle)
                .align(Alignment.Center)
                .size(if (playerPlayButtonType == PlayerPlayButtonType.Disabled) 40.dp else 30.dp)
                .bounceClick()
        )

        val fmtSpeed = "%.1fx".format(playbackSpeed).replace(",", ".")
        if (fmtSpeed != "1.0x")
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)

            ) {
                BasicText(
                    text = fmtSpeed,
                    style = TextStyle(
                        color = colorPalette().text,
                        fontStyle = typography().xxxs.semiBold.fontStyle,
                        fontSize = typography().xxxs.semiBold.fontSize
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
        colorFilter = ColorFilter.tint(colorPalette().text),
        modifier = Modifier
            .combinedClickable(
                indication = ripple(bounded = false),
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    //binder.player.forceSeekToNext()
                    binder.player.playNext()
                    if (effectRotationEnabled) isRotated = !isRotated
                },
                onLongClick = {}
            )
            .rotate(rotationAngle)
            .padding(10.dp)
            .size(26.dp)

    )



    IconButton(
        icon = getIconQueueLoopState(queueLoopType),
        color = colorPalette().text,
        onClick = {
            queueLoopType = setQueueLoopState(queueLoopType)
        },
        modifier = Modifier
            //.padding(10.dp)
            .size(26.dp)
    )

}