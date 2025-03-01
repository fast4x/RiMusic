package it.fast4x.environment.requests

import it.fast4x.environment.Environment
import it.fast4x.environment.Environment.getBestQuality
import it.fast4x.environment.models.MusicResponsiveListItemRenderer
import it.fast4x.environment.models.MusicShelfRenderer
import it.fast4x.environment.models.NavigationEndpoint
import it.fast4x.environment.models.oddElements

data class HistoryPage(
    val sections: List<HistorySection>?,
) {
    data class HistorySection(
        val title: String,
        val songs: List<Environment.SongItem>
    )

    companion object {
        fun fromMusicShelfRenderer(renderer: MusicShelfRenderer): HistorySection {

            println("getHistory() fromMusicShelfRenderer songs: ${renderer.contents?.map {
                it.musicResponsiveListItemRenderer?.let { it1 ->
                    fromMusicResponsiveListItemRenderer(
                        it1
                    )
                }
            }}")

            return HistorySection(
                title = renderer.title?.runs?.firstOrNull()?.text!!,
                songs = renderer.contents?.mapNotNull {
                    it.musicResponsiveListItemRenderer?.let { it1 ->
                        fromMusicResponsiveListItemRenderer(
                            it1
                        )
                    }
                }!!
            )
        }

        private fun fromMusicResponsiveListItemRenderer(renderer: MusicResponsiveListItemRenderer): Environment.SongItem {
            println("getHistory() fromMusicResponsiveListItemRenderer: ${renderer.flexColumns}")
            return Environment.SongItem(
                info = Environment.Info(
                    name = renderer.flexColumns.firstOrNull()
                        ?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()
                        ?.text,
                    endpoint = NavigationEndpoint.Endpoint.Watch(
                        videoId = renderer.playlistItemData?.videoId
                    )
                ),
                authors = renderer.flexColumns.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.oddElements()
                    ?.map {
                        Environment.Info(
                            name = it.text,
                            endpoint = it.navigationEndpoint?.browseEndpoint
                        )
                    } ?: emptyList(),
                album = renderer.flexColumns.getOrNull(3)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()
                    ?.let {
                        Environment.Info(
                            name = it.text,
                            endpoint = it.navigationEndpoint?.browseEndpoint
                        )
                    },
                durationText = renderer.fixedColumns?.firstOrNull()?.musicResponsiveListItemFlexColumnRenderer
                    ?.text?.runs?.firstOrNull()?.text,
                thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.getBestQuality(),
                explicit = renderer.badges?.find {
                    it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                } != null,
//                endpoint = renderer.overlay?.musicItemThumbnailOverlayRenderer?.content
//                    ?.musicPlayButtonRenderer?.playNavigationEndpoint?.watchEndpoint
            )
        }
    }
}
