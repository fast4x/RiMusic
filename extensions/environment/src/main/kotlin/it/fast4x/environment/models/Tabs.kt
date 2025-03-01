package it.fast4x.environment.models

import kotlinx.serialization.Serializable

@Serializable
data class Tabs(
    val tabs: List<Tab>?
) {
    @Serializable
    data class Tab(
        val tabRenderer: TabRenderer?
    ) {
        @Serializable
        data class TabRenderer(
            val content: Content?,
            val title: String?,
            val tabIdentifier: String?,
        ) {
            @Serializable
            data class Content(
                val sectionListRenderer: SectionListRenderer?,
            )
        }
    }
}

@Serializable
data class TwoColumnBrowseResultsRenderer(
    val tabs: List<Tabs.Tab>?,
    val secondaryContents: Tabs.Tab.TabRenderer.Content?
)