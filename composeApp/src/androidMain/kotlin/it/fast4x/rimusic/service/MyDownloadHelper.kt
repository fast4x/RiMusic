package it.fast4x.rimusic.service


import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
//import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.scheduler.Requirements
import it.fast4x.innertube.Innertube
import it.fast4x.innertube.models.bodies.PlayerBody
import it.fast4x.innertube.requests.player
import it.fast4x.innertube.utils.ProxyPreferences
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.enums.AudioQualityFormat
import it.fast4x.rimusic.models.Format
import it.fast4x.rimusic.query
import it.fast4x.rimusic.transaction
import it.fast4x.rimusic.utils.RingBufferPrevious
import it.fast4x.rimusic.utils.audioQualityFormatKey
import it.fast4x.rimusic.utils.defaultDataSourceFactory
import it.fast4x.rimusic.utils.download
import it.fast4x.rimusic.utils.getEnum
import it.fast4x.rimusic.utils.getPipedSession
import it.fast4x.rimusic.utils.preferences
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import timber.log.Timber
import java.io.File
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.Duration
import java.util.concurrent.Executors

@UnstableApi
object MyDownloadHelper {
    private val executor = Executors.newCachedThreadPool()
    private val coroutineScope = CoroutineScope(
        executor.asCoroutineDispatcher() +
                SupervisorJob() +
                CoroutineName("MyDownloadService-Worker-Scope")
    )

    // While the class is not a singleton (lifecycle), there should only be one download state at a time
    private val mutableDownloadState = MutableStateFlow(false)
    val downloadState = mutableDownloadState.asStateFlow()
    private val downloadQueue =
        Channel<DownloadManager>(onBufferOverflow = BufferOverflow.DROP_OLDEST)

    const val DOWNLOAD_NOTIFICATION_CHANNEL_ID = "download_channel"

    private const val DOWNLOAD_CONTENT_DIRECTORY = "downloads"

    private lateinit var databaseProvider: DatabaseProvider
    lateinit var downloadCache: Cache

    private lateinit var downloadNotificationHelper: DownloadNotificationHelper
    private lateinit var downloadDirectory: File
    private lateinit var downloadManager: DownloadManager
    lateinit var audioQualityFormat: AudioQualityFormat
    //private lateinit var connectivityManager: ConnectivityManager


    var downloads = MutableStateFlow<Map<String, Download>>(emptyMap())

    fun getDownload(songId: String): Flow<Download?> {
        return downloads.map { it[songId] }

    }

    @SuppressLint("LongLogTag")
    @Synchronized
    fun getDownloads() {
        val result = mutableMapOf<String, Download>()
        val cursor = downloadManager.downloadIndex.getDownloads()
        while (cursor.moveToNext()) {
            result[cursor.download.request.id] = cursor.download
        }
        downloads.value = result

    }


    @SuppressLint("SuspiciousIndentation")
    @Synchronized
    fun getResolvingDataSourceFactory(context: Context): ResolvingDataSource.Factory {
        audioQualityFormat =
            context.preferences.getEnum(audioQualityFormatKey, AudioQualityFormat.Auto)

        //connectivityManager =
        //    getSystemService(context, ConnectivityManager::class.java) as ConnectivityManager

        val dataSourceFactory =
            ResolvingDataSource.Factory(createCacheDataSource(context)) { dataSpec ->
                val videoId = dataSpec.key?.removePrefix("https://youtube.com/watch?v=")
                    ?: error("A key must be set")

                //val chunkLength = 512 * 1024L
                //val chunkLength = 1024 * 1024L
                //val chunkLength = 10000 * 1024L
                //val chunkLength = 30000 * 1024L
                val chunkLength = 180000 * 1024L
                val ringBuffer = RingBufferPrevious<Pair<String, Uri>?>(2) { null }

                if (
                    dataSpec.isLocal ||
                    downloadCache.isCached(videoId, dataSpec.position, chunkLength)
                ) {
                    dataSpec
                } else {
                    when (videoId) {
                        ringBuffer.getOrNull(0)?.first -> dataSpec.withUri(ringBuffer.getOrNull(0)!!.second)
                        ringBuffer.getOrNull(1)?.first -> dataSpec.withUri(ringBuffer.getOrNull(1)!!.second)
                        "initVideoId" -> dataSpec
                        else -> run {
                            val body = runBlocking(Dispatchers.IO) {
                                Innertube.player(
                                    body = PlayerBody(videoId = videoId),
                                    pipedSession = getPipedSession().toApiSession()
                                )
                            }?.getOrElse { throwable ->
                                when (throwable) {
                                    is ConnectException, is UnknownHostException -> throw NoInternetException()
                                    is SocketTimeoutException -> throw TimeoutException()
                                    else -> throw UnknownException()
                                }
                            }
                            println("MyDownloadHelper createDataSourceResolverFactory body playabilityStatus ${body?.playabilityStatus?.status}")

                            val format = when (audioQualityFormat) {
                                AudioQualityFormat.Auto -> body?.streamingData?.highestQualityFormat
                                AudioQualityFormat.High -> body?.streamingData?.highestQualityFormat
                                AudioQualityFormat.Medium -> body?.streamingData?.mediumQualityFormat
                                AudioQualityFormat.Low -> body?.streamingData?.lowestQualityFormat
                            } ?: error("No format found") //throw PlayableFormatNotFoundException()

                            println("MyDownloadHelper createDataSourceResolverFactory adaptiveFormats available bitrate ${body?.streamingData?.adaptiveFormats?.map { it.mimeType }}")
                            println("MyDownloadHelper createDataSourceResolverFactory adaptiveFormats selected $format")

                            val url = when (body?.playabilityStatus?.status) {
                                    "OK" -> {
                                        format.let { formatIn ->
                                            query {
                                                if (Database.songExist(videoId) == 1) {
                                                    Database.upsert(
                                                        Format(
                                                            songId = videoId,
                                                            itag = formatIn.itag,
                                                            mimeType = formatIn.mimeType,
                                                            bitrate = formatIn.bitrate,
                                                            loudnessDb = body.playerConfig?.audioConfig?.normalizedLoudnessDb,
                                                            contentLength = formatIn.contentLength,
                                                            lastModified = formatIn.lastModified
                                                        )
                                                    )
                                                }
                                            }

                                            formatIn.url
                                        } ?: throw PlayableFormatNotFoundException()
                                    }

                                    "UNPLAYABLE" -> throw UnplayableException()
                                    "LOGIN_REQUIRED" -> throw LoginRequiredException()
                                    else -> throw UnknownException()
                                }


                            val uri = url.toUri()
                            ringBuffer.append(videoId to uri)

                            dataSpec
                                .withUri(uri)

                        }
                    }
                }


            }
        return dataSourceFactory
    }

    private fun okHttpClient(): OkHttpClient {
        ProxyPreferences.preference?.let {
            return OkHttpClient.Builder()
                .proxy(
                    Proxy(
                        it.proxyMode,
                        InetSocketAddress(it.proxyHost, it.proxyPort)
                    )
                )
                .connectTimeout(Duration.ofSeconds(16))
                .readTimeout(Duration.ofSeconds(8))
                .build()
        }
        return OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(16))
            .readTimeout(Duration.ofSeconds(8))
            .build()
    }

    private fun createCacheDataSource(context: Context): DataSource.Factory {
        return CacheDataSource.Factory()
            .setCache(getDownloadCache(context)).apply {
            setUpstreamDataSourceFactory(
                context.defaultDataSourceFactory
                //OkHttpDataSource.Factory(okHttpClient())
                //    .setUserAgent("Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Mobile Safari/537.36")
                /*
                DefaultHttpDataSource.Factory()
                    .setConnectTimeoutMs(16000)
                    .setReadTimeoutMs(8000)
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; rv:91.0) Gecko/20100101 Firefox/91.0")

                 */
            )
            setCacheWriteDataSinkFactory(null)
        }
    }


    @Synchronized
    fun getDownloadNotificationHelper(context: Context?): DownloadNotificationHelper {
        if (!MyDownloadHelper::downloadNotificationHelper.isInitialized) {
            downloadNotificationHelper =
                DownloadNotificationHelper(context!!, DOWNLOAD_NOTIFICATION_CHANNEL_ID)
        }
        return downloadNotificationHelper
    }

    @Synchronized
    fun getDownloadManager(context: Context): DownloadManager {
        ensureDownloadManagerInitialized(context)
        return downloadManager
    }

    /*
        @Synchronized
        fun getDownloadTracker(context: Context): DownloadTracker {
            ensureDownloadManagerInitialized(context)
            return downloadTracker
        }

     */


    /*
    fun getDownloadString(context: Context, @Download.State downloadState: Int): String {
        return when (downloadState) {
            /*
            Download.STATE_COMPLETED -> context.resources.getString(R.string.exo_download_completed)
            Download.STATE_DOWNLOADING -> context.resources.getString(R.string.exo_download_downloading)
            Download.STATE_FAILED -> context.resources.getString(R.string.exo_download_failed)
            Download.STATE_QUEUED -> context.resources.getString(R.string.exo_download_queued)
            Download.STATE_REMOVING -> context.resources.getString(R.string.exo_download_removing)
            Download.STATE_RESTARTING -> context.resources.getString(R.string.exo_download_restarting)
            Download.STATE_STOPPED -> context.resources.getString(R.string.exo_download_stopped)
            else -> throw IllegalArgumentException()
             */
            Download.STATE_COMPLETED -> "Completed"
            Download.STATE_DOWNLOADING -> "Downloading"
            Download.STATE_FAILED -> "Failed"
            Download.STATE_QUEUED -> "Queued"
            Download.STATE_REMOVING -> "Removing"
            Download.STATE_RESTARTING -> "Restarting"
            Download.STATE_STOPPED -> "Stopped"
            else -> throw IllegalArgumentException()

        }
    }
    */

    @Synchronized
    fun getDownloadCache(context: Context): Cache {
        if (!MyDownloadHelper::downloadCache.isInitialized) {
            val downloadContentDirectory =
                File(getDownloadDirectory(context), DOWNLOAD_CONTENT_DIRECTORY)
            downloadCache = SimpleCache(
                downloadContentDirectory,
                NoOpCacheEvictor(),
                getDatabaseProvider(context)
            )
        }
        return downloadCache
    }

    @Synchronized
    fun getDownloadSimpleCache(context: Context): Cache {
        if (!MyDownloadHelper::downloadCache.isInitialized) {
            val downloadContentDirectory =
                File(getDownloadDirectory(context), DOWNLOAD_CONTENT_DIRECTORY)
            downloadCache = SimpleCache(
                downloadContentDirectory,
                NoOpCacheEvictor(),
                getDatabaseProvider(context)
            )
        }
        return downloadCache
    }

    @Synchronized
    private fun ensureDownloadManagerInitialized(context: Context) {
        audioQualityFormat =
            context.preferences.getEnum(audioQualityFormatKey, AudioQualityFormat.Auto)

        if (!MyDownloadHelper::downloadManager.isInitialized) {
            downloadManager = DownloadManager(
                context,
                getDatabaseProvider(context),
                getDownloadCache(context),
                //getResolvingDataSourceFactory(context),
                createDataSourceFactory(),
                executor
            ).apply {
                maxParallelDownloads = 3
                minRetryCount = 1
                requirements = Requirements(Requirements.NETWORK)

                addListener(
                    object : DownloadManager.Listener {
                        override fun onIdle(downloadManager: DownloadManager) =
                            mutableDownloadState.update { false }

                        override fun onDownloadChanged(
                            downloadManager: DownloadManager,
                            download: Download,
                            finalException: Exception?
                        ) = run {
                            downloadQueue.trySend(downloadManager).let { }
                            syncDownloads(download)
                        }

                        override fun onDownloadRemoved(
                            downloadManager: DownloadManager,
                            download: Download
                        ) = run {
                            downloadQueue.trySend(downloadManager).let { }
                            syncDownloads(download)
                        }
                    }
                )
            }

            //downloadTracker =
            //    DownloadTracker(context, getHttpDataSourceFactory(context), downloadManager)
        }
    }

    @Synchronized
    private fun syncDownloads(download: Download) {
        downloads.update { map ->
            map.toMutableMap().apply {
                set(download.request.id, download)
            }
        }
        getDownloads()
    }

    @Synchronized
    private fun getDatabaseProvider(context: Context): DatabaseProvider {
        if (!MyDownloadHelper::databaseProvider.isInitialized) databaseProvider =
            StandaloneDatabaseProvider(context)
        return databaseProvider
    }

    @Synchronized
    fun getDownloadDirectory(context: Context): File {
        if (!MyDownloadHelper::downloadDirectory.isInitialized) {
            downloadDirectory = context.getExternalFilesDir(null) ?: context.filesDir
            downloadDirectory.resolve(DOWNLOAD_CONTENT_DIRECTORY).also { directory ->
                if (directory.exists()) return@also
                directory.mkdir()
            }
            //Log.d("downloadMedia", downloadDirectory.path)
        }
        return downloadDirectory
    }


        fun scheduleDownload(context: Context, mediaItem: MediaItem) {
            if (mediaItem.isLocal) return

            val downloadRequest = DownloadRequest
                .Builder(
                    /* id      = */ mediaItem.mediaId,
                    /* uri     = */ mediaItem.requestMetadata.mediaUri
                        ?: Uri.parse("https://music.youtube.com/watch?v=${mediaItem.mediaId}")
                )
                .setCustomCacheKey(mediaItem.mediaId)
                //.setData(mediaItem.mediaId.encodeToByteArray())
                .setData("${mediaItem.mediaMetadata.artist.toString()} - ${mediaItem.mediaMetadata.title.toString()}".encodeToByteArray()) // Title in notification
                .build()

            transaction {
                runCatching {
                    Database.insert(mediaItem)
                }.also { if (it.isFailure) return@transaction }

                coroutineScope.launch {
                    context.download<MyDownloadService>(downloadRequest).exceptionOrNull()?.let {
                        if (it is CancellationException) throw it

                        Timber.e(it.stackTraceToString())
                        println("MyDownloadHelper scheduleDownload exception ${it.stackTraceToString()}")
                    }
                }
            }
        }


}
