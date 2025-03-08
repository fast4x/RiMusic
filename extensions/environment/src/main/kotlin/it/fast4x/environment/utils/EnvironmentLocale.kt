package it.fast4x.environment.utils

import kotlinx.serialization.Serializable

@Serializable
data class EnvironmentLocale(
    val gl: String, // geolocation
    val hl: String, // host language
)
