package it.fast4x.rimusic.ui.screens.mood

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.valentinilk.shimmer.shimmer
import it.fast4x.compose.persist.persist
import it.fast4x.environment.Environment
import it.fast4x.environment.requests.discoverPage
import it.fast4x.rimusic.LocalPlayerAwareWindowInsets
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.models.toUiMood
import it.fast4x.rimusic.ui.components.ShimmerHost
import it.fast4x.rimusic.ui.components.themed.HeaderPlaceholder
import it.fast4x.rimusic.ui.components.themed.HeaderWithIcon
import it.fast4x.rimusic.ui.components.themed.TextPlaceholder
import it.fast4x.rimusic.ui.items.AlbumItemPlaceholder
import it.fast4x.rimusic.ui.screens.home.MoodGridItemColored
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.utils.center
import it.fast4x.rimusic.utils.secondary
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.typography

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun MoodsPage(
    navController: NavController
) {
    val windowInsets = LocalPlayerAwareWindowInsets.current

    var discoverPage by persist<Result<Environment.DiscoverPage>>("home/discoveryMoods")
    LaunchedEffect(Unit) {
        discoverPage = Environment.discoverPage()
    }
    val thumbnailSizeDp = Dimensions.thumbnails.album + 24.dp

    val moodAngGenresLazyGridState = rememberLazyGridState()

    val endPaddingValues = windowInsets.only(WindowInsetsSides.End).asPaddingValues()

    val sectionTextModifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 24.dp, bottom = 8.dp)
        .padding(endPaddingValues)

    Column (
        modifier = Modifier
            .background(colorPalette().background0)
            //.fillMaxSize()
            .fillMaxHeight()
            .fillMaxWidth(
                if( NavigationBarPosition.Right.isCurrent() )
                    Dimensions.contentWidthRightBar
                else
                    1f
            )
    ) {
        discoverPage?.getOrNull()?.let { moodResult ->
            LazyVerticalGrid(
                state = moodAngGenresLazyGridState,
                columns = GridCells.Adaptive(Dimensions.thumbnails.album + 24.dp),
                modifier = Modifier
                    .background(colorPalette().background0)
                    .fillMaxSize()
            ) {
                item(
                    key = "header",
                    contentType = 0,
                    span = { GridItemSpan(maxLineSpan) }
                ) {
                    HeaderWithIcon(
                        title = stringResource(R.string.moods_and_genres),
                        iconId = R.drawable.search,
                        enabled = true,
                        showIcon = false,
                        modifier = Modifier,
                        onClick = {}
                    )
                }

                discoverPage?.getOrNull()?.let { page ->
                    if (page.moods.isNotEmpty()) {

                            items(
                                items = page.moods.sortedBy { it.title },
                                key = { it.endpoint.params ?: it.title }
                            ) {
                                MoodGridItemColored(
                                    mood = it,
                                    onClick = { it.endpoint.browseId?.let { _ ->
                                        navController.currentBackStackEntry?.savedStateHandle?.set("mood", it.toUiMood())
                                        navController.navigate(NavRoutes.mood.name)
                                    } },
                                    thumbnailSizeDp = thumbnailSizeDp,
                                    modifier = Modifier
                                        .animateItem()

                                )
                            }
                        }

                    }

                item(key = "bottom") {
                    Spacer(modifier = Modifier.height(Dimensions.bottomSpacer))
                }

                }


        } ?: discoverPage?.exceptionOrNull()?.let {
            BasicText(
                text = stringResource(R.string.page_not_been_loaded),
                style = typography().s.secondary.center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(all = 16.dp)
            )
        } ?: ShimmerHost {
            HeaderPlaceholder(modifier = Modifier.shimmer())
            repeat(4) {
                TextPlaceholder(modifier = sectionTextModifier)
                Row {
                    repeat(6) {
                        AlbumItemPlaceholder(
                            thumbnailSizeDp = thumbnailSizeDp,
                            alternative = true
                        )
                    }
                }
            }
        }
    }
}
