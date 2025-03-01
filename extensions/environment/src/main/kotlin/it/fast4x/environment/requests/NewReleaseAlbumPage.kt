package it.fast4x.environment.requests

import it.fast4x.environment.Environment
import it.fast4x.environment.Environment.getBestQuality
import it.fast4x.environment.models.MusicTwoRowItemRenderer
import it.fast4x.environment.models.oddElements
import it.fast4x.environment.models.splitBySeparator

object NewReleaseAlbumPage {
    fun fromMusicTwoRowItemRenderer(renderer: MusicTwoRowItemRenderer): Environment.AlbumItem {
        println("otherVersions NewReleaseAlbumPage fromMusicTwoRowItemRenderer: ${renderer.navigationEndpoint?.browseEndpoint}")
        return Environment.AlbumItem(
            info = Environment.Info(
                name = renderer.title?.runs?.firstOrNull()?.text ?: "",
                endpoint = renderer.navigationEndpoint?.browseEndpoint
            ),
//            playlistId = renderer.thumbnailOverlay
//                ?.musicItemThumbnailOverlayRenderer?.content
//                ?.musicPlayButtonRenderer?.playNavigationEndpoint
//                ?.watchPlaylistEndpoint?.playlistId ?: return null,
            authors = renderer.subtitle?.runs?.splitBySeparator()?.getOrNull(1)?.oddElements()
                ?.map {
                    Environment.Info(
                        name = it.text,
                        endpoint = it.navigationEndpoint?.browseEndpoint
                    )
                } ?: emptyList(),
            year = renderer.subtitle?.runs?.lastOrNull()?.text,
            thumbnail = renderer.thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails?.getBestQuality(),
//            explicit = renderer.subtitleBadges?.find {
//                it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
//            } != null
        )
    }
}
