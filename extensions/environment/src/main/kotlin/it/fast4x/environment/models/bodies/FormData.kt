package it.fast4x.environment.models.bodies

import kotlinx.serialization.Serializable

@Serializable
data class FormData (
    val selectedValues: List<String> = listOf("ZZ")
)