@file:OptIn(UnstableApi::class)

package it.fast4x.rimusic.utils

import android.content.Context
import androidx.annotation.OptIn
import androidx.core.text.isDigitsOnly
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.Cache.CacheException
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheSpan
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import it.fast4x.rimusic.appContext
import it.fast4x.rimusic.enums.ExoPlayerCacheLocation
import it.fast4x.rimusic.enums.ExoPlayerDiskCacheMaxSize
import it.fast4x.rimusic.utils.principalCache.exoPlayerCustomCache
import java.io.File

object principalCache {
    private val exoPlayerCustomCache = appContext().preferences.getInt(exoPlayerCustomCacheKey, 32) * 1000 * 1000L
    private var principalCache: SimpleCache? = null
    private var databaseProvider: StandaloneDatabaseProvider? = null
    private val exoPlayerCacheLocation = appContext().preferences.getEnum(
        exoPlayerCacheLocationKey, ExoPlayerCacheLocation.System
    )
    private val directoryLocation =
        if (exoPlayerCacheLocation == ExoPlayerCacheLocation.Private) appContext().filesDir else appContext().cacheDir

    private val cacheSize =
        appContext().preferences.getEnum(exoPlayerDiskCacheMaxSizeKey, ExoPlayerDiskCacheMaxSize.`2GB`)

    private val cacheDirName = if (cacheSize == ExoPlayerDiskCacheMaxSize.Disabled) "rimusic_no_cache" else "rimusic_cache"

    private val directory = directoryLocation.resolve(cacheDirName).also { dir ->
        if (dir.exists()) return@also

        dir.mkdir()

        directoryLocation.listFiles()?.forEach { file ->
            if (file.isDirectory && file.name.length == 1 && file.name.isDigitsOnly() || file.extension == "uid") {
                if (!file.renameTo(dir.resolve(file.name))) {
                    file.deleteRecursively()
                }
            }
        }

        appContext().filesDir.resolve("coil").deleteRecursively()
    }
    private val cacheEvictor = when (val size =
        appContext().preferences.getEnum(exoPlayerDiskCacheMaxSizeKey, ExoPlayerDiskCacheMaxSize.`2GB`)) {
        ExoPlayerDiskCacheMaxSize.Unlimited -> NoOpCacheEvictor()
        ExoPlayerDiskCacheMaxSize.Custom -> LeastRecentlyUsedCacheEvictor(exoPlayerCustomCache)
        else -> LeastRecentlyUsedCacheEvictor(size.bytes)
    }

    fun getDatabaseProvider(context: Context): StandaloneDatabaseProvider {
        if (databaseProvider == null) databaseProvider = StandaloneDatabaseProvider(context)
        return databaseProvider as StandaloneDatabaseProvider
    }

    fun getInstance(context: Context): SimpleCache {
        if (principalCache == null) principalCache = SimpleCache(directory, cacheEvictor, getDatabaseProvider(context))
        return principalCache as SimpleCache
    }
}

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
