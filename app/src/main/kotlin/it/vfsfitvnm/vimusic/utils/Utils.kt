package it.vfsfitvnm.vimusic.utils

import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.format.DateUtils
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import io.ktor.client.HttpClient
import io.ktor.client.plugins.UserAgent
import it.vfsfitvnm.innertube.Innertube
import it.vfsfitvnm.innertube.models.bodies.ContinuationBody
import it.vfsfitvnm.innertube.requests.playlistPage
import it.vfsfitvnm.innertube.utils.ProxyPreferences
import it.vfsfitvnm.innertube.utils.plus
import it.vfsfitvnm.vimusic.BuildConfig
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.models.Song
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.service.LOCAL_KEY_PREFIX
import it.vfsfitvnm.vimusic.service.isLocal
import it.vfsfitvnm.vimusic.ui.components.themed.NewVersionDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONException
import java.io.File
import java.net.InetSocketAddress
import java.net.Proxy
import java.time.Duration
import java.time.LocalTime
import java.util.Timer
import kotlin.concurrent.timerTask
import kotlin.time.Duration.Companion.minutes


fun songToggleLike( song: Song ) {
    query {
        if (Database.songExist(song.asMediaItem.mediaId) == 0)
            Database.insert(song.asMediaItem, Song::toggleLike)
        else {
            if (Database.songliked(song.asMediaItem.mediaId) == 0)
                Database.like(
                    song.asMediaItem.mediaId,
                    System.currentTimeMillis()
                )
            else Database.like(
                song.asMediaItem.mediaId,
                null
            )
        }
    }
}

fun mediaItemToggleLike( mediaItem: MediaItem ) {
    query {
        if (Database.songExist(mediaItem.mediaId) == 0)
            Database.insert(mediaItem, Song::toggleLike)
        else {
            if (Database.songliked(mediaItem.mediaId) == 0)
                Database.like(
                    mediaItem.mediaId,
                    System.currentTimeMillis()
                )
            else Database.like(
                mediaItem.mediaId,
                null
            )
        }
    }
}

val Innertube.SongItem.asMediaItem: MediaItem
    @UnstableApi
    get() = MediaItem.Builder()
        .setMediaId(key)
        .setUri(key)
        .setCustomCacheKey(key)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(info?.name)
                .setArtist(authors?.joinToString("") { it.name ?: "" })
                .setAlbumTitle(album?.name)
                .setArtworkUri(thumbnail?.url?.toUri())
                .setExtras(
                    bundleOf(
                        "albumId" to album?.endpoint?.browseId,
                        "durationText" to durationText,
                        "artistNames" to authors?.filter { it.endpoint != null }
                            ?.mapNotNull { it.name },
                        "artistIds" to authors?.mapNotNull { it.endpoint?.browseId },
                    )
                )
                .build()
        )
        .build()

val Innertube.VideoItem.asMediaItem: MediaItem
    @UnstableApi
    get() = MediaItem.Builder()
        .setMediaId(key)
        .setUri(key)
        .setCustomCacheKey(key)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(info?.name)
                .setArtist(authors?.joinToString("") { it.name ?: "" })
                .setArtworkUri(thumbnail?.url?.toUri())
                .setExtras(
                    bundleOf(
                        "durationText" to durationText,
                        "artistNames" to authors?.filter { it.endpoint != null }
                            ?.mapNotNull { it.name },
                        "artistIds" to authors?.mapNotNull { it.endpoint?.browseId },
                        "isOfficialMusicVideo" to isOfficialMusicVideo,
                        "isUserGeneratedContent" to isUserGeneratedContent
                        // "artistNames" to if (isOfficialMusicVideo) authors?.filter { it.endpoint != null }?.mapNotNull { it.name } else null,
                        // "artistIds" to if (isOfficialMusicVideo) authors?.mapNotNull { it.endpoint?.browseId } else null,
                    )
                )
                .build()
        )
        .build()
/*
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
                        "durationText" to durationText
                    )
                )
                .build()
        )
        .setMediaId(id)
        .setUri(id)
        .setCustomCacheKey(id)
        .build()

*/

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
                        "durationText" to durationText
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
    val hours = parts[0].toLong()
    val minutes = parts[1].toLong()
    return hours * 3600000 + minutes * 60000
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
    val timePart1 = Duration.ofMillis(millis / 60).toMinutes().minutes
    val timePart2 = Duration.ofMillis(millis / 60).seconds % 60

    return "${timePart1} ${timePart2}s"
}

fun formatTimelineSongDurationToTime(millis: Long) =
    Duration.ofMillis(millis*1000).toMinutes().minutes.toString()


suspend fun Result<Innertube.PlaylistOrAlbumPage>.completed(
    maxDepth: Int = Int.MAX_VALUE
): Result<Innertube.PlaylistOrAlbumPage>? {
    var playlistPage = getOrNull() ?: return null

    //playlistPage.songsPage?.continuation?.let { Log.d("mediaItem", it) }

    var depth = 0
    while (playlistPage.songsPage?.continuation != null && depth++ < maxDepth) {
        //Log.d("mediaItemDepth","depth $depth")
        val newSongs = Innertube.playlistPage(
            body = ContinuationBody(continuation = playlistPage.songsPage?.continuation!!)
        )?.getOrNull()?.takeIf { result ->
            //Log.d("mediaItemResult","result items ${result.items?.size}")
            result.items?.let { items ->
                items.isNotEmpty() && playlistPage.songsPage?.items?.none { it in items } != false
            } != false
        } ?: break

        playlistPage = playlistPage.copy(songsPage = playlistPage.songsPage + newSongs)
    }

    return Result.success(playlistPage)
}


@Composable
fun CheckAvailableNewVersion(
    onDismiss: () -> Unit
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

    //if (updatedVersionCode > getVersionCode().toInt()))
    if (updatedVersionCode > BuildConfig.VERSION_CODE)
        NewVersionDialog(
            updatedVersionName = updatedVersionName,
            updatedVersionCode = updatedVersionCode,
            updatedProductName = updatedProductName,
            onDismiss = onDismiss
        )
}

@Composable
fun isAvailableUpdate(): String {
    var newVersion = ""
    val file = File(LocalContext.current.filesDir, "RiMusicUpdatedVersion.ver")
    if (file.exists()) {
        newVersion = file.readText().substring(0, file.readText().length - 1)
        //Log.d("updatedVersion","${file.readText().length.toString()} ${file.readText().substring(0,file.readText().length-1)}")
        //Log.d("updatedVersion","${file.readText().length} ${newVersion.length}")
    } else newVersion = ""

    return if (newVersion == getVersionName() || newVersion == "") "" else newVersion
    //return if (newVersion == BuildConfig.VERSION_NAME || newVersion == "") "" else newVersion
}

@Composable
fun checkInternetConnection(): Boolean {
    val client = OkHttpClient()
    val request = OkHttpRequest(client)
    val coroutineScope = CoroutineScope(Dispatchers.Main)
    val url = "https://raw.githubusercontent.com/fast4x/RiMusic/master/updatedVersion/updatedVersionCode.ver"

    var check by remember {
        mutableStateOf("")
    }

    request.GET(url, object : Callback {
        override fun onResponse(call: Call, response: Response) {
            val responseData = response.body?.string()
            coroutineScope.launch {
                try {
                    responseData.let { check = it.toString() }
                    //Log.d("CheckInternet",check.substring(0,5))
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }

        override fun onFailure(call: Call, e: java.io.IOException) {
            //Log.d("CheckInternet","Check failure")
        }
    })

    //Log.d("CheckInternetRet",check)
    return check.isNotEmpty()
}


fun isNetworkAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        ?: return false
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val networkInfo = cm.activeNetwork
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        return networkInfo != null
    } else {
        return try {
            if (cm.activeNetworkInfo == null) {
                false
            } else {
                cm.activeNetworkInfo?.isConnected!!
            }
        } catch (e: Exception) {
            false
        }
    }

}

@Composable
fun isNetworkAvailableComposable(): Boolean {
    val context = LocalContext.current
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        ?: return false
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val networkInfo = cm.activeNetwork
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        return networkInfo != null
    } else {
        return try {
            if (cm.activeNetworkInfo == null) {
                false
            } else {
                cm.activeNetworkInfo?.isConnected!!
            }
        } catch (e: Exception) {
            false
        }
    }
}

/*
suspend fun Result<Innertube.PlaylistOrAlbumPage>.completed(): Result<Innertube.PlaylistOrAlbumPage>? {
    var playlistPage = getOrNull() ?: return null

    while (playlistPage.songsPage?.continuation != null) {
        val continuation = playlistPage.songsPage?.continuation!!
        val otherPlaylistPageResult = Innertube.playlistPage(ContinuationBody(continuation = continuation)) ?: break

        if (otherPlaylistPageResult.isFailure) break

        otherPlaylistPageResult.getOrNull()?.let { otherSongsPage ->
            playlistPage = playlistPage.copy(songsPage = playlistPage.songsPage + otherSongsPage)
        }
    }

    return Result.success(playlistPage)
}
 */

fun getHttpClient() = HttpClient() {
    install(UserAgent) {
        agent = "Mozilla/5.0 (Windows NT 10.0; rv:91.0) Gecko/20100101 Firefox/91.0"
    }
    engine {
        ProxyPreferences.preference?.let{
            proxy = Proxy(it.proxyMode, InetSocketAddress(it.proxyHost, it.proxyPort))
        }

    }
}

@Composable
fun getVersionName(): String {
    val context = LocalContext.current
    try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return pInfo.versionName
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




inline val isAtLeastAndroid6
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

inline val isAtLeastAndroid8
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

inline val isAtLeastAndroid10
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

inline val isAtLeastAndroid11
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
inline val isAtLeastAndroid12
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

inline val isAtLeastAndroid13
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
