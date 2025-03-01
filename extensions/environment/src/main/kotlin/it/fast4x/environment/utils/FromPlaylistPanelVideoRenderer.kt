package it.fast4x.environment.utils

import it.fast4x.environment.Environment
import it.fast4x.environment.models.PlaylistPanelVideoRenderer

fun Environment.SongItem.Companion.from(renderer: PlaylistPanelVideoRenderer): Environment.SongItem? {
    return Environment.SongItem(
        info = Environment.Info(
            name = renderer
                .title
                ?.text,
            endpoint = renderer
                .navigationEndpoint
                ?.watchEndpoint
        ),
        authors = renderer
            .longBylineText
            ?.splitBySeparator()
            ?.getOrNull(0)
            ?.map(Environment::Info),
        album = renderer
            .longBylineText
            ?.splitBySeparator()
            ?.getOrNull(1)
            ?.getOrNull(0)
            ?.let(Environment::Info),
        thumbnail = renderer
            .thumbnail
            ?.thumbnails
            ?.getOrNull(0),
        durationText = renderer
            .lengthText
            ?.text
    ).takeIf { it.info?.endpoint?.videoId != null }
}
