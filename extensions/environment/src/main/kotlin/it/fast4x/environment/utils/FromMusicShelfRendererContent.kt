package it.fast4x.environment.utils

import it.fast4x.environment.Environment
import it.fast4x.environment.models.MusicShelfRenderer
import it.fast4x.environment.models.NavigationEndpoint

fun Environment.SongItem.Companion.from(content: MusicShelfRenderer.Content): Environment.SongItem? {
    val (mainRuns, otherRuns) = content.runs

    // Possible configurations:
    // "song" • author(s) • album • duration
    // "song" • author(s) • duration
    // author(s) • album • duration
    // author(s) • duration

    val album: Environment.Info<NavigationEndpoint.Endpoint.Browse>? = otherRuns
        .getOrNull(otherRuns.lastIndex - 1)
        ?.firstOrNull()
        ?.takeIf { run ->
            run
                .navigationEndpoint
                ?.browseEndpoint
                ?.type == "MUSIC_PAGE_TYPE_ALBUM"
        }
        ?.let(Environment::Info)

    val isExplicit = content.musicResponsiveListItemRenderer
                                      ?.badges
                                      ?.any {
                                          it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                                      } ?: false

    return Environment.SongItem(
        info = mainRuns
            .firstOrNull()
            ?.let(Environment::Info),
        authors = otherRuns
            .getOrNull(otherRuns.lastIndex - if (album == null) 1 else 2)
            ?.map(Environment::Info),
        album = album,
        durationText = otherRuns
            .lastOrNull()
            ?.firstOrNull()?.text,
        thumbnail = content
            .thumbnail,
        explicit = isExplicit
    ).takeIf { it.info?.endpoint?.videoId != null }
}

fun Environment.VideoItem.Companion.from(content: MusicShelfRenderer.Content): Environment.VideoItem? {
    val (mainRuns, otherRuns) = content.runs

    return runCatching {
        Environment.VideoItem(
            info = mainRuns
                .firstOrNull()
                ?.let(Environment::Info),
            authors = otherRuns
                .getOrNull(otherRuns.lastIndex - 2)
                ?.map(Environment::Info),
            viewsText = otherRuns
                .getOrNull(otherRuns.lastIndex - 1)
                ?.firstOrNull()
                ?.text,
            durationText = otherRuns
                .getOrNull(otherRuns.lastIndex)
                ?.firstOrNull()
                ?.text,
            thumbnail = content
                .thumbnail
        ).takeIf { it.info?.endpoint?.videoId != null }
    }.getOrNull()

}

fun Environment.AlbumItem.Companion.from(content: MusicShelfRenderer.Content): Environment.AlbumItem? {
    val (mainRuns, otherRuns) = content.runs

    return Environment.AlbumItem(
        info = Environment.Info(
            name = mainRuns
                .firstOrNull()
                ?.text,
            endpoint = content
                .musicResponsiveListItemRenderer
                ?.navigationEndpoint
                ?.browseEndpoint
        ),
        authors = otherRuns
            .getOrNull(otherRuns.lastIndex - 1)
            ?.map(Environment::Info),
        year = otherRuns
            .getOrNull(otherRuns.lastIndex)
            ?.firstOrNull()
            ?.text,
        thumbnail = content
            .thumbnail
    ).takeIf { it.info?.endpoint?.browseId != null }
}

fun Environment.ArtistItem.Companion.from(content: MusicShelfRenderer.Content): Environment.ArtistItem? {
    val (mainRuns, otherRuns) = content.runs

    return Environment.ArtistItem(
        info = Environment.Info(
            name = mainRuns
                .firstOrNull()
                ?.text,
            endpoint = content
                .musicResponsiveListItemRenderer
                ?.navigationEndpoint
                ?.browseEndpoint
        ),
        subscribersCountText = otherRuns
            .lastOrNull()
            ?.last()
            ?.text,
        thumbnail = content
            .thumbnail
    ).takeIf { it.info?.endpoint?.browseId != null }
}

fun Environment.PlaylistItem.Companion.from(content: MusicShelfRenderer.Content): Environment.PlaylistItem? {
    val (mainRuns, otherRuns) = content.runs

    return Environment.PlaylistItem(
        info = Environment.Info(
            name = mainRuns
                .firstOrNull()
                ?.text,
            endpoint = content
                .musicResponsiveListItemRenderer
                ?.navigationEndpoint
                ?.browseEndpoint
        ),
        channel = otherRuns
            .firstOrNull()
            ?.firstOrNull()
            ?.let(Environment::Info),
        songCount = otherRuns
            .lastOrNull()
            ?.firstOrNull()
            ?.text
            ?.split(' ')
            ?.firstOrNull()
            ?.toIntOrNull(),
        thumbnail = content
            .thumbnail,
        isEditable = false
    ).takeIf { it.info?.endpoint?.browseId != null }
}
