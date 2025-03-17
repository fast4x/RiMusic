package it.fast4x.rimusic.ui.components.tab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.utils.bold
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.typography

@Composable
private fun Title( titleId: Int ) {
    val fontStyle = TabHeader.style()
    val sPadding: Dp
    val ePadding: Dp
    val alignment: TextAlign

    when( UiType.current() ) {
        UiType.RiMusic -> {
            sPadding = Dp.Hairline
            ePadding = 12.dp
            alignment = TextAlign.Start
        }
        UiType.ViMusic -> {
            sPadding = 12.dp
            ePadding = Dp.Hairline
            alignment = TextAlign.End
        }
    }

    Text(
        text = stringResource( titleId ),
        style = TextStyle(
            fontSize = fontStyle.fontSize,
            fontWeight = fontStyle.fontWeight,
            color = colorPalette().text,
            textAlign = alignment
        ),
        modifier = Modifier.padding( start = sPadding, end = ePadding )
    )
}

@Composable
private fun ViMusicHeader( titleId: Int, additionalContent: @Composable () -> Unit ) {
    additionalContent()
    Title( titleId )
}

@Composable
private fun RiMusicHeader( titleId: Int, additionalContent: @Composable () -> Unit ) {
    Title( titleId )
    additionalContent()
}

interface TabHeader {

    companion object {

        @Composable
        fun style(): TextStyle =
            when( UiType.current() ) {
                UiType.RiMusic -> typography().xl.bold
                UiType.ViMusic -> typography().xxl.bold
            }

        @Composable
        fun height(): Dp = style().fontSize.value.dp + 14.dp
    }
}

/**
 * Display tab's header depends on current theme
 * For instance:
 *  - RiMusic theme has header aligned to the left
 *  - ViMusic theme has header aligned to the right
 *  and slightly bigger than RiMusic
 */
@Composable
fun TabHeader(
    titleId: Int,
    additionalContent: @Composable () -> Unit
) {
    // Align left if RiMusic, right if ViMusic
    val arrangement =
        if( UiType.ViMusic.isCurrent() )
            Arrangement.End
        else
            Arrangement.Start

    Row(
        horizontalArrangement = arrangement,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(start = 24.dp, end = 12.dp)
            //.padding(horizontal = 12.dp)
            .padding(top = 10.dp, bottom = 4.dp)
            .fillMaxWidth()
    ) {
        when( UiType.current() ) {
            UiType.RiMusic -> RiMusicHeader( titleId, additionalContent )
            UiType.ViMusic -> ViMusicHeader( titleId, additionalContent )
        }
    }
}