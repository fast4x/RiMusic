package it.fast4x.rimusic.enums

import it.fast4x.rimusic.R
import me.knighthat.appContext

enum class QueueSwipeAction {
    NoAction,
    PlayNext,
    Download,
    RemoveFromQueue;

    val displayName: String
        get() = when (this) {
            NoAction -> appContext().resources.getString(R.string.none)
            PlayNext -> appContext().resources.getString(R.string.play_next)
            Download  -> appContext().resources.getString(R.string.download)
            RemoveFromQueue  -> appContext().resources.getString(R.string.remove_from_queue)
        }

    val icon: Int
        get() = when (this) {
            NoAction -> R.drawable.alert
            PlayNext -> R.drawable.play_skip_forward
            Download -> R.drawable.download
            RemoveFromQueue -> R.drawable.trash
        }
}
