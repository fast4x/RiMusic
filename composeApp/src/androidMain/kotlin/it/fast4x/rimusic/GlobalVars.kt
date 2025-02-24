package it.fast4x.rimusic

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import it.fast4x.rimusic.ui.styling.LocalAppearance
import it.fast4x.rimusic.utils.autosyncKey
import it.fast4x.rimusic.utils.bassboostEnabledKey
import it.fast4x.rimusic.utils.bassboostLevelKey
import it.fast4x.rimusic.utils.handleAudioFocusEnabledKey
import it.fast4x.rimusic.utils.isConnectionMetered
import it.fast4x.rimusic.utils.isConnectionMeteredEnabledKey
import it.fast4x.rimusic.utils.logDebugEnabledKey
import it.fast4x.rimusic.utils.preferences
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.showButtonPlayerVideoKey
import it.fast4x.rimusic.utils.showSearchTabKey
import it.fast4x.rimusic.utils.showStatsInNavbarKey
import it.fast4x.rimusic.utils.useYtLoginOnlyForBrowseKey
import it.fast4x.rimusic.utils.ytAccountNameKey
import it.fast4x.rimusic.utils.ytAccountThumbnailKey

@Composable
fun typography() = LocalAppearance.current.typography

@Composable
@ReadOnlyComposable
fun colorPalette() = LocalAppearance.current.colorPalette

@Composable
fun thumbnailShape() = LocalAppearance.current.thumbnailShape

@Composable
fun showSearchIconInNav() = rememberPreference( showSearchTabKey, false ).value

@Composable
fun showStatsIconInNav() = rememberPreference( showStatsInNavbarKey, false ).value

@Composable
fun binder() = LocalPlayerServiceBinder.current?.service

fun appContext(): Context = Dependencies.application.applicationContext
fun context(): Context = Dependencies.application

fun ytAccountName() = appContext().preferences.getString(ytAccountNameKey, "")
fun ytAccountThumbnail() = appContext().preferences.getString(ytAccountThumbnailKey, "")
fun useYtLoginOnlyForBrowse() = appContext().preferences.getBoolean(useYtLoginOnlyForBrowseKey, false)
fun isVideoEnabled() = appContext().preferences.getBoolean(showButtonPlayerVideoKey, false)

fun isConnectionMetered() = appContext().isConnectionMetered()
fun isConnectionMeteredEnabled() = appContext().preferences.getBoolean(isConnectionMeteredEnabledKey, true)
fun isAutoSyncEnabled() = appContext().preferences.getBoolean(autosyncKey, false)
fun isHandleAudioFocusEnabled() = appContext().preferences.getBoolean(handleAudioFocusEnabledKey, true)
fun isBassBoostEnabled() = appContext().preferences.getBoolean(bassboostEnabledKey, false)
fun isDebugModeEnabled() = appContext().preferences.getBoolean(logDebugEnabledKey, false)