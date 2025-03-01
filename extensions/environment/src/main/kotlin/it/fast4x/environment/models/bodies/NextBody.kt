package it.fast4x.environment.models.bodies

import it.fast4x.environment.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class NextBody(
    val context: Context = Context.DefaultWeb,
    val videoId: String?,
    val isAudioOnly: Boolean = true,
    val playlistId: String? = null,
    val tunerSettingValue: String = "AUTOMIX_SETTING_NORMAL",
    val index: Int? = null,
    val params: String? = null,
    val playlistSetVideoId: String? = null,
    val watchEndpointMusicSupportedConfigs: WatchEndpointMusicSupportedConfigs = WatchEndpointMusicSupportedConfigs(
        musicVideoType = "MUSIC_VIDEO_TYPE_ATV"
        //musicVideoType = "MUSIC_VIDEO_TYPE_OMV"
    ),
    val continuation: String? = null,
) {
    @Serializable
    data class WatchEndpointMusicSupportedConfigs(
        val musicVideoType: String
    )
}
