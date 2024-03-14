package it.vfsfitvnm.vimusic.ui.components.themed

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.size.Dimension
import it.vfsfitvnm.vimusic.LocalPlayerAwareWindowInsets
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.NavigationBarType
import it.vfsfitvnm.vimusic.enums.UiType
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.bold
import it.vfsfitvnm.vimusic.utils.center
import it.vfsfitvnm.vimusic.utils.color
import it.vfsfitvnm.vimusic.utils.isLandscape
import it.vfsfitvnm.vimusic.utils.navigationBarTypeKey
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.semiBold

@Composable
inline fun NavigationRail(
    topIconButtonId: Int,
    noinline onTopIconButtonClick: () -> Unit,
    topIconButton2Id: Int,
    noinline onTopIconButton2Click: () -> Unit,
    showButton2: Boolean,
    bottomIconButtonId: Int? = R.drawable.search,
    noinline onBottomIconButtonClick: () -> Unit,
    showBottomButton: Boolean? = false,
    tabIndex: Int,
    crossinline onTabIndexChanged: (Int) -> Unit,
    content: @Composable ColumnScope.(@Composable (Int, String, Int) -> Unit) -> Unit,
    hideTabs: Boolean? = false,
    modifier: Modifier = Modifier
) {
    val (colorPalette, typography) = LocalAppearance.current

    val isLandscape = isLandscape

    val paddingValues = LocalPlayerAwareWindowInsets.current
        .only(WindowInsetsSides.Vertical + WindowInsetsSides.Start).asPaddingValues()

    val navigationBarType by rememberPreference(navigationBarTypeKey, NavigationBarType.IconAndText)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(paddingValues)
    ) {
        if (hideTabs == false)
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier
                    .size(
                        width = if (isLandscape) Dimensions.navigationRailWidthLandscape else Dimensions.navigationRailWidth,
                        height = if (showButton2) Dimensions.headerHeight else Dimensions.halfheaderHeight
                    )
            ) {
                Image(
                    painter = painterResource(topIconButtonId),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorPalette.textSecondary),
                    modifier = Modifier
                        .offset(
                            x = 0.dp, //if (isLandscape) 0.dp else Dimensions.navigationRailIconOffset,
                            y = 7.dp
                        )
                        .clip(CircleShape)
                        .clickable(onClick = onTopIconButtonClick)
                        .padding(all = 12.dp)
                        .size(24.dp)
                )
                if (showButton2) {
                    Image(
                        painter = painterResource(topIconButton2Id),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette.textSecondary),
                        modifier = Modifier
                            .offset(
                                x = 0.dp, //if (isLandscape) 0.dp else Dimensions.navigationRailIconOffset,
                                y = 70.dp
                            )
                            .clip(CircleShape)
                            .clickable(onClick = onTopIconButton2Click)
                            .padding(all = 12.dp)
                            .size(24.dp)
                    )
                }

            }

        if (hideTabs == false)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(if (isLandscape) Dimensions.navigationRailWidthLandscape else Dimensions.navigationRailWidth)
            ) {
                val transition = updateTransition(targetState = tabIndex, label = null)

                content { index, text, icon ->

                    val textColor by transition.animateColor(label = "") {
                        if (it == index) colorPalette.text else colorPalette.textDisabled
                    }
                    val dothAlpha by transition.animateFloat(label = "") {
                        if (it == index) 1f else 0f
                    }

                    val textContent: @Composable () -> Unit = {
                        if (navigationBarType == NavigationBarType.IconOnly) {
                            /*
                            BasicText(
                                text = "",
                                style = typography.xs.semiBold.center.color(textColor),
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                            )
                             */
                        } else {
                            BasicText(
                                text = text,
                                //style = typography.xs.semiBold.center.color(textColor),
                                style = TextStyle(
                                    fontSize = typography.xs.semiBold.fontSize,
                                    fontWeight = typography.xs.semiBold.fontWeight,
                                    color = colorPalette.text,
                                    //textAlign = if(uiType != UiType.ViMusic) TextAlign.Center else TextAlign.End

                                ),
                                modifier = Modifier
                                    .vertical(enabled = !isLandscape)
                                    .rotate(if (isLandscape) 0f else -90f)
                                    .padding(horizontal = 16.dp)
                            )
                        }
                    }

                    val iconContent: @Composable () -> Unit = {
                        if (navigationBarType == NavigationBarType.IconOnly) {
                            Image(
                                painter = painterResource(icon),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(textColor),
                                modifier = Modifier
                                    .padding(all = 12.dp)
                                    .size(24.dp)
                            )
                        } else {
                            Image(
                                painter = painterResource(icon),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorPalette.text),
                                modifier = Modifier
                                    .vertical(enabled = !isLandscape)
                                    .graphicsLayer {
                                        alpha = dothAlpha
                                        translationX = (1f - dothAlpha) * -48.dp.toPx()
                                        rotationZ = if (isLandscape) 0f else -90f
                                    }
                                    .size(Dimensions.navigationRailIconOffset * 2)
                            )
                        }
                    }

                    /*
                    val dothAlpha by transition.animateFloat(label = "") {
                        if (it == index) 1f else 0f
                    }

                    val textColor by transition.animateColor(label = "") {
                        if (it == index) colorPalette.text else colorPalette.textDisabled
                    }

                    val iconContent: @Composable () -> Unit = {
                        Image(
                            painter = painterResource(icon),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.text),
                            modifier = Modifier
                                .vertical(enabled = !isLandscape)
                                .graphicsLayer {
                                    alpha = dothAlpha
                                    //translationX = (1f - dothAlpha) * -48.dp.toPx()
                                    rotationZ = if (isLandscape) 0f else -90f
                                }
                                .size(Dimensions.navigationRailIconOffset * 2)
                        )
                    }

                    val textContent: @Composable () -> Unit = {
                        BasicText(
                            text = text,
                            style = typography.xs.semiBold.center.color(textColor),
                            modifier = Modifier
                                .vertical(enabled = !isLandscape)
                                .rotate(if (isLandscape) 0f else -90f)
                                .padding(horizontal = 16.dp)
                        )
                    }
                    */
                    val contentModifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .clickable(onClick = { onTabIndexChanged(index) })

                    if (isLandscape) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = contentModifier
                                .padding(vertical = 8.dp)
                        ) {
                            iconContent()
                            textContent()
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = contentModifier
                                .padding(horizontal = 8.dp)
                        ) {
                            iconContent()
                            textContent()
                        }
                    }
                }
            }

        if (showBottomButton == true)
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier
                    .size(
                        width = if (isLandscape) Dimensions.navigationRailWidthLandscape else Dimensions.navigationRailWidth,
                        height = Dimensions.halfheaderHeight
                    )
            ) {
                Image(
                    painter = painterResource(bottomIconButtonId ?: R.drawable.search ),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorPalette.textSecondary),
                    modifier = Modifier
                        .clickable(onClick = onBottomIconButtonClick )
                        .padding(all = 12.dp)
                        .size(24.dp)
                )
            }


    }
}

@Composable
inline fun NavigationRail3(
    topIconButtonId: Int,
    noinline onTopIconButtonClick: () -> Unit,
    topIconButton2Id: Int,
    noinline onTopIconButton2Click: () -> Unit,
    showButton2: Boolean,
    topIconButton3Id: Int,
    noinline onTopIconButton3Click: () -> Unit,
    showButton3: Boolean,
    tabIndex: Int,
    crossinline onTabIndexChanged: (Int) -> Unit,
    content: @Composable ColumnScope.(@Composable (Int, String, Int) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val (colorPalette, typography) = LocalAppearance.current

    val isLandscape = isLandscape

    val paddingValues = LocalPlayerAwareWindowInsets.current
        .only(WindowInsetsSides.Vertical + WindowInsetsSides.Start).asPaddingValues()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(paddingValues)
    ) {


        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .size(
                    width = if (isLandscape) Dimensions.navigationRailWidthLandscape else Dimensions.navigationRailWidth,
                    height = Dimensions.headerHeight3
                )
        ) {
            Image(
                painter = painterResource(topIconButtonId),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorPalette.textSecondary),
                modifier = Modifier
                    .offset(
                        x = if (isLandscape) 0.dp else Dimensions.navigationRailIconOffset,
                        y = 7.dp
                    )
                    .clip(CircleShape)
                    .clickable(onClick = onTopIconButtonClick)
                    .padding(all = 12.dp)
                    .size(22.dp)
            )
            if (showButton2) {
                Image(
                    painter = painterResource(topIconButton2Id),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorPalette.textSecondary),
                    modifier = Modifier
                        .offset(
                            x = if (isLandscape) 0.dp else Dimensions.navigationRailIconOffset,
                            y = 60.dp
                        )
                        .clip(CircleShape)
                        .clickable(onClick = onTopIconButton2Click)
                        .padding(all = 12.dp)
                        .size(22.dp)
                )
            }
            if (showButton3) {
                Image(
                    painter = painterResource(topIconButton3Id),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorPalette.textSecondary),
                    modifier = Modifier
                        .offset(
                            x = if (isLandscape) 0.dp else Dimensions.navigationRailIconOffset,
                            y = if (showButton2) 113.dp else 60.dp
                        )
                        .clip(CircleShape)
                        .clickable(onClick = onTopIconButton3Click)
                        .padding(all = 12.dp)
                        .size(22.dp)
                )
            }

        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(if (isLandscape) Dimensions.navigationRailWidthLandscape else Dimensions.navigationRailWidth)
        ) {
            val transition = updateTransition(targetState = tabIndex, label = null)

            content { index, text, icon ->
                val dothAlpha by transition.animateFloat(label = "") {
                    if (it == index) 1f else 0f
                }

                val textColor by transition.animateColor(label = "") {
                    if (it == index) colorPalette.text else colorPalette.textDisabled
                }

                val iconContent: @Composable () -> Unit = {
                    Image(
                        painter = painterResource(icon),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette.text),
                        modifier = Modifier
                            .vertical(enabled = !isLandscape)
                            .graphicsLayer {
                                alpha = dothAlpha
                                translationX = (1f - dothAlpha) * -48.dp.toPx()
                                rotationZ = if (isLandscape) 0f else -90f
                            }
                            .size(Dimensions.navigationRailIconOffset * 2)
                    )
                }

                val textContent: @Composable () -> Unit = {
                    BasicText(
                        text = text,
                        //style = typography.xs.semiBold.center.color(textColor),
                        style = TextStyle(
                            fontSize = typography.xs.semiBold.fontSize,
                            fontWeight = typography.xs.semiBold.fontWeight,
                            color = colorPalette.text,
                            //textAlign = if(uiType != UiType.ViMusic) TextAlign.Center else TextAlign.End

                        ),
                        modifier = Modifier
                            .vertical(enabled = !isLandscape)
                            .rotate(if (isLandscape) 0f else -90f)
                            .padding(horizontal = 16.dp)
                    )
                }

                val contentModifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .clickable(onClick = { onTabIndexChanged(index) })

                if (isLandscape) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = contentModifier
                            .padding(vertical = 8.dp)
                    ) {
                        iconContent()
                        textContent()
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = contentModifier
                            .padding(horizontal = 8.dp)
                    ) {
                        iconContent()
                        textContent()
                    }
                }
            }
        }
    }
}

fun Modifier.vertical(enabled: Boolean = true) =
    if (enabled)
        layout { measurable, constraints ->
            val placeable = measurable.measure(constraints.copy(maxWidth = Int.MAX_VALUE))
            layout(placeable.height, placeable.width) {
                placeable.place(
                    x = -(placeable.width / 2 - placeable.height / 2),
                    y = -(placeable.height / 2 - placeable.width / 2)
                )
            }
        } else this
