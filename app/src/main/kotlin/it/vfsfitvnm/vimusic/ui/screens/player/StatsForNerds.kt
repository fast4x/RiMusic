package it.vfsfitvnm.vimusic.ui.screens.player

import android.text.format.Formatter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheSpan
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.models.Format
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.onOverlay
import it.vfsfitvnm.vimusic.ui.styling.overlay
import it.vfsfitvnm.vimusic.utils.color
import it.vfsfitvnm.vimusic.utils.medium
import it.vfsfitvnm.vimusic.utils.rememberVolume
import it.vfsfitvnm.innertube.Innertube
import it.vfsfitvnm.innertube.models.bodies.PlayerBody
import it.vfsfitvnm.innertube.requests.player
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.runBlocking

@Composable
fun StatsForNerds(
    mediaId: String,
    isDisplayed: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (colorPalette, typography) = LocalAppearance.current
    val context = LocalContext.current
    val binder = LocalPlayerServiceBinder.current ?: return

    AnimatedVisibility(
        visible = isDisplayed,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        var cachedBytes by remember(mediaId) {
            mutableStateOf(binder.cache.getCachedBytes(mediaId, 0, -1))
        }

        val format by remember(mediaId) {
            Database.format(mediaId).distinctUntilChanged()
        }.collectAsState(initial = null, context = Dispatchers.IO)

        val volume by rememberVolume(binder.player)

        DisposableEffect(mediaId) {
            val listener = object : Cache.Listener {
                override fun onSpanAdded(cache: Cache, span: CacheSpan) {
                    cachedBytes += span.length
                }

                override fun onSpanRemoved(cache: Cache, span: CacheSpan) {
                    cachedBytes -= span.length
                }

                override fun onSpanTouched(
                    cache: Cache,
                    oldSpan: CacheSpan,
                    newSpan: CacheSpan
                ) = Unit
            }

            binder.cache.addListener(mediaId, listener)

            onDispose {
                binder.cache.removeListener(mediaId, listener)
            }
        }

        Box(
            modifier = modifier
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            onDismiss()
                        }
                    )
                }
                .background(colorPalette.overlay)
                .fillMaxSize()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(all = 16.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    BasicText(
                        text = "Id",
                        style = typography.xs.medium.color(colorPalette.onOverlay)
                    )
                    BasicText(
                        text = "Volume",
                        style = typography.xs.medium.color(colorPalette.onOverlay)
                    )
                    BasicText(
                        text = "Loudness",
                        style = typography.xs.medium.color(colorPalette.onOverlay)
                    )
                    BasicText(
                        text = "Bitrate",
                        style = typography.xs.medium.color(colorPalette.onOverlay)
                    )
                    BasicText(
                        text = "Size",
                        style = typography.xs.medium.color(colorPalette.onOverlay)
                    )
                    BasicText(
                        text = "Cached",
                        style = typography.xs.medium.color(colorPalette.onOverlay)
                    )
                }

                Column {
                    BasicText(
                        text = mediaId,
                        style = typography.xs.medium.color(colorPalette.onOverlay)
                    )
                    BasicText(
                        text = "${volume.times(100).roundToInt()}%",
                        style = typography.xs.medium.color(colorPalette.onOverlay)
                    )
                    BasicText(
                        text = format?.loudnessDb?.let { loudnessDb ->
                            "%.2f dB".format(loudnessDb)
                        } ?: "Unknown",
                        style = typography.xs.medium.color(colorPalette.onOverlay)
                    )
                    BasicText(
                        text = format?.bitrate?.let { bitrate ->
                            "${bitrate / 1000} kbps"
                        } ?: "Unknown",
                        style = typography.xs.medium.color(colorPalette.onOverlay)
                    )
                    BasicText(
                        text = format?.contentLength?.let { contentLength ->
                            Formatter.formatShortFileSize(
                                context,
                                contentLength
                            )
                        } ?: "Unknown",
                        style = typography.xs.medium.color(colorPalette.onOverlay)
                    )
                    BasicText(
                        text = buildString {
                            append(Formatter.formatShortFileSize(context, cachedBytes))

                            format?.contentLength?.let { contentLength ->
                                append(" (${(cachedBytes.toFloat() / contentLength * 100).roundToInt()}%)")
                            }
                        },
                        style = typography.xs.medium.color(colorPalette.onOverlay)
                    )
                }
            }

            if (format != null && format?.itag == null) {
                BasicText(
                    text = "FETCH MISSING DATA",
                    style = typography.xxs.medium.color(colorPalette.onOverlay),
                    modifier = Modifier
                        .clickable(
                            onClick = {
                                query {
                                    runBlocking(Dispatchers.IO) {
                                        Innertube.player(PlayerBody(videoId = mediaId))
                                            ?.map { response ->
                                                response.streamingData?.adaptiveFormats
                                                    ?.findLast { format ->
                                                        format.itag == 251 || format.itag == 140
                                                    }
                                                    ?.let { format ->
                                                        Format(
                                                            songId = mediaId,
                                                            itag = format.itag,
                                                            mimeType = format.mimeType,
                                                            bitrate = format.bitrate,
                                                            loudnessDb = response.playerConfig?.audioConfig?.loudnessDb?.toFloat()?.plus(7),
                                                            contentLength = format.contentLength,
                                                            lastModified = format.lastModified
                                                        )
                                                    }
                                            }
                                    }
                                        ?.getOrNull()
                                        ?.let(Database::insert)
                                }
                            }
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .align(Alignment.BottomEnd)
                )
            }
        }
    }
}
