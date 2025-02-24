package it.fast4x.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class MusicShelfRenderer(
    val bottomEndpoint: NavigationEndpoint?,
    val contents: List<Content>?,
    val continuations: List<Continuation>?,
    val title: Runs?
) {
    @Serializable
    data class Content(
        val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer?,
        val musicMultiRowListItemRenderer: MusicMultiRowListItemRenderer?,
        val continuationItemRenderer: ContinuationItemRenderer?,
    ) {
        @Serializable
        data class ContinuationItemRenderer (
            val trigger: String? = null,
            val continuationEndpoint: ContinuationEndpoint? = null
        )
        @Serializable
        data class ContinuationEndpoint (
            val clickTrackingParams: String? = null,
            val continuationCommand: ContinuationCommand? = null
        )
        @Serializable
        data class ContinuationCommand (
            val token: String? = null,
            val request: String? = null
        )

        @Serializable
        data class MusicMultiRowListItemRenderer(
            val description: Description?,
            val subtitle: Subtitle?,
            val title: Title?,
            val thumbnail: Thumbnail?,
            val onTap: OnTap?,
        ) {
            @Serializable
            data class Description(
                val runs: List<Run>?,
            )

            @Serializable
            data class Subtitle(
                val runs: List<Run>?,
            )

            @Serializable
            data class Title(
                val runs: List<Run>?,
            )

            @Serializable
            data class Thumbnail(
                val musicThumbnailRenderer: ThumbnailRenderer.MusicThumbnailRenderer?,
            )

            @Serializable
            data class OnTap(
                val watchEndpoint: WatchEndpoint?,
            )
        }

        val runs: Pair<List<Runs.Run>, List<List<Runs.Run>>>
            get() = (musicResponsiveListItemRenderer
                ?.flexColumns
                ?.firstOrNull()
                ?.musicResponsiveListItemFlexColumnRenderer
                ?.text
                ?.runs
                ?: emptyList()) to
                    (musicResponsiveListItemRenderer
                        ?.flexColumns
                        ?.getOrNull(1)
                        ?.musicResponsiveListItemFlexColumnRenderer
                        ?.text
                        ?.splitBySeparator()
                        ?: emptyList()
                            )

        val thumbnail: Thumbnail?
            get() = musicResponsiveListItemRenderer
                ?.thumbnail
                ?.musicThumbnailRenderer
                ?.thumbnail
                ?.thumbnails
                ?.firstOrNull()
    }
}

fun List<Continuation>.getContinuation() =
    firstOrNull()?.nextContinuationData?.continuation