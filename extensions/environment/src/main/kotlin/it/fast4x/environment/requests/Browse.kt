package it.fast4x.environment.requests

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import it.fast4x.environment.Environment
import it.fast4x.environment.models.BrowseResponse
import it.fast4x.environment.models.MusicTwoRowItemRenderer
import it.fast4x.environment.models.bodies.BrowseBodyWithLocale
import it.fast4x.environment.utils.from
import it.fast4x.environment.utils.runCatchingNonCancellable

suspend fun Environment.browse(body: BrowseBodyWithLocale) = runCatchingNonCancellable {
    val response = client.post(_3djbhqyLpE) {
        setBody(body)
    }.body<BrowseResponse>()

    BrowseResult(
        title = response.header?.musicImmersiveHeaderRenderer?.title?.text ?: response.header
            ?.musicDetailHeaderRenderer?.title?.text,
        items = response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
            ?.tabRenderer?.content?.sectionListRenderer?.contents?.mapNotNull { content ->
                when {
                    content.gridRenderer != null -> BrowseResult.Item(
                        title = content.gridRenderer.header?.gridHeaderRenderer?.title?.runs
                            ?.firstOrNull()?.text ?: return@mapNotNull null,
                        items = content.gridRenderer.items?.mapNotNull { it.musicTwoRowItemRenderer?.toItem() }
                            .orEmpty()
                    )

                    content.musicCarouselShelfRenderer != null -> BrowseResult.Item(
                        title = content.musicCarouselShelfRenderer.header?.musicCarouselShelfBasicHeaderRenderer
                            ?.title?.runs?.firstOrNull()?.text ?: return@mapNotNull null,
                        items = content.musicCarouselShelfRenderer.contents?.mapNotNull { it.musicTwoRowItemRenderer?.toItem() }
                            .orEmpty()
                    )

                    else -> null
                }
            }.orEmpty()
    )
}

data class BrowseResult(
    val title: String?,
    val items: List<Item>
) {
    data class Item(
        val title: String,
        val items: List<Environment.Item>
    )
}

fun MusicTwoRowItemRenderer.toItem() = when {
    isAlbum -> Environment.AlbumItem.from(this)
    isPlaylist -> Environment.PlaylistItem.from(this)
    isArtist -> Environment.ArtistItem.from(this)
    else -> null
}