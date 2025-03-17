package it.fast4x.rimusic.ui.components.themed

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import coil.Coil
import coil.annotation.ExperimentalCoilApi
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.enums.CacheType
import it.fast4x.rimusic.enums.CoilDiskCacheMaxSize
import it.fast4x.rimusic.enums.ExoPlayerDiskCacheMaxSize
import it.fast4x.rimusic.enums.ExoPlayerDiskDownloadCacheMaxSize
import it.fast4x.rimusic.utils.coilDiskCacheMaxSizeKey
import it.fast4x.rimusic.utils.exoPlayerDiskCacheMaxSizeKey
import it.fast4x.rimusic.utils.exoPlayerDiskDownloadCacheMaxSizeKey
import it.fast4x.rimusic.utils.rememberPreference


@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalCoilApi::class)
@Composable
fun CacheSpaceIndicator(
    cacheType: CacheType = CacheType.Images,
    circularIndicator: Boolean = false,
    horizontalPadding: Dp = 12.dp,
) {

    val coilDiskCacheMaxSize by rememberPreference(
        coilDiskCacheMaxSizeKey,
        CoilDiskCacheMaxSize.`128MB`
    )
    val exoPlayerDiskCacheMaxSize by rememberPreference(
        exoPlayerDiskCacheMaxSizeKey,
        ExoPlayerDiskCacheMaxSize.`2GB`
    )

    val exoPlayerDiskDownloadCacheMaxSize by rememberPreference(
        exoPlayerDiskDownloadCacheMaxSizeKey,
        ExoPlayerDiskDownloadCacheMaxSize.`2GB`
    )

    when (cacheType) {
        CacheType.Images -> {}
        CacheType.CachedSongs -> {
            if (exoPlayerDiskCacheMaxSize == ExoPlayerDiskCacheMaxSize.Unlimited) return
        }
        CacheType.DownloadedSongs -> {
            if (exoPlayerDiskDownloadCacheMaxSize == ExoPlayerDiskDownloadCacheMaxSize.Unlimited) return
        }
    }

    val context = LocalContext.current
    val binder = LocalPlayerServiceBinder.current

    val imageDiskCacheSize = remember {
        try {
            Coil.imageLoader(context).diskCache?.size
        } catch (e: Exception) {
            0L
        }
    }

    val cachedSongsDiskCacheSize = remember {
        try {
            binder?.cache?.cacheSpace
        } catch (e: Exception) {
            0L
        }
    }

    val downloadedSongsDiskCacheSize = remember {
        try {
            binder?.downloadCache?.cacheSpace
        } catch (e: Exception) {
            0L
        }
    }

    val progressValue = remember { mutableStateOf(0f) }

    LaunchedEffect (Unit, cacheType) {
        progressValue.value =
        when (cacheType) {
            CacheType.Images -> imageDiskCacheSize?.toFloat()
                ?.div(coilDiskCacheMaxSize.bytes.coerceAtLeast(1)) ?: 0.0f
            CacheType.CachedSongs -> cachedSongsDiskCacheSize?.toFloat()
                ?.div(exoPlayerDiskCacheMaxSize.bytes.coerceAtLeast(1)) ?: 0.0f
            CacheType.DownloadedSongs -> downloadedSongsDiskCacheSize?.toFloat()
                ?.div(exoPlayerDiskDownloadCacheMaxSize.bytes.coerceAtLeast(1)) ?: 0.0f
        }
    }

    if (!circularIndicator)
        ProgressIndicator(
            progress = progressValue.value,
            strokeCap = StrokeCap.Round,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .padding(horizontal = horizontalPadding)
        )
    else
        ProgressIndicatorCircular(
            progress = progressValue.value,
            strokeCap = StrokeCap.Round,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .padding(horizontal = horizontalPadding)
        )
}