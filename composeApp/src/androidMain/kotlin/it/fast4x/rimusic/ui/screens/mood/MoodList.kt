package it.fast4x.rimusic.ui.screens.mood

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import it.fast4x.environment.models.bodies.BrowseBodyWithLocale
import it.fast4x.environment.requests.BrowseResult
import it.fast4x.environment.requests.browse
import it.fast4x.rimusic.LocalPlayerAwareWindowInsets
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.models.Mood
import it.fast4x.rimusic.ui.components.ShimmerHost
import it.fast4x.rimusic.ui.components.themed.HeaderPlaceholder
import it.fast4x.rimusic.ui.components.themed.HeaderWithIcon
import it.fast4x.rimusic.ui.components.themed.TextPlaceholder
import it.fast4x.rimusic.ui.items.AlbumItem
import it.fast4x.rimusic.ui.items.AlbumItemPlaceholder
import it.fast4x.rimusic.ui.items.ArtistItem
import it.fast4x.rimusic.ui.items.PlaylistItem
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.px
import it.fast4x.rimusic.utils.center
import it.fast4x.rimusic.utils.disableScrollingTextKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.secondary
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.typography

internal const val defaultBrowseId = "FEmusic_moods_and_genres_category"

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun MoodList(
    navController: NavController,
    mood: Mood
) {
    val windowInsets = LocalPlayerAwareWindowInsets.current

    val browseId = mood.browseId ?: defaultBrowseId
    var moodPage by persist<Result<BrowseResult>>("playlist/$browseId${mood.params?.let { "/$it" } ?: ""}")

    LaunchedEffect(Unit) {
        moodPage = Environment.browse(BrowseBodyWithLocale(browseId = browseId, params = mood.params))
    }

    val thumbnailSizeDp = Dimensions.thumbnails.album
    val thumbnailSizePx = thumbnailSizeDp.px

    val lazyListState = rememberLazyListState()

    val endPaddingValues = windowInsets.only(WindowInsetsSides.End).asPaddingValues()

    val sectionTextModifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 24.dp, bottom = 8.dp)
        .padding(endPaddingValues)

    val disableScrollingText by rememberPreference(disableScrollingTextKey, false)

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
        moodPage?.getOrNull()?.let { moodResult ->
            LazyColumn(
                state = lazyListState,
                //contentPadding = LocalPlayerAwareWindowInsets.current
                //    .only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
                modifier = Modifier
                    .background(colorPalette().background0)
                    .fillMaxSize()
            ) {
                item(
                    key = "header",
                    contentType = 0
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        HeaderWithIcon(
                            title = mood.name,
                            iconId = R.drawable.globe,
                            enabled = true,
                            showIcon = true,
                            modifier = Modifier,
                            onClick = {}
                        )
                    }
                }

                moodResult.items.forEach { item ->
                    item {
                        BasicText(
                            text = item.title,
                            style = typography().m.semiBold,
                            modifier = sectionTextModifier
                        )
                    }
                    item {
                        LazyRow {
                            items(items = item.items, key = { it.key }) { childItem ->
                                if (childItem.key == defaultBrowseId) return@items
                                when (childItem) {
                                    is Environment.AlbumItem -> AlbumItem(
                                        album = childItem,
                                        thumbnailSizePx = thumbnailSizePx,
                                        thumbnailSizeDp = thumbnailSizeDp,
                                        alternative = true,
                                        modifier = Modifier.clickable {
                                            childItem.info?.endpoint?.browseId?.let {
                                                //albumRoute.global(it)
                                                navController.navigate(route = "${NavRoutes.album.name}/$it")
                                            }
                                        },
                                        disableScrollingText = disableScrollingText
                                    )

                                    is Environment.ArtistItem -> ArtistItem(
                                        artist = childItem,
                                        thumbnailSizePx = thumbnailSizePx,
                                        thumbnailSizeDp = thumbnailSizeDp,
                                        alternative = true,
                                        modifier = Modifier.clickable {
                                            childItem.info?.endpoint?.browseId?.let {
                                                navController.navigate(route = "${NavRoutes.artist.name}/$it")
                                            }
                                        },
                                        disableScrollingText = disableScrollingText
                                    )

                                    is Environment.PlaylistItem -> PlaylistItem(
                                        playlist = childItem,
                                        thumbnailSizePx = thumbnailSizePx,
                                        thumbnailSizeDp = thumbnailSizeDp,
                                        alternative = true,
                                        modifier = Modifier.clickable {
                                            childItem.info?.endpoint?.let { endpoint ->
                                                /*
                                                playlistRoute.global(
                                                    p0 = endpoint.browseId,
                                                    p1 = endpoint.params,
                                                    p2 = childItem.songCount?.let { it / 100 }
                                                )
                                                 */
                                                navController.navigate(route = "${NavRoutes.playlist.name}/${endpoint.browseId}")
                                            }
                                            /*
                                            childItem.info?.endpoint?.browseId?.let {
                                                playlistRoute.global(
                                                    it,
                                                    null

                                                )
                                            }
                                             */
                                        },
                                        disableScrollingText = disableScrollingText
                                    )

                                    else -> {}
                                }
                            }
                        }
                    }
                }

                item(key = "bottom") {
                    Spacer(modifier = Modifier.height(Dimensions.bottomSpacer))
                }

            }
        } ?: moodPage?.exceptionOrNull()?.let {
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
