package it.fast4x.environment.models

import kotlinx.serialization.Serializable

@Serializable
data class Thumbnails(
    val thumbnails: List<Thumbnail>,
)