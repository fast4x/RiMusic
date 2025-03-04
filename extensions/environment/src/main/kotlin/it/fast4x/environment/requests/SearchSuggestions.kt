package it.fast4x.environment.requests

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import it.fast4x.environment.Environment
import it.fast4x.environment.Environment.getBestQuality
import it.fast4x.environment.models.GetSearchSuggestionsResponse
import it.fast4x.environment.models.MusicResponsiveListItemRenderer
import it.fast4x.environment.models.NavigationEndpoint
import it.fast4x.environment.models.SearchSuggestionsResponse
import it.fast4x.environment.models.bodies.SearchSuggestionsBody
import it.fast4x.environment.models.oddElements
import it.fast4x.environment.models.splitBySeparator
import it.fast4x.environment.utils.runCatchingNonCancellable

suspend fun Environment.searchSuggestions(body: SearchSuggestionsBody) = runCatchingNonCancellable {
    val response = client.post(_Uwjb1AiI8t) {
        setBody(body)
        mask("contents.searchSuggestionsSectionRenderer.contents.searchSuggestionRenderer.navigationEndpoint.searchEndpoint.query")
    }.body<SearchSuggestionsResponse>()

    response
        .contents
        ?.firstOrNull()
        ?.searchSuggestionsSectionRenderer
        ?.contents
        ?.mapNotNull { content ->
            content
                .searchSuggestionRenderer
                ?.navigationEndpoint
                ?.searchEndpoint
                ?.query
        }
}

suspend fun Environment.searchSuggestionsWithItems(body: SearchSuggestionsBody) = runCatchingNonCancellable {
    val response = client.post(_Uwjb1AiI8t) {
        setBody(body)
        //mask("contents.searchSuggestionsSectionRenderer.contents.searchSuggestionRenderer.navigationEndpoint.searchEndpoint.query")
    }.body<GetSearchSuggestionsResponse>()

    val queries = response.contents?.getOrNull(0)?.searchSuggestionsSectionRenderer?.contents?.mapNotNull { content ->
        content.searchSuggestionRenderer?.suggestion?.runs?.joinToString(separator = "") { it.text.toString() }
    }.orEmpty()

    val recommendedItems =
        response.contents?.getOrNull(1)?.searchSuggestionsSectionRenderer?.contents?.mapNotNull {
            it.musicResponsiveListItemRenderer?.let { renderer ->
                SearchSuggestionPage.fromMusicResponsiveListItemRenderer(renderer)
            }
        }.orEmpty()

    println("mediaItem Innertube.searchSuggestionsWithItems queries $queries")
    println("mediaItem Innertube.searchSuggestionsWithItems recommendedItems $recommendedItems")

    Environment.SearchSuggestions(
        queries = queries,
        recommendedSong = recommendedItems.filterIsInstance<Environment.SongItem>().firstOrNull(),
        recommendedPlaylist = recommendedItems.filterIsInstance<Environment.PlaylistItem>().firstOrNull(),
        recommendedAlbum = recommendedItems.filterIsInstance<Environment.AlbumItem>().firstOrNull(),
        recommendedArtist = recommendedItems.filterIsInstance<Environment.ArtistItem>().firstOrNull(),
        recommendedVideo = recommendedItems.filterIsInstance<Environment.VideoItem>().firstOrNull(),
    )

}

object SearchSuggestionPage {
    fun fromMusicResponsiveListItemRenderer(renderer: MusicResponsiveListItemRenderer): Environment.Item? {
        return when {
            /*
            renderer.isVideo -> {
                VideoItem(
                    id = renderer.playlistItemData?.videoId ?: return null,
                    title = renderer.flexColumns.firstOrNull()?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.text ?: return null,
                    thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.getThumbnailUrl() ?: return null,
                    thumbnails = renderer.thumbnail.musicThumbnailRenderer.thumbnail,
                    explicit = renderer.badges?.find {
                        it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                    } != null,
                    artists = renderer.flexColumns.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.splitBySeparator()
                        ?.getOrNull(1)?.oddElements()?.map {
                            Artist(
                                name = it.text,
                                id = it.navigationEndpoint?.browseEndpoint?.browseId
                            )
                        } ?: return null,
                    album = renderer.flexColumns.getOrNull(2)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.let {
                        Album(
                            name = it.text,
                            id = it.navigationEndpoint?.browseEndpoint?.browseId ?: return null
                        )
                    },
                    duration = null,
                    view = null
                )
            }
             */
            renderer.isSong -> {
                val explicitBadge = if (renderer.badges?.find {
                        it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                    } != null) "e:" else ""
                Environment.SongItem(
                    info = Environment.Info(
                        name = "${explicitBadge}${renderer.flexColumns.firstOrNull()?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.text}",
                        endpoint = renderer.navigationEndpoint?.endpoint as NavigationEndpoint.Endpoint.Watch
                    ),
                    authors = renderer.flexColumns.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.splitBySeparator()
                        ?.getOrNull(1)?.oddElements()?.map {
                            Environment.Info(
                                name = it.text,
                                endpoint = it.navigationEndpoint?.endpoint as NavigationEndpoint.Endpoint.Browse
                            )
                        } ?: return null,
                    album = renderer.flexColumns.getOrNull(2)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()
                        ?.let {
                            Environment.Info(
                                name = it.text,
                                endpoint = it.navigationEndpoint?.endpoint as NavigationEndpoint.Endpoint.Browse
                            )
                        },
                    durationText = null,
                    thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.getBestQuality()
                        ?: return null,
                    explicit = renderer.badges?.find {
                        it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                    } != null
                )
            }
            renderer.isArtist -> {
                Environment.ArtistItem(
                    info = Environment.Info(
                        name = renderer.flexColumns.firstOrNull()?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.text ?: return null,
                        endpoint = renderer.navigationEndpoint?.endpoint as NavigationEndpoint.Endpoint.Browse
                    ),
                    thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.getBestQuality()
                        ?: return null,
                    subscribersCountText = renderer.flexColumns.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.text
                )
            }
            renderer.isAlbum -> {
                val secondaryLine = renderer.flexColumns.getOrNull(1)
                    ?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.splitBySeparator() ?: return null
                Environment.AlbumItem(
                    info = Environment.Info(
                        name = renderer.flexColumns.firstOrNull()?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.text ?: return null,
                        endpoint = renderer.navigationEndpoint?.endpoint as NavigationEndpoint.Endpoint.Browse
                    ),
                    authors = renderer.flexColumns.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.splitBySeparator()
                        ?.getOrNull(1)?.oddElements()?.map {
                            Environment.Info(
                                name = it.text,
                                endpoint = it.navigationEndpoint?.endpoint as NavigationEndpoint.Endpoint.Browse
                            )
                        } ?: return null,
                    year = secondaryLine.lastOrNull()?.firstOrNull()?.text,
                    thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.getBestQuality()
                        ?: return null
                )
            }
            else -> null
        }
    }
}
