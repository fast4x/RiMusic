package it.fast4x.rimusic.utils


import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.platform.LocalContext

import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.Download

import it.fast4x.rimusic.Database
import it.fast4x.rimusic.LocalDownloadHelper
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.appContext
import it.fast4x.rimusic.enums.DownloadedStateMedia
import it.fast4x.rimusic.service.MyDownloadHelper
import it.fast4x.rimusic.service.MyPreCacheHelper
import it.fast4x.rimusic.service.isLocal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@UnstableApi
@Composable
fun InitDownloader() {
    val context = LocalContext.current
    MyDownloadHelper.getDownloadManager(context)
    MyDownloadHelper.getDownloads()
}


@UnstableApi
@Composable
fun downloadedStateMedia(mediaId: String): DownloadedStateMedia {
    val binder = LocalPlayerServiceBinder.current

    val cachedBytes by remember(mediaId) {
        try {
            mutableStateOf(binder?.cache?.getCachedBytes(mediaId, 0, -1))
        } catch (e: Exception) {
            mutableLongStateOf(0L)
        }

    }


    var isDownloaded by remember { mutableStateOf(false) }
    LaunchedEffect(mediaId) {
        MyDownloadHelper.getDownload(mediaId).collect { download ->
            isDownloaded = download?.state == Download.STATE_COMPLETED
        }
    }
    var isCached by remember { mutableStateOf(false) }
    LaunchedEffect(mediaId) {
        Database.format(mediaId).distinctUntilChanged().collectLatest { format ->
           isCached = format?.contentLength == cachedBytes
        }
    }

    return when {
        isDownloaded && isCached -> DownloadedStateMedia.CACHED_AND_DOWNLOADED
        isDownloaded && !isCached -> DownloadedStateMedia.DOWNLOADED
        !isDownloaded && isCached -> DownloadedStateMedia.CACHED
        else -> DownloadedStateMedia.NOT_CACHED_OR_DOWNLOADED
    }
}


@UnstableApi
fun manageDownload(
    context: android.content.Context,
    mediaItem: MediaItem,
    downloadState: Boolean = false
) {

    if (mediaItem.isLocal) return

    if (downloadState) {
        MyDownloadHelper.removeDownload(context = context, mediaItem = mediaItem)
    } else {
        if (isNetworkConnected(context)) {
            MyDownloadHelper.addDownload(context = context, mediaItem = mediaItem)
        }
    }

}

@UnstableApi
fun preCacheMedia(
    context: android.content.Context,
    mediaItem: MediaItem
) {
    if (mediaItem.isLocal || !isNetworkConnected(appContext())) return
    val cache: SimpleCache by lazy {
        principalCache.getInstance(context)
    }
    var contentLength = 0L
    CoroutineScope(Dispatchers.IO).launch {
         contentLength = Database.formatContentLength(mediaItem.mediaId).also {
             println("preCacheMedia: contentLength inside is $it")
         }
    }
    println("preCacheMedia: mediaId ${mediaItem.mediaId} $contentLength")
    val isCached = try {
        cache.isCached(mediaItem.mediaId,0L, contentLength)
    } catch (e: Exception) {
        false
    }
    if (!isCached) {
        println("preCacheMedia: mediaId ${mediaItem.mediaId} not cached")
        MyPreCacheHelper.addDownload(context = context, mediaItem = mediaItem)
    } else println("preCacheMedia: mediaId ${mediaItem.mediaId} is cached")


}

@UnstableApi
@Composable
fun getDownloadState(mediaId: String): Int {
    val downloader = LocalDownloadHelper.current
    val context = LocalContext.current
    if (!isNetworkConnected(context)) return 3

    return downloader.getDownload(mediaId).collectAsState(initial = null).value?.state
        ?: 3
}

@OptIn(UnstableApi::class)
@Composable
fun isDownloadedSong(mediaId: String): Boolean {
    return when (downloadedStateMedia(mediaId)) {
        DownloadedStateMedia.CACHED -> false
        DownloadedStateMedia.CACHED_AND_DOWNLOADED, DownloadedStateMedia.DOWNLOADED -> true
        else -> false
    }
}