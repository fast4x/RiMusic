package it.fast4x.environment.models.bodies

import it.fast4x.environment.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class EditPlaylistBody(
    val context: Context,
    val playlistId: String,
    val actions: List<Action>
)

@Serializable
sealed class Action {
    @Serializable
    data class AddVideoAction(
        val action: String = "ACTION_ADD_VIDEO",
        val addedVideoId: String,
        val dedupeOption: String = "DEDUPE_OPTION_DROP_DUPLICATE"
    ) : Action()

    @Serializable
    data class AddPlaylistAction(
        val action: String = "ACTION_ADD_PLAYLIST",
        val addedFullListId: String,
        val dedupeOption: String = "DEDUPE_OPTION_DROP_DUPLICATE"
    ) : Action()

    @Serializable
    data class MoveVideoAction(
        val action: String = "ACTION_MOVE_VIDEO_BEFORE",
        val setVideoId: String,
        val movedSetVideoIdSuccessor: String
    ) : Action()

    @Serializable
    data class RemoveVideoAction(
        val action: String = "ACTION_REMOVE_VIDEO",
        val setVideoId: String? = null,
        val removedVideoId: String
    ) : Action()

    @Serializable
    data class RenamePlaylistAction(
        val action: String = "ACTION_SET_PLAYLIST_NAME",
        val playlistName: String
    ) : Action()
}
