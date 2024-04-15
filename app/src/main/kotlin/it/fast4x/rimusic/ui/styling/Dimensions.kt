package it.fast4x.rimusic.ui.styling

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Suppress("ClassName")
object Dimensions {
    val itemsVerticalPadding = 8.dp

    val navigationRailWidth = 50.dp
    val navigationRailWidthLandscape = 128.dp
    val navigationRailIconOffset = 6.dp
    val headerHeight = 140.dp
    val headerHeight3 = 160.dp
    val halfheaderHeight = 60.dp
    val mediumheaderHeight = 80.dp
    val collapsedPlayer = 64.dp
    val navigationBarHeight = 74.dp
    val contentWidthRightBar = 0.88f
    val additionalVerticalSpaceForFloatingAction = 85.dp


    object thumbnails {
        val album = 128.dp
        val artist = 128.dp
        val song = 54.dp
        val playlist = album

        object player {
            val song: Dp
                @Composable
                get() = with(LocalConfiguration.current) {
                    minOf(screenHeightDp, screenWidthDp)
                }.dp
        }
    }

}

inline val Dp.px: Int
    @Composable
    inline get() = with(LocalDensity.current) { roundToPx() }
