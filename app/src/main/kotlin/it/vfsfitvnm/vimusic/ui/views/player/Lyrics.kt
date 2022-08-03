package it.vfsfitvnm.vimusic.ui.views.player

import android.app.SearchManager
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaMetadata
import com.valentinilk.shimmer.shimmer
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.themed.Menu
import it.vfsfitvnm.vimusic.ui.components.themed.MenuEntry
import it.vfsfitvnm.vimusic.ui.components.themed.TextFieldDialog
import it.vfsfitvnm.vimusic.ui.components.themed.TextPlaceholder
import it.vfsfitvnm.vimusic.ui.styling.BlackColorPalette
import it.vfsfitvnm.vimusic.ui.styling.DarkColorPalette
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.center
import it.vfsfitvnm.vimusic.utils.color
import it.vfsfitvnm.vimusic.utils.medium
import it.vfsfitvnm.vimusic.utils.verticalFadingEdge
import it.vfsfitvnm.youtubemusic.YouTube
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Composable
fun Lyrics(
    mediaId: String,
    isDisplayed: Boolean,
    onDismiss: () -> Unit,
    size: Dp,
    mediaMetadataProvider: () -> MediaMetadata,
    onLyricsUpdate: (String, String) -> Unit,
    nestedScrollConnectionProvider: () -> NestedScrollConnection,
    modifier: Modifier = Modifier
) {
    val (_, typography) = LocalAppearance.current
    val context = LocalContext.current

    AnimatedVisibility(
        visible = isDisplayed,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        var isLoading by remember(mediaId) {
            mutableStateOf(false)
        }

        var isEditingLyrics by remember(mediaId) {
            mutableStateOf(false)
        }

        val lyrics by remember(mediaId) {
            Database.lyrics(mediaId).distinctUntilChanged().map flowMap@{ lyrics ->
                if (lyrics != null) return@flowMap lyrics

                isLoading = true

                YouTube.next(mediaId, null)?.map { nextResult ->
                    nextResult.lyrics?.text()?.map { newLyrics ->
                        onLyricsUpdate(mediaId, newLyrics ?: "")
                        isLoading = false
                        return@flowMap newLyrics ?: ""
                    }
                }

                isLoading = false
                null
            }.distinctUntilChanged()
        }.collectAsState(initial = ".", context = Dispatchers.IO)

        if (isEditingLyrics) {
            TextFieldDialog(
                hintText = "Enter the lyrics",
                initialTextInput = lyrics ?: "",
                singleLine = false,
                maxLines = 10,
                isTextInputValid = { true },
                onDismiss = {
                    isEditingLyrics = false
                },
                onDone = {
                    query {
                        Database.updateLyrics(mediaId, it)
                    }
                }
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            onDismiss()
                        }
                    )
                }
                .fillMaxSize()
                .background(Color.Black.copy(0.8f))
        ) {
            AnimatedVisibility(
                visible = !isLoading && lyrics == null,
                enter = slideInVertically { -it },
                exit = slideOutVertically { -it },
                modifier = Modifier
                    .align(Alignment.TopCenter)
            ) {
                BasicText(
                    text = "An error has occurred while fetching the lyrics",
                    style = typography.xs.center.medium.color(BlackColorPalette.text),
                    modifier = Modifier
                        .background(Color.Black.copy(0.4f))
                        .padding(all = 8.dp)
                        .fillMaxWidth()
                )
            }

            AnimatedVisibility(
                visible = lyrics?.let(String::isEmpty) ?: false,
                enter = slideInVertically { -it },
                exit = slideOutVertically { -it },
                modifier = Modifier
                    .align(Alignment.TopCenter)
            ) {
                BasicText(
                    text = "Lyrics are not available for this song",
                    style = typography.xs.center.medium.color(BlackColorPalette.text),
                    modifier = Modifier
                        .background(Color.Black.copy(0.4f))
                        .padding(all = 8.dp)
                        .fillMaxWidth()
                )
            }

            if (isLoading) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .shimmer()
                ) {
                    repeat(4) { index ->
                        TextPlaceholder(
                            modifier = Modifier
                                .alpha(1f - index * 0.05f)
                        )
                    }
                }
            } else {
                lyrics?.let { lyrics ->
                    if (lyrics.isNotEmpty()) {
                        BasicText(
                            text = lyrics,
                            style = typography.xs.center.medium.color(BlackColorPalette.text),
                            modifier = Modifier
                                .nestedScroll(remember { nestedScrollConnectionProvider() })
                                .verticalFadingEdge()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = size / 4, horizontal = 32.dp)
                        )
                    }

                    val menuState = LocalMenuState.current

                    Image(
                        painter = painterResource(R.drawable.ellipsis_horizontal),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(DarkColorPalette.text),
                        modifier = Modifier
                            .padding(all = 4.dp)
                            .clickable {
                                menuState.display {
                                    Menu {
                                        MenuEntry(
                                            icon = R.drawable.pencil,
                                            text = "Edit lyrics",
                                            onClick = {
                                                menuState.hide()
                                                isEditingLyrics = true
                                            }
                                        )

                                        MenuEntry(
                                            icon = R.drawable.search,
                                            text = "Search lyrics online",
                                            onClick = {
                                                menuState.hide()
                                                val mediaMetadata = mediaMetadataProvider()

                                                val intent =
                                                    Intent(Intent.ACTION_WEB_SEARCH).apply {
                                                        putExtra(
                                                            SearchManager.QUERY,
                                                            "${mediaMetadata.title} ${mediaMetadata.artist} lyrics"
                                                        )
                                                    }

                                                if (intent.resolveActivity(context.packageManager) != null) {
                                                    context.startActivity(intent)
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "No browser app found!",
                                                        Toast.LENGTH_SHORT
                                                    )
                                                        .show()
                                                }
                                            }
                                        )

                                        MenuEntry(
                                            icon = R.drawable.download,
                                            text = "Fetch lyrics again",
                                            onClick = {
                                                menuState.hide()
                                                query {
                                                    Database.updateLyrics(mediaId, null)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                            .padding(all = 8.dp)
                            .size(20.dp)
                            .align(Alignment.BottomEnd)
                    )
                }
            }
        }
    }
}
