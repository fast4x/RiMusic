package com.zionhuang.innertube.pages

import it.fast4x.environment.Environment

data class LibraryContinuationPage(
    val items: List<Environment.Item>,
    val continuation: String?,
)