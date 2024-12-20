package it.fast4x.rimusic.extensions.nextvisualizer

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.extensions.nextvisualizer.painters.Painter
import it.fast4x.rimusic.extensions.nextvisualizer.painters.fft.FftBar
import it.fast4x.rimusic.extensions.nextvisualizer.painters.fft.FftCBar
import it.fast4x.rimusic.extensions.nextvisualizer.painters.fft.FftCLine
import it.fast4x.rimusic.extensions.nextvisualizer.painters.fft.FftCWave
import it.fast4x.rimusic.extensions.nextvisualizer.painters.fft.FftCWaveRgb
import it.fast4x.rimusic.extensions.nextvisualizer.painters.fft.FftLine
import it.fast4x.rimusic.extensions.nextvisualizer.painters.fft.FftWave
import it.fast4x.rimusic.extensions.nextvisualizer.painters.fft.FftWaveRgb
import it.fast4x.rimusic.extensions.nextvisualizer.painters.misc.Gradient
import it.fast4x.rimusic.extensions.nextvisualizer.painters.misc.Icon
import it.fast4x.rimusic.extensions.nextvisualizer.painters.modifier.Beat
import it.fast4x.rimusic.extensions.nextvisualizer.painters.modifier.Blend
import it.fast4x.rimusic.extensions.nextvisualizer.painters.modifier.Compose
import it.fast4x.rimusic.extensions.nextvisualizer.painters.modifier.Glitch
import it.fast4x.rimusic.extensions.nextvisualizer.painters.modifier.Move
import it.fast4x.rimusic.extensions.nextvisualizer.painters.modifier.Shake
import it.fast4x.rimusic.extensions.nextvisualizer.painters.waveform.WfmAnalog
import it.fast4x.rimusic.extensions.nextvisualizer.utils.Preset
import it.fast4x.rimusic.extensions.nextvisualizer.utils.VisualizerHelper
import it.fast4x.rimusic.extensions.nextvisualizer.views.VisualizerView
import it.fast4x.rimusic.ui.components.themed.IconButton
import it.fast4x.rimusic.ui.components.themed.SecondaryTextButton
import it.fast4x.rimusic.utils.DisposableListener
import it.fast4x.rimusic.utils.currentVisualizerKey
import it.fast4x.rimusic.utils.currentWindow
import it.fast4x.rimusic.utils.getBitmapFromUrl
import it.fast4x.rimusic.utils.hasPermission
import it.fast4x.rimusic.utils.isCompositionLaunched
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.resize
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.visualizerEnabledKey
import kotlinx.coroutines.launch
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.typography
import timber.log.Timber

@OptIn(UnstableApi::class)
@Composable
fun NextVisualizer() {

    val context = LocalContext.current
    val visualizerEnabled by rememberPreference(visualizerEnabledKey, false)

    if (visualizerEnabled) {

        val permission = Manifest.permission.RECORD_AUDIO

        var relaunchPermission by remember {
            mutableStateOf(false)
        }

        var hasPermission by remember(isCompositionLaunched()) {
            mutableStateOf(context.applicationContext.hasPermission(permission))
        }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { hasPermission = it }
        )

        if (!hasPermission) {

            LaunchedEffect(Unit, relaunchPermission) { launcher.launch(permission) }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(
                    2.dp,
                    Alignment.CenterVertically
                ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BasicText(
                    text = stringResource(R.string.require_mic_permission),
                    modifier = Modifier.fillMaxWidth(0.75f),
                    style = typography().xs.semiBold
                )
                /*
                Spacer(modifier = Modifier.height(12.dp))
                SecondaryTextButton(
                    text = stringResource(R.string.grant_permission),
                    onClick = {
                        relaunchPermission = !relaunchPermission
                    }
                )
                 */
                Spacer(modifier = Modifier.height(20.dp))
                SecondaryTextButton(
                    text = stringResource(R.string.open_permission_settings),
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                setData(Uri.fromParts("package", context.packageName, null))
                            }
                        )
                    }
                )

            }

        } else {

            val binder = LocalPlayerServiceBinder.current
            val visualizerView = VisualizerView(context)
            val helper = VisualizerHelper(binder?.player?.audioSessionId ?: 0)

            val visualizersList = getVisualizers()
            var currentVisualizer by rememberPreference(currentVisualizerKey, 0)
            if (currentVisualizer < 0) currentVisualizer = 0

            Box(
                modifier = Modifier.fillMaxSize()
            ) {

                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                        /*
                        .border(
                            BorderStroke(
                                8.dp,
                                colorPalette().accent
                            )
                        ),
                         */
                    factory = {
                        visualizerView
                        /*
                        visualizerView.apply {
                            helper.let {
                                println("VisualizerView inside")
                                setup(helper, visualizersList[currentVisualizer])
                                setOnClickListener {
                                    if (current < list.lastIndex) current++ else current = 0
                                    //visualizerView.setup(helper, visualizersList[currentVisualizer])
                                }
                            }
                        }

                         */
                    },
                    update = {
                        it.setup(helper, visualizersList[currentVisualizer])
                    }

                )


                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .height(50.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (currentVisualizer <= visualizersList.lastIndex) currentVisualizer--
                                if (currentVisualizer < 0) currentVisualizer = visualizersList.lastIndex
                            },
                            icon = R.drawable.arrow_left,
                            color = colorPalette().text,
                            modifier = Modifier
                                .size(32.dp)
                        )
                        
                        BasicText(
                            text = "${currentVisualizer + 1}/${visualizersList.size}",
                            style = typography().xs.semiBold.copy(color = colorPalette().text),
                        )
                        
                        IconButton(
                            onClick = { if (currentVisualizer < visualizersList.lastIndex) currentVisualizer++ else currentVisualizer = 0 },
                            icon = R.drawable.arrow_right,
                            color = colorPalette().text,
                            modifier = Modifier
                                .size(32.dp)
                        )
                    }

            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun getVisualizers(): List<Painter> {

    val context = LocalContext.current
    val circleBitmap: Bitmap
    val ampR = 3f
    val yR = 0.2f
    val color = colorPalette().text.hashCode()
    var logoBitmapCover by remember { mutableStateOf(ContextCompat.getDrawable(context, R.drawable.app_logo)?.toBitmap()!!) }
    var bitmapCover by remember { mutableStateOf(logoBitmapCover) }
    val binder = LocalPlayerServiceBinder.current
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
            try {
                bitmapCover = getBitmapFromUrl(
                    context,
                    binder?.player?.currentWindow?.mediaItem?.mediaMetadata?.artworkUri.toString()
                        .resize(1200, 1200)
                )
            } catch (e: Exception) {
                Timber.e("Failed to get bitmap in NextVisualizer ${e.stackTraceToString()}")
            }
    }
    /*
    LaunchedEffect(Unit, binder?.player?.currentWindow?.mediaItem?.mediaId) {
        try {
            bitmapCover = getBitmapFromUrl(
                context,
                binder?.player?.currentWindow?.mediaItem?.mediaMetadata?.artworkUri.toString().resize(1200, 1200)
            )
        } catch (e: Exception) {
            Timber.e("Failed get bitmap in NextVisualizer ${e.stackTraceToString()}")
        }
    }
     */

    binder?.player?.DisposableListener {
        object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                coroutineScope.launch {
                    try {
                        bitmapCover = getBitmapFromUrl(
                            context,
                            binder.player.currentWindow?.mediaItem?.mediaMetadata?.artworkUri.toString()
                                .resize(1200, 1200)
                        )
                    } catch (e: Exception) {
                        bitmapCover = logoBitmapCover
                        Timber.e("Failed to get bitmap in NextVisualizer ${e.stackTraceToString()}")
                    }
                }
            }
        }
    }

    val background: Bitmap = bitmapCover //ContextCompat.getDrawable(context, R.drawable.app_logo)?.toBitmap()!!
    val bitmap: Bitmap = bitmapCover //ContextCompat.getDrawable(context, R.drawable.app_logo)?.toBitmap()!!
    circleBitmap = bitmap.let { Icon.getCircledBitmap(it) }
    return listOf(
        // Basic components
        Move(WfmAnalog(colorPaint = color, ampR = ampR)),
        Move(FftBar(colorPaint = color, ampR = ampR), yR = yR),
        Move(FftLine(colorPaint = color, ampR = ampR), yR = yR),
        Move(FftWave(ampR = ampR), yR = yR),
        Move(FftWaveRgb(ampR = ampR), yR = yR),
        Compose(
            Move(WfmAnalog(colorPaint = color), yR = -.3f),
            Move(FftBar(colorPaint = color), yR = -.1f),
            Move(FftLine(colorPaint = color), yR = .1f),
            Move(FftWave(), yR = .3f),
            Move(FftWaveRgb(), yR = .5f)
        ),
        Move(FftBar(colorPaint = color, side = "b", ampR = ampR), yR = -yR),
        Move(FftLine(colorPaint = color, side = "b", ampR = ampR), yR = -yR),
        Move(FftWave(side = "b", ampR = ampR), yR = -yR),
        Move(FftWaveRgb(side = "b", ampR = ampR), yR = -yR),
        Compose(
            Move(FftBar(colorPaint = color, side = "b"), yR = -.3f),
            Move(FftLine(colorPaint = color, side = "b"), yR = -.1f),
            Move(FftWave(side = "b"), yR = .1f),
            Move(FftWaveRgb(side = "b"), yR = .3f)
        ),
        Move(FftBar(colorPaint = color, side = "ab", ampR = ampR), yR = yR-0.1f),
        Move(FftLine(colorPaint = color, side = "ab", ampR = ampR), yR = yR-0.1f),
        Move(FftWave(side = "ab", ampR = ampR), yR = yR-0.1f),
        Move(FftWaveRgb(side = "ab", ampR = ampR), yR = yR-0.1f),
        Compose(
            Move(FftBar(colorPaint = color, side = "ab"), yR = -.3f),
            Move(FftLine(colorPaint = color, side = "ab"), yR = -.1f),
            Move(FftWave(side = "ab"), yR = .1f),
            Move(FftWaveRgb(side = "ab"), yR = .3f)
        ),
        // Basic components (Circle)
        Move(FftCLine(colorPaint = color, ampR = ampR)),
        FftCWave(colorPaint = color, ampR = ampR),
        Move(FftCWaveRgb(colorPaint = color, ampR = ampR)),
        Compose(
            Move(FftCLine(colorPaint = color, ampR = ampR)),
            FftCWave(colorPaint = color, ampR = ampR),
            Move(FftCWaveRgb(colorPaint = color, ampR = ampR))
        ),
        Move(FftCLine(colorPaint = color, side = "b", ampR = ampR)),
        FftCWave(side = "b", colorPaint = color, ampR = ampR),
        Move(FftCWaveRgb(side = "b",colorPaint = color, ampR = ampR)),
        Compose(
            Move(FftCLine(colorPaint = color, side = "b", ampR = ampR)),
            FftCWave(side = "b", colorPaint = color, ampR = ampR),
            Move(FftCWaveRgb(side = "b",colorPaint = color, ampR = ampR)),
        ),
        Move(FftCLine(colorPaint = color, side = "ab", ampR = ampR)),
        FftCWave(side = "ab", colorPaint = color, ampR = ampR),
        Move(FftCWaveRgb(side = "ab", colorPaint = color, ampR = ampR)),
        Compose(
            Move(FftCLine(colorPaint = color, side = "ab", ampR = ampR)),
            FftCWave(side = "ab", colorPaint = color, ampR = ampR),
            Move(FftCWaveRgb(side = "ab", colorPaint = color, ampR = ampR))
        ),
        //Blend
        Move(Blend(
            FftLine(colorPaint = color, ampR = ampR).apply {
                paint.strokeWidth = 8f;paint.strokeCap = Paint.Cap.ROUND
            },
            Gradient(preset = Gradient.LINEAR_HORIZONTAL)
        ), yR = yR),
        Move(Blend(
            FftLine(colorPaint = color, ampR = ampR).apply {
                paint.strokeWidth = 8f;paint.strokeCap = Paint.Cap.ROUND
            },
            Gradient(preset = Gradient.LINEAR_VERTICAL, hsv = true)
        ), yR = yR),
        Move(Blend(
            FftLine(colorPaint = color, ampR = ampR).apply {
                paint.strokeWidth = 8f;paint.strokeCap = Paint.Cap.ROUND
            },
            Gradient(preset = Gradient.LINEAR_VERTICAL_MIRROR, hsv = true)
        ), yR = yR),
        Move(Blend(
            FftLine(colorPaint = color, ampR = ampR).apply {
                paint.strokeWidth = 8f;paint.strokeCap = Paint.Cap.ROUND
            },
            Gradient(preset = Gradient.RADIAL)
        ), yR = yR),
        Move(Blend(
            FftCBar(colorPaint = color, side = "ab", gapX = 8f, ampR = ampR).apply {
                paint.style = Paint.Style.FILL
            },
            Gradient(preset = Gradient.SWEEP, hsv = true)
        )),
        // Composition
        Glitch(Beat(Preset.getPresetWithBitmap("cIcon", circleBitmap))),
        Compose(
            WfmAnalog(colorPaint = color, ampR = ampR).apply { paint.alpha = 150 },
            Shake(Preset.getPresetWithBitmap("cWaveRgbIcon", circleBitmap)).apply {
                animX.duration = 1000
                animY.duration = 2000
            }),
        Compose(
            Preset.getPresetWithBitmap("liveBg", background),
            FftCLine(colorPaint = color, ampR = ampR).apply {
                paint.strokeWidth = 8f;paint.strokeCap = Paint.Cap.ROUND
            }
        )
    )
    /*
    return listOf(
        // Basic components
        Compose(
            Move(WfmAnalog(ampR = 2f)),
            Move(FftBar(ampR = 2f), yR = -.1f),
            //Move(FftLine(), yR = .1f),
            //Move(FftWave(), yR = .3f),
            //Move(FftWaveRgb(), yR = .5f)
        ),
        Compose(
            Move(WfmAnalog(), yR = -.3f),
            Move(FftBar(), yR = -.1f),
            Move(FftLine(), yR = .1f),
            Move(FftWave(), yR = .3f),
            Move(FftWaveRgb(), yR = .5f)
        ),
        Compose(
            Move(FftBar(side = "b"), yR = -.3f),
            Move(FftLine(side = "b"), yR = -.1f),
            Move(FftWave(side = "b"), yR = .1f),
            Move(FftWaveRgb(side = "b"), yR = .3f)
        ),
        Compose(
            Move(FftBar(side = "ab"), yR = -.3f),
            Move(FftLine(side = "ab"), yR = -.1f),
            Move(FftWave(side = "ab"), yR = .1f),
            Move(FftWaveRgb(side = "ab"), yR = .3f)
        ),
        // Basic components (Circle)
        Compose(
            Move(FftCLine(), xR = -.3f),
            FftCWave(),
            Move(FftCWaveRgb(), xR = .3f)
        ),
        Compose(
            Move(FftCLine(side = "b"), xR = -.3f),
            FftCWave(side = "b"),
            Move(FftCWaveRgb(side = "b"), xR = .3f)
        ),
        Compose(
            Move(FftCLine(side = "ab"), xR = -.3f),
            FftCWave(side = "ab"),
            Move(FftCWaveRgb(side = "ab"), xR = .3f)
        ),
        //Blend
        Blend(
            FftLine().apply {
                paint.strokeWidth = 8f;paint.strokeCap = Paint.Cap.ROUND
            },
            Gradient(preset = Gradient.LINEAR_HORIZONTAL)
        ),
        Blend(
            FftLine().apply {
                paint.strokeWidth = 8f;paint.strokeCap = Paint.Cap.ROUND
            },
            Gradient(preset = Gradient.LINEAR_VERTICAL, hsv = true)
        ),
        Blend(
            FftLine().apply {
                paint.strokeWidth = 8f;paint.strokeCap = Paint.Cap.ROUND
            },
            Gradient(preset = Gradient.LINEAR_VERTICAL_MIRROR, hsv = true)
        ),
        Blend(
            FftCLine().apply {
                paint.strokeWidth = 8f;paint.strokeCap = Paint.Cap.ROUND
            },
            Gradient(preset = Gradient.RADIAL)
        ),
        Blend(
            FftCBar(side = "ab", gapX = 8f).apply {
                paint.style = Paint.Style.FILL
            },
            Gradient(preset = Gradient.SWEEP, hsv = true)
        ),
        // Composition
        Glitch(Beat(Preset.getPresetWithBitmap("cIcon", circleBitmap))),
        Compose(
            WfmAnalog().apply { paint.alpha = 150 },
            Shake(Preset.getPresetWithBitmap("cWaveRgbIcon", circleBitmap)).apply {
                animX.duration = 1000
                animY.duration = 2000
            }),
        Compose(
            Preset.getPresetWithBitmap("liveBg", background),
            FftCLine().apply {
                paint.strokeWidth = 8f;paint.strokeCap = Paint.Cap.ROUND
            }
        )
    )
     */
}