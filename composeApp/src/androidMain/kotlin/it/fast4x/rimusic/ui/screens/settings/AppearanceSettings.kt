package it.fast4x.rimusic.ui.screens.settings

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.BackgroundProgress
import it.fast4x.rimusic.enums.CarouselSize
import it.fast4x.rimusic.enums.IconLikeType
import it.fast4x.rimusic.enums.MiniPlayerType
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.enums.NotificationButtons
import it.fast4x.rimusic.enums.PlayerBackgroundColors
import it.fast4x.rimusic.enums.PlayerControlsType
import it.fast4x.rimusic.enums.PlayerInfoType
import it.fast4x.rimusic.enums.PlayerPlayButtonType
import it.fast4x.rimusic.enums.PlayerThumbnailSize
import it.fast4x.rimusic.enums.PlayerTimelineSize
import it.fast4x.rimusic.enums.PlayerTimelineType
import it.fast4x.rimusic.enums.PlayerType
import it.fast4x.rimusic.enums.PrevNextSongs
import it.fast4x.rimusic.enums.QueueType
import it.fast4x.rimusic.enums.SongsNumber
import it.fast4x.rimusic.enums.ThumbnailCoverType
import it.fast4x.rimusic.enums.ThumbnailRoundness
import it.fast4x.rimusic.enums.ThumbnailType
import it.fast4x.rimusic.enums.WallpaperType
import it.fast4x.rimusic.ui.components.themed.HeaderWithIcon
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.utils.RestartPlayerService
import it.fast4x.rimusic.utils.actionExpandedKey
import it.fast4x.rimusic.utils.actionspacedevenlyKey
import it.fast4x.rimusic.utils.backgroundProgressKey
import it.fast4x.rimusic.utils.blackgradientKey
import it.fast4x.rimusic.utils.bottomgradientKey
import it.fast4x.rimusic.utils.buttonzoomoutKey
import it.fast4x.rimusic.utils.carouselKey
import it.fast4x.rimusic.utils.carouselSizeKey
import it.fast4x.rimusic.utils.clickOnLyricsTextKey
import it.fast4x.rimusic.utils.controlsExpandedKey
import it.fast4x.rimusic.utils.coverThumbnailAnimationKey
import it.fast4x.rimusic.utils.disablePlayerHorizontalSwipeKey
import it.fast4x.rimusic.utils.disableScrollingTextKey
import it.fast4x.rimusic.utils.effectRotationKey
import it.fast4x.rimusic.utils.enableWallpaperKey
import it.fast4x.rimusic.utils.expandedplayerKey
import it.fast4x.rimusic.utils.expandedplayertoggleKey
import it.fast4x.rimusic.utils.fadingedgeKey
import it.fast4x.rimusic.utils.iconLikeTypeKey
import it.fast4x.rimusic.utils.isAtLeastAndroid7
import it.fast4x.rimusic.utils.isLandscape
import it.fast4x.rimusic.utils.isShowingThumbnailInLockscreenKey
import it.fast4x.rimusic.utils.keepPlayerMinimizedKey
import it.fast4x.rimusic.utils.lastPlayerPlayButtonTypeKey
import it.fast4x.rimusic.utils.miniPlayerTypeKey
import it.fast4x.rimusic.utils.miniQueueExpandedKey
import it.fast4x.rimusic.utils.navigationBarPositionKey
import it.fast4x.rimusic.utils.noblurKey
import it.fast4x.rimusic.utils.notificationPlayerFirstIconKey
import it.fast4x.rimusic.utils.notificationPlayerSecondIconKey
import it.fast4x.rimusic.utils.playerBackgroundColorsKey
import it.fast4x.rimusic.utils.playerControlsTypeKey
import it.fast4x.rimusic.utils.playerEnableLyricsPopupMessageKey
import it.fast4x.rimusic.utils.playerInfoShowIconsKey
import it.fast4x.rimusic.utils.playerInfoTypeKey
import it.fast4x.rimusic.utils.playerPlayButtonTypeKey
import it.fast4x.rimusic.utils.playerSwapControlsWithTimelineKey
import it.fast4x.rimusic.utils.playerThumbnailSizeKey
import it.fast4x.rimusic.utils.playerTimelineSizeKey
import it.fast4x.rimusic.utils.playerTimelineTypeKey
import it.fast4x.rimusic.utils.playerTypeKey
import it.fast4x.rimusic.utils.prevNextSongsKey
import it.fast4x.rimusic.utils.queueDurationExpandedKey
import it.fast4x.rimusic.utils.queueTypeKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.showBackgroundLyricsKey
import it.fast4x.rimusic.utils.showButtonPlayerAddToPlaylistKey
import it.fast4x.rimusic.utils.showButtonPlayerArrowKey
import it.fast4x.rimusic.utils.showButtonPlayerDiscoverKey
import it.fast4x.rimusic.utils.showButtonPlayerDownloadKey
import it.fast4x.rimusic.utils.showButtonPlayerLoopKey
import it.fast4x.rimusic.utils.showButtonPlayerLyricsKey
import it.fast4x.rimusic.utils.showButtonPlayerMenuKey
import it.fast4x.rimusic.utils.showButtonPlayerShuffleKey
import it.fast4x.rimusic.utils.showButtonPlayerSleepTimerKey
import it.fast4x.rimusic.utils.showButtonPlayerStartRadioKey
import it.fast4x.rimusic.utils.showButtonPlayerSystemEqualizerKey
import it.fast4x.rimusic.utils.showButtonPlayerVideoKey
import it.fast4x.rimusic.utils.showDownloadButtonBackgroundPlayerKey
import it.fast4x.rimusic.utils.showLikeButtonBackgroundPlayerKey
import it.fast4x.rimusic.utils.showNextSongsInPlayerKey
import it.fast4x.rimusic.utils.showRemainingSongTimeKey
import it.fast4x.rimusic.utils.showTopActionsBarKey
import it.fast4x.rimusic.utils.showTotalTimeQueueKey
import it.fast4x.rimusic.utils.showCoverThumbnailAnimationKey
import it.fast4x.rimusic.utils.showalbumcoverKey
import it.fast4x.rimusic.utils.showlyricsthumbnailKey
import it.fast4x.rimusic.utils.showsongsKey
import it.fast4x.rimusic.utils.showthumbnailKey
import it.fast4x.rimusic.utils.showvisthumbnailKey
import it.fast4x.rimusic.utils.statsExpandedKey
import it.fast4x.rimusic.utils.statsfornerdsKey
import it.fast4x.rimusic.utils.swipeUpQueueKey
import it.fast4x.rimusic.utils.tapqueueKey
import it.fast4x.rimusic.utils.textoutlineKey
import it.fast4x.rimusic.utils.thumbnailRoundnessKey
import it.fast4x.rimusic.utils.thumbnailTapEnabledKey
import it.fast4x.rimusic.utils.thumbnailTypeKey
import it.fast4x.rimusic.utils.thumbnailpauseKey
import it.fast4x.rimusic.utils.timelineExpandedKey
import it.fast4x.rimusic.utils.titleExpandedKey
import it.fast4x.rimusic.utils.transparentBackgroundPlayerActionBarKey
import it.fast4x.rimusic.utils.transparentbarKey
import it.fast4x.rimusic.utils.visualizerEnabledKey
import it.fast4x.rimusic.utils.wallpaperTypeKey
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.enums.AnimatedGradient
import it.fast4x.rimusic.enums.BuiltInPlaylist
import it.fast4x.rimusic.enums.ColorPaletteMode
import it.fast4x.rimusic.enums.ColorPaletteName
import it.fast4x.rimusic.enums.PopupType
import it.fast4x.rimusic.enums.SwipeAnimationNoThumbnail
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.getUiType
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.typography
import it.fast4x.rimusic.ui.components.themed.Search
import it.fast4x.rimusic.ui.components.themed.AppearancePresetDialog
import it.fast4x.rimusic.ui.components.themed.InputTextDialog
import it.fast4x.rimusic.utils.albumCoverRotationKey
import it.fast4x.rimusic.utils.animatedGradientKey
import it.fast4x.rimusic.utils.blurStrengthKey
import it.fast4x.rimusic.utils.colorPaletteModeKey
import it.fast4x.rimusic.utils.colorPaletteNameKey
import it.fast4x.rimusic.utils.playerThumbnailSizeLKey
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.swipeAnimationsNoThumbnailKey
import it.fast4x.rimusic.utils.thumbnailFadeExKey
import it.fast4x.rimusic.utils.thumbnailFadeKey
import it.fast4x.rimusic.utils.thumbnailSpacingKey
import it.fast4x.rimusic.utils.topPaddingKey
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun DefaultAppearanceSettings() {
    var isShowingThumbnailInLockscreen by rememberPreference(
        isShowingThumbnailInLockscreenKey,
        true
    )
    isShowingThumbnailInLockscreen = true
    var showthumbnail by rememberPreference(showthumbnailKey, true)
    showthumbnail = true
    var transparentbar by rememberPreference(transparentbarKey, true)
    transparentbar = true
    var blackgradient by rememberPreference(blackgradientKey, false)
    blackgradient = false
    var showlyricsthumbnail by rememberPreference(showlyricsthumbnailKey, false)
    showlyricsthumbnail = false
    var playerPlayButtonType by rememberPreference(
        playerPlayButtonTypeKey,
        PlayerPlayButtonType.Disabled
    )
    playerPlayButtonType = PlayerPlayButtonType.Disabled
    var bottomgradient by rememberPreference(bottomgradientKey, false)
    bottomgradient = false
    var textoutline by rememberPreference(textoutlineKey, false)
    textoutline = false
    var lastPlayerPlayButtonType by rememberPreference(
        lastPlayerPlayButtonTypeKey,
        PlayerPlayButtonType.Rectangular
    )
    lastPlayerPlayButtonType = PlayerPlayButtonType.Rectangular
    var disablePlayerHorizontalSwipe by rememberPreference(disablePlayerHorizontalSwipeKey, false)
    disablePlayerHorizontalSwipe = false
    var disableScrollingText by rememberPreference(disableScrollingTextKey, false)
    disableScrollingText = false
    var showLikeButtonBackgroundPlayer by rememberPreference(
        showLikeButtonBackgroundPlayerKey,
        true
    )
    showLikeButtonBackgroundPlayer = true
    var showDownloadButtonBackgroundPlayer by rememberPreference(
        showDownloadButtonBackgroundPlayerKey,
        true
    )
    showDownloadButtonBackgroundPlayer = true
    var visualizerEnabled by rememberPreference(visualizerEnabledKey, false)
    visualizerEnabled = false
    var playerTimelineType by rememberPreference(playerTimelineTypeKey, PlayerTimelineType.FakeAudioBar)
    playerTimelineType = PlayerTimelineType.FakeAudioBar
    var playerThumbnailSize by rememberPreference(
        playerThumbnailSizeKey,
        PlayerThumbnailSize.Biggest
    )
    playerThumbnailSize = PlayerThumbnailSize.Biggest
    var playerTimelineSize by rememberPreference(
        playerTimelineSizeKey,
        PlayerTimelineSize.Biggest
    )
    playerTimelineSize = PlayerTimelineSize.Biggest
    var effectRotationEnabled by rememberPreference(effectRotationKey, true)
    effectRotationEnabled = true
    var thumbnailTapEnabled by rememberPreference(thumbnailTapEnabledKey, true)
    thumbnailTapEnabled = true
    var showButtonPlayerAddToPlaylist by rememberPreference(showButtonPlayerAddToPlaylistKey, true)
    showButtonPlayerAddToPlaylist = true
    var showButtonPlayerArrow by rememberPreference(showButtonPlayerArrowKey, true)
    showButtonPlayerArrow = false
    var showButtonPlayerDownload by rememberPreference(showButtonPlayerDownloadKey, true)
    showButtonPlayerDownload = true
    var showButtonPlayerLoop by rememberPreference(showButtonPlayerLoopKey, true)
    showButtonPlayerLoop = true
    var showButtonPlayerLyrics by rememberPreference(showButtonPlayerLyricsKey, true)
    showButtonPlayerLyrics = true
    var expandedplayertoggle by rememberPreference(expandedplayertoggleKey, true)
    expandedplayertoggle = true
    var showButtonPlayerShuffle by rememberPreference(showButtonPlayerShuffleKey, true)
    showButtonPlayerShuffle = true
    var showButtonPlayerSleepTimer by rememberPreference(showButtonPlayerSleepTimerKey, false)
    showButtonPlayerSleepTimer = false
    var showButtonPlayerMenu by rememberPreference(showButtonPlayerMenuKey, false)
    showButtonPlayerMenu = false
    var showButtonPlayerSystemEqualizer by rememberPreference(
        showButtonPlayerSystemEqualizerKey,
        false
    )
    showButtonPlayerSystemEqualizer = false
    var showButtonPlayerDiscover by rememberPreference(showButtonPlayerDiscoverKey, false)
    showButtonPlayerDiscover = false
    var showButtonPlayerVideo by rememberPreference(showButtonPlayerVideoKey, false)
    showButtonPlayerVideo = false
    var navigationBarPosition by rememberPreference(
        navigationBarPositionKey,
        NavigationBarPosition.Bottom
    )
    if (getUiType()==UiType.RiMusic)
        navigationBarPosition = NavigationBarPosition.Bottom
    else
        navigationBarPosition = NavigationBarPosition.Left

    var showTotalTimeQueue by rememberPreference(showTotalTimeQueueKey, true)
    showTotalTimeQueue = true
    var backgroundProgress by rememberPreference(
        backgroundProgressKey,
        BackgroundProgress.MiniPlayer
    )
    backgroundProgress = BackgroundProgress.MiniPlayer
    var showNextSongsInPlayer by rememberPreference(showNextSongsInPlayerKey, false)
    showNextSongsInPlayer = false
    var showRemainingSongTime by rememberPreference(showRemainingSongTimeKey, true)
    showRemainingSongTime = true
    var clickLyricsText by rememberPreference(clickOnLyricsTextKey, true)
    clickLyricsText = true
    var showBackgroundLyrics by rememberPreference(showBackgroundLyricsKey, false)
    showBackgroundLyrics = false
    var thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )
    thumbnailRoundness = ThumbnailRoundness.Heavy
    var miniPlayerType by rememberPreference(
        miniPlayerTypeKey,
        MiniPlayerType.Modern
    )
    miniPlayerType = MiniPlayerType.Modern
    var playerBackgroundColors by rememberPreference(
        playerBackgroundColorsKey,
        PlayerBackgroundColors.BlurredCoverColor
    )
    playerBackgroundColors = PlayerBackgroundColors.BlurredCoverColor
    var showTopActionsBar by rememberPreference(showTopActionsBarKey, true)
    showTopActionsBar = true
    var playerControlsType by rememberPreference(playerControlsTypeKey, PlayerControlsType.Essential)
    playerControlsType = PlayerControlsType.Modern
    var playerInfoType by rememberPreference(playerInfoTypeKey, PlayerInfoType.Essential)
    playerInfoType = PlayerInfoType.Modern
    var transparentBackgroundActionBarPlayer by rememberPreference(
        transparentBackgroundPlayerActionBarKey,
        false
    )
    transparentBackgroundActionBarPlayer = false
    var iconLikeType by rememberPreference(iconLikeTypeKey, IconLikeType.Essential)
    iconLikeType = IconLikeType.Essential
    var playerSwapControlsWithTimeline by rememberPreference(
        playerSwapControlsWithTimelineKey,
        false
    )
    playerSwapControlsWithTimeline = false
    var playerEnableLyricsPopupMessage by rememberPreference(
        playerEnableLyricsPopupMessageKey,
        true
    )
    playerEnableLyricsPopupMessage = true
    var actionspacedevenly by rememberPreference(actionspacedevenlyKey, false)
    actionspacedevenly = false
    var thumbnailType by rememberPreference(thumbnailTypeKey, ThumbnailType.Modern)
    thumbnailType = ThumbnailType.Modern
    var showvisthumbnail by rememberPreference(showvisthumbnailKey, false)
    showvisthumbnail = false
    var buttonzoomout by rememberPreference(buttonzoomoutKey, false)
    buttonzoomout = false
    var thumbnailpause by rememberPreference(thumbnailpauseKey, false)
    thumbnailpause = false
    var showsongs by rememberPreference(showsongsKey, SongsNumber.`2`)
    showsongs = SongsNumber.`2`
    var showalbumcover by rememberPreference(showalbumcoverKey, true)
    showalbumcover = true
    var prevNextSongs by rememberPreference(prevNextSongsKey, PrevNextSongs.twosongs)
    prevNextSongs = PrevNextSongs.twosongs
    var tapqueue by rememberPreference(tapqueueKey, true)
    tapqueue = true
    var swipeUpQueue by rememberPreference(swipeUpQueueKey, true)
    swipeUpQueue = true
    var statsfornerds by rememberPreference(statsfornerdsKey, false)
    statsfornerds = false
    var playerType by rememberPreference(playerTypeKey, PlayerType.Essential)
    playerType = PlayerType.Essential
    var queueType by rememberPreference(queueTypeKey, QueueType.Essential)
    queueType = QueueType.Essential
    var noblur by rememberPreference(noblurKey, true)
    noblur = true
    var fadingedge by rememberPreference(fadingedgeKey, false)
    fadingedge = false
    var carousel by rememberPreference(carouselKey, true)
    carousel = true
    var carouselSize by rememberPreference(carouselSizeKey, CarouselSize.Biggest)
    carouselSize = CarouselSize.Biggest
    var keepPlayerMinimized by rememberPreference(keepPlayerMinimizedKey,false)
    keepPlayerMinimized = false
    var playerInfoShowIcons by rememberPreference(playerInfoShowIconsKey, true)
    playerInfoShowIcons = true
}

@ExperimentalAnimationApi
@UnstableApi
@Composable
fun AppearanceSettings(
    navController: NavController,
) {

//    var isShowingThumbnailInLockscreen by rememberPreference(
//        isShowingThumbnailInLockscreenKey,
//        true
//    )

    var showthumbnail by rememberPreference(showthumbnailKey, true)
    var transparentbar by rememberPreference(transparentbarKey, true)
    var blackgradient by rememberPreference(blackgradientKey, false)
    var showlyricsthumbnail by rememberPreference(showlyricsthumbnailKey, false)
    var expandedplayer by rememberPreference(expandedplayerKey, false)
    var playerPlayButtonType by rememberPreference(
        playerPlayButtonTypeKey,
        PlayerPlayButtonType.Disabled
    )
    var bottomgradient by rememberPreference(bottomgradientKey, false)
    var textoutline by rememberPreference(textoutlineKey, false)

    var lastPlayerPlayButtonType by rememberPreference(
        lastPlayerPlayButtonTypeKey,
        PlayerPlayButtonType.Rectangular
    )
    var disablePlayerHorizontalSwipe by rememberPreference(disablePlayerHorizontalSwipeKey, false)

    var disableScrollingText by rememberPreference(disableScrollingTextKey, false)
    var showLikeButtonBackgroundPlayer by rememberPreference(
        showLikeButtonBackgroundPlayerKey,
        true
    )
    var showDownloadButtonBackgroundPlayer by rememberPreference(
        showDownloadButtonBackgroundPlayerKey,
        true
    )
    var visualizerEnabled by rememberPreference(visualizerEnabledKey, false)
    /*
    var playerVisualizerType by rememberPreference(
        playerVisualizerTypeKey,
        PlayerVisualizerType.Disabled
    )
    */
    var playerTimelineType by rememberPreference(playerTimelineTypeKey, PlayerTimelineType.FakeAudioBar)
    var playerThumbnailSize by rememberPreference(
        playerThumbnailSizeKey,
        PlayerThumbnailSize.Biggest
    )
    var playerThumbnailSizeL by rememberPreference(
        playerThumbnailSizeLKey,
        PlayerThumbnailSize.Biggest
    )
    var playerTimelineSize by rememberPreference(
        playerTimelineSizeKey,
        PlayerTimelineSize.Biggest
    )
    //

    var effectRotationEnabled by rememberPreference(effectRotationKey, true)

    var thumbnailTapEnabled by rememberPreference(thumbnailTapEnabledKey, true)


    var showButtonPlayerAddToPlaylist by rememberPreference(showButtonPlayerAddToPlaylistKey, true)
    var showButtonPlayerArrow by rememberPreference(showButtonPlayerArrowKey, true)
    var showButtonPlayerDownload by rememberPreference(showButtonPlayerDownloadKey, true)
    var showButtonPlayerLoop by rememberPreference(showButtonPlayerLoopKey, true)
    var showButtonPlayerLyrics by rememberPreference(showButtonPlayerLyricsKey, true)
    var expandedplayertoggle by rememberPreference(expandedplayertoggleKey, true)
    var showButtonPlayerShuffle by rememberPreference(showButtonPlayerShuffleKey, true)
    var showButtonPlayerSleepTimer by rememberPreference(showButtonPlayerSleepTimerKey, false)
    var showButtonPlayerMenu by rememberPreference(showButtonPlayerMenuKey, false)
    var showButtonPlayerStartradio by rememberPreference(showButtonPlayerStartRadioKey, false)
    var showButtonPlayerSystemEqualizer by rememberPreference(
        showButtonPlayerSystemEqualizerKey,
        false
    )
    var showButtonPlayerDiscover by rememberPreference(showButtonPlayerDiscoverKey, false)
    var showButtonPlayerVideo by rememberPreference(showButtonPlayerVideoKey, false)

    val navigationBarPosition by rememberPreference(
        navigationBarPositionKey,
        NavigationBarPosition.Bottom
    )

    //var isGradientBackgroundEnabled by rememberPreference(isGradientBackgroundEnabledKey, false)
    var showTotalTimeQueue by rememberPreference(showTotalTimeQueueKey, true)
    var backgroundProgress by rememberPreference(
        backgroundProgressKey,
        BackgroundProgress.MiniPlayer
    )
    var showNextSongsInPlayer by rememberPreference(showNextSongsInPlayerKey, false)
    var showRemainingSongTime by rememberPreference(showRemainingSongTimeKey, true)
    var clickLyricsText by rememberPreference(clickOnLyricsTextKey, true)
    var showBackgroundLyrics by rememberPreference(showBackgroundLyricsKey, false)

    val search = Search.init()

    var thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )

    var miniPlayerType by rememberPreference(
        miniPlayerTypeKey,
        MiniPlayerType.Modern
    )
    var playerBackgroundColors by rememberPreference(
        playerBackgroundColorsKey,
        PlayerBackgroundColors.BlurredCoverColor
    )

    var showTopActionsBar by rememberPreference(showTopActionsBarKey, true)
    var playerControlsType by rememberPreference(playerControlsTypeKey, PlayerControlsType.Essential)
    var playerInfoType by rememberPreference(playerInfoTypeKey, PlayerInfoType.Essential)
    var transparentBackgroundActionBarPlayer by rememberPreference(
        transparentBackgroundPlayerActionBarKey,
        false
    )
    var iconLikeType by rememberPreference(iconLikeTypeKey, IconLikeType.Essential)
    var playerSwapControlsWithTimeline by rememberPreference(
        playerSwapControlsWithTimelineKey,
        false
    )
    var playerEnableLyricsPopupMessage by rememberPreference(
        playerEnableLyricsPopupMessageKey,
        true
    )
    var actionspacedevenly by rememberPreference(actionspacedevenlyKey, false)
    var thumbnailType by rememberPreference(thumbnailTypeKey, ThumbnailType.Modern)
    var showvisthumbnail by rememberPreference(showvisthumbnailKey, false)
    var buttonzoomout by rememberPreference(buttonzoomoutKey, false)
    var thumbnailpause by rememberPreference(thumbnailpauseKey, false)
    var showsongs by rememberPreference(showsongsKey, SongsNumber.`2`)
    var showalbumcover by rememberPreference(showalbumcoverKey, true)
    var prevNextSongs by rememberPreference(prevNextSongsKey, PrevNextSongs.twosongs)
    var tapqueue by rememberPreference(tapqueueKey, true)
    var swipeUpQueue by rememberPreference(swipeUpQueueKey, true)
    var statsfornerds by rememberPreference(statsfornerdsKey, false)

    var playerType by rememberPreference(playerTypeKey, PlayerType.Essential)
    var queueType by rememberPreference(queueTypeKey, QueueType.Essential)
    var noblur by rememberPreference(noblurKey, true)
    var fadingedge by rememberPreference(fadingedgeKey, false)
    var carousel by rememberPreference(carouselKey, true)
    var carouselSize by rememberPreference(carouselSizeKey, CarouselSize.Biggest)
    var keepPlayerMinimized by rememberPreference(keepPlayerMinimizedKey,false)
    var playerInfoShowIcons by rememberPreference(playerInfoShowIconsKey, true)
    var queueDurationExpanded by rememberPreference(queueDurationExpandedKey, true)
    var titleExpanded by rememberPreference(titleExpandedKey, true)
    var timelineExpanded by rememberPreference(timelineExpandedKey, true)
    var controlsExpanded by rememberPreference(controlsExpandedKey, true)
    var miniQueueExpanded by rememberPreference(miniQueueExpandedKey, true)
    var statsExpanded by rememberPreference(statsExpandedKey, true)
    var actionExpanded by rememberPreference(actionExpandedKey, true)
    var restartService by rememberSaveable { mutableStateOf(false) }
    var showCoverThumbnailAnimation by rememberPreference(showCoverThumbnailAnimationKey, false)
    var coverThumbnailAnimation by rememberPreference(coverThumbnailAnimationKey, ThumbnailCoverType.Vinyl)

    var notificationPlayerFirstIcon by rememberPreference(notificationPlayerFirstIconKey, NotificationButtons.Download)
    var notificationPlayerSecondIcon by rememberPreference(notificationPlayerSecondIconKey, NotificationButtons.Favorites)
    var enableWallpaper by rememberPreference(enableWallpaperKey, false)
    var wallpaperType by rememberPreference(wallpaperTypeKey, WallpaperType.Lockscreen)
    var topPadding by rememberPreference(topPaddingKey, true)
    var animatedGradient by rememberPreference(
        animatedGradientKey,
        AnimatedGradient.Linear
    )
    var appearanceChooser by remember{ mutableStateOf(false)}
    var albumCoverRotation by rememberPreference(albumCoverRotationKey, false)

    var blurStrength by rememberPreference(blurStrengthKey, 25f)
    var thumbnailFadeEx  by rememberPreference(thumbnailFadeExKey, 5f)
    var thumbnailFade  by rememberPreference(thumbnailFadeKey, 5f)
    var thumbnailSpacing  by rememberPreference(thumbnailSpacingKey, 0f)
    var colorPaletteName by rememberPreference(colorPaletteNameKey, ColorPaletteName.Dynamic)
    var colorPaletteMode by rememberPreference(colorPaletteModeKey, ColorPaletteMode.Dark)
    var swipeAnimationNoThumbnail by rememberPreference(swipeAnimationsNoThumbnailKey, SwipeAnimationNoThumbnail.Sliding)

    var appearanceFilename by remember {
        mutableStateOf("")
    }
    val context = LocalContext.current
    val exportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            context.applicationContext.contentResolver.openOutputStream(uri)
                ?.use { outputStream ->
                    csvWriter().open(outputStream){
                        writeRow("SettingsType", "Name", "Parameter", "Value")
                        writeRow("Appearance", appearanceFilename, "albumCoverRotation", albumCoverRotation)
                        writeRow("Appearance", appearanceFilename, "showthumbnail", showthumbnail)
                        writeRow("Appearance", appearanceFilename, "playerBackgroundColors", playerBackgroundColors.ordinal)
                        writeRow("Appearance", appearanceFilename, "thumbnailRoundness", thumbnailRoundness.ordinal)
                        writeRow("Appearance", appearanceFilename, "playerType", playerType.ordinal)
                        writeRow("Appearance", appearanceFilename, "queueType", queueType.ordinal)
                        writeRow("Appearance", appearanceFilename, "noblur", noblur)
                        writeRow("Appearance", appearanceFilename, "fadingedge", fadingedge)
                        writeRow("Appearance", appearanceFilename, "carousel", carousel)
                        writeRow("Appearance", appearanceFilename, "carouselSize", carouselSize.ordinal)
                        writeRow("Appearance", appearanceFilename, "keepPlayerMinimized", keepPlayerMinimized)
                        writeRow("Appearance", appearanceFilename, "playerInfoShowIcons", playerInfoShowIcons)
                        writeRow("Appearance", appearanceFilename, "showTopActionsBar", showTopActionsBar)
                        writeRow("Appearance", appearanceFilename, "playerControlsType", playerControlsType.ordinal)
                        writeRow("Appearance", appearanceFilename, "playerInfoType", playerInfoType.ordinal)
                        writeRow("Appearance", appearanceFilename, "transparentBackgroundActionBarPlayer", transparentBackgroundActionBarPlayer)
                        writeRow("Appearance", appearanceFilename, "iconLikeType", iconLikeType.ordinal)
                        writeRow("Appearance", appearanceFilename, "playerSwapControlsWithTimeline", playerSwapControlsWithTimeline)
                        writeRow("Appearance", appearanceFilename, "playerEnableLyricsPopupMessage", playerEnableLyricsPopupMessage)
                        writeRow("Appearance", appearanceFilename, "actionspacedevenly", actionspacedevenly)
                        writeRow("Appearance", appearanceFilename, "thumbnailType", thumbnailType.ordinal)
                        writeRow("Appearance", appearanceFilename, "showvisthumbnail", showvisthumbnail)
                        writeRow("Appearance", appearanceFilename, "buttonzoomout", buttonzoomout)
                        writeRow("Appearance", appearanceFilename, "thumbnailpause", thumbnailpause)
                        writeRow("Appearance", appearanceFilename, "showsongs", showsongs.ordinal)
                        writeRow("Appearance", appearanceFilename, "showalbumcover", showalbumcover)
                        writeRow("Appearance", appearanceFilename, "prevNextSongs", prevNextSongs.ordinal)
                        writeRow("Appearance", appearanceFilename, "tapqueue", tapqueue)
                        writeRow("Appearance", appearanceFilename, "swipeUpQueue", swipeUpQueue)
                        writeRow("Appearance", appearanceFilename, "statsfornerds", statsfornerds)
                        writeRow("Appearance", appearanceFilename, "transparentbar", transparentbar)
                        writeRow("Appearance", appearanceFilename, "blackgradient", blackgradient)
                        writeRow("Appearance", appearanceFilename, "showlyricsthumbnail", showlyricsthumbnail)
                        writeRow("Appearance", appearanceFilename, "expandedplayer", expandedplayer)
                        writeRow("Appearance", appearanceFilename, "playerPlayButtonType", playerPlayButtonType.ordinal)
                        writeRow("Appearance", appearanceFilename, "bottomgradient", bottomgradient)
                        writeRow("Appearance", appearanceFilename, "textoutline", textoutline)
                        writeRow("Appearance", appearanceFilename, "effectRotationEnabled", effectRotationEnabled)
                        writeRow("Appearance", appearanceFilename, "thumbnailTapEnabled", thumbnailTapEnabled)
                        writeRow("Appearance", appearanceFilename, "showButtonPlayerAddToPlaylist", showButtonPlayerAddToPlaylist)
                        writeRow("Appearance", appearanceFilename, "showButtonPlayerArrow", showButtonPlayerArrow)
                        writeRow("Appearance", appearanceFilename, "showButtonPlayerDownload", showButtonPlayerDownload)
                        writeRow("Appearance", appearanceFilename, "showButtonPlayerLoop", showButtonPlayerLoop)
                        writeRow("Appearance", appearanceFilename, "showButtonPlayerLyrics", showButtonPlayerLyrics)
                        writeRow("Appearance", appearanceFilename, "expandedplayertoggle", expandedplayertoggle)
                        writeRow("Appearance", appearanceFilename, "showButtonPlayerShuffle", showButtonPlayerShuffle)
                        writeRow("Appearance", appearanceFilename, "showButtonPlayerSleepTimer", showButtonPlayerSleepTimer)
                        writeRow("Appearance", appearanceFilename, "showButtonPlayerMenu", showButtonPlayerMenu)
                        writeRow("Appearance", appearanceFilename, "showButtonPlayerStartradio", showButtonPlayerStartradio)
                        writeRow("Appearance", appearanceFilename, "showButtonPlayerSystemEqualizer", showButtonPlayerSystemEqualizer)
                        writeRow("Appearance", appearanceFilename, "showButtonPlayerDiscover", showButtonPlayerDiscover)
                        writeRow("Appearance", appearanceFilename, "showButtonPlayerVideo", showButtonPlayerVideo)
                        writeRow("Appearance", appearanceFilename, "showBackgroundLyrics", showBackgroundLyrics)
                        writeRow("Appearance", appearanceFilename, "showTotalTimeQueue", showTotalTimeQueue)
                        writeRow("Appearance", appearanceFilename, "backgroundProgress", backgroundProgress.ordinal)
                        writeRow("Appearance", appearanceFilename, "showNextSongsInPlayer", showNextSongsInPlayer)
                        writeRow("Appearance", appearanceFilename, "showRemainingSongTime", showRemainingSongTime)
                        writeRow("Appearance", appearanceFilename, "clickLyricsText", clickLyricsText)
                        writeRow("Appearance", appearanceFilename, "queueDurationExpanded", queueDurationExpanded)
                        writeRow("Appearance", appearanceFilename, "titleExpanded", titleExpanded)
                        writeRow("Appearance", appearanceFilename, "timelineExpanded", timelineExpanded)
                        writeRow("Appearance", appearanceFilename, "controlsExpanded", controlsExpanded)
                        writeRow("Appearance", appearanceFilename, "miniQueueExpanded", miniQueueExpanded)
                        writeRow("Appearance", appearanceFilename, "statsExpanded", statsExpanded)
                        writeRow("Appearance", appearanceFilename, "actionExpanded", actionExpanded)
                        writeRow("Appearance", appearanceFilename, "showCoverThumbnailAnimation", showCoverThumbnailAnimation)
                        writeRow("Appearance", appearanceFilename, "coverThumbnailAnimation", coverThumbnailAnimation.ordinal)
                        writeRow("Appearance", appearanceFilename, "notificationPlayerFirstIcon", notificationPlayerFirstIcon.ordinal)
                        writeRow("Appearance", appearanceFilename, "notificationPlayerSecondIcon", notificationPlayerSecondIcon.ordinal)
                        writeRow("Appearance", appearanceFilename, "enableWallpaper", enableWallpaper)
                        writeRow("Appearance", appearanceFilename, "wallpaperType", wallpaperType.ordinal)
                        writeRow("Appearance", appearanceFilename, "topPadding", topPadding)
                        writeRow("Appearance", appearanceFilename, "animatedGradient", animatedGradient.ordinal)
                        writeRow("Appearance", appearanceFilename, "albumCoverRotation", albumCoverRotation)
                        writeRow("Appearance", appearanceFilename, "blurStrength", blurStrength)
                        writeRow("Appearance", appearanceFilename, "thumbnailFadeEx", thumbnailFadeEx)
                        writeRow("Appearance", appearanceFilename, "thumbnailFade", thumbnailFade)
                        writeRow("Appearance", appearanceFilename, "thumbnailSpacing", thumbnailSpacing)
                        writeRow("Appearance", appearanceFilename, "colorPaletteName", colorPaletteName.ordinal)
                        writeRow("Appearance", appearanceFilename, "colorPaletteMode", colorPaletteMode.ordinal)
                        writeRow("Appearance", appearanceFilename, "swipeAnimationNoThumbnail", swipeAnimationNoThumbnail.ordinal)
                        writeRow("Appearance", appearanceFilename, "showLikeButtonBackgroundPlayer", showLikeButtonBackgroundPlayer)
                        writeRow("Appearance", appearanceFilename, "showDownloadButtonBackgroundPlayer", showDownloadButtonBackgroundPlayer)
                        writeRow("Appearance", appearanceFilename, "visualizerEnabled", visualizerEnabled)
                    }
                }

        }

    var isExporting by rememberSaveable {
        mutableStateOf(false)
    }


    if (isExporting) {
        InputTextDialog(
            onDismiss = {
                isExporting = false
            },
            title = "Enter the name of settings export",
            value = "RM_Appearance",
            placeholder = "Enter the name of settings export",
            setValue = { text ->
                appearanceFilename = text
                try {
                    @SuppressLint("SimpleDateFormat")
                    val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")
                    exportLauncher.launch("RMAppearance_${text.take(20)}_${dateFormat.format(
                        Date()
                    )}")
                } catch (e: ActivityNotFoundException) {
                    SmartMessage("Couldn't find an application to create documents",
                        type = PopupType.Warning, context = context)
                }
            }
        )
    }

    val importLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            //requestPermission(activity, "Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED")

            context.applicationContext.contentResolver.openInputStream(uri)
                ?.use { inputStream ->
                    csvReader().open(inputStream) {
                        readAllWithHeaderAsSequence().forEachIndexed { index, row: Map<String, String> ->
                            if (row["SettingsType"] == "Appearance") {
                                println("Import appearance settings parameter ${row["Parameter"]}")
                                when (row["Parameter"]) {
                                    "showthumbnail" -> {
                                        showthumbnail = row["Value"]!!.toBoolean()
                                    }
                                    "playerBackgroundColors" -> {
                                        playerBackgroundColors = PlayerBackgroundColors.entries.toTypedArray()[row["Value"]!!.toInt()]
                                    }
                                    "thumbnailRoundness" -> {
                                        thumbnailRoundness = ThumbnailRoundness.entries.toTypedArray()[row["Value"]!!.toInt()]
                                    }
                                    "playerType" -> {
                                        playerType = PlayerType.entries.toTypedArray()[row["Value"]!!.toInt()]
                                    }
                                    "queueType" -> {
                                        queueType = QueueType.entries.toTypedArray()[row["Value"]!!.toInt()]
                                    }
                                    "noblur" -> {
                                        noblur = row["Value"]!!.toBoolean()
                                    }
                                    "fadingedge" -> {
                                        fadingedge = row["Value"]!!.toBoolean()
                                    }
                                    "carousel" -> {
                                        carousel = row["Value"]!!.toBoolean()
                                    }
                                    "carouselSize" -> {
                                        carouselSize =
                                            CarouselSize.entries.toTypedArray()[row["Value"]!!.toInt()]
                                    }
                                    "keepPlayerMinimized" -> {
                                        keepPlayerMinimized = row["Value"]!!.toBoolean()
                                    }
                                    "playerInfoShowIcons" -> {
                                        playerInfoShowIcons = row["Value"]!!.toBoolean()
                                    }
                                    "showTopActionsBar" -> {
                                        showTopActionsBar = row["Value"]!!.toBoolean()
                                    }
                                    "playerControlsType" -> {
                                        playerControlsType = PlayerControlsType.entries.toTypedArray()[row["Value"]!!.toInt()]
                                    }
                                    "playerInfoType" -> {
                                        playerInfoType = PlayerInfoType.entries.toTypedArray()[row["Value"]!!.toInt()]
                                    }
                                    "transparentBackgroundActionBarPlayer" -> {
                                        transparentBackgroundActionBarPlayer = row["Value"]!!.toBoolean()
                                    }
                                    "iconLikeType" -> {
                                        iconLikeType = IconLikeType.entries.toTypedArray()[row["Value"]!!.toInt()]
                                    }
                                    "playerSwapControlsWithTimeline" -> {
                                        playerSwapControlsWithTimeline = row["Value"]!!.toBoolean()
                                    }
                                    "playerEnableLyricsPopupMessage" -> {
                                        playerEnableLyricsPopupMessage = row["Value"]!!.toBoolean()
                                    }
                                    "actionspacedevenly" -> {
                                        actionspacedevenly = row["Value"]!!.toBoolean()
                                    }
                                    "thumbnailType" -> {
                                        thumbnailType = ThumbnailType.entries.toTypedArray()[row["Value"]!!.toInt()]
                                    }
                                    "showvisthumbnail" -> {
                                        showvisthumbnail = row["Value"]!!.toBoolean()
                                    }
                                    "buttonzoomout" -> {
                                        buttonzoomout = row["Value"]!!.toBoolean()
                                    }
                                    "thumbnailpause" -> {
                                        thumbnailpause = row["Value"]!!.toBoolean()
                                    }
                                    "showsongs" -> {
                                        showsongs = SongsNumber.entries.toTypedArray()[row["Value"]!!.toInt()]
                                    }
                                    "showalbumcover" -> {
                                        showalbumcover = row["Value"]!!.toBoolean()
                                    }
                                    "prevNextSongs" -> {
                                        prevNextSongs = PrevNextSongs.entries.toTypedArray()[row["Value"]!!.toInt()]
                                    }
                                    "tapqueue" -> {
                                        tapqueue = row["Value"]!!.toBoolean()
                                    }
                                    "swipeUpQueue" -> {
                                        swipeUpQueue = row["Value"]!!.toBoolean()
                                    }
                                    "statsfornerds" -> {
                                        statsfornerds = row["Value"]!!.toBoolean()
                                    }
                                    "transparentbar" -> {
                                        transparentbar = row["Value"]!!.toBoolean()
                                    }
                                    "blackgradient" -> {
                                        blackgradient = row["Value"]!!.toBoolean()
                                    }
                                    "showlyricsthumbnail" -> {
                                        showlyricsthumbnail = row["Value"]!!.toBoolean()
                                    }
                                    "expandedplayer" -> {
                                        expandedplayer = row["Value"]!!.toBoolean()
                                    }
                                    "playerPlayButtonType" -> {
                                        playerPlayButtonType = PlayerPlayButtonType.entries.toTypedArray()[row["Value"]!!.toInt()]
                                    }
                                    "bottomgradient" -> {
                                        bottomgradient = row["Value"]!!.toBoolean()
                                    }
                                    "textoutline" -> {
                                        textoutline = row["Value"]!!.toBoolean()
                                    }
                                    "effectRotationEnabled" -> {
                                        effectRotationEnabled = row["Value"]!!.toBoolean()
                                    }
                                    "thumbnailTapEnabled" -> {
                                        thumbnailTapEnabled = row["Value"]!!.toBoolean()
                                    }
                                    "showButtonPlayerAddToPlaylist" -> {
                                        showButtonPlayerAddToPlaylist = row["Value"]!!.toBoolean()
                                    }
                                    "showButtonPlayerArrow" -> {
                                        showButtonPlayerArrow = row["Value"]!!.toBoolean()
                                    }
                                    "showButtonPlayerDownload" -> {
                                        showButtonPlayerDownload = row["Value"]!!.toBoolean()
                                    }
                                    "showButtonPlayerLoop" -> {
                                        showButtonPlayerLoop = row["Value"]!!.toBoolean()
                                    }
                                    "showButtonPlayerLyrics" -> {
                                        showButtonPlayerLyrics = row["Value"]!!.toBoolean()
                                    }
                                    "expandedplayertoggle" -> {
                                        expandedplayertoggle = row["Value"]!!.toBoolean()
                                    }
                                    "showButtonPlayerShuffle" -> {
                                        showButtonPlayerShuffle = row["Value"]!!.toBoolean()
                                    }
                                    "showButtonPlayerSleepTimer" -> {
                                        showButtonPlayerSleepTimer = row["Value"]!!.toBoolean()
                                    }
                                    "showButtonPlayerMenu" -> {
                                        showButtonPlayerMenu = row["Value"]!!.toBoolean()
                                    }
                                    "showButtonPlayerStartradio" -> {
                                        showButtonPlayerStartradio = row["Value"]!!.toBoolean()
                                    }
                                    "showButtonPlayerSystemEqualizer" -> {
                                        showButtonPlayerSystemEqualizer = row["Value"]!!.toBoolean()
                                    }
                                    "showButtonPlayerDiscover" -> {
                                        showButtonPlayerDiscover = row["Value"]!!.toBoolean()
                                    }
                                    "showButtonPlayerVideo" -> {
                                        showButtonPlayerVideo = row["Value"]!!.toBoolean()
                                    }
                                    "showBackgroundLyrics" -> {
                                        showBackgroundLyrics = row["Value"]!!.toBoolean()
                                    }
                                    "showTotalTimeQueue" -> {
                                        showTotalTimeQueue = row["Value"]!!.toBoolean()
                                    }
                                    "backgroundProgress" -> {
                                        backgroundProgress = BackgroundProgress.entries.toTypedArray()[row["Value"]!!.toInt()]
                                    }
                                    "showNextSongsInPlayer" -> {
                                        showNextSongsInPlayer = row["Value"]!!.toBoolean()
                                    }
                                    "showRemainingSongTime" -> {
                                        showRemainingSongTime = row["Value"]!!.toBoolean()
                                    }
                                    "clickLyricsText" -> {
                                        clickLyricsText = row["Value"]!!.toBoolean()
                                    }
                                    "queueDurationExpanded" -> {
                                        queueDurationExpanded = row["Value"]!!.toBoolean()
                                    }
                                    "titleExpanded" -> {
                                        titleExpanded = row["Value"]!!.toBoolean()
                                    }
                                    "timelineExpanded" -> {
                                        timelineExpanded = row["Value"]!!.toBoolean()
                                    }
                                    "controlsExpanded" -> {
                                        controlsExpanded = row["Value"]!!.toBoolean()
                                    }
                                    "miniQueueExpanded" -> {
                                        miniQueueExpanded = row["Value"]!!.toBoolean()
                                    }
                                    "statsExpanded" -> {
                                        statsExpanded = row["Value"]!!.toBoolean()
                                    }
                                    "actionExpanded" -> {
                                        actionExpanded = row["Value"]!!.toBoolean()
                                    }
                                    "showCoverThumbnailAnimation" -> {
                                        showCoverThumbnailAnimation = row["Value"]!!.toBoolean()
                                    }
                                    "coverThumbnailAnimation" -> {
                                        coverThumbnailAnimation =
                                            ThumbnailCoverType.entries.toTypedArray()[row["Value"]!!.toInt()]
                                    }
                                    "notificationPlayerFirstIcon" -> {
                                        notificationPlayerFirstIcon =
                                            NotificationButtons.entries.toTypedArray()[row["Value"]!!.toInt()]
                                    }
                                    "notificationPlayerSecondIcon" -> {
                                        notificationPlayerSecondIcon =
                                            NotificationButtons.entries.toTypedArray()[row["Value"]!!.toInt()]
                                    }
                                    "enableWallpaper" -> {
                                        enableWallpaper = row["Value"]!!.toBoolean()
                                    }
                                    "wallpaperType" -> {
                                        wallpaperType = WallpaperType.entries.toTypedArray()[row["Value"]!!.toInt()]
                                    }
                                    "topPadding" -> {
                                        topPadding = row["Value"]!!.toBoolean()
                                    }
                                    "animatedGradient" -> {
                                        animatedGradient = AnimatedGradient.entries.toTypedArray()[row["Value"]!!.toInt()]
                                    }
                                    "albumCoverRotation" -> {
                                        albumCoverRotation = row["Value"]!!.toBoolean()
                                    }
                                    "blurStrength" -> {
                                        blurStrength = row["Value"]!!.toFloat()
                                    }
                                    "thumbnailFadeEx" -> {
                                        thumbnailFadeEx = row["Value"]!!.toFloat()
                                    }
                                    "thumbnailFade" -> {
                                        thumbnailFade = row["Value"]!!.toFloat()
                                    }
                                    "thumbnailSpacing" -> {
                                        thumbnailSpacing = row["Value"]!!.toFloat()
                                    }
                                    "colorPaletteName" -> {
                                        colorPaletteName =
                                            ColorPaletteName.entries.toTypedArray()[row["Value"]!!.toInt()]
                                    }
                                    "colorPaletteMode" -> {
                                        colorPaletteMode =
                                            ColorPaletteMode.entries.toTypedArray()[row["Value"]!!.toInt()]
                                    }
                                    "swipeAnimationNoThumbnail" -> {
                                        swipeAnimationNoThumbnail =
                                            SwipeAnimationNoThumbnail.entries.toTypedArray()[row["Value"]!!.toInt()]
                                    }
                                    "showLikeButtonBackgroundPlayer" -> {
                                        showLikeButtonBackgroundPlayer = row["Value"]!!.toBoolean()
                                    }
                                    "showDownloadButtonBackgroundPlayer" -> {
                                        showDownloadButtonBackgroundPlayer = row["Value"]!!.toBoolean()
                                    }
                                    "visualizerEnabled" -> {
                                        visualizerEnabled = row["Value"]!!.toBoolean()
                                    }




                                }
                            }

                        }
                    }
                }
        }

    Column(
        modifier = Modifier
            .background(colorPalette().background0)
            //.fillMaxSize()
            .fillMaxHeight()
            .fillMaxWidth(
                if (navigationBarPosition == NavigationBarPosition.Left ||
                    navigationBarPosition == NavigationBarPosition.Top ||
                    navigationBarPosition == NavigationBarPosition.Bottom
                ) 1f
                else Dimensions.contentWidthRightBar
            )
            .verticalScroll(rememberScrollState())
        /*
        .padding(
            LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
                .asPaddingValues()
        )

         */
    ) {
        HeaderWithIcon(
            title = stringResource(R.string.player_appearance),
            iconId = R.drawable.color_palette,
            enabled = false,
            showIcon = true,
            modifier = Modifier,
            onClick = {}
        )

        search.ToolBarButton()
        search.SearchBar( this )

        //SettingsEntryGroupText(stringResource(R.string.user_interface))

        //SettingsGroupSpacer()
        SettingsEntryGroupText(title = stringResource(R.string.player))

        if (playerBackgroundColors != PlayerBackgroundColors.BlurredCoverColor)
            showthumbnail = true
        if (!visualizerEnabled) showvisthumbnail = false
        if (!showthumbnail) {showlyricsthumbnail = false; showvisthumbnail = false}
        if (playerType == PlayerType.Modern) {
            showlyricsthumbnail = false
            showvisthumbnail = false
            thumbnailpause = false
            //keepPlayerMinimized = false
        }


        if (appearanceChooser){
            AppearancePresetDialog(
            onDismiss = {appearanceChooser = false},
            onClick0 = {
                showTopActionsBar = true
                showthumbnail = true
                playerBackgroundColors = PlayerBackgroundColors.BlurredCoverColor
                blurStrength = 50f
                thumbnailRoundness = ThumbnailRoundness.None
                playerInfoType = PlayerInfoType.Essential
                playerTimelineType = PlayerTimelineType.ThinBar
                playerTimelineSize = PlayerTimelineSize.Biggest
                playerControlsType = PlayerControlsType.Essential
                playerPlayButtonType = PlayerPlayButtonType.Disabled
                transparentbar = true
                playerType = PlayerType.Essential
                showlyricsthumbnail = false
                expandedplayer = true
                thumbnailType = ThumbnailType.Modern
                playerThumbnailSize = PlayerThumbnailSize.Big
                showTotalTimeQueue = false
                bottomgradient = true
                showRemainingSongTime = true
                showNextSongsInPlayer = false
                colorPaletteName = ColorPaletteName.Dynamic
                colorPaletteMode = ColorPaletteMode.System
                ///////ACTION BAR BUTTONS////////////////
                transparentBackgroundActionBarPlayer = true
                actionspacedevenly = true
                showButtonPlayerVideo = false
                showButtonPlayerDiscover = false
                showButtonPlayerDownload = false
                showButtonPlayerAddToPlaylist = true
                showButtonPlayerLoop = false
                showButtonPlayerShuffle = true
                showButtonPlayerLyrics = false
                expandedplayertoggle = false
                showButtonPlayerSleepTimer = false
                visualizerEnabled = false
                appearanceChooser = false
                showButtonPlayerArrow = false
                showButtonPlayerStartradio = false
                showButtonPlayerMenu = true
                ///////////////////////////
                appearanceChooser = false
            },
            onClick1 = {
                showTopActionsBar = true
                showthumbnail = true
                playerBackgroundColors = PlayerBackgroundColors.BlurredCoverColor
                blurStrength = 50f
                playerInfoType = PlayerInfoType.Essential
                playerPlayButtonType = PlayerPlayButtonType.Disabled
                playerTimelineType = PlayerTimelineType.ThinBar
                playerControlsType = PlayerControlsType.Essential
                transparentbar = true
                playerType = PlayerType.Modern
                expandedplayer = true
                fadingedge = true
                thumbnailFadeEx = 4f
                thumbnailSpacing = -32f
                thumbnailType = ThumbnailType.Essential
                carouselSize = CarouselSize.Big
                playerThumbnailSize = PlayerThumbnailSize.Biggest
                showTotalTimeQueue = false
                transparentBackgroundActionBarPlayer = true
                showRemainingSongTime = true
                bottomgradient = true
                showlyricsthumbnail = false
                thumbnailRoundness = ThumbnailRoundness.Medium
                showNextSongsInPlayer = true
                colorPaletteName = ColorPaletteName.Dynamic
                colorPaletteMode = ColorPaletteMode.System
                ///////ACTION BAR BUTTONS////////////////
                transparentBackgroundActionBarPlayer = true
                actionspacedevenly = true
                showButtonPlayerVideo = false
                showButtonPlayerDiscover = false
                showButtonPlayerDownload = false
                showButtonPlayerAddToPlaylist = true
                showButtonPlayerLoop = false
                showButtonPlayerShuffle = false
                showButtonPlayerLyrics = false
                expandedplayertoggle = true
                showButtonPlayerSleepTimer = false
                visualizerEnabled = false
                appearanceChooser = false
                showButtonPlayerArrow = false
                showButtonPlayerStartradio = false
                showButtonPlayerMenu = true
                ///////////////////////////
                appearanceChooser = false
            },
            onClick2 = {
                showTopActionsBar = false
                showthumbnail = false
                noblur = true
                topPadding = false
                playerBackgroundColors = PlayerBackgroundColors.BlurredCoverColor
                blurStrength = 50f
                playerPlayButtonType = PlayerPlayButtonType.Disabled
                playerInfoType = PlayerInfoType.Modern
                playerInfoShowIcons = false
                playerTimelineType = PlayerTimelineType.ThinBar
                playerControlsType = PlayerControlsType.Essential
                transparentbar = true
                playerType = PlayerType.Modern
                expandedplayer = true
                showTotalTimeQueue = false
                transparentBackgroundActionBarPlayer = true
                showRemainingSongTime = true
                bottomgradient = true
                showlyricsthumbnail = false
                showNextSongsInPlayer = false
                colorPaletteName = ColorPaletteName.Dynamic
                colorPaletteMode = ColorPaletteMode.System
                ///////ACTION BAR BUTTONS////////////////
                transparentBackgroundActionBarPlayer = true
                actionspacedevenly = true
                showButtonPlayerVideo = false
                showButtonPlayerDiscover = false
                showButtonPlayerDownload = false
                showButtonPlayerAddToPlaylist = false
                showButtonPlayerLoop = false
                showButtonPlayerShuffle = false
                showButtonPlayerLyrics = false
                expandedplayertoggle = false
                showButtonPlayerSleepTimer = false
                visualizerEnabled = false
                appearanceChooser = false
                showButtonPlayerArrow = false
                showButtonPlayerStartradio = false
                showButtonPlayerMenu = true
                ///////////////////////////
                appearanceChooser = false
            },
            onClick3 = {
                showTopActionsBar = false
                topPadding = false
                showthumbnail = true
                playerBackgroundColors = PlayerBackgroundColors.BlurredCoverColor
                blurStrength = 50f
                playerInfoType = PlayerInfoType.Essential
                playerTimelineType = PlayerTimelineType.FakeAudioBar
                playerTimelineSize = PlayerTimelineSize.Biggest
                playerControlsType = PlayerControlsType.Modern
                playerPlayButtonType = PlayerPlayButtonType.Disabled
                colorPaletteName = ColorPaletteName.PureBlack
                transparentbar = false
                playerType = PlayerType.Essential
                expandedplayer = false
                playerThumbnailSize = PlayerThumbnailSize.Expanded
                showTotalTimeQueue = false
                transparentBackgroundActionBarPlayer = true
                showRemainingSongTime = true
                bottomgradient = true
                showlyricsthumbnail = false
                thumbnailType = ThumbnailType.Essential
                thumbnailRoundness = ThumbnailRoundness.Light
                playerType = PlayerType.Modern
                fadingedge = true
                thumbnailFade = 5f
                showNextSongsInPlayer = false
                ///////ACTION BAR BUTTONS////////////////
                transparentBackgroundActionBarPlayer = true
                actionspacedevenly = true
                showButtonPlayerVideo = false
                showButtonPlayerDiscover = false
                showButtonPlayerDownload = false
                showButtonPlayerAddToPlaylist = false
                showButtonPlayerLoop = true
                showButtonPlayerShuffle = true
                showButtonPlayerLyrics = false
                expandedplayertoggle = false
                showButtonPlayerSleepTimer = false
                visualizerEnabled = false
                appearanceChooser = false
                showButtonPlayerArrow = true
                showButtonPlayerStartradio = false
                showButtonPlayerMenu = true
                ///////////////////////////
                appearanceChooser = false
            },
            onClick4 = {
                showTopActionsBar = false
                topPadding = true
                showthumbnail = true
                playerBackgroundColors = PlayerBackgroundColors.AnimatedGradient
                animatedGradient = AnimatedGradient.Linear
                playerInfoType = PlayerInfoType.Essential
                playerTimelineType = PlayerTimelineType.PinBar
                playerTimelineSize = PlayerTimelineSize.Biggest
                playerControlsType = PlayerControlsType.Essential
                playerPlayButtonType = PlayerPlayButtonType.Square
                colorPaletteName = ColorPaletteName.Dynamic
                colorPaletteMode = ColorPaletteMode.PitchBlack
                transparentbar = false
                playerType = PlayerType.Modern
                expandedplayer = false
                playerThumbnailSize = PlayerThumbnailSize.Biggest
                showTotalTimeQueue = false
                transparentBackgroundActionBarPlayer = true
                showRemainingSongTime = true
                showlyricsthumbnail = false
                thumbnailType = ThumbnailType.Modern
                thumbnailRoundness = ThumbnailRoundness.Heavy
                fadingedge = true
                thumbnailFade = 0f
                thumbnailFadeEx = 5f
                thumbnailSpacing = -32f
                showNextSongsInPlayer = false
                ///////ACTION BAR BUTTONS////////////////
                transparentBackgroundActionBarPlayer = true
                actionspacedevenly = true
                showButtonPlayerVideo = false
                showButtonPlayerDiscover = false
                showButtonPlayerDownload = true
                showButtonPlayerAddToPlaylist = false
                showButtonPlayerLoop = false
                showButtonPlayerShuffle = false
                showButtonPlayerLyrics = false
                expandedplayertoggle = true
                showButtonPlayerSleepTimer = false
                visualizerEnabled = false
                appearanceChooser = false
                showButtonPlayerArrow =false
                showButtonPlayerStartradio = false
                showButtonPlayerMenu = true
                ///////////////////////////
                appearanceChooser = false
            },
            onClick5 = {
                showTopActionsBar = true
                showthumbnail = true
                playerBackgroundColors = PlayerBackgroundColors.CoverColorGradient
                playerInfoType = PlayerInfoType.Essential
                playerTimelineType = PlayerTimelineType.Wavy
                playerTimelineSize = PlayerTimelineSize.Biggest
                playerControlsType = PlayerControlsType.Essential
                playerPlayButtonType = PlayerPlayButtonType.CircularRibbed
                colorPaletteName = ColorPaletteName.Dynamic
                colorPaletteMode = ColorPaletteMode.System
                transparentbar = false
                playerType = PlayerType.Essential
                expandedplayer = true
                playerThumbnailSize = PlayerThumbnailSize.Big
                showTotalTimeQueue = false
                transparentBackgroundActionBarPlayer = true
                showRemainingSongTime = true
                showlyricsthumbnail = false
                thumbnailType = ThumbnailType.Modern
                thumbnailRoundness = ThumbnailRoundness.Heavy
                showNextSongsInPlayer = false
                ///////ACTION BAR BUTTONS////////////////
                transparentBackgroundActionBarPlayer = true
                actionspacedevenly = true
                showButtonPlayerVideo = false
                showButtonPlayerDiscover = false
                showButtonPlayerDownload = false
                showButtonPlayerAddToPlaylist = false
                showButtonPlayerLoop = false
                showButtonPlayerShuffle = true
                showButtonPlayerLyrics = true
                expandedplayertoggle = false
                showButtonPlayerSleepTimer = false
                visualizerEnabled = false
                appearanceChooser = false
                showButtonPlayerArrow =false
                showButtonPlayerStartradio = false
                showButtonPlayerMenu = true
                ///////////////////////////
                appearanceChooser = false
            }
            )
        }

        if (!isLandscape) {
            Column {
                BasicText(
                    text = stringResource(R.string.appearancepresets),
                    style = typography().m.semiBold.copy(color = colorPalette().text),
                    modifier = Modifier
                        .padding(all = 12.dp)
                        .clickable(onClick = { appearanceChooser = true })
                )
                BasicText(
                    text = stringResource(R.string.appearancepresetssecondary),
                    style = typography().xs.semiBold.copy(color = colorPalette().textSecondary),
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .padding(bottom = 10.dp)
                )
            }

            if (search.input.isBlank() || stringResource(R.string.show_player_top_actions_bar).contains(
                    search.input,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.show_player_top_actions_bar),
                    text = "",
                    isChecked = showTopActionsBar,
                    onCheckedChange = { showTopActionsBar = it }
                )

            if (!showTopActionsBar) {
                if (search.input.isBlank() || stringResource(R.string.blankspace).contains(
                        search.input,
                        true
                    )
                )
                    SwitchSettingEntry(
                        title = stringResource(R.string.blankspace),
                        text = "",
                        isChecked = topPadding,
                        onCheckedChange = { topPadding = it }
                    )
            }
        }
        if (search.input.isBlank() || stringResource(R.string.playertype).contains(
                search.input,
                true
            )
        )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.playertype),
                selectedValue = playerType,
                onValueSelected = {
                    playerType = it
                },
                valueText = {
                    when (it) {
                        PlayerType.Modern -> stringResource(R.string.pcontrols_modern)
                        PlayerType.Essential -> stringResource(R.string.pcontrols_essential)
                    }
                },
            )

        if (search.input.isBlank() || stringResource(R.string.queuetype).contains(
                search.input,
                true
            )
        )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.queuetype),
                selectedValue = queueType,
                onValueSelected = {
                    queueType = it
                },
                valueText = {
                    when (it) {
                        QueueType.Modern -> stringResource(R.string.pcontrols_modern)
                        QueueType.Essential -> stringResource(R.string.pcontrols_essential)
                    }
                },
            )

        if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) {
            if (search.input.isBlank() || stringResource(R.string.show_thumbnail).contains(
                    search.input,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.show_thumbnail),
                    text = "",
                    isChecked = showthumbnail,
                    onCheckedChange = {showthumbnail = it},
                )
        }
        AnimatedVisibility(visible = !showthumbnail && playerType == PlayerType.Modern && !isLandscape) {
            if (search.input.isBlank() || stringResource(R.string.swipe_Animation_No_Thumbnail).contains(
                    search.input,
                    true
                )
            )
                EnumValueSelectorSettingsEntry(
                    title = stringResource(R.string.swipe_Animation_No_Thumbnail),
                    selectedValue = swipeAnimationNoThumbnail,
                    onValueSelected = { swipeAnimationNoThumbnail = it },
                    valueText = {
                        when (it) {
                            SwipeAnimationNoThumbnail.Sliding -> stringResource(R.string.te_slide_vertical)
                            SwipeAnimationNoThumbnail.Fade -> stringResource(R.string.te_fade)
                            SwipeAnimationNoThumbnail.Scale -> stringResource(R.string.te_scale)
                            SwipeAnimationNoThumbnail.Carousel -> stringResource(R.string.carousel)
                            SwipeAnimationNoThumbnail.Circle -> stringResource(R.string.vt_circular)
                        }
                    },
                    modifier = Modifier.padding(start = if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) 25.dp else 0.dp)
                )
        }
        AnimatedVisibility(visible = showthumbnail) {
            Column {
                if (playerType == PlayerType.Modern) {
                    if (search.input.isBlank() || stringResource(R.string.fadingedge).contains(
                            search.input,
                            true
                        )
                    )
                        SwitchSettingEntry(
                            title = stringResource(R.string.fadingedge),
                            text = "",
                            isChecked = fadingedge,
                            onCheckedChange = { fadingedge = it },
                            modifier = Modifier.padding(start = if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) 25.dp else 0.dp)
                        )
                }

                if (playerType == PlayerType.Modern && !isLandscape && (expandedplayertoggle || expandedplayer)) {
                    if (search.input.isBlank() || stringResource(R.string.carousel).contains(
                            search.input,
                            true
                        )
                    )
                        SwitchSettingEntry(
                            title = stringResource(R.string.carousel),
                            text = "",
                            isChecked = carousel,
                            onCheckedChange = { carousel = it },
                            modifier = Modifier.padding(start = if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) 25.dp else 0.dp)
                        )

                    if (search.input.isBlank() || stringResource(R.string.carouselsize).contains(
                            search.input,
                            true
                        )
                    )
                        EnumValueSelectorSettingsEntry(
                            title = stringResource(R.string.carouselsize),
                            selectedValue = carouselSize,
                            onValueSelected = { carouselSize = it },
                            valueText = {
                                when (it) {
                                    CarouselSize.Small -> stringResource(R.string.small)
                                    CarouselSize.Medium -> stringResource(R.string.medium)
                                    CarouselSize.Big -> stringResource(R.string.big)
                                    CarouselSize.Biggest -> stringResource(R.string.biggest)
                                    CarouselSize.Expanded -> stringResource(R.string.expanded)
                                }
                            },
                            modifier = Modifier.padding(start = if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) 25.dp else 0.dp)
                        )
                }
                if (playerType == PlayerType.Essential) {

                    if (search.input.isBlank() || stringResource(R.string.thumbnailpause).contains(
                            search.input,
                            true
                        )
                    )
                        SwitchSettingEntry(
                            title = stringResource(R.string.thumbnailpause),
                            text = "",
                            isChecked = thumbnailpause,
                            onCheckedChange = { thumbnailpause = it },
                            modifier = Modifier.padding(start = if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) 25.dp else 0.dp)
                        )

                    if (search.input.isBlank() || stringResource(R.string.show_lyrics_thumbnail).contains(
                            search.input,
                            true
                        )
                    )
                        SwitchSettingEntry(
                            title = stringResource(R.string.show_lyrics_thumbnail),
                            text = "",
                            isChecked = showlyricsthumbnail,
                            onCheckedChange = { showlyricsthumbnail = it },
                            modifier = Modifier.padding(start = if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) 25.dp else 0.dp)
                        )
                    if (visualizerEnabled) {
                        if (search.input.isBlank() || stringResource(R.string.showvisthumbnail).contains(
                                search.input,
                                true
                            )
                        )
                            SwitchSettingEntry(
                                title = stringResource(R.string.showvisthumbnail),
                                text = "",
                                isChecked = showvisthumbnail,
                                onCheckedChange = { showvisthumbnail = it },
                                modifier = Modifier.padding(start = if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) 25.dp else 0.dp)
                            )
                    }
                }

                if (search.input.isBlank() || stringResource(R.string.show_cover_thumbnail_animation).contains(
                        search.input,
                        true
                    )
                ) {
                    SwitchSettingEntry(
                        title = stringResource(R.string.show_cover_thumbnail_animation),
                        text = "",
                        isChecked = showCoverThumbnailAnimation,
                        onCheckedChange = { showCoverThumbnailAnimation = it },
                        modifier = Modifier.padding(start = if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) 25.dp else 0.dp)
                    )
                    AnimatedVisibility(visible = showCoverThumbnailAnimation) {
                        Column {
                            EnumValueSelectorSettingsEntry(
                                title = stringResource(R.string.cover_thumbnail_animation_type),
                                selectedValue = coverThumbnailAnimation,
                                onValueSelected = { coverThumbnailAnimation = it },
                                valueText = { it.textName },
                                modifier = Modifier.padding(start = if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) 50.dp else 25.dp)
                            )
                        }
                    }
                }

                if (isLandscape) {
                    if (search.input.isBlank() || stringResource(R.string.player_thumbnail_size).contains(
                            search.input,
                            true
                        )
                    )
                        EnumValueSelectorSettingsEntry(
                            title = stringResource(R.string.player_thumbnail_size),
                            selectedValue = playerThumbnailSizeL,
                            onValueSelected = { playerThumbnailSizeL = it },
                            valueText = {
                                when (it) {
                                    PlayerThumbnailSize.Small -> stringResource(R.string.small)
                                    PlayerThumbnailSize.Medium -> stringResource(R.string.medium)
                                    PlayerThumbnailSize.Big -> stringResource(R.string.big)
                                    PlayerThumbnailSize.Biggest -> stringResource(R.string.biggest)
                                    PlayerThumbnailSize.Expanded -> stringResource(R.string.expanded)
                                }
                            },
                            modifier = Modifier.padding(start = if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) 25.dp else 0.dp)
                        )
                } else {
                    if (search.input.isBlank() || stringResource(R.string.player_thumbnail_size).contains(
                            search.input,
                            true
                        )
                    )
                        EnumValueSelectorSettingsEntry(
                            title = stringResource(R.string.player_thumbnail_size),
                            selectedValue = playerThumbnailSize,
                            onValueSelected = { playerThumbnailSize = it },
                            valueText = {
                                when (it) {
                                    PlayerThumbnailSize.Small -> stringResource(R.string.small)
                                    PlayerThumbnailSize.Medium -> stringResource(R.string.medium)
                                    PlayerThumbnailSize.Big -> stringResource(R.string.big)
                                    PlayerThumbnailSize.Biggest -> stringResource(R.string.biggest)
                                    PlayerThumbnailSize.Expanded -> stringResource(R.string.expanded)
                                }
                            },
                            modifier = Modifier.padding(start = if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) 25.dp else 0.dp)
                        )
                }
                if (search.input.isBlank() || stringResource(R.string.thumbnailtype).contains(
                        search.input,
                        true
                    )
                )
                    EnumValueSelectorSettingsEntry(
                        title = stringResource(R.string.thumbnailtype),
                        selectedValue = thumbnailType,
                        onValueSelected = {
                            thumbnailType = it
                        },
                        valueText = {
                            when (it) {
                                ThumbnailType.Modern -> stringResource(R.string.pcontrols_modern)
                                ThumbnailType.Essential -> stringResource(R.string.pcontrols_essential)
                            }
                        },
                        modifier = Modifier.padding(start = if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) 25.dp else 0.dp)
                    )

                if (search.input.isBlank() || stringResource(R.string.thumbnail_roundness).contains(
                        search.input,
                        true
                    )
                )
                    EnumValueSelectorSettingsEntry(
                        title = stringResource(R.string.thumbnail_roundness),
                        selectedValue = thumbnailRoundness,
                        onValueSelected = { thumbnailRoundness = it },
                        trailingContent = {
                            Spacer(
                                modifier = Modifier
                                    .border(
                                        width = 1.dp,
                                        color = colorPalette().accent,
                                        shape = thumbnailRoundness.shape()
                                    )
                                    .background(
                                        color = colorPalette().background1,
                                        shape = thumbnailRoundness.shape()
                                    )
                                    .size(36.dp)
                            )
                        },
                        valueText = {
                            when (it) {
                                ThumbnailRoundness.None -> stringResource(R.string.none)
                                ThumbnailRoundness.Light -> stringResource(R.string.light)
                                ThumbnailRoundness.Heavy -> stringResource(R.string.heavy)
                                ThumbnailRoundness.Medium -> stringResource(R.string.medium)
                            }
                        },
                        modifier = Modifier.padding(start = if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) 25.dp else 0.dp)
                    )
            }
        }

        if (!showthumbnail) {
            if (search.input.isBlank() || stringResource(R.string.noblur).contains(
                    search.input,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.noblur),
                    text = "",
                    isChecked = noblur,
                    onCheckedChange = { noblur = it }
                )


        }

        if (!(showthumbnail && playerType == PlayerType.Essential)){
            if (search.input.isBlank() || stringResource(R.string.statsfornerdsplayer).contains(
                    search.input,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.statsfornerdsplayer),
                    text = "",
                    isChecked = statsfornerds,
                    onCheckedChange = { statsfornerds = it }
                )
        }

        if (search.input.isBlank() || stringResource(R.string.timelinesize).contains(
                search.input,
                true
            )
        )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.timelinesize),
                selectedValue = playerTimelineSize,
                onValueSelected = { playerTimelineSize = it },
                valueText = {
                    when (it) {
                        PlayerTimelineSize.Small -> stringResource(R.string.small)
                        PlayerTimelineSize.Medium -> stringResource(R.string.medium)
                        PlayerTimelineSize.Big -> stringResource(R.string.big)
                        PlayerTimelineSize.Biggest -> stringResource(R.string.biggest)
                        PlayerTimelineSize.Expanded -> stringResource(R.string.expanded)
                    }
                }
            )

        if (search.input.isBlank() || stringResource(R.string.pinfo_type).contains(
                search.input,
                true
            )
        ) {
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.pinfo_type),
                selectedValue = playerInfoType,
                onValueSelected = {
                    playerInfoType = it
                },
                valueText = {
                    when (it) {
                        PlayerInfoType.Modern -> stringResource(R.string.pcontrols_modern)
                        PlayerInfoType.Essential -> stringResource(R.string.pcontrols_essential)
                    }
                },
            )
            SettingsDescription(text = stringResource(R.string.pinfo_album_and_artist_name))

            AnimatedVisibility( visible = playerInfoType == PlayerInfoType.Modern) {
                Column {
                    if (search.input.isBlank() || stringResource(R.string.pinfo_show_icons).contains(
                            search.input,
                            true
                        )
                    )
                        SwitchSettingEntry(
                            title = stringResource(R.string.pinfo_show_icons),
                            text = "",
                            isChecked = playerInfoShowIcons,
                            onCheckedChange = { playerInfoShowIcons = it },
                            modifier = Modifier
                                .padding(start = 25.dp)
                        )
                }
            }

        }



        if (search.input.isBlank() || stringResource(R.string.miniplayertype).contains(
                search.input,
                true
            )
        )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.miniplayertype),
                selectedValue = miniPlayerType,
                onValueSelected = {
                    miniPlayerType = it
                },
                valueText = {
                    when (it) {
                        MiniPlayerType.Modern -> stringResource(R.string.pcontrols_modern)
                        MiniPlayerType.Essential -> stringResource(R.string.pcontrols_essential)
                    }
                },
            )

        if (search.input.isBlank() || stringResource(R.string.player_swap_controls_with_timeline).contains(
                search.input,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.player_swap_controls_with_timeline),
                text = "",
                isChecked = playerSwapControlsWithTimeline,
                onCheckedChange = { playerSwapControlsWithTimeline = it }
            )

        if (search.input.isBlank() || stringResource(R.string.timeline).contains(
                search.input,
                true
            )
        )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.timeline),
                selectedValue = playerTimelineType,
                onValueSelected = { playerTimelineType = it },
                valueText = { it.textName }
            )

        if (search.input.isBlank() || stringResource(R.string.transparentbar).contains(
                search.input,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.transparentbar),
                text = "",
                isChecked = transparentbar,
                onCheckedChange = { transparentbar = it }
            )

        if (search.input.isBlank() || stringResource(R.string.pcontrols_type).contains(
                search.input,
                true
            )
        )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.pcontrols_type),
                selectedValue = playerControlsType,
                onValueSelected = {
                    playerControlsType = it
                },
                valueText = {
                    when (it) {
                        PlayerControlsType.Modern -> stringResource(R.string.pcontrols_modern)
                        PlayerControlsType.Essential -> stringResource(R.string.pcontrols_essential)
                    }
                },
            )


        if (search.input.isBlank() || stringResource(R.string.play_button).contains(
                search.input,
                true
            )
        )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.play_button),
                selectedValue = playerPlayButtonType,
                onValueSelected = {
                    playerPlayButtonType = it
                    lastPlayerPlayButtonType = it
                },
                valueText = {
                    when (it) {
                        PlayerPlayButtonType.Disabled -> stringResource(R.string.vt_disabled)
                        PlayerPlayButtonType.Default -> stringResource(R.string._default)
                        PlayerPlayButtonType.Rectangular -> stringResource(R.string.rectangular)
                        PlayerPlayButtonType.Square -> stringResource(R.string.square)
                        PlayerPlayButtonType.CircularRibbed -> stringResource(R.string.circular_ribbed)
                    }
                },
            )

        if (search.input.isBlank() || stringResource(R.string.buttonzoomout).contains(
                search.input,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.buttonzoomout),
                text = "",
                isChecked = buttonzoomout,
                onCheckedChange = { buttonzoomout = it }
            )


        if (search.input.isBlank() || stringResource(R.string.play_button).contains(
                search.input,
                true
            )
        )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.icon_like_button),
                selectedValue = iconLikeType,
                onValueSelected = {
                    iconLikeType = it
                },
                valueText = {
                    when (it) {
                        IconLikeType.Essential -> stringResource(R.string.pcontrols_essential)
                        IconLikeType.Apple -> stringResource(R.string.icon_like_apple)
                        IconLikeType.Breaked -> stringResource(R.string.icon_like_breaked)
                        IconLikeType.Gift -> stringResource(R.string.icon_like_gift)
                        IconLikeType.Shape -> stringResource(R.string.icon_like_shape)
                        IconLikeType.Striped -> stringResource(R.string.icon_like_striped)
                        IconLikeType.Brilliant -> stringResource(R.string.icon_like_brilliant)
                    }
                },
            )

        /*

        if (filter.isNullOrBlank() || stringResource(R.string.use_gradient_background).contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = stringResource(R.string.use_gradient_background),
                text = "",
                isChecked = isGradientBackgroundEnabled,
                onCheckedChange = { isGradientBackgroundEnabled = it }
            )
         */

        if (search.input.isBlank() || stringResource(R.string.background_colors).contains(
                search.input,
                true
            )
        )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.background_colors),
                selectedValue = playerBackgroundColors,
                onValueSelected = {
                    playerBackgroundColors = it
                },
                valueText = {
                    when (it) {
                        PlayerBackgroundColors.CoverColor -> stringResource(R.string.bg_colors_background_from_cover)
                        PlayerBackgroundColors.ThemeColor -> stringResource(R.string.bg_colors_background_from_theme)
                        PlayerBackgroundColors.CoverColorGradient -> stringResource(R.string.bg_colors_gradient_background_from_cover)
                        PlayerBackgroundColors.ThemeColorGradient -> stringResource(R.string.bg_colors_gradient_background_from_theme)
                        PlayerBackgroundColors.BlurredCoverColor -> stringResource(R.string.bg_colors_blurred_cover_background)
                        PlayerBackgroundColors.ColorPalette -> stringResource(R.string.colorpalette)
                        PlayerBackgroundColors.AnimatedGradient -> stringResource(R.string.animatedgradient)
                    }
                },
            )

        AnimatedVisibility(visible = playerBackgroundColors == PlayerBackgroundColors.AnimatedGradient) {
            if (search.input.isBlank() || stringResource(R.string.gradienttype).contains(
                    search.input,
                    true
                )
            )
                EnumValueSelectorSettingsEntry(
                    title = stringResource(R.string.gradienttype),
                    selectedValue = animatedGradient,
                    onValueSelected = {
                        animatedGradient = it
                    },
                    valueText = {
                        when (it) {
                            AnimatedGradient.FluidThemeColorGradient -> stringResource(R.string.bg_colors_fluid_gradient_background_from_theme)
                            AnimatedGradient.FluidCoverColorGradient -> stringResource(R.string.bg_colors_fluid_gradient_background_from_cover)
                            AnimatedGradient.Linear -> stringResource(R.string.linear)
                            AnimatedGradient.Mesh -> stringResource(R.string.mesh)
                            AnimatedGradient.MesmerizingLens -> stringResource(R.string.mesmerizinglens)
                            AnimatedGradient.GlossyGradients -> stringResource(R.string.glossygradient)
                            AnimatedGradient.GradientFlow -> stringResource(R.string.gradientflow)
                            AnimatedGradient.PurpleLiquid -> stringResource(R.string.purpleliquid)
                            AnimatedGradient.Stage -> stringResource(R.string.stage)
                            AnimatedGradient.InkFlow -> stringResource(R.string.inkflow)
                            AnimatedGradient.GoldenMagma -> stringResource(R.string.goldenmagma)
                            AnimatedGradient.OilFlow -> stringResource(R.string.oilflow)
                            AnimatedGradient.IceReflection -> stringResource(R.string.icereflection)
                            AnimatedGradient.BlackCherryCosmos -> stringResource(R.string.blackcherrycosmos)
                            AnimatedGradient.Random -> stringResource(R.string.random)
                        }
                    },
                    modifier = Modifier.padding(start = if (playerBackgroundColors == PlayerBackgroundColors.AnimatedGradient) 25.dp else 0.dp)
                )
        }

        if ((playerBackgroundColors == PlayerBackgroundColors.CoverColorGradient) || (playerBackgroundColors == PlayerBackgroundColors.ThemeColorGradient))
            if (search.input.isBlank() || stringResource(R.string.blackgradient).contains(
                    search.input,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.blackgradient),
                    text = "",
                    isChecked = blackgradient,
                    onCheckedChange = { blackgradient = it }
                )

        if ((playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) && (playerType == PlayerType.Modern))
            if (search.input.isBlank() || stringResource(R.string.albumCoverRotation).contains(
                    search.input,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.albumCoverRotation),
                    text = "",
                    isChecked = albumCoverRotation,
                    onCheckedChange = { albumCoverRotation = it },
                    modifier = Modifier
                        .padding(start = 25.dp)
                )

        if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor)
            if (search.input.isBlank() || stringResource(R.string.bottomgradient).contains(
                    search.input,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.bottomgradient),
                    text = "",
                    isChecked = bottomgradient,
                    onCheckedChange = { bottomgradient = it }
                )
        if (search.input.isBlank() || stringResource(R.string.textoutline).contains(
              search.input,
              true
              )
         )
             SwitchSettingEntry(
                 title = stringResource(R.string.textoutline),
                 text = "",
                 isChecked = textoutline,
                 onCheckedChange = { textoutline = it }
             )

       if (search.input.isBlank() || stringResource(R.string.show_total_time_of_queue).contains(
                search.input,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.show_total_time_of_queue),
                text = "",
                isChecked = showTotalTimeQueue,
                onCheckedChange = { showTotalTimeQueue = it }
            )

        if (search.input.isBlank() || stringResource(R.string.show_remaining_song_time).contains(
                search.input,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.show_remaining_song_time),
                text = "",
                isChecked = showRemainingSongTime,
                onCheckedChange = { showRemainingSongTime = it }
            )

        if (search.input.isBlank() || stringResource(R.string.show_next_songs_in_player).contains(
                search.input,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.show_next_songs_in_player),
                text = "",
                isChecked = showNextSongsInPlayer,
                onCheckedChange = { showNextSongsInPlayer = it }
            )
        AnimatedVisibility( visible = showNextSongsInPlayer) {
          Column {
              if (search.input.isBlank() || stringResource(R.string.showtwosongs).contains(search.input,true))
                  EnumValueSelectorSettingsEntry(
                      title = stringResource(R.string.songs_number_to_show),
                      selectedValue = showsongs,
                      onValueSelected = {
                          showsongs = it
                      },
                      valueText = {
                          it.name
                      },
                      modifier = Modifier
                          .padding(start = 25.dp)
                  )


            if (search.input.isBlank() || stringResource(R.string.showalbumcover).contains(
                    search.input,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.showalbumcover),
                    text = "",
                    isChecked = showalbumcover,
                    onCheckedChange = { showalbumcover = it },
                      modifier = Modifier.padding(start = 25.dp)
                  )
          }
        }

        if (search.input.isBlank() || stringResource(R.string.disable_scrolling_text).contains(
                search.input,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.disable_scrolling_text),
                text = stringResource(R.string.scrolling_text_is_used_for_long_texts),
                isChecked = disableScrollingText,
                onCheckedChange = { disableScrollingText = it }
            )

        if (search.input.isBlank() || stringResource(if (playerType == PlayerType.Modern && !isLandscape) R.string.disable_horizontal_swipe else R.string.disable_vertical_swipe).contains(
                search.input,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(if (playerType == PlayerType.Modern && !isLandscape) R.string.disable_vertical_swipe else R.string.disable_horizontal_swipe),
                text = stringResource(if (playerType == PlayerType.Modern && !isLandscape) R.string.disable_vertical_swipe_secondary else R.string.disable_song_switching_via_swipe),
                isChecked = disablePlayerHorizontalSwipe,
                onCheckedChange = { disablePlayerHorizontalSwipe = it }
            )

        if (search.input.isBlank() || stringResource(R.string.player_rotating_buttons).contains(
                search.input,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.player_rotating_buttons),
                text = stringResource(R.string.player_enable_rotation_buttons),
                isChecked = effectRotationEnabled,
                onCheckedChange = { effectRotationEnabled = it }
            )

        if (search.input.isBlank() || stringResource(R.string.toggle_lyrics).contains(
                search.input,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.toggle_lyrics),
                text = stringResource(R.string.by_tapping_on_the_thumbnail),
                isChecked = thumbnailTapEnabled,
                onCheckedChange = { thumbnailTapEnabled = it }
            )

        if (search.input.isBlank() || stringResource(R.string.click_lyrics_text).contains(
                search.input,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.click_lyrics_text),
                text = "",
                isChecked = clickLyricsText,
                onCheckedChange = { clickLyricsText = it }
            )
        if (showlyricsthumbnail)
            if (search.input.isBlank() || stringResource(R.string.show_background_in_lyrics).contains(
                    search.input,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.show_background_in_lyrics),
                    text = "",
                    isChecked = showBackgroundLyrics,
                    onCheckedChange = { showBackgroundLyrics = it }
                )

        if (search.input.isBlank() || stringResource(R.string.player_enable_lyrics_popup_message).contains(
                search.input,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.player_enable_lyrics_popup_message),
                text = "",
                isChecked = playerEnableLyricsPopupMessage,
                onCheckedChange = { playerEnableLyricsPopupMessage = it }
            )

        if (search.input.isBlank() || stringResource(R.string.background_progress_bar).contains(
                search.input,
                true
            )
        )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.background_progress_bar),
                selectedValue = backgroundProgress,
                onValueSelected = {
                    backgroundProgress = it
                },
                valueText = {
                    when (it) {
                        BackgroundProgress.Player -> stringResource(R.string.player)
                        BackgroundProgress.MiniPlayer -> stringResource(R.string.minimized_player)
                        BackgroundProgress.Both -> stringResource(R.string.both)
                        BackgroundProgress.Disabled -> stringResource(R.string.vt_disabled)
                    }
                },
            )


        if (search.input.isBlank() || stringResource(R.string.visualizer).contains(
                search.input,
                true
            )
        ) {
            SwitchSettingEntry(
                title = stringResource(R.string.visualizer),
                text = "",
                isChecked = visualizerEnabled,
                onCheckedChange = { visualizerEnabled = it }
            )
            /*
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.visualizer),
                selectedValue = playerVisualizerType,
                onValueSelected = { playerVisualizerType = it },
                valueText = {
                    when (it) {
                        PlayerVisualizerType.Fancy -> stringResource(R.string.vt_fancy)
                        PlayerVisualizerType.Circular -> stringResource(R.string.vt_circular)
                        PlayerVisualizerType.Disabled -> stringResource(R.string.vt_disabled)
                        PlayerVisualizerType.Stacked -> stringResource(R.string.vt_stacked)
                        PlayerVisualizerType.Oneside -> stringResource(R.string.vt_one_side)
                        PlayerVisualizerType.Doubleside -> stringResource(R.string.vt_double_side)
                        PlayerVisualizerType.DoublesideCircular -> stringResource(R.string.vt_double_side_circular)
                        PlayerVisualizerType.Full -> stringResource(R.string.vt_full)
                    }
                }
            )
            */
            ImportantSettingsDescription(text = stringResource(R.string.visualizer_require_mic_permission))
        }

        SettingsGroupSpacer()
        SettingsEntryGroupText(title = stringResource(R.string.player_action_bar))

        if (search.input.isBlank() || stringResource(R.string.action_bar_transparent_background).contains(
                search.input,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_transparent_background),
                text = "",
                isChecked = transparentBackgroundActionBarPlayer,
                onCheckedChange = { transparentBackgroundActionBarPlayer = it }
            )

        if (search.input.isBlank() || stringResource(R.string.actionspacedevenly).contains(
                search.input,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.actionspacedevenly),
                text = "",
                isChecked = actionspacedevenly,
                onCheckedChange = { actionspacedevenly = it }
            )

        if (search.input.isBlank() || stringResource(R.string.tapqueue).contains(
                search.input,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.tapqueue),
                text = "",
                isChecked = tapqueue,
                onCheckedChange = { tapqueue = it }
            )

        if (search.input.isBlank() || stringResource(R.string.swipe_up_to_open_the_queue).contains(
                search.input,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.swipe_up_to_open_the_queue),
                text = "",
                isChecked = swipeUpQueue,
                onCheckedChange = { swipeUpQueue = it }
            )

        if (search.input.isBlank() || stringResource(R.string.action_bar_show_video_button).contains(
                search.input,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_show_video_button),
                text = "",
                isChecked = showButtonPlayerVideo,
                onCheckedChange = { showButtonPlayerVideo = it }
            )

        if (search.input.isBlank() || stringResource(R.string.action_bar_show_discover_button).contains(
                search.input,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_show_discover_button),
                text = "",
                isChecked = showButtonPlayerDiscover,
                onCheckedChange = { showButtonPlayerDiscover = it }
            )

        if (search.input.isBlank() || stringResource(R.string.action_bar_show_download_button).contains(
                search.input,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_show_download_button),
                text = "",
                isChecked = showButtonPlayerDownload,
                onCheckedChange = { showButtonPlayerDownload = it }
            )

        if (search.input.isBlank() || stringResource(R.string.action_bar_show_add_to_playlist_button).contains(
                search.input,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_show_add_to_playlist_button),
                text = "",
                isChecked = showButtonPlayerAddToPlaylist,
                onCheckedChange = { showButtonPlayerAddToPlaylist = it }
            )

        if (search.input.isBlank() || stringResource(R.string.action_bar_show_loop_button).contains(
                search.input,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_show_loop_button),
                text = "",
                isChecked = showButtonPlayerLoop,
                onCheckedChange = { showButtonPlayerLoop = it }
            )

        if (search.input.isBlank() || stringResource(R.string.action_bar_show_shuffle_button).contains(
                search.input,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_show_shuffle_button),
                text = "",
                isChecked = showButtonPlayerShuffle,
                onCheckedChange = { showButtonPlayerShuffle = it }
            )

        if (search.input.isBlank() || stringResource(R.string.action_bar_show_lyrics_button).contains(
                search.input,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_show_lyrics_button),
                text = "",
                isChecked = showButtonPlayerLyrics,
                onCheckedChange = { showButtonPlayerLyrics = it }
            )
        if (!isLandscape || !showthumbnail) {
            if (!showlyricsthumbnail) {
                if (search.input.isBlank() || stringResource(R.string.expandedplayer).contains(
                        search.input,
                        true
                    )
                )
                    SwitchSettingEntry(
                        title = stringResource(R.string.expandedplayer),
                        text = "",
                        isChecked = expandedplayertoggle,
                        onCheckedChange = { expandedplayertoggle = it }
                    )
            }
        }

        if (search.input.isBlank() || stringResource(R.string.action_bar_show_sleep_timer_button).contains(
                search.input,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_show_sleep_timer_button),
                text = "",
                isChecked = showButtonPlayerSleepTimer,
                onCheckedChange = { showButtonPlayerSleepTimer = it }
            )

        if (search.input.isBlank() || stringResource(R.string.show_equalizer).contains(
                search.input,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.show_equalizer),
                text = "",
                isChecked = showButtonPlayerSystemEqualizer,
                onCheckedChange = { showButtonPlayerSystemEqualizer = it }
            )

        if (search.input.isBlank() || stringResource(R.string.action_bar_show_arrow_button_to_open_queue).contains(
                search.input,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_show_arrow_button_to_open_queue),
                text = "",
                isChecked = showButtonPlayerArrow,
                onCheckedChange = { showButtonPlayerArrow = it }
            )

        if (search.input.isBlank() || stringResource(R.string.action_bar_show_start_radio_button).contains(
                search.input,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_show_start_radio_button),
                text = "",
                isChecked = showButtonPlayerStartradio,
                onCheckedChange = { showButtonPlayerStartradio = it }
            )

        if (search.input.isBlank() || stringResource(R.string.action_bar_show_menu_button).contains(
                search.input,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_show_menu_button),
                text = "",
                isChecked = showButtonPlayerMenu,
                onCheckedChange = { showButtonPlayerMenu = it }
            )

        if (!showlyricsthumbnail) {
            SettingsGroupSpacer()
            SettingsEntryGroupText(title = stringResource(R.string.full_screen_lyrics_components))

            if (showTotalTimeQueue) {
                if (search.input.isBlank() || stringResource(R.string.show_total_time_of_queue).contains(
                        search.input,
                        true
                    )
                )
                    SwitchSettingEntry(
                        title = stringResource(R.string.show_total_time_of_queue),
                        text = "",
                        isChecked = queueDurationExpanded,
                        onCheckedChange = { queueDurationExpanded = it }
                    )
            }

            if (search.input.isBlank() || stringResource(R.string.titleartist).contains(
                    search.input,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.titleartist),
                    text = "",
                    isChecked = titleExpanded,
                    onCheckedChange = { titleExpanded = it }
                )

            if (search.input.isBlank() || stringResource(R.string.timeline).contains(
                    search.input,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.timeline),
                    text = "",
                    isChecked = timelineExpanded,
                    onCheckedChange = { timelineExpanded = it }
                )

            if (search.input.isBlank() || stringResource(R.string.controls).contains(
                    search.input,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.controls),
                    text = "",
                    isChecked = controlsExpanded,
                    onCheckedChange = { controlsExpanded = it }
                )

            if (statsfornerds && (!(showthumbnail && playerType == PlayerType.Essential))){
                if (search.input.isBlank() || stringResource(R.string.statsfornerds).contains(
                        search.input,
                        true
                    )
                )
                    SwitchSettingEntry(
                        title = stringResource(R.string.statsfornerds),
                        text = "",
                        isChecked = statsExpanded,
                        onCheckedChange = { statsExpanded = it }
                    )
            }

            if (
                showButtonPlayerDownload ||
                showButtonPlayerAddToPlaylist ||
                showButtonPlayerLoop ||
                showButtonPlayerShuffle ||
                showButtonPlayerLyrics ||
                showButtonPlayerSleepTimer ||
                showButtonPlayerSystemEqualizer ||
                showButtonPlayerArrow ||
                showButtonPlayerMenu ||
                expandedplayertoggle ||
                showButtonPlayerDiscover ||
                showButtonPlayerVideo
            ){
                if (search.input.isBlank() || stringResource(R.string.actionbar).contains(
                        search.input,
                        true
                    )
                )
                    SwitchSettingEntry(
                        title = stringResource(R.string.actionbar),
                        text = "",
                        isChecked = actionExpanded,
                        onCheckedChange = {
                            actionExpanded = it
                        }
                    )
            }
            if (showNextSongsInPlayer && actionExpanded) {
                if (search.input.isBlank() || stringResource(R.string.miniqueue).contains(
                        search.input,
                        true
                    )
                )
                    SwitchSettingEntry(
                        title = stringResource(R.string.miniqueue),
                        text = "",
                        isChecked = miniQueueExpanded,
                        onCheckedChange = { miniQueueExpanded = it }
                    )
            }

        }


        SettingsGroupSpacer()
        SettingsEntryGroupText(title = stringResource(R.string.notification_player))

        if (search.input.isBlank() || stringResource(R.string.notification_player).contains(
                search.input,
                true
            )
        ) {
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.notificationPlayerFirstIcon),
                selectedValue = notificationPlayerFirstIcon,
                onValueSelected = {
                    notificationPlayerFirstIcon = it
                    restartService = true
                },
                valueText = {
                    it.displayName
                },
            )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.notificationPlayerSecondIcon),
                selectedValue = notificationPlayerSecondIcon,
                onValueSelected = {
                    notificationPlayerSecondIcon = it
                    restartService = true
                },
                valueText = {
                    it.displayName
                },
            )
            RestartPlayerService(restartService, onRestart = { restartService = false })
        }


//        if (search.input.isBlank() || stringResource(R.string.show_song_cover).contains(
//                search.input,
//                true
//            )
//        )
//            if (!isAtLeastAndroid13) {
//                SettingsGroupSpacer()
//
//                SettingsEntryGroupText(title = stringResource(R.string.lockscreen))
//
//                SwitchSettingEntry(
//                    title = stringResource(R.string.show_song_cover),
//                    text = stringResource(R.string.use_song_cover_on_lockscreen),
//                    isChecked = isShowingThumbnailInLockscreen,
//                    onCheckedChange = { isShowingThumbnailInLockscreen = it }
//                )
//            }

        if (isAtLeastAndroid7) {
            SettingsGroupSpacer()
            SettingsEntryGroupText(title = stringResource(R.string.wallpaper))
            SwitchSettingEntry(
                title = stringResource(R.string.enable_wallpaper),
                text = "",
                isChecked = enableWallpaper,
                onCheckedChange = { enableWallpaper = it }
            )
            AnimatedVisibility(visible = enableWallpaper) {
                Column {
                    EnumValueSelectorSettingsEntry(
                        title = stringResource(R.string.set_cover_thumbnail_as_wallpaper),
                        selectedValue = wallpaperType,
                        onValueSelected = {
                            wallpaperType = it
                            restartService = true
                        },
                        valueText = {
                            it.displayName
                        },
                        modifier = Modifier.padding(start = 25.dp)
                    )
                    RestartPlayerService(restartService, onRestart = { restartService = false })
                }
            }
        }

        SettingsGroupSpacer()
        var resetToDefault by remember { mutableStateOf(false) }

        ButtonBarSettingEntry(
            title = stringResource(R.string.settings_reset),
            text = stringResource(R.string.settings_restore_default_settings),
            icon = R.drawable.refresh,
            iconColor = colorPalette().text,
            onClick = { resetToDefault = true },
        )
        if (resetToDefault) {
            DefaultAppearanceSettings()
            resetToDefault = false
            navController.popBackStack()
            SmartMessage(stringResource(R.string.done), context = context)
        }

//        SettingsGroupSpacer()
//        ButtonBarSettingEntry(
//            title = "Export appearance settings",
//            text = "Backup or share appearance settings",
//            icon = R.drawable.export,
//            iconColor = colorPalette().text,
//            onClick = { isExporting = true },
//        )
//
//        ButtonBarSettingEntry(
//            title = "Import appearance settings",
//            text = "Restore backup or shared appearance settings",
//            icon = R.drawable.resource_import,
//            iconColor = colorPalette().text,
//            onClick = {
//                try {
//                    importLauncher.launch(
//                        arrayOf(
//                            "text/*"
//                        )
//                    )
//                } catch (e: ActivityNotFoundException) {
//                    SmartMessage(
//                        context.resources.getString(R.string.info_not_find_app_open_doc),
//                        type = PopupType.Warning, context = context
//                    )
//                }
//            },
//        )

        SettingsGroupSpacer(
            modifier = Modifier.height(Dimensions.bottomSpacer)
        )
    }




}
