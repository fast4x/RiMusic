package it.fast4x.environment.models.bodies

import it.fast4x.environment.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class PlayerBody(
    val context: Context = Context.DefaultWeb,
    val videoId: String,
    val playlistId: String? = null,
    val contentCheckOk: Boolean = true,
    val racyCheckOk: Boolean = true,
    val playbackContext: PlaybackContext? = null,
    val cpn: String? = "dPK7AEPTvFz8geNI",
    val params: String? = null,
    val serviceIntegrityDimensions: ServiceIntegrityDimensions? = ServiceIntegrityDimensions(),
) {
    @Serializable
    data class ServiceIntegrityDimensions(
        val poToken: String =
            "Mlt6vqPMnRAc93qGSJr4d9wyzWNClpcDwVQGZ7ooTJoc6IjxwPaMoyTMXRkU5OHQQvLdQqF4v9W_U6JRCUmCPatLIOlbBqjasxsmO3PnigwoLSQ81o0MpFeX8nJA",
    )
    @Serializable
    data class PlaybackContext(
        val contentPlaybackContext: ContentPlaybackContext = ContentPlaybackContext(),
    ) {
        @Serializable
        data class ContentPlaybackContext(
            val html5Preference: String = "HTML5_PREF_WANTS",
            val signatureTimestamp: Int = 20110,
        )
    }
}
