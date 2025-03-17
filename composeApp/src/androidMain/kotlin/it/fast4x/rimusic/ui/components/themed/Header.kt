package it.fast4x.rimusic.ui.components.themed


import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.shimmer
import it.fast4x.rimusic.utils.bold
import it.fast4x.rimusic.utils.conditional
import it.fast4x.rimusic.utils.medium
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.typography
import kotlin.random.Random

@Composable
fun Header(
    title: String,
    modifier: Modifier = Modifier,
    actionsContent: @Composable RowScope.() -> Unit = {},
    disableScrollingText: Boolean
) {
    Header(
        modifier = modifier,
        titleContent = {
            BasicText(
                text = title,
                style = typography().xxl.medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.conditional(!disableScrollingText) { basicMarquee(iterations = Int.MAX_VALUE) }
            )
        },
        actionsContent = actionsContent
    )
}

@Composable
fun Header(
    modifier: Modifier = Modifier,
    titleContent: @Composable () -> Unit,
    actionsContent: @Composable RowScope.() -> Unit,
) {
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
            //.padding(horizontal = 16.dp, vertical = 16.dp)
            //.height(Dimensions.mediumheaderHeight)
            .fillMaxWidth()
    ) {
        titleContent()

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd),
            content = actionsContent,
        )
    }
}

@Composable
fun HeaderPlaceholder(
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.CenterEnd,
        modifier = modifier
            .padding(horizontal = 16.dp)
            .height(Dimensions.headerHeight)
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(colorPalette().shimmer)
                .fillMaxWidth(remember { 0.25f + Random.nextFloat() * 0.5f })
        ) {
            BasicText(
                text = "",
                style = typography().xxl.medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/*
@SuppressLint("SuspiciousIndentation")
@Composable
fun HeaderWithIcon (
    title: String,
    modifier: Modifier,
    @DrawableRes iconId: Int,
    showIcon: Boolean = true,
    enabled: Boolean = true,
    onClick: () -> Unit
){
    Row (
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ){

            HalfHeader(
                title = title,
                modifier = Modifier
                    .fillMaxSize(0.9f)
            )

            if (showIcon)
            SecondaryButton(
                iconId = iconId,
                enabled = enabled,
                onClick = onClick,
            )



    }
}
 */

@SuppressLint("SuspiciousIndentation")
@Composable
fun HeaderWithIcon (
    title: String,
    modifier: Modifier,
    @DrawableRes iconId: Int,
    showIcon: Boolean = true,
    enabled: Boolean = true,
    onClick: () -> Unit,
    navController: NavController = rememberNavController()
){
    //val disableIconButtonOnTop by rememberPreference(disableIconButtonOnTopKey, false)
    Row (
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            //.requiredHeight(Dimensions.halfheaderHeight)
            .padding(all = 8.dp)

    ){
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (UiType.ViMusic.isCurrent() &&
                (NavigationBarPosition.Top.isCurrent() || NavigationBarPosition.Bottom.isCurrent())
            ) {
                SecondaryButton(
                    iconId = R.drawable.settings,
                    onClick = {
                        navController.navigate(NavRoutes.settings.name)
                    },
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }

            BasicText(
                text = title,
                style = TextStyle(
                    fontSize = typography().xxl.bold.fontSize,
                    fontWeight = typography().xxl.bold.fontWeight,
                    color = colorPalette().text,
                    textAlign = if (UiType.ViMusic.isNotCurrent()) TextAlign.Center else TextAlign.End

                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxSize(if (showIcon && UiType.ViMusic.isCurrent()) 0.9f else 1f)
            )

            if (showIcon && UiType.ViMusic.isCurrent() &&
                (NavigationBarPosition.Left.isCurrent() || NavigationBarPosition.Right.isCurrent())
            )
                SecondaryButton(
                    iconId = iconId,
                    enabled = enabled,
                    onClick = onClick,
                )
        }
    }
}

@Composable
fun HalfHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionsContent: @Composable RowScope.() -> Unit = {},
) {
    HalfHeader(
        modifier = modifier,
        titleContent = {
            BasicText(
                text = title,
                style = typography().xxl.medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        actionsContent = actionsContent
    )
}

@Composable
fun HalfHeader(
    modifier: Modifier = Modifier,
    titleContent: @Composable () -> Unit,
    actionsContent: @Composable RowScope.() -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(horizontal = 8.dp)
            .height(Dimensions.halfheaderHeight)
    ) {
        titleContent()

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .heightIn(min = 30.dp),
            content = actionsContent,
        )
    }
}

@Composable
fun HeaderInfo (
    title: String,
    iconId: Int,
    spacer: Dp = 5.dp
) {
    Image(
        painter = painterResource( iconId ),
        contentDescription = null,
        colorFilter = ColorFilter.tint(colorPalette().textSecondary),
        modifier = Modifier.size( 12.dp )
    )
    BasicText(
        text = title,
        style = TextStyle(
            color = colorPalette().textSecondary,
            fontStyle = typography().xxxs.semiBold.fontStyle,
            fontWeight = typography().xxxs.semiBold.fontWeight,
            fontSize = typography().xxxs.semiBold.fontSize
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding( start = 4.dp )
    )

    Spacer(
        modifier = Modifier.width( spacer )
    )
}