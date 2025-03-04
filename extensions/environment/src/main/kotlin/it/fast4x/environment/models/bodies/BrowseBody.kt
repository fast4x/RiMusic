package it.fast4x.environment.models.bodies

import it.fast4x.environment.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class BrowseBody(
    val context: Context = Context.DefaultWeb,
    val browseId: String?,
    val params: String? = null,
)
