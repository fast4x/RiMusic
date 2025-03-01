package it.fast4x.rimusic.ui.items

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import it.fast4x.environment.Environment
import it.fast4x.rimusic.ui.components.themed.TextPlaceholder
import it.fast4x.rimusic.ui.styling.onOverlay
import it.fast4x.rimusic.ui.styling.overlay
import it.fast4x.rimusic.ui.styling.shimmer
import it.fast4x.rimusic.utils.color
import it.fast4x.rimusic.utils.conditional
import it.fast4x.rimusic.utils.medium
import it.fast4x.rimusic.utils.secondary
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.thumbnailShape
import it.fast4x.rimusic.typography

@Composable
fun VideoItem(
    video: Environment.VideoItem,
    thumbnailHeightDp: Dp,
    thumbnailWidthDp: Dp,
    modifier: Modifier = Modifier,
    disableScrollingText: Boolean
) {
    VideoItem(
        thumbnailUrl = video.thumbnail?.url,
        duration = video.durationText,
        title = video.info?.name,
        uploader = video.authors?.joinToString(", ") { it.name ?: "" },
        views = video.viewsText,
        thumbnailHeightDp = thumbnailHeightDp,
        thumbnailWidthDp = thumbnailWidthDp,
        modifier = modifier,
        disableScrollingText = disableScrollingText
    )
}

@Composable
fun VideoItem(
    thumbnailUrl: String?,
    duration: String?,
    title: String?,
    uploader: String?,
    views: String?,
    thumbnailHeightDp: Dp,
    thumbnailWidthDp: Dp,
    modifier: Modifier = Modifier,
    disableScrollingText: Boolean
) {
    ItemContainer(
        alternative = false,
        thumbnailSizeDp = 0.dp,
        modifier = modifier
    ) {
        Box {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(thumbnailShape())
                    .size(width = thumbnailWidthDp, height = thumbnailHeightDp)
            )

            duration?.let {
                BasicText(
                    text = duration,
                    style = typography().xxs.medium.color(colorPalette().onOverlay),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(all = 4.dp)
                        .background(
                            color = colorPalette().overlay,
                            shape = RoundedCornerShape(2.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .align(Alignment.BottomEnd)
                )
            }
        }

        ItemInfoContainer {
            BasicText(
                text = title ?: "",
                style = typography().xs.semiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .conditional(!disableScrollingText) { basicMarquee(iterations = Int.MAX_VALUE) }
            )

            BasicText(
                text = uploader ?: "",
                style = typography().xs.semiBold.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .conditional(!disableScrollingText) { basicMarquee(iterations = Int.MAX_VALUE) }
            )

            views?.let {
                BasicText(
                    text = views,
                    style = typography().xxs.medium.secondary,
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
fun VideoItemPlaceholder(
    thumbnailHeightDp: Dp,
    thumbnailWidthDp: Dp,
    modifier: Modifier = Modifier
) {
    ItemContainer(
        alternative = false,
        thumbnailSizeDp = 0.dp,
        modifier = modifier
    ) {
        Spacer(
            modifier = Modifier
                .background(color = colorPalette().shimmer, shape = thumbnailShape())
                .size(width = thumbnailWidthDp, height = thumbnailHeightDp)
        )

        ItemInfoContainer {
            TextPlaceholder()
            TextPlaceholder()
            TextPlaceholder(
                modifier = Modifier
                    .padding(top = 8.dp)
            )
        }
    }
}
