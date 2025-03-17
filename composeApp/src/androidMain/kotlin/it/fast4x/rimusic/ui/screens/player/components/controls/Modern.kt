package it.fast4x.rimusic.ui.screens.player.components.controls

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.blur
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
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.R
import it.fast4x.rimusic.appContext
import it.fast4x.rimusic.cleanPrefix
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.context
import it.fast4x.rimusic.enums.ColorPaletteMode
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.PlayerBackgroundColors
import it.fast4x.rimusic.enums.PlayerControlsType
import it.fast4x.rimusic.enums.PlayerPlayButtonType
import it.fast4x.rimusic.enums.PopupType
import it.fast4x.rimusic.models.Info
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.models.ui.UiMedia
import it.fast4x.rimusic.service.MyDownloadHelper
import it.fast4x.rimusic.service.modern.PlayerServiceModern
import it.fast4x.rimusic.typography
import it.fast4x.rimusic.ui.components.themed.CustomElevatedButton
import it.fast4x.rimusic.ui.components.themed.IconButton
import it.fast4x.rimusic.ui.components.themed.SelectorArtistsDialog
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.ui.screens.player.bounceClick
import it.fast4x.rimusic.ui.screens.settings.isYouTubeSyncEnabled
import it.fast4x.rimusic.ui.styling.favoritesIcon
import it.fast4x.rimusic.utils.addToYtLikedSong
import it.fast4x.rimusic.utils.bold
import it.fast4x.rimusic.utils.colorPaletteModeKey
import it.fast4x.rimusic.utils.doubleShadowDrop
import it.fast4x.rimusic.utils.dropShadow
import it.fast4x.rimusic.utils.effectRotationKey
import it.fast4x.rimusic.utils.getLikeState
import it.fast4x.rimusic.utils.getUnlikedIcon
import org.dailyislam.android.utilities.isNetworkConnected
import it.fast4x.rimusic.utils.jumpPreviousKey
import it.fast4x.rimusic.utils.playNext
import it.fast4x.rimusic.utils.playPrevious
import it.fast4x.rimusic.utils.playerBackgroundColorsKey
import it.fast4x.rimusic.utils.playerControlsTypeKey
import it.fast4x.rimusic.utils.playerInfoShowIconsKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.setLikeState
import it.fast4x.rimusic.utils.showthumbnailKey
import it.fast4x.rimusic.utils.textCopyToClipboard
import it.fast4x.rimusic.utils.textoutlineKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@UnstableApi
@ExperimentalFoundationApi
@Composable
fun InfoAlbumAndArtistModern(
    binder: PlayerServiceModern.Binder,
    navController: NavController,
    albumId: String?,
    media: UiMedia,
    mediaId: String,
    title: String?,
    likedAt: Long?,
    artistIds: List<Info>?,
    isExplicit: Boolean,
    artist: String?,
    onCollapse: () -> Unit,
    disableScrollingText: Boolean = false
) {
    val colorPaletteMode by rememberPreference(colorPaletteModeKey, ColorPaletteMode.Dark)
    val playerControlsType by rememberPreference(playerControlsTypeKey, PlayerControlsType.Essential)
    var showthumbnail by rememberPreference(showthumbnailKey, true)
    var effectRotationEnabled by rememberPreference(effectRotationKey, true)
    var isRotated by rememberSaveable { mutableStateOf(false) }
    var showSelectDialog by remember { mutableStateOf(false) }
    val playerBackgroundColors by rememberPreference(playerBackgroundColorsKey,PlayerBackgroundColors.BlurredCoverColor)
    val playerInfoShowIcon by rememberPreference(playerInfoShowIconsKey, true)
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
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {

            if (playerInfoShowIcon) {
                IconButton(
                    icon = if (albumId == null && !media.isLocal) R.drawable.logo_youtube else R.drawable.album,
                    color = if (albumId == null) colorPalette().textDisabled else colorPalette().text,
                    enabled = albumId != null,
                    onClick = {
                        if (albumId != null) {
                            //onGoToAlbum(albumId)
                            navController.navigate(route = "${NavRoutes.album.name}/${albumId}")
                            //layoutState.collapseSoft()
                            onCollapse()
                        }
                    },
                    modifier = Modifier
                        .size(26.dp)
                )

                Spacer(
                    modifier = Modifier
                        .width(8.dp)
                )
            }

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


            val textoutline by rememberPreference(textoutlineKey, false)

            if (!disableScrollingText) modifierTitle = modifierTitle.basicMarquee()
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                 .weight(1f)
            ) {
                if ( isExplicit )
                    IconButton(
                        icon = R.drawable.explicit,
                        color = colorPalette().text,
                        enabled = true,
                        onClick = {},
                        modifier = Modifier
                            .size(18.dp)
                    )
             Box(

             ){
                BasicText(
                    text = cleanPrefix(title ?: ""),
                    style = TextStyle(
                        color = if (albumId == null)
                            /*if (showthumbnail) colorPalette().textDisabled else if (colorPaletteMode == ColorPaletteMode.Light) colorPalette().textDisabled.copy(0.35f).compositeOver(Color.Black) else colorPalette().textDisabled.copy(0.35f).compositeOver(Color.White)
                        else colorPalette().text,*/
                            if (colorPaletteMode == ColorPaletteMode.Light || (colorPaletteMode == ColorPaletteMode.System && (!isSystemInDarkTheme()))) colorPalette().textDisabled.copy(0.35f).compositeOver(Color.Black) else colorPalette().textDisabled.copy(0.35f).compositeOver(Color.White)
                        else colorPalette().text,
                        fontStyle = typography().l.bold.fontStyle,
                        fontWeight = typography().l.bold.fontWeight,
                        fontSize = typography().l.bold.fontSize,
                        fontFamily = typography().l.bold.fontFamily
                    ),
                    maxLines = 1,
                    modifier = modifierTitle
                )
                BasicText(
                    text = cleanPrefix(title ?: ""),
                    style = TextStyle(
                        drawStyle = Stroke(width = 1.5f, join = StrokeJoin.Round),
                        color = if (!textoutline) Color.Transparent else if (colorPaletteMode == ColorPaletteMode.Light || (colorPaletteMode == ColorPaletteMode.System && (!isSystemInDarkTheme()))) Color.White.copy(0.5f)
                        else Color.Black,
                        fontStyle = typography().l.bold.fontStyle,
                        fontWeight = typography().l.bold.fontWeight,
                        fontSize = typography().l.bold.fontSize,
                        fontFamily = typography().l.bold.fontFamily
                    ),
                    maxLines = 1,
                    modifier = modifierTitle
                )
                }
            }
            //}
            if (playerControlsType == PlayerControlsType.Modern)
                Box(
                    modifier = Modifier
                        .weight(0.1f)
                ){
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


    Spacer(
        modifier = Modifier
            .height(10.dp)
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
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
                    //onGoToArtist(it)
                    navController.navigate(route = "${NavRoutes.artist.name}/${it}")
                    showSelectDialog = false
                    //layoutState.collapseSoft()
                    onCollapse()
                }
            )


        if (playerInfoShowIcon) {
            IconButton(
                icon = if (artistIds?.isEmpty() == true && !media.isLocal) R.drawable.logo_youtube else R.drawable.artists,
                color = if (artistIds?.isEmpty() == true) colorPalette().textDisabled else colorPalette().text,
                onClick = {
                    if (artistIds?.isNotEmpty() == true && artistIds.size > 1)
                        showSelectDialog = true
                    if (artistIds?.isNotEmpty() == true && artistIds.size == 1) {
                        //onGoToArtist( artistIds[0].id )
                        navController.navigate(route = "${NavRoutes.artist.name}/${artistIds[0].id}")
                        //layoutState.collapseSoft()
                        onCollapse()
                    }
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

        var textoutline by rememberPreference(textoutlineKey, false)
        if (!disableScrollingText) modifierArtist = modifierArtist.basicMarquee()
        Box(

        ) {
            BasicText(
                text = artist ?: "",
                style = TextStyle(
                    color = if (albumId == null)
                        /*if (showthumbnail) colorPalette().textDisabled else if (colorPaletteMode == ColorPaletteMode.Light) colorPalette().textDisabled.copy(0.35f).compositeOver(Color.Black) else colorPalette().textDisabled.copy(0.35f).compositeOver(Color.White)
                    else colorPalette().text,*/
                        if (colorPaletteMode == ColorPaletteMode.Light || (colorPaletteMode == ColorPaletteMode.System && (!isSystemInDarkTheme()))) colorPalette().textDisabled.copy(0.35f).compositeOver(Color.Black) else colorPalette().textDisabled.copy(0.35f).compositeOver(Color.White)
                    else colorPalette().text,
                    fontStyle = typography().m.bold.fontStyle,
                    fontSize = typography().m.bold.fontSize,
                    fontWeight = typography().m.bold.fontWeight,
                    fontFamily = typography().m.bold.fontFamily
                ),
                maxLines = 1,
                modifier = modifierArtist

            )
            BasicText(
                text = artist ?: "",
                style = TextStyle(
                    drawStyle = Stroke(width = 1.5f, join = StrokeJoin.Round),
                    color = if (!textoutline) Color.Transparent else if (colorPaletteMode == ColorPaletteMode.Light || (colorPaletteMode == ColorPaletteMode.System && (!isSystemInDarkTheme()))) Color.White.copy(0.5f)
                    else Color.Black,
                    fontStyle = typography().m.bold.fontStyle,
                    fontSize = typography().m.bold.fontSize,
                    fontWeight = typography().m.bold.fontWeight,
                    fontFamily = typography().m.bold.fontFamily
                ),
                maxLines = 1,
                modifier = modifierArtist

            )
        }

    }

}


@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ControlsModern(
    binder: PlayerServiceModern.Binder,
    position: Long,
    playbackSpeed: Float,
    shouldBePlaying: Boolean,
    playerPlayButtonType: PlayerPlayButtonType,
    isGradientBackgroundEnabled: Boolean,
    onShowSpeedPlayerDialog: () -> Unit,
) {
    var effectRotationEnabled by rememberPreference(effectRotationKey, true)
    var isRotated by rememberSaveable { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isRotated) 360F else 0f,
        animationSpec = tween(durationMillis = 200), label = ""
    )
    var jumpPrevious by rememberPreference(jumpPreviousKey, "3")

  if (playerPlayButtonType != PlayerPlayButtonType.Disabled) {
      CustomElevatedButton(
          backgroundColor = colorPalette().background2.copy(0.95f),
          onClick = {},
          modifier = Modifier
              .size(55.dp)
              .doubleShadowDrop(RoundedCornerShape(8.dp), 4.dp, 8.dp)
              .clip(RoundedCornerShape(8.dp))
              .combinedClickable(
                  indication = ripple(bounded = true),
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

      ) {
          Image(
              painter = painterResource(R.drawable.play_skip_back),
              contentDescription = null,
              colorFilter = ColorFilter.tint(colorPalette().text),
              modifier = Modifier
                  .padding(10.dp)
                  .size(26.dp)
                  .rotate(rotationAngle)
          )
      }

      if (playerPlayButtonType == PlayerPlayButtonType.CircularRibbed){
          Box(
             contentAlignment = Alignment.Center,
             modifier = Modifier
                 .clip(CircleShape)
                 .combinedClickable(
                     indication = ripple(bounded = false),
                     interactionSource = remember { MutableInteractionSource() },
                     onClick = {
                         if (shouldBePlaying) {
                             //binder.player.pause()
                             binder.callPause({ binder.player.pause() } )
                         } else {
                             if (binder.player.playbackState == Player.STATE_IDLE) {
                                 binder.player.prepare()
                             }
                             binder.player.play()
                         }
                         if (effectRotationEnabled) isRotated = !isRotated
                     },
                     onLongClick = onShowSpeedPlayerDialog
                 )
                 .bounceClick()

          ) {
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                  Icon(
                      painter = painterResource(R.drawable.a13shape),
                      contentDescription = null,
                      modifier = Modifier
                          .offset(x = (0).dp, y = (0).dp)
                          .blur(7.dp)
                          .size(115.dp)
                          .rotate(rotationAngle),
                      tint = Color.Black.copy(0.75f)
                  )
              }
              Image(
                  painter = painterResource(R.drawable.a13shape),
                  colorFilter = ColorFilter.tint(colorPalette().background2.copy(0.95f)),
                  modifier = Modifier
                      .rotate(rotationAngle)
                      .dropShadow(
                          CircleShape,
                          if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) Color.Black.copy(0.75f) else Color.Transparent,
                          6.dp,
                          0.dp,
                          0.dp,
                          0.dp
                      )
                      .size(100.dp),
                  contentDescription = "Background Image",
                  contentScale = ContentScale.Fit
              )
              Image(
                  painter = painterResource(if (shouldBePlaying) R.drawable.pause else R.drawable.play),
                  contentDescription = null,
                  colorFilter = ColorFilter.tint(colorPalette().text),  //ColorFilter.tint(colorPalette().collapsedPlayerProgressBar),
                  modifier = Modifier
                      .rotate(rotationAngle)
                      .align(Alignment.Center)
                      .size(30.dp)
              )
          }
      }
      else {
          CustomElevatedButton(
              backgroundColor = colorPalette().background2.copy(0.95f),
              onClick = {},
              modifier = Modifier
                  .doubleShadowDrop(RoundedCornerShape(8.dp), 4.dp, 8.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .combinedClickable(
                      indication = ripple(bounded = true),
                      interactionSource = remember { MutableInteractionSource() },
                      onClick = {
                          if (shouldBePlaying) {
                              //binder.player.pause()
                              binder.callPause({ binder.player.pause() } )
                          } else {
                              if (binder.player.playbackState == Player.STATE_IDLE) {
                                  binder.player.prepare()
                              }
                              binder.player.play()
                          }
                          if (effectRotationEnabled) isRotated = !isRotated
                      },
                      onLongClick = onShowSpeedPlayerDialog
                  )
                  .bounceClick()
                  .width(playerPlayButtonType.width.dp)
                  .height(playerPlayButtonType.height.dp)

          ) {
              /*
        if (playerPlayButtonType == PlayerPlayButtonType.CircularRibbed)
            Image(
                painter = painterResource(R.drawable.a13shape),
                colorFilter = ColorFilter.tint(
                    when (colorPaletteName) {
                        ColorPaletteName.PureBlack, ColorPaletteName.ModernBlack -> colorPalette().background4
                        else -> if (isGradientBackgroundEnabled) colorPalette().background1
                        else colorPalette().background2
                    }
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(rotationAngle),
                contentDescription = "Background Image",
                contentScale = ContentScale.Fit
            )
         */

              Image(
                  painter = painterResource(if (shouldBePlaying) R.drawable.pause else R.drawable.play),
                  contentDescription = null,
                  colorFilter = ColorFilter.tint(colorPalette().text),  //ColorFilter.tint(colorPalette().collapsedPlayerProgressBar),
                  modifier = Modifier
                      .rotate(rotationAngle)
                      .align(Alignment.Center)
                      .size(30.dp)
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
      }

    CustomElevatedButton(
        backgroundColor = colorPalette().background2.copy(0.95f),
        onClick = {},
        modifier = Modifier
            .size(55.dp)
            .doubleShadowDrop(RoundedCornerShape(8.dp), 4.dp, 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
                indication = ripple(bounded = true),
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    //binder.player.forceSeekToNext()
                    binder.player.playNext()
                    if (effectRotationEnabled) isRotated = !isRotated
                },
                onLongClick = {}
            )
            .clip(RoundedCornerShape(8.dp))

      ) {
          Image(
              painter = painterResource(R.drawable.play_skip_forward),
              contentDescription = null,
              colorFilter = ColorFilter.tint(colorPalette().text),  //ColorFilter.tint(colorPalette().collapsedPlayerProgressBar),
              modifier = Modifier
                  .padding(10.dp)
                  .size(26.dp)
                  .rotate(rotationAngle)
          )
      }
  }

  if (playerPlayButtonType == PlayerPlayButtonType.Disabled) {

      Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceEvenly,
          modifier = Modifier
              .fillMaxWidth()
      ) {
          Box(

          )
          {
              Icon(
                  painter = painterResource(R.drawable.play_skip_back),
                  contentDescription = null,
                  modifier = Modifier
                      .offset(x = (8).dp, y = (8).dp)
                      .blur(4.dp)
                      .size(38.dp)
                      .rotate(rotationAngle),
                  tint = Color.Black
              )
              Image(
                  painter = painterResource(R.drawable.play_skip_back),
                  contentDescription = null,
                  colorFilter = ColorFilter.tint(colorPalette().accent),
                  modifier = Modifier
                      .padding(10.dp)
                      .size(34.dp)
                      .rotate(rotationAngle)
                      .combinedClickable(
                          interactionSource = null,
                          indication = null,
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
              )
          }

          Box(
              modifier = Modifier
                .bounceClick()
          ) {
              Icon(
                  painter = painterResource(if (shouldBePlaying) R.drawable.pause else R.drawable.play),
                  contentDescription = null,
                  modifier = Modifier
                      .offset(x = (0).dp, y = (0).dp)
                      .blur(7.dp)
                      .size(54.dp)
                      .rotate(rotationAngle),
                  tint = Color.Black
              )
              Image(
                  painter = painterResource(if (shouldBePlaying) R.drawable.pause else R.drawable.play),
                  contentDescription = null,
                  colorFilter = ColorFilter.tint(colorPalette().accent),  //ColorFilter.tint(colorPalette().collapsedPlayerProgressBar),
                  modifier = Modifier
                      .rotate(rotationAngle)
                      .size(44.dp)
                      .align(Alignment.Center)
                      .combinedClickable(
                          interactionSource = null,
                          indication = null,
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
              )

          }

          Box(


          ) {
              Icon(
                  painter = painterResource(R.drawable.play_skip_forward),
                  contentDescription = null,
                  modifier = Modifier
                      .offset(x = (8).dp, y = (8).dp)
                      .blur(4.dp)
                      .size(38.dp)
                      .rotate(rotationAngle),
                  tint = Color.Black
              )
              Image(
                  painter = painterResource(R.drawable.play_skip_forward),
                  contentDescription = null,
                  colorFilter = ColorFilter.tint(colorPalette().accent),  //ColorFilter.tint(colorPalette().collapsedPlayerProgressBar),
                  modifier = Modifier
                      .padding(10.dp)
                      .size(34.dp)
                      .rotate(rotationAngle)
                      .combinedClickable(
                          interactionSource = null,
                          indication = null,
                          onClick = {
                              //binder.player.forceSeekToNext()
                              binder.player.playNext()
                              if (effectRotationEnabled) isRotated = !isRotated
                          },
                          onLongClick = {}
                      )
              )
          }
      }
  }



}