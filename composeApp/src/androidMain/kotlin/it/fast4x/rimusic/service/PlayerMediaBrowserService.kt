package it.fast4x.rimusic.service

import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.DrawableRes
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media.MediaBrowserServiceCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.Cache
import androidx.media3.exoplayer.offline.Download
import it.fast4x.environment.Environment
import it.fast4x.environment.models.NavigationEndpoint
import it.fast4x.environment.models.bodies.SearchBody
import it.fast4x.environment.requests.searchPage
import it.fast4x.environment.utils.from
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.MONTHLY_PREFIX
import it.fast4x.rimusic.PINNED_PREFIX
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.MaxTopPlaylistItems
import it.fast4x.rimusic.models.Album
import it.fast4x.rimusic.models.Artist
import it.fast4x.rimusic.models.PlaylistPreview
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.models.SongEntity
import it.fast4x.rimusic.models.SongWithContentLength
import it.fast4x.rimusic.utils.MaxTopPlaylistItemsKey
import it.fast4x.rimusic.utils.asMediaItem
import it.fast4x.rimusic.utils.asSong
import it.fast4x.rimusic.utils.forcePlayAtIndex
import it.fast4x.rimusic.utils.getEnum
import it.fast4x.rimusic.utils.getTitleMonthlyPlaylistFromContext
import it.fast4x.rimusic.utils.intent
import it.fast4x.rimusic.utils.playNext
import it.fast4x.rimusic.utils.playPrevious
import it.fast4x.rimusic.utils.preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class PlayerMediaBrowserService : MediaBrowserServiceCompat(), ServiceConnection {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var lastSongs = emptyList<Song>()
    private var searchedSongs = emptyList<Song>()

    private var bound = false


    override fun onDestroy() {
        if (bound) {
            unbindService(this)
        }
        super.onDestroy()
    }

    @UnstableApi
    override fun onServiceConnected(className: ComponentName, service: IBinder) {
        if (service is PlayerService.Binder) {
            bound = true
            sessionToken = service.mediaSession.sessionToken
            service.mediaSession.setCallback(SessionCallback(service, service.cache))
        }
    }

    override fun onServiceDisconnected(name: ComponentName) = Unit

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        bindService(intent<PlayerService>(), this, Context.BIND_AUTO_CREATE)
        return BrowserRoot(
            MediaId.root,
            //bundleOf("android.media.browse.CONTENT_STYLE_BROWSABLE_HINT" to 1)
            Bundle().apply {
                putBoolean(MEDIA_SEARCH_SUPPORTED, true)
                putBoolean(CONTENT_STYLE_SUPPORTED, true)
                putInt(CONTENT_STYLE_BROWSABLE_HINT, CONTENT_STYLE_GRID)
                putInt(CONTENT_STYLE_PLAYABLE_HINT, CONTENT_STYLE_LIST)
            }
        )
        /*
        return if (clientUid == Process.myUid()
            || clientUid == Process.SYSTEM_UID
            || clientPackageName == "com.google.android.projection.gearhead"
        ) {
            bindService(intent<PlayerService>(), this, Context.BIND_AUTO_CREATE)
            BrowserRoot(
                MediaId.root,
                bundleOf("android.media.browse.CONTENT_STYLE_BROWSABLE_HINT" to 1)
            )
        } else {
            null
        }
         */
    }

    @OptIn(UnstableApi::class)
    override fun onSearch(
        query: String,
        extras: Bundle?,
        result: Result<List<MediaItem>>
    ) {
        result.detach()
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
                //it.asBrowserMediaItem
                MediaItem(
                    MediaDescriptionCompat.Builder()
                        .setMediaId(MediaId.forSearched(it.id))
                        .setTitle(it.title)
                        .setSubtitle(it.artistsText)
                        .setIconUri(it.thumbnailUrl?.toUri())
                        .build(),
                    MediaItem.FLAG_PLAYABLE
                )
            }

            result.sendResult(resultList)
        }
    }

     override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaItem>>) {
        runBlocking(Dispatchers.IO) {
            result.sendResult(
                when (parentId) {
                    MediaId.root -> mutableListOf(
                        songsBrowserMediaItem,
                        playlistsBrowserMediaItem,
                        albumsBrowserMediaItem,
                        artistsBrowserMediaItem
                    )

                    MediaId.songs -> Database
                        .sortAllSongsByPlayTime( 0 )
                        .first()
                        .reversed()
                        .take(500)
                        .also { lastSongs = it.map { it.song } }
                        .map { it.song.asBrowserMediaItem }
                        .toMutableList()
                        .apply {
                            if (isNotEmpty()) add(0, shuffleBrowserMediaItem)
                        }

                    MediaId.playlists -> Database
                        .playlistPreviewsByNameAsc()
                        .first()
                        .map { it.asBrowserMediaItem }
                        .sortedBy { it.description.title.toString() }
                        .map { it.asCleanMediaItem }
                        .toMutableList()
                        .apply {
                            add(0, favoritesBrowserMediaItem)
                            add(1, offlineBrowserMediaItem)
                            add(2, downloadedBrowserMediaItem)
                            add(3, topBrowserMediaItem)
                            add(4, ondeviceBrowserMediaItem)
                        }

                    MediaId.albums -> Database
                        .albumsByRowIdDesc()
                        .first()
                        .map { it.asBrowserMediaItem }
                        .toMutableList()

                    MediaId.artists -> Database
                        .artistsByRowIdDesc()
                        .first()
                        .map { it.asBrowserMediaItem }
                        .toMutableList()

                    else -> mutableListOf()
                }
            )
        }
    }

    private fun uriFor(@DrawableRes id: Int) = Uri.Builder()
        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .authority(resources.getResourcePackageName(id))
        .appendPath(resources.getResourceTypeName(id))
        .appendPath(resources.getResourceEntryName(id))
        .build()

    //private fun uriFor(path: String) = Res.getUri(path).toUri()


    private val shuffleBrowserMediaItem
        inline get() = MediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId(MediaId.shuffle)
                .setTitle((this as Context).resources.getString(R.string.shuffle))
                .setIconUri(uriFor(R.drawable.shuffle))
                .build(),
            MediaItem.FLAG_PLAYABLE
        )

    private val songsBrowserMediaItem
        inline get() = MediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId(MediaId.songs)
                .setTitle((this as Context).resources.getString(R.string.songs))
                .setIconUri(uriFor(R.drawable.musical_notes))
                //.setIconUri(uriFor("drawable/musical_notes.xml"))
                .build(),
            MediaItem.FLAG_BROWSABLE
        )


    private val playlistsBrowserMediaItem
        inline get() = MediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId(MediaId.playlists)
                .setTitle((this as Context).resources.getString(R.string.library))
                .setIconUri(uriFor(R.drawable.library))
                //.setIconUri(uriFor("drawable/library.xml"))
                .build(),
            MediaItem.FLAG_BROWSABLE
        )

    private val albumsBrowserMediaItem
        inline get() = MediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId(MediaId.albums)
                .setTitle((this as Context).resources.getString(R.string.albums))
                .setIconUri(uriFor(R.drawable.album))
                //.setIconUri(uriFor("drawable/album.xml"))
                .build(),
            MediaItem.FLAG_BROWSABLE
        )

    private val artistsBrowserMediaItem
        inline get() = MediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId(MediaId.artists)
                .setTitle((this as Context).resources.getString(R.string.artists))
                .setIconUri(uriFor(R.drawable.artists))
                .build(),
            MediaItem.FLAG_BROWSABLE
        )

    private val favoritesBrowserMediaItem
        inline get() = MediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId(MediaId.favorites)
                .setTitle((this as Context).resources.getString(R.string.favorites))
                .setIconUri(uriFor(R.drawable.heart))
                .build(),
            MediaItem.FLAG_PLAYABLE
        )

    private val offlineBrowserMediaItem
        inline get() = MediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId(MediaId.offline)
                .setTitle((this as Context).resources.getString(R.string.cached))
                .setIconUri(uriFor(R.drawable.sync))
                .build(),
            MediaItem.FLAG_PLAYABLE
        )

    private val downloadedBrowserMediaItem
        inline get() = MediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId(MediaId.downloaded)
                .setTitle((this as Context).resources.getString(R.string.downloaded))
                .setIconUri(uriFor(R.drawable.downloaded))
                .build(),
            MediaItem.FLAG_PLAYABLE
        )

    private val ondeviceBrowserMediaItem
        inline get() = MediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId(MediaId.ondevice)
                .setTitle((this as Context).resources.getString(R.string.on_device))
                .setIconUri(uriFor(R.drawable.musical_notes))
                //.setIconUri(uriFor("drawable/musical_notes.xml"))
                .build(),
            MediaItem.FLAG_PLAYABLE
        )

    private val topBrowserMediaItem
        inline get() = MediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId(MediaId.top)
                .setTitle((this as Context).resources.getString(R.string.my_playlist_top)
                    .format((this as Context).preferences.getEnum(MaxTopPlaylistItemsKey,
                            MaxTopPlaylistItems.`10`).number))
                .setIconUri(uriFor(R.drawable.trending))
                .build(),
            MediaItem.FLAG_PLAYABLE
        )

    private val Song.asBrowserMediaItem
        inline get() = MediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId(MediaId.forSong(id))
                .setTitle(title)
                .setSubtitle(artistsText)
                .setIconUri(thumbnailUrl?.toUri())
                .build(),
            MediaItem.FLAG_PLAYABLE
        )

    private val PlaylistPreview.asBrowserMediaItem
        inline get() = MediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId(MediaId.forPlaylist(playlist.id))
                //.setTitle(playlist.name.substringAfter(PINNED_PREFIX))
                .setTitle(if (playlist.name.startsWith(PINNED_PREFIX)) playlist.name.replace(PINNED_PREFIX,"0:",true) else
                    if (playlist.name.startsWith(MONTHLY_PREFIX)) playlist.name.replace(
                        MONTHLY_PREFIX,"1:",true) else playlist.name)
                .setSubtitle("$songCount ${(this@PlayerMediaBrowserService as Context).resources.getString(R.string.songs)}")
                .setIconUri(uriFor(if (playlist.name.startsWith(PINNED_PREFIX)) R.drawable.pin else
                    if (playlist.name.startsWith(MONTHLY_PREFIX)) R.drawable.stat_month else R.drawable.playlist))
                .build(),
            MediaItem.FLAG_PLAYABLE
        )

    private val MediaItem.asCleanMediaItem
        inline get() = MediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId(mediaId)
                .setTitle(if (description.title.toString().startsWith("0:")) description.title.toString().substringAfter("0:") else
                    if (description.title.toString().startsWith("1:")) getTitleMonthlyPlaylistFromContext(description.title.toString().substringAfter("1:"), this@PlayerMediaBrowserService) else description.title.toString())
                .setIconUri(uriFor(if (description.title.toString().startsWith("0:")) R.drawable.pin else
                    if (description.title.toString().startsWith("1:")) R.drawable.stat_month else R.drawable.playlist))
                .build(),
            MediaItem.FLAG_PLAYABLE
        )

    private val Album.asBrowserMediaItem
        inline get() = MediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId(MediaId.forAlbum(id))
                .setTitle(title)
                .setSubtitle(authorsText)
                .setIconUri(thumbnailUrl?.toUri())
                .build(),
            MediaItem.FLAG_PLAYABLE
        )

    private val Artist.asBrowserMediaItem
        inline get() = MediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId(MediaId.forArtistByName(name ?: ""))
                .setTitle(name)
                //.setSubtitle()
                .setIconUri(thumbnailUrl?.toUri())
                .build(),
            MediaItem.FLAG_PLAYABLE
        )

    private inner class SessionCallback @OptIn(UnstableApi::class) constructor(
        private val binder: PlayerService.Binder,
        private val cache: Cache
    ) :
        MediaSessionCompat.Callback() {
        override fun onPlay() = binder.player.play()
        override fun onPause() = binder.player.pause()
        override fun onSkipToPrevious() = binder.player.playPrevious()
        override fun onSkipToNext() = binder.player.playNext()
        override fun onSeekTo(pos: Long) = binder.player.seekTo(pos)
        override fun onSkipToQueueItem(id: Long) = binder.player.seekToDefaultPosition(id.toInt())
        @OptIn(UnstableApi::class)
        override fun onPlayFromSearch(query: String?, extras: Bundle?) {
            if (query.isNullOrBlank()) return
            binder.playFromSearch(query)
        }



        @FlowPreview
        @ExperimentalCoroutinesApi
        @UnstableApi
        override fun onCustomAction(action: String?, extras: Bundle?) {
            if (action == "LIKE") {
                binder.toggleLike()
                binder.refreshPlayer()
            }
            if (action == "DOWNLOAD") {
                binder.toggleDownload()
                binder.refreshPlayer()
            }
            /*
            if (action == "SHUFFLE") {
                binder.toggleShuffle()
                binder.refreshPlayer()
            }
             */
            if (action == "PLAYRADIO") {
                coroutineScope.launch {
                    withContext(Dispatchers.Main) {
                        binder.stopRadio()
                        binder.playRadio(NavigationEndpoint.Endpoint.Watch(videoId = binder.player.currentMediaItem?.mediaId))
                    }
                }

            }


            super.onCustomAction(action, extras)
        }

        @UnstableApi
        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            val data = mediaId?.split('/') ?: return
            var index = 0

            //println("RiMusicMediaBrowser mediaId ${mediaId} data $data ")

            coroutineScope.launch {
                val mediaItems = when (data.getOrNull(0)) {
                    MediaId.shuffle -> lastSongs.shuffled()

                    MediaId.songs ->  data
                        .getOrNull(1)
                        ?.let { songId ->
                            index = lastSongs.indexOfFirst { it.id == songId }
                            lastSongs
                        }

                    MediaId.favorites -> Database
                        .favorites()
                        .first()

                    MediaId.offline -> Database
                        .songsWithContentLength()
                        .first()
                        .filter { song ->
                            song.contentLength?.let {
                                cache.isCached(song.song.id, 0, it)
                            } ?: false
                        }
                        .map(SongWithContentLength::song)

                    MediaId.ondevice -> Database
                        .songsOnDevice()
                        .first()

                    MediaId.downloaded -> {
                        val downloads = MyDownloadHelper.downloads.value
                        Database.listAllSongs( 1 )
                                .first()
                                .map( SongEntity::song )
                                .filter {
                                       downloads[it.id]?.state == Download.STATE_COMPLETED
                                }
                    }

                    MediaId.top -> {
                        val maxTopSongs = preferences.getEnum(MaxTopPlaylistItemsKey,
                            MaxTopPlaylistItems.`10`).number.toInt()

                        Database.trending(maxTopSongs)
                                .first()
                    }

                    MediaId.playlists -> data
                        .getOrNull(1)
                        ?.toLongOrNull()
                        ?.let(Database::playlistWithSongs)
                        ?.first()
                        ?.songs

                    MediaId.albums -> data
                        .getOrNull(1)
                        ?.let(Database::albumSongs)
                        ?.first()

                    MediaId.artists -> {
                        data
                        .getOrNull(1)
                        ?.let(Database::artistSongsByname)
                        ?.first()
                    }

                    MediaId.searched -> data
                        .getOrNull(1)
                        ?.let { songId ->
                            searchedSongs.filter { it.id == songId }
                            /*index = searchedSongs.indexOfFirst { it.id == songId }
                            searchedSongs*/
                        }

                    else -> emptyList()
                }?.map(Song::asMediaItem) ?: return@launch

                withContext(Dispatchers.Main) {
                    binder.player.forcePlayAtIndex(mediaItems, index.coerceIn(0, mediaItems.size))
                }
            }
        }
    }

    private object MediaId {
        const val root = "root"
        const val songs = "songs"
        const val playlists = "playlists"
        const val albums = "albums"
        const val artists = "artists"
        const val searched = "searched"

        const val favorites = "favorites"
        const val offline = "offline"
        const val shuffle = "shuffle"
        const val downloaded = "downloaded"
        const val ondevice = "ondevice"
        const val top = "top"

        fun forSong(id: String) = "songs/$id"
        fun forPlaylist(id: Long) = "playlists/$id"
        fun forAlbum(id: String) = "albums/$id"
        fun forArtistByName(name: String) = "artists/$name"
        fun forSearched(id: String) = "searched/$id"
    }
}

const val MEDIA_SEARCH_SUPPORTED = "android.media.browse.SEARCH_SUPPORTED"
private const val CONTENT_STYLE_BROWSABLE_HINT = "android.media.browse.CONTENT_STYLE_BROWSABLE_HINT"
private const val CONTENT_STYLE_PLAYABLE_HINT = "android.media.browse.CONTENT_STYLE_PLAYABLE_HINT"
private const val CONTENT_STYLE_SUPPORTED = "android.media.browse.CONTENT_STYLE_SUPPORTED"
private const val CONTENT_STYLE_LIST = 1
private const val CONTENT_STYLE_GRID = 2