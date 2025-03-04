package it.fast4x.rimusic.ui.components.themed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.fast4x.rimusic.utils.center
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.typography

@Composable
fun SliderControl(
    //title: String,
    //text: String,
    state: Float,
    range: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    onSlide: (Float) -> Unit = { },
    onSlideComplete: () -> Unit = { },
    toDisplay: @Composable (Float) -> String = { it.toString() },
    steps: Int = 0,
    isEnabled: Boolean = true,
    usePadding: Boolean = true,
    showValue: Boolean = true
) = Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier.height(36.dp)
) {

    Box(
        modifier = Modifier
            //.weight(1f)
            .fillMaxSize()
    ) {
        Slider(
            isEnabled = isEnabled,
            state = state,
            setState = onSlide,
            onSlideComplete = onSlideComplete,
            range = range,
            steps = steps,
            modifier = Modifier
                .height(36.dp)
                .alpha(if (isEnabled) 0.6f else 0.5f)
                .let { if (usePadding) it.padding(start = 12.dp) else it }
                .padding(vertical = 16.dp)
                .fillMaxWidth()
        )

        if (showValue)
            BasicText(
                text = toDisplay(state),
                style = TextStyle(
                    textAlign = TextAlign.Center,
                    color = typography().xs.semiBold.color.copy(alpha = if (isEnabled) 1.0f else 0.5f),
                    fontSize = typography().xs.semiBold.center.fontSize,
                    fontWeight = typography().xs.semiBold.center.fontWeight
                ),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 10.dp)
            )
    }

}