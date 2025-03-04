package it.fast4x.environment.models

import kotlinx.serialization.Serializable

@Serializable
data class ButtonRenderer(
    val navigationEndpoint: NavigationEndpoint?
)
