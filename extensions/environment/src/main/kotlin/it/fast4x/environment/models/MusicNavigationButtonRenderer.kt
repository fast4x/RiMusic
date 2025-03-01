package it.fast4x.environment.models

import kotlinx.serialization.Serializable

@Serializable
data class MusicNavigationButtonRenderer(
    val buttonText: Runs,
    val solid: Solid?,
    val iconStyle: IconStyle?,
    val clickCommand: NavigationEndpoint
) {
    @Serializable
    data class Solid(
        val leftStripeColor: Long
    )

    @Serializable
    data class IconStyle(
        val icon: Icon
    )

    @Serializable
    data class Icon(
        val iconType: String
    )
}
