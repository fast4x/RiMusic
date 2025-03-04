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
import database.DB
import database.entities.Album
import database.entities.Song
import database.entities.SongAlbumMap
import it.fast4x.environment.Environment
import it.fast4x.environment.models.bodies.BrowseBody
import it.fast4x.environment.requests.albumPage
import it.fast4x.rimusic.MODIFIED_PREFIX
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
import it.fast4x.rimusic.ui.components.ToolButton
import it.fast4x.rimusic.utils.asSong
import it.fast4x.rimusic.utils.fadingEdge
import it.fast4x.rimusic.utils.getHttpClient
import it.fast4x.rimusic.utils.languageDestination
import it.fast4x.rimusic.utils.resize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.bush.translator.Language
import me.bush.translator.Translator
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import rimusic.composeapp.generated.resources.Res
import rimusic.composeapp.generated.resources.album_alternative_versions
import rimusic.composeapp.generated.resources.bookmark
import rimusic.composeapp.generated.resources.bookmark_outline
import rimusic.composeapp.generated.resources.from_wikipedia_cca
import rimusic.composeapp.generated.resources.information
import rimusic.composeapp.generated.resources.shuffle
import rimusic.composeapp.generated.resources.songs
import rimusic.composeapp.generated.resources.translate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlbumScreen(
    browseId: String,
    onSongClick: (Song) -> Unit,
    onAlbumClick: (String) -> Unit
) {
    val coroutineScope by remember { mutableStateOf(CoroutineScope(Dispatchers.IO)) }
    //val leftScrollState = rememberScrollState()
    //val rightScrollState = rememberScrollState()


    var album by remember { mutableStateOf<Album?>(null) }
//    LaunchedEffect(Unit) {
//        MusicDatabaseDesktop.album(browseId).collect { album = it }
//    }



    val albumPage = remember { mutableStateOf<Environment.PlaylistOrAlbumPage?>(null) }
    LaunchedEffect(browseId) {
        DB.album(browseId).collect { currentAlbum ->
            album = currentAlbum

            if (albumPage.value == null)
                Environment.albumPage(BrowseBody(browseId = browseId))
                    ?.onSuccess { currentAlbumPage ->
                        albumPage.value = currentAlbumPage

                        //println("browseId: $browseId album: $album")
                        println("browseId: $browseId albumPage: ${albumPage.value}")

                        coroutineScope.launch {
                            DB.upsert(
                                Album(
                                    id = browseId,
                                    title = if (currentAlbum?.title?.startsWith(MODIFIED_PREFIX) == true) currentAlbum.title else currentAlbumPage.title,
                                    thumbnailUrl = if (currentAlbum?.thumbnailUrl?.startsWith(
                                            MODIFIED_PREFIX
                                        ) == true
                                    ) currentAlbum.thumbnailUrl else currentAlbumPage.thumbnail?.url,
                                    year = currentAlbumPage.year,
                                    authorsText = if (currentAlbum?.authorsText?.startsWith(
                                            MODIFIED_PREFIX
                                        ) == true
                                    ) currentAlbum.authorsText else currentAlbumPage.authors
                                        ?.joinToString("") { it.name ?: "" },
                                    shareUrl = currentAlbumPage.url,
                                    timestamp = System.currentTimeMillis(),
                                    bookmarkedAt = currentAlbum?.bookmarkedAt
                                )
                            )

                            currentAlbumPage
                                .songsPage
                                ?.items
                                ?.map(Environment.SongItem::asSong)
                                ?.onEach {
                                    DB.upsert(it)
                                }
                                ?.mapIndexed { position, song ->
                                    DB.upsert(
                                        SongAlbumMap(
                                            songId = song.id,
                                            albumId = browseId,
                                            position = position
                                        )
                                    )
                                }
                        }

                    }
        }

    }

    val endPaddingValues = windowInsets.only(WindowInsetsSides.End).asPaddingValues()

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
            ) {

                Box(
                    modifier = Modifier
                        .aspectRatio(4f / 3)
                        //.height(300.dp)
                        .fillMaxWidth()
                ) {
                    albumPage.value?.let {
                        AsyncImage(
                            model = albumPage.value!!.thumbnail?.url?.resize(1200, 900),
                            contentDescription = "loading...",
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .fadingEdge(
                                    top = fadeSpacingTop,
                                    bottom = fadeSpacingBottom
                                )
                        )

                        if (!albumPage.value?.title.isNullOrEmpty())
                            AutoResizeText(
                                text = albumPage.value?.title.toString(),
                                style = typography.displayMedium,
                                fontSizeRange = FontSizeRange(50.sp, 100.sp),
                                fontWeight = typography.displayMedium.fontWeight,
                                fontFamily = typography.displayMedium.fontFamily,
                                color = typography.displayMedium.color,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(horizontal = 30.dp)
                                    .padding(bottom = 30.dp)
                            )


                        Text(
                            text = "${albumPage.value?.year} - " + albumPage.value?.songsPage?.items?.size.toString() + " "
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

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp)
                ) {
                    ToolButton(
                        icon = if (album?.bookmarkedAt == null) Res.drawable.bookmark_outline else Res.drawable.bookmark,
                        onAction = {
                            val bookmarkedAt =
                                if (album?.bookmarkedAt == null) System.currentTimeMillis() else null

                            album.let {
                                coroutineScope.launch {
                                    if (it != null) {
                                        DB.upsert(it.copy(bookmarkedAt = bookmarkedAt))
                                    }
                                }
                            }


                        }
                    )
                    ToolButton(
                        icon = Res.drawable.shuffle,
                        onAction = {}
                    )

                }


                albumPage.value?.description?.let { description ->
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
                            .clickable {
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

                    Column(
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp)
                    ) {
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

                if (albumPage.value != null) {

                    albumPage.value!!.songsPage?.items.let { allSongs ->

                        val parentalControlEnabled = false

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

                    albumPage.value!!.otherVersions?.let { otherVersion ->
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