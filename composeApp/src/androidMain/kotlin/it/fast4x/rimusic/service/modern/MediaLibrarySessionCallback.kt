package it.fast4x.rimusic.service.modern

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import dagger.hilt.android.qualifiers.ApplicationContext
import it.fast4x.environment.Environment
import it.fast4x.environment.models.bodies.SearchBody
import it.fast4x.environment.requests.searchPage
import it.fast4x.environment.utils.from
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.R
import it.fast4x.rimusic.appContext
import it.fast4x.rimusic.cleanPrefix
import it.fast4x.rimusic.enums.MaxTopPlaylistItems
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.service.MyDownloadHelper
import it.fast4x.rimusic.service.modern.MediaSessionConstants.ID_CACHED
import it.fast4x.rimusic.service.modern.MediaSessionConstants.ID_DOWNLOADED
import it.fast4x.rimusic.service.modern.MediaSessionConstants.ID_FAVORITES
import it.fast4x.rimusic.service.modern.MediaSessionConstants.ID_ONDEVICE
import it.fast4x.rimusic.service.modern.MediaSessionConstants.ID_TOP
import it.fast4x.rimusic.utils.MaxTopPlaylistItemsKey
import it.fast4x.rimusic.utils.asSong
import it.fast4x.rimusic.utils.getEnum
import it.fast4x.rimusic.utils.persistentQueueKey
import it.fast4x.rimusic.utils.preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.guava.future
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@UnstableApi
class MediaLibrarySessionCallback (
    val context: Context,
    val database: Database,
    val downloadHelper: MyDownloadHelper
) : MediaLibrarySession.Callback {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    lateinit var binder: PlayerServiceModern.Binder
    var toggleLike: () -> Unit = {}
    var toggleDownload: () -> Unit = {}
    var toggleRepeat: () -> Unit = {}
    var toggleShuffle: () -> Unit = {}
    var startRadio: () -> Unit = {}
    var callPause: () -> Unit = {}
    var actionSearch: () -> Unit = {}
    var searchedSongs: List<Song> = emptyList()

    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        println("PlayerServiceModern MediaLibrarySessionCallback.onConnect")
        val connectionResult = super.onConnect(session, controller)
        return MediaSession.ConnectionResult.accept(
            connectionResult.availableSessionCommands.buildUpon()
                .add(MediaSessionConstants.CommandToggleDownload)
                .add(MediaSessionConstants.CommandToggleLike)
                .add(MediaSessionConstants.CommandToggleShuffle)
                .add(MediaSessionConstants.CommandToggleRepeatMode)
                .add(MediaSessionConstants.CommandStartRadio)
                .add(MediaSessionConstants.CommandSearch)
                .build(),
            connectionResult.availablePlayerCommands
        )
    }

    override fun onSearch(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<Void>> {
        println("PlayerServiceModern MediaLibrarySessionCallback.onSearch: $query")
        session.notifySearchResultChanged(browser, query, 0, params)
        return Futures.immediateFuture(LibraryResult.ofVoid(params))
    }

    override fun onGetSearchResult(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        page: Int,
        pageSize: Int,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        println("PlayerServiceModern MediaLibrarySessionCallback.onGetSearchResult: $query")
        runBlocking(Dispatchers.IO) {
            searchedSongs = Environment.searchPage(
                body = SearchBody(
                    query = query,
                    params = Environment.SearchFilter.Song.value
                ),
                fromMusicShelfRendererContent = Environment.SongItem.Companion::from
            )?.map {
                it?.items?.map { it.asSong }
            }?.getOrNull() ?: emptyList()

            val resultList = searchedSongs.map {
                it.toMediaItem(PlayerServiceModern.SEARCHED)
            }
            return@runBlocking Futures.immediateFuture(LibraryResult.ofItemList(resultList, params))
        }

        return Futures.immediateFuture(LibraryResult.ofItemList(searchedSongs.map {
            it.toMediaItem(
                PlayerServiceModern.SEARCHED
            )
        }, params))
    }

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle,
    ): ListenableFuture<SessionResult> {
        println("PlayerServiceModern MediaLibrarySessionCallback.onCustomCommand: ${customCommand.customAction}")
        when (customCommand.customAction) {
            MediaSessionConstants.ACTION_TOGGLE_LIKE -> toggleLike()
            MediaSessionConstants.ACTION_TOGGLE_DOWNLOAD -> toggleDownload()
            MediaSessionConstants.ACTION_TOGGLE_SHUFFLE -> toggleShuffle()
            MediaSessionConstants.ACTION_TOGGLE_REPEAT_MODE -> toggleRepeat()
            MediaSessionConstants.ACTION_START_RADIO -> startRadio()
            MediaSessionConstants.ACTION_SEARCH -> actionSearch()
        }
        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
    }

    @OptIn(UnstableApi::class)
    override fun onGetLibraryRoot(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        params: MediaLibraryService.LibraryParams?,
    ): ListenableFuture<LibraryResult<MediaItem>>  {
        println("PlayerServiceModern MediaLibrarySessionCallback.onGetLibraryRoot")
        return Futures.immediateFuture(
            LibraryResult.ofItem(
                MediaItem.Builder()
                    .setMediaId(PlayerServiceModern.ROOT)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setIsPlayable(false)
                            .setIsBrowsable(false)
                            .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                            .build()
                    )
                    .build(),
                params
            )
        )
    }

    @OptIn(UnstableApi::class)
    override fun onGetChildren(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        parentId: String,
        page: Int,
        pageSize: Int,
        params: MediaLibraryService.LibraryParams?,
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        println("PlayerServiceModern MediaLibrarySessionCallback.onGetChildren")
        return scope.future(Dispatchers.IO) {
                LibraryResult.ofItemList(
                    when (parentId) {
                        PlayerServiceModern.ROOT -> listOf(
                            browsableMediaItem(
                                PlayerServiceModern.SONG,
                                context.getString(R.string.songs),
                                null,
                                drawableUri(R.drawable.musical_notes),
                                MediaMetadata.MEDIA_TYPE_PLAYLIST
                            ),
                            browsableMediaItem(
                                PlayerServiceModern.ARTIST,
                                context.getString(R.string.artists),
                                null,
                                drawableUri(R.drawable.artists),
                                MediaMetadata.MEDIA_TYPE_FOLDER_ARTISTS
                            ),
                            browsableMediaItem(
                                PlayerServiceModern.ALBUM,
                                context.getString(R.string.albums),
                                null,
                                drawableUri(R.drawable.album),
                                MediaMetadata.MEDIA_TYPE_FOLDER_ALBUMS
                            ),
                            browsableMediaItem(
                                PlayerServiceModern.PLAYLIST,
                                context.getString(R.string.playlists),
                                null,
                                drawableUri(R.drawable.library),
                                MediaMetadata.MEDIA_TYPE_FOLDER_PLAYLISTS
                            )
                        )

                        PlayerServiceModern.SONG -> database.sortAllSongsByRowId(0).first()
                            .map { it.song.toMediaItem(parentId) }

                        PlayerServiceModern.ARTIST -> database.artistsByRowIdAsc().first()
                            .map { artist ->
                                browsableMediaItem(
                                    "${PlayerServiceModern.ARTIST}/${artist.id}",
                                    artist.name ?: "",
                                    "",
                                    artist.thumbnailUrl?.toUri(),
                                    MediaMetadata.MEDIA_TYPE_ARTIST
                                )
                            }

                        PlayerServiceModern.ALBUM -> database.albumsByRowIdAsc().first()
                            .map { album ->
                                browsableMediaItem(
                                    "${PlayerServiceModern.ALBUM}/${album.id}",
                                    album.title ?: "",
                                    album.authorsText,
                                    album.thumbnailUrl?.toUri(),
                                    MediaMetadata.MEDIA_TYPE_ALBUM
                                )
                            }


                        PlayerServiceModern.PLAYLIST -> {
                            val likedSongCount = database.likedSongsCount().first()
                            val cachedSongCount = getCountCachedSongs().first()
                            val downloadedSongCount = getCountDownloadedSongs().first()
                            val onDeviceSongCount = database.onDeviceSongsCount().first()
                            val playlists = database.playlistPreviewsByDateSongCountAsc().first()
                            listOf(
                                browsableMediaItem(
                                    "${PlayerServiceModern.PLAYLIST}/${ID_FAVORITES}",
                                    context.getString(R.string.favorites),
                                    likedSongCount.toString(),
                                    drawableUri(R.drawable.heart),
                                    MediaMetadata.MEDIA_TYPE_PLAYLIST
                                ),
                                browsableMediaItem(
                                    "${PlayerServiceModern.PLAYLIST}/${ID_CACHED}",
                                    context.getString(R.string.cached),
                                    cachedSongCount.toString(),
                                    drawableUri(R.drawable.download),
                                    MediaMetadata.MEDIA_TYPE_PLAYLIST
                                ),
                                browsableMediaItem(
                                    "${PlayerServiceModern.PLAYLIST}/$ID_DOWNLOADED",
                                    context.getString(R.string.downloaded),
                                    downloadedSongCount.toString(),
                                    drawableUri(R.drawable.downloaded),
                                    MediaMetadata.MEDIA_TYPE_PLAYLIST
                                ),
                                browsableMediaItem(
                                    "${PlayerServiceModern.PLAYLIST}/$ID_TOP",
                                    context.getString(R.string.playlist_top),
                                    context.preferences.getEnum(
                                        MaxTopPlaylistItemsKey,
                                        MaxTopPlaylistItems.`10`
                                    ).number.toString(),
                                    drawableUri(R.drawable.trending),
                                    MediaMetadata.MEDIA_TYPE_PLAYLIST
                                ),
                                browsableMediaItem(
                                    "${PlayerServiceModern.PLAYLIST}/$ID_ONDEVICE",
                                    context.getString(R.string.on_device),
                                    onDeviceSongCount.toString(),
                                    drawableUri(R.drawable.devices),
                                    MediaMetadata.MEDIA_TYPE_PLAYLIST
                                )

                            ) + playlists.map { playlist ->
                                browsableMediaItem(
                                    "${PlayerServiceModern.PLAYLIST}/${playlist.playlist.id}",
                                    playlist.playlist.name,
                                    playlist.songCount.toString(),
                                    drawableUri(R.drawable.playlist),
                                    MediaMetadata.MEDIA_TYPE_PLAYLIST
                                )
                            }

                        }


                        else -> when {

                            parentId.startsWith("${PlayerServiceModern.ARTIST}/") ->
                                database.artistSongs(parentId.removePrefix("${PlayerServiceModern.ARTIST}/"))
                                    .first().map {
                                        it.toMediaItem(parentId)
                                    }

                            parentId.startsWith("${PlayerServiceModern.ALBUM}/") ->
                                database.albumSongs(parentId.removePrefix("${PlayerServiceModern.ALBUM}/"))
                                    .first().map {
                                        it.toMediaItem(parentId)
                                    }

                            parentId.startsWith("${PlayerServiceModern.PLAYLIST}/") -> {

                                when (val playlistId =
                                    parentId.removePrefix("${PlayerServiceModern.PLAYLIST}/")) {
                                    ID_FAVORITES -> database.sortFavoriteSongsByRowId()
                                        .map { list ->
                                            list.map { it.song }
                                        }

                                    ID_CACHED -> database.sortOfflineSongsByPlayTime().map { list ->
                                        list.filter { song ->
                                            try {
                                                binder.cache.isCached(
                                                    song.song.id,
                                                    0L,
                                                    song.contentLength ?: 0L
                                                )
                                            } catch (e: Exception) {
                                                false
                                            }
                                        }.reversed()
                                            .map { it.song }
                                    }

                                    ID_TOP -> database.trending(
                                        context.preferences.getEnum(
                                            MaxTopPlaylistItemsKey,
                                            MaxTopPlaylistItems.`10`
                                        ).number.toInt()
                                    )

                                    ID_ONDEVICE -> database.songsEntityOnDevice().map { list ->
                                        list.map { it.song }
                                    }

                                    ID_DOWNLOADED -> {
                                        val downloads = downloadHelper.downloads.value
                                        database.listAllSongs(1)
                                            .flowOn(Dispatchers.IO)
                                            .map { list ->
                                                list.map { it.song }
                                                    .filter {
                                                        downloads[it.id]?.state == Download.STATE_COMPLETED
                                                    }
                                            }
                                    }

                                    else -> database.sortSongsFromPlaylistByRowId(playlistId.toLong())
                                        .map { list ->
                                            list.map { it.song }
                                        }
                                }.first().map {
                                    it.toMediaItem(parentId)
                                }


                            }

                            else -> emptyList()
                        }

                    },
                    params
                )
            }
        }

    @OptIn(UnstableApi::class)
    override fun onGetItem(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        mediaId: String,
    ): ListenableFuture<LibraryResult<MediaItem>> {
        println("PlayerServiceModern MediaLibrarySessionCallback.onGetItem")
        return scope.future(Dispatchers.IO) {
            println("PlayerServiceModern MediaLibrarySessionCallback.onGetItem: $mediaId")
            database.song(mediaId).first()?.toMediaItem()?.let {
                LibraryResult.ofItem(it, null)
            } ?: LibraryResult.ofError(SessionError.ERROR_UNKNOWN)
        }
    }

    override fun onSetMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>,
        startIndex: Int,
        startPositionMs: Long,
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
        println("PlayerServiceModern MediaLibrarySessionCallback.onSetMediaItems")
        return scope.future {
            // Play from Android Auto
            val defaultResult =
                MediaSession.MediaItemsWithStartPosition(
                    emptyList(),
                    startIndex,
                    startPositionMs
                )
            val path = mediaItems.firstOrNull()?.mediaId?.split("/")
                ?: return@future defaultResult
            when (path.firstOrNull()) {

                PlayerServiceModern.SEARCHED -> {
                    val songId = path.getOrNull(1) ?: return@future defaultResult
                    MediaSession.MediaItemsWithStartPosition(
                        searchedSongs.map { it.toMediaItem() },
                        searchedSongs.indexOfFirst { it.id == songId }.takeIf { it != -1 } ?: 0,
                        startPositionMs
                    )
                }

                PlayerServiceModern.SONG -> {
                    val songId = path.getOrNull(1) ?: return@future defaultResult
                    val allSongs = database.listAllSongs(-1).first()
                    MediaSession.MediaItemsWithStartPosition(
                        allSongs.map { it.song.toMediaItem() },
                        allSongs.indexOfFirst { it.song.id == songId }.takeIf { it != -1 } ?: 0,
                        startPositionMs
                    )
                }

                PlayerServiceModern.ARTIST -> {
                    val songId = path.getOrNull(2) ?: return@future defaultResult
                    val artistId = path.getOrNull(1) ?: return@future defaultResult
                    val songs = database.artistSongs(artistId).first()
                    MediaSession.MediaItemsWithStartPosition(
                        songs.map { it.toMediaItem() },
                        songs.indexOfFirst { it.id == songId }.takeIf { it != -1 } ?: 0,
                        startPositionMs
                    )
                }

                PlayerServiceModern.ALBUM -> {
                    val songId = path.getOrNull(2) ?: return@future defaultResult
                    val albumId = path.getOrNull(1) ?: return@future defaultResult
                    val albumWithSongs =
                        database.albumSongs(albumId).first() ?: return@future defaultResult
                    MediaSession.MediaItemsWithStartPosition(
                        albumWithSongs.map { it.toMediaItem() },
                        albumWithSongs.indexOfFirst { it.id == songId }.takeIf { it != -1 }
                            ?: 0,
                        startPositionMs
                    )
                }

                PlayerServiceModern.PLAYLIST -> {
                    val songId = path.getOrNull(2) ?: return@future defaultResult
                    val playlistId = path.getOrNull(1) ?: return@future defaultResult
                    val songs = when (playlistId) {
                        ID_FAVORITES -> database.sortFavoriteSongsByRowId().map { it.reversed() }
                        ID_CACHED -> database.sortOfflineSongsByPlayTime().map {
                            it.filter { song ->
                                try {
                                    binder.cache.isCached(song.song.id, 0L, song.contentLength ?: 0L)
                                } catch (e: Exception) {
                                    false
                                }
                            }.reversed()
                        }

                        ID_TOP -> database.trendingSongEntity(
                            context.preferences.getEnum(
                                MaxTopPlaylistItemsKey,
                                MaxTopPlaylistItems.`10`
                            ).number.toInt()
                        )

                        ID_ONDEVICE -> database.songsEntityOnDevice()
                        ID_DOWNLOADED -> {
                            val downloads = downloadHelper.downloads.value
                            database.listAllSongs(-1)
                                .flowOn(Dispatchers.IO)
                                .map { songs ->
                                    songs.filter {
                                        downloads[it.song.id]?.state == Download.STATE_COMPLETED
                                    }
                                }
                                .map { songs ->
                                    songs.map { it to downloads[it.song.id] }
                                        .sortedBy { it.second?.updateTimeMs ?: 0L }
                                        .map { it.first }
                                }
                        }

                        else -> database.sortSongsFromPlaylistByRowId(playlistId.toLong())
                            .map { list ->
                                list.map { it }
                            }
                    }.first()

                    MediaSession.MediaItemsWithStartPosition(
                        songs.map { it.song.toMediaItem() },
                        songs.indexOfFirst { it.song.id == songId }.takeIf { it != -1 } ?: 0,
                        startPositionMs
                    )
                }

                else -> defaultResult
            }
        }
    }

    override fun onPlaybackResumption(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
        println("PlayerServiceModern MediaLibrarySessionCallback onPlaybackResumption")
        val settablePlaylist = SettableFuture.create<MediaSession.MediaItemsWithStartPosition>()
        val defaultResult =
            MediaSession.MediaItemsWithStartPosition(
                emptyList(),
                0,
                0
            )
        if(!context.preferences.getBoolean(persistentQueueKey, false))
            return Futures.immediateFuture(defaultResult)

        scope.future {
                val queuedSong = database.queue()
                if (queuedSong.isEmpty()) return@future Futures.immediateFuture(defaultResult)

            val startIndex = queuedSong.indexOfFirst { it.position != null }.coerceAtLeast(0)
            val startPositionMs = queuedSong[startIndex].position ?: C.TIME_UNSET
            val mediaItems = queuedSong.map { it.mediaItem.asSong.toMediaItem(isFromPersistentQueue = true) }

            val resumptionPlaylist = MediaSession.MediaItemsWithStartPosition(
                mediaItems,
                startIndex,
                startPositionMs
            )
            settablePlaylist.set(resumptionPlaylist)
        }
        return settablePlaylist
    }

    private fun drawableUri(@DrawableRes id: Int) = Uri.Builder()
        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .authority(context.resources.getResourcePackageName(id))
        .appendPath(context.resources.getResourceTypeName(id))
        .appendPath(context.resources.getResourceEntryName(id))
        .build()

    private fun browsableMediaItem(
        id: String,
        title: String,
        subtitle: String?,
        iconUri: Uri?,
        mediaType: Int = MediaMetadata.MEDIA_TYPE_MUSIC
    ) =
        MediaItem.Builder()
            .setMediaId(id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(cleanPrefix(title))
                    .setSubtitle(subtitle)
                    .setArtist(subtitle)
                    .setArtworkUri(iconUri)
                    .setIsPlayable(false)
                    .setIsBrowsable(true)
                    .setMediaType(mediaType)
                    .build()
            )
            .build()

    private fun Song.toMediaItem(path: String) =
        MediaItem.Builder()
            .setMediaId("$path/$id")
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(cleanPrefix(title))
                    .setSubtitle(artistsText)
                    .setArtist(artistsText)
                    .setArtworkUri(thumbnailUrl?.toUri())
                    .setIsPlayable(true)
                    .setIsBrowsable(false)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                    .build()
            )
            .build()

    private fun Song.toMediaItem(isFromPersistentQueue: Boolean = false) =
        MediaItem.Builder()
            .setMediaId(id)
            .setUri(id)
            .setCustomCacheKey(id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(cleanPrefix(title))
                    .setSubtitle(artistsText)
                    .setArtist(artistsText)
                    .setArtworkUri(thumbnailUrl?.toUri())
                    .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                    .setExtras(
                        Bundle().apply {
                            putBoolean(persistentQueueKey, isFromPersistentQueue)
                        }
                    )
                    .build()
            )
            .build()

    private fun getCountCachedSongs() = database.sortOfflineSongsByPlayTime().map {
        it.filter { song ->
            try {
                binder.cache.isCached(song.song.id, 0L, song.contentLength ?: 0L)
            } catch (e: Exception) {
                false
            }
        }.size
    }

    private fun getCountDownloadedSongs() = downloadHelper.downloads.map {
        it.filter {
            it.value.state == Download.STATE_COMPLETED
        }.size
    }
}



object MediaSessionConstants {
    const val ID_FAVORITES = "FAVORITES"
    const val ID_CACHED = "CACHED"
    const val ID_DOWNLOADED = "DOWNLOADED"
    const val ID_TOP = "TOP"
    const val ID_ONDEVICE = "ONDEVICE"
    const val ACTION_TOGGLE_DOWNLOAD = "TOGGLE_DOWNLOAD"
    const val ACTION_TOGGLE_LIKE = "TOGGLE_LIKE"
    const val ACTION_TOGGLE_SHUFFLE = "TOGGLE_SHUFFLE"
    const val ACTION_TOGGLE_REPEAT_MODE = "TOGGLE_REPEAT_MODE"
    const val ACTION_START_RADIO = "START_RADIO"
    const val ACTION_SEARCH = "ACTION_SEARCH"
    val CommandToggleDownload = SessionCommand(ACTION_TOGGLE_DOWNLOAD, Bundle.EMPTY)
    val CommandToggleLike = SessionCommand(ACTION_TOGGLE_LIKE, Bundle.EMPTY)
    val CommandToggleShuffle = SessionCommand(ACTION_TOGGLE_SHUFFLE, Bundle.EMPTY)
    val CommandToggleRepeatMode = SessionCommand(ACTION_TOGGLE_REPEAT_MODE, Bundle.EMPTY)
    val CommandStartRadio = SessionCommand(ACTION_START_RADIO, Bundle.EMPTY)
    val CommandSearch = SessionCommand(ACTION_SEARCH, Bundle.EMPTY)
}