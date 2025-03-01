package it.fast4x.environment.models

import kotlinx.serialization.Serializable

@Serializable
data class SearchResponse(
    val contents: Contents?,
) {
    @Serializable
    data class Contents(
        val tabbedSearchResultsRenderer: Tabs?
    )
}
