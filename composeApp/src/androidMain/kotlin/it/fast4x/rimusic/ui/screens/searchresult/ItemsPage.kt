package it.fast4x.rimusic.ui.screens.searchresult

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.fast4x.compose.persist.persist
import it.fast4x.environment.Environment
import it.fast4x.environment.utils.plus
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.ui.components.ShimmerHost
import it.fast4x.rimusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.utils.center
import it.fast4x.rimusic.utils.secondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.typography

@ExperimentalAnimationApi
@Composable
inline fun <T : Environment.Item> ItemsPage(
    tag: String,
    crossinline headerContent: @Composable (textButton: (@Composable () -> Unit)?) -> Unit,
    crossinline itemContent: @Composable LazyItemScope.(T) -> Unit,
    noinline itemPlaceholderContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    initialPlaceholderCount: Int = 8,
    continuationPlaceholderCount: Int = 3,
    emptyItemsText: String = "No items found",
    noinline itemsPageProvider: (suspend (String?) -> Result<Environment.ItemsPage<T>?>?)? = null,
) {
    val updatedItemsPageProvider by rememberUpdatedState(itemsPageProvider)

    val lazyListState = rememberLazyListState()

    var itemsPage by persist<Environment.ItemsPage<T>?>(tag)

    LaunchedEffect(lazyListState, updatedItemsPageProvider) {
        val currentItemsPageProvider = updatedItemsPageProvider ?: return@LaunchedEffect

        snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo.any { it.key == "loading" } }
            .collect { shouldLoadMore ->
                if (!shouldLoadMore) return@collect

                withContext(Dispatchers.IO) {
                    currentItemsPageProvider(itemsPage?.continuation)
                }?.onSuccess {
                    if (it == null) {
                        if (itemsPage == null) {
                            itemsPage = Environment.ItemsPage(null, null)
                        }
                    } else {
                        itemsPage += it
                    }
                }?.exceptionOrNull()?.printStackTrace()
            }
    }

    Box(
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
        LazyColumn(
            state = lazyListState,
            //contentPadding = LocalPlayerAwareWindowInsets.current
            //    .only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
            modifier = modifier
                .fillMaxSize()
        ) {
            item(
                key = "header",
                contentType = "header",
            ) {
                headerContent(null)
            }

            items(
                items = itemsPage?.items ?: emptyList(),
                key = Environment.Item::key,
                itemContent = itemContent
            )

            if (itemsPage != null && itemsPage?.items.isNullOrEmpty()) {
                item(key = "empty") {
                    BasicText(
                        text = emptyItemsText,
                        style = typography().xs.secondary.center,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 32.dp)
                            .fillMaxWidth()
                    )
                }
            }

            if (!(itemsPage != null && itemsPage?.continuation == null)) {
                item(key = "loading") {
                    val isFirstLoad = itemsPage?.items.isNullOrEmpty()
                    ShimmerHost(
                        modifier = Modifier
                            .run {
                                if (isFirstLoad) fillParentMaxSize() else this
                            }
                    ) {
                        repeat(if (isFirstLoad) initialPlaceholderCount else continuationPlaceholderCount) {
                            itemPlaceholderContent()
                        }
                    }
                }
            }

            item(
                key = "footer",
                contentType = 0,
            ) {
                Spacer(modifier = Modifier.height(Dimensions.bottomSpacer))
            }
        }

        FloatingActionsContainerWithScrollToTop(lazyListState = lazyListState)


    }
}
