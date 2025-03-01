package it.fast4x.rimusic.utils

import androidx.media3.common.MediaMetadata
import androidx.media3.common.Timeline
import it.fast4x.environment.Environment
import it.fast4x.rimusic.models.Album
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.models.SongEntity

enum class TokenType { REQUIRED, EXCLUDED }
data class Token(val field: String, val value: String, val type: TokenType)

// TODO explicit operator (?)
fun parseSearchQuery(query: String): List<List<Token>> {
    // The search tokens can be labeled (with quotes), labeled (without quotes), and unlabeled.
    val regex = Regex("""(-?)(title|artist|album):"([^"]+)"|(-?)(title|artist|album):(\S+)|(-?)(\S+)""")
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

fun filterMediaMetadata(metadata: MediaMetadata, filter: String): Boolean {
    val filterTrim = filter.trim()
    if (filterTrim.isBlank()) return true // Default should let everything be shown.

    val tokenGroups = parseSearchQuery(filterTrim) // TODO this is done more than it needs to be.
    val exclusionTokens = tokenGroups.flatten().filter { it.type == TokenType.EXCLUDED }
    val orGroups = tokenGroups.map { group -> group.filter { it.type != TokenType.EXCLUDED } }

    val metadataFields = mapOf(
        "title" to (metadata.title ?: ""),
        "artist" to (metadata.artist ?: ""),
        "album" to (metadata.albumTitle ?: ""),
        // TODO year / range of years.
    )

    // Step 1: Apply OR groups (at least one group must match)
    val matchesOrCondition = orGroups.any { group ->
        group.all { token ->
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
    return items.filter { song ->
        val metadata = MediaMetadata.Builder()
            .setTitle(song.cleanTitle())
            .setArtist(song.artistsText)
            // TODO: album. use database ?
            .build()
        filterMediaMetadata(metadata, filter)
    }
}

// Filter function for InnerTube.SongItem (used in playlists)
fun filterInnerTubeSongs(items: List<Environment.SongItem>?, filter: String): List<Environment.SongItem> {
    return items?.filter { songItem ->
        filterMediaMetadata(songItem.asMediaItem.mediaMetadata, filter)
    }!!
}

// Filter function for SongEntity
fun filterSongEntities(items: List<SongEntity>, filter: String): List<SongEntity> {
    return items.filter { entity ->
        val metadata = MediaMetadata.Builder()
            .setTitle(entity.song.title)
            .setArtist(entity.song.artistsText)
            .setAlbumTitle(entity.albumTitle)
            // TODO year.
            .build()
        filterMediaMetadata(metadata, filter)
    }
}

// Filter function for Timeline.Window (Queue item)
fun filterWindowSongs(items: List<Timeline.Window>, filter: String): List<Timeline.Window> {
    return items.filter { window ->
        filterMediaMetadata(window.mediaItem.mediaMetadata, filter)
    }
}

// Filter function for Album
fun filterAlbums(items: List<Album>, filter: String): List<Album> {
    return items.filter { album ->
        val metadata = MediaMetadata.Builder()
            .setTitle(album.title)
            .setArtist(album.authorsText)
            .setReleaseYear(album.year?.toInt()) // TODO not yet implemented.
            .build()
        filterMediaMetadata(metadata, filter)
    }
}
