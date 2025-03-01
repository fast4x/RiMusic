package it.fast4x.rimusic.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerDefaults.windowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.fast4x.environment.Environment
import it.fast4x.environment.models.bodies.BrowseBodyWithLocale
import it.fast4x.environment.requests.BrowseResult
import it.fast4x.environment.requests.browse
import it.fast4x.rimusic.items.AlbumItem
import it.fast4x.rimusic.items.ArtistItem
import it.fast4x.rimusic.items.PlaylistItem
import it.fast4x.rimusic.styling.Dimensions.albumThumbnailSize
import it.fast4x.rimusic.styling.Dimensions.artistThumbnailSize
import it.fast4x.rimusic.styling.Dimensions.layoutColumnBottomSpacer
import it.fast4x.rimusic.styling.Dimensions.playlistThumbnailSize
import it.fast4x.rimusic.ui.components.Loader
import it.fast4x.rimusic.ui.components.Title

internal const val defaultBrowseId = "FEmusic_moods_and_genres_category"

@Composable
fun MoodScreen(
    mood: Environment.Mood.Item,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit
) {

    val browseId = mood.endpoint.browseId ?: defaultBrowseId

    println("mediaItem browseId: $browseId")

    var moodPage by remember { mutableStateOf<BrowseResult?>(null) }
    var moodPageResult by remember { mutableStateOf<Result<BrowseResult>?>(null) }
    LaunchedEffect(browseId) {
        moodPageResult = Environment.browse(BrowseBodyWithLocale(browseId = browseId, params = mood.endpoint.params))
    }

    moodPageResult?.getOrThrow().also { moodPage = it }

    val endPaddingValues = windowInsets.only(WindowInsetsSides.End).asPaddingValues()

    val sectionTextModifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 24.dp, bottom = 8.dp)

    val lazyListState = rememberLazyListState()

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxHeight()
                    //.fillMaxWidth(0.5f)
                    .fillMaxWidth()
                //.verticalScroll(leftScrollState)
            ) {
                moodPage?.let { moodResult ->
                    /*
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    //.background(colorPalette().background0)
                    .fillMaxSize()
            ) {
                item(
                    key = "header",
                    contentType = 0
                ) {

             */
                    //Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Title(
                        title = mood.title ?: "Moods",
                    )

                    //}
                    //}

                    moodResult.items.forEach { item ->
                        Title(
                            title = item.title,
                        )

                        LazyRow {
                            items(items = item.items, key = { it.key }) { childItem ->
                                if (childItem.key == defaultBrowseId) return@items
                                when (childItem) {
                                    is Environment.AlbumItem -> AlbumItem(
                                        album = childItem,
                                        thumbnailSizeDp = albumThumbnailSize,
                                        alternative = true,
                                        modifier = Modifier.clickable {
                                            childItem.info?.endpoint?.browseId?.let {
                                                onAlbumClick(it)
                                            }
                                        }
                                    )

                                    is Environment.ArtistItem -> ArtistItem(
                                        artist = childItem,
                                        thumbnailSizeDp = artistThumbnailSize,
                                        alternative = true,
                                        modifier = Modifier.clickable {
                                            childItem.info?.endpoint?.browseId?.let {
                                                onArtistClick(it)
                                            }
                                        }
                                    )

                                    is Environment.PlaylistItem -> PlaylistItem(
                                        playlist = childItem,
                                        thumbnailSizeDp = playlistThumbnailSize,
                                        alternative = true,
                                        modifier = Modifier.clickable {
                                            childItem.info?.endpoint?.browseId?.let {
                                                onPlaylistClick(it)
                                            }
                                        }
                                    )

                                    else -> {}
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(layoutColumnBottomSpacer))

                } ?: Loader()

            }
        }
    }

}