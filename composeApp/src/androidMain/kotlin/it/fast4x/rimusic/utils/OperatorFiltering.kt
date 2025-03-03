package it.fast4x.rimusic.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Timeline
import it.fast4x.environment.Environment
import it.fast4x.rimusic.R
import it.fast4x.rimusic.context
import it.fast4x.rimusic.models.Album
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.models.SongEntity

enum class TokenType { REQUIRED, EXCLUDED }
data class Token(val field: String, val value: String, val type: TokenType)

/*
    TODO explicit operator (?)
    TODO OR localization
    TODO year range possibly
*/
fun parseSearchQuery(query: String): List<List<Token>> {
    // The search tokens can be labeled (with quotes), labeled (without quotes), and unlabeled.
    val regex = Regex("""(-?)(\S+):"([^"]+)"|(-?)(\S+):(\S+)|(-?)(\S+)""")
    val tokens = mutableListOf<List<Token>>()
    var currentGroup = mutableListOf<Token>()

    // Find all the search tokens.
    regex.findAll(query).forEach { match ->
        val (neg1, field1, quoted1, neg2, field2, value2, neg3, value3) = match.destructured

        val type = when {
            neg1 == "-" || neg2 == "-" || neg3 == "-" -> TokenType.EXCLUDED
            else -> TokenType.REQUIRED
        }

        val field = field1.ifEmpty { field2.ifEmpty { "" } }
        val value = quoted1.ifEmpty { value2.ifEmpty { value3 } }

        // By default, everything is AND (original behavior). This lets OR work.
        if (value.equals("OR", ignoreCase = true) || value == "|") {
            tokens.add(currentGroup)
            currentGroup = mutableListOf()
        } else {
            currentGroup.add(Token(field, value, type))
        }
    }

    if (currentGroup.isNotEmpty()) tokens.add(currentGroup)
    return tokens
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
    val exclusionTokens = tokenGroups.flatten().filter { it.type == TokenType.EXCLUDED }
    val orGroups = tokenGroups.map { group: List<Token> -> group.filter { it.type != TokenType.EXCLUDED } }

    // Map labels to what the correspond to.
    val metadataFields = mapOf(
        context().getString(R.string.sort_title) to (metadata.title ?: ""),
        context().getString(R.string.sort_artist) to (metadata.artist ?: ""),
        context().getString(R.string.sort_album) to (metadata.albumTitle ?: ""),
        context().getString(R.string.sort_year) to (metadata.releaseYear.toString()),
    )

    // Step 1: Apply OR groups (at least one group must match)
    val matchesOrCondition = orGroups.any { group ->
        group.all { token: Token ->
            val searchFields = if (metadataFields.containsKey(token.field)) {
                listOf(metadataFields[token.field] ?: "")
            } else {
                metadataFields.values
            }

            searchFields.any { it.contains(token.value, ignoreCase = true) }
        }
    }

    // Step 2: Apply exclusions (AFTER OR logic)
    val passesExclusions = exclusionTokens.none { token ->
        metadataFields.values.any { it.contains(token.value, ignoreCase = true) }
    }

    return matchesOrCondition && passesExclusions
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
