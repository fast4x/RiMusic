package it.fast4x.rimusic.utils

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import it.fast4x.environment.Environment
import it.fast4x.environment.models.bodies.ContinuationBody
import it.fast4x.environment.models.bodies.SearchBody
import it.fast4x.environment.requests.searchPage
import it.fast4x.environment.utils.from
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.SwipeablePlaylistItem
import it.fast4x.rimusic.ui.components.themed.NonQueuedMediaItemMenu
import it.fast4x.rimusic.ui.components.themed.Title
import it.fast4x.rimusic.ui.items.VideoItem
import it.fast4x.rimusic.ui.items.VideoItemPlaceholder
import it.fast4x.rimusic.ui.screens.searchresult.ItemsPage
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.ui.components.themed.SmartMessage

@ExperimentalAnimationApi
@ExperimentalTextApi
@ExperimentalFoundationApi
@UnstableApi
@Composable
fun SearchYoutubeEntity (
    navController: NavController,
    onDismiss: () -> Unit,
    query: String,
    filter: Environment.SearchFilter = Environment.SearchFilter.Video,
    disableScrollingText: Boolean
) {
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalContext.current
    //val context = LocalContext.current
    val thumbnailHeightDp = 72.dp
    val thumbnailWidthDp = 128.dp
    val emptyItemsText = stringResource(R.string.no_results_found)
    val headerContent: @Composable (textButton: (@Composable () -> Unit)?) -> Unit = {
        Title(
            title = stringResource(id = R.string.videos),
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
    val isVideoEnabled = LocalContext.current.preferences.getBoolean(showButtonPlayerVideoKey, false)

    Box(
        modifier = Modifier
            .background(colorPalette().background0)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp)
        ) {
            ItemsPage(
                tag = "searchYTEntity/$query/videos",
                itemsPageProvider = { continuation ->
                    if (continuation == null) {
                        Environment.searchPage(
                            body = SearchBody(
                                query = query,
                                params = Environment.SearchFilter.Video.value
                            ),
                            fromMusicShelfRendererContent = Environment.VideoItem::from
                        )
                    } else {
                        Environment.searchPage(
                            body = ContinuationBody(continuation = continuation),
                            fromMusicShelfRendererContent = Environment.VideoItem::from
                        )
                    }
                },
                emptyItemsText = emptyItemsText,
                headerContent = headerContent,
                itemContent = { video ->
                    SwipeablePlaylistItem(
                        mediaItem = video.asMediaItem,
                        onPlayNext = {
                            binder?.player?.addNext(video.asMediaItem)
                        },
                        onDownload = {
                            val message = context.resources.getString(R.string.downloading_videos_not_supported)

                            SmartMessage(
                                message,
                                durationLong = false,
                                context = context
                            )
                        },
                        onEnqueue = {
                            binder?.player?.enqueue(video.asMediaItem)
                        }
                    ) {
                        VideoItem(
                            video = video,
                            thumbnailWidthDp = thumbnailWidthDp,
                            thumbnailHeightDp = thumbnailHeightDp,
                            modifier = Modifier
                                .combinedClickable(
                                    onLongClick = {
                                        menuState.display {
                                            NonQueuedMediaItemMenu(
                                                navController = rememberNavController(),
                                                mediaItem = video.asMediaItem,
                                                onDismiss = menuState::hide,
                                                disableScrollingText = disableScrollingText
                                            )
                                        };
                                        hapticFeedback.performHapticFeedback(
                                            HapticFeedbackType.LongPress
                                        )
                                    },
                                    onClick = {
                                        binder?.stopRadio()
                                        if (isVideoEnabled)
                                            binder?.player?.playVideo(video.asMediaItem)
                                        else
                                            binder?.player?.forcePlay(video.asMediaItem)
                                        //binder?.setupRadio(video.info?.endpoint)
                                        onDismiss()
                                    }
                                ),
                            disableScrollingText = disableScrollingText
                        )
                    }
                },
                itemPlaceholderContent = {
                    VideoItemPlaceholder(
                        thumbnailHeightDp = thumbnailHeightDp,
                        thumbnailWidthDp = thumbnailWidthDp
                    )
                }
            )
        }
    }
}