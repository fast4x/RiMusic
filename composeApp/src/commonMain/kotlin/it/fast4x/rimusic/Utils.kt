package it.fast4x.rimusic

import coil3.Uri
import coil3.toUri

const val PINNED_PREFIX = "pinned:"
const val MODIFIED_PREFIX = "modified:"
const val MONTHLY_PREFIX = "monthly:"
const val PIPED_PREFIX = "piped:"
const val EXPLICIT_PREFIX = "e:"
const val LOCAL_KEY_PREFIX = "local:"
const val YTP_PREFIX = "account:"


/**
 * Assumption: all prefixes end with ":" and have at least 1 (other) character.
 * Removes a "prefix of prefixes" including multiple times the same prefix (at different locations).
 */
fun cleanPrefix(text: String): String {
    val splitText = text.split(":")
    var i = 0
    while (i < splitText.size-1) {
        if ("${splitText[i]}:" !in listOf(PINNED_PREFIX, MODIFIED_PREFIX, MONTHLY_PREFIX, PIPED_PREFIX,
                EXPLICIT_PREFIX, LOCAL_KEY_PREFIX, YTP_PREFIX)) {
            break
        }
        i++
    }
    if(i >= splitText.size) return ""
    return splitText.subList(i, splitText.size).joinToString(":")
}

fun cleanString(text: String): String {
    var cleanText = text.replace("/", "", true)
    cleanText = cleanText.replace("#", "", true)
    return cleanText
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