package it.vfsfitvnm.vimusic.ui.components.themed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.medium


@Composable
fun DropDownSection(content: @Composable ColumnScope.() -> Unit) {
    val (colorPalette) = LocalAppearance.current
    Column(
        modifier = Modifier
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .background(colorPalette.elevatedBackground)
            .width(IntrinsicSize.Max),
        content = content
    )
}

@Composable
fun DropDownSectionSpacer() {
    Spacer(
        modifier = Modifier
            .height(4.dp)
    )
}

@Composable
fun DropDownTextItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val (colorPalette) = LocalAppearance.current

    DropDownTextItem(
        text = text,
        textColor = if (isSelected) {
            colorPalette.onPrimaryContainer
        } else {
            colorPalette.textSecondary
        },
        backgroundColor = if (isSelected) {
            colorPalette.primaryContainer
        } else {
            colorPalette.elevatedBackground
        },
        onClick = onClick
    )
}

@Composable
fun DropDownTextItem(
    text: String,
    backgroundColor: Color? = null,
    textColor: Color? = null,
    onClick: () -> Unit
) {
    val (colorPalette, typography) = LocalAppearance.current

    BasicText(
        text = text,
        style = typography.xxs.medium.copy(
            color = textColor ?: colorPalette.text,
            letterSpacing = 1.sp
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                indication = rememberRipple(bounded = true),
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .background(backgroundColor ?: colorPalette.elevatedBackground)
            .fillMaxWidth()
            .widthIn(min = 124.dp, max = 248.dp)
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            )
    )
}