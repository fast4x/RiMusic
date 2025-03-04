package it.fast4x.rimusic.items

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import database.entities.Album
import database.entities.SongEntity
import it.fast4x.environment.Environment
import it.fast4x.rimusic.MONTHLY_PREFIX
import it.fast4x.rimusic.PINNED_PREFIX
import it.fast4x.rimusic.PIPED_PREFIX
import it.fast4x.rimusic.cleanPrefix
import it.fast4x.rimusic.enums.ThumbnailRoundness
import it.fast4x.rimusic.utils.LoadImage
import it.fast4x.rimusic.utils.getTitleMonthlyPlaylist
import org.jetbrains.compose.resources.painterResource
import rimusic.composeapp.generated.resources.Res
import rimusic.composeapp.generated.resources.pin
import rimusic.composeapp.generated.resources.piped_logo
import rimusic.composeapp.generated.resources.stat_month

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongItem(
    thumbnailContent: @Composable BoxScope.() -> Unit?,
    title: String?,
    authors: String?,
    duration: String?,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null,
    isDownloaded: Boolean,
    onDownloadClick: () -> Unit
) {

    ItemContainer(
        alternative = false,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(thumbnailSizeDp) //.border(1.dp, Color.Red)
        ) {
            thumbnailContent()
        }

        ItemInfoContainer {
            trailingContent?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BasicText(
                        text = title ?: "",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = typography.titleSmall.fontSize,
                            //fontWeight = typography.titleMedium.fontWeight
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .basicMarquee(iterations = Int.MAX_VALUE)
                    )

                    it()
                }
            } ?: BasicText(
                text = title ?: "",
                style = TextStyle(
                    color = Color.White,
                    fontSize = typography.titleSmall.fontSize,
                    //fontWeight = typography.titleSmall.fontWeight
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .basicMarquee(iterations = Int.MAX_VALUE)
            )


            Row(verticalAlignment = Alignment.CenterVertically) {

                /*
                IconButton(
                    onClick = onDownloadClick,
                    icon = if (isDownloaded) R.drawable.downloaded else R.drawable.download,
                    color = if (isDownloaded) colorPalette.text else colorPalette.textDisabled,
                    modifier = Modifier
                        .size(16.dp)
                )
                 */

                Spacer(modifier = Modifier.padding(horizontal = 2.dp))

                BasicText(
                    text = authors ?: "",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = typography.titleSmall.fontSize,
                        //fontWeight = typography.titleSmall.fontWeight
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier
                        .weight(1f)
                        .basicMarquee(iterations = Int.MAX_VALUE)
                )

                duration?.let {
                    BasicText(
                        text = duration,
                        style = TextStyle(
                            color = Color.White,
                            fontSize = typography.titleSmall.fontSize,
                            //fontWeight = typography.titleSmall.fontWeight
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SongItem(
    song: Environment.SongItem,
    isDownloaded: Boolean = false,
    onDownloadClick: () -> Unit = {},
    thumbnailSizeDp: Dp = 50.dp,
    modifier: Modifier
) {
    SongItem(
        title = song.info?.name,
        authors = song.authors?.joinToString(", ") { it.name.toString() },
        duration = song.durationText,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier,
        isDownloaded = isDownloaded,
        thumbnailContent = {

            song.thumbnail?.url.let {
                if (it != null) {
                    LoadImage(it)
                }
            }
            //onThumbnailContent?.invoke(this)
        },
        trailingContent = {},
        onDownloadClick = onDownloadClick
    )
}

@Composable
fun SongItem(
    songEntity: SongEntity,
    isDownloaded: Boolean = false,
    onDownloadClick: () -> Unit = {},
    thumbnailSizeDp: Dp = 50.dp,
    modifier: Modifier
) {
    SongItem(
        title = songEntity.song.title,
        authors = songEntity.song.artistsText,
        duration = songEntity.song.durationText,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier,
        isDownloaded = isDownloaded,
        thumbnailContent = {

            songEntity.song.thumbnailUrl.let {
                if (it != null) {
                    LoadImage(it)
                }
            }
            //onThumbnailContent?.invoke(this)
        },
        trailingContent = {},
        onDownloadClick = onDownloadClick
    )
}



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlbumItem(
    thumbnailUrl: String?,
    title: String?,
    authors: String?,
    year: String?,
    yearCentered: Boolean? = true,
    thumbnailSizePx: Int?,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
    showAuthors: Boolean? = false
) {

    ItemContainer(
        alternative = alternative,
        thumbnailSizeDp = thumbnailSizeDp,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        AsyncImage(
            model = thumbnailUrl.let { cleanPrefix(it ?: "") }, //thumbnailUrl?.thumbnail(thumbnailSizePx)?.let { it1 -> cleanPrefix(it1) },) } //thumbnailUrl?.thumbnail(thumbnailSizePx)?.let { it1 -> cleanPrefix(it1) },
            contentDescription = null,
            //contentScale = ContentScale.Crop,
            modifier = Modifier
                .clip(ThumbnailRoundness.Medium.shape())
                .requiredSize(thumbnailSizeDp)
        )

        ItemInfoContainer {
            BasicText(
                text = cleanPrefix(title ?: ""),
                style = TextStyle(
                    color = Color.White,
                    fontSize = typography.titleSmall.fontSize,
                    //fontWeight = typography.titleSmall.fontWeight
                ),
                maxLines = 1, //if (alternative) 1 else 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .basicMarquee(iterations = Int.MAX_VALUE)
            )

            if (!alternative || showAuthors == true) {
                authors?.let {
                    BasicText(
                        text = cleanPrefix(authors),
                        style = TextStyle(
                            color = Color.White,
                            fontSize = typography.titleSmall.fontSize,
                            //fontWeight = typography.titleSmall.fontWeight
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .basicMarquee(iterations = Int.MAX_VALUE)
                            .align(
                                if (yearCentered == true) Alignment.CenterHorizontally else Alignment.Start)
                    )
                }
            }

            BasicText(
                text = year ?: "",
                style = TextStyle(
                    color = Color.White,
                    fontSize = typography.titleSmall.fontSize,
                    //fontWeight = typography.titleSmall.fontWeight
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .align(
                        if (yearCentered == true) Alignment.CenterHorizontally else Alignment.Start)
            )
        }
    }
}

@Composable
fun AlbumItem(
    album: Environment.AlbumItem,
    yearCentered: Boolean? = true,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
    showAuthors: Boolean? = false
) {
    AlbumItem(
        thumbnailUrl = album.thumbnail?.url,
        title = album.info?.name,
        authors = album.authors?.joinToString(", ") { it.name.toString() },
        year = album.year,
        yearCentered = yearCentered,
        thumbnailSizePx = null,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier,
        alternative = alternative,
        showAuthors = showAuthors
    )
}

@Composable
fun AlbumItem(
    album: Album,
    yearCentered: Boolean? = true,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
    showAuthors: Boolean? = false
) {
    AlbumItem(
        thumbnailUrl = album.thumbnailUrl,
        title = album.title,
        authors = album.authorsText,
        year = album.year,
        yearCentered = yearCentered,
        thumbnailSizePx = null,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier,
        alternative = alternative,
        showAuthors = showAuthors
    )
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArtistItem(
    thumbnailUrl: String?,
    name: String?,
    subscribersCount: String?,
    //thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
    showName: Boolean = true
) {
    ItemContainer(
        alternative = alternative,
        thumbnailSizeDp = thumbnailSizeDp,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        AsyncImage(
            model = thumbnailUrl,
            contentDescription = null,
            modifier = Modifier
                .clip(ThumbnailRoundness.Medium.shape())
                .requiredSize(thumbnailSizeDp)
        )

        if (showName)
            ItemInfoContainer(
                horizontalAlignment = if (alternative) Alignment.CenterHorizontally else Alignment.Start,
            ) {
                BasicText(
                    text = name ?: "",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = typography.titleSmall.fontSize,
                        //fontWeight = typography.titleSmall.fontWeight
                    ),
                    maxLines = 1, //if (alternative) 1 else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .basicMarquee(iterations = Int.MAX_VALUE)
                )

                subscribersCount?.let {
                    BasicText(
                        text = subscribersCount,
                        style = TextStyle(
                            color = Color.White,
                            fontSize = typography.titleSmall.fontSize,
                            //fontWeight = typography.titleSmall.fontWeight
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .basicMarquee(iterations = Int.MAX_VALUE)
                    )
                }
            }
    }
}

@Composable
fun ArtistItem(
    artist: Environment.ArtistItem,
    //thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
) {
    ArtistItem(
        thumbnailUrl = artist.thumbnail?.url,
        name = artist.info?.name,
        subscribersCount = artist.subscribersCountText,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier,
        alternative = alternative
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaylistItem(
    thumbnailContent: @Composable BoxScope.(
        //modifier: Modifier
    ) -> Unit,
    songCount: Int?,
    name: String?,
    channelName: String?,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
    showName: Boolean = true,
    showSongsCount: Boolean = true
) {
    ItemContainer(
        alternative = alternative,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier
    ) { //centeredModifier ->
        Box(
            modifier = Modifier
                .clip(ThumbnailRoundness.Medium.shape())
                //.background(color = colorPalette().background4)
                .requiredSize(thumbnailSizeDp)
        ) {
            thumbnailContent(
                /*
                modifier = Modifier
                    .fillMaxSize()

                 */
            )

            name?.let {
                if (it.startsWith(PIPED_PREFIX,0,true)) {
                    Image(
                        painter = painterResource(Res.drawable.piped_logo),
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier
                            .size(40.dp)
                            .padding(all = 5.dp),
                        contentDescription = "Background Image"
                    )
                }
                if (it.startsWith(PINNED_PREFIX,0,true)) {
                    Image(
                        painter = painterResource(Res.drawable.pin),
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier
                            .size(40.dp)
                            .padding(all = 5.dp),
                        contentDescription = "Background Image",
                        contentScale = ContentScale.Fit
                    )
                }
                if (it.startsWith(MONTHLY_PREFIX,0,true)) {
                    Image(
                        painter = painterResource(Res.drawable.stat_month),
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier
                            .size(40.dp)
                            .padding(all = 5.dp),
                        contentDescription = "Background Image",
                        contentScale = ContentScale.Fit
                    )
                }
            }


            if (showSongsCount)
                songCount?.let {
                    BasicText(
                        text = "$songCount",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = typography.titleSmall.fontSize,
                            //fontWeight = typography.titleSmall.fontWeight
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(all = 4.dp)
                            .background(color = Color.White, shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                            .align(Alignment.BottomEnd)
                    )
                }

        }


        ItemInfoContainer(
            horizontalAlignment = if (alternative && channelName == null) Alignment.CenterHorizontally else Alignment.Start,
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (showName)
                if (name != null) {
                    BasicText(
                        //text = name.substringAfter(PINNED_PREFIX) ?: "",
                        text = if (name.startsWith(PINNED_PREFIX,0,true))
                            name.substringAfter(PINNED_PREFIX) else
                            if (name.startsWith(MONTHLY_PREFIX,0,true))
                                getTitleMonthlyPlaylist(name.substringAfter(MONTHLY_PREFIX)) else
                                if (name.startsWith(PIPED_PREFIX,0,true))
                                    name.substringAfter(PIPED_PREFIX) else name,
                        style = TextStyle(
                            color = Color.White,
                            fontSize = typography.titleSmall.fontSize,
                            //fontWeight = typography.titleSmall.fontWeight
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .basicMarquee(iterations = Int.MAX_VALUE)
                    )
                }

            channelName?.let {
                BasicText(
                    text = channelName,
                    style = TextStyle(
                        color = Color.White,
                        fontSize = typography.titleSmall.fontSize,
                        //fontWeight = typography.titleSmall.fontWeight
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .basicMarquee(iterations = Int.MAX_VALUE)
                )
            }
        }
    }
}

@Composable
fun PlaylistItem(
    thumbnailUrl: String?,
    songCount: Int?,
    name: String?,
    channelName: String?,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
    showSongsCount: Boolean = true
) {
    PlaylistItem(
        thumbnailContent = {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        },
        songCount = songCount,
        showSongsCount = showSongsCount,
        name = name,
        channelName = channelName,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier,
        alternative = alternative,
    )
}

@Composable
fun PlaylistItem(
    playlist: Environment.PlaylistItem,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
    showSongsCount: Boolean = true
) {
    PlaylistItem(
        thumbnailUrl = playlist.thumbnail?.url,
        songCount = playlist.songCount,
        showSongsCount = showSongsCount,
        name = playlist.info?.name,
        channelName = playlist.channel?.name,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier,
        alternative = alternative
    )
}

@Composable
fun MoodItemColored(
    mood: Environment.Mood.Item,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val moodColor by remember { derivedStateOf { Color(mood.stripeColor) } }

    Column (
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .clip(ThumbnailRoundness.Medium.shape())
            .clickable { onClick() }

    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(color = moodColor)
                .padding(start = 10.dp)
                .fillMaxHeight(0.9f)
        ) {
            Box(
                modifier = Modifier
                    .requiredWidth(150.dp)
                    //.background(color = colorPalette().background4)
                    .fillMaxSize()
            ) {

                BasicText(
                    text = mood.title,
                    style = TextStyle(
                        color = Color.White,
                        fontSize = typography.titleSmall.fontSize,
                        //fontWeight = typography.titleSmall.fontWeight
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp).align(Alignment.CenterStart),
                    maxLines = 2,

                    )
            }
        }
    }
}