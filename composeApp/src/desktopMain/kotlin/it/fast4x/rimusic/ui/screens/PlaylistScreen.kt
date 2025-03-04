package it.fast4x.rimusic.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DrawerDefaults.windowInsets
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import database.entities.Song
import it.fast4x.environment.Environment
import it.fast4x.environment.models.bodies.BrowseBody
import it.fast4x.environment.requests.playlistPage
import it.fast4x.rimusic.EXPLICIT_PREFIX
import it.fast4x.rimusic.items.AlbumItem
import it.fast4x.rimusic.items.SongItem
import it.fast4x.rimusic.styling.Dimensions.albumThumbnailSize
import it.fast4x.rimusic.styling.Dimensions.fadeSpacingBottom
import it.fast4x.rimusic.styling.Dimensions.fadeSpacingTop
import it.fast4x.rimusic.styling.Dimensions.layoutColumnBottomSpacer
import it.fast4x.rimusic.ui.components.AutoResizeText
import it.fast4x.rimusic.ui.components.FontSizeRange
import it.fast4x.rimusic.ui.components.Loader
import it.fast4x.rimusic.ui.components.Title
import it.fast4x.rimusic.utils.asSong
import it.fast4x.rimusic.utils.fadingEdge
import it.fast4x.rimusic.utils.getHttpClient
import it.fast4x.rimusic.utils.languageDestination
import it.fast4x.rimusic.utils.resize
import it.fast4x.rimusic.utils.completed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.bush.translator.Language
import me.bush.translator.Translator
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import rimusic.composeapp.generated.resources.Res
import rimusic.composeapp.generated.resources.album_alternative_versions
import rimusic.composeapp.generated.resources.from_wikipedia_cca
import rimusic.composeapp.generated.resources.information
import rimusic.composeapp.generated.resources.songs
import rimusic.composeapp.generated.resources.translate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaylistScreen(
    browseId: String,
    onSongClick: (Song) -> Unit,
    onAlbumClick: (String) -> Unit,
    onClosePage: () -> Unit
) {
    //val leftScrollState = rememberScrollState()
    //val rightScrollState = rememberScrollState()
    var playlistPage by remember { mutableStateOf<Environment.PlaylistOrAlbumPage?>(null) }
    var playlistSongs by remember { mutableStateOf<List<Environment.SongItem?>>(emptyList()) }
    val parentalControlEnabled by remember{ mutableStateOf(false)}
    LaunchedEffect(Unit) {
        if (playlistPage != null && playlistPage!!.songsPage?.continuation == null) return@LaunchedEffect

        playlistPage = withContext(Dispatchers.IO) {
            Environment.playlistPage(BrowseBody(browseId = browseId))?.completed()?.getOrNull()
        }

        println("mediaItem playlistPage ${playlistPage?.songsPage}")

        playlistSongs = if (parentalControlEnabled)
            playlistPage?.songsPage?.items?.filter { !it.asSong.title.startsWith(EXPLICIT_PREFIX) }!!
        else playlistPage?.songsPage?.items ?: emptyList()
    }
    val playlist = playlistPage
    val endPaddingValues = windowInsets.only(WindowInsetsSides.End).asPaddingValues()

    val sectionTextModifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 24.dp, bottom = 8.dp)

    var translateEnabled by remember {
        mutableStateOf(false)
    }

    val translator = Translator(getHttpClient())
    val languageDestination = languageDestination()

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
                    .fillMaxWidth(0.5f)
                    //.verticalScroll(leftScrollState)
            ) {
                /*
                ExpandIcon(
                    onAction = onClosePage,
                    actionClose = true,
                    modifier = Modifier.fillMaxWidth()
                )
                 */


                Box(
                    modifier = Modifier
                        .aspectRatio(4f / 3)
                        //.height(300.dp)
                        .fillMaxWidth()
                ) {

                    playlistPage?.let {
                        AsyncImage(
                            model = playlistPage!!.thumbnail?.url?.resize(1200, 900),
                            contentDescription = "loading...",
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .fadingEdge(
                                    top = fadeSpacingTop,
                                    bottom = fadeSpacingBottom
                                )
                        )

                        if (!playlistPage?.title.isNullOrEmpty())
                            AutoResizeText(
                                text = playlistPage?.title.toString(),
                                style = typography.displayLarge,
                                fontSizeRange = FontSizeRange(50.sp, 100.sp),
                                fontWeight = typography.displayLarge.fontWeight,
                                fontFamily = typography.displayLarge.fontFamily,
                                color = typography.displayLarge.color,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(horizontal = 30.dp)
                            )


                        Text(
                            text = "${playlistPage?.year} - " + playlistPage?.songsPage?.items?.size.toString() + " "
                                    + stringResource(Res.string.songs),
                            //+ " - " + formatAsTime(totalPlayTimes),
                            style = TextStyle(
                                fontSize = typography.titleMedium.fontSize,
                                fontWeight = typography.titleMedium.fontWeight,
                                color = Color.White,
                                textAlign = TextAlign.Start
                            ),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)

                        )
                    } ?: Loader()

                }



                playlistPage?.description?.let { description ->
                    val attributionsIndex = description.lastIndexOf("\n\nFrom Wikipedia")

                    Title(
                        title = stringResource(Res.string.information)
                    )

                    var translatedText by remember { mutableStateOf("") }
                    val nonTranslatedText by remember {
                        mutableStateOf(
                            if (attributionsIndex == -1) {
                                description
                            } else {
                                description.substring(0, attributionsIndex)
                            }
                        )
                    }

                    Image(
                        painter = painterResource(Res.drawable.translate),
                        colorFilter = ColorFilter.tint(if (translateEnabled) Color.White else Color.Gray),
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable{
                                translateEnabled = !translateEnabled
                            }
                    )

                    if (translateEnabled == true) {
                        LaunchedEffect(Unit) {
                            val result = withContext(Dispatchers.IO) {
                                try {
                                    translator.translate(
                                        nonTranslatedText,
                                        languageDestination,
                                        Language.AUTO
                                    ).translatedText
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            translatedText =
                                if (result.toString() == "kotlin.Unit") "" else result.toString()
                        }
                    } else translatedText = nonTranslatedText

                    Column (
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp)
                    ){
                        Text(
                            text = "“ $translatedText „",
                            style = TextStyle(
                                fontSize = typography.titleSmall.fontSize,
                                //fontWeight = typography.titleSmall.fontWeight,
                                color = Color.White,
                                textAlign = TextAlign.Start
                            )

                        )

                        if (attributionsIndex != -1) {
                            Text(
                                text = stringResource(Res.string.from_wikipedia_cca),
                                style = TextStyle(
                                    fontSize = typography.titleSmall.fontSize,
                                    fontWeight = typography.titleSmall.fontWeight,
                                    color = Color.White,
                                    textAlign = TextAlign.Start
                                ),
                                //modifier = Modifier
                                    //.padding(horizontal = 16.dp)
                                    //.padding(bottom = 16.dp)
                                //.padding(endPaddingValues)

                            )
                        }
                    }

                }
                Spacer(modifier = Modifier.height(layoutColumnBottomSpacer))
            }

            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxHeight()
                    //.fillMaxWidth(0.5f)
                    .fillMaxSize()
                    //.verticalScroll(rightScrollState)
            ) {

                if (playlistPage != null) {

                    playlistPage!!.songsPage?.items.let { allSongs ->

                        val songs = if (parentalControlEnabled)
                            allSongs?.filter { !it.explicit } else allSongs
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(endPaddingValues)
                        ) {
                            Title(
                                title = stringResource(Res.string.songs),
                                onClick = {
                                    //if (youtubeArtistPage.songsEndpoint?.browseId != null) {

                                    //} else SmartToast(context.resources.getString(R.string.info_no_songs_yet))
                                },
                                //modifier = Modifier.fillMaxWidth(0.7f)
                            )

                        }

                        songs?.forEachIndexed { index, song ->

                            SongItem(
                                song = song,
                                isDownloaded = false,
                                onDownloadClick = {},
                                //thumbnailSizeDp = songThumbnailSizeDp,
                                modifier = Modifier
                                    .combinedClickable(
                                        onLongClick = {
                                            /*
                                            menuState.display {
                                                NonQueuedMediaItemMenu(
                                                    navController = navController,
                                                    onDismiss = menuState::hide,
                                                    mediaItem = song.asMediaItem,
                                                )
                                            }
                                             */
                                        },
                                        onClick = {
                                            onSongClick(song.asSong)
                                        }
                                    )
                                    .padding(endPaddingValues)
                            )

                        }
                    }

                    playlistPage!!.otherVersions?.let { otherVersion ->
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(endPaddingValues)
                        ) {
                            Title(
                                title = stringResource(Res.string.album_alternative_versions),
                                onClick = {
                                    //if (youtubeArtistPage.albumsEndpoint?.browseId != null) {
                                    //onViewAllAlbumsClick()
                                    //} else SmartToast(context.resources.getString(R.string.info_no_albums_yet))
                                }
                            )
                        }

                        LazyRow(
                            contentPadding = endPaddingValues,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            items(
                                items = otherVersion,
                                key = Environment.AlbumItem::key
                            ) { album ->
                                AlbumItem(
                                    album = album,
                                    thumbnailSizeDp = albumThumbnailSize,
                                    alternative = true,
                                    modifier = Modifier
                                        .clickable(onClick = {
                                            onAlbumClick(album.key)
                                        }
                                        )
                                )
                            }
                        }
                    }

                }


            }
        }

    }

}