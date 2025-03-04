package it.fast4x.environment.utils

import it.fast4x.environment.Environment
import it.fast4x.environment.models.MusicTwoRowItemRenderer

fun Environment.VideoItem.Companion.from(renderer: MusicTwoRowItemRenderer): Environment.VideoItem? {
    return Environment.VideoItem(
        info = renderer
            .title
            ?.runs
            ?.firstOrNull()
            ?.let(Environment::Info),
        authors = null,
        thumbnail = renderer
            .thumbnailRenderer
            ?.musicThumbnailRenderer
            ?.thumbnail
            ?.thumbnails
            ?.firstOrNull(),
        durationText = null,
        viewsText = null

    ).takeIf { it.info?.endpoint?.videoId != null }
}

fun Environment.AlbumItem.Companion.from(renderer: MusicTwoRowItemRenderer): Environment.AlbumItem? {
    return Environment.AlbumItem(
        info = renderer
            .title
            ?.runs
            ?.firstOrNull()
            ?.let(Environment::Info),
        authors = null,
        year = renderer
            .subtitle
            ?.runs
            ?.lastOrNull()
            ?.text,
        thumbnail = renderer
            .thumbnailRenderer
            ?.musicThumbnailRenderer
            ?.thumbnail
            ?.thumbnails
            ?.firstOrNull()
    ).takeIf { it.info?.endpoint?.browseId != null }
}

fun Environment.ArtistItem.Companion.from(renderer: MusicTwoRowItemRenderer): Environment.ArtistItem? {
    return Environment.ArtistItem(
        info = renderer
            .title
            ?.runs
            ?.firstOrNull()
            ?.let(Environment::Info),
        subscribersCountText = renderer
            .subtitle
            ?.runs
            ?.firstOrNull()
            ?.text,
        thumbnail = renderer
            .thumbnailRenderer
            ?.musicThumbnailRenderer
            ?.thumbnail
            ?.thumbnails
            ?.firstOrNull()
    ).takeIf { it.info?.endpoint?.browseId != null }
}

fun Environment.PlaylistItem.Companion.from(renderer: MusicTwoRowItemRenderer): Environment.PlaylistItem? {
    return Environment.PlaylistItem(
        info = renderer
            .title
            ?.runs
            ?.firstOrNull()
            ?.let(Environment::Info),
        channel = renderer
            .subtitle
            ?.runs
            ?.getOrNull(2)
            ?.let(Environment::Info),
        songCount = renderer
            .subtitle
            ?.runs
            ?.getOrNull(4)
            ?.text
            ?.split(' ')
            ?.firstOrNull()
            ?.toIntOrNull(),
        thumbnail = renderer
            .thumbnailRenderer
            ?.musicThumbnailRenderer
            ?.thumbnail
            ?.thumbnails
            ?.firstOrNull(),
        isEditable = false
    ).takeIf { it.info?.endpoint?.browseId != null }
}
