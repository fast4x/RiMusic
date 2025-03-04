package it.fast4x.rimusic.ui.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import it.fast4x.environment.Environment
import it.fast4x.rimusic.R
import it.fast4x.rimusic.cleanPrefix
import it.fast4x.rimusic.models.Album
import it.fast4x.rimusic.ui.components.themed.TextPlaceholder
import it.fast4x.rimusic.ui.styling.shimmer
import it.fast4x.rimusic.utils.conditional
import it.fast4x.rimusic.utils.secondary
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.thumbnail
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.thumbnailShape
import it.fast4x.rimusic.typography

@Composable
fun AlbumItem(
    album: Album,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
    yearCentered: Boolean = true,
    showAuthors: Boolean = false,
    disableScrollingText: Boolean,
    isYoutubeAlbum: Boolean = false
) {
    AlbumItem(
        thumbnailUrl = album.thumbnailUrl,
        title = album.title,
        authors = album.authorsText,
        year = album.year,
        yearCentered = yearCentered,
        thumbnailSizePx = thumbnailSizePx,
        thumbnailSizeDp = thumbnailSizeDp,
        alternative = alternative,
        showAuthors = showAuthors,
        modifier = modifier,
        disableScrollingText = disableScrollingText,
        isYoutubeAlbum = isYoutubeAlbum
    )
}

@Composable
fun AlbumItem(
    album: Environment.AlbumItem,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
    yearCentered: Boolean? = true,
    showAuthors: Boolean? = false,
    isYoutubeAlbum: Boolean = false,
    disableScrollingText: Boolean
) {
    AlbumItem(
        thumbnailUrl = album.thumbnail?.url,
        title = album.info?.name,
        authors = album.authors?.joinToString(", ") { it.name ?: "" },
        year = album.year,
        yearCentered = yearCentered,
        thumbnailSizePx = thumbnailSizePx,
        thumbnailSizeDp = thumbnailSizeDp,
        alternative = alternative,
        modifier = modifier,
        disableScrollingText = disableScrollingText,
        isYoutubeAlbum = isYoutubeAlbum
    )
}

@Composable
fun AlbumItem(
    thumbnailUrl: String?,
    title: String?,
    authors: String?,
    year: String?,
    yearCentered: Boolean? = true,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
    showAuthors: Boolean? = false,
    disableScrollingText: Boolean,
    isYoutubeAlbum: Boolean = false
) {
    ItemContainer(
        alternative = alternative,
        thumbnailSizeDp = thumbnailSizeDp,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box {
            AsyncImage(
                model = thumbnailUrl?.thumbnail(thumbnailSizePx)?.let { it1 -> cleanPrefix(it1) },
                contentDescription = null,
                //contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(thumbnailShape())
                    .requiredSize(thumbnailSizeDp)
            )
            if (isYoutubeAlbum) {
                Image(
                    painter = painterResource(R.drawable.ytmusic),
                    colorFilter = ColorFilter.tint(Color.Red.copy(0.75f).compositeOver(Color.White)),
                    modifier = Modifier
                        .size(40.dp)
                        .padding(all = 5.dp),
                    contentDescription = "Background Image",
                    contentScale = ContentScale.Fit
                )
            }
        }
        ItemInfoContainer {
            BasicText(
                text = cleanPrefix(title ?: ""),
                style = typography().xs.semiBold,
                maxLines = 1, //if (alternative) 1 else 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .conditional(!disableScrollingText) { basicMarquee(iterations = Int.MAX_VALUE) }
            )

            if (!alternative || showAuthors == true) {
                authors?.let {
                    BasicText(
                        text = cleanPrefix(authors),
                        style = typography().xs.semiBold.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .conditional(!disableScrollingText) { basicMarquee(iterations = Int.MAX_VALUE) }
                            .align(
                                if (yearCentered == true) Alignment.CenterHorizontally else Alignment.Start)
                    )
                }
            }

            BasicText(
                text = year ?: "",
                style = typography().xxs.semiBold.secondary,
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
fun AlbumItemPlaceholder(
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false
) {
    ItemContainer(
        alternative = alternative,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier
    ) {
        Spacer(
            modifier = Modifier
                .background(color = colorPalette().shimmer, shape = thumbnailShape())
                .size(thumbnailSizeDp)
        )

        ItemInfoContainer {
            TextPlaceholder()

            if (!alternative) {
                TextPlaceholder()
            }

            TextPlaceholder(
                modifier = Modifier
                    .padding(top = 4.dp)
            )
        }
    }
}
