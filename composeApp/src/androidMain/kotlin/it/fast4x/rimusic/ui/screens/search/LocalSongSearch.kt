package it.fast4x.rimusic.ui.screens.search

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
import it.fast4x.compose.persist.persistList
import it.fast4x.environment.models.NavigationEndpoint
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.LocalPlayerAwareWindowInsets
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.enums.ThumbnailRoundness
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.service.isLocal
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import it.fast4x.rimusic.ui.components.themed.Header
import it.fast4x.rimusic.ui.components.themed.InHistoryMediaItemMenu
import it.fast4x.rimusic.ui.items.SongItem
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.px
import it.fast4x.rimusic.utils.align
import it.fast4x.rimusic.utils.asMediaItem
import it.fast4x.rimusic.utils.disableScrollingTextKey
import it.fast4x.rimusic.utils.forcePlay
import it.fast4x.rimusic.utils.getDownloadState
import it.fast4x.rimusic.utils.isDownloadedSong
import it.fast4x.rimusic.utils.isNowPlaying
import it.fast4x.rimusic.utils.manageDownload
import it.fast4x.rimusic.utils.medium
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.thumbnailRoundnessKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.typography

@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@UnstableApi
@Composable
fun LocalSongSearch(
    navController: NavController,
    textFieldValue: TextFieldValue,
    onTextFieldValueChanged: (TextFieldValue) -> Unit,
    decorationBox: @Composable (@Composable () -> Unit) -> Unit,
    onAction1: () -> Unit,
    onAction2: () -> Unit,
    onAction3: () -> Unit,
    onAction4: () -> Unit,
) {
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current

    var items by persistList<Song>("search/local/songs")

    LaunchedEffect(textFieldValue.text) {
        if (textFieldValue.text.length > 1) {
            Database.search("%${textFieldValue.text}%").collect { items = it }
        }
    }

    val thumbnailSizeDp = Dimensions.thumbnails.song
    val thumbnailSizePx = thumbnailSizeDp.px

    val lazyListState = rememberLazyListState()

    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }
    val context = LocalContext.current

    var thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )

    val disableScrollingText by rememberPreference(disableScrollingTextKey, false)

    val focusRequester = remember {
        FocusRequester()
    }

    //val navigationBarPosition by rememberPreference(navigationBarPositionKey, NavigationBarPosition.Bottom)
    //val contentWidth = context.preferences.getFloat(contentWidthKey,0.8f)
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
            contentPadding = LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
            modifier = Modifier
                .fillMaxSize()
        ) {
            item(
                key = "header",
                contentType = 0
            ) {
                /*
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    HeaderWithIcon(
                        title = "${stringResource(R.string.search)} ${stringResource(R.string.library)}",
                        iconId = R.drawable.library,
                        enabled = true,
                        showIcon = true,
                        modifier = Modifier
                            .padding(bottom = 8.dp),
                        onClick = {}
                    )

                }
                 */
                Header(
                    titleContent = {
                        BasicTextField(
                            value = textFieldValue,
                            onValueChange = onTextFieldValueChanged,
                            textStyle = typography().l.medium.align(TextAlign.Start),
                            singleLine = true,
                            maxLines = 1,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            cursorBrush = SolidColor(colorPalette().text),
                            decorationBox = decorationBox,
                            modifier = Modifier
                                .background(
                                    colorPalette().background1,
                                    shape = thumbnailRoundness.shape()
                                )
                                .padding(all = 4.dp)
                                .focusRequester(focusRequester)
                                .fillMaxWidth()
                        )
                    },
                    actionsContent = {
                        /*
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(horizontal = 40.dp)
                                .fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = onAction1,
                                icon = R.drawable.globe,
                                color = colorPalette().favoritesIcon,
                                modifier = Modifier
                                    .size(24.dp)
                            )
                            IconButton(
                                onClick = onAction2,
                                icon = R.drawable.library,
                                color = colorPalette().favoritesIcon,
                                modifier = Modifier
                                    .size(24.dp)
                            )
                            IconButton(
                                onClick = onAction3,
                                icon = R.drawable.link,
                                color = colorPalette().favoritesIcon,
                                modifier = Modifier
                                    .size(24.dp)
                            )
                            /*
                            IconButton(
                                onClick = onAction4,
                                icon = R.drawable.chevron_back,
                                color = colorPalette().favoritesIcon,
                                modifier = Modifier
                                    .size(24.dp)
                            )
                             */
                        }
                        /*
                        if (textFieldValue.text.isNotEmpty()) {
                            SecondaryTextButton(
                                text = stringResource(R.string.clear),
                                onClick = { onTextFieldValueChanged(TextFieldValue()) }
                            )
                        }
                         */

                         */
                    },
                    /*
                    modifier = Modifier
                        .drawBehind {

                            val strokeWidth = 1 * density
                            val y = size.height - strokeWidth / 2

                            drawLine(
                                color = colorPalette().textDisabled,
                                start = Offset(x = 0f, y = y/2),
                                end = Offset(x = size.maxDimension, y = y/2),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                     */
                )
            }

            items(
                items = items,
                key = Song::id,
            ) { song ->
                val isLocal by remember { derivedStateOf { song.asMediaItem.isLocal } }
                downloadState = getDownloadState(song.asMediaItem.mediaId)
                val isDownloaded = if (!isLocal) isDownloadedSong(song.asMediaItem.mediaId) else true
                SongItem(
                    song = song,
                    onDownloadClick = {
                        binder?.cache?.removeResource(song.asMediaItem.mediaId)
                        CoroutineScope(Dispatchers.IO).launch {
                            Database.deleteFormat( song.asMediaItem.mediaId )
                        }

                        if (!isLocal)
                        manageDownload(
                            context = context,
                            mediaItem = song.asMediaItem,
                            downloadState = isDownloaded
                        )
                    },
                    downloadState = downloadState,
                    thumbnailSizePx = thumbnailSizePx,
                    thumbnailSizeDp = thumbnailSizeDp,
                    modifier = Modifier
                        .combinedClickable(
                            onLongClick = {
                                menuState.display {
                                    InHistoryMediaItemMenu(
                                        navController = navController,
                                        song = song,
                                        onDismiss = menuState::hide,
                                        disableScrollingText = disableScrollingText
                                    )
                                }
                            },
                            onClick = {
                                val mediaItem = song.asMediaItem
                                binder?.stopRadio()
                                binder?.player?.forcePlay(mediaItem)
                                binder?.setupRadio(
                                    NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId)
                                )
                            }
                        )
                        .animateItem(),
                    disableScrollingText = disableScrollingText,
                    isNowPlaying = binder?.player?.isNowPlaying(song.id) ?: false
                )
            }
        }

        FloatingActionsContainerWithScrollToTop(lazyListState = lazyListState)
    }
    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
    }
}
