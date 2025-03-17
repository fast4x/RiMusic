package it.fast4x.rimusic.ui.screens.settings

import android.os.Build
import android.text.TextUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.AlbumSwipeAction
import it.fast4x.rimusic.enums.AudioQualityFormat
import it.fast4x.rimusic.enums.BackgroundProgress
import it.fast4x.rimusic.enums.CarouselSize
import it.fast4x.rimusic.enums.ColorPaletteMode
import it.fast4x.rimusic.enums.ColorPaletteName
import it.fast4x.rimusic.enums.DurationInMilliseconds
import it.fast4x.rimusic.enums.DurationInMinutes
import it.fast4x.rimusic.enums.ExoPlayerMinTimeForEvent
import it.fast4x.rimusic.enums.FontType
import it.fast4x.rimusic.enums.HomeScreenTabs
import it.fast4x.rimusic.enums.IconLikeType
import it.fast4x.rimusic.enums.Languages
import it.fast4x.rimusic.enums.MaxSongs
import it.fast4x.rimusic.enums.MaxStatisticsItems
import it.fast4x.rimusic.enums.MaxTopPlaylistItems
import it.fast4x.rimusic.enums.MenuStyle
import it.fast4x.rimusic.enums.MessageType
import it.fast4x.rimusic.enums.MiniPlayerType
import it.fast4x.rimusic.enums.MusicAnimationType
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.enums.NavigationBarType
import it.fast4x.rimusic.enums.NotificationType
import it.fast4x.rimusic.enums.PauseBetweenSongs
import it.fast4x.rimusic.enums.PipModule
import it.fast4x.rimusic.enums.PlayerBackgroundColors
import it.fast4x.rimusic.enums.PlayerControlsType
import it.fast4x.rimusic.enums.PlayerInfoType
import it.fast4x.rimusic.enums.PlayerPlayButtonType
import it.fast4x.rimusic.enums.PlayerPosition
import it.fast4x.rimusic.enums.PlayerThumbnailSize
import it.fast4x.rimusic.enums.PlayerTimelineSize
import it.fast4x.rimusic.enums.PlayerTimelineType
import it.fast4x.rimusic.enums.PlayerType
import it.fast4x.rimusic.enums.PlaylistSwipeAction
import it.fast4x.rimusic.enums.QueueSwipeAction
import it.fast4x.rimusic.enums.QueueType
import it.fast4x.rimusic.enums.RecommendationsNumber
import it.fast4x.rimusic.enums.ThumbnailRoundness
import it.fast4x.rimusic.enums.ThumbnailType
import it.fast4x.rimusic.enums.TransitionEffect
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.ui.components.themed.ConfirmationDialog
import it.fast4x.rimusic.ui.components.themed.HeaderWithIcon
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.ui.styling.DefaultDarkColorPalette
import it.fast4x.rimusic.ui.styling.DefaultLightColorPalette
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.utils.MaxTopPlaylistItemsKey
import it.fast4x.rimusic.utils.RestartActivity
import it.fast4x.rimusic.utils.RestartPlayerService
import it.fast4x.rimusic.utils.UiTypeKey
import it.fast4x.rimusic.utils.actionspacedevenlyKey
import it.fast4x.rimusic.utils.albumSwipeLeftActionKey
import it.fast4x.rimusic.utils.albumSwipeRightActionKey
import it.fast4x.rimusic.utils.applyFontPaddingKey
import it.fast4x.rimusic.utils.audioQualityFormatKey
import it.fast4x.rimusic.utils.autoLoadSongsInQueueKey
import it.fast4x.rimusic.utils.backgroundProgressKey
import it.fast4x.rimusic.utils.blackgradientKey
import it.fast4x.rimusic.utils.buttonzoomoutKey
import it.fast4x.rimusic.utils.carouselKey
import it.fast4x.rimusic.utils.carouselSizeKey
import it.fast4x.rimusic.utils.clickOnLyricsTextKey
import it.fast4x.rimusic.utils.closeWithBackButtonKey
import it.fast4x.rimusic.utils.closebackgroundPlayerKey
import it.fast4x.rimusic.utils.colorPaletteModeKey
import it.fast4x.rimusic.utils.colorPaletteNameKey
import it.fast4x.rimusic.utils.customThemeDark_Background0Key
import it.fast4x.rimusic.utils.customThemeDark_Background1Key
import it.fast4x.rimusic.utils.customThemeDark_Background2Key
import it.fast4x.rimusic.utils.customThemeDark_Background3Key
import it.fast4x.rimusic.utils.customThemeDark_Background4Key
import it.fast4x.rimusic.utils.customThemeDark_TextKey
import it.fast4x.rimusic.utils.customThemeDark_accentKey
import it.fast4x.rimusic.utils.customThemeDark_iconButtonPlayerKey
import it.fast4x.rimusic.utils.customThemeDark_textDisabledKey
import it.fast4x.rimusic.utils.customThemeDark_textSecondaryKey
import it.fast4x.rimusic.utils.customThemeLight_Background0Key
import it.fast4x.rimusic.utils.customThemeLight_Background1Key
import it.fast4x.rimusic.utils.customThemeLight_Background2Key
import it.fast4x.rimusic.utils.customThemeLight_Background3Key
import it.fast4x.rimusic.utils.customThemeLight_Background4Key
import it.fast4x.rimusic.utils.customThemeLight_TextKey
import it.fast4x.rimusic.utils.customThemeLight_accentKey
import it.fast4x.rimusic.utils.customThemeLight_iconButtonPlayerKey
import it.fast4x.rimusic.utils.customThemeLight_textDisabledKey
import it.fast4x.rimusic.utils.customThemeLight_textSecondaryKey
import it.fast4x.rimusic.utils.disableClosingPlayerSwipingDownKey
import it.fast4x.rimusic.utils.disableIconButtonOnTopKey
import it.fast4x.rimusic.utils.disablePlayerHorizontalSwipeKey
import it.fast4x.rimusic.utils.disableScrollingTextKey
import it.fast4x.rimusic.utils.discoverKey
import it.fast4x.rimusic.utils.effectRotationKey
import it.fast4x.rimusic.utils.enableCreateMonthlyPlaylistsKey
import it.fast4x.rimusic.utils.enablePictureInPictureAutoKey
import it.fast4x.rimusic.utils.enablePictureInPictureKey
import it.fast4x.rimusic.utils.excludeSongsWithDurationLimitKey
import it.fast4x.rimusic.utils.exoPlayerMinTimeForEventKey
import it.fast4x.rimusic.utils.expandedplayertoggleKey
import it.fast4x.rimusic.utils.fadingedgeKey
import it.fast4x.rimusic.utils.fontTypeKey
import it.fast4x.rimusic.utils.iconLikeTypeKey
import it.fast4x.rimusic.utils.indexNavigationTabKey
import it.fast4x.rimusic.utils.isAtLeastAndroid12
import it.fast4x.rimusic.utils.isAtLeastAndroid6
import it.fast4x.rimusic.utils.isPauseOnVolumeZeroEnabledKey
import it.fast4x.rimusic.utils.isSwipeToActionEnabledKey
import it.fast4x.rimusic.utils.jumpPreviousKey
import it.fast4x.rimusic.utils.keepPlayerMinimizedKey
import it.fast4x.rimusic.utils.languageAppKey
import it.fast4x.rimusic.utils.languageDestinationName
import it.fast4x.rimusic.utils.lastPlayerPlayButtonTypeKey
import it.fast4x.rimusic.utils.lastPlayerThumbnailSizeKey
import it.fast4x.rimusic.utils.lastPlayerTimelineTypeKey
import it.fast4x.rimusic.utils.loudnessBaseGainKey
import it.fast4x.rimusic.utils.maxSongsInQueueKey
import it.fast4x.rimusic.utils.maxStatisticsItemsKey
import it.fast4x.rimusic.utils.menuStyleKey
import it.fast4x.rimusic.utils.messageTypeKey
import it.fast4x.rimusic.utils.miniPlayerTypeKey
import it.fast4x.rimusic.utils.minimumSilenceDurationKey
import it.fast4x.rimusic.utils.navigationBarPositionKey
import it.fast4x.rimusic.utils.navigationBarTypeKey
import it.fast4x.rimusic.utils.notificationTypeKey
import it.fast4x.rimusic.utils.nowPlayingIndicatorKey
import it.fast4x.rimusic.utils.pauseBetweenSongsKey
import it.fast4x.rimusic.utils.pauseListenHistoryKey
import it.fast4x.rimusic.utils.persistentQueueKey
import it.fast4x.rimusic.utils.pipModuleKey
import it.fast4x.rimusic.utils.playbackFadeAudioDurationKey
import it.fast4x.rimusic.utils.playerBackgroundColorsKey
import it.fast4x.rimusic.utils.playerControlsTypeKey
import it.fast4x.rimusic.utils.playerEnableLyricsPopupMessageKey
import it.fast4x.rimusic.utils.playerInfoShowIconsKey
import it.fast4x.rimusic.utils.playerInfoTypeKey
import it.fast4x.rimusic.utils.playerPlayButtonTypeKey
import it.fast4x.rimusic.utils.playerPositionKey
import it.fast4x.rimusic.utils.playerSwapControlsWithTimelineKey
import it.fast4x.rimusic.utils.playerThumbnailSizeKey
import it.fast4x.rimusic.utils.playerTimelineSizeKey
import it.fast4x.rimusic.utils.playerTimelineTypeKey
import it.fast4x.rimusic.utils.playerTypeKey
import it.fast4x.rimusic.utils.playlistSwipeLeftActionKey
import it.fast4x.rimusic.utils.playlistSwipeRightActionKey
import it.fast4x.rimusic.utils.playlistindicatorKey
import it.fast4x.rimusic.utils.queueSwipeLeftActionKey
import it.fast4x.rimusic.utils.queueSwipeRightActionKey
import it.fast4x.rimusic.utils.queueTypeKey
import it.fast4x.rimusic.utils.recommendationsNumberKey
import it.fast4x.rimusic.utils.rememberEqualizerLauncher
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.resumePlaybackOnStartKey
import it.fast4x.rimusic.utils.resumePlaybackWhenDeviceConnectedKey
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.shakeEventEnabledKey
import it.fast4x.rimusic.utils.showButtonPlayerAddToPlaylistKey
import it.fast4x.rimusic.utils.showButtonPlayerArrowKey
import it.fast4x.rimusic.utils.showButtonPlayerDiscoverKey
import it.fast4x.rimusic.utils.showButtonPlayerDownloadKey
import it.fast4x.rimusic.utils.showButtonPlayerLoopKey
import it.fast4x.rimusic.utils.showButtonPlayerLyricsKey
import it.fast4x.rimusic.utils.showButtonPlayerMenuKey
import it.fast4x.rimusic.utils.showButtonPlayerShuffleKey
import it.fast4x.rimusic.utils.showButtonPlayerSleepTimerKey
import it.fast4x.rimusic.utils.showButtonPlayerSystemEqualizerKey
import it.fast4x.rimusic.utils.showCachedPlaylistKey
import it.fast4x.rimusic.utils.showDownloadedPlaylistKey
import it.fast4x.rimusic.utils.showFavoritesPlaylistKey
import it.fast4x.rimusic.utils.showFloatingIconKey
import it.fast4x.rimusic.utils.showMonthlyPlaylistsKey
import it.fast4x.rimusic.utils.showMyTopPlaylistKey
import it.fast4x.rimusic.utils.showNextSongsInPlayerKey
import it.fast4x.rimusic.utils.showOnDevicePlaylistKey
import it.fast4x.rimusic.utils.showPinnedPlaylistsKey
import it.fast4x.rimusic.utils.showPipedPlaylistsKey
import it.fast4x.rimusic.utils.showRemainingSongTimeKey
import it.fast4x.rimusic.utils.showSearchTabKey
import it.fast4x.rimusic.utils.showStatsInNavbarKey
import it.fast4x.rimusic.utils.showStatsListeningTimeKey
import it.fast4x.rimusic.utils.showTopActionsBarKey
import it.fast4x.rimusic.utils.showTotalTimeQueueKey
import it.fast4x.rimusic.utils.showthumbnailKey
import it.fast4x.rimusic.utils.skipMediaOnErrorKey
import it.fast4x.rimusic.utils.skipSilenceKey
import it.fast4x.rimusic.utils.swipeUpQueueKey
import it.fast4x.rimusic.utils.tapqueueKey
import it.fast4x.rimusic.utils.thumbnailRoundnessKey
import it.fast4x.rimusic.utils.thumbnailTapEnabledKey
import it.fast4x.rimusic.utils.thumbnailTypeKey
import it.fast4x.rimusic.utils.transitionEffectKey
import it.fast4x.rimusic.utils.transparentBackgroundPlayerActionBarKey
import it.fast4x.rimusic.utils.useSystemFontKey
import it.fast4x.rimusic.utils.useVolumeKeysToChangeSongKey
import it.fast4x.rimusic.utils.visualizerEnabledKey
import it.fast4x.rimusic.utils.volumeNormalizationKey
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.ui.components.themed.Search
import it.fast4x.rimusic.typography
import it.fast4x.rimusic.utils.autoDownloadSongKey
import it.fast4x.rimusic.utils.autoDownloadSongWhenAlbumBookmarkedKey
import it.fast4x.rimusic.utils.autoDownloadSongWhenLikedKey
import it.fast4x.rimusic.utils.customColorKey

@Composable
fun DefaultUiSettings() {
    var exoPlayerMinTimeForEvent by rememberPreference(
        exoPlayerMinTimeForEventKey,
        ExoPlayerMinTimeForEvent.`20s`
    )
    exoPlayerMinTimeForEvent = ExoPlayerMinTimeForEvent.`20s`
    var persistentQueue by rememberPreference(persistentQueueKey, false)
    persistentQueue = false
    var resumePlaybackOnStart by rememberPreference(resumePlaybackOnStartKey, false)
    resumePlaybackOnStart = false
    var closebackgroundPlayer by rememberPreference(closebackgroundPlayerKey, false)
    closebackgroundPlayer = false
    var closeWithBackButton by rememberPreference(closeWithBackButtonKey, true)
    closeWithBackButton = true
    var resumePlaybackWhenDeviceConnected by rememberPreference(
        resumePlaybackWhenDeviceConnectedKey,
        false
    )
    resumePlaybackWhenDeviceConnected = false

    var skipSilence by rememberPreference(skipSilenceKey, false)
    skipSilence = false
    var skipMediaOnError by rememberPreference(skipMediaOnErrorKey, false)
    skipMediaOnError = false
    var volumeNormalization by rememberPreference(volumeNormalizationKey, false)
    volumeNormalization = false
    var recommendationsNumber by rememberPreference(recommendationsNumberKey,   RecommendationsNumber.`5`)
    recommendationsNumber = RecommendationsNumber.`5`
    var keepPlayerMinimized by rememberPreference(keepPlayerMinimizedKey,   false)
    keepPlayerMinimized = false
    var disableIconButtonOnTop by rememberPreference(disableIconButtonOnTopKey, false)
    disableIconButtonOnTop = false
    var lastPlayerTimelineType by rememberPreference(lastPlayerTimelineTypeKey, PlayerTimelineType.Default)
    lastPlayerTimelineType = PlayerTimelineType.Default
    var lastPlayerThumbnailSize by rememberPreference(lastPlayerThumbnailSizeKey, PlayerThumbnailSize.Medium)
    lastPlayerThumbnailSize = PlayerThumbnailSize.Medium
    var uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)
    uiType = UiType.RiMusic
    var disablePlayerHorizontalSwipe by rememberPreference(disablePlayerHorizontalSwipeKey, false)
    disablePlayerHorizontalSwipe = false
    var lastPlayerPlayButtonType by rememberPreference(lastPlayerPlayButtonTypeKey, PlayerPlayButtonType.Rectangular)
    lastPlayerPlayButtonType = PlayerPlayButtonType.Rectangular
    var colorPaletteName by rememberPreference(colorPaletteNameKey, ColorPaletteName.Dynamic)
    colorPaletteName = ColorPaletteName.Dynamic
    var colorPaletteMode by rememberPreference(colorPaletteModeKey, ColorPaletteMode.Dark)
    colorPaletteMode = ColorPaletteMode.Dark
    var indexNavigationTab by rememberPreference(
        indexNavigationTabKey,
        HomeScreenTabs.Default
    )
    indexNavigationTab = HomeScreenTabs.Default
    var fontType by rememberPreference(fontTypeKey, FontType.Rubik)
    fontType = FontType.Rubik
    var useSystemFont by rememberPreference(useSystemFontKey, false)
    useSystemFont = false
    var applyFontPadding by rememberPreference(applyFontPaddingKey, false)
    applyFontPadding = false
    var isSwipeToActionEnabled by rememberPreference(isSwipeToActionEnabledKey, true)
    isSwipeToActionEnabled = true
    var disableClosingPlayerSwipingDown by rememberPreference(disableClosingPlayerSwipingDownKey, false)
    disableClosingPlayerSwipingDown = false
    var showSearchTab by rememberPreference(showSearchTabKey, false)
    showSearchTab = false
    var showStatsInNavbar by rememberPreference(showStatsInNavbarKey, false)
    showStatsInNavbar = false
    var maxStatisticsItems by rememberPreference(
        maxStatisticsItemsKey,
        MaxStatisticsItems.`10`
    )
    maxStatisticsItems = MaxStatisticsItems.`10`
    var showStatsListeningTime by rememberPreference(showStatsListeningTimeKey,   true)
    showStatsListeningTime = true
    var maxTopPlaylistItems by rememberPreference(
        MaxTopPlaylistItemsKey,
        MaxTopPlaylistItems.`10`
    )
    maxTopPlaylistItems = MaxTopPlaylistItems.`10`
    var navigationBarPosition by rememberPreference(navigationBarPositionKey, NavigationBarPosition.Bottom)
    navigationBarPosition = NavigationBarPosition.Bottom
    var navigationBarType by rememberPreference(navigationBarTypeKey, NavigationBarType.IconAndText)
    navigationBarType = NavigationBarType.IconAndText
    var pauseBetweenSongs  by rememberPreference(pauseBetweenSongsKey, PauseBetweenSongs.`0`)
    pauseBetweenSongs = PauseBetweenSongs.`0`
    var maxSongsInQueue  by rememberPreference(maxSongsInQueueKey, MaxSongs.`500`)
    maxSongsInQueue = MaxSongs.`500`
    var thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )
    thumbnailRoundness = ThumbnailRoundness.Heavy
    var showFavoritesPlaylist by rememberPreference(showFavoritesPlaylistKey, true)
    showFavoritesPlaylist = true
    var showMyTopPlaylist by rememberPreference(showMyTopPlaylistKey, true)
    showMyTopPlaylist = true
    var showDownloadedPlaylist by rememberPreference(showDownloadedPlaylistKey, true)
    showDownloadedPlaylist = true
    var showOnDevicePlaylist by rememberPreference(showOnDevicePlaylistKey, true)
    showOnDevicePlaylist = true
    var shakeEventEnabled by rememberPreference(shakeEventEnabledKey, false)
    shakeEventEnabled = false
    var useVolumeKeysToChangeSong by rememberPreference(useVolumeKeysToChangeSongKey, false)
    useVolumeKeysToChangeSong = false
    var showFloatingIcon by rememberPreference(showFloatingIconKey, false)
    showFloatingIcon = false
    var menuStyle by rememberPreference(menuStyleKey, MenuStyle.List)
    menuStyle = MenuStyle.List
    var transitionEffect by rememberPreference(transitionEffectKey, TransitionEffect.Scale)
    transitionEffect = TransitionEffect.Scale
    var enableCreateMonthlyPlaylists by rememberPreference(enableCreateMonthlyPlaylistsKey, true)
    enableCreateMonthlyPlaylists = true
    var showPipedPlaylists by rememberPreference(showPipedPlaylistsKey, true)
    showPipedPlaylists = true
    var showPinnedPlaylists by rememberPreference(showPinnedPlaylistsKey, true)
    showPinnedPlaylists = true
    var showMonthlyPlaylists by rememberPreference(showMonthlyPlaylistsKey, true)
    showMonthlyPlaylists = true
    var customThemeLight_Background0 by rememberPreference(customThemeLight_Background0Key, DefaultLightColorPalette.background0.hashCode())
    customThemeLight_Background0 = DefaultLightColorPalette.background0.hashCode()
    var customThemeLight_Background1 by rememberPreference(customThemeLight_Background1Key, DefaultLightColorPalette.background1.hashCode())
    customThemeLight_Background1 = DefaultLightColorPalette.background1.hashCode()
    var customThemeLight_Background2 by rememberPreference(customThemeLight_Background2Key, DefaultLightColorPalette.background2.hashCode())
    customThemeLight_Background2 = DefaultLightColorPalette.background2.hashCode()
    var customThemeLight_Background3 by rememberPreference(customThemeLight_Background3Key, DefaultLightColorPalette.background3.hashCode())
    customThemeLight_Background3 = DefaultLightColorPalette.background3.hashCode()
    var customThemeLight_Background4 by rememberPreference(customThemeLight_Background4Key, DefaultLightColorPalette.background4.hashCode())
    customThemeLight_Background4 = DefaultLightColorPalette.background4.hashCode()
    var customThemeLight_Text by rememberPreference(customThemeLight_TextKey, DefaultLightColorPalette.text.hashCode())
    customThemeLight_Text = DefaultLightColorPalette.text.hashCode()
    var customThemeLight_TextSecondary by rememberPreference(customThemeLight_textSecondaryKey, DefaultLightColorPalette.textSecondary.hashCode())
    customThemeLight_TextSecondary = DefaultLightColorPalette.textSecondary.hashCode()
    var customThemeLight_TextDisabled by rememberPreference(customThemeLight_textDisabledKey, DefaultLightColorPalette.textDisabled.hashCode())
    customThemeLight_TextDisabled = DefaultLightColorPalette.textDisabled.hashCode()
    var customThemeLight_IconButtonPlayer by rememberPreference(customThemeLight_iconButtonPlayerKey, DefaultLightColorPalette.iconButtonPlayer.hashCode())
    customThemeLight_IconButtonPlayer = DefaultLightColorPalette.iconButtonPlayer.hashCode()
    var customThemeLight_Accent by rememberPreference(customThemeLight_accentKey, DefaultLightColorPalette.accent.hashCode())
    customThemeLight_Accent = DefaultLightColorPalette.accent.hashCode()
    var customThemeDark_Background0 by rememberPreference(customThemeDark_Background0Key, DefaultDarkColorPalette.background0.hashCode())
    customThemeDark_Background0 = DefaultDarkColorPalette.background0.hashCode()
    var customThemeDark_Background1 by rememberPreference(customThemeDark_Background1Key, DefaultDarkColorPalette.background1.hashCode())
    customThemeDark_Background1 = DefaultDarkColorPalette.background1.hashCode()
    var customThemeDark_Background2 by rememberPreference(customThemeDark_Background2Key, DefaultDarkColorPalette.background2.hashCode())
    customThemeDark_Background2 = DefaultDarkColorPalette.background2.hashCode()
    var customThemeDark_Background3 by rememberPreference(customThemeDark_Background3Key, DefaultDarkColorPalette.background3.hashCode())
    customThemeDark_Background3 = DefaultDarkColorPalette.background3.hashCode()
    var customThemeDark_Background4 by rememberPreference(customThemeDark_Background4Key, DefaultDarkColorPalette.background4.hashCode())
    customThemeDark_Background4 = DefaultDarkColorPalette.background4.hashCode()
    var customThemeDark_Text by rememberPreference(customThemeDark_TextKey, DefaultDarkColorPalette.text.hashCode())
    customThemeDark_Text = DefaultDarkColorPalette.text.hashCode()
    var customThemeDark_TextSecondary by rememberPreference(customThemeDark_textSecondaryKey, DefaultDarkColorPalette.textSecondary.hashCode())
    customThemeDark_TextSecondary = DefaultDarkColorPalette.textSecondary.hashCode()
    var customThemeDark_TextDisabled by rememberPreference(customThemeDark_textDisabledKey, DefaultDarkColorPalette.textDisabled.hashCode())
    customThemeDark_TextDisabled = DefaultDarkColorPalette.textDisabled.hashCode()
    var customThemeDark_IconButtonPlayer by rememberPreference(customThemeDark_iconButtonPlayerKey, DefaultDarkColorPalette.iconButtonPlayer.hashCode())
    customThemeDark_IconButtonPlayer = DefaultDarkColorPalette.iconButtonPlayer.hashCode()
    var customThemeDark_Accent by rememberPreference(customThemeDark_accentKey, DefaultDarkColorPalette.accent.hashCode())
    customThemeDark_Accent = DefaultDarkColorPalette.accent.hashCode()
    var resetCustomLightThemeDialog by rememberSaveable { mutableStateOf(false) }
    resetCustomLightThemeDialog = false
    var resetCustomDarkThemeDialog by rememberSaveable { mutableStateOf(false) }
    resetCustomDarkThemeDialog = false
    var playbackFadeAudioDuration by rememberPreference(playbackFadeAudioDurationKey, DurationInMilliseconds.Disabled)
    playbackFadeAudioDuration = DurationInMilliseconds.Disabled
    var playerPosition by rememberPreference(playerPositionKey, PlayerPosition.Bottom)
    playerPosition = PlayerPosition.Bottom
    var excludeSongWithDurationLimit by rememberPreference(excludeSongsWithDurationLimitKey, DurationInMinutes.Disabled)
    excludeSongWithDurationLimit = DurationInMinutes.Disabled
    var playlistindicator by rememberPreference(playlistindicatorKey, false)
    playlistindicator = false
    var discoverIsEnabled by rememberPreference(discoverKey, false)
    discoverIsEnabled = false
    var isPauseOnVolumeZeroEnabled by rememberPreference(isPauseOnVolumeZeroEnabledKey, false)
    isPauseOnVolumeZeroEnabled = false
    var messageType by rememberPreference(messageTypeKey, MessageType.Modern)
    messageType = MessageType.Modern
    var minimumSilenceDuration by rememberPreference(minimumSilenceDurationKey, 2_000_000L)
    minimumSilenceDuration = 2_000_000L
    var pauseListenHistory by rememberPreference(pauseListenHistoryKey, false)
    pauseListenHistory = false
    var showTopActionsBar by rememberPreference(showTopActionsBarKey, true)
    showTopActionsBar = true
    var playerControlsType by rememberPreference(playerControlsTypeKey, PlayerControlsType.Essential)
    playerControlsType = PlayerControlsType.Modern
    var playerInfoType by rememberPreference(playerInfoTypeKey, PlayerInfoType.Essential)
    playerInfoType = PlayerInfoType.Modern
    var playerType by rememberPreference(playerTypeKey, PlayerType.Essential)
    playerType = PlayerType.Essential
    var queueType by rememberPreference(queueTypeKey, QueueType.Essential)
    queueType = QueueType.Essential
    var fadingedge by rememberPreference(fadingedgeKey, false)
    fadingedge = false
    var carousel by rememberPreference(carouselKey, true)
    carousel = true
    var carouselSize by rememberPreference(carouselSizeKey, CarouselSize.Biggest)
    carouselSize = CarouselSize.Biggest
    var thumbnailType by rememberPreference(thumbnailTypeKey, ThumbnailType.Modern)
    thumbnailType = ThumbnailType.Modern
    var playerTimelineType by rememberPreference(playerTimelineTypeKey, PlayerTimelineType.FakeAudioBar)
    playerTimelineType = PlayerTimelineType.Default
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
    var playerInfoShowIcons by rememberPreference(playerInfoShowIconsKey, true)
    playerInfoShowIcons = true
    var miniPlayerType by rememberPreference(
        miniPlayerTypeKey,
        MiniPlayerType.Modern
    )
    miniPlayerType = MiniPlayerType.Modern
    var playerSwapControlsWithTimeline by rememberPreference(
        playerSwapControlsWithTimelineKey,
        false
    )
    playerSwapControlsWithTimeline = false
    var playerPlayButtonType by rememberPreference(
        playerPlayButtonTypeKey,
        PlayerPlayButtonType.Disabled
    )
    playerPlayButtonType = PlayerPlayButtonType.Disabled
    var buttonzoomout by rememberPreference(buttonzoomoutKey, false)
    buttonzoomout = false
    var iconLikeType by rememberPreference(iconLikeTypeKey, IconLikeType.Essential)
    iconLikeType = IconLikeType.Essential
    var playerBackgroundColors by rememberPreference(
        playerBackgroundColorsKey,
        PlayerBackgroundColors.BlurredCoverColor
    )
    playerBackgroundColors = PlayerBackgroundColors.BlurredCoverColor
    var blackgradient by rememberPreference(blackgradientKey, false)
    blackgradient = false
    var showTotalTimeQueue by rememberPreference(showTotalTimeQueueKey, true)
    showTotalTimeQueue = true
    var showNextSongsInPlayer by rememberPreference(showNextSongsInPlayerKey, false)
    showNextSongsInPlayer = false
    var showRemainingSongTime by rememberPreference(showRemainingSongTimeKey, true)
    showRemainingSongTime = true
    var disableScrollingText by rememberPreference(disableScrollingTextKey, false)
    disableScrollingText = false
    var effectRotationEnabled by rememberPreference(effectRotationKey, true)
    effectRotationEnabled = true
    var thumbnailTapEnabled by rememberPreference(thumbnailTapEnabledKey, true)
    thumbnailTapEnabled = true
    var clickLyricsText by rememberPreference(clickOnLyricsTextKey, true)
    clickLyricsText = true
    var backgroundProgress by rememberPreference(
        backgroundProgressKey,
        BackgroundProgress.MiniPlayer
    )
    backgroundProgress = BackgroundProgress.MiniPlayer
    var transparentBackgroundActionBarPlayer by rememberPreference(
        transparentBackgroundPlayerActionBarKey,
        false
    )
    transparentBackgroundActionBarPlayer = false
    var actionspacedevenly by rememberPreference(actionspacedevenlyKey, false)
    actionspacedevenly = false
    var tapqueue by rememberPreference(tapqueueKey, true)
    tapqueue = true
    var swipeUpQueue by rememberPreference(swipeUpQueueKey, true)
    swipeUpQueue = true
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
    var queueSwipeLeftAction by rememberPreference(queueSwipeLeftActionKey, QueueSwipeAction.RemoveFromQueue)
    queueSwipeLeftAction = QueueSwipeAction.RemoveFromQueue
    var queueSwipeRightAction by rememberPreference(queueSwipeRightActionKey, QueueSwipeAction.PlayNext)
    queueSwipeRightAction = QueueSwipeAction.PlayNext

    var playlistSwipeLeftAction by rememberPreference(playlistSwipeLeftActionKey, PlaylistSwipeAction.Favourite)
    playlistSwipeLeftAction = PlaylistSwipeAction.Favourite
    var playlistSwipeRightAction by rememberPreference(playlistSwipeRightActionKey, PlaylistSwipeAction.PlayNext)
    playlistSwipeRightAction = PlaylistSwipeAction.PlayNext

    var albumSwipeLeftAction by rememberPreference(albumSwipeLeftActionKey, AlbumSwipeAction.PlayNext)
    albumSwipeLeftAction = AlbumSwipeAction.PlayNext
    var albumSwipeRightAction by rememberPreference(albumSwipeRightActionKey, AlbumSwipeAction.Bookmark)
    albumSwipeRightAction = AlbumSwipeAction.Bookmark

    var showButtonPlayerDiscover by rememberPreference(showButtonPlayerDiscoverKey, false)
    showButtonPlayerDiscover = false
    var playerEnableLyricsPopupMessage by rememberPreference(
        playerEnableLyricsPopupMessageKey,
        true
    )
    playerEnableLyricsPopupMessage = true
    var visualizerEnabled by rememberPreference(visualizerEnabledKey, false)
    visualizerEnabled = false
    var showthumbnail by rememberPreference(showthumbnailKey, true)
    showthumbnail = true
}

@ExperimentalAnimationApi
@UnstableApi
@Composable
fun UiSettings(
    navController: NavController
) {
    val binder = LocalPlayerServiceBinder.current



    var recommendationsNumber by rememberPreference(recommendationsNumberKey,   RecommendationsNumber.`5`)

    var keepPlayerMinimized by rememberPreference(keepPlayerMinimizedKey,   false)

    var disableIconButtonOnTop by rememberPreference(disableIconButtonOnTopKey, false)
    var lastPlayerTimelineType by rememberPreference(lastPlayerTimelineTypeKey, PlayerTimelineType.Default)
    var lastPlayerThumbnailSize by rememberPreference(lastPlayerThumbnailSizeKey, PlayerThumbnailSize.Medium)
    var disablePlayerHorizontalSwipe by rememberPreference(disablePlayerHorizontalSwipeKey, false)

    var lastPlayerPlayButtonType by rememberPreference(lastPlayerPlayButtonTypeKey, PlayerPlayButtonType.Rectangular)

    var colorPaletteName by rememberPreference(colorPaletteNameKey, ColorPaletteName.Dynamic)
    var colorPaletteMode by rememberPreference(colorPaletteModeKey, ColorPaletteMode.Dark)
    var indexNavigationTab by rememberPreference(
        indexNavigationTabKey,
        HomeScreenTabs.Default
    )
    var fontType by rememberPreference(fontTypeKey, FontType.Rubik)
    var useSystemFont by rememberPreference(useSystemFontKey, false)
    var applyFontPadding by rememberPreference(applyFontPaddingKey, false)
    var isSwipeToActionEnabled by rememberPreference(isSwipeToActionEnabledKey, true)
    var showSearchTab by rememberPreference(showSearchTabKey, false)
    var showStatsInNavbar by rememberPreference(showStatsInNavbarKey, false)

    var maxStatisticsItems by rememberPreference(
        maxStatisticsItemsKey,
        MaxStatisticsItems.`10`
    )

    var showStatsListeningTime by rememberPreference(showStatsListeningTimeKey,   true)

    var maxTopPlaylistItems by rememberPreference(
        MaxTopPlaylistItemsKey,
        MaxTopPlaylistItems.`10`
    )

    var navigationBarPosition by rememberPreference(navigationBarPositionKey, NavigationBarPosition.Bottom)
    var navigationBarType by rememberPreference(navigationBarTypeKey, NavigationBarType.IconAndText)
    val search = Search.init()

    var showFavoritesPlaylist by rememberPreference(showFavoritesPlaylistKey, true)
    var showCachedPlaylist by rememberPreference(showCachedPlaylistKey, true)
    var showMyTopPlaylist by rememberPreference(showMyTopPlaylistKey, true)
    var showDownloadedPlaylist by rememberPreference(showDownloadedPlaylistKey, true)
    var showOnDevicePlaylist by rememberPreference(showOnDevicePlaylistKey, true)
    var showFloatingIcon by rememberPreference(showFloatingIconKey, false)
    var menuStyle by rememberPreference(menuStyleKey, MenuStyle.List)
    var transitionEffect by rememberPreference(transitionEffectKey, TransitionEffect.Scale)
    var enableCreateMonthlyPlaylists by rememberPreference(enableCreateMonthlyPlaylistsKey, true)
    var showPipedPlaylists by rememberPreference(showPipedPlaylistsKey, true)
    var showPinnedPlaylists by rememberPreference(showPinnedPlaylistsKey, true)
    var showMonthlyPlaylists by rememberPreference(showMonthlyPlaylistsKey, true)

    var customThemeLight_Background0 by rememberPreference(customThemeLight_Background0Key, DefaultLightColorPalette.background0.hashCode())
    var customThemeLight_Background1 by rememberPreference(customThemeLight_Background1Key, DefaultLightColorPalette.background1.hashCode())
    var customThemeLight_Background2 by rememberPreference(customThemeLight_Background2Key, DefaultLightColorPalette.background2.hashCode())
    var customThemeLight_Background3 by rememberPreference(customThemeLight_Background3Key, DefaultLightColorPalette.background3.hashCode())
    var customThemeLight_Background4 by rememberPreference(customThemeLight_Background4Key, DefaultLightColorPalette.background4.hashCode())
    var customThemeLight_Text by rememberPreference(customThemeLight_TextKey, DefaultLightColorPalette.text.hashCode())
    var customThemeLight_TextSecondary by rememberPreference(customThemeLight_textSecondaryKey, DefaultLightColorPalette.textSecondary.hashCode())
    var customThemeLight_TextDisabled by rememberPreference(customThemeLight_textDisabledKey, DefaultLightColorPalette.textDisabled.hashCode())
    var customThemeLight_IconButtonPlayer by rememberPreference(customThemeLight_iconButtonPlayerKey, DefaultLightColorPalette.iconButtonPlayer.hashCode())
    var customThemeLight_Accent by rememberPreference(customThemeLight_accentKey, DefaultLightColorPalette.accent.hashCode())

    var customThemeDark_Background0 by rememberPreference(customThemeDark_Background0Key, DefaultDarkColorPalette.background0.hashCode())
    var customThemeDark_Background1 by rememberPreference(customThemeDark_Background1Key, DefaultDarkColorPalette.background1.hashCode())
    var customThemeDark_Background2 by rememberPreference(customThemeDark_Background2Key, DefaultDarkColorPalette.background2.hashCode())
    var customThemeDark_Background3 by rememberPreference(customThemeDark_Background3Key, DefaultDarkColorPalette.background3.hashCode())
    var customThemeDark_Background4 by rememberPreference(customThemeDark_Background4Key, DefaultDarkColorPalette.background4.hashCode())
    var customThemeDark_Text by rememberPreference(customThemeDark_TextKey, DefaultDarkColorPalette.text.hashCode())
    var customThemeDark_TextSecondary by rememberPreference(customThemeDark_textSecondaryKey, DefaultDarkColorPalette.textSecondary.hashCode())
    var customThemeDark_TextDisabled by rememberPreference(customThemeDark_textDisabledKey, DefaultDarkColorPalette.textDisabled.hashCode())
    var customThemeDark_IconButtonPlayer by rememberPreference(customThemeDark_iconButtonPlayerKey, DefaultDarkColorPalette.iconButtonPlayer.hashCode())
    var customThemeDark_Accent by rememberPreference(customThemeDark_accentKey, DefaultDarkColorPalette.accent.hashCode())

    var resetCustomLightThemeDialog by rememberSaveable { mutableStateOf(false) }
    var resetCustomDarkThemeDialog by rememberSaveable { mutableStateOf(false) }
    var playerPosition by rememberPreference(playerPositionKey, PlayerPosition.Bottom)

    var messageType by rememberPreference(messageTypeKey, MessageType.Modern)


    /*  ViMusic Mode Settings  */
    var showTopActionsBar by rememberPreference(showTopActionsBarKey, true)
    var playerControlsType by rememberPreference(playerControlsTypeKey, PlayerControlsType.Essential)
    var playerInfoType by rememberPreference(playerInfoTypeKey, PlayerInfoType.Essential)
    var playerType by rememberPreference(playerTypeKey, PlayerType.Essential)
    var queueType by rememberPreference(queueTypeKey, QueueType.Essential)
    var fadingedge by rememberPreference(fadingedgeKey, false)
    var carousel by rememberPreference(carouselKey, true)
    var carouselSize by rememberPreference(carouselSizeKey, CarouselSize.Biggest)
    var thumbnailType by rememberPreference(thumbnailTypeKey, ThumbnailType.Modern)
    var playerTimelineType by rememberPreference(playerTimelineTypeKey, PlayerTimelineType.FakeAudioBar)
    var playerThumbnailSize by rememberPreference(
        playerThumbnailSizeKey,
        PlayerThumbnailSize.Biggest
    )
    var playerTimelineSize by rememberPreference(
        playerTimelineSizeKey,
        PlayerTimelineSize.Biggest
    )
    var playerInfoShowIcons by rememberPreference(playerInfoShowIconsKey, true)
    var miniPlayerType by rememberPreference(
        miniPlayerTypeKey,
        MiniPlayerType.Modern
    )
    var playerSwapControlsWithTimeline by rememberPreference(
        playerSwapControlsWithTimelineKey,
        false
    )
    var playerPlayButtonType by rememberPreference(
        playerPlayButtonTypeKey,
        PlayerPlayButtonType.Disabled
    )
    var buttonzoomout by rememberPreference(buttonzoomoutKey, false)
    var iconLikeType by rememberPreference(iconLikeTypeKey, IconLikeType.Essential)
    var playerBackgroundColors by rememberPreference(
        playerBackgroundColorsKey,
        PlayerBackgroundColors.BlurredCoverColor
    )
    var blackgradient by rememberPreference(blackgradientKey, false)
    var showTotalTimeQueue by rememberPreference(showTotalTimeQueueKey, true)
    var showNextSongsInPlayer by rememberPreference(showNextSongsInPlayerKey, false)
    var showRemainingSongTime by rememberPreference(showRemainingSongTimeKey, true)
    var disableScrollingText by rememberPreference(disableScrollingTextKey, false)
    var effectRotationEnabled by rememberPreference(effectRotationKey, true)
    var thumbnailTapEnabled by rememberPreference(thumbnailTapEnabledKey, true)
    var clickLyricsText by rememberPreference(clickOnLyricsTextKey, true)
    var backgroundProgress by rememberPreference(
        backgroundProgressKey,
        BackgroundProgress.MiniPlayer
    )
    var transparentBackgroundActionBarPlayer by rememberPreference(
        transparentBackgroundPlayerActionBarKey,
        false
    )
    var actionspacedevenly by rememberPreference(actionspacedevenlyKey, false)
    var tapqueue by rememberPreference(tapqueueKey, true)
    var swipeUpQueue by rememberPreference(swipeUpQueueKey, true)
    var showButtonPlayerAddToPlaylist by rememberPreference(showButtonPlayerAddToPlaylistKey, true)
    var showButtonPlayerArrow by rememberPreference(showButtonPlayerArrowKey, true)
    var showButtonPlayerDownload by rememberPreference(showButtonPlayerDownloadKey, true)
    var showButtonPlayerLoop by rememberPreference(showButtonPlayerLoopKey, true)
    var showButtonPlayerLyrics by rememberPreference(showButtonPlayerLyricsKey, true)
    var expandedplayertoggle by rememberPreference(expandedplayertoggleKey, true)
    var showButtonPlayerShuffle by rememberPreference(showButtonPlayerShuffleKey, true)
    var showButtonPlayerSleepTimer by rememberPreference(showButtonPlayerSleepTimerKey, false)
    var showButtonPlayerMenu by rememberPreference(showButtonPlayerMenuKey, false)
    var showButtonPlayerSystemEqualizer by rememberPreference(
        showButtonPlayerSystemEqualizerKey,
        false
    )
    var showButtonPlayerDiscover by rememberPreference(showButtonPlayerDiscoverKey, false)
    var playerEnableLyricsPopupMessage by rememberPreference(
        playerEnableLyricsPopupMessageKey,
        true
    )
    var visualizerEnabled by rememberPreference(visualizerEnabledKey, false)
    var showthumbnail by rememberPreference(showthumbnailKey, true)
    /*  ViMusic Mode Settings  */

    var queueSwipeLeftAction by rememberPreference(
        queueSwipeLeftActionKey,
        QueueSwipeAction.RemoveFromQueue
    )
    var queueSwipeRightAction by rememberPreference(
        queueSwipeRightActionKey,
        QueueSwipeAction.PlayNext
    )
    var playlistSwipeLeftAction by rememberPreference(
        playlistSwipeLeftActionKey,
        PlaylistSwipeAction.Favourite
    )
    var playlistSwipeRightAction by rememberPreference(
        playlistSwipeRightActionKey,
        PlaylistSwipeAction.PlayNext
    )
    var albumSwipeLeftAction by rememberPreference(
        albumSwipeLeftActionKey,
        AlbumSwipeAction.PlayNext
    )
    var albumSwipeRightAction by rememberPreference(
        albumSwipeRightActionKey,
        AlbumSwipeAction.Bookmark
    )

    var customColor by rememberPreference(customColorKey, Color.Green.hashCode())

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
            title = stringResource(R.string.user_interface),
            iconId = R.drawable.ui,
            enabled = false,
            showIcon = true,
            modifier = Modifier,
            onClick = {}
        )

        search.ToolBarButton()
        search.SearchBar( this )

        if (resetCustomLightThemeDialog) {
            ConfirmationDialog(
                text = stringResource(R.string.do_you_really_want_to_reset_the_custom_light_theme_colors),
                onDismiss = { resetCustomLightThemeDialog = false },
                onConfirm = {
                    resetCustomLightThemeDialog = false
                    customThemeLight_Background0 = DefaultLightColorPalette.background0.hashCode()
                    customThemeLight_Background1 = DefaultLightColorPalette.background1.hashCode()
                    customThemeLight_Background2 = DefaultLightColorPalette.background2.hashCode()
                    customThemeLight_Background3 = DefaultLightColorPalette.background3.hashCode()
                    customThemeLight_Background4 = DefaultLightColorPalette.background4.hashCode()
                    customThemeLight_Text = DefaultLightColorPalette.text.hashCode()
                    customThemeLight_TextSecondary = DefaultLightColorPalette.textSecondary.hashCode()
                    customThemeLight_TextDisabled = DefaultLightColorPalette.textDisabled.hashCode()
                    customThemeLight_IconButtonPlayer = DefaultLightColorPalette.iconButtonPlayer.hashCode()
                    customThemeLight_Accent = DefaultLightColorPalette.accent.hashCode()
                }
            )
        }

        if (resetCustomDarkThemeDialog) {
            ConfirmationDialog(
                text = stringResource(R.string.do_you_really_want_to_reset_the_custom_dark_theme_colors),
                onDismiss = { resetCustomDarkThemeDialog = false },
                onConfirm = {
                    resetCustomDarkThemeDialog = false
                    customThemeDark_Background0 = DefaultDarkColorPalette.background0.hashCode()
                    customThemeDark_Background1 = DefaultDarkColorPalette.background1.hashCode()
                    customThemeDark_Background2 = DefaultDarkColorPalette.background2.hashCode()
                    customThemeDark_Background3 = DefaultDarkColorPalette.background3.hashCode()
                    customThemeDark_Background4 = DefaultDarkColorPalette.background4.hashCode()
                    customThemeDark_Text = DefaultDarkColorPalette.text.hashCode()
                    customThemeDark_TextSecondary = DefaultDarkColorPalette.textSecondary.hashCode()
                    customThemeDark_TextDisabled = DefaultDarkColorPalette.textDisabled.hashCode()
                    customThemeDark_IconButtonPlayer = DefaultDarkColorPalette.iconButtonPlayer.hashCode()
                    customThemeDark_Accent = DefaultDarkColorPalette.accent.hashCode()
                }
            )
        }

        SettingsGroupSpacer()
        SettingsEntryGroupText(stringResource(R.string.user_interface))

        var uiType by rememberPreference(UiTypeKey, UiType.RiMusic)
        if (search.input.isBlank() || stringResource(R.string.interface_in_use).contains(search.input,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.interface_in_use),
                selectedValue = uiType,
                onValueSelected = {
                    uiType = it
                    if (uiType == UiType.ViMusic) {
                        disablePlayerHorizontalSwipe = true
                        disableIconButtonOnTop = true
                        playerTimelineType = PlayerTimelineType.FakeAudioBar
                        visualizerEnabled = false
                        playerThumbnailSize = PlayerThumbnailSize.Medium
                        thumbnailTapEnabled = true
                        showSearchTab = true
                        showStatsInNavbar = true
                        navigationBarPosition = NavigationBarPosition.Left
                        showTopActionsBar = false
                        playerType = PlayerType.Modern
                        queueType = QueueType.Modern
                        fadingedge = false
                        carousel = true
                        carouselSize = CarouselSize.Medium
                        thumbnailType = ThumbnailType.Essential
                        playerTimelineSize = PlayerTimelineSize.Medium
                        playerInfoShowIcons = true
                        miniPlayerType = MiniPlayerType.Modern
                        playerSwapControlsWithTimeline = false
                        transparentBackgroundActionBarPlayer = false
                        playerControlsType = PlayerControlsType.Essential
                        playerPlayButtonType = PlayerPlayButtonType.Disabled
                        buttonzoomout = true
                        iconLikeType = IconLikeType.Essential
                        playerBackgroundColors = PlayerBackgroundColors.CoverColorGradient
                        blackgradient = true
                        showTotalTimeQueue = false
                        showRemainingSongTime = false
                        showNextSongsInPlayer = false
                        disableScrollingText = false
                        effectRotationEnabled = true
                        clickLyricsText = true
                        playerEnableLyricsPopupMessage = true
                        backgroundProgress = BackgroundProgress.MiniPlayer
                        transparentBackgroundActionBarPlayer = true
                        actionspacedevenly = false
                        tapqueue = false
                        swipeUpQueue = true
                        showButtonPlayerDiscover = false
                        showButtonPlayerDownload = false
                        showButtonPlayerAddToPlaylist = false
                        showButtonPlayerLoop = false
                        showButtonPlayerShuffle = false
                        showButtonPlayerLyrics = false
                        expandedplayertoggle = false
                        showButtonPlayerSleepTimer = false
                        showButtonPlayerSystemEqualizer = false
                        showButtonPlayerArrow = false
                        showButtonPlayerShuffle = false
                        showButtonPlayerMenu = true
                        showthumbnail = true
                        keepPlayerMinimized = false
                    } else {
                        disablePlayerHorizontalSwipe = false
                        disableIconButtonOnTop = false
                        playerTimelineType = lastPlayerTimelineType
                        playerThumbnailSize = lastPlayerThumbnailSize
                        playerPlayButtonType = lastPlayerPlayButtonType

                    }

                },
                valueText = {
                    it.name
                }
            )

        if (search.input.isBlank() || stringResource(R.string.theme).contains(search.input,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.theme),
                selectedValue = colorPaletteName,
                onValueSelected = {
                    colorPaletteName = it
                   when (it) {
                       ColorPaletteName.PureBlack,
                       ColorPaletteName.ModernBlack -> colorPaletteMode = ColorPaletteMode.System
                       else -> {}
                   }
                },
                valueText = {
                    when (it) {
                        ColorPaletteName.Default -> stringResource(R.string._default)
                        ColorPaletteName.Dynamic -> stringResource(R.string.dynamic)
                        ColorPaletteName.PureBlack -> stringResource(R.string.theme_pure_black)
                        ColorPaletteName.ModernBlack -> stringResource(R.string.theme_modern_black)
                        ColorPaletteName.MaterialYou -> stringResource(R.string.theme_material_you)
                        ColorPaletteName.Customized -> stringResource(R.string.theme_customized)
                        ColorPaletteName.CustomColor -> stringResource(R.string.customcolor)
                    }
                }
            )

        AnimatedVisibility(visible = colorPaletteName == ColorPaletteName.CustomColor) {
            Column{
            ColorSettingEntry(
                title = stringResource(R.string.customcolor),
                text = "",
                color = Color(customColor),
                onColorSelected = {
                    customColor = it.hashCode()
                },
                modifier = Modifier
                    .padding(start = 25.dp)
            )
            ImportantSettingsDescription(
                text = stringResource(R.string.restarting_rimusic_is_required),
                modifier = Modifier
                .padding(start = 25.dp)
            )
            }
        }
        AnimatedVisibility(visible = colorPaletteName == ColorPaletteName.Customized) {
            Column {
                SettingsEntryGroupText(stringResource(R.string.title_customized_light_theme_colors))
                ButtonBarSettingEntry(
                    title = stringResource(R.string.title_reset_customized_light_colors),
                    text = stringResource(R.string.info_click_to_reset_default_light_colors),
                    icon = R.drawable.trash,
                    onClick = { resetCustomLightThemeDialog = true }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_background_1),
                    text = "",
                    color = Color(customThemeLight_Background0),
                    onColorSelected = {
                        customThemeLight_Background0 = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_background_2),
                    text = "",
                    color = Color(customThemeLight_Background1),
                    onColorSelected = {
                        customThemeLight_Background1 = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_background_3),
                    text = "",
                    color = Color(customThemeLight_Background2),
                    onColorSelected = {
                        customThemeLight_Background2 = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_background_4),
                    text = "",
                    color = Color(customThemeLight_Background3),
                    onColorSelected = {
                        customThemeLight_Background3 = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_background_5),
                    text = "",
                    color = Color(customThemeLight_Background4),
                    onColorSelected = {
                        customThemeLight_Background4 = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_text),
                    text = "",
                    color = Color(customThemeLight_Text),
                    onColorSelected = {
                        customThemeLight_Text= it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_text_secondary),
                    text = "",
                    color = Color(customThemeLight_TextSecondary),
                    onColorSelected = {
                        customThemeLight_TextSecondary = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_text_disabled),
                    text = "",
                    color = Color(customThemeLight_TextDisabled),
                    onColorSelected = {
                        customThemeLight_TextDisabled = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_icon_button_player),
                    text = "",
                    color = Color(customThemeLight_IconButtonPlayer),
                    onColorSelected = {
                        customThemeLight_IconButtonPlayer = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_accent),
                    text = "",
                    color = Color(customThemeLight_Accent),
                    onColorSelected = {
                        customThemeLight_Accent = it.hashCode()
                    }
                )

                SettingsEntryGroupText(stringResource(R.string.title_customized_dark_theme_colors))
                ButtonBarSettingEntry(
                    title = stringResource(R.string.title_reset_customized_dark_colors),
                    text = stringResource(R.string.click_to_reset_default_dark_colors),
                    icon = R.drawable.trash,
                    onClick = { resetCustomDarkThemeDialog = true }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_background_1),
                    text = "",
                    color = Color(customThemeDark_Background0),
                    onColorSelected = {
                        customThemeDark_Background0 = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_background_2),
                    text = "",
                    color = Color(customThemeDark_Background1),
                    onColorSelected = {
                        customThemeDark_Background1 = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_background_3),
                    text = "",
                    color = Color(customThemeDark_Background2),
                    onColorSelected = {
                        customThemeDark_Background2 = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_background_4),
                    text = "",
                    color = Color(customThemeDark_Background3),
                    onColorSelected = {
                        customThemeDark_Background3 = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_background_5),
                    text = "",
                    color = Color(customThemeDark_Background4),
                    onColorSelected = {
                        customThemeDark_Background4 = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_text),
                    text = "",
                    color = Color(customThemeDark_Text),
                    onColorSelected = {
                        customThemeDark_Text= it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_text_secondary),
                    text = "",
                    color = Color(customThemeDark_TextSecondary),
                    onColorSelected = {
                        customThemeDark_TextSecondary = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_text_disabled),
                    text = "",
                    color = Color(customThemeDark_TextDisabled),
                    onColorSelected = {
                        customThemeDark_TextDisabled = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_icon_button_player),
                    text = "",
                    color = Color(customThemeDark_IconButtonPlayer),
                    onColorSelected = {
                        customThemeDark_IconButtonPlayer = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_accent),
                    text = "",
                    color = Color(customThemeDark_Accent),
                    onColorSelected = {
                        customThemeDark_Accent = it.hashCode()
                    }
                )
            }
        }

        if (search.input.isBlank() || stringResource(R.string.theme_mode).contains(search.input,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.theme_mode),
                selectedValue = colorPaletteMode,
                isEnabled = when (colorPaletteName) {
                    ColorPaletteName.PureBlack -> false
                    ColorPaletteName.ModernBlack -> false
                    else -> { true }
                },
                onValueSelected = {
                    colorPaletteMode = it
                    //if (it == ColorPaletteMode.PitchBlack) colorPaletteName = ColorPaletteName.ModernBlack
                },
                valueText = {
                    when (it) {
                        ColorPaletteMode.Dark -> stringResource(R.string.dark)
                        ColorPaletteMode.Light -> stringResource(R.string._light)
                        ColorPaletteMode.System -> stringResource(R.string.system)
                        ColorPaletteMode.PitchBlack -> stringResource(R.string.theme_mode_pitch_black)
                    }
                }
            )

        if (search.input.isBlank() || stringResource(R.string.navigation_bar_position).contains(search.input,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.navigation_bar_position),
                selectedValue = navigationBarPosition,
                onValueSelected = { navigationBarPosition = it },
                // As of version 0.6.53, changing navigation bar to top or bottom
                // while using ViMusic theme breaks the UI
                isEnabled = uiType != UiType.ViMusic,
                valueText = {
                    when (it) {
                        NavigationBarPosition.Left -> stringResource(R.string.direction_left)
                        NavigationBarPosition.Right -> stringResource(R.string.direction_right)
                        NavigationBarPosition.Top -> stringResource(R.string.direction_top)
                        NavigationBarPosition.Bottom -> stringResource(R.string.direction_bottom)
                    }
                }
            )

        if (search.input.isBlank() || stringResource(R.string.navigation_bar_type).contains(search.input,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.navigation_bar_type),
                selectedValue = navigationBarType,
                onValueSelected = { navigationBarType = it },
                valueText = {
                    when (it) {
                        NavigationBarType.IconAndText -> stringResource(R.string.icon_and_text)
                        NavigationBarType.IconOnly -> stringResource(R.string.only_icon)
                    }
                }
            )

        if (search.input.isBlank() || stringResource(R.string.player_position).contains(search.input,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.player_position),
                selectedValue = playerPosition,
                onValueSelected = { playerPosition = it },
                valueText = {
                    when (it) {
                        PlayerPosition.Top -> stringResource(R.string.position_top)
                        PlayerPosition.Bottom -> stringResource(R.string.position_bottom)
                    }
                }
            )

        if (search.input.isBlank() || stringResource(R.string.menu_style).contains(search.input,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.menu_style),
                selectedValue = menuStyle,
                onValueSelected = { menuStyle = it },
                valueText = {
                    when (it) {
                        MenuStyle.Grid -> stringResource(R.string.style_grid)
                        MenuStyle.List -> stringResource(R.string.style_list)
                    }
                }
            )

        if (search.input.isBlank() || stringResource(R.string.message_type).contains(search.input,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.message_type),
                selectedValue = messageType,
                onValueSelected = { messageType = it },
                valueText = {
                    when (it) {
                        MessageType.Modern -> stringResource(R.string.message_type_modern)
                        MessageType.Essential -> stringResource(R.string.message_type_essential)
                    }
                }
            )

        if (search.input.isBlank() || stringResource(R.string.default_page).contains(search.input,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.default_page),
                selectedValue = indexNavigationTab,
                onValueSelected = {indexNavigationTab = it},
                valueText = {
                    when (it) {
                        HomeScreenTabs.Default -> stringResource(R.string._default)
                        HomeScreenTabs.QuickPics -> stringResource(R.string.quick_picks)
                        HomeScreenTabs.Songs -> stringResource(R.string.songs)
                        HomeScreenTabs.Albums -> stringResource(R.string.albums)
                        HomeScreenTabs.Artists -> stringResource(R.string.artists)
                        HomeScreenTabs.Playlists -> stringResource(R.string.playlists)
                        HomeScreenTabs.Search -> stringResource(R.string.search)
                    }
                }
            )

        if (search.input.isBlank() || stringResource(R.string.transition_effect).contains(search.input,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.transition_effect),
                selectedValue = transitionEffect,
                onValueSelected = { transitionEffect = it },
                valueText = {
                    when (it) {
                        TransitionEffect.None -> stringResource(R.string.none)
                        TransitionEffect.Expand -> stringResource(R.string.te_expand)
                        TransitionEffect.Fade -> stringResource(R.string.te_fade)
                        TransitionEffect.Scale -> stringResource(R.string.te_scale)
                        TransitionEffect.SlideVertical -> stringResource(R.string.te_slide_vertical)
                        TransitionEffect.SlideHorizontal -> stringResource(R.string.te_slide_horizontal)
                    }
                }
            )

        if ( UiType.ViMusic.isCurrent() ) {
            if (search.input.isBlank() || stringResource(R.string.vimusic_show_search_button_in_navigation_bar).contains(
                    search.input,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.vimusic_show_search_button_in_navigation_bar),
                    text = stringResource(R.string.vismusic_only_in_left_right_navigation_bar),
                    isChecked = showSearchTab,
                    onCheckedChange = { showSearchTab = it }
                )



            if (search.input.isBlank() || stringResource(R.string.show_statistics_in_navigation_bar).contains(
                    search.input,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.show_statistics_in_navigation_bar),
                    text = "",
                    isChecked = showStatsInNavbar,
                    onCheckedChange = { showStatsInNavbar = it }
                )
        }

        if (uiType == UiType.ViMusic) {
            if (search.input.isBlank() || stringResource(R.string.show_floating_icon).contains(
                    search.input,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.show_floating_icon),
                    text = "",
                    isChecked = showFloatingIcon,
                    onCheckedChange = { showFloatingIcon = it }
                )
        } else showFloatingIcon = false



        if (search.input.isBlank() || stringResource(R.string.settings_use_font_type).contains(search.input,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.settings_use_font_type),
                selectedValue = fontType,
                onValueSelected = { fontType = it },
                valueText = {
                    when (it) {
                        FontType.Rubik -> FontType.Rubik.name
                        FontType.Poppins -> FontType.Poppins.name
                    }
                }
            )

        if (search.input.isBlank() || stringResource(R.string.use_system_font).contains(search.input,true))
            SwitchSettingEntry(
                title = stringResource(R.string.use_system_font),
                text = stringResource(R.string.use_font_by_the_system),
                isChecked = useSystemFont,
                onCheckedChange = { useSystemFont = it }
            )

        if (search.input.isBlank() || stringResource(R.string.apply_font_padding).contains(search.input,true))
            SwitchSettingEntry(
                title = stringResource(R.string.apply_font_padding),
                text = stringResource(R.string.add_spacing_around_texts),
                isChecked = applyFontPadding,
                onCheckedChange = { applyFontPadding = it }
            )


        if (search.input.isBlank() || stringResource(R.string.swipe_to_action).contains(search.input,true))
        {
            SwitchSettingEntry(
                title = stringResource(R.string.swipe_to_action),
                text = stringResource(R.string.activate_the_action_menu_by_swiping_the_song_left_or_right),
                isChecked = isSwipeToActionEnabled,
                onCheckedChange = { isSwipeToActionEnabled = it }
            )

            AnimatedVisibility(visible = isSwipeToActionEnabled) {
                Column(
                    modifier = Modifier.padding(start = 25.dp)
                ) {
                    EnumValueSelectorSettingsEntry<QueueSwipeAction>(
                        title = stringResource(R.string.queue_and_local_playlists_left_swipe),
                        selectedValue = queueSwipeLeftAction,
                        onValueSelected = {
                            queueSwipeLeftAction = it
                        },
                        valueText = {
                            it.displayName
                        },
                    )
                    EnumValueSelectorSettingsEntry<QueueSwipeAction>(
                        title = stringResource(R.string.queue_and_local_playlists_right_swipe),
                        selectedValue = queueSwipeRightAction,
                        onValueSelected = {
                            queueSwipeRightAction = it
                        },
                        valueText = {
                            it.displayName
                        },
                    )
                    EnumValueSelectorSettingsEntry<PlaylistSwipeAction>(
                        title = stringResource(R.string.playlist_left_swipe),
                        selectedValue = playlistSwipeLeftAction,
                        onValueSelected = {
                            playlistSwipeLeftAction = it
                        },
                        valueText = {
                            it.displayName
                        },
                    )
                    EnumValueSelectorSettingsEntry<PlaylistSwipeAction>(
                        title = stringResource(R.string.playlist_right_swipe),
                        selectedValue = playlistSwipeRightAction,
                        onValueSelected = {
                            playlistSwipeRightAction = it
                        },
                        valueText = {
                            it.displayName
                        },
                    )
                    EnumValueSelectorSettingsEntry<AlbumSwipeAction>(
                        title = stringResource(R.string.album_left_swipe),
                        selectedValue = albumSwipeLeftAction,
                        onValueSelected = {
                            albumSwipeLeftAction = it
                        },
                        valueText = {
                            it.displayName
                        },
                    )
                    EnumValueSelectorSettingsEntry<AlbumSwipeAction>(
                        title = stringResource(R.string.album_right_swipe),
                        selectedValue = albumSwipeRightAction,
                        onValueSelected = {
                            albumSwipeRightAction = it
                        },
                        valueText = {
                            it.displayName
                        },
                    )
                }
            }
        }

        SettingsGroupSpacer()
        SettingsEntryGroupText(title = stringResource(R.string.songs).uppercase())

        if (search.input.isBlank() || "${stringResource(R.string.show)} ${stringResource(R.string.favorites)}".contains(search.input,true))
            SwitchSettingEntry(
                title = "${stringResource(R.string.show)} ${stringResource(R.string.favorites)}",
                text = "",
                isChecked = showFavoritesPlaylist,
                onCheckedChange = { showFavoritesPlaylist = it }
            )

        if (search.input.isBlank() || "${stringResource(R.string.show)} ${stringResource(R.string.cached)}".contains(search.input,true))
            SwitchSettingEntry(
                title = "${stringResource(R.string.show)} ${stringResource(R.string.cached)}",
                text = "",
                isChecked = showCachedPlaylist,
                onCheckedChange = { showCachedPlaylist = it }
            )

        if (search.input.isBlank() || "${stringResource(R.string.show)} ${stringResource(R.string.downloaded)}".contains(search.input,true))
            SwitchSettingEntry(
                title = "${stringResource(R.string.show)} ${stringResource(R.string.downloaded)}",
                text = "",
                isChecked = showDownloadedPlaylist,
                onCheckedChange = { showDownloadedPlaylist = it }
            )
        if (search.input.isBlank() || "${stringResource(R.string.show)} ${stringResource(R.string.my_playlist_top)}".contains(search.input,true))
            SwitchSettingEntry(
                title = "${stringResource(R.string.show)} ${stringResource(R.string.my_playlist_top).format(maxTopPlaylistItems)}",
                text = "",
                isChecked = showMyTopPlaylist,
                onCheckedChange = { showMyTopPlaylist = it }
            )
        if (search.input.isBlank() || "${stringResource(R.string.show)} ${stringResource(R.string.on_device)}".contains(search.input,true))
            SwitchSettingEntry(
                title = "${stringResource(R.string.show)} ${stringResource(R.string.on_device)}",
                text = "",
                isChecked = showOnDevicePlaylist,
                onCheckedChange = { showOnDevicePlaylist = it }
            )

        /*
        SettingsGroupSpacer()
        SettingsEntryGroupText(title = stringResource(R.string.playlists).uppercase())

        if (filter.isNullOrBlank() || "${stringResource(R.string.show)} ${stringResource(R.string.playlists)}".contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = "${stringResource(R.string.show)} ${stringResource(R.string.playlists)}",
                text = "",
                isChecked = showPlaylists,
                onCheckedChange = { showPlaylists = it }
            )
        if (filter.isNullOrBlank() || "${stringResource(R.string.show)} ${stringResource(R.string.monthly_playlists)}".contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = "${stringResource(R.string.show)} ${stringResource(R.string.monthly_playlists)}",
                text = "",
                isChecked = showMonthlyPlaylistInLibrary,
                onCheckedChange = { showMonthlyPlaylistInLibrary = it }
            )
         */

        SettingsGroupSpacer()
        SettingsEntryGroupText(title = stringResource(R.string.playlists).uppercase())

        if (search.input.isBlank() || "${stringResource(R.string.show)} ${stringResource(R.string.piped_playlists)}".contains(search.input,true))
            SwitchSettingEntry(
                title = "${stringResource(R.string.show)} ${stringResource(R.string.piped_playlists)}",
                text = "",
                isChecked = showPipedPlaylists,
                onCheckedChange = { showPipedPlaylists = it }
            )

        if (search.input.isBlank() || "${stringResource(R.string.show)} ${stringResource(R.string.pinned_playlists)}".contains(search.input,true))
            SwitchSettingEntry(
                title = "${stringResource(R.string.show)} ${stringResource(R.string.pinned_playlists)}",
                text = "",
                isChecked = showPinnedPlaylists,
                onCheckedChange = { showPinnedPlaylists = it }
            )

        if (search.input.isBlank() || "${stringResource(R.string.show)} ${stringResource(R.string.monthly_playlists)}".contains(search.input,true))
            SwitchSettingEntry(
                title = "${stringResource(R.string.show)} ${stringResource(R.string.monthly_playlists)}",
                text = "",
                isChecked = showMonthlyPlaylists,
                onCheckedChange = { showMonthlyPlaylists = it }
            )

        SettingsGroupSpacer()
        SettingsEntryGroupText(stringResource(R.string.monthly_playlists).uppercase())

        if (search.input.isBlank() || stringResource(R.string.monthly_playlists).contains(search.input,true))
            SwitchSettingEntry(
                title = stringResource(R.string.enable_monthly_playlists_creation),
                text = "",
                isChecked = enableCreateMonthlyPlaylists,
                onCheckedChange = {
                    enableCreateMonthlyPlaylists = it
                }
            )

        SettingsGroupSpacer()
        SettingsEntryGroupText(stringResource(R.string.smart_recommendations))

        if (search.input.isBlank() || stringResource(R.string.statistics_max_number_of_items).contains(search.input,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.statistics_max_number_of_items),
                selectedValue = recommendationsNumber,
                onValueSelected = { recommendationsNumber = it },
                valueText = {
                    it.number.toString()
                }
            )

        SettingsGroupSpacer()
        SettingsEntryGroupText(stringResource(R.string.statistics))

        if (search.input.isBlank() || stringResource(R.string.statistics_max_number_of_items).contains(search.input,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.statistics_max_number_of_items),
                selectedValue = maxStatisticsItems,
                onValueSelected = { maxStatisticsItems = it },
                valueText = {
                    it.number.toString()
                }
            )

        if (search.input.isBlank() || stringResource(R.string.listening_time).contains(search.input,true))
            SwitchSettingEntry(
                title = stringResource(R.string.listening_time),
                text = stringResource(R.string.shows_the_number_of_songs_heard_and_their_listening_time),
                isChecked = showStatsListeningTime,
                onCheckedChange = {
                    showStatsListeningTime = it
                }
            )

        SettingsGroupSpacer()
        SettingsEntryGroupText(stringResource(R.string.playlist_top))

        if (search.input.isBlank() || stringResource(R.string.statistics_max_number_of_items).contains(search.input,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.statistics_max_number_of_items),
                selectedValue = maxTopPlaylistItems,
                onValueSelected = { maxTopPlaylistItems = it },
                valueText = {
                    it.number.toString()
                }
            )

        var resetToDefault by remember { mutableStateOf(false) }
        val context = LocalContext.current
        ButtonBarSettingEntry(
            title = stringResource(R.string.settings_reset),
            text = stringResource(R.string.settings_restore_default_settings),
            icon = R.drawable.refresh,
            iconColor = colorPalette().text,
            onClick = { resetToDefault = true },
        )
        if (resetToDefault) {
            DefaultUiSettings()
            resetToDefault = false
            navController.popBackStack()
            SmartMessage(stringResource(R.string.done), context = context)
        }

        SettingsGroupSpacer(
            modifier = Modifier.height(Dimensions.bottomSpacer)
        )

    }
}
