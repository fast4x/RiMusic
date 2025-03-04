package it.fast4x.rimusic.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.ViewType
import it.fast4x.rimusic.getViewType
import it.fast4x.rimusic.ui.components.tab.toolbar.Descriptive
import it.fast4x.rimusic.ui.components.tab.toolbar.DynamicColor
import it.fast4x.rimusic.ui.components.tab.toolbar.MenuIcon

@Composable
fun viewTypeToolbutton(messageId: Int): MenuIcon = object : MenuIcon, DynamicColor, Descriptive {
    var viewType by rememberPreference(viewTypeKey, ViewType.Grid)
    override var isFirstColor: Boolean = true
    override var iconId: Int = if (viewType == ViewType.Grid) R.drawable.list_view else R.drawable.grid_view
    override val messageId: Int = messageId
    override val menuIconTitle: String
        @Composable
        get() = stringResource(messageId)

    override fun onShortClick() {
        viewType = when (viewType) {
            ViewType.Grid -> ViewType.List
            ViewType.List -> ViewType.Grid
        }
    }
}