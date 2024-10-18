package it.fast4x.rimusic.ui.screens.settings

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.os.Build
import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import coil.Coil
import coil.annotation.ExperimentalCoilApi
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.CoilDiskCacheMaxSize
import it.fast4x.rimusic.enums.ExoPlayerCacheLocation
import it.fast4x.rimusic.enums.ExoPlayerDiskCacheMaxSize
import it.fast4x.rimusic.enums.ExoPlayerDiskDownloadCacheMaxSize
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.enums.PopupType
import it.fast4x.rimusic.internal
import it.fast4x.rimusic.path
import it.fast4x.rimusic.query
import it.fast4x.rimusic.service.MyDownloadService
import it.fast4x.rimusic.service.PlayerService
import it.fast4x.rimusic.ui.components.themed.ConfirmationDialog
import it.fast4x.rimusic.ui.components.themed.DefaultDialog
import it.fast4x.rimusic.ui.components.themed.HeaderIconButton
import it.fast4x.rimusic.ui.components.themed.HeaderWithIcon
import it.fast4x.rimusic.ui.components.themed.InputNumericDialog
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.shimmer
import it.fast4x.rimusic.utils.bold
import it.fast4x.rimusic.utils.coilCustomDiskCacheKey
import it.fast4x.rimusic.utils.coilDiskCacheMaxSizeKey
import it.fast4x.rimusic.utils.exoPlayerCacheLocationKey
import it.fast4x.rimusic.utils.exoPlayerCustomCacheKey
import it.fast4x.rimusic.utils.exoPlayerDiskCacheMaxSizeKey
import it.fast4x.rimusic.utils.exoPlayerDiskDownloadCacheMaxSizeKey
import it.fast4x.rimusic.utils.intent
import it.fast4x.rimusic.utils.pauseSearchHistoryKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.semiBold
import kotlinx.coroutines.flow.distinctUntilChanged
import me.knighthat.colorPalette
import me.knighthat.typography
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.system.exitProcess

@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalCoilApi::class)
@ExperimentalAnimationApi
@UnstableApi
@Composable
fun DataSettings() {
    val context = LocalContext.current
    val binder = LocalPlayerServiceBinder.current

    var coilDiskCacheMaxSize by rememberPreference(
        coilDiskCacheMaxSizeKey,
        CoilDiskCacheMaxSize.`128MB`
    )
    var exoPlayerDiskCacheMaxSize by rememberPreference(
        exoPlayerDiskCacheMaxSizeKey,
        ExoPlayerDiskCacheMaxSize.`32MB`
    )

    var exoPlayerDiskDownloadCacheMaxSize by rememberPreference(
        exoPlayerDiskDownloadCacheMaxSizeKey,
        ExoPlayerDiskDownloadCacheMaxSize.`2GB`
    )

    /*
    var exoPlayerAlternateCacheLocation by rememberPreference(
        exoPlayerAlternateCacheLocationKey,""
    )
     */

    var exoPlayerCacheLocation by rememberPreference(
        exoPlayerCacheLocationKey, ExoPlayerCacheLocation.System
    )


    /*
    val dirRequest = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            context.applicationContext.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.applicationContext.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            //Log.d("exoAltLocationCache",uri.path.toString())
            exoPlayerAlternateCacheLocation = uri.path.toString()
        }
    }
     */


    var showExoPlayerCustomCacheDialog by remember { mutableStateOf(false) }
    var exoPlayerCustomCache by rememberPreference(
        exoPlayerCustomCacheKey,32
    )

    var showCoilCustomDiskCacheDialog by remember { mutableStateOf(false) }
    var coilCustomDiskCache by rememberPreference(
        coilCustomDiskCacheKey,32
    )

    //val release = Build.VERSION.RELEASE;
    val sdkVersion = Build.VERSION.SDK_INT;
    //if (sdkVersion.toShort() < 29) exoPlayerAlternateCacheLocation=""
    //Log.d("SystemInfo","Android SDK: " + sdkVersion + " (" + release +")")

    val backupLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.sqlite3")) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            query {
                Database.checkpoint()

                context.applicationContext.contentResolver.openOutputStream(uri)
                    ?.use { outputStream ->
                        FileInputStream(Database.internal.path).use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
            }
        }

    var exitAfterRestore by remember { mutableStateOf(false) }

    val restoreLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            query {
                Database.checkpoint()
                Database.internal.close()

                context.applicationContext.contentResolver.openInputStream(uri)
                    ?.use { inputStream ->
                        FileOutputStream(Database.internal.path).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                context.stopService(context.intent<PlayerService>())
                context.stopService(context.intent<MyDownloadService>())

                exitAfterRestore = true
            }
        }

    if (exitAfterRestore)
        DefaultDialog(
            onDismiss = {
                exitAfterRestore = false
                exitProcess(0)
            },
            content = {
                BasicText(
                    text = stringResource(R.string.restore_completed),
                    style = typography().s.bold.copy(color = colorPalette().text),
                )
                Spacer(modifier = Modifier.height(20.dp))
                BasicText(
                    text = stringResource(R.string.click_to_close),
                    style = typography().xs.semiBold.copy(color = colorPalette().textSecondary),
                )
                Spacer(modifier = Modifier.height(10.dp))
                Image(
                    painter = painterResource(R.drawable.server),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorPalette().shimmer),
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            exitAfterRestore = false
                            exitProcess(0)
                        }
                )
            }

        )

    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }

    if (isExporting) {
        ConfirmationDialog(
            text = stringResource(R.string.export_the_database),
            onDismiss = { isExporting = false },
            onConfirm = {
                @SuppressLint("SimpleDateFormat")
                val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")
                try {
                    backupLauncher.launch("rimusic_${dateFormat.format(Date())}.db")
                } catch (e: ActivityNotFoundException) {
                    SmartMessage(context.resources.getString(R.string.info_not_find_app_create_doc), type = PopupType.Warning, context = context)
                }
            }
        )
    }
    if (isImporting) {
        ConfirmationDialog(
            text = stringResource(R.string.import_the_database),
            onDismiss = { isImporting = false },
            onConfirm = {
                try {
                    restoreLauncher.launch(
                        arrayOf(
                            "application/vnd.sqlite3",
                            "application/x-sqlite3",
                            "application/octet-stream"
                        )
                    )
                } catch (e: ActivityNotFoundException) {
                    SmartMessage(context.resources.getString(R.string.info_not_find_app_open_doc), type = PopupType.Warning, context = context)
                }
            }
        )
    }

    var pauseSearchHistory by rememberPreference(pauseSearchHistoryKey, false)

    val queriesCount by remember {
        Database.queriesCount().distinctUntilChanged()
    }.collectAsState(initial = 0)

    var cleanCacheOfflineSongs by remember {
        mutableStateOf(false)
    }

    var cleanDownloadCache by remember {
        mutableStateOf(false)
    }
    var cleanCacheImages by remember {
        mutableStateOf(false)
    }

    if (cleanCacheOfflineSongs) {
        ConfirmationDialog(
            text = stringResource(R.string.do_you_really_want_to_delete_cache),
            onDismiss = {
                cleanCacheOfflineSongs = false
            },
            onConfirm = {
                binder?.cache?.keys?.forEach { song ->
                    binder.cache.removeResource(song)
                }
            }
        )
    }

    if (cleanDownloadCache) {
        ConfirmationDialog(
            text = stringResource(R.string.do_you_really_want_to_delete_cache),
            onDismiss = {
                cleanDownloadCache = false
            },
            onConfirm = {
                binder?.downloadCache?.keys?.forEach { song ->
                    binder.downloadCache.removeResource(song)
                }
            }
        )
    }

    if (cleanCacheImages) {
        ConfirmationDialog(
            text = stringResource(R.string.do_you_really_want_to_delete_cache),
            onDismiss = {
                cleanCacheImages = false
            },
            onConfirm = {
                Coil.imageLoader(context).diskCache?.clear()
            }
        )
    }

    Column(
        modifier = Modifier
            .background(colorPalette().background0)
            //.fillMaxSize()
            .fillMaxHeight()
            .fillMaxWidth(
                if( NavigationBarPosition.Right.isCurrent() )
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
            title = stringResource(R.string.tab_data),
            iconId = R.drawable.server,
            enabled = false,
            showIcon = true,
            modifier = Modifier,
            onClick = {}
        )

        SettingsDescription(text = stringResource(R.string.cache_cleared))

        Coil.imageLoader(context).diskCache?.let { diskCache ->
            val diskCacheSize = remember(diskCache) {
                diskCache.size
            }

            //SettingsGroupSpacer()

            //SettingsEntryGroupText(title = stringResource(R.string.image_cache))
/*
            SettingsDescription(
                text = "${
                    Formatter.formatShortFileSize(
                        context,
                        diskCacheSize
                    )
                } ${stringResource(R.string.used)} (${diskCacheSize * 100 / coilDiskCacheMaxSize.bytes.coerceAtLeast(1)}%)"
            )

 */
            SettingsGroupSpacer()
            SettingsEntryGroupText(title = stringResource(R.string.cache))

            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.image_cache_max_size),
                titleSecondary = when (coilDiskCacheMaxSize) {
                    CoilDiskCacheMaxSize.Custom -> buildString {
                            append(Formatter.formatShortFileSize(context, diskCacheSize))
                            append("/${Formatter.formatShortFileSize(context, coilCustomDiskCache.toLong() * 1000 * 1000)}")
                            append(" ${stringResource(R.string.used)}")
                    }
                    else -> buildString {
                        append(Formatter.formatShortFileSize(context, diskCacheSize))
                        append(" ${stringResource(R.string.used)}")
                        append(" (${diskCacheSize * 100 / coilDiskCacheMaxSize.bytes}%)")
                    }
                },
                trailingContent = {
                    HeaderIconButton(
                        icon = R.drawable.trash,
                        enabled = true,
                        color = colorPalette().text,
                        onClick = { cleanCacheImages = true }
                    )
                },
                selectedValue = coilDiskCacheMaxSize,
                onValueSelected = {
                    coilDiskCacheMaxSize = it
                    if (coilDiskCacheMaxSize == CoilDiskCacheMaxSize.Custom)
                        showCoilCustomDiskCacheDialog = true
                },
                valueText = {
                    when (it) {
                        CoilDiskCacheMaxSize.Custom -> stringResource(R.string.custom)
                        CoilDiskCacheMaxSize.`32MB` -> "32MB"
                        CoilDiskCacheMaxSize.`64MB` -> "64MB"
                        CoilDiskCacheMaxSize.`128MB` -> "128MB"
                        CoilDiskCacheMaxSize.`256MB`-> "256MB"
                        CoilDiskCacheMaxSize.`512MB`-> "512MB"
                        CoilDiskCacheMaxSize.`1GB`-> "1GB"
                        CoilDiskCacheMaxSize.`2GB` -> "2GB"
                        CoilDiskCacheMaxSize.`4GB` -> "4GB"
                        CoilDiskCacheMaxSize.`8GB` -> "8GB"
                    }
                }
            )

            if (showCoilCustomDiskCacheDialog)
                InputNumericDialog(
                    title = stringResource(R.string.set_custom_cache),
                    placeholder = stringResource(R.string.enter_value_in_mb),
                    value = coilCustomDiskCache.toString(),
                    valueMin = "32",
                    valueMax = "10000",
                    onDismiss = { showCoilCustomDiskCacheDialog = false },
                    setValue = {
                        //Log.d("customCache", it)
                        coilCustomDiskCache = it.toInt()
                        showCoilCustomDiskCacheDialog = false
                    }
                )

            /*
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.image_cache_max_size),
                titleSecondary = "${
                    Formatter.formatShortFileSize(
                        context,
                        diskCacheSize
                    )
                } ${stringResource(R.string.used)} (${diskCacheSize * 100 / coilDiskCacheMaxSize.bytes.coerceAtLeast(1)}%)",
                selectedValue = coilDiskCacheMaxSize,
                onValueSelected = { coilDiskCacheMaxSize = it },
                valueText = {
                    when (it) {
                        CoilDiskCacheMaxSize.`32MB` -> "32MB"
                        CoilDiskCacheMaxSize.`64MB` -> "64MB"
                        CoilDiskCacheMaxSize.`128MB` -> "128MB"
                        CoilDiskCacheMaxSize.`256MB`-> "256MB"
                        CoilDiskCacheMaxSize.`512MB`-> "512MB"
                        CoilDiskCacheMaxSize.`1GB`-> "1GB"
                        CoilDiskCacheMaxSize.`2GB` -> "2GB"
                        CoilDiskCacheMaxSize.`4GB` -> "4GB"
                    }
                }
            )
            */
        }

        binder?.cache?.let { cache ->
            val diskCacheSize by remember {
                derivedStateOf {
                    cache.cacheSpace
                }
            }

            //SettingsGroupSpacer()

            //SettingsEntryGroupText(title = stringResource(R.string.song_cache_by_player))
            /*
            if(exoPlayerDiskCacheMaxSize != ExoPlayerDiskCacheMaxSize.Disabled)
            SettingsDescription(
                text = buildString {
                    append(Formatter.formatShortFileSize(context, diskCacheSize))
                    append(" ${stringResource(R.string.used)}")
                    when (val size = exoPlayerDiskCacheMaxSize) {
                        ExoPlayerDiskCacheMaxSize.Unlimited -> {}
                        ExoPlayerDiskCacheMaxSize.Custom -> { exoPlayerCustomCache }
                        else -> append(" (${diskCacheSize * 100 / size.bytes}%)")
                    }
                }
            )

            if(exoPlayerDiskCacheMaxSize == ExoPlayerDiskCacheMaxSize.Custom)
                SettingsDescription(
                    text = stringResource(R.string.custom_cache_size) +" "+exoPlayerCustomCache+"MB"
                )


            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.max_size),
                selectedValue = exoPlayerDiskCacheMaxSize,
                onValueSelected = {
                    exoPlayerDiskCacheMaxSize = it
                    if (exoPlayerDiskCacheMaxSize == ExoPlayerDiskCacheMaxSize.Custom)
                    showExoPlayerCustomCacheDialog = true
                },
                valueText = {
                    when (it) {
                        ExoPlayerDiskCacheMaxSize.Disabled -> stringResource(R.string.turn_off)
                        ExoPlayerDiskCacheMaxSize.Unlimited -> stringResource(R.string.unlimited)
                        ExoPlayerDiskCacheMaxSize.Custom -> stringResource(R.string.custom)
                        ExoPlayerDiskCacheMaxSize.`32MB` -> "32MB"
                        ExoPlayerDiskCacheMaxSize.`512MB` -> "512MB"
                        ExoPlayerDiskCacheMaxSize.`1GB` -> "1GB"
                        ExoPlayerDiskCacheMaxSize.`2GB` -> "2GB"
                        ExoPlayerDiskCacheMaxSize.`4GB` -> "4GB"
                        ExoPlayerDiskCacheMaxSize.`8GB` -> "8GB"

                    }
                }
            )
             */

            /*
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 30.dp)
                    .fillMaxWidth()
            ){
                Column (
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                )
                {
                    BasicText(
                        text = stringResource(R.string.song_cache_max_size),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = typography().xs.semiBold.secondary.copy(color = colorPalette().text),
                    )
                    BasicText(
                        text = Formatter.formatShortFileSize(context, diskCacheSize),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = typography().xs.semiBold.secondary.copy(color = colorPalette().textDisabled),
                    )
                }


                HeaderIconButton(
                    icon = R.drawable.trash,
                    enabled = true,
                    color = colorPalette().text,
                    onClick = { cleanCacheOfflineSongs = true }
                )
            }
            */


            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.song_cache_max_size),
                titleSecondary = when (exoPlayerDiskCacheMaxSize) {
                    ExoPlayerDiskCacheMaxSize.Disabled -> ""
                    ExoPlayerDiskCacheMaxSize.Custom -> buildString {
                        append(Formatter.formatShortFileSize(context, diskCacheSize))
                        append("/${Formatter.formatShortFileSize(context,
                            exoPlayerCustomCache.toLong() * 1000 * 1000
                        )}")
                        append(" ${stringResource(R.string.used)}")
                    }
                        // stringResource(R.string.custom_cache_size) +" "+exoPlayerCustomCache+"MB"
                    else -> buildString {
                        append(Formatter.formatShortFileSize(context, diskCacheSize))
                        append(" ${stringResource(R.string.used)}")
                        when (val size = exoPlayerDiskCacheMaxSize) {
                            ExoPlayerDiskCacheMaxSize.Unlimited -> {}
                            ExoPlayerDiskCacheMaxSize.Custom -> {} // only needed because of UNLIMITED
                            else -> append(" (${diskCacheSize * 100 / size.bytes}%)")
                        }
                    }
                },
                trailingContent = {
                    HeaderIconButton(
                        icon = R.drawable.trash,
                        enabled = true,
                        color = colorPalette().text,
                        onClick = { cleanCacheOfflineSongs = true }
                    )
                },
                selectedValue = exoPlayerDiskCacheMaxSize,
                onValueSelected = {
                    exoPlayerDiskCacheMaxSize = it
                    if (exoPlayerDiskCacheMaxSize == ExoPlayerDiskCacheMaxSize.Custom)
                        showExoPlayerCustomCacheDialog = true
                },
                valueText = {
                    when (it) {
                        ExoPlayerDiskCacheMaxSize.Disabled -> stringResource(R.string.turn_off)
                        ExoPlayerDiskCacheMaxSize.Unlimited -> stringResource(R.string.unlimited)
                        ExoPlayerDiskCacheMaxSize.Custom -> stringResource(R.string.custom)
                        ExoPlayerDiskCacheMaxSize.`32MB` -> "32MB"
                        ExoPlayerDiskCacheMaxSize.`512MB` -> "512MB"
                        ExoPlayerDiskCacheMaxSize.`1GB` -> "1GB"
                        ExoPlayerDiskCacheMaxSize.`2GB` -> "2GB"
                        ExoPlayerDiskCacheMaxSize.`4GB` -> "4GB"
                        ExoPlayerDiskCacheMaxSize.`8GB` -> "8GB"

                    }
                }
            )

            if (showExoPlayerCustomCacheDialog)
                InputNumericDialog(
                    title = stringResource(R.string.set_custom_cache),
                    placeholder = stringResource(R.string.enter_value_in_mb),
                    value = exoPlayerCustomCache.toString(),
                    valueMin = "32",
                    valueMax = "10000",
                    onDismiss = { showExoPlayerCustomCacheDialog = false },
                    setValue = {
                        //Log.d("customCache", it)
                        exoPlayerCustomCache = it.toInt()
                        showExoPlayerCustomCacheDialog = false
                    }
                )


        }

        binder?.downloadCache?.let { downloadCache ->
            val diskDownloadCacheSize by remember {
                derivedStateOf {
                    downloadCache.cacheSpace
                }
            }
            /*
            SettingsGroupSpacer()

            SettingsEntryGroupText(title = stringResource(R.string.song_cache_by_download))

            if(exoPlayerDiskDownloadCacheMaxSize != ExoPlayerDiskDownloadCacheMaxSize.Disabled)
            SettingsDescription(
                text = buildString {
                    append(Formatter.formatShortFileSize(context, diskDownloadCacheSize))
                    append(" ${stringResource(R.string.used)}")
                    when (val size = exoPlayerDiskDownloadCacheMaxSize) {
                        ExoPlayerDiskDownloadCacheMaxSize.Unlimited -> {}
                        else -> append(" (${diskDownloadCacheSize * 100 / size.bytes}%)")
                    }
                }
            )

            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.max_size),
                selectedValue = exoPlayerDiskDownloadCacheMaxSize,
                onValueSelected = { exoPlayerDiskDownloadCacheMaxSize = it },
                valueText = {
                    when (it) {
                        ExoPlayerDiskDownloadCacheMaxSize.Disabled -> stringResource(R.string.turn_off)
                        ExoPlayerDiskDownloadCacheMaxSize.Unlimited -> stringResource(R.string.unlimited)
                        ExoPlayerDiskDownloadCacheMaxSize.`32MB` -> "32MB"
                        ExoPlayerDiskDownloadCacheMaxSize.`512MB` -> "512MB"
                        ExoPlayerDiskDownloadCacheMaxSize.`1GB` -> "1GB"
                        ExoPlayerDiskDownloadCacheMaxSize.`2GB` -> "2GB"
                        ExoPlayerDiskDownloadCacheMaxSize.`4GB` -> "4GB"
                        ExoPlayerDiskDownloadCacheMaxSize.`8GB` -> "8GB"

                    }
                }
            )
            */
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.song_download_max_size),
                titleSecondary = when (exoPlayerDiskDownloadCacheMaxSize) {
                    ExoPlayerDiskDownloadCacheMaxSize.Disabled -> ""
                    else -> buildString {
                        append(Formatter.formatShortFileSize(context, diskDownloadCacheSize))
                        append(" ${stringResource(R.string.used)}")
                        when (val size = exoPlayerDiskDownloadCacheMaxSize) {
                            ExoPlayerDiskDownloadCacheMaxSize.Unlimited -> {}
                            else -> append(" (${diskDownloadCacheSize * 100 / size.bytes}%)")
                        }
                    }
                },
                trailingContent = {
                    HeaderIconButton(
                        icon = R.drawable.trash,
                        enabled = true,
                        color = colorPalette().text,
                        onClick = { cleanDownloadCache = true }
                    )
                },
                selectedValue = exoPlayerDiskDownloadCacheMaxSize,
                onValueSelected = { exoPlayerDiskDownloadCacheMaxSize = it },
                valueText = {
                    when (it) {
                        ExoPlayerDiskDownloadCacheMaxSize.Disabled -> stringResource(R.string.turn_off)
                        ExoPlayerDiskDownloadCacheMaxSize.Unlimited -> stringResource(R.string.unlimited)
                        ExoPlayerDiskDownloadCacheMaxSize.`32MB` -> "32MB"
                        ExoPlayerDiskDownloadCacheMaxSize.`512MB` -> "512MB"
                        ExoPlayerDiskDownloadCacheMaxSize.`1GB` -> "1GB"
                        ExoPlayerDiskDownloadCacheMaxSize.`2GB` -> "2GB"
                        ExoPlayerDiskDownloadCacheMaxSize.`4GB` -> "4GB"
                        ExoPlayerDiskDownloadCacheMaxSize.`8GB` -> "8GB"

                    }
                }
            )

        }

        EnumValueSelectorSettingsEntry(
            title = stringResource(R.string.set_cache_location),
            selectedValue = exoPlayerCacheLocation,
            onValueSelected = { exoPlayerCacheLocation = it },
            valueText = {
                when (it) {
                    ExoPlayerCacheLocation.Private -> stringResource(R.string.cache_location_private)
                    ExoPlayerCacheLocation.System -> stringResource(R.string.cache_location_system)
                }
            }
        )

        SettingsDescription(stringResource(R.string.info_private_cache_location_can_t_cleaned))
        ImportantSettingsDescription(stringResource(R.string.restarting_rimusic_is_required))

        SettingsGroupSpacer()

        //SettingsEntryGroupText(title = stringResource(R.string.folder_cache))

        /*
        SettingsDescription(
            text = stringResource(R.string.custom_cache_from_android_10_may_not_be_available)
        )


        SettingsEntry(
            title = stringResource(R.string.cache_location_folder),
            text = if (exoPlayerAlternateCacheLocation == "") stringResource(R.string._default) else exoPlayerAlternateCacheLocation,
            //isEnabled = if (sdkVersion.toShort() < 29) true else false,
            onClick = {
                dirRequest.launch(null)
            }
        )


        ImportantSettingsDescription(text = stringResource(R.string.cache_reset_by_clicking_button))

        SettingsEntry(
            title = stringResource(R.string.reset_cache_location_folder),
            text = "",
            //isEnabled = if (sdkVersion.toShort() < 29) true else false,
            onClick = {
                exoPlayerAlternateCacheLocation = ""
            }
        )
         */


        SettingsEntryGroupText(title = stringResource(R.string.title_backup_and_restore))

        SettingsEntry(
            title = stringResource(R.string.save_to_backup),
            text = stringResource(R.string.export_the_database),
            onClick = {
                isExporting = true
            }
        )
        SettingsDescription(text = stringResource(R.string.personal_preference))

        SettingsEntry(
            title = stringResource(R.string.restore_from_backup),
            text = stringResource(R.string.import_the_database),
            onClick = {
                isImporting = true
            }
        )
        ImportantSettingsDescription(text = stringResource(
            R.string.existing_data_will_be_overwritten,
            context.applicationInfo.nonLocalizedLabel
        ))

        SettingsGroupSpacer()
        SettingsEntryGroupText(title = stringResource(R.string.search_history))

        SwitchSettingEntry(
            title = stringResource(R.string.pause_search_history),
            text = stringResource(R.string.neither_save_new_searched_query),
            isChecked = pauseSearchHistory,
            onCheckedChange = { pauseSearchHistory = it }
        )
        SettingsEntry(
            title = stringResource(R.string.clear_search_history),
            text = if (queriesCount > 0) {
                "${stringResource(R.string.delete)} " + queriesCount + stringResource(R.string.search_queries)
            } else {
                stringResource(R.string.history_is_empty)
            },
            isEnabled = queriesCount > 0,
            onClick = { query(Database::clearQueries) }
        )
        SettingsGroupSpacer(
            modifier = Modifier.height(Dimensions.bottomSpacer)
        )
    }
}
