package it.fast4x.rimusic.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import it.fast4x.environment.Environment
import it.fast4x.environment.requests.HomePage
import it.fast4x.rimusic.models.Song
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber

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
const val otherLanguageAppKey = "otherLanguageApp"
const val indexNavigationTabKey = "indexNavigationTab"
const val effectRotationKey = "effectRotation"
const val playerThumbnailSizeKey = "playerThumbnailSize"
const val playerThumbnailSizeLKey = "playerThumbnailSizeL"
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
const val queueLoopTypeKey = "queueLoopType"
const val reorderInQueueEnabledKey = "reorderInQueueEnabled"
const val skipSilenceKey = "skipSilence"
const val skipMediaOnErrorKey = "skipMediaOnError"
const val volumeNormalizationKey = "volumeNormalization"
const val resumePlaybackWhenDeviceConnectedKey = "resumePlaybackWhenDeviceConnected"
const val persistentQueueKey = "persistentQueue"
const val resumePlaybackOnStartKey = "resumePlaybackOnStart"
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
const val coilCustomDiskCacheKey = "exoPlayerCustomCache"
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
const val showButtonPlayerStartRadioKey = "showButtonPlayerStartRadio"
const val showButtonPlayerSystemEqualizerKey = "showButtonPlayerSystemEqualizer"
const val showButtonPlayerDiscoverKey = "showButtonPlayerDiscover"
const val showButtonPlayerVideoKey = "showButtonPlayerVideo"
const val disableClosingPlayerSwipingDownKey = "disableClosingPlayerSwipingDown"
const val showSearchTabKey = "showSearchTab"
const val MaxTopPlaylistItemsKey = "MaxTopPlaylistItems"
const val topPlaylistPeriodKey = "topPlaylistPeriod"
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
const val playbackDurationKey = "playbackDuration"
const val blurStrengthKey = "blurScale"
//const val blurStrength2Key = "blurScale"
const val blurDarkenFactorKey = "blurDarkenFactor"
const val playbackFadeDurationKey = "playbackFadeDuration"
const val playbackFadeAudioDurationKey = "playbackFadeAudioDuration"
const val showTotalTimeQueueKey = "showTotalTimeQueue"
const val backgroundProgressKey = "backgroundProgress"
const val maxSongsInQueueKey = "maxSongsInQueue"
const val showFoldersOnDeviceKey = "showFoldersOnDevice"
const val showNextSongsInPlayerKey = "showNextSongsInPlayer"
const val showRemainingSongTimeKey = "showRemainingSongTime"
const val lyricsFontSizeKey = "lyricsFontSize"
const val showBackgroundLyricsKey = "showBackgroundLyrics"
const val includeLocalSongsKey = "includeLocalSongs"
const val clickOnLyricsTextKey = "clickOnLyricsText"
const val defaultFolderKey = "defaultFolder"
const val menuStyleKey = "menuStyle"
const val shakeEventEnabledKey = "shakeEventEnabled"
const val useVolumeKeysToChangeSongKey = "useVolumeKeysToChangeSong"
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
const val animatedGradientKey = "animatedGradient"
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
const val historyTypeKey = "historyType"
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
/**** CUSTOM THEME **** */

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
const val buttonStateKey = "buttonState"
const val buttonzoomoutKey = "buttonzoomout"
const val thumbnailpauseKey = "thumbnailpause"
const val showsongsKey = "showsongs"
const val showalbumcoverKey = "showalbumcover"
const val lyricsBackgroundKey = "lyricsBackground"
const val lyricsAlignmentKey = "lyricsAlignment"
const val hideprevnextKey = "hideprevnext"
const val prevNextSongsKey = "prevNextSongs"
const val tapqueueKey = "tapqueue"
const val swipeUpQueueKey = "swipeUpQueue"
const val playlistindicatorKey = "playlistindicator"
const val nowPlayingIndicatorKey = "nowPlayingIndicator"
const val statsfornerdsKey = "statsfornerds"
const val statsfornerdsfullKey = "statsfornerdsfull"
const val discoverKey = "discover"
const val playerTypeKey = "playerType"
const val noblurKey = "noblur"
const val fadingedgeKey = "fadingedge"
const val thumbnailOffsetKey = "thumbnailOffset"
const val thumbnailFadeKey = "thumbnailFade"
const val thumbnailFadeExKey = "thumbnailFadeEx"
const val carouselKey = "carousel"
const val carouselSizeKey = "carouselSize"
const val thumbnailSpacingKey = "thumbnailSpacing"
const val thumbnailSpacingLKey = "thumbnailSpacingL"
const val autosyncKey = "autosync"
const val queueTypeKey = "queueType"


const val parentalControlEnabledKey = "parentalControlEnabled"
const val playerPositionKey = "playerPosition"
const val excludeSongsWithDurationLimitKey = "excludeSongsWithDurationLimit"
const val logDebugEnabledKey = "logDebugEnabled"
const val isPipedEnabledKey = "isPipedEnabled"
const val isPipedCustomEnabledKey = "isPipedCustomEnabled"

const val messageTypeKey = "messageType"
const val isPauseOnVolumeZeroEnabledKey = "isPauseOnVolumeZeroEnabled"
const val playerInfoShowIconsKey = "playerInfoShowIcons"
const val minimumSilenceDurationKey = "minimumSilenceDuration"
const val pauseListenHistoryKey = "pauseListenHistory"
const val selectedCountryCodeKey = "selectedCountryCode"

const val lastVideoIdKey = "lastVideoId"
const val lastVideoSecondsKey = "lastVideoSeconds"
const val isDiscordPresenceEnabledKey = "isDiscordPresenceEnabled"
const val loudnessBaseGainKey = "loudnessBaseGain"
const val statisticsCategoryKey = "statisticsCategory"
const val queueDurationExpandedKey = "queueDurationExpanded"
const val titleExpandedKey = "titleExpanded"
const val timelineExpandedKey = "timelineExpanded"
const val controlsExpandedKey = "controlsExpanded"
const val miniQueueExpandedKey = "miniQueueExpanded"
const val statsExpandedKey = "statsExpanded"
const val actionExpandedKey = "actionExpanded"
//const val showVinylThumbnailAnimationKey = "showVinylThumbnailAnimation"
const val showCoverThumbnailAnimationKey = "showCoverThumbnailAnimation"
const val coverThumbnailAnimationKey = "coverThumbnailAnimation"

const val restartActivityKey = "restartActivity"
const val enableYouTubeLoginKey = "enableYoutubeLogin"
const val enableYouTubeSyncKey = "enableYoutubeSync"
const val useYtLoginOnlyForBrowseKey = "useYtLoginOnlyForBrowse"

const val autoLoadSongsInQueueKey = "autoLoadSongsInQueue"
const val showSecondLineKey = "showSecondLine"
const val VinylSizeKey = "VinylSize"
const val romanizationKey = "romanization"

const val quickPicsTrendingSongKey = "quickPicsTrendingSong"
const val quickPicsRelatedPageKey = "quickPicsRelatedPage"
const val quickPicsChartsPageKey = "quickPicsChartsPage"
const val quickPicsDiscoverPageKey = "quickPicsDiscoverPage"
const val quickPicsHomePageKey = "quickPicsHomePage"
const val loadedDataKey = "loadedData"

const val enablePictureInPictureKey = "enablePicturInPicture"
const val enablePictureInPictureAutoKey = "enablePicturInPictureAuto"
const val pipModuleKey = "pipModule"

const val notificationPlayerFirstIconKey = "notificationPlayerFirstIcon"
const val notificationPlayerSecondIconKey = "notificationPlayerSecondIcon"
const val jumpPreviousKey = "jumpPrevious"

const val artistTypeKey = "artistType"
const val albumTypeKey = "albumType"

const val enableWallpaperKey = "enableWallpaper"
const val wallpaperTypeKey = "wallpaperType"

const val notificationTypeKey = "notificationType"
const val topPaddingKey = "topPadding"

const val autoDownloadSongKey = "autoDownloadSong"
const val autoDownloadSongWhenLikedKey = "autoDownloadSongWhenLiked"
const val autoDownloadSongWhenAlbumBookmarkedKey = "autoDownloadSongWhenAlbumBookmarked"

const val queueSwipeLeftActionKey = "queueSwipeLeftAction"
const val queueSwipeRightActionKey = "queueSwipeRightAction"
const val playlistSwipeLeftActionKey = "playlistSwipeLeftAction"
const val playlistSwipeRightActionKey = "playlistSwipeRightAction"
const val albumSwipeLeftActionKey = "albumSwipeLeftAction"
const val albumSwipeRightActionKey = "albumSwipeRightAction"
const val customColorKey = "customColor"
const val lyricsSizeAnimateKey = "lyricsSizeAnimate"
const val lyricsSizeKey = "lyricsSize"
const val lyricsSizeLKey = "lyricsSizeL"
const val ytAccountNameKey = "ytAccountName"
const val ytAccountEmailKey = "ytAccountEmail"
const val albumCoverRotationKey = "albumCoverRotation"
const val isConnectionMeteredEnabledKey = "isConnectionMeteredEnabled"
const val landscapeControlsKey = "landscapeControls"
const val swipeAnimationsNoThumbnailKey = "swipeAnimationsNoThumbnail"
const val playlistSongsTypeFilterKey = "playlistSongsTypeFilter"

const val ytVisitorDataKey = "ytVisitorData"
const val ytCookieKey = "ytCookie"
const val ytAccountChannelHandleKey = "ytAccountChannelHandle"
const val ytAccountThumbnailKey = "ytAccountThumbnail"
const val ytDataSyncIdKey = "ytDataSyncId"
const val filterByKey = "filterBy"

const val bassboostEnabledKey = "bassboostEnabled"
const val bassboostLevelKey = "bassboostLevel"
const val audioReverbPresetKey = "audioReverbPreset"
const val handleAudioFocusEnabledKey = "handleAudioFocusEnabled"

const val pipedUsernameKey = "pipedUsername"
const val pipedPasswordKey = "pipedPassword"
const val pipedInstanceNameKey = "pipedInstanceName"
const val pipedApiBaseUrlKey = "pipedApiBaseUrl"
const val pipedApiTokenKey = "pipedApiToken"
const val discordPersonalAccessTokenKey = "DiscordPersonalAccessToken"
const val viewTypeKey = "viewType"
const val enableYouTubeHistorySyncKey = "enableYouTubeHistorySync"
const val volumeBoostLevelKey = "volumeBoostLevel"
const val dnsOverHttpsTypeKey = "dnsOverHttpsType"

/*
@PublishedApi
internal val defaultJson = Json {
    isLenient = true
    prettyPrint = false
    ignoreUnknownKeys = true
    encodeDefaults = true
}

@OptIn(InternalSerializationApi::class)
inline fun <reified T : Json> SharedPreferences.Editor.putJson(
    key: String,
    defaultValue: T,
    json: Json = defaultJson
): SharedPreferences.Editor =
    putString(
        key,
        try {
            json.encodeToString(T::class.serializer(), defaultValue)
        } catch (e: IllegalArgumentException) {
            null
        }

    )

@OptIn(InternalSerializationApi::class)
inline fun <reified T : Json> SharedPreferences.getJson(
    key: String,
    defaultValue: T,
    json: Json = defaultJson
): T =
    getString(key, null)?.let {
        try {
            json.decodeFromString(T::class.serializer(), it)
        } catch (e: IllegalArgumentException) {
            null
        }
    } ?: defaultValue
*/

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

/*
@Composable
inline fun <reified T : Json> rememberPreference(key: String, defaultValue: T, json: Json = defaultJson): MutableState<T> {
    val context = LocalContext.current
    return remember {
        mutableStatePreferenceOf(context.preferences.getJson(key, defaultValue)) {
            context.preferences.edit { putJson(key, it) }
        }
    }
}
*/

@Composable
fun rememberPreference(key: String, defaultValue: Song?): MutableState<Song?> {
    val context = LocalContext.current
    val json = Json.encodeToString(defaultValue)
    return remember {
        mutableStatePreferenceOf(
            try {
                context.preferences.getString(key, json)
                    ?.let { Json.decodeFromString<Song>(it) }
            } catch (e: Exception) {
                Timber.e("RememberPreference RelatedPage Error: ${ e.stackTraceToString() }")
                null
            }
        ) {
            context.preferences.edit { putString(
                key,
                Json.encodeToString(it)
            ) }
        }
    }
}

@Composable
fun rememberPreference(key: String, defaultValue: Environment.DiscoverPage?): MutableState<Environment.DiscoverPage?> {
    val context = LocalContext.current
    val json = Json.encodeToString(defaultValue)
    return remember {
        mutableStatePreferenceOf(
            try {
                context.preferences.getString(key, json)
                    ?.let { Json.decodeFromString<Environment.DiscoverPage>(it) }
            } catch (e: Exception) {
                Timber.e("RememberPreference DiscoverPage Error: ${ e.stackTraceToString() }")
                null
            }
        ) {
            context.preferences.edit { putString(
                key,
                Json.encodeToString(it)
            ) }
        }
    }
}

@Composable
fun rememberPreference(key: String, defaultValue: Environment.ChartsPage?): MutableState<Environment.ChartsPage?> {
    val context = LocalContext.current
    val json = Json.encodeToString(defaultValue)
    return remember {
        mutableStatePreferenceOf(
            try {
                context.preferences.getString(key, json)
                    ?.let { Json.decodeFromString<Environment.ChartsPage>(it) }
            } catch (e: Exception) {
                Timber.e("RememberPreference ChartsPage Error: ${ e.stackTraceToString() }")
                null
            }
        ) {
            context.preferences.edit { putString(
                key,
                Json.encodeToString(it)
            ) }
        }
    }
}

@Composable
fun rememberPreference(key: String, defaultValue: Environment.RelatedPage?): MutableState<Environment.RelatedPage?> {
    val context = LocalContext.current
    val json = Json.encodeToString(defaultValue)
    return remember {
        mutableStatePreferenceOf(
            try {
                context.preferences.getString(key, json)
                    ?.let { Json.decodeFromString<Environment.RelatedPage>(it) }
            } catch (e: Exception) {
                Timber.e("RememberPreference RelatedPage Error: ${ e.stackTraceToString() }")
                null
            }
        ) {
            context.preferences.edit { putString(
                key,
                Json.encodeToString(it)
            ) }
        }
    }
}

@Composable
fun rememberPreference(key: String, defaultValue: HomePage?): MutableState<HomePage?> {
    val context = LocalContext.current
    val json = Json.encodeToString(defaultValue)
    return remember {
        mutableStatePreferenceOf(
            try {
                context.preferences.getString(key, json)
                    ?.let { Json.decodeFromString<HomePage>(it) }
            } catch (e: Exception) {
                Timber.e("RememberPreference HomePage Error: ${ e.stackTraceToString() }")
                null
            }
        ) {
            context.preferences.edit { putString(
                key,
                Json.encodeToString(it)
            ) }
        }
    }
}

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
fun rememberPreference(key: String, defaultValue: Long): MutableState<Long> {
    val context = LocalContext.current
    return remember {
        mutableStatePreferenceOf(context.preferences.getLong(key, defaultValue)) {
            context.preferences.edit { putLong(key, it) }
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

fun clearPreference(context: Context, key: String): Unit {
    try {
        context.preferences.edit { remove(key) }
    } catch (e: Exception) {
        Timber.e("ClearPreference Error: ${e.stackTraceToString()}")
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