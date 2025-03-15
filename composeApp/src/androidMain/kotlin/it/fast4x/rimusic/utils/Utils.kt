package it.fast4x.rimusic.utils


import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.format.DateUtils
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import com.zionhuang.innertube.pages.LibraryPage
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import it.fast4x.environment.Environment
import it.fast4x.environment.EnvironmentExt
import it.fast4x.environment.EnvironmentExt.addToPlaylist
import it.fast4x.environment.EnvironmentExt.likeVideoOrSong
import it.fast4x.environment.EnvironmentExt.removelikeVideoOrSong
import it.fast4x.environment.models.bodies.ContinuationBody
import it.fast4x.environment.models.bodies.SearchBody
import it.fast4x.environment.requests.playlistPage
import it.fast4x.environment.requests.searchPage
import it.fast4x.environment.utils.from
import it.fast4x.kugou.KuGou
import it.fast4x.lrclib.LrcLib
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.Database.Companion.getLikedAt
import it.fast4x.rimusic.EXPLICIT_PREFIX
import it.fast4x.rimusic.R
import it.fast4x.rimusic.appContext
import it.fast4x.rimusic.cleanPrefix
import it.fast4x.rimusic.context
import it.fast4x.rimusic.enums.PopupType
import it.fast4x.rimusic.models.Album
import it.fast4x.rimusic.models.Artist
import it.fast4x.rimusic.models.Lyrics
import it.fast4x.rimusic.models.Playlist
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.models.SongAlbumMap
import it.fast4x.rimusic.models.SongArtistMap
import it.fast4x.rimusic.models.SongEntity
import it.fast4x.rimusic.models.SongPlaylistMap
import it.fast4x.rimusic.service.LOCAL_KEY_PREFIX
import it.fast4x.rimusic.service.MyDownloadHelper
import it.fast4x.rimusic.service.isLocal
import it.fast4x.rimusic.ui.components.themed.NewVersionDialog
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.ui.screens.settings.isYouTubeSyncEnabled
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

const val EXPLICIT_BUNDLE_TAG = "is_explicit"

fun <T> MutableList<T>.move(fromIndex: Int, toIndex: Int): MutableList<T> {
    add(toIndex, removeAt(fromIndex))
    return this
}

fun getDateTimeAsFormattedString(dateAsLongInMs: Long): String? {
    try {
        return SimpleDateFormat("dd/MM/yyyy").format(Date(dateAsLongInMs))
    } catch (e: Exception) {
        return null // parsing exception
    }
}

fun getTimestampFromDate(date: String): Long {
    return try {
        SimpleDateFormat("dd-MM-yyyy").parse(date).time
    } catch (e: Exception) {
        return 0
    }
}

@OptIn(UnstableApi::class)
fun mediaItemToggleLike( mediaItem: MediaItem) {
    Database.asyncTransaction {
        if (songExist(mediaItem.mediaId) == 0)
            insert(mediaItem)
        if (getLikedAt(mediaItem.mediaId) in listOf(null, -1L))
            like(
                mediaItem.mediaId,
                System.currentTimeMillis()
            )
        else like(
            mediaItem.mediaId,
            null
        )
        MyDownloadHelper.autoDownloadWhenLiked(
            context(),
            mediaItem
        )
    }
}

@UnstableApi
fun mediaItemSetLiked( mediaItem: MediaItem ) {
    Database.asyncTransaction {
        if (songExist(mediaItem.mediaId) == 0)
            insert(mediaItem)
        if (getLikedAt(mediaItem.mediaId) in listOf(null, -1L))
            like(
                mediaItem.mediaId,
                System.currentTimeMillis()
            )
        MyDownloadHelper.autoDownloadWhenLiked(
            context(),
            mediaItem
        )
    }
}

val Environment.AlbumItem.asAlbum: Album
    get() = Album (
        id = key,
        title = info?.name,
        thumbnailUrl = thumbnail?.url,
        year = year,
        authorsText = authors?.joinToString(", ") { it.name ?: "" },
        //shareUrl =
    )

val Environment.Podcast.EpisodeItem.asMediaItem: MediaItem
    @UnstableApi
    get() = MediaItem.Builder()
        .setMediaId(videoId)
        .setUri(videoId)
        .setCustomCacheKey(videoId)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(author.toString())
                .setAlbumTitle(title)
                .setArtworkUri(thumbnail.firstOrNull()?.url?.toUri())
                .setExtras(
                    bundleOf(
                        //"albumId" to album?.endpoint?.browseId,
                        "durationText" to durationString,
                        "artistNames" to author,
                        //"artistIds" to authors?.mapNotNull { it.endpoint?.browseId },
                    )
                )

                .build()
        )
        .build()

val Environment.SongItem.asMediaItem: MediaItem
    @UnstableApi
    get() = MediaItem.Builder()
        .setMediaId(key)
        .setUri(key)
        .setCustomCacheKey(key)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(info?.name)
                .setArtist(authors?.filter {it.name?.matches(Regex("\\s*([,&])\\s*")) == false }?.joinToString(", ") { it.name ?: "" })
                .setAlbumTitle(album?.name)
                .setArtworkUri(thumbnail?.url?.toUri())
                .setExtras(
                    bundleOf(
                        "albumId" to album?.endpoint?.browseId,
                        "durationText" to durationText,
                        "artistNames" to authors?.filter { it.endpoint != null }
                            ?.mapNotNull { it.name },
                        "artistIds" to authors?.mapNotNull { it.endpoint?.browseId },
                        EXPLICIT_BUNDLE_TAG to explicit,
                        "setVideoId" to setVideoId,
                    )
                )
                .build()
        )
        .build()

val Environment.SongItem.asSong: Song
    @UnstableApi
    get() = Song (
        id = key,
        title = info?.name ?: "",
        artistsText = authors?.joinToString(", ") { it.name ?: "" },
        durationText = durationText,
        thumbnailUrl = thumbnail?.url
    )

val Environment.VideoItem.asMediaItem: MediaItem
    @UnstableApi
    get() = MediaItem.Builder()
        .setMediaId(key)
        .setUri(key)
        .setCustomCacheKey(key)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(info?.name)
                .setArtist(authors?.joinToString(", ") { it.name ?: "" })
                .setArtworkUri(thumbnail?.url?.toUri())
                .setExtras(
                    bundleOf(
                        "durationText" to durationText,
                        "artistNames" to authors?.filter { it.endpoint != null }
                            ?.mapNotNull { it.name },
                        "artistIds" to authors?.mapNotNull { it.endpoint?.browseId },
                        "isOfficialMusicVideo" to isOfficialMusicVideo,
                        "isUserGeneratedContent" to isUserGeneratedContent,
                        "isVideo" to true,
                        // "artistNames" to if (isOfficialMusicVideo) authors?.filter { it.endpoint != null }?.mapNotNull { it.name } else null,
                        // "artistIds" to if (isOfficialMusicVideo) authors?.mapNotNull { it.endpoint?.browseId } else null,
                    )
                )
                .build()
        )
        .build()


val Song.asMediaItem: MediaItem
    @UnstableApi
    get() = MediaItem.Builder()
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artistsText)
                .setArtworkUri(thumbnailUrl?.toUri())
                .setExtras(
                    bundleOf(
                        "durationText" to durationText,
                        EXPLICIT_BUNDLE_TAG to title.startsWith( EXPLICIT_PREFIX, true )
                    )
                )
                .build()
        )
        .setMediaId(id)
        .setUri(
            if (isLocal) ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                id.substringAfter(LOCAL_KEY_PREFIX).toLong()
            ) else id.toUri()
        )
        .setCustomCacheKey(id)
        .build()

val SongEntity.asMediaItem: MediaItem
    @UnstableApi
    get() = MediaItem.Builder()
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artistsText)
                .setAlbumTitle(albumTitle)
                .setArtworkUri(song.thumbnailUrl?.toUri())
                .setExtras(
                    bundleOf(
                        "durationText" to song.durationText,
                        EXPLICIT_BUNDLE_TAG to song.title.startsWith( EXPLICIT_PREFIX, true )
                    )
                )
                .build()
        )
        .setMediaId(song.id)
        .setUri(
            if (song.isLocal) ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                song.id.substringAfter(LOCAL_KEY_PREFIX).toLong()
            ) else song.id.toUri()
        )
        .setCustomCacheKey(song.id)
        .build()

val MediaItem.asSong: Song
    @UnstableApi
    get() = Song (
        id = mediaId,
        title = mediaMetadata.title.toString(),
        artistsText = mediaMetadata.artist.toString(),
        durationText = mediaMetadata.extras?.getString("durationText"),
        thumbnailUrl = mediaMetadata.artworkUri.toString()
    )

val MediaItem.cleaned: MediaItem
    @UnstableApi
    get() = MediaItem.Builder()
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(cleanPrefix(mediaMetadata.title.toString()))
                .setArtist(mediaMetadata.artist)
                .setArtworkUri(mediaMetadata.artworkUri)
                .setExtras(mediaMetadata.extras)
                .build()
        )
        .setMediaId(mediaId)
        .setUri(
            if (isLocal) ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                mediaId.substringAfter(LOCAL_KEY_PREFIX).toLong()
            ) else mediaId.toUri()
        )
        .setCustomCacheKey(mediaId)
        .build()

val MediaItem.isVideo: Boolean
    get() = mediaMetadata.extras?.getBoolean("isVideo") == true

val MediaItem.isExplicit: Boolean
    get() {
        val isTitleContain = mediaMetadata.title?.startsWith( EXPLICIT_PREFIX, true )
        val isBundleContain = mediaMetadata.extras?.getBoolean( EXPLICIT_BUNDLE_TAG )

        return isTitleContain == true || isBundleContain == true
    }

fun String.resize(
    width: Int? = null,
    height: Int? = null,
): String {
    if (width == null && height == null) return this
    "https://lh3\\.googleusercontent\\.com/.*=w(\\d+)-h(\\d+).*".toRegex().matchEntire(this)?.groupValues?.let { group ->
        val (W, H) = group.drop(1).map { it.toInt() }
        var w = width
        var h = height
        if (w != null && h == null) h = (w / W) * H
        if (w == null && h != null) w = (h / H) * W
        return "${split("=w")[0]}=w$w-h$h-p-l90-rj"
    }
    if (this matches "https://yt3\\.ggpht\\.com/.*=s(\\d+)".toRegex()) {
        return "$this-s${width ?: height}"
    }
    return this
}

fun String?.thumbnail(size: Int): String? {
    return when {
        this?.startsWith("https://lh3.googleusercontent.com") == true -> "$this-w$size-h$size"
        this?.startsWith("https://yt3.ggpht.com") == true -> "$this-w$size-h$size-s$size"
        else -> this
    }
}
fun String?.thumbnail(): String? {
    return this
}
fun Uri?.thumbnail(size: Int): Uri? {
    return toString().thumbnail(size)?.toUri()
}

fun formatAsDuration(millis: Long) = DateUtils.formatElapsedTime(millis / 1000).removePrefix("0")
fun durationToMillis(duration: String): Long {
    val parts = duration.split(":")
    if (parts.size == 3){
        val hours = parts[0].toLong()
        val minutes = parts[1].toLong()
        val seconds = parts[2].toLong()
        return hours * 3600000 + minutes * 60000 + seconds * 1000
    } else {
        val minutes = parts[0].toLong()
        val seconds = parts[1].toLong()
        return minutes * 60000 + seconds * 1000
    }
}

fun durationTextToMillis(duration: String): Long {
    return try {
        durationToMillis(duration)
    } catch (e: Exception) {
        0L
    }
}


fun formatAsTime(millis: Long): String {
    //if (millis == 0L) return ""
    val timePart1 = Duration.ofMillis(millis).toMinutes().minutes
    val timePart2 = Duration.ofMillis(millis).seconds % 60

    return "${timePart1} ${timePart2}s"
}

fun formatTimelineSongDurationToTime(millis: Long) =
    Duration.ofMillis(millis*1000).toMinutes().minutes.toString()

@SuppressLint("SimpleDateFormat")
fun getCalculatedMonths( month: Int): String? {
    val c: Calendar = GregorianCalendar()
    c.add(Calendar.MONTH, -month)
    val sdfr = SimpleDateFormat("yyyy-MM")
    return sdfr.format(c.time).toString()
}

@JvmName("ResultInnertubeItemsPageCompleted")
suspend fun Result<Environment.ItemsPage<Environment.SongItem>?>.completed(
    maxDepth: Int =  Int.MAX_VALUE
): Result<Environment.ItemsPage<Environment.SongItem>?> = runCatching {
    val page = getOrThrow()
    val songs = page?.items.orEmpty().toMutableList()
    var continuation = page?.continuation

    var depth = 0
    var continuationsList = arrayOf<String>()
    //continuationsList += continuation.orEmpty()

    println("mediaItem playlist completed() continuation? $continuation")

    while (continuation != null && depth++ < maxDepth) {
        val newSongs = Environment
            .playlistPage(
                body = ContinuationBody(continuation = continuation)
            )
            ?.getOrNull()
            ?.takeUnless { it.items.isNullOrEmpty() } ?: break

        newSongs.items?.let { songs += it.filter { it !in songs } }
        continuation = newSongs.continuation

        //println("mediaItem loop $depth continuation founded ${continuationsList.contains(continuation)} $continuation")
        if (continuationsList.contains(continuation)) break

        continuationsList += continuation.orEmpty()
        //println("mediaItem loop continuationList size ${continuationsList.size}")
    }

    page?.copy(items = songs, continuation = null)
}.also { it.exceptionOrNull()?.printStackTrace() }

@JvmName("ResultInnertubePlaylistOrAlbumPageCompleted")
suspend fun Result<Environment.PlaylistOrAlbumPage>.completed(
    maxDepth: Int =  Int.MAX_VALUE
): Result<Environment.PlaylistOrAlbumPage> = runCatching {
    val page = getOrThrow()
    val songsPage = runCatching {
        page.songsPage
    }.onFailure {
        println("Innertube songsPage PlaylistOrAlbumPage>.completed ${it.stackTraceToString()}")
    }
    val itemsPage = songsPage.completed(maxDepth).getOrThrow()
    page.copy(songsPage = itemsPage)
}.onFailure {
    println("Innertube PlaylistOrAlbumPage>.completed ${it.stackTraceToString()}")
}

//@JvmName("completedPlaylist")
suspend fun Result<LibraryPage?>.completed(): Result<LibraryPage> = runCatching {
    val page = getOrThrow()
    val items = page?.items?.toMutableList()
    var continuation = page?.continuation
    while (continuation != null) {
        val continuationPage = Environment.libraryContinuation(continuation).getOrNull()
        if (continuationPage != null)
            if (items != null) {
                items += continuationPage.items
            }

        continuation = continuationPage?.continuation
    }
    LibraryPage(
        items = items ?: emptyList(),
        continuation = page?.continuation
    )
}


@Composable
fun CheckAvailableNewVersion(
    onDismiss: () -> Unit,
    updateAvailable: (Boolean) -> Unit
) {
    var updatedProductName = ""
    var updatedVersionName = ""
    var updatedVersionCode = 0
    val file = File(LocalContext.current.filesDir, "RiMusicUpdatedVersionCode.ver")
    if (file.exists()) {
        val dataText = file.readText().substring(0, file.readText().length - 1).split("-")
        updatedVersionCode =
            try {
                dataText.first().toInt()
            } catch (e: Exception) {
                0
            }
        updatedVersionName = if(dataText.size == 3) dataText[1] else ""
        updatedProductName =  if(dataText.size == 3) dataText[2] else ""
    }

    if (updatedVersionCode > getVersionCode()) {
        //if (updatedVersionCode > BuildConfig.VERSION_CODE)
        NewVersionDialog(
            updatedVersionName = updatedVersionName,
            updatedVersionCode = updatedVersionCode,
            updatedProductName = updatedProductName,
            onDismiss = onDismiss
        )
        updateAvailable(true)
    } else {
        updateAvailable(false)
        onDismiss()
    }
}

@Composable
fun getVersionName(): String {
    val context = LocalContext.current
    try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return pInfo.versionName ?: ""
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return ""
}
@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun getLongVersionCode(): Long {
    val context = LocalContext.current
    try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return pInfo.longVersionCode
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return 0L
}


@Composable
fun getVersionCode(): Int {
    val context = LocalContext.current
    try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return pInfo.versionCode
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return 0
}


inline val isAtLeastAndroid6
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

inline val isAtLeastAndroid7
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

inline val isAtLeastAndroid8
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

inline val isAtLeastAndroid81
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1

inline val isAtLeastAndroid10
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

inline val isAtLeastAndroid11
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

inline val isAtLeastAndroid12
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

inline val isAtLeastAndroid13
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

inline val isAtLeastAndroid14
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

fun Modifier.conditional(condition : Boolean, modifier : Modifier.() -> Modifier) : Modifier {
    return if (condition) {
        then(modifier(Modifier))
    } else {
        this
    }
}

@OptIn(UnstableApi::class)
suspend fun getAlbumVersionFromVideo(song: Song,playlistId : Long, position : Int, playlist : Playlist?){
    val isExtPlaylist = (song.thumbnailUrl == "") && (song.durationText != "0:00")
    var songNotFound: Song
    var random4Digit  = Random.nextInt(1000, 10000)
    fun filteredText(text : String): String{
        val filteredText = text
            .lowercase()
            .replace("(", " ")
            .replace(")", " ")
            .replace("-", " ")
            .replace("lyrics", "")
            .replace("vevo", "")
            .replace(" hd", "")
            .replace("official video", "")
            .filter {it.isLetterOrDigit() || it.isWhitespace() || it == '\'' || it == ',' }
            .replace(Regex("\\s+"), " ")
        return filteredText
    }

    val searchQuery = Environment.searchPage(
        body = SearchBody(
            query = filteredText("${cleanPrefix(song.title)} ${song.artistsText}"),
            params = Environment.SearchFilter.Song.value
        ),
        fromMusicShelfRendererContent = Environment.SongItem.Companion::from
    )

    val searchResults = searchQuery?.getOrNull()?.items

    val sourceSongWords = filteredText(cleanPrefix(song.title))
        .split(" ").filter { it.isNotEmpty() }
    val lofi = sourceSongWords.contains("lofi")
    val rock = sourceSongWords.contains("rock")
    val reprise = sourceSongWords.contains("reprise")
    val unplugged = sourceSongWords.contains("unplugged")
    val instrumental = sourceSongWords.contains("instrumental")
    val remix = sourceSongWords.contains("remix")
    val acapella = sourceSongWords.contains("acapella")
    val acoustic = sourceSongWords.contains("acoustic")
    val live = sourceSongWords.contains("live")
    val concert = sourceSongWords.contains("concert")
    val tour = sourceSongWords.contains("tour")
    val redux = sourceSongWords.contains("redux")

    fun shuffle(word: String): String {
        val chars = word.toCharArray()
        for (i in chars.indices) {
            val randomIndex = Random.nextInt(chars.size)
            chars[i] = chars[randomIndex]
        }
        return String(chars)
    }

    fun findSongIndex() : Int {
        for (i in 0..4) {
            val requiredSong = searchResults?.getOrNull(i)
            val requiredSongWords = filteredText(cleanPrefix(requiredSong?.title ?: ""))
                .split(" ").filter { it.isNotEmpty() }

            val songMatched = (requiredSong != null)
                    && (requiredSongWords.any { it in sourceSongWords })
                    && (if (lofi) (requiredSongWords.any { it == "lofi" }) else requiredSongWords.all { it != "lofi" })
                    && (if (rock) (requiredSongWords.any { it == "rock" }) else requiredSongWords.all { it != "rock" })
                    && (if (reprise) (requiredSongWords.any { it == "reprise" }) else requiredSongWords.all { it != "reprise" })
                    && (if (unplugged) (requiredSongWords.any { it == "unplugged" }) else requiredSongWords.all { it != "unplugged" })
                    && (if (instrumental) (requiredSongWords.any { it == "instrumental" }) else requiredSongWords.all { it != "instrumental" })
                    && (if (remix) (requiredSongWords.any { it == "remix" }) else requiredSongWords.all { it != "remix" })
                    && (if (acapella) (requiredSongWords.any { it == "acapella" }) else requiredSongWords.all { it != "acapella" })
                    && (if (acoustic) (requiredSongWords.any { it == "acoustic" }) else requiredSongWords.all { it != "acoustic" })
                    && (if (live) (requiredSongWords.any { it == "live" }) else requiredSongWords.all { it != "live" })
                    && (if (concert) (requiredSongWords.any { it == "concert" }) else requiredSongWords.all { it != "concert" })
                    && (if (tour) (requiredSongWords.any { it == "tour" }) else requiredSongWords.all { it != "tour" })
                    && (if (redux) (requiredSongWords.any { it == "redux" }) else requiredSongWords.all { it != "redux" })
                    && (if (song.asMediaItem.isExplicit) {requiredSong.asMediaItem.isExplicit} else {true})
                    && (if (isExtPlaylist) {(durationTextToMillis(requiredSong.durationText ?: "") - durationTextToMillis(song.durationText ?: "")).absoluteValue <= 2000}
            else {true})

            if (songMatched) return i
        }
        return -1
    }

    val matchedSong = searchResults?.getOrNull(findSongIndex())
    val artistsNames = matchedSong?.authors?.filter { it.endpoint != null }?.map { it.name }
    val artistNameString = matchedSong?.asMediaItem?.mediaMetadata?.artist?.toString() ?: ""
    val artistsIds = matchedSong?.authors?.filter { it.endpoint != null }?.map { it.endpoint?.browseId }

    Database.asyncTransaction {
        if (findSongIndex() != -1) {
            if (isYouTubeSyncEnabled() && playlist?.isYoutubePlaylist == true && playlist.isEditable){
                Database.asyncTransaction {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (removeYTSongFromPlaylist(
                                song.id,
                                playlist.browseId ?: "",
                                playlist.id
                            )
                        )
                            deleteSongFromPlaylist(song.id, playlist.id)

                    }
                }

            } else {
                deleteSongFromPlaylist(song.id, playlist?.id ?: 0L)
            }
            if (matchedSong != null) {
                if (songExist(matchedSong.asSong.id) == 0) {
                    Database.insert(matchedSong.asMediaItem)
                }
                insert(
                    SongPlaylistMap(
                        songId = matchedSong.asMediaItem.mediaId,
                        playlistId = playlistId,
                        position = position
                    ).default()
                )
                insert(
                    Album(id = matchedSong.album?.endpoint?.browseId ?: "", title = matchedSong.asMediaItem.mediaMetadata.albumTitle?.toString()),
                    SongAlbumMap(songId = matchedSong.asMediaItem.mediaId, albumId = matchedSong.album?.endpoint?.browseId ?: "", position = null)
                )
                CoroutineScope(Dispatchers.IO).launch {
                    val album = Database.album(matchedSong.album?.endpoint?.browseId ?: "").firstOrNull()
                    album?.copy(thumbnailUrl = matchedSong.thumbnail?.url)?.let { update(it) }

                    if (isYouTubeSyncEnabled() && playlist?.isYoutubePlaylist == true && playlist.isEditable){
                        EnvironmentExt.addToPlaylist(playlist.browseId ?: "", matchedSong.asMediaItem.mediaId)
                    }
                }
                if ((artistsNames != null) && (artistsIds != null)) {
                    artistsNames.let { artistNames ->
                        artistsIds.let { artistIds ->
                            if (artistNames.size == artistIds.size) {
                                insert(
                                    artistNames.mapIndexed { index, artistName ->
                                        Artist(id = (artistIds[index]) ?: "", name = artistName)
                                    },
                                    artistIds.map { artistId ->
                                        SongArtistMap(songId = matchedSong.asMediaItem.mediaId, artistId = (artistId) ?: "")
                                    }
                                )
                            }
                        }
                    }
                }
                Database.updateSongArtist(matchedSong.asMediaItem.mediaId, artistNameString)
                if (song.thumbnailUrl == "") Database.delete(song)
            }
        } else if (song.id == ((cleanPrefix(song.title)+song.artistsText).filter {it.isLetterOrDigit()})){
            songNotFound = song.copy(id = shuffle(song.artistsText+random4Digit+cleanPrefix(song.title)+"56Music").filter{it.isLetterOrDigit()})
            Database.delete(song)
            Database.insert(songNotFound)
            Database.insert(
                SongPlaylistMap(
                    songId = songNotFound.id,
                    playlistId = playlistId,
                    position = position
                ).default()
            )
        }
    }
}

suspend fun updateLocalPlaylist(song: Song){
    val searchQuery = Environment.searchPage(
        body = SearchBody(
            query = "${cleanPrefix(song.title)} ${song.artistsText}",
            params = Environment.SearchFilter.Song.value
        ),
        fromMusicShelfRendererContent = Environment.SongItem.Companion::from
    )

    val searchResults = searchQuery?.getOrNull()?.items

    fun findSongIndex() : Int {
        for (i in 0..9) {
            val requiredSong = searchResults?.getOrNull(i)
            val songMatched = (requiredSong?.asMediaItem?.mediaId) == (song.asMediaItem.mediaId)
            if (songMatched) return i
        }
        return -1
    }

    val matchedSong = searchResults?.getOrNull(findSongIndex())
    val artistsNames = matchedSong?.authors?.filter { it.endpoint != null }?.map { it.name }
    val artistNameString = matchedSong?.asMediaItem?.mediaMetadata?.artist?.toString() ?: ""
    val artistsIds = matchedSong?.authors?.filter { it.endpoint != null }?.map { it.endpoint?.browseId }

    Database.asyncTransaction {
        if (findSongIndex() != -1) {
            if (matchedSong != null) {
                insert(
                    Album(id = matchedSong.album?.endpoint?.browseId ?: "", title = matchedSong.asMediaItem.mediaMetadata.albumTitle?.toString()),
                    SongAlbumMap(songId = matchedSong.asMediaItem.mediaId, albumId = matchedSong.album?.endpoint?.browseId ?: "", position = null)
                )
                CoroutineScope(Dispatchers.IO).launch {
                    val album = Database.album(matchedSong.album?.endpoint?.browseId ?: "").firstOrNull()
                    album?.copy(thumbnailUrl = matchedSong.thumbnail?.url)?.let { update(it) }
                }

                if ((artistsNames != null) && (artistsIds != null)) {
                    artistsNames.let { artistNames ->
                        artistsIds.let { artistIds ->
                            if (artistNames.size == artistIds.size) {
                                insert(
                                    artistNames.mapIndexed { index, artistName ->
                                        Artist(id = (artistIds[index]) ?: "", name = artistName)
                                    },
                                    artistIds.map { artistId ->
                                        SongArtistMap(songId = song.id, artistId = (artistId) ?: "")
                                    }
                                )
                            }
                        }
                    }
                }
                Database.updateSongArtist(matchedSong.asMediaItem.mediaId, artistNameString)
            }
        }
    }
}

fun DownloadSyncedLyrics(it : SongEntity, coroutineScope : CoroutineScope){
    var lyrics by mutableStateOf<Lyrics?>(null)
    coroutineScope.launch {
        withContext(Dispatchers.IO) {
            Database.lyrics(it.asMediaItem.mediaId)
                .collect { currentLyrics ->
                    if (currentLyrics?.synced == null) {
                        lyrics = null
                        runCatching {
                            LrcLib.lyrics(
                                artist = it.song.artistsText
                                    ?: "",
                                title = cleanPrefix(it.song.title),
                                duration = durationTextToMillis(
                                    it.song.durationText
                                        ?: ""
                                ).milliseconds,
                                album = it.albumTitle
                            )?.onSuccess { lyrics ->
                                Database.upsert(
                                    Lyrics(
                                        songId = it.asMediaItem.mediaId,
                                        fixed = currentLyrics?.fixed,
                                        synced = lyrics?.text.orEmpty()
                                    )
                                )
                            }?.onFailure { lyrics ->
                                runCatching {
                                    KuGou.lyrics(
                                        artist = it.song.artistsText
                                            ?: "",
                                        title = cleanPrefix(
                                            it.song.title
                                        ),
                                        duration = durationTextToMillis(
                                            it.song.durationText
                                                ?: ""
                                        ) / 1000
                                    )?.onSuccess { lyrics ->
                                        Database.upsert(
                                            Lyrics(
                                                songId = it.asMediaItem.mediaId,
                                                fixed = currentLyrics?.fixed,
                                                synced = lyrics?.value.orEmpty()
                                            )
                                        )
                                    }?.onFailure {}
                                }.onFailure {}
                            }
                        }.onFailure {}
                    }
                }
        }
    }
}

suspend fun addToYtPlaylist(localPlaylistId: Long, position: Int, ytplaylistId: String, mediaItems: List<MediaItem>){
    val mediaItemsChunks = mediaItems.chunked(50)
    mediaItemsChunks.forEachIndexed { index, items ->
        if (mediaItems.size <= 50) {}
        else if (index == 0) {
            SmartMessage(
                "${mediaItems.size} "+appContext().resources.getString(R.string.songs_adding_in_yt),
                context = appContext(),
                durationLong = true
            )
        } else {
            delay(2000)
        }
        addToPlaylist(ytplaylistId, items.map { it.mediaId })
            .onSuccess {
                items.forEachIndexed { index, item ->
                    Database.asyncTransaction {
                        if (songExist(item.mediaId) == 0){
                            Database.insert(item)
                        }
                        Database.insert(
                            SongPlaylistMap(
                                songId = item.mediaId,
                                playlistId = localPlaylistId,
                                position = position + index
                            ).default()
                        )
                    }
                }
                if (items.size == 50) {
                    SmartMessage(
                        "${mediaItems.size - (index + 1) * 50} Songs Remaining",
                        context = appContext(),
                        durationLong = false
                    )
                }
            }
            .onFailure {
                println("YtMusic addToPlaylist (list of size ${items.size}) error: ${it.stackTraceToString()}")
                if(it is ClientRequestException && it.response.status == HttpStatusCode.BadRequest) {
                    SmartMessage(
                        appContext().resources.getString(R.string.adding_yt_to_pl_failed),
                        context = appContext(),
                        durationLong = false
                    )
                    items.forEach { item ->
                        delay(500)
                        addToPlaylist(ytplaylistId, item.mediaId)
                            .onFailure {
                            println("YtMusic addToPlaylist (list insert backup) error: ${it.stackTraceToString()}")
                                SmartMessage(
                                    appContext().resources.getString(R.string.songs_add_yt_failed)+"${item.mediaMetadata.title} - ${item.mediaMetadata.artist}",
                                    type = PopupType.Error,
                                    context = appContext(),
                                    durationLong = false
                                )
                            }.onSuccess {
                                Database.asyncTransaction {
                                    if (songExist(item.mediaId) == 0){
                                       Database.insert(item)
                                   }
                                   insert(
                                      SongPlaylistMap(
                                        songId = item.mediaId,
                                        playlistId = localPlaylistId,
                                        position = position
                                      ).default()
                                  )
                                }
                                SmartMessage(
                                    "${items.size - (index + 1)} Songs Remaining",
                                    context = appContext(),
                                    durationLong = false
                                )
                            }
                    }
                }
            }
    }
    SmartMessage(
        "${mediaItems.size} "+ appContext().resources.getString(R.string.songs_added_in_yt),
        context = appContext(),
        durationLong = true
    )
}

suspend fun addSongToYtPlaylist(localPlaylistId: Long, position: Int, ytplaylistId: String, mediaItem: MediaItem){
    if (isYouTubeSyncEnabled()) {
        addToPlaylist(ytplaylistId,mediaItem.mediaId)
            .onSuccess {
                Database.asyncTransaction {
                    if (songExist(mediaItem.mediaId) == 0) {
                        Database.insert(mediaItem)
                    }
                    insert(
                        SongPlaylistMap(
                            songId = mediaItem.mediaId,
                            playlistId = localPlaylistId,
                            position = position
                        ).default()
                    )
                }
                SmartMessage(
                    appContext().resources.getString(R.string.songs_add_yt_success),
                    context = appContext(),
                    durationLong = true
                )
            }
            .onFailure {
                SmartMessage(
                    appContext().resources.getString(R.string.songs_add_yt_failed),
                    context = appContext(),
                    durationLong = true
                )
            }

    }
}


@OptIn(UnstableApi::class)
suspend fun addToYtLikedSong(mediaItem: MediaItem){
    if (isYouTubeSyncEnabled()) {
        if (getLikedAt(mediaItem.mediaId) in listOf(-1L, null)) {
            likeVideoOrSong(mediaItem.mediaId)
                .onSuccess {
                    Database.asyncTransaction {
                        if (songExist(mediaItem.mediaId) == 0) {
                            Database.insert(mediaItem)
                        }
                        like(mediaItem.mediaId, System.currentTimeMillis())
                        MyDownloadHelper.autoDownloadWhenLiked(
                            context(),
                            mediaItem
                        )
                    }
                    SmartMessage(
                        appContext().resources.getString(R.string.songs_liked_yt),
                        context = appContext(),
                        durationLong = false
                    )
                }
                .onFailure {
                    SmartMessage(
                        appContext().resources.getString(R.string.songs_liked_yt_failed),
                        context = appContext(),
                        durationLong = false
                    )
                }
        } else {
            unlikeYtVideoOrSong(mediaItem)
        }
    }
}

@OptIn(UnstableApi::class)
suspend fun unlikeYtVideoOrSong(mediaItem: MediaItem){
    if(isYouTubeSyncEnabled()){
        removelikeVideoOrSong(mediaItem.mediaId)
            .onSuccess {
                Database.asyncTransaction {
                    if(songExist(mediaItem.mediaId) == 0){
                        insert(mediaItem)
                    }
                    like(mediaItem.mediaId, null)
                    MyDownloadHelper.autoDownloadWhenLiked(
                        context(),
                        mediaItem
                    )
                }
                SmartMessage(
                    appContext().resources.getString(R.string.song_unliked_yt),
                    context = appContext(),
                    durationLong = false
                )
            }
            .onFailure {
                SmartMessage(
                    appContext().resources.getString(R.string.songs_unliked_yt_failed),
                    context = appContext(),
                    durationLong = false
                )
            }
    }
}

@OptIn(UnstableApi::class)
suspend fun addToYtLikedSongs(mediaItems: List<MediaItem>){
    if (isYouTubeSyncEnabled()) {
        mediaItems.forEachIndexed { index, item ->
            delay(1000)
            likeVideoOrSong(item.mediaId).onSuccess {
                Database.asyncTransaction {
                    if (songExist(item.mediaId) == 0) {
                        Database.insert(item)
                    }
                    like(item.mediaId, System.currentTimeMillis())
                    MyDownloadHelper.autoDownloadWhenLiked(
                        context(),
                        item
                    )
                }
                SmartMessage(
                    "${index + 1}/${mediaItems.size} " + appContext().resources.getString(R.string.songs_liked_yt),
                    context = appContext(),
                    durationLong = false
                )
            }.onFailure {
                SmartMessage(
                    "${index + 1}/${mediaItems.size} " + appContext().resources.getString(R.string.songs_liked_yt_failed),
                    context = appContext(),
                    durationLong = false
                )
            }
        }
    }
}

fun numberFormatter(n: Int) =
    DecimalFormat("#,###")
        .format(n)
        .replace(",", ".")

inline fun <reified T : Throwable> Throwable.findCause(): T? {
    if (this is T) return this

    var th = cause
    while (th != null) {
        if (th is T) return th
        th = th.cause
    }

    return null
}