package it.fast4x.rimusic.utils

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import it.fast4x.environment.Environment
import it.fast4x.environment.models.bodies.ContinuationBody
import it.fast4x.environment.models.bodies.NextBody
import it.fast4x.environment.requests.nextPage
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.PopupType
import it.fast4x.rimusic.service.modern.PlayerServiceModern
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

data class YouTubeRadio @OptIn(UnstableApi::class) constructor
    (
    private val videoId: String? = null,
    private var playlistId: String? = null,
    private var playlistSetVideoId: String? = null,
    private var parameters: String? = null,
    private val isDiscoverEnabled: Boolean = false,
    private val context: Context,
    private val binder: PlayerServiceModern.Binder? = null,
    private val coroutineScope: CoroutineScope
) {
    private var nextContinuation: String? = null

    @OptIn(UnstableApi::class) suspend fun process(): List<MediaItem> {
        var mediaItems: List<MediaItem>? = null

        nextContinuation = withContext(Dispatchers.IO) {
            val continuation = nextContinuation

            if (continuation == null) {
                Environment.nextPage(
                    NextBody(
                        videoId = videoId,
                        playlistId = playlistId,
                        params = parameters,
                        playlistSetVideoId = playlistSetVideoId
                    )
                )?.map { nextResult ->
                    playlistId = nextResult.playlistId
                    parameters = nextResult.params
                    playlistSetVideoId = nextResult.playlistSetVideoId

                    nextResult.itemsPage
                }
            } else {
                Environment.nextPage(ContinuationBody(continuation = continuation))
            }?.getOrNull()?.let { songsPage ->
                mediaItems = songsPage.items?.map(Environment.SongItem::asMediaItem)
                songsPage.continuation?.takeUnless { nextContinuation == it }
            }

        }
            //coroutineScope.launch(Dispatchers.Main) {

        fun songsInQueue(mediaId: String): String? {
            var mediaIdFound = false
            runBlocking {
                withContext(Dispatchers.Main) {
                    for (i in 0 until (binder?.player?.mediaItemCount ?: 0) - 1) {
                        if (mediaId == binder?.player?.getMediaItemAt(i)?.mediaId) {
                            mediaIdFound = true
                            return@withContext
                        }
                    }
                }
            }
            if(mediaIdFound){
                return mediaId
            }
            return null
        }


            if (isDiscoverEnabled) {
                var listMediaItems = mutableListOf<MediaItem>()
                withContext(Dispatchers.IO) {
                    mediaItems?.forEach {
                        val songInPlaylist = Database.songUsedInPlaylists(it.mediaId)
                        val songIsLiked = (Database.getLikedAt(it.mediaId) !in listOf(-1L,null))
                        val sIQ = songsInQueue(it.mediaId)
                        if (songInPlaylist == 0 && !songIsLiked && (it.mediaId != sIQ)) {
                            listMediaItems.add(it)
                        }
                    }
                }

                SmartMessage(
                    context.resources.getString(R.string.discover_has_been_applied_to_radio).format(
                        mediaItems?.size?.minus(listMediaItems.size) ?: 0
                    ), PopupType.Success, context = context
                )

                mediaItems = listMediaItems
            }

        withContext(Dispatchers.IO) {
            mediaItems = mediaItems?.filter {
                (Database.getLikedAt(it.mediaId) != -1L)
            }?.distinct()
        }

        return mediaItems ?: emptyList()
    }
}
