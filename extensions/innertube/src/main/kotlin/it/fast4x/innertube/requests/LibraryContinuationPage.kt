package com.zionhuang.innertube.pages

import it.fast4x.innertube.Innertube

data class LibraryContinuationPage(
    val items: List<Innertube.Item>,
    val continuation: String?,
)