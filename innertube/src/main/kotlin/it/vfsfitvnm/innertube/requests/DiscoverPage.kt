package it.vfsfitvnm.innertube.requests

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import it.vfsfitvnm.innertube.Innertube
import it.vfsfitvnm.innertube.models.BrowseResponse
import it.vfsfitvnm.innertube.models.MusicTwoRowItemRenderer
import it.vfsfitvnm.innertube.models.bodies.BrowseBody
import it.vfsfitvnm.innertube.models.bodies.BrowseBodyWithLocale
import it.vfsfitvnm.innertube.models.oddElements
import it.vfsfitvnm.innertube.models.splitBySeparator
import it.vfsfitvnm.innertube.utils.runCatchingNonCancellable

suspend fun Innertube.discoverPage() = runCatching {
    val response = client.post(browse) {
        setBody(BrowseBodyWithLocale(browseId = "FEmusic_explore"))
        mask("contents")
    }.body<BrowseResponse>()

    Innertube.DiscoverPage(
        newReleaseAlbums = response.contents?.singleColumnBrowseResultsRenderer?.tabs
            ?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents?.find {
                it.musicCarouselShelfRenderer?.header?.musicCarouselShelfBasicHeaderRenderer
                    ?.moreContentButton?.buttonRenderer?.navigationEndpoint?.browseEndpoint?.browseId == "FEmusic_new_releases_albums"
            }?.musicCarouselShelfRenderer?.contents?.mapNotNull { it.musicTwoRowItemRenderer?.toNewReleaseAlbumPage() }
            .orEmpty(),
        moods = response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
            ?.tabRenderer?.content?.sectionListRenderer?.contents?.find {
                it.musicCarouselShelfRenderer?.header?.musicCarouselShelfBasicHeaderRenderer
                    ?.moreContentButton?.buttonRenderer?.navigationEndpoint?.browseEndpoint?.browseId == "FEmusic_moods_and_genres"
            }?.musicCarouselShelfRenderer?.contents?.mapNotNull { it.musicNavigationButtonRenderer?.toMood() }
            .orEmpty()
    )
}

suspend fun Innertube.discoverPageNewAlbums() = runCatching {
    val response = client.post(browse) {
        setBody(BrowseBodyWithLocale(browseId = "FEmusic_explore"))
        mask("contents")
    }.body<BrowseResponse>()

    Innertube.DiscoverPageAlbums(
        newReleaseAlbums = response.contents?.singleColumnBrowseResultsRenderer?.tabs
            ?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents?.find {
                it.musicCarouselShelfRenderer?.header?.musicCarouselShelfBasicHeaderRenderer
                    ?.moreContentButton?.buttonRenderer?.navigationEndpoint?.browseEndpoint?.browseId == "FEmusic_new_releases_albums"
            }?.musicCarouselShelfRenderer?.contents?.mapNotNull { it.musicTwoRowItemRenderer?.toNewReleaseAlbumPage() }
            .orEmpty()
    )
}

suspend fun Innertube.discoverPageNewAlbumsComplete() = runCatching {
    val response = client.post(browse) {
        setBody(BrowseBodyWithLocale(browseId = "FEmusic_new_releases_albums"))
        mask("contents")
    }.body<BrowseResponse>()

    Innertube.DiscoverPageAlbums(
        newReleaseAlbums = response.contents?.singleColumnBrowseResultsRenderer?.tabs
            ?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()?.gridRenderer?.items?.mapNotNull { it.musicTwoRowItemRenderer?.toNewReleaseAlbumPage() }
            .orEmpty()
    )
}

fun MusicTwoRowItemRenderer.toNewReleaseAlbumPage() = Innertube.AlbumItem(
    info = Innertube.Info(
        name = title?.text,
        endpoint = navigationEndpoint?.browseEndpoint
    ),
    authors = subtitle?.runs?.splitBySeparator()?.getOrNull(1)?.oddElements()?.map {
        Innertube.Info(
            name = it.text,
            endpoint = it.navigationEndpoint?.browseEndpoint
        )
    },
    year = subtitle?.runs?.lastOrNull()?.text,
    thumbnail = thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails?.firstOrNull()
)