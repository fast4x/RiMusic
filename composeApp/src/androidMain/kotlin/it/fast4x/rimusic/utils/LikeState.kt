package it.fast4x.rimusic.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import it.fast4x.rimusic.Database
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun getLikeState(mediaId: String): Int {
    var likedAt by remember {
        mutableStateOf<Long?>(null)
    }

    LaunchedEffect(mediaId) {
        Database.likedAt(mediaId).distinctUntilChanged().collect { likedAt = it }
    }

    return when (likedAt) {
        -1L -> getDislikedIcon()
        null -> getUnlikedIcon()
        else -> getLikedIcon()
    }

}

fun setLikeState(likedAt: Long?): Long? {
    val current =
     when (likedAt) {
        -1L -> null
        null -> System.currentTimeMillis()
        else -> -1L
    }
    //println("mediaItem setLikeState: $current")
    return current
}

fun setDisLikeState(likedAt: Long?): Long? {
    val current =
        when (likedAt) {
            -1L -> null
            null -> -1L
            else -> -1L
        }
    return current
}

