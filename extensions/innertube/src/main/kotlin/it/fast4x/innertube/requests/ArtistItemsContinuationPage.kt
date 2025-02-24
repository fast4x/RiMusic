package it.fast4x.innertube.requests

import it.fast4x.innertube.Innertube


data class ArtistItemsContinuationPage(
    val items: List<Innertube.Item>,
    val continuation: String?,
)
