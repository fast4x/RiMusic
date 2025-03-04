package it.fast4x.rimusic.utils

import coil3.Uri
import coil3.toUri
import database.entities.Song
import io.ktor.client.HttpClient
import io.ktor.client.plugins.UserAgent
import it.fast4x.environment.Environment
import it.fast4x.environment.models.bodies.ContinuationBody
import it.fast4x.environment.requests.playlistPage
import it.fast4x.environment.utils.ProxyPreferences
import database.entities.SongEntity
import it.fast4x.environment.utils.getProxy

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

fun getHttpClient() = HttpClient() {
    install(UserAgent) {
        agent = "Mozilla/5.0 (Windows NT 10.0; rv:91.0) Gecko/20100101 Firefox/91.0"
    }
    engine {
        ProxyPreferences.preference?.let{
            proxy = getProxy(it)
        }
    }
}

suspend fun Result<Environment.PlaylistOrAlbumPage>.completed(
    maxDepth: Int =  Int.MAX_VALUE
) = runCatching {
    val page = getOrThrow()
    val songs = page.songsPage?.items.orEmpty().toMutableList()
    var continuation = page.songsPage?.continuation

    var depth = 0
    var continuationsList = arrayOf<String>()
    //continuationsList += continuation.orEmpty()

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

    page.copy(songsPage = Environment.ItemsPage(items = songs, continuation = null))
}.also { it.exceptionOrNull()?.printStackTrace() }

val Environment.SongItem.asSong: Song
    get() = Song (
        id = key,
        title = info?.name ?: "",
        artistsText = authors?.joinToString(", ") { it.name ?: "" },
        durationText = durationText,
        thumbnailUrl = thumbnail?.url
    )

val Environment.SongItem.asSongEntity: SongEntity
    get() = SongEntity (
        song = Song (
            id = key,
            title = info?.name ?: "",
            artistsText = authors?.joinToString(", ") { it.name ?: "" },
            durationText = durationText,
            thumbnailUrl = thumbnail?.url
            ),
        contentLength = null,
        albumTitle = null
    )