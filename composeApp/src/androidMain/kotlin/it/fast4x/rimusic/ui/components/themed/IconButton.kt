package it.fast4x.rimusic.ui.components.themed

import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun HeaderIconButton(
    onClick: () -> Unit,
    @DrawableRes icon: Int,
    color: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    indication: Indication? = null,
    iconSize: Dp? = 20.dp
) {
    IconButton(
        icon = icon,
        color = color,
        onClick = onClick,
        enabled = enabled,
        indication = indication,
        modifier = modifier
            .padding(all = 2.dp)
            .size(iconSize ?: 18.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IconButton(
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    @DrawableRes icon: Int,
    color: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    indication: Indication? = null
) {
    Image(
        painter = painterResource(icon),
        contentDescription = null,
        colorFilter = ColorFilter.tint(color),
        modifier = Modifier
            .combinedClickable(
                indication = indication ?: ripple(bounded = false),
                interactionSource = remember { MutableInteractionSource() },
                enabled = enabled,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .then(modifier)
    )
}

