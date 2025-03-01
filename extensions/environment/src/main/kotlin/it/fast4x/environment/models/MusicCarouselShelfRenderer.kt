package it.fast4x.environment.models

import kotlinx.serialization.Serializable

@Serializable
data class MusicCarouselShelfRenderer(
    val header: Header?,
    val contents: List<Content>,
    val numItemsPerColumn: String?,
) {
    @Serializable
    data class Content(
        val musicTwoRowItemRenderer: MusicTwoRowItemRenderer?,
        val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer?,
        val musicNavigationButtonRenderer: MusicNavigationButtonRenderer? = null
    )

    @Serializable
    data class Header(
        val musicTwoRowItemRenderer: MusicTwoRowItemRenderer?,
        val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer?,
        val musicCarouselShelfBasicHeaderRenderer: MusicCarouselShelfBasicHeaderRenderer?
    ) {
        @Serializable
        data class MusicCarouselShelfBasicHeaderRenderer(
            val moreContentButton: MoreContentButton?,
            val title: Runs?,
            val strapline: Runs?,
            val thumbnail: ThumbnailRenderer?,
        ) {
            @Serializable
            data class MoreContentButton(
                val buttonRenderer: ButtonRenderer?
            )
        }
    }
}
