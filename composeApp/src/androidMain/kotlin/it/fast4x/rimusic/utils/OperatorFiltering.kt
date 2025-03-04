package it.fast4x.rimusic.utils

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import it.fast4x.environment.Environment
import it.fast4x.rimusic.R
import it.fast4x.rimusic.context
import it.fast4x.rimusic.models.Album
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.models.SongEntity
import kotlin.text.contains

data class Token(val field: String, val value: String,
                 val shouldInclude: Boolean, val valueType: String? = null)

/*
    TODO explicit operator (?)
    TODO OR localization
    TODO do songs don't have year.
*/
fun parseSearchQuery(query: String): List<List<Token>> {
    // The search tokens can be labeled (with quotes), labeled (without quotes), and unlabeled.
    val regex = Regex("""(-?)(\S+):"([^"]+)"|(-?)(\S+):(\S+)|(-?)(\S+)""")
    val tokens = mutableListOf<List<Token>>()
    var currentGroup = mutableListOf<Token>()

    // Find all the search tokens.
    regex.findAll(query).forEach { match ->
        val (neg1, field1, quoted1, neg2, field2, value2, neg3, value3) = match.destructured

        val include = !(neg1 == "-" || neg2 == "-" || neg3 == "-")

        val field = field1.ifEmpty { field2.ifEmpty { "" } }
        val value = quoted1.ifEmpty { value2.ifEmpty { value3 } }

        // By default, everything is AND (original behavior). This lets OR work.
        if (value.equals("OR", ignoreCase = true) || value == "|") {
            tokens.add(currentGroup)
            currentGroup = mutableListOf()
        } else {
            val valueType = if (value.contains("-") && value.contains(":")) "DurationRange"
                else if (value.contains("-")) "IntRange" else null
            currentGroup.add(Token(field.lowercase(), value, include, valueType))
        }
    }

    if (currentGroup.isNotEmpty()) tokens.add(currentGroup)
    return tokens
}

fun isWithinIntRange(number: String, range: String): Boolean {
    var (min, max) = range.split("-").map { it.toIntOrNull() }
    min = min ?: 0
    max = max ?: Int.MAX_VALUE
    return number.toIntOrNull()?.let { it in min..max } == true
}

fun isWithinDurationRange(duration: String, range: String): Boolean {
    var (min, max) = range.split("-").map { durationTextToMillis(it) }
    if (max == 0L) max = Long.MAX_VALUE // Default to infinite
    return durationTextToMillis(duration) in min..max == true
}

fun getSearchFields(metadataFields: Map<String, String>, token: Token) =
    if (metadataFields.containsKey(token.field)) {
        listOf(metadataFields[token.field] ?: "")
    } else {
        metadataFields.values
    }

var tokensCache: Pair<String, List<List<Token>>>? = null

fun filterMediaMetadata(metadata: MediaMetadata, filter: String): Boolean {
    val filterTrim = filter.trim()
    if (filterTrim.isBlank()) return true // Default should let everything be shown.

    // If in the cache, do not re parse the search query.
    val tokenGroups: List<List<Token>> = when (tokensCache) {
        null -> parseSearchQuery(filterTrim)
        else -> when (tokensCache!!.first) {
            filter -> tokensCache!!.second
            else -> parseSearchQuery(filterTrim)
        }
    }
    tokensCache = filter to tokenGroups

    // Map labels to what the correspond to.
    val metadataFields = mapOf(
        context().getString(R.string.sort_title).lowercase() to (metadata.title.toString()),
        context().getString(R.string.sort_artist).lowercase() to (metadata.artist.toString()),
        context().getString(R.string.sort_duration).lowercase()
                to (metadata.extras?.getString("durationText").toString()),
        context().getString(R.string.explicit).lowercase()
                to (metadata.extras?.getString("explicit").toString()),
        context().getString(R.string.sort_album).lowercase() to (metadata.albumTitle.toString()),
        context().getString(R.string.sort_year).lowercase() to (metadata.releaseYear.toString()),
    )

    val included = tokenGroups.any { group ->
        group.all { token ->
            getSearchFields(metadataFields, token).any {
                val groupApplies = when(token.valueType) {
                    "IntRange" -> isWithinIntRange(it, token.value)
                    "DurationRange" -> isWithinDurationRange(it, token.value)
                    else -> it.contains(token.value, ignoreCase = true)
                }
                groupApplies == token.shouldInclude
            }
        }
    }

    return included
}

// Filter function for Song
fun filterSongs(items: List<Song>, filter: String): List<Song> {
    return items.filter { it -> filterMediaMetadata(it.asMediaItem.mediaMetadata, filter) }
}

// Filter function for InnerTube.SongItem (used in playlists)
fun filterSongItems(items: List<Environment.SongItem>?, filter: String): List<Environment.SongItem> {
    return items?.filter { it -> filterMediaMetadata(it.asMediaItem.mediaMetadata, filter) }!!
}

// Filter function for SongEntity
fun filterSongEntities(items: List<SongEntity>, filter: String): List<SongEntity> {
    return items.filter { filterMediaMetadata(it.asMediaItem.mediaMetadata, filter) }
}

// Filter function for Timeline.Window (Queue item)
fun filterWindowSongs(items: List<Timeline.Window>, filter: String): List<Timeline.Window> {
    return items.filter { filterMediaMetadata(it.mediaItem.mediaMetadata, filter) }
}

// Filter function for Album
fun filterAlbums(items: List<Album>, filter: String): List<Album> {
    return items.filter { album ->
        val metadata = MediaMetadata.Builder()
            .setAlbumTitle(album.title)
            .setArtist(album.authorsText)
            .setReleaseYear(album.year?.toInt())
            .build()
        filterMediaMetadata(metadata, filter)
    }
}
