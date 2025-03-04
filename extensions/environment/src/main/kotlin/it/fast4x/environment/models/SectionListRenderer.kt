package it.fast4x.environment.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class SectionListRenderer(
    val header: Header?,
    val contents: List<Content>?,
    val continuations: List<Continuation>?
) {
    @Serializable
    data class Header(
        val chipCloudRenderer: ChipCloudRenderer?,
    ) {
        @Serializable
        data class ChipCloudRenderer(
            val chips: List<Chip>,
        ) {
            @Serializable
            data class Chip(
                val chipCloudChipRenderer: ChipCloudChipRenderer,
            ) {
                @Serializable
                data class ChipCloudChipRenderer(
                    val isSelected: Boolean,
                    val navigationEndpoint: NavigationEndpoint,
                    // The close button doesn't have the following two fields
                    val text: Runs?,
                    val uniqueId: String?,
                )
            }
        }
    }

    @Serializable
    data class Content(
        @JsonNames("musicImmersiveCarouselShelfRenderer")
        val musicCarouselShelfRenderer: MusicCarouselShelfRenderer?,
        @JsonNames("musicPlaylistShelfRenderer")
        val musicShelfRenderer: MusicShelfRenderer?,
        val gridRenderer: GridRenderer?,
        val musicDescriptionShelfRenderer: MusicDescriptionShelfRenderer?,
        //val musicResponsiveHeaderRenderer: MusicResponsiveHeaderRenderer?,
        val musicPlaylistShelfRenderer: MusicPlaylistShelfRenderer?,
        val musicEditablePlaylistDetailHeaderRenderer: MusicEditablePlaylistDetailHeaderRenderer?,
        val musicResponsiveHeaderRenderer: MusicResponsiveHeaderRenderer?,
    ) {

        @Serializable
        data class MusicDescriptionShelfRenderer(
            val description: Runs?,
        )

//        @Serializable
//        data class MusicResponsiveHeaderRenderer(
//            val title: Runs?,
//            //val description: MusicDescriptionShelfRenderer?,
//            val description: Description?,
//            val subtitle: Runs?,
//            val secondSubtitle: Runs?,
//            val thumbnail: ThumbnailRenderer?,
//            val straplineTextOne: Runs?,
//        ) {
//            @Serializable
//            data class Description(
//                val musicDescriptionShelfRenderer: MusicDescriptionShelfRenderer?,
//            )
//        }
    }

}
