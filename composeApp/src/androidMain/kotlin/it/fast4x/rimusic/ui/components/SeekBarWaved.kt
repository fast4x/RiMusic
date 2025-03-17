package it.fast4x.rimusic.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun SeekBarWaved(
    position: () -> Float,
    onSeek: (updated: Float) -> Unit,
    modifier: Modifier = Modifier,
    onSeekStarted: (updated: Float) -> Unit = {},
    onSeekFinished: () -> Unit = {},
    color: Color,
    backgroundColor: Color = Color.Transparent,
    range: ClosedRange<Float> = 0f..100f,
    isActive: Boolean = true,
    scrubberRadius: Dp = 6.dp,
    shape: Shape = RectangleShape,
) {
    val minimumValue = range.start
    val maximumValue = range.endInclusive
    val isDragging = remember {
        MutableTransitionState(false)
    }

    val transition = rememberTransition(transitionState = isDragging, label = null)

    val currentAmplitude by transition.animateDp(label = "") { if (it || !isActive) 0.dp else 2.dp }
    val currentScrubberHeight by transition.animateDp(label = "") {
        //if (it) 10.dp else 5.dp
        if (it) 20.dp else 15.dp
    }

    Box(modifier = modifier
        .pointerInput(minimumValue, maximumValue) {
            if (maximumValue < minimumValue) return@pointerInput

            detectDrags(isDragging, maximumValue, minimumValue, onSeek, onSeekFinished)
        }
        .pointerInput(minimumValue, maximumValue) {
            detectTaps(maximumValue, minimumValue, onSeekStarted, onSeekFinished)
        }
        .padding(horizontal = scrubberRadius)
        .drawWithContent {
            drawContent()
            drawScrubber(range, position(), color, currentScrubberHeight)
        }
    ) {
        SeekBarContent(
            backgroundColor,
            amplitude = { currentAmplitude },
            position(),
            minimumValue,
            maximumValue,
            shape,
            color
        )
    }
}

private suspend fun PointerInputScope.detectDrags(
    isDragging: MutableTransitionState<Boolean>,
    maximumValue: Float,
    minimumValue: Float,
    onSeek: (delta: Float) -> Unit,
    onSeekFinished: () -> Unit
) {
    var acc = 0f

    detectHorizontalDragGestures(onDragStart = {
        isDragging.targetState = true
    }, onHorizontalDrag = { _, delta ->
        acc += delta / size.width * (maximumValue - minimumValue)

        if (acc !in -1f..1f) {
            onSeek(acc)
            acc -= acc
        }
    }, onDragEnd = {
        isDragging.targetState = false
        acc = 0f
        onSeekFinished()
    }, onDragCancel = {
        isDragging.targetState = false
        acc = 0f

        onSeekFinished()
    })
}

private suspend fun PointerInputScope.detectTaps(
    maximumValue: Float,
    minimumValue: Float,
    onSeekStarted: (updated: Float) -> Unit,
    onSeekFinished: () -> Unit
) {
    if (maximumValue < minimumValue) return

    detectTapGestures(onPress = { offset ->
        val updatedOffset = (offset.x / size.width * (maximumValue - minimumValue) + minimumValue)
        onSeekStarted(updatedOffset)
    }, onTap = {
        onSeekFinished()
    })
}

private fun ContentDrawScope.drawScrubber(
    range: ClosedRange<Float>, position: Float, color: Color, height: Dp
) {
    val minimumValue = range.start
    val maximumValue = range.endInclusive
    val scrubberPosition = if (maximumValue < minimumValue) {
        0f
    } else {
        (position - minimumValue) / (maximumValue - minimumValue) * size.width
    }

    drawRoundRect(
        color, topLeft = Offset(scrubberPosition - 5f, (size.height - height.toPx()) / 2),
        size = Size(10f, height.toPx()),
        cornerRadius = CornerRadius(5f)
    )
}


@Composable
private fun SeekBarContent(
    backgroundColor: Color,
    amplitude: () -> Dp,
    position: Float,
    minimumValue: Float,
    maximumValue: Float,
    shape: Shape,
    color: Color
) {
    val fraction = (position - minimumValue) / (maximumValue - minimumValue)
    val progress by rememberInfiniteTransition().animateFloat(
        0f,
        1f,
        animationSpec = infiniteRepeatable(
            tween(2000, easing = LinearEasing)
        ), label = ""
    )
    Box(
        Modifier
            .fillMaxWidth()
            .height(6.dp)) {
        Spacer(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(1f - fraction)
                .background(color = backgroundColor, shape = shape)
                .align(Alignment.CenterEnd)
        )

        Canvas(
            Modifier
                .fillMaxWidth(fraction)
                .height(amplitude())
                .align(Alignment.CenterStart)) {
            drawPath(
                wavePath(size.copy(height = size.height * 2), progress),
                color,
                //style = Stroke(width = 15f)
                //style = Stroke(width = 10f)
                style = Stroke(width = 5f)
            )
        }
    }
}

private fun wavePath(size: Size, progress: Float): Path {
    fun yFromX(x: Float) = (sin(x / 15f + progress * 2 * PI.toFloat()) + 1) * size.height / 2
    return Path().apply {
        moveTo(0f, yFromX(0f))
        var currentX = 0f
        while (currentX < size.width) {
            lineTo(currentX, yFromX(currentX))
            lineTo(currentX-5, yFromX(currentX)-5)
           // lineTo(yFromX(currentX),0f)
            //lineTo(0f, yFromX(currentX)-50f)
            currentX += 1
        }
    }
}
private fun wavePath2(size: Size, progress: Float): Path {
    fun yFromX(x: Float) = (x / 150f + progress * 2 * PI.toFloat() + 1) * size.height / 2
    return Path().apply {
        moveTo(0f, yFromX(0f))
        var currentX = 0f
        while (currentX < size.width) {
            lineTo(currentX, yFromX(currentX))
            //lineTo(currentX, yFromX(currentX * PI.toFloat()))
            //lineTo(currentX, yFromX(currentX * PI.toFloat() * 2))
            currentX += 1
        }
    }
}
