package it.fast4x.rimusic.ui.components.navigation.header

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.ui.styling.favoritesIcon
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.ui.components.themed.Button
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.getAudioQualityFormat
import it.fast4x.rimusic.isDebugModeEnabled
import it.fast4x.rimusic.isParentalControlEnabled
import it.fast4x.rimusic.typography
import it.fast4x.rimusic.utils.isAtLeastAndroid7
import org.dailyislam.android.utilities.getNetworkType

private fun appIconClickAction(
    navController: NavController,
    countToReveal: MutableIntState,
    context: Context
) {
    countToReveal.intValue++

    val message: String =
        when( countToReveal.intValue ) {
            10 -> {
                countToReveal.intValue = 0
                navController.navigate( NavRoutes.gamePacman.name )
                ""
            }
            3 -> "Do you like clicking? Then continue..."
            6 -> "Okay, you’re looking for something, keep..."
            9 -> "You are a number one, click and enjoy the surprise"
            else -> ""
        }
    if( message.isNotEmpty() )
        SmartMessage(
            message = message,
            durationLong = true,
            context = context
        )
}

private fun appIconLongClickAction(
    navController: NavController,
    context: Context
) {
    SmartMessage(
        "You are a number one, click and enjoy the surprise",
        durationLong = true,
        context = context
    )
    navController.navigate( NavRoutes.gameSnake.name )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppLogo(
    navController: NavController,
    context: Context
) {
    val countToReveal = remember { mutableIntStateOf(0) }
    val modifier = Modifier.combinedClickable(
        onClick = { appIconClickAction( navController, countToReveal, context ) },
        onLongClick = { appIconLongClickAction( navController, context ) }
    )

    Button(
        iconId = R.drawable.app_icon,
        color = colorPalette().favoritesIcon,
        padding = 0.dp,
        size = 36.dp,
        modifier = modifier
    ).Draw()
}

@Composable
private fun AppLogoText( navController: NavController ) {
    val iconTextClick: () -> Unit = {
        if ( NavRoutes.home.isNotHere( navController ) )
            navController.navigate(NavRoutes.home.name)
    }



    Button(
        iconId = R.drawable.app_logo_text,
        color = AppBar.contentColor(),
        padding = 0.dp,
        size = 36.dp,
        forceWidth = 100.dp,
        modifier = Modifier.clickable { iconTextClick() }
    ).Draw()
}


@Composable
fun AppTitle(
    navController: NavController,
    context: Context
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy( 5.dp ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppLogo( navController, context )
        Box {
            AppLogoText( navController )
            if (isAtLeastAndroid7) {
                val dataTypeIcon = when (getNetworkType(context)) {
                    "WIFI" -> R.drawable.datawifi
                    "CELLULAR" -> R.drawable.datamobile
                    else -> R.drawable.alert_circle_not_filled
                }
                Image(
                    painter = painterResource(dataTypeIcon),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorPalette().text),
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.TopEnd)
                )
            }
            Image(
                painter = painterResource(R.drawable.dot),
                contentDescription = null,
                colorFilter = ColorFilter.tint(getAudioQualityFormat().color),
                modifier = Modifier
                    .size(12.dp)
                    .align(Alignment.TopEnd)
                    .absoluteOffset(0.dp, (-10).dp)
            )

            if (isDebugModeEnabled())
                Image(
                    painter = painterResource(R.drawable.maintenance),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorPalette().red),
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.BottomEnd)
                )
        }

        if(isParentalControlEnabled())
            Button(
                iconId = R.drawable.shield_checkmark,
                color = AppBar.contentColor(),
                padding = 0.dp,
                size = 20.dp
            ).Draw()
    }

}