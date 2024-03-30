package it.fast4x.rimusic.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

fun Modifier.verticalFadingEdge() =
    graphicsLayer(alpha = 0.99f)
        .drawWithContent {
            drawContent()
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        Color.Black, Color.Black, Color.Black,
                        Color.Transparent
                    )
                ),
                blendMode = BlendMode.DstIn
            )
        }
