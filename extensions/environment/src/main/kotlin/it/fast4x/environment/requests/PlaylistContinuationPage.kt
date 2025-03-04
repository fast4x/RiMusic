package it.fast4x.environment.requests

import it.fast4x.environment.Environment


data class PlaylistContinuationPage(
    val songs: List<Environment.SongItem>,
    val continuation: String?,
)
