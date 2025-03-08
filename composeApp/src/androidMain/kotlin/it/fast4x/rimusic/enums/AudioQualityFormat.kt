package it.fast4x.rimusic.enums

import androidx.compose.ui.graphics.Color
import it.fast4x.rimusic.R

enum class AudioQualityFormat {
    Auto,
    High,
    Medium,
    Low;

    val icon: Int
        get() = when (this) {
            Auto -> R.drawable.up_right_arrow
            High -> R.drawable.arrow_up
            Medium -> R.drawable.arrow_right
            Low -> R.drawable.arrow_down
        }

    val color: Color
        get() = when (this) {
            Auto -> Color.White
            High -> Color.Green
            Medium -> Color.Yellow
            Low -> Color.Red
        }
}