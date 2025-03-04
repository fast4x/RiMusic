package it.fast4x.environment.utils

import io.ktor.utils.io.CancellationException
import it.fast4x.environment.Environment
import it.fast4x.environment.EnvironmentExt
import it.fast4x.environment.models.SectionListRenderer
import it.fast4x.environment.requests.ArtistItemsPage
import it.fast4x.environment.requests.PlaylistPage
import java.security.MessageDigest

@JvmName("getPlaylistCompleted")
suspend fun Result<PlaylistPage>.completed(): Result<PlaylistPage> = runCatching {
    val page = getOrThrow()
    val songs = page.songs.toMutableList()
    var continuation = page.songsContinuation


    println("getPlaylist complete PlaylistPage songs: ${songs.size} continuation: ${continuation}")

    while (continuation != null) {
        val continuationPage = EnvironmentExt.getPlaylistContinuation(continuation).getOrNull()
        if (continuationPage != null) {
            songs += continuationPage.songs
        }
        continuation = continuationPage?.continuation
    }
    PlaylistPage(
        playlist = page.playlist,
        songs = songs,
        songsContinuation = null,
        continuation = page.continuation,
        description = page.description,
        isEditable = page.isEditable,
    )
}

@JvmName("getArtistItemsPageCompleted")
suspend fun Result<ArtistItemsPage>.completed(): Result<ArtistItemsPage> = runCatching {
    val page = getOrThrow()
    var items = page.items
    var continuation = page.continuation


    println("getArtistItemsPage complete ArtistItemsPage items: ${items.size} continuation: ${continuation}")

    while (continuation != null) {
        val continuationPage = EnvironmentExt.getArtistItemsContinuation(continuation).getOrNull()
        if (continuationPage != null) {
            items += continuationPage.items
        }
        continuation = continuationPage?.continuation
    }
    ArtistItemsPage(
        title = page.title,
        items = items,
        continuation = page.continuation
    )
}

internal fun SectionListRenderer.findSectionByTitle(text: String): SectionListRenderer.Content? {
    return contents?.find { content ->
        val title = content
            .musicCarouselShelfRenderer
            ?.header
            ?.musicCarouselShelfBasicHeaderRenderer
            ?.title
            ?: content
                .musicShelfRenderer
                ?.title

        title
            ?.runs
            ?.firstOrNull()
            ?.text == text
    }
}

internal fun SectionListRenderer.findSectionByStrapline(text: String): SectionListRenderer.Content? {
    return contents?.find { content ->
        content
            .musicCarouselShelfRenderer
            ?.header
            ?.musicCarouselShelfBasicHeaderRenderer
            ?.strapline
            ?.runs
            ?.firstOrNull()
            ?.text == text
    }
}

internal inline fun <R> runCatchingNonCancellable(block: () -> R): Result<R>? {
    val result = runCatching(block)
    return when (result.exceptionOrNull()) {
        is CancellationException -> null
        else -> result
    }
}

internal inline fun <T> runCatchingCancellable(block: () -> T) =
    runCatching(block).takeIf { it.exceptionOrNull() !is CancellationException }

infix operator fun <T : Environment.Item> Environment.ItemsPage<T>?.plus(other: Environment.ItemsPage<T>) =
    other.copy(
        items = (this?.items?.plus(other.items ?: emptyList())
            ?: other.items)?.distinctBy(Environment.Item::key)
    )

fun parseCookieString(cookie: String): Map<String, String> =
    cookie.split("; ")
        .filter { it.isNotEmpty() }
        .associate {
            val (key, value) = it.split("=")
            key to value
        }

fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }
fun sha1(str: String): String = MessageDigest.getInstance("SHA-1").digest(str.toByteArray()).toHex()

