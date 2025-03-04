package it.fast4x.environment.utils

import it.fast4x.environment.Environment
import it.fast4x.environment.Environment.getBestQuality
import it.fast4x.environment.models.MusicResponsiveListItemRenderer
import it.fast4x.environment.models.NavigationEndpoint

fun Environment.SongItem.Companion.from(renderer: MusicResponsiveListItemRenderer): Environment.SongItem? {
    val albumId = renderer
        .flexColumns
        .getOrNull(2)
        ?.musicResponsiveListItemFlexColumnRenderer
        ?.text
        ?.runs
        ?.firstOrNull()
        ?.navigationEndpoint?.browseEndpoint?.browseId

    val albumRow = if (albumId == null) 3 else 2

    val explicitBadge = if (renderer
        .badges
        ?.find {
            it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
        } != null) "e:" else ""

    return Environment.SongItem(
        info = renderer
            .flexColumns
            .getOrNull(0)
            ?.musicResponsiveListItemFlexColumnRenderer
            ?.text
            ?.runs
            ?.getOrNull(0)
            ?.let {
                if (it.navigationEndpoint?.endpoint is NavigationEndpoint.Endpoint.Watch) Environment.Info(
                    name = "$explicitBadge${it.text}",
                    endpoint = it.navigationEndpoint.endpoint as NavigationEndpoint.Endpoint.Watch
                ) else null
            },
        authors = renderer
            .flexColumns
            .getOrNull(1)
            ?.musicResponsiveListItemFlexColumnRenderer
            ?.text
            ?.runs
            ?.map { Environment.Info(name = it.text, endpoint = it.navigationEndpoint?.endpoint) }
            ?.filterIsInstance<Environment.Info<NavigationEndpoint.Endpoint.Browse>>()
            ?.takeIf(List<Any>::isNotEmpty),
        durationText = renderer
            .fixedColumns
            ?.getOrNull(0)
            ?.musicResponsiveListItemFlexColumnRenderer
            ?.text
            ?.runs
            ?.getOrNull(0)
            ?.text,
        album = renderer
            .flexColumns
            .getOrNull(albumRow)
            ?.musicResponsiveListItemFlexColumnRenderer
            ?.text
            ?.runs
            ?.firstOrNull()
            ?.let(Environment::Info),
        thumbnail = renderer
            .thumbnail
            ?.musicThumbnailRenderer
            ?.thumbnail
            ?.thumbnails
            ?.getBestQuality(),
            //?.lastOrNull(),
        explicit = renderer
            .badges
            ?.find {
                it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
           } != null,
    )      .takeIf { it.info?.endpoint?.videoId != null }
}
