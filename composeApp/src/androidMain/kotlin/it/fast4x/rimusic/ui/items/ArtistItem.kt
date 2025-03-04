package it.fast4x.rimusic.ui.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import it.fast4x.rimusic.models.Artist
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
fun ArtistItem(
    artist: Artist,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
    showName: Boolean = true,
    disableScrollingText: Boolean,
    isYoutubeArtist : Boolean = false
) {
    ArtistItem(
        thumbnailUrl = artist.thumbnailUrl,
        name = artist.name ?: "",
        subscribersCount = null,
        thumbnailSizePx = thumbnailSizePx,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier,
        alternative = alternative,
        showName = showName,
        disableScrollingText = disableScrollingText,
        isYoutubeArtist = isYoutubeArtist
    )
}

@Composable
fun ArtistItem(
    artist: Environment.ArtistItem,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
    disableScrollingText: Boolean,
    isYoutubeArtist : Boolean = false,
    smallThumbnail: Boolean = false
) {
    ArtistItem(
        thumbnailUrl = artist.thumbnail?.url,
        name = artist.info?.name,
        subscribersCount = artist.subscribersCountText,
        thumbnailSizePx = thumbnailSizePx,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier,
        alternative = alternative,
        disableScrollingText = disableScrollingText,
        isYoutubeArtist = isYoutubeArtist,
        smallThumbnail = smallThumbnail
    )
}

@Composable
fun ArtistItem(
    thumbnailUrl: String?,
    name: String?,
    subscribersCount: String?,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
    showName: Boolean = true,
    disableScrollingText: Boolean,
    isYoutubeArtist : Boolean = false,
    smallThumbnail: Boolean = false
) {
    ItemContainer(
        alternative = alternative,
        thumbnailSizeDp = thumbnailSizeDp,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box {
            AsyncImage(
                model = thumbnailUrl?.thumbnail(thumbnailSizePx),
                contentDescription = null,
                modifier = Modifier
                    //.clip(CircleShape)
                    .clip(thumbnailShape())
                    .requiredSize(thumbnailSizeDp)
            )
            if (isYoutubeArtist) {
                Image(
                    painter = painterResource(R.drawable.ytmusic),
                    colorFilter = ColorFilter.tint(
                        Color.Red.copy(0.75f).compositeOver(Color.White)
                    ),
                    modifier = Modifier
                        .size(if (smallThumbnail) 30.dp else 40.dp)
                        .padding(all = 5.dp),
                    contentDescription = "Background Image",
                    contentScale = ContentScale.Fit
                )
            }
        }

    if (showName)
        ItemInfoContainer(
            horizontalAlignment = if (alternative) Alignment.CenterHorizontally else Alignment.Start,
        ) {
            BasicText(
                text = cleanPrefix(name ?: ""),
                style = typography().xs.semiBold,
                maxLines = 1, //if (alternative) 1 else 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .conditional(!disableScrollingText) { basicMarquee(iterations = Int.MAX_VALUE) }
            )

            subscribersCount?.let {
                BasicText(
                    text = subscribersCount,
                    style = typography().xxs.semiBold.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .conditional(!disableScrollingText) { basicMarquee(iterations = Int.MAX_VALUE) }
                )
            }
        }
    }
}

@Composable
fun ArtistItemPlaceholder(
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
) {
    ItemContainer(
        alternative = alternative,
        thumbnailSizeDp = thumbnailSizeDp,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Spacer(
            modifier = Modifier
                .background(color = colorPalette().shimmer, shape = CircleShape)
                .size(thumbnailSizeDp)
        )

        ItemInfoContainer(
            horizontalAlignment = if (alternative) Alignment.CenterHorizontally else Alignment.Start,
        ) {
            TextPlaceholder()
            TextPlaceholder(
                modifier = Modifier
                    .padding(top = 4.dp)
            )
        }
    }
}
