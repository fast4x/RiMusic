package it.fast4x.rimusic.ui.screens.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.text.format.Formatter
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.media3.common.util.UnstableApi
import coil.Coil
import coil.annotation.ExperimentalCoilApi
import it.fast4x.rimusic.LocalPlayerAwareWindowInsets
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.CoilDiskCacheMaxSize
import it.fast4x.rimusic.enums.ExoPlayerDiskCacheMaxSize
import it.fast4x.rimusic.enums.ExoPlayerDiskDownloadCacheMaxSize
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.ui.components.themed.HeaderWithIcon
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.utils.coilDiskCacheMaxSizeKey
import it.fast4x.rimusic.utils.exoPlayerAlternateCacheLocationKey
import it.fast4x.rimusic.utils.exoPlayerCustomCacheKey
import it.fast4x.rimusic.utils.exoPlayerDiskCacheMaxSizeKey
import it.fast4x.rimusic.utils.exoPlayerDiskDownloadCacheMaxSizeKey
import it.fast4x.rimusic.utils.rememberPreference
import me.knighthat.colorPalette

@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalCoilApi::class)
@ExperimentalAnimationApi
@UnstableApi
@Composable
fun CacheSettings() {
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

    var exoPlayerAlternateCacheLocation by rememberPreference(
        exoPlayerAlternateCacheLocationKey,""
    )

    val dirRequest = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            context.applicationContext.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.applicationContext.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            Log.d("exoAltLocationCache",uri.path.toString())
            exoPlayerAlternateCacheLocation = uri.path.toString()
        }
    }


    var showExoPlayerCustomCacheDialog by remember { mutableStateOf(false) }
    var exoPlayerCustomCache by rememberPreference(
        exoPlayerCustomCacheKey,32
    )

    //val release = Build.VERSION.RELEASE;
    val sdkVersion = Build.VERSION.SDK_INT;
    //if (sdkVersion.toShort() < 29) exoPlayerAlternateCacheLocation=""
    //Log.d("SystemInfo","Android SDK: " + sdkVersion + " (" + release +")")

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
            .padding(
                LocalPlayerAwareWindowInsets.current
                    .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
                    .asPaddingValues()
            )
    ) {
        HeaderWithIcon(
            title = stringResource(R.string.cache),
            iconId = R.drawable.sync,
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

            SettingsGroupSpacer()

            SettingsEntryGroupText(title = stringResource(R.string.image_cache))

            SettingsDescription(
                text = "${
                    Formatter.formatShortFileSize(
                        context,
                        diskCacheSize
                    )
                } ${stringResource(R.string.used)} (${diskCacheSize * 100 / coilDiskCacheMaxSize.bytes.coerceAtLeast(1)}%)"
            )

            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.max_size),
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
                        CoilDiskCacheMaxSize.`8GB` -> "8GB"
                        CoilDiskCacheMaxSize.Custom -> stringResource(R.string.custom)
                    }
                }
            )
        }


        binder?.cache?.let { cache ->
            val diskCacheSize by remember {
                derivedStateOf {
                    cache.cacheSpace
                }
            }



            SettingsGroupSpacer()

            SettingsEntryGroupText(title = stringResource(R.string.song_cache_by_player))

            //if(exoPlayerDiskCacheMaxSize != ExoPlayerDiskCacheMaxSize.Disabled)
            SettingsDescription(
                text = buildString {
                    append(Formatter.formatShortFileSize(context, diskCacheSize))
                    //append(" ${stringResource(R.string.used)}")
                    //append(" (${diskCacheSize * 100 / exoPlayerDiskCacheMaxSize.bytes}%)")
                    /*
                    when (val size = exoPlayerDiskCacheMaxSize) {
                        ExoPlayerDiskCacheMaxSize.Unlimited -> {}
                        ExoPlayerDiskCacheMaxSize.Custom -> { exoPlayerCustomCache }
                        else -> append(" (${diskCacheSize * 100 / size.bytes}%)")

                    }
                     */
                }
            )

            /*
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


            if (showExoPlayerCustomCacheDialog)
                InputNumericDialog(
                    title = stringResource(R.string.set_custom_cache),
                    placeholder = stringResource(R.string.enter_value_in_mb),
                    value = exoPlayerCustomCache.toString(),
                    valueMin = "32",
                    valueMax = "1000000",
                    onDismiss = { showExoPlayerCustomCacheDialog = false },
                    setValue = {
                        //Log.d("customCache", it)
                        exoPlayerCustomCache = it.toInt()
                        showExoPlayerCustomCacheDialog = false
                    }
                )
            */

        }


        binder?.downloadCache?.let { downloadCache ->
            val diskDownloadCacheSize by remember {
                derivedStateOf {
                    downloadCache.cacheSpace
                }
            }

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
        }


        SettingsGroupSpacer()

        SettingsEntryGroupText(title = stringResource(R.string.folder_cache))

        SettingsDescription(
            text = stringResource(R.string.custom_cache_from_android_10_may_not_be_available)
        )

        SettingsEntry(
            title = stringResource(R.string.cache_location_folder),
            text = if (exoPlayerAlternateCacheLocation == "") "Default" else exoPlayerAlternateCacheLocation,
            isEnabled = sdkVersion.toShort() < 29,
            onClick = {
                dirRequest.launch(null)
            }
        )

        ImportantSettingsDescription(text = stringResource(R.string.cache_reset_by_clicking_button))

        SettingsEntry(
            title = stringResource(R.string.reset_cache_location_folder),
            text = "",
            isEnabled = if (sdkVersion.toShort() < 29) true else false,
            onClick = {
                exoPlayerAlternateCacheLocation = ""
            }
        )


    }
}
