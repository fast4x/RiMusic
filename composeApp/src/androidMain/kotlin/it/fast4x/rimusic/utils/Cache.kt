@file:OptIn(UnstableApi::class)

package it.fast4x.rimusic.utils

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.Cache.CacheException
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheSpan
import java.io.File

val Cache.asDataSource get() = CacheDataSource.Factory().setCache(this)

class ReadOnlyException : CacheException("Cache is read-only")

class ConditionalReadOnlyCache(
    private val cache: Cache,
    private val readOnly: () -> Boolean
) : Cache by cache {
    private fun stub() = if (readOnly()) throw ReadOnlyException() else Unit

    override fun startFile(key: String, position: Long, length: Long): File {
        stub()
        return cache.startFile(key, position, length)
    }

    override fun commitFile(file: File, length: Long) {
        stub()
        cache.commitFile(file, length)
    }

    override fun releaseHoleSpan(holeSpan: CacheSpan) {
        stub()
        cache.releaseHoleSpan(holeSpan)
    }
}

fun Cache.readOnlyWhen(readOnly: () -> Boolean) = ConditionalReadOnlyCache(
    cache = this,
    readOnly = readOnly
)
