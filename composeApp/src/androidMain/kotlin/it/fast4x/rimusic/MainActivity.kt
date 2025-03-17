package it.fast4x.rimusic

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.ComponentName
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.drawable.BitmapDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.WindowManager
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.rememberNavController
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import com.kieronquinn.monetcompat.core.MonetActivityAccessException
import com.kieronquinn.monetcompat.core.MonetCompat
import com.kieronquinn.monetcompat.interfaces.MonetColorsChangedListener
import com.valentinilk.shimmer.LocalShimmerTheme
import com.valentinilk.shimmer.defaultShimmerTheme
import de.raphaelebner.roomdatabasebackup.core.RoomBackup
import dev.kdrag0n.monet.theme.ColorScheme
import it.fast4x.environment.Environment
import it.fast4x.environment.models.bodies.BrowseBody
import it.fast4x.environment.requests.playlistPage
import it.fast4x.environment.requests.song
import it.fast4x.environment.utils.EnvironmentPreferenceItem
import it.fast4x.environment.utils.EnvironmentPreferences
import it.fast4x.environment.utils.LocalePreferenceItem
import it.fast4x.environment.utils.LocalePreferences
import it.fast4x.environment.utils.ProxyPreferenceItem
import it.fast4x.environment.utils.ProxyPreferences
//import it.fast4x.environment.utils.YoutubePreferenceItem
//import it.fast4x.environment.utils.YoutubePreferences
import it.fast4x.rimusic.enums.AnimatedGradient
import it.fast4x.rimusic.enums.CheckUpdateState
import it.fast4x.rimusic.enums.ColorPaletteMode
import it.fast4x.rimusic.enums.ColorPaletteName
import it.fast4x.rimusic.enums.DnsOverHttpsType
import it.fast4x.rimusic.enums.FontType
import it.fast4x.rimusic.enums.HomeScreenTabs
import it.fast4x.rimusic.enums.Languages
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.PipModule
import it.fast4x.rimusic.enums.PlayerBackgroundColors
import it.fast4x.rimusic.enums.PopupType
import it.fast4x.rimusic.enums.ThumbnailRoundness
import it.fast4x.rimusic.extensions.pip.PipEventContainer
import it.fast4x.rimusic.extensions.pip.PipModuleContainer
import it.fast4x.rimusic.extensions.pip.PipModuleCover
import it.fast4x.rimusic.service.MyDownloadHelper
import it.fast4x.rimusic.service.modern.PlayerServiceModern
import it.fast4x.rimusic.ui.components.CustomModalBottomSheet
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.themed.CrossfadeContainer
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.ui.screens.AppNavigation
import it.fast4x.rimusic.ui.screens.player.MiniPlayer
import it.fast4x.rimusic.ui.screens.player.Player
import it.fast4x.rimusic.ui.screens.player.components.YoutubePlayer
import it.fast4x.rimusic.ui.screens.player.rememberPlayerSheetState
import it.fast4x.rimusic.ui.styling.Appearance
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.LocalAppearance
import it.fast4x.rimusic.ui.styling.applyPitchBlack
import it.fast4x.rimusic.ui.styling.colorPaletteOf
import it.fast4x.rimusic.ui.styling.customColorPalette
import it.fast4x.rimusic.ui.styling.dynamicColorPaletteOf
import it.fast4x.rimusic.ui.styling.typographyOf
import it.fast4x.rimusic.utils.InitDownloader
import it.fast4x.rimusic.utils.LocalMonetCompat
import it.fast4x.rimusic.utils.OkHttpRequest
import it.fast4x.rimusic.extensions.rescuecenter.RescueScreen
import it.fast4x.rimusic.utils.UiTypeKey
import it.fast4x.rimusic.utils.animatedGradientKey
import it.fast4x.rimusic.utils.applyFontPaddingKey
import it.fast4x.rimusic.utils.asMediaItem
import it.fast4x.rimusic.utils.backgroundProgressKey
import it.fast4x.rimusic.utils.checkUpdateStateKey
import it.fast4x.rimusic.utils.closeWithBackButtonKey
import it.fast4x.rimusic.utils.colorPaletteModeKey
import it.fast4x.rimusic.utils.colorPaletteNameKey
import it.fast4x.rimusic.utils.customColorKey
import it.fast4x.rimusic.utils.customDnsOverHttpsServerKey
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
import it.fast4x.rimusic.utils.disablePlayerHorizontalSwipeKey
import it.fast4x.rimusic.utils.effectRotationKey
import it.fast4x.rimusic.utils.fontTypeKey
import it.fast4x.rimusic.utils.forcePlay
import it.fast4x.rimusic.utils.getEnum
import it.fast4x.rimusic.utils.getSystemlanguage
import it.fast4x.rimusic.utils.intent
import it.fast4x.rimusic.utils.invokeOnReady
import it.fast4x.rimusic.utils.isAtLeastAndroid6
import it.fast4x.rimusic.utils.isAtLeastAndroid8
import it.fast4x.rimusic.utils.isKeepScreenOnEnabledKey
import it.fast4x.rimusic.utils.isProxyEnabledKey
import it.fast4x.rimusic.utils.isValidHttpUrl
import it.fast4x.rimusic.utils.isValidIP
import it.fast4x.rimusic.utils.isVideo
import it.fast4x.rimusic.utils.keepPlayerMinimizedKey
import it.fast4x.rimusic.utils.languageAppKey
import it.fast4x.rimusic.utils.loadedDataKey
import it.fast4x.rimusic.utils.miniPlayerTypeKey
import it.fast4x.rimusic.utils.navigationBarPositionKey
import it.fast4x.rimusic.utils.navigationBarTypeKey
import it.fast4x.rimusic.utils.parentalControlEnabledKey
import it.fast4x.rimusic.utils.pipModuleKey
import it.fast4x.rimusic.utils.playNext
import it.fast4x.rimusic.utils.playerBackgroundColorsKey
import it.fast4x.rimusic.utils.playerThumbnailSizeKey
import it.fast4x.rimusic.utils.playerVisualizerTypeKey
import it.fast4x.rimusic.utils.preferences
import it.fast4x.rimusic.utils.proxyHostnameKey
import it.fast4x.rimusic.utils.proxyModeKey
import it.fast4x.rimusic.utils.proxyPortKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.resize
import it.fast4x.rimusic.utils.restartActivityKey
import it.fast4x.rimusic.utils.setDefaultPalette
import it.fast4x.rimusic.utils.shakeEventEnabledKey
import it.fast4x.rimusic.utils.showButtonPlayerVideoKey
import it.fast4x.rimusic.utils.showSearchTabKey
import it.fast4x.rimusic.utils.showTotalTimeQueueKey
import it.fast4x.rimusic.utils.thumbnail
import it.fast4x.rimusic.utils.thumbnailRoundnessKey
import it.fast4x.rimusic.utils.transitionEffectKey
import it.fast4x.rimusic.utils.useSystemFontKey
import it.fast4x.rimusic.utils.ytCookieKey
import it.fast4x.rimusic.utils.ytDataSyncIdKey
import it.fast4x.rimusic.utils.ytVisitorDataKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.net.Proxy
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects
import java.util.UUID
import kotlin.math.sqrt

@UnstableApi
class MainActivity :
//MonetCompatActivity(),
    AppCompatActivity(),
    MonetColorsChangedListener
//,PersistMapOwner
{
    var downloadHelper = MyDownloadHelper

    var client = OkHttpClient()
    var request = OkHttpRequest(client)
    lateinit var backup: RoomBackup

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is PlayerServiceModern.Binder) {
                this@MainActivity.binder = service
                println("MainActivity.onServiceConnected PlayerServiceModern is connected")
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            binder = null
            println("MainActivity.onServiceDisconnected")
        }

    }

    private var binder by mutableStateOf<PlayerServiceModern.Binder?>(null)
    private var intentUriData by mutableStateOf<Uri?>(null)

    //override lateinit var persistMap: PersistMap

    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f
    private var shakeCounter = 0

    private var _monet: MonetCompat? by mutableStateOf(null)
    private val monet get() = _monet ?: throw MonetActivityAccessException()

    private val pipState: MutableState<Boolean> = mutableStateOf(false)

    override fun onStart() {
        super.onStart()

        runCatching {
            val intent = Intent(this, PlayerServiceModern::class.java)
            startService(intent)
            bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        }.onFailure {
            Timber.e("MainActivity.onStart bindService ${it.stackTraceToString()}")
        }
    }

    @ExperimentalMaterialApi
    @ExperimentalTextApi
    @UnstableApi
    @ExperimentalComposeUiApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MonetCompat.enablePaletteCompat()

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                scrim = Color.Transparent.toArgb(),
            ),
            navigationBarStyle = SystemBarStyle.light(
                scrim = Color.Transparent.toArgb(),
                darkScrim = Color.Transparent.toArgb()
            )
        )

        WindowCompat.setDecorFitsSystemWindows(window, false)

        MonetCompat.setup(this)
        _monet = MonetCompat.getInstance()
        monet.setDefaultPalette()
        monet.addMonetColorsChangedListener(
            listener = this,
            notifySelf = false
        )
        monet.updateMonetColors()

        monet.invokeOnReady {
            startApp()
        }

        if (preferences.getBoolean(shakeEventEnabledKey, false)) {
            sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
            Objects.requireNonNull(sensorManager)
                ?.registerListener(
                    sensorListener,
                    sensorManager!!
                        .getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_NORMAL
                )
        }

        backup = RoomBackup(this)

        checkIfAppIsRunningInBackground()

    }

    private fun checkIfAppIsRunningInBackground() {
        val runningAppProcessInfo = ActivityManager.RunningAppProcessInfo()
        ActivityManager.getMyMemoryState(runningAppProcessInfo)
        appRunningInBackground =
            runningAppProcessInfo.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND

    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        pipState.value = isInPictureInPictureMode
        println("MainActivity.onPictureInPictureModeChanged isInPictureInPictureMode: $isInPictureInPictureMode")
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)

    }


    /*
    @Suppress("DEPRECATION")
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (isAtLeastAndroid8 && !isInPictureInPictureMode) {
            enterPictureInPictureMode()
            println("MainActivity.onUserLeaveHint isInPictureInPictureMode: $isInPictureInPictureMode")
        }
    }
    */

    @Composable
    fun ThemeApp(
        isDark: Boolean = false,
        content: @Composable () -> Unit
    ) {
        val view = LocalView.current
        if (!view.isInEditMode) {
            SideEffect {
                (view.context as Activity).window.let { window ->
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                        !isDark
                    WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars =
                        !isDark
                }
            }

        }
        content()
    }

    @SuppressLint("UnusedBoxWithConstraintsScope")
    @OptIn(
        ExperimentalTextApi::class,
        ExperimentalFoundationApi::class, ExperimentalAnimationApi::class,
        ExperimentalMaterial3Api::class
    )
    fun startApp() {

        // Used in QuickPics for load data from remote instead of last saved in SharedPreferences
        preferences.edit(commit = true) { putBoolean(loadedDataKey, false) }

        if (!preferences.getBoolean(closeWithBackButtonKey, false))
            if (Build.VERSION.SDK_INT >= 33) {
                onBackInvokedDispatcher.registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT
                ) {
                    //Log.d("onBackPress", "yeah")
                }
            }

        /*
            Instead of checking getBoolean() individually, we can use .let() to express condition.
            Or, the whole thing is 'false' if null appears in the process.
         */
        val launchedFromNotification: Boolean =
            intent?.extras?.let {
                it.getBoolean("expandPlayerBottomSheet") || it.getBoolean("fromWidget")
            } ?: false

        println("MainActivity.onCreate launchedFromNotification: $launchedFromNotification intent $intent.action")

        intentUriData = intent.data ?: intent.getStringExtra(Intent.EXTRA_TEXT)?.toUri()

        with(preferences) {
            if (getBoolean(isKeepScreenOnEnabledKey, false)) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            if (getBoolean(isProxyEnabledKey, false)) {
                val hostName = getString(proxyHostnameKey, null)
                val proxyPort = getInt(proxyPortKey, 8080)
                val proxyMode = getEnum(proxyModeKey, Proxy.Type.HTTP)
                if (isValidIP(hostName)) {
                    hostName?.let { hName ->
                        ProxyPreferences.preference =
                            ProxyPreferenceItem(hName, proxyPort, proxyMode)
                    }
                } else {
                    SmartMessage(
                        "Your Proxy Hostname is invalid, please check it",
                        PopupType.Warning,
                        context = this@MainActivity
                    )
                }
            }
            //if (getBoolean(isEnabledDiscoveryLangCodeKey, true))
        }

        setContent {

            // Valid to get log when app crash
//            if (intent.action == action_rescuecenter) {
//                RescueScreen()
//            } else {

                val colorPaletteMode by rememberPreference(
                    colorPaletteModeKey,
                    ColorPaletteMode.Dark
                )
                val isPicthBlack = colorPaletteMode == ColorPaletteMode.PitchBlack
//            val isDark =
//                colorPaletteMode == ColorPaletteMode.Dark || isPicthBlack || (colorPaletteMode == ColorPaletteMode.System && isSystemInDarkTheme())

                //TODO: Check internet connection
//            val internetConnectivityObserver = InternetConnectivityObserver(this)
//            val internetConnected by internetConnectivityObserver.networkStatus.collectAsState(false)
//            if (internetConnected) downloadHelper.resumeDownloads(this)

                if (preferences.getEnum(
                        checkUpdateStateKey,
                        CheckUpdateState.Ask
                    ) == CheckUpdateState.Enabled
                ) {
                    val urlVersionCode =
                        "https://raw.githubusercontent.com/fast4x/RiMusic/master/updatedVersion/updatedVersionCode.ver"
                    //val urlVersionCode = "https://rimusic.xyz/update/updatedVersionCode.ver"
                    request.GET(urlVersionCode, object : Callback {
                        override fun onResponse(call: Call, response: Response) {
                            val responseData = response.body?.string()
                            runOnUiThread {
                                try {
                                    if (responseData != null) {
                                        val file = File(filesDir, "RiMusicUpdatedVersionCode.ver")
                                        file.writeText(responseData.toString())
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }

                        }

                        override fun onFailure(call: Call, e: IOException) {
                            Log.d("UpdatedVersionCode", "Check failure")
                        }
                    })
                }

                runBlocking {
                    InitializeEnvironment()
                }

                val coroutineScope = rememberCoroutineScope()
                val isSystemInDarkTheme = isSystemInDarkTheme()
                val navController = rememberNavController()
                var showPlayer by rememberSaveable { mutableStateOf(false) }
                var switchToAudioPlayer by rememberSaveable { mutableStateOf(false) }
                var animatedGradient by rememberPreference(
                    animatedGradientKey,
                    AnimatedGradient.Linear
                )
                var customColor by rememberPreference(customColorKey, Color.Green.hashCode())
                val lightTheme =
                    colorPaletteMode == ColorPaletteMode.Light || (colorPaletteMode == ColorPaletteMode.System && (!isSystemInDarkTheme()))

                val locale = Locale.getDefault()
                val languageTag = locale.toLanguageTag().replace("-Hant", "")
                val languageApp =
                    appContext().preferences.getEnum(languageAppKey, getSystemlanguage())
                LocalePreferences.preference =
                    LocalePreferenceItem(
                        hl = languageApp.code.takeIf { it != Languages.System.code }
                            ?: locale.language.takeIf { it != Languages.System.code }
                            ?: languageTag.takeIf { it != Languages.System.code }
                            ?: "en",
                        gl = locale.country
                            ?: "US"
                    )
                //TODO Manage login
                //if (preferences.getBoolean(enableYouTubeLoginKey, false)) {
                var visitorData by rememberPreference(
                    key = ytVisitorDataKey,
                    defaultValue = Environment._uMYwa66ycM
                )

                if (visitorData.isEmpty()) runBlocking {
                    Environment.visitorData().getOrNull()?.also {
                        visitorData = it
                    }
                }

                val cookie = preferences.getString(ytCookieKey, "")
                println("MainActivity.onCreate cookie: $cookie")
                val customDnsOverHttpsServer =
                    preferences.getString(customDnsOverHttpsServerKey, "")

                Environment.cookie = cookie
                Environment.visitorData = visitorData.takeIf { it != "null" }
                    ?: Environment._uMYwa66ycM
                Environment.dataSyncId = preferences.getString(ytDataSyncIdKey, "").toString()
                val customDnsIsOk = customDnsOverHttpsServer?.let { isValidHttpUrl(it) }
                if (customDnsIsOk == false && getDnsOverHttpsType() == DnsOverHttpsType.Custom)
                    SmartMessage(
                        stringResource(R.string.custom_dns_is_invalid),
                        PopupType.Error,
                        context = this@MainActivity
                    )

                val customDnsUrl = if (customDnsIsOk == true) customDnsOverHttpsServer else null
                Environment.customDnsToUse = customDnsUrl
                Environment.dnsToUse = getDnsOverHttpsType().type

                var appearance by rememberSaveable(
                    !lightTheme,
                    stateSaver = Appearance
                ) {
                    with(preferences) {
                        val colorPaletteName =
                            getEnum(colorPaletteNameKey, ColorPaletteName.Dynamic)
                        //val colorPaletteMode = getEnum(colorPaletteModeKey, ColorPaletteMode.Dark)
                        val thumbnailRoundness =
                            getEnum(thumbnailRoundnessKey, ThumbnailRoundness.Heavy)
                        val useSystemFont = getBoolean(useSystemFontKey, false)
                        val applyFontPadding = getBoolean(applyFontPaddingKey, false)

                        var colorPalette =
                            colorPaletteOf(colorPaletteName, colorPaletteMode, !lightTheme)

                        val fontType = getEnum(fontTypeKey, FontType.Rubik)

                        if (colorPaletteName == ColorPaletteName.MaterialYou) {
                            colorPalette = dynamicColorPaletteOf(
                                Color(monet.getAccentColor(this@MainActivity)),
                                !lightTheme
                            )
                        }
                        if (colorPaletteName == ColorPaletteName.CustomColor) {
                            println("MainActivity.startApp SetContent with(preferences) customColor PRE colorPalette: $colorPalette")
                            colorPalette = dynamicColorPaletteOf(
                                Color(customColor),
                                !lightTheme
                            )
                            println("MainActivity.startApp SetContent with(preferences) customColor POST colorPalette: $colorPalette")
                        }

                        setSystemBarAppearance(colorPalette.isDark)

                        mutableStateOf(
                            Appearance(
                                colorPalette = colorPalette,
                                typography = typographyOf(
                                    colorPalette.text,
                                    useSystemFont,
                                    applyFontPadding,
                                    fontType
                                ),
                                thumbnailShape = thumbnailRoundness.shape()
                            )
                        )
                    }


                }

                fun setDynamicPalette(url: String) {
                    val playerBackgroundColors = preferences.getEnum(
                        playerBackgroundColorsKey,
                        PlayerBackgroundColors.BlurredCoverColor
                    )
                    val colorPaletteName =
                        preferences.getEnum(colorPaletteNameKey, ColorPaletteName.Dynamic)
                    val isDynamicPalette = colorPaletteName == ColorPaletteName.Dynamic
                    val isCoverColor =
                        playerBackgroundColors == PlayerBackgroundColors.CoverColorGradient ||
                                playerBackgroundColors == PlayerBackgroundColors.CoverColor ||
                                animatedGradient == AnimatedGradient.FluidCoverColorGradient

                    if (!isDynamicPalette) return

//                val colorPaletteMode =
//                    preferences.getEnum(colorPaletteModeKey, ColorPaletteMode.Dark)
                    coroutineScope.launch(Dispatchers.IO) {
                        val result = imageLoader.execute(
                            ImageRequest.Builder(this@MainActivity)
                                .data(url)
                                .allowHardware(false)
                                .build()
                        )
                        val isPicthBlack = colorPaletteMode == ColorPaletteMode.PitchBlack
                        val isDark =
                            colorPaletteMode == ColorPaletteMode.Dark || isPicthBlack || (colorPaletteMode == ColorPaletteMode.System && isSystemInDarkTheme)

                        val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                        if (bitmap != null) {
                            val palette = Palette
                                .from(bitmap)
                                .maximumColorCount(8)
                                .addFilter(if (isDark) ({ _, hsl -> hsl[0] !in 36f..100f }) else null)
                                .generate()
                            println("Mainactivity onmediaItemTransition palette dominantSwatch: ${palette.dominantSwatch}")

                            dynamicColorPaletteOf(bitmap, isDark)?.let {
                                withContext(Dispatchers.Main) {
                                    setSystemBarAppearance(it.isDark)
                                }
                                appearance = appearance.copy(
                                    colorPalette = if (!isPicthBlack) it else it.copy(
                                        background0 = Color.Black,
                                        background1 = Color.Black,
                                        background2 = Color.Black,
                                        background3 = Color.Black,
                                        background4 = Color.Black,
                                        // text = Color.White
                                    ),
                                    typography = appearance.typography.copy(it.text)
                                )
                                println("Mainactivity onmediaItemTransition appearance inside: ${appearance.colorPalette}")
                            }

                        }
                    }
                    println("Mainactivity onmediaItemTransition appearance outside: ${appearance.colorPalette}")
                }


                DisposableEffect(binder, !lightTheme) {
                    /*
            var bitmapListenerJob: Job? = null

            fun setDynamicPalette(colorPaletteMode: ColorPaletteMode) {
                val isDark =
                    colorPaletteMode == ColorPaletteMode.Dark || (colorPaletteMode == ColorPaletteMode.System && isSystemInDarkTheme)
                val isPicthBlack = colorPaletteMode == ColorPaletteMode.PitchBlack

                binder?.setBitmapListener { bitmap: Bitmap? ->
                    if (bitmap == null) {
                        val colorPalette =
                            colorPaletteOf(
                                ColorPaletteName.Dynamic,
                                colorPaletteMode,
                                isSystemInDarkTheme
                            )

                        setSystemBarAppearance(colorPalette.isDark)

                        appearance = appearance.copy(
                            colorPalette = colorPalette,
                            typography = appearance.typography.copy(colorPalette.text)
                        )

                        return@setBitmapListener
                    }

                    bitmapListenerJob = coroutineScope.launch(Dispatchers.IO) {
                        dynamicColorPaletteOf(bitmap, isDark, isPicthBlack)?.let {
                            withContext(Dispatchers.Main) {
                                setSystemBarAppearance(it.isDark)
                            }
                            appearance = appearance.copy(
                                colorPalette = it,
                                typography = appearance.typography.copy(it.text)
                            )
                        }
                    }
                }
            }
            */

                    val listener =
                        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
                            when (key) {

                                languageAppKey -> {
                                    val lang = sharedPreferences.getEnum(
                                        languageAppKey,
                                        Languages.English
                                    )

                                    //val precLangCode = LocaleListCompat.getDefault().get(0).toString()
                                    val systemLangCode =
                                        AppCompatDelegate.getApplicationLocales().get(0).toString()
                                    //Log.d("LanguageActivity", "lang.code ${lang.code} precLangCode $precLangCode systemLangCode $systemLangCode")

                                    val sysLocale: LocaleListCompat =
                                        LocaleListCompat.forLanguageTags(systemLangCode)
                                    val appLocale: LocaleListCompat =
                                        LocaleListCompat.forLanguageTags(lang.code)
                                    AppCompatDelegate.setApplicationLocales(if (lang.code == "") sysLocale else appLocale)
                                }

                                effectRotationKey, playerThumbnailSizeKey,
                                playerVisualizerTypeKey,
                                UiTypeKey,
                                disablePlayerHorizontalSwipeKey,
                                disableClosingPlayerSwipingDownKey,
                                showSearchTabKey,
                                navigationBarPositionKey,
                                navigationBarTypeKey,
                                showTotalTimeQueueKey,
                                backgroundProgressKey,
                                transitionEffectKey,
                                playerBackgroundColorsKey,
                                miniPlayerTypeKey,
                                restartActivityKey
                                    -> {
                                    this@MainActivity.recreate()
                                    println("MainActivity.recreate()")
                                }

                                colorPaletteNameKey, colorPaletteModeKey,
                                customThemeLight_Background0Key,
                                customThemeLight_Background1Key,
                                customThemeLight_Background2Key,
                                customThemeLight_Background3Key,
                                customThemeLight_Background4Key,
                                customThemeLight_TextKey,
                                customThemeLight_textSecondaryKey,
                                customThemeLight_textDisabledKey,
                                customThemeLight_iconButtonPlayerKey,
                                customThemeLight_accentKey,
                                customThemeDark_Background0Key,
                                customThemeDark_Background1Key,
                                customThemeDark_Background2Key,
                                customThemeDark_Background3Key,
                                customThemeDark_Background4Key,
                                customThemeDark_TextKey,
                                customThemeDark_textSecondaryKey,
                                customThemeDark_textDisabledKey,
                                customThemeDark_iconButtonPlayerKey,
                                customThemeDark_accentKey,
                                customColorKey
                                    -> {
                                    val colorPaletteName =
                                        sharedPreferences.getEnum(
                                            colorPaletteNameKey,
                                            ColorPaletteName.Dynamic
                                        )

//                                val colorPaletteMode =
//                                    sharedPreferences.getEnum(
//                                        colorPaletteModeKey,
//                                        ColorPaletteMode.System
//                                    )

                                    var colorPalette = colorPaletteOf(
                                        colorPaletteName,
                                        colorPaletteMode,
                                        !lightTheme
                                    )

                                    if (colorPaletteName == ColorPaletteName.Dynamic) {
                                        val artworkUri =
                                            (binder?.player?.currentMediaItem?.mediaMetadata?.artworkUri.thumbnail(
                                                1200
                                            )
                                                ?: "").toString()
                                        artworkUri.let {
                                            if (it.isNotEmpty())
                                                setDynamicPalette(it)
                                            else {

                                                setSystemBarAppearance(colorPalette.isDark)
                                                appearance = appearance.copy(
                                                    colorPalette = if (!isPicthBlack) colorPalette else colorPalette.copy(
                                                        background0 = Color.Black,
                                                        background1 = Color.Black,
                                                        background2 = Color.Black,
                                                        background3 = Color.Black,
                                                        background4 = Color.Black,
                                                        // text = Color.White
                                                    ),
                                                    typography = appearance.typography.copy(
                                                        colorPalette.text
                                                    ),
                                                )
                                            }

                                        }

                                    } else {
                                        //bitmapListenerJob?.cancel()
                                        //binder?.setBitmapListener(null)

                                        if (colorPaletteName == ColorPaletteName.MaterialYou) {
                                            colorPalette = dynamicColorPaletteOf(
                                                Color(monet.getAccentColor(this@MainActivity)),
                                                !lightTheme
                                            )
                                        }

                                        if (colorPaletteName == ColorPaletteName.Customized) {
                                            colorPalette = customColorPalette(
                                                colorPalette,
                                                this@MainActivity,
                                                isSystemInDarkTheme
                                            )
                                        }
                                        if (colorPaletteName == ColorPaletteName.CustomColor) {
                                            println("MainActivity.startApp SetContent DisposableEffect customColor PRE colorPalette: $colorPalette")
                                            colorPalette = dynamicColorPaletteOf(
                                                Color(customColor),
                                                !lightTheme
                                            )
                                            println("MainActivity.startApp SetContent DisposableEffect customColor POST colorPalette: $colorPalette")
                                        }

                                        setSystemBarAppearance(colorPalette.isDark)

                                        appearance = appearance.copy(
                                            colorPalette = if (!isPicthBlack) colorPalette else colorPalette.copy(
                                                background0 = Color.Black,
                                                background1 = Color.Black,
                                                background2 = Color.Black,
                                                background3 = Color.Black,
                                                background4 = Color.Black,
                                                text = Color.White
                                            ),
                                            typography = appearance.typography.copy(if (!isPicthBlack) colorPalette.text else Color.White),
                                        )
                                    }
                                }

                                thumbnailRoundnessKey -> {
                                    val thumbnailRoundness =
                                        sharedPreferences.getEnum(key, ThumbnailRoundness.Heavy)

                                    appearance = appearance.copy(
                                        thumbnailShape = thumbnailRoundness.shape()
                                    )
                                }

                                useSystemFontKey, applyFontPaddingKey, fontTypeKey -> {
                                    val useSystemFont =
                                        sharedPreferences.getBoolean(useSystemFontKey, false)
                                    val applyFontPadding =
                                        sharedPreferences.getBoolean(applyFontPaddingKey, false)
                                    val fontType =
                                        sharedPreferences.getEnum(fontTypeKey, FontType.Rubik)

                                    appearance = appearance.copy(
                                        typography = typographyOf(
                                            appearance.colorPalette.text,
                                            useSystemFont,
                                            applyFontPadding,
                                            fontType
                                        ),
                                    )
                                }
                            }
                        }

                    with(preferences) {
                        registerOnSharedPreferenceChangeListener(listener)

                        val colorPaletteName =
                            getEnum(colorPaletteNameKey, ColorPaletteName.Dynamic)
                        if (colorPaletteName == ColorPaletteName.Dynamic) {
                            setDynamicPalette(
                                (binder?.player?.currentMediaItem?.mediaMetadata?.artworkUri.thumbnail(
                                    1200
                                )
                                    ?: "").toString()
                            )
                        }

                        onDispose {
                            //bitmapListenerJob?.cancel()
                            //binder?.setBitmapListener(null)
                            unregisterOnSharedPreferenceChangeListener(listener)
                        }
                    }
                }

                val rippleConfiguration =
                    remember(appearance.colorPalette.text, appearance.colorPalette.isDark) {
                        RippleConfiguration(color = appearance.colorPalette.text)
                    }

                val shimmerTheme = remember {
                    defaultShimmerTheme.copy(
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                durationMillis = 800,
                                easing = LinearEasing,
                                delayMillis = 250,
                            ),
                            repeatMode = RepeatMode.Restart
                        ),
                        shaderColors = listOf(
                            Color.Unspecified.copy(alpha = 0.25f),
                            Color.White.copy(alpha = 0.50f),
                            Color.Unspecified.copy(alpha = 0.25f),
                        ),
                    )
                }

                LaunchedEffect(Unit) {
                    val colorPaletteName =
                        preferences.getEnum(colorPaletteNameKey, ColorPaletteName.Dynamic)
                    if (colorPaletteName == ColorPaletteName.Customized) {
                        appearance = appearance.copy(
                            colorPalette = customColorPalette(
                                appearance.colorPalette,
                                this@MainActivity,
                                isSystemInDarkTheme
                            )
                        )
                    }
                }


                if (colorPaletteMode == ColorPaletteMode.PitchBlack)
                    appearance = appearance.copy(
                        colorPalette = appearance.colorPalette.applyPitchBlack,
                        typography = appearance.typography.copy(appearance.colorPalette.text)
                    )

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(appearance.colorPalette.background0)
                ) {


                    val density = LocalDensity.current
                    val windowsInsets = WindowInsets.systemBars
                    val bottomDp = with(density) { windowsInsets.getBottom(density).toDp() }

                    val playerSheetState = rememberPlayerSheetState(
                        dismissedBound = 0.dp,
                        collapsedBound = Dimensions.collapsedPlayer + bottomDp,
                        expandedBound = maxHeight,
                    )

                    val playerState =
                        rememberModalBottomSheetState(skipPartiallyExpanded = true)

                    val playerAwareWindowInsets by remember(
                        bottomDp,
                        playerSheetState.value
                    ) {
                        derivedStateOf {
                            val bottom = playerSheetState.value.coerceIn(
                                bottomDp,
                                playerSheetState.collapsedBound
                            )

                            windowsInsets
                                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                                .add(WindowInsets(bottom = bottom))
                        }
                    }

                    var openTabFromShortcut = remember { -1 }
                    if (intent.action in arrayOf(
                            action_songs,
                            action_albums,
                            action_library,
                            action_search
                        )
                    ) {
                        openTabFromShortcut =
                            when (intent?.action) {
                                action_songs -> HomeScreenTabs.Songs.index
                                action_albums -> HomeScreenTabs.Albums.index
                                action_library -> HomeScreenTabs.Playlists.index
                                action_search -> -2
                                else -> -1
                            }
                        intent.action = null
                    }

                    fun <I, O> ComponentActivity.registerActivityResultLauncher(
                        contract: ActivityResultContract<I, O>,
                        callback: ActivityResultCallback<O>
                    ): ActivityResultLauncher<I> {
                        val key = UUID.randomUUID().toString()
                        return activityResultRegistry.register(key, contract, callback)
                    }

                    CrossfadeContainer(state = pipState.value) { isCurrentInPip ->
                        println("MainActivity pipState ${pipState.value} CrossfadeContainer isCurrentInPip $isCurrentInPip ")
                        val pipModule by rememberPreference(pipModuleKey, PipModule.Cover)
                        if (isCurrentInPip) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Transparent)
                            ) {
                                when (pipModule) {
                                    PipModule.Cover -> {
                                        PipModuleContainer {
                                            PipModuleCover(
                                                url = binder?.player?.currentMediaItem?.mediaMetadata?.artworkUri.toString()
                                                    .resize(1200, 1200)
                                            )
                                        }
                                    }

                                }

                            }

                        } else
                            CompositionLocalProvider(
                                LocalAppearance provides appearance,
                                LocalIndication provides ripple(bounded = true),
                                LocalRippleConfiguration provides rippleConfiguration,
                                LocalShimmerTheme provides shimmerTheme,
                                LocalPlayerServiceBinder provides binder,
                                LocalPlayerAwareWindowInsets provides playerAwareWindowInsets,
                                LocalLayoutDirection provides LayoutDirection.Ltr,
                                LocalDownloadHelper provides downloadHelper,
                                LocalPlayerSheetState provides playerState,
                                LocalMonetCompat provides monet,
                                //LocalInternetConnected provides internetConnected
                            ) {

                                if (intent.action == action_rescuecenter) {
                                    RescueScreen(
                                        onBackup = {
                                            @SuppressLint("SimpleDateFormat")
                                            val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")
                                                backup.database(Database.getInstance)
                                                    .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_DIALOG)
                                                    .customBackupFileName("RiMusic_RescueBackup_${dateFormat.format(
                                                        Date()
                                                    )}.db")
                                                    .apply {
                                                        onCompleteListener { success, message, exitCode ->
                                                            Timber.d(
                                                                TAG,
                                                                "Rescue backup success: $success, message: $message, exitCode: $exitCode"
                                                            )
                                                            println("Rescue backup success: $success, message: $message, exitCode: $exitCode")

                                                        }
                                                    }
                                                    .backup()

                                        },
                                        onRestore = {
                                            backup.database(Database.getInstance)
                                                .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_DIALOG)
                                                .apply {
                                                    onCompleteListener { success, message, exitCode ->
                                                        Timber.d(
                                                            TAG,
                                                            "Rescue restore success: $success, message: $message, exitCode: $exitCode"
                                                        )
                                                        println("Rescue restore success: $success, message: $message, exitCode: $exitCode")

                                                    }
                                                }
                                                .restore()
                                        }
                                    )
                                } else {

                                    AppNavigation(
                                        navController = navController,
                                        miniPlayer = {
                                            MiniPlayer(
                                                showPlayer = { showPlayer = true },
                                                hidePlayer = { showPlayer = false },
                                                navController = navController
                                            )
                                        },
                                        openTabFromShortcut = openTabFromShortcut
                                    )

                                    checkIfAppIsRunningInBackground()


                                    val thumbnailRoundness by rememberPreference(
                                        thumbnailRoundnessKey,
                                        ThumbnailRoundness.Heavy
                                    )

                                    val isVideo = binder?.player?.currentMediaItem?.isVideo ?: false
                                    val isVideoEnabled =
                                        preferences.getBoolean(showButtonPlayerVideoKey, false)
                                    val player: @Composable () -> Unit = {
                                        Player(
                                            navController = navController,
                                            onDismiss = {
                                                showPlayer = false
                                            }
                                        )
                                    }

                                    val youtubePlayer: @Composable () -> Unit = {
                                        binder?.player?.currentMediaItem?.mediaId?.let {
                                            YoutubePlayer(
                                                ytVideoId = it,
                                                lifecycleOwner = LocalLifecycleOwner.current,
                                                onCurrentSecond = {},
                                                showPlayer = showPlayer,
                                                onSwitchToAudioPlayer = {
                                                    showPlayer = false
                                                    switchToAudioPlayer = true
                                                }
                                            )
                                        }
                                    }

                                    PipEventContainer(
                                        enable = true,
                                        onPipOutAction = {
                                            showPlayer = false
                                            switchToAudioPlayer = false
                                        }
                                    ) {
                                        CustomModalBottomSheet(
                                            showSheet = switchToAudioPlayer || showPlayer,
                                            onDismissRequest = {
                                                showPlayer = false
                                                switchToAudioPlayer = false
                                            },
                                            containerColor = colorPalette().background0,
                                            contentColor = colorPalette().background0,
                                            modifier = Modifier.fillMaxWidth(),
                                            sheetState = playerState,
                                            dragHandle = {
                                                Surface(
                                                    modifier = Modifier.padding(vertical = 0.dp),
                                                    color = colorPalette().background0,
                                                    shape = thumbnailShape()
                                                ) {}
                                            },
                                            shape = thumbnailRoundness.shape()
                                        ) {
                                            player()
                                        }
                                    }

                                    CustomModalBottomSheet(
                                        showSheet = isVideo && isVideoEnabled && showPlayer,
                                        onDismissRequest = { showPlayer = false },
                                        containerColor = colorPalette().background0,
                                        contentColor = colorPalette().background0,
                                        modifier = Modifier.fillMaxWidth(),
                                        sheetState = playerState,
                                        dragHandle = {
                                            Surface(
                                                modifier = Modifier.padding(vertical = 0.dp),
                                                color = colorPalette().background0,
                                                shape = thumbnailShape()
                                            ) {}
                                        },
                                        shape = thumbnailRoundness.shape()
                                    ) {
                                        youtubePlayer()
                                    }

                                    val menuState = LocalMenuState.current
                                    CustomModalBottomSheet(
                                        showSheet = menuState.isDisplayed,
                                        onDismissRequest = menuState::hide,
                                        containerColor = Color.Transparent,
                                        sheetState = rememberModalBottomSheetState(
                                            skipPartiallyExpanded = true
                                        ),
                                        dragHandle = {
                                            Surface(
                                                modifier = Modifier.padding(vertical = 0.dp),
                                                color = Color.Transparent,
                                                //shape = thumbnailShape
                                            ) {}
                                        },
                                        shape = thumbnailRoundness.shape()
                                    ) {
                                        menuState.content()
                                    }
                                }
                            }

                    }
                    DisposableEffect(binder?.player) {
                        val player = binder?.player ?: return@DisposableEffect onDispose { }

                        if (player.currentMediaItem == null) {
                            if (playerState.isVisible) {
                                showPlayer = false
                            }
                        } else {
                            if (launchedFromNotification) {
                                intent.replaceExtras(Bundle())
                                if (preferences.getBoolean(keepPlayerMinimizedKey, false))
                                    showPlayer = false
                                else showPlayer = true
                            } else {
                                showPlayer = false
                            }
                        }

                        val listener = object : Player.Listener {
                            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED && mediaItem != null) {
                                    if (mediaItem.mediaMetadata.extras?.getBoolean("isFromPersistentQueue") != true) {
                                        if (preferences.getBoolean(keepPlayerMinimizedKey, false))
                                            showPlayer = false
                                        else showPlayer = true
                                    }
                                }

                                setDynamicPalette(
                                    mediaItem?.mediaMetadata?.artworkUri.thumbnail(
                                        1200
                                    ).toString()
                                )
                            }


                        }

                        player.addListener(listener)

                        onDispose { player.removeListener(listener) }
                    }

                    InitDownloader()

                }

                LaunchedEffect(intentUriData) {
                    val uri = intentUriData ?: return@LaunchedEffect

                    SmartMessage(
                        message = "${"RiMusic "}${getString(R.string.opening_url)}",
                        durationLong = true,
                        context = this@MainActivity
                    )

                    lifecycleScope.launch(Dispatchers.Main) {
                        when (val path = uri.pathSegments.firstOrNull()) {
                            "playlist" -> uri.getQueryParameter("list")?.let { playlistId ->
                                val browseId = "VL$playlistId"

                                if (playlistId.startsWith("OLAK5uy_")) {
                                    Environment.playlistPage(BrowseBody(browseId = browseId))
                                        ?.getOrNull()?.let {
                                            it.songsPage?.items?.firstOrNull()?.album?.endpoint?.browseId?.let { browseId ->
                                                navController.navigate(route = "${NavRoutes.album.name}/$browseId")

                                            }
                                        }
                                } else {
                                    navController.navigate(route = "${NavRoutes.playlist.name}/$browseId")
                                }
                            }

                            "channel", "c" -> uri.lastPathSegment?.let { channelId ->
                                try {
                                    navController.navigate(route = "${NavRoutes.artist.name}/$channelId")
                                } catch (e: Exception) {
                                    Timber.e("MainActivity.onCreate intentUriData ${e.stackTraceToString()}")
                                }
                            }

                            "search" -> uri.getQueryParameter("q")?.let { query ->
                                navController.navigate(route = "${NavRoutes.searchResults.name}/$query")
                            }

                            else -> when {
                                path == "watch" -> uri.getQueryParameter("v")
                                uri.host == "youtu.be" -> path
                                else -> null
                            }?.let { videoId ->
                                Environment.song(videoId)?.getOrNull()?.let { song ->
                                    val binder = snapshotFlow { binder }.filterNotNull().first()
                                    withContext(Dispatchers.Main) {
                                        if (!song.explicit && !preferences.getBoolean(
                                                parentalControlEnabledKey,
                                                false
                                            )
                                        )
                                            binder?.player?.forcePlay(song.asMediaItem)
                                        else
                                            SmartMessage(
                                                "Parental control is enabled",
                                                PopupType.Warning,
                                                context = this@MainActivity
                                            )
                                    }
                                }
                            }
                        }
                    }
                    intentUriData = null
                }


                //throw RuntimeException("This is a simulated exception to crash");
            //}
        }
    }


    private val sensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {

            if (preferences.getBoolean(shakeEventEnabledKey, false)) {
                // Fetching x,y,z values
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                lastAcceleration = currentAcceleration

                // Getting current accelerations
                // with the help of fetched x,y,z values
                currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                val delta: Float = currentAcceleration - lastAcceleration
                acceleration = acceleration * 0.9f + delta

                // Display a Toast message if
                // acceleration value is over 12
                if (acceleration > 12) {
                    shakeCounter++
                    //Toast.makeText(applicationContext, "Shake event detected", Toast.LENGTH_SHORT).show()
                }
                if (shakeCounter >= 1) {
                    //Toast.makeText(applicationContext, "Shaked $shakeCounter times", Toast.LENGTH_SHORT).show()
                    shakeCounter = 0
                    binder?.player?.playNext()
                }

            }

        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    override fun onResume() {
        super.onResume()
        runCatching {
            sensorManager?.registerListener(
                sensorListener, sensorManager!!.getDefaultSensor(
                    Sensor.TYPE_ACCELEROMETER
                ), SensorManager.SENSOR_DELAY_NORMAL
            )
        }.onFailure {
            Timber.e("MainActivity.onResume registerListener sensorManager ${it.stackTraceToString()}")
        }
        appRunningInBackground = false
    }

    override fun onPause() {
        super.onPause()
        runCatching {
            sensorListener.let { sensorManager?.unregisterListener(it) }
        }.onFailure {
            Timber.e("MainActivity.onPause unregisterListener sensorListener ${it.stackTraceToString()}")
        }
        appRunningInBackground = true
    }

    @UnstableApi
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intentUriData = intent.data ?: intent.getStringExtra(Intent.EXTRA_TEXT)?.toUri()

    }

    override fun onStop() {
        runCatching {
            unbindService(serviceConnection)
        }.onFailure {
            Timber.e("MainActivity.onStop unbindService ${it.stackTraceToString()}")
        }
        super.onStop()
    }

    @UnstableApi
    override fun onDestroy() {
        super.onDestroy()

        runCatching {
            monet.removeMonetColorsChangedListener(this)
            _monet = null
        }.onFailure {
            Timber.e("MainActivity.onDestroy removeMonetColorsChangedListener ${it.stackTraceToString()}")
        }

    }

    private fun setSystemBarAppearance(isDark: Boolean) {
        with(WindowCompat.getInsetsController(window, window.decorView.rootView)) {
            isAppearanceLightStatusBars = !isDark
            isAppearanceLightNavigationBars = !isDark
        }

        if (!isAtLeastAndroid6) {
            window.statusBarColor =
                (if (isDark) Color.Transparent else Color.Black.copy(alpha = 0.2f)).toArgb()
        }

        if (!isAtLeastAndroid8) {
            window.navigationBarColor =
                (if (isDark) Color.Transparent else Color.Black.copy(alpha = 0.2f)).toArgb()
        }
    }

    companion object {
        const val action_search = "it.fast4x.rimusic.action.search"
        const val action_songs = "it.fast4x.rimusic.action.songs"
        const val action_albums = "it.fast4x.rimusic.action.albums"
        const val action_library = "it.fast4x.rimusic.action.library"
        const val action_rescuecenter = "it.fast4x.rimusic.action.rescuecenter"
    }


    override fun onMonetColorsChanged(
        monet: MonetCompat,
        monetColors: ColorScheme,
        isInitialChange: Boolean
    ) {
        val colorPaletteName =
            preferences.getEnum(colorPaletteNameKey, ColorPaletteName.Dynamic)
        if (!isInitialChange && colorPaletteName == ColorPaletteName.MaterialYou) {
            /*
            monet.updateMonetColors()
            monet.invokeOnReady {
                startApp()
            }
             */
            this@MainActivity.recreate()
        }
    }

    fun InitializeEnvironment() {
        EnvironmentPreferences.preference = EnvironmentPreferenceItem(
            p0 = resources.getString(R.string.env_CrQ0JjAXgv),
            p1 = resources.getString(R.string.env_hNpBzzAn7i),
            p2 = resources.getString(R.string.env_lEi9YM74OL),
            p3 = resources.getString(R.string.env_C0ZR993zmk),
            p4 = resources.getString(R.string.env_w3TFBFL74Y),
            p5 = resources.getString(R.string.env_mcchaHCWyK),
            p6 = resources.getString(R.string.env_L2u4JNdp7L),
            p7 = resources.getString(R.string.env_sqDlfmV4Mt),
            p8 = resources.getString(R.string.env_WpLlatkrVv),
            p9 = resources.getString(R.string.env_1zNshDpFoh),
            p10 = resources.getString(R.string.env_mPVWVuCxJz),
            p11 = resources.getString(R.string.env_auDsjnylCZ),
            p12 = resources.getString(R.string.env_AW52cvJIJx),
            p13 = resources.getString(R.string.env_0RGAyC1Zqu),
            p14 = resources.getString(R.string.env_4Fdmu9Jkax),
            p15 = resources.getString(R.string.env_kuSdQLhP8I),
            p16 = resources.getString(R.string.env_QrgDKwvam1),
            p17 = resources.getString(R.string.env_wLwNESpPtV),
            p18 = resources.getString(R.string.env_JJUQaehRFg),
            p19 = resources.getString(R.string.env_i7WX2bHV6R),
            p20 = resources.getString(R.string.env_XpiuASubrV),
            p21 = resources.getString(R.string.env_lOlIIVw38L),
            p22 = resources.getString(R.string.env_mtcR0FhFEl),
            p23 = resources.getString(R.string.env_DTihHAFaBR),
            p24 = resources.getString(R.string.env_a4AcHS8CSg),
            p25 = resources.getString(R.string.env_krdLqpYLxM),
            p26 = resources.getString(R.string.env_ye6KGLZL7n),
            p27 = resources.getString(R.string.env_ec09m20YH5),
            p28 = resources.getString(R.string.env_LDRlbOvbF1),
            p29 = resources.getString(R.string.env_EEqX0yizf2),
            p30 = resources.getString(R.string.env_i3BRhLrV1v),
            p31 = resources.getString(R.string.env_MApdyHLMyJ),
            p32 = resources.getString(R.string.env_hizI7yLjL4),
            p33 = resources.getString(R.string.env_rLoZP7BF4c),
            p34 = resources.getString(R.string.env_nza34sU88C),
            p35 = resources.getString(R.string.env_dwbUvjWUl3),
            p36 = resources.getString(R.string.env_fqqhBZd0cf),
            p37 = resources.getString(R.string.env_9sZKrkMg8p),
            p38 = resources.getString(R.string.env_aQpNCVOe2i),

            )
    }

}

var appRunningInBackground: Boolean = false

val LocalPlayerServiceBinder = staticCompositionLocalOf<PlayerServiceModern.Binder?> { null }

val LocalPlayerAwareWindowInsets = staticCompositionLocalOf<WindowInsets> { TODO() }

val LocalDownloadHelper = staticCompositionLocalOf<MyDownloadHelper> { error("No Downloader provided") }

@OptIn(ExperimentalMaterial3Api::class)
val LocalPlayerSheetState =
    staticCompositionLocalOf<SheetState> { error("No player sheet state provided") }

//val LocalInternetConnected = staticCompositionLocalOf<Boolean> { error("No Network Status provided") }

