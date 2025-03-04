package it.fast4x.environment.models.bodies

import it.fast4x.environment.models.Context
import kotlinx.serialization.Serializable


@Serializable
data class ContinuationBodyWithLocale(
    val context: Context = Context.DefaultWebWithLocale,
    val continuation: String,
)
