package it.fast4x.rimusic.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit

const val lastPlayerThumbnailSizeKey = "lastPlayerThumbnailSize"
const val lastPlayerPlayButtonTypeKey = "lastPlayerPlayButtonType"
const val lastPlayerTimelineTypeKey = "lastPlayerTimelineType"
const val lastPlayerVisualizerTypeKey = "lastPlayerVisualizerType"
const val playerPlayButtonTypeKey = "playerPlayButtonType"
const val playerTimelineTypeKey = "playerTimelineType"
const val playerVisualizerTypeKey = "playerVisualizerType"
const val currentVisualizerKey = "currentVisualizer"
const val visualizerEnabledKey = "visualizerEnabled"
const val thumbnailTapEnabledKey = "thumbnailTapEnabled"
const val wavedPlayerTimelineKey = "wavedPlayerTimeline"
const val languageAppKey = "languageApp"
const val indexNavigationTabKey = "indexNavigationTab"
const val effectRotationKey = "effectRotation"
const val playerThumbnailSizeKey = "playerThumbnailSize"
const val playerTimelineSizeKey = "playerTimelineSize"
const val colorPaletteNameKey = "colorPaletteName"
const val colorPaletteModeKey = "colorPaletteMode"
const val thumbnailRoundnessKey = "thumbnailRoundness"
const val coilDiskCacheMaxSizeKey = "coilDiskCacheMaxSize"
const val exoPlayerDiskCacheMaxSizeKey = "exoPlayerDiskCacheMaxSize"
const val exoPlayerDiskDownloadCacheMaxSizeKey = "exoPlayerDiskDownloadCacheMaxSize"
const val exoPlayerMinTimeForEventKey = "exoPlayerMinTimeForEvent"
const val exoPlayerAlternateCacheLocationKey = "exoPlayerAlternateCacheLocation"
const val exoPlayerCacheLocationKey = "exoPlayerCacheLocationKey"
const val isInvincibilityEnabledKey = "isInvincibilityEnabled"
const val useSystemFontKey = "useSystemFont"
const val applyFontPaddingKey = "applyFontPadding"
const val songSortOrderKey = "songSortOrder"
const val songSortByKey = "songSortBy"
const val onDeviceSongSortByKey = "onDeviceSongSortBy"
const val onDeviceFolderSortByKey = "onDeviceFolderSortBy"
const val playlistSortOrderKey = "playlistSortOrder"
const val playlistSortByKey = "playlistSortBy"
const val albumSortOrderKey = "albumSortOrder"
const val albumSortByKey = "albumSortBy"
const val artistSortOrderKey = "artistSortOrder"
const val artistSortByKey = "artistSortBy"
const val trackLoopEnabledKey = "trackLoopEnabled"
const val queueLoopEnabledKey = "queueLoopEnabled"
const val reorderInQueueEnabledKey = "reorderInQueueEnabled"
const val skipSilenceKey = "skipSilence"
const val volumeNormalizationKey = "volumeNormalization"
const val resumePlaybackWhenDeviceConnectedKey = "resumePlaybackWhenDeviceConnected"
const val persistentQueueKey = "persistentQueue"
const val closebackgroundPlayerKey = "closebackgroundPlayer"
const val closeWithBackButtonKey = "closeWithBackButton"
const val isShowingSynchronizedLyricsKey = "isShowingSynchronizedLyrics"
const val isShowingThumbnailInLockscreenKey = "isShowingThumbnailInLockscreen"
const val homeScreenTabIndexKey = "homeScreenTabIndex"
const val searchResultScreenTabIndexKey = "searchResultScreenTabIndex"
const val artistScreenTabIndexKey = "artistScreenTabIndex"
const val pauseSearchHistoryKey = "pauseSearchHistory"
const val UiTypeKey = "UiType"
const val disablePlayerHorizontalSwipeKey = "disablePlayerHorizontalSwipe"
const val disableIconButtonOnTopKey = "disableIconButtonOnTop"
const val exoPlayerCustomCacheKey = "exoPlayerCustomCache"
const val disableScrollingTextKey = "disableScrollingText"
const val audioQualityFormatKey = "audioQualityFormat"
const val showLikeButtonBackgroundPlayerKey = "showLikeButtonBackgroundPlayer"
const val showDownloadButtonBackgroundPlayerKey = "showDownloadButtonBackgroundPlayer"
const val playEventsTypeKey = "playEventsType"
const val fontTypeKey = "fontType"
const val playlistSongSortByKey = "playlistSongSortBy"
const val showTipsKey = "showTips"
const val showChartsKey = "showCharts"
const val showRelatedAlbumsKey = "showRelatedAlbums"
const val showSimilarArtistsKey = "showSimilarArtists"
const val showNewAlbumsArtistsKey = "showNewAlbumsArtists"
const val showNewAlbumsKey = "showNewAlbums"
const val showPlaylistMightLikeKey = "showPlaylistMightLike"
const val showMoodsAndGenresKey = "showMoodsAndGenres"
const val maxStatisticsItemsKey = "maxStatisticsItems"
const val showStatsListeningTimeKey = "showStatsListeningTime"
const val isProxyEnabledKey = "isProxyEnabled"
const val proxyHostnameKey = "proxyHostname"
const val proxyPortKey = "proxyPort"
const val proxyModeKey = "ProxyMode"
const val isRecommendationEnabledKey = "isRecommendationEnabled"
const val showButtonPlayerAddToPlaylistKey = "showButtonPlayerAddToPlaylist"
const val showButtonPlayerArrowKey = "showButtonPlayerArrow"
const val showButtonPlayerDownloadKey = "showButtonPlayerDownload"
const val showButtonPlayerLoopKey = "showButtonPlayerLoop"
const val showButtonPlayerLyricsKey = "showButtonPlayerLyrics"
const val showButtonPlayerShuffleKey = "showButtonPlayerShuffle"
const val isKeepScreenOnEnabledKey = "isKeepScreenOnEnabled"
const val isEnabledDiscoveryLangCodeKey = "isEnabledDiscoveryLangCode"
const val recommendationsNumberKey = "recommendationsNumber"
const val checkUpdateStateKey = "checkUpdateState"
const val showButtonPlayerSleepTimerKey = "showButtonPlayerSleepTimer"
const val keepPlayerMinimizedKey = "keepPlayerMinimized"
const val isSwipeToActionEnabledKey = "isSwipeToActionEnabled"
const val showButtonPlayerMenuKey = "showButtonPlayerMenu"
const val showButtonPlayerSystemEqualizerKey = "showButtonPlayerSystemEqualizer"
const val disableClosingPlayerSwipingDownKey = "disableClosingPlayerSwipingDown"
const val showSearchTabKey = "showSearchTab"
const val MaxTopPlaylistItemsKey = "MaxTopPlaylistItems"
const val navigationBarPositionKey = "navigationBarPosition"
const val navigationBarTypeKey = "navigationBarType"
const val pauseBetweenSongsKey = "pauseBetweenSongs"
const val showFavoritesPlaylistKey = "showFavoritesPlaylist"
const val showCachedPlaylistKey = "showCachedPlaylist"
const val showMyTopPlaylistKey = "showMyTopPlaylist"
const val showDownloadedPlaylistKey = "showDownloadedPlaylist"
const val showOnDevicePlaylistKey = "showOnDevicePlaylist"
const val showPlaylistsKey = "showPlaylists"
const val isGradientBackgroundEnabledKey = "isGradientBackgroundEnabled"
const val playbackSpeedKey = "playbackSpeed"
const val playbackPitchKey = "playbackPitch"
const val playbackVolumeKey = "playbackVolume"
const val playbackDeviceVolumeKey = "playbackDeviceVolume"
const val blurStrengthKey = "blurScale"
//const val blurStrength2Key = "blurScale"
const val blurDarkenFactorKey = "blurDarkenFactor"
const val playbackFadeDurationKey = "playbackFadeDuration"
const val showTotalTimeQueueKey = "showTotalTimeQueue"
const val backgroundProgressKey = "backgroundProgress"
const val maxSongsInQueueKey = "maxSongsInQueue"
const val showFoldersOnDeviceKey = "showFoldersOnDevice"
const val showNextSongsInPlayerKey = "showNextSongsInPlayer"
const val showRemainingSongTimeKey = "showRemainingSongTime"
const val lyricsFontSizeKey = "lyricsFontSize"
const val showBackgroundLyricsKey = "showBackgroundLyrics"
const val includeLocalSongsKey = "includeLocalSongs"
const val clickLyricsTextKey = "clickLyricsText"
const val defaultFolderKey = "defaultFolder"
const val menuStyleKey = "menuStyle"
const val shakeEventEnabledKey = "shakeEventEnabled"
const val showStatsInNavbarKey = "showStatsInNavbar"
const val showActionsBarKey = "showActionsBar"
const val floatActionIconOffsetXkey = "floatActionIconOffsetX"
const val floatActionIconOffsetYkey = "floatActionIconOffsetY"
const val multiFloatActionIconOffsetXkey = "multiFloatActionIconOffsetX"
const val multiFloatActionIconOffsetYkey = "multiFloatActionIconOffsetY"
const val showBuiltinPlaylistsKey = "showBuiltinPlaylists"
const val showPinnedPlaylistsKey = "showPinnedPlaylists"
const val showPlaylistsListKey = "showPlaylistsList"
const val showPlaylistsGeneralKey = "showPlaylistsGeneral"
const val libraryItemSizeKey = "libraryItemSize"
const val albumsItemSizeKey = "albumsItemSize"
const val artistsItemSizeKey = "artistsItemSize"
const val showFloatingIconKey = "showFloatingIcon"
const val transitionEffectKey = "transitionEffect"
const val showMonthlyPlaylistsKey = "showMonthlyPlaylists"
const val showPipedPlaylistsKey = "showPipedPlaylists"
const val showMonthlyPlaylistInQuickPicksKey = "showMonthlyPlaylistInQuickPicks"
const val showMonthlyPlaylistInLibraryKey = "showMonthlyPlaylistInLibrary"
const val enableQuickPicksPageKey = "enableQuickPicksPage"
const val playerBackgroundColorsKey = "playerBackgroundColors"
const val playerControlsTypeKey = "playerControlsType"
const val playerInfoTypeKey = "playerInfoType"
const val showTopActionsBarKey = "showTopActionsBar"
const val transparentBackgroundPlayerActionBarKey = "transparentBackgroundPlayerActionBar"
const val enableCreateMonthlyPlaylistsKey = "enableCreateMonthlyPlaylists"
const val autoShuffleKey = "autoShuffle"
const val builtInPlaylistKey = "builtInPlaylist"
const val playlistTypeKey = "playlistType"
const val iconLikeTypeKey = "iconLikeType"
const val playerSwapControlsWithTimelineKey = "playerSwapControlsWithTimeline"
const val playerEnableLyricsPopupMessageKey = "playerEnableLyricsPopupMessage"
/**** CUSTOM THEME **** */
const val customThemeLight_Background0Key = "customThemeLight_Background0"
const val customThemeLight_Background1Key = "customThemeLight_Background1"
const val customThemeLight_Background2Key = "customThemeLight_Background2"
const val customThemeLight_Background3Key = "customThemeLight_Background3"
const val customThemeLight_Background4Key = "customThemeLight_Background4"
const val customThemeLight_TextKey = "customThemeLight_Text"
const val customThemeLight_textSecondaryKey = "customThemeLight_textSecondary"
const val customThemeLight_textDisabledKey = "customThemeLight_textDisabled"
const val customThemeLight_iconButtonPlayerKey = "customThemeLight_iconButtonPlayer"
const val customThemeLight_accentKey = "customThemeLight_accent"

const val customThemeDark_Background0Key = "customThemeDark_Background0"
const val customThemeDark_Background1Key = "customThemeDark_Background1"
const val customThemeDark_Background2Key = "customThemeDark_Background2"
const val customThemeDark_Background3Key = "customThemeDark_Background3"
const val customThemeDark_Background4Key = "customThemeDark_Background4"
const val customThemeDark_TextKey = "customThemeDark_Text"
const val customThemeDark_textSecondaryKey = "customThemeDark_textSecondary"
const val customThemeDark_textDisabledKey = "customThemeDark_textDisabled"
const val customThemeDark_iconButtonPlayerKey = "customThemeDark_iconButtonPlayer"
const val customThemeDark_accentKey = "customThemeDark_accent"
const val showthumbnailKey = "showthumbnail"
const val showlyricsthumbnailKey = "showlyricsthumbnail"
const val miniPlayerTypeKey = "miniPlayerType"
const val lyricsColorKey = "lyricsColor"
const val lyricsOutlineKey = "lyricsOutline"
const val transparentbarKey = "transparentbar"
const val isShowingLyricsKey = "isShowingLyrics"
const val actionspacedevenlyKey = "actionspacedevenly"
const val expandedplayerKey = "expandedplayer"
const val expandedplayertoggleKey = "expandedplayertoggle"
const val blackgradientKey = "blackgradient"
const val bottomgradientKey = "bottomgradient"
const val textoutlineKey = "textoutline"
const val thumbnailTypeKey = "thumbnailType"
const val showvisthumbnailKey = "showvisthumbnail"
const val extraspaceKey = "extraspace"
const val expandedlyricsKey = "expandedlyrics"
const val lyricsHighlightKey = "lyricsHighlight"
/**** CUSTOM THEME **** */

const val parentalControlEnabledKey = "parentalControlEnabled"
const val playerPositionKey = "playerPosition"
const val excludeSongsWithDurationLimitKey = "excludeSongsWithDurationLimit"
const val logDebugEnabledKey = "logDebugEnabled"
const val isPipedEnabledKey = "isPipedEnabled"

inline fun <reified T : Enum<T>> SharedPreferences.getEnum(
    key: String,
    defaultValue: T
): T =
    getString(key, null)?.let {
        try {
            enumValueOf<T>(it)
        } catch (e: IllegalArgumentException) {
            null
        }
    } ?: defaultValue

inline fun <reified T : Enum<T>> SharedPreferences.Editor.putEnum(
    key: String,
    value: T
): SharedPreferences.Editor =
    putString(key, value.name)

val Context.preferences: SharedPreferences
    get() = getSharedPreferences("preferences", Context.MODE_PRIVATE)

@Composable
fun rememberPreference(key: String, defaultValue: Boolean): MutableState<Boolean> {
    val context = LocalContext.current
    return remember {
        mutableStatePreferenceOf(context.preferences.getBoolean(key, defaultValue)) {
            context.preferences.edit { putBoolean(key, it) }
        }
    }
}

@Composable
fun rememberPreference(key: String, defaultValue: Int): MutableState<Int> {
    val context = LocalContext.current
    return remember {
        mutableStatePreferenceOf(context.preferences.getInt(key, defaultValue)) {
            context.preferences.edit { putInt(key, it) }
        }
    }
}



@Composable
fun rememberPreference(key: String, defaultValue: Float): MutableState<Float> {
    val context = LocalContext.current
    return remember {
        mutableStatePreferenceOf(context.preferences.getFloat(key, defaultValue)) {
            context.preferences.edit { putFloat(key, it) }
        }
    }
}

@Composable
fun rememberPreference(key: String, defaultValue: String): MutableState<String> {
    val context = LocalContext.current
    return remember {
        mutableStatePreferenceOf(context.preferences.getString(key, null) ?: defaultValue) {
            context.preferences.edit { putString(key, it) }
        }
    }
}

@Composable
inline fun <reified T : Enum<T>> rememberPreference(key: String, defaultValue: T): MutableState<T> {
    val context = LocalContext.current
    return remember {
        mutableStatePreferenceOf(context.preferences.getEnum(key, defaultValue)) {
            context.preferences.edit { putEnum(key, it) }
        }
    }
}

inline fun <T> mutableStatePreferenceOf(
    value: T,
    crossinline onStructuralInequality: (newValue: T) -> Unit
) =
    mutableStateOf(
        value = value,
        policy = object : SnapshotMutationPolicy<T> {
            override fun equivalent(a: T, b: T): Boolean {
                val areEquals = a == b
                if (!areEquals) onStructuralInequality(b)
                return areEquals
            }
        })