package it.fast4x.rimusic.ui.screens.settings

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.webkit.CookieManager
import android.webkit.WebStorage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.password
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.ktor.http.Url
import it.fast4x.compose.persist.persistList
import it.fast4x.innertube.utils.parseCookieString
import it.fast4x.piped.Piped
import it.fast4x.piped.models.Instance
import it.fast4x.piped.models.Session
import it.fast4x.rimusic.R
import it.fast4x.rimusic.appContext
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.enums.PopupType
import it.fast4x.rimusic.enums.ThumbnailRoundness
import it.fast4x.rimusic.enums.ValidationType
import it.fast4x.rimusic.extensions.discord.DiscordLoginAndGetToken
import it.fast4x.rimusic.extensions.youtubelogin.YouTubeLogin
import it.fast4x.rimusic.service.PlayerMediaBrowserService
import it.fast4x.rimusic.service.modern.PlayerServiceModern
import it.fast4x.rimusic.thumbnailShape
import it.fast4x.rimusic.ui.components.CustomModalBottomSheet
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.themed.DefaultDialog
import it.fast4x.rimusic.ui.components.themed.HeaderWithIcon
import it.fast4x.rimusic.ui.components.themed.IconButton
import it.fast4x.rimusic.ui.components.themed.Loader
import it.fast4x.rimusic.ui.components.themed.Menu
import it.fast4x.rimusic.ui.components.themed.MenuEntry
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.utils.RestartPlayerService
import it.fast4x.rimusic.utils.textCopyToClipboard
import it.fast4x.rimusic.utils.ytAccountChannelHandleKey
import it.fast4x.rimusic.utils.ytCookieKey
import it.fast4x.rimusic.utils.ytVisitorDataKey
import it.fast4x.rimusic.utils.defaultFolderKey
import it.fast4x.rimusic.utils.discordPersonalAccessTokenKey
import it.fast4x.rimusic.utils.enableYouTubeLoginKey
import it.fast4x.rimusic.utils.enableYouTubeSyncKey
import it.fast4x.rimusic.utils.encryptedPreferences
import it.fast4x.rimusic.utils.extraspaceKey
import it.fast4x.rimusic.utils.isAtLeastAndroid10
import it.fast4x.rimusic.utils.isAtLeastAndroid12
import it.fast4x.rimusic.utils.isAtLeastAndroid6
import it.fast4x.rimusic.utils.isAtLeastAndroid7
import it.fast4x.rimusic.utils.isAtLeastAndroid81
import it.fast4x.rimusic.utils.isDiscordPresenceEnabledKey
import it.fast4x.rimusic.utils.isIgnoringBatteryOptimizations
import it.fast4x.rimusic.utils.isInvincibilityEnabledKey
import it.fast4x.rimusic.utils.isKeepScreenOnEnabledKey
import it.fast4x.rimusic.utils.isPipedCustomEnabledKey
import it.fast4x.rimusic.utils.isPipedEnabledKey
import it.fast4x.rimusic.utils.isProxyEnabledKey
import it.fast4x.rimusic.utils.logDebugEnabledKey
import it.fast4x.rimusic.utils.parentalControlEnabledKey
import it.fast4x.rimusic.utils.pipedApiBaseUrlKey
import it.fast4x.rimusic.utils.pipedApiTokenKey
import it.fast4x.rimusic.utils.pipedInstanceNameKey
import it.fast4x.rimusic.utils.pipedPasswordKey
import it.fast4x.rimusic.utils.pipedUsernameKey
import it.fast4x.rimusic.utils.preferences
import it.fast4x.rimusic.utils.proxyHostnameKey
import it.fast4x.rimusic.utils.proxyModeKey
import it.fast4x.rimusic.utils.proxyPortKey
import it.fast4x.rimusic.utils.rememberEncryptedPreference
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.restartActivityKey
import it.fast4x.rimusic.utils.showFoldersOnDeviceKey
import it.fast4x.rimusic.utils.thumbnailRoundnessKey
import it.fast4x.rimusic.utils.useYtLoginOnlyForBrowseKey
import it.fast4x.rimusic.utils.ytAccountEmailKey
import it.fast4x.rimusic.utils.ytAccountNameKey
import it.fast4x.rimusic.utils.ytAccountThumbnailKey
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.net.Proxy

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("BatteryLife")
@ExperimentalAnimationApi
@Composable
fun AccountsSettings() {
    val context = LocalContext.current
    val thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )

    var restartActivity by rememberPreference(restartActivityKey, false)
    var restartService by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .background(colorPalette().background0)
            //.fillMaxSize()
            .fillMaxHeight()
            .fillMaxWidth(
                if (NavigationBarPosition.Right.isCurrent())
                    Dimensions.contentWidthRightBar
                else
                    1f
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
            title = stringResource(R.string.tab_accounts),
            iconId = R.drawable.person,
            enabled = false,
            showIcon = true,
            modifier = Modifier,
            onClick = {}
        )

        // rememberEncryptedPreference only works correct with API 24 and up

        //TODO MANAGE LOGIN
        /****** YOUTUBE LOGIN ******/

        //var useYtLoginOnlyForBrowse by rememberPreference(useYtLoginOnlyForBrowseKey, false)
        var isYouTubeLoginEnabled by rememberPreference(enableYouTubeLoginKey, false)
        var isYouTubeSyncEnabled by rememberPreference(enableYouTubeSyncKey, false)
        var loginYouTube by remember { mutableStateOf(false) }
        var visitorData by rememberPreference(key = ytVisitorDataKey, defaultValue = "")
        var cookie by rememberPreference(key = ytCookieKey, defaultValue = "")
        var accountName by rememberPreference(key = ytAccountNameKey, defaultValue = "")
        var accountEmail by rememberPreference(key = ytAccountEmailKey, defaultValue = "")
        var accountChannelHandle by rememberPreference(
            key = ytAccountChannelHandleKey,
            defaultValue = ""
        )
        var accountThumbnail by rememberPreference(key = ytAccountThumbnailKey, defaultValue = "")
        var isLoggedIn = remember(cookie) {
            "SAPISID" in parseCookieString(cookie)
        }




        SettingsGroupSpacer()
        SettingsEntryGroupText(title = "YOUTUBE MUSIC")

        SwitchSettingEntry(
            title = "Enable YouTube Music Login",
            text = "",
            isChecked = isYouTubeLoginEnabled,
            onCheckedChange = {
                isYouTubeLoginEnabled = it
                if (!it) {
                    visitorData = ""
                    cookie = ""
                    accountName = ""
                    accountChannelHandle = ""
                    accountEmail = ""
                }
            }
        )

        AnimatedVisibility(visible = isYouTubeLoginEnabled) {
            Column(
                modifier = Modifier.padding(start = 25.dp)
            ) {
                //if (isAtLeastAndroid7) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween

                    ){

                        if (isLoggedIn && accountThumbnail != "")
                            AsyncImage(
                                model = accountThumbnail,
                                contentDescription = null,
                                modifier = Modifier
                                    .height(50.dp)
                                    .clip(thumbnailShape())
                            )

                        Column {
                            ButtonBarSettingEntry(
                                isEnabled = true,
                                title = if (isLoggedIn) "Disconnect" else "Connect",
                                text = if (isLoggedIn) "$accountName ${accountChannelHandle}" else "",
                                icon = R.drawable.ytmusic,
                                iconColor = colorPalette().text,
                                onClick = {
                                    if (isLoggedIn) {
                                        cookie = ""
                                        accountName = ""
                                        accountChannelHandle = ""
                                        accountEmail = ""
                                        accountThumbnail = ""
                                        visitorData = ""
                                        loginYouTube = false
                                        //Delete cookies after logout
                                        val cookieManager = CookieManager.getInstance()
                                        cookieManager.removeAllCookies(null)
                                        cookieManager.flush()
                                        WebStorage.getInstance().deleteAllData()
                                        restartService = true
                                    } else
                                        loginYouTube = true
                                }
                            )
                            /*
                            ImportantSettingsDescription(
                                text = "You need to log in to listen the songs online"
                            )
                             */
                            //SettingsDescription(text = stringResource(R.string.restarting_rimusic_is_required))

                            CustomModalBottomSheet(
                                showSheet = loginYouTube,
                                onDismissRequest = {
//                                    SmartMessage(
//                                        "Restart RiMusic, please",
//                                        type = PopupType.Info,
//                                        context = context
//                                    )
                                    loginYouTube = false
                                },
                                containerColor = colorPalette().background0,
                                contentColor = colorPalette().background0,
                                modifier = Modifier.fillMaxWidth(),
                                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                                dragHandle = {
                                    Surface(
                                        modifier = Modifier.padding(vertical = 0.dp),
                                        color = colorPalette().background0,
                                        shape = thumbnailShape()
                                    ) {}
                                },
                                shape = thumbnailRoundness.shape()
                            ) {
                                YouTubeLogin(
                                    onLogin = { cookieRetrieved ->
                                        if (cookieRetrieved.contains("SAPISID")) {
                                            isLoggedIn = true
                                            loginYouTube = false
                                            SmartMessage(
                                                "Login successful",
                                                type = PopupType.Info,
                                                context = context
                                            )
                                            restartService = true
                                        }

                                    }
                                )
                            }
                            RestartPlayerService(restartService, onRestart = {
                                restartService = false
                                restartActivity = !restartActivity
                            })
                        }

                    }

                //}

//                SwitchSettingEntry(
//                    title = stringResource(R.string.use_ytm_login_only_for_browse),
//                    text = stringResource(R.string.info_use_ytm_login_only_for_browse),
//                    isChecked = useYtLoginOnlyForBrowse,
//                    onCheckedChange = {
//                        useYtLoginOnlyForBrowse = it
//                    }
//                )

                SwitchSettingEntry(
                    //isEnabled = false,
                    title = "Sync data with YTM account",
                    text = "Playlists, albums, artists, history, like, etc.",
                    isChecked = isYouTubeSyncEnabled,
                    onCheckedChange = {
                        isYouTubeSyncEnabled = it
                    }
                )

            }
        }

    /****** YOUTUBE LOGIN ******/

    /****** PIPED ******/

    // rememberEncryptedPreference only works correct with API 24 and up
    if (isAtLeastAndroid7) {
        var isPipedEnabled by rememberPreference(isPipedEnabledKey, false)
        var isPipedCustomEnabled by rememberPreference(isPipedCustomEnabledKey, false)
        var pipedUsername by rememberEncryptedPreference(pipedUsernameKey, "")
        var pipedPassword by rememberEncryptedPreference(pipedPasswordKey, "")
        var pipedInstanceName by rememberEncryptedPreference(pipedInstanceNameKey, "")
        var pipedApiBaseUrl by rememberEncryptedPreference(pipedApiBaseUrlKey, "")
        var pipedApiToken by rememberEncryptedPreference(pipedApiTokenKey, "")

        var loadInstances by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(false) }
        var instances by persistList<Instance>(tag = "otherSettings/pipedInstances")
        var noInstances by remember { mutableStateOf(false) }
        var executeLogin by remember { mutableStateOf(false) }
        var showInstances by remember { mutableStateOf(false) }
        var session by remember {
            mutableStateOf<Result<Session>?>(
                null
            )
        }
        println("OtherSettings bookmark second")

        val menuState = LocalMenuState.current
        val coroutineScope = rememberCoroutineScope()

        if (isLoading)
            DefaultDialog(
                onDismiss = {
                    isLoading = false
                }
            ) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

        if (loadInstances) {
            LaunchedEffect(Unit) {
                isLoading = true
                Piped.getInstances()?.getOrNull()?.let {
                    instances = it
                    //println("mediaItem Instances $it")
                } ?: run { noInstances = true }
                isLoading = false
                showInstances = true
            }
        }
        if (noInstances) {
            SmartMessage("No instances found", type = PopupType.Info, context = context)
        }

        if (executeLogin) {
            LaunchedEffect(Unit) {
                coroutineScope.launch {
                    isLoading = true
                    session = Piped.login(
                        apiBaseUrl = Url(pipedApiBaseUrl), //instances[instanceSelected!!].apiBaseUrl,
                        username = pipedUsername,
                        password = pipedPassword
                    )?.onFailure {
                        Timber.e("Failed piped login ${it.stackTraceToString()}")
                        isLoading = false
                        SmartMessage(
                            "Piped login failed",
                            type = PopupType.Error,
                            context = context
                        )
                        loadInstances = false
                        session = null
                        executeLogin = false
                    }
                    if (session?.isSuccess == false)
                        return@launch

                    SmartMessage(
                        "Piped login successful",
                        type = PopupType.Success,
                        context = context
                    )
                    Timber.i("Piped login successful")

                    session.let {
                        it?.getOrNull()?.token?.let { it1 ->
                            pipedApiToken = it1
                            pipedApiBaseUrl = it.getOrNull()!!.apiBaseUrl.toString()
                        }
                    }

                    isLoading = false
                    loadInstances = false
                    executeLogin = false
                }
            }
        }

        if (showInstances && instances.isNotEmpty()) {
            menuState.display {
                Menu {
                    MenuEntry(
                        icon = R.drawable.chevron_back,
                        text = stringResource(R.string.cancel),
                        onClick = {
                            loadInstances = false
                            showInstances = false
                            menuState.hide()
                        }
                    )
                    instances.forEach {
                        MenuEntry(
                            icon = R.drawable.server,
                            text = it.name,
                            secondaryText = "${it.locationsFormatted} Users: ${it.userCount}",
                            onClick = {
                                menuState.hide()
                                pipedApiBaseUrl = it.apiBaseUrl.toString()
                                pipedInstanceName = it.name
                                /*
                                instances.indexOf(it).let { index ->
                                    //instances[index].apiBaseUrl
                                    instanceSelected = index
                                    //println("mediaItem Instance ${instances[index].apiBaseUrl}")
                                }
                                 */
                                loadInstances = false
                                showInstances = false
                            }
                        )
                    }
                    MenuEntry(
                        icon = R.drawable.chevron_back,
                        text = stringResource(R.string.cancel),
                        onClick = {
                            loadInstances = false
                            showInstances = false
                            menuState.hide()
                        }
                    )
                }
            }
        }


        SettingsGroupSpacer()
        SettingsEntryGroupText(title = stringResource(R.string.piped_account))
        SwitchSettingEntry(
            title = stringResource(R.string.enable_piped_syncronization),
            text = "",
            isChecked = isPipedEnabled,
            onCheckedChange = { isPipedEnabled = it }
        )

        AnimatedVisibility(visible = isPipedEnabled) {
            Column(
                modifier = Modifier.padding(start = 25.dp)
            ) {
                SwitchSettingEntry(
                    title = stringResource(R.string.piped_custom_instance),
                    text = "",
                    isChecked = isPipedCustomEnabled,
                    onCheckedChange = { isPipedCustomEnabled = it }
                )
                AnimatedVisibility(visible = isPipedCustomEnabled) {
                    Column {
                        TextDialogSettingEntry(
                            title = stringResource(R.string.piped_custom_instance),
                            text = pipedApiBaseUrl,
                            currentText = pipedApiBaseUrl,
                            onTextSave = {
                                pipedApiBaseUrl = it
                            }
                        )
                    }
                }
                AnimatedVisibility(visible = !isPipedCustomEnabled) {
                    Column {
                        ButtonBarSettingEntry(
                            //isEnabled = pipedApiToken.isEmpty(),
                            title = stringResource(R.string.piped_change_instance),
                            text = pipedInstanceName,
                            icon = R.drawable.open,
                            onClick = {
                                loadInstances = true
                            }
                        )
                    }
                }

                TextDialogSettingEntry(
                    //isEnabled = pipedApiToken.isEmpty(),
                    title = stringResource(R.string.piped_username),
                    text = pipedUsername,
                    currentText = pipedUsername,
                    onTextSave = { pipedUsername = it }
                )
                TextDialogSettingEntry(
                    //isEnabled = pipedApiToken.isEmpty(),
                    title = stringResource(R.string.piped_password),
                    text = if (pipedPassword.isNotEmpty()) "********" else "",
                    currentText = pipedPassword,
                    onTextSave = { pipedPassword = it },
                    modifier = Modifier
                        .semantics {
                            password()
                        }
                )

                ButtonBarSettingEntry(
                    isEnabled = pipedPassword.isNotEmpty() && pipedUsername.isNotEmpty() && pipedApiBaseUrl.isNotEmpty(),
                    title = if (pipedApiToken.isNotEmpty()) stringResource(R.string.piped_disconnect) else stringResource(
                        R.string.piped_connect
                    ),
                    text = if (pipedApiToken.isNotEmpty()) stringResource(R.string.piped_connected_to_s).format(
                        pipedInstanceName
                    ) else "",
                    icon = R.drawable.piped_logo,
                    iconColor = colorPalette().red,
                    onClick = {
                        if (pipedApiToken.isNotEmpty()) {
                            pipedApiToken = ""
                            executeLogin = false
                        } else executeLogin = true
                    }
                )

            }
        }
    }

    /****** PIPED ******/

    /****** DISCORD ******/

    // rememberEncryptedPreference only works correct with API 24 and up
    if (isAtLeastAndroid7) {
        var isDiscordPresenceEnabled by rememberPreference(isDiscordPresenceEnabledKey, false)
        var loginDiscord by remember { mutableStateOf(false) }
        var discordPersonalAccessToken by rememberEncryptedPreference(
            key = discordPersonalAccessTokenKey,
            defaultValue = ""
        )
        SettingsGroupSpacer()
        SettingsEntryGroupText(title = stringResource(R.string.social_discord))
        SwitchSettingEntry(
            isEnabled = isAtLeastAndroid81,
            title = stringResource(R.string.discord_enable_rich_presence),
            text = "",
            isChecked = isDiscordPresenceEnabled,
            onCheckedChange = { isDiscordPresenceEnabled = it }
        )

        AnimatedVisibility(visible = isDiscordPresenceEnabled) {
            Column {
                ButtonBarSettingEntry(
                    isEnabled = true,
                    title = if (discordPersonalAccessToken.isNotEmpty()) stringResource(R.string.discord_disconnect) else stringResource(
                        R.string.discord_connect
                    ),
                    text = if (discordPersonalAccessToken.isNotEmpty()) stringResource(R.string.discord_connected_to_discord_account) else "",
                    icon = R.drawable.logo_discord,
                    iconColor = colorPalette().text,
                    onClick = {
                        if (discordPersonalAccessToken.isNotEmpty())
                            discordPersonalAccessToken = ""
                        else
                            loginDiscord = true
                    }
                )

                CustomModalBottomSheet(
                    showSheet = loginDiscord,
                    onDismissRequest = {
                        loginDiscord = false
                    },
                    containerColor = colorPalette().background0,
                    contentColor = colorPalette().background0,
                    modifier = Modifier.fillMaxWidth(),
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    dragHandle = {
                        Surface(
                            modifier = Modifier.padding(vertical = 0.dp),
                            color = colorPalette().background0,
                            shape = thumbnailShape()
                        ) {}
                    },
                    shape = thumbnailRoundness.shape()
                ) {
                    DiscordLoginAndGetToken(
                        rememberNavController(),
                        onGetToken = { token ->
                            loginDiscord = false
                            discordPersonalAccessToken = token
                            SmartMessage(token, type = PopupType.Info, context = context)
                        }
                    )
                }
            }
        }
    }

    /****** DISCORD ******/



    }


}

fun isYouTubeLoginEnabled(): Boolean {
    val isYouTubeLoginEnabled = appContext().preferences.getBoolean(enableYouTubeLoginKey, false)
    return isYouTubeLoginEnabled
}

fun isYouTubeSyncEnabled(): Boolean {
    val isYouTubeSyncEnabled = appContext().preferences.getBoolean(enableYouTubeSyncKey, false)
    return isYouTubeSyncEnabled && isYouTubeLoggedIn() && isYouTubeLoginEnabled()
}

fun isYouTubeLoggedIn(): Boolean {
    val cookie = appContext().preferences.getString(ytCookieKey, "")
    val isLoggedIn = cookie?.let { parseCookieString(it) }?.contains("SAPISID") == true
    return isLoggedIn
}





