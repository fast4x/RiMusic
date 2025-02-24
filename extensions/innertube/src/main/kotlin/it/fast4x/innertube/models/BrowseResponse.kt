package it.fast4x.innertube.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class BrowseResponse(
    val contents: Contents?,
    val continuationContents: ContinuationContents?,
    val header: Header?,
    val microformat: Microformat?,
    val responseContext: ResponseContext?,
    val background: ThumbnailRenderer?,
    val onResponseReceivedActions: List<OnResponseReceivedAction>?
) {
    @Serializable
    data class OnResponseReceivedAction (
        val clickTrackingParams: String?,
        val appendContinuationItemsAction: AppendContinuationItemsAction?
    )
    @Serializable
    data class AppendContinuationItemsAction (
        val continuationItems: List<ContinuationItem>?,
    )
    @Serializable
    data class ContinuationItem (
        val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer? = null,
        val continuationItemRenderer: ContinuationItemRenderer? = null
    )
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
    data class Contents(
        val singleColumnBrowseResultsRenderer: Tabs?,
        val twoColumnBrowseResultsRenderer: TwoColumnBrowseResultsRenderer?,
        val sectionListRenderer: SectionListRenderer?,
    )

    @Serializable
    data class Header @OptIn(ExperimentalSerializationApi::class) constructor(
        @JsonNames("musicVisualHeaderRenderer")
        val musicImmersiveHeaderRenderer: MusicImmersiveHeaderRenderer?,
        val musicDetailHeaderRenderer: MusicDetailHeaderRenderer?,
        val musicVisualHeaderRenderer: MusicVisualHeaderRenderer?,
        val musicHeaderRenderer: MusicHeaderRenderer?,
        val musicEditablePlaylistDetailHeaderRenderer: MusicEditablePlaylistDetailHeaderRenderer?,
    ) {
//        @Serializable
//        data class MusicDetailHeaderRenderer(
//            val title: Runs?,
//            val description: Runs?,
//            val subtitle: Runs?,
//            val secondSubtitle: Runs?,
//            val thumbnail: ThumbnailRenderer?,
//        )

        @Serializable
        data class MusicVisualHeaderRenderer(
            val title: Runs?,
            val foregroundThumbnail: ThumbnailRenderer?,
            val thumbnail: ThumbnailRenderer?,
        )

        @Serializable
        data class MusicImmersiveHeaderRenderer(
            val description: Runs?,
            val playButton: PlayButton?,
            val startRadioButton: StartRadioButton?,
            val thumbnail: ThumbnailRenderer?,
            val foregroundThumbnail: ThumbnailRenderer?,
            val title: Runs?,
            val subscriptionButton: SubscriptionButton?
        ) {
            @Serializable
            data class PlayButton(
                val buttonRenderer: ButtonRenderer?
            )

            @Serializable
            data class StartRadioButton(
                val buttonRenderer: ButtonRenderer?
            )

        }

        @Serializable
        data class Buttons(
            val menuRenderer: Menu.MenuRenderer?,
        )

        @Serializable
        data class MusicThumbnail(
            val url: String?,
        )

        @Serializable
        data class MusicThumbnailRenderer(
            val musicThumbnailRenderer: MusicThumbnailRenderer?,
            val thumbnails: List<MusicThumbnail>?,
        )

        @Serializable
        data class MusicHeaderRenderer(
            val buttons: List<Buttons>?,
            val title: Runs?,
            val thumbnail: MusicThumbnailRenderer?,
            val subtitle: Runs?,
            val secondSubtitle: Runs?,
            val straplineTextOne: Runs?,
            val straplineThumbnail: MusicThumbnailRenderer?,
        )
    }

    @Serializable
    data class Microformat(
        val microformatDataRenderer: MicroformatDataRenderer?
    ) {
        @Serializable
        data class MicroformatDataRenderer(
            val urlCanonical: String?
        )
    }

    @Serializable
    data class ContinuationContents(
        val sectionListContinuation: SectionListContinuation?,
        val musicPlaylistShelfContinuation: MusicPlaylistShelfContinuation?,
        val gridContinuation: GridRenderer?,
        val musicShelfContinuation: MusicShelfRenderer?,
    ) {
        @Serializable
        data class SectionListContinuation(
            val contents: List<SectionListRenderer.Content>?,
            val continuations: List<Continuation>?,
        )

        @Serializable
        data class MusicPlaylistShelfContinuation(
            val contents: List<MusicShelfRenderer.Content>?,
            val continuations: List<Continuation>?,
        )
    }

//    @Serializable
//    data class ResponseContext(
//        val visitorData: String?,
//        val serviceTrackingParams: List<ServiceTrackingParam>?,
//    ) {
//        @Serializable
//        data class ServiceTrackingParam(
//            val params: List<Param>?,
//            val service: String?,
//        ) {
//            @Serializable
//            data class Param(
//                val key: String?,
//                val value: String?,
//            )
//        }
//    }
}
