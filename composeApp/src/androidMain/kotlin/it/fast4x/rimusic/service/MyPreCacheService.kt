package it.fast4x.rimusic.service

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.NotificationUtil
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.PlatformScheduler
import it.fast4x.rimusic.R
import it.fast4x.rimusic.appContext
import it.fast4x.rimusic.service.MyPreCacheHelper.DOWNLOAD_NOTIFICATION_CHANNEL_ID
import it.fast4x.rimusic.service.MyPreCacheHelper.downloads
import it.fast4x.rimusic.utils.ActionReceiver

private const val JOB_ID = 7777
private const val FOREGROUND_NOTIFICATION_ID = 7878

@UnstableApi
class MyPreCacheService : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    DOWNLOAD_NOTIFICATION_CHANNEL_ID,
    R.string.caching, 0
) {

    private val notificationActionReceiver = NotificationActionReceiver()

    override fun onCreate() {
        super.onCreate()
        notificationActionReceiver.register()
    }
    override fun onDestroy() {
        unregisterReceiver(notificationActionReceiver)
        super.onDestroy()
    }

    override fun getDownloadManager(): DownloadManager {

        // This will only happen once, because getDownloadManager is guaranteed to be called only once
        // in the life cycle of the process.
        val downloadManager: DownloadManager = MyPreCacheHelper.getDownloadManager(this)
        //not required for caching
//        val downloadNotificationHelper: DownloadNotificationHelper =
//            MyPreCacheHelper.getDownloadNotificationHelper(this)
//        downloadManager.addListener(
//            TerminalStateNotificationHelper(
//                this,
//                downloadNotificationHelper,
//                FOREGROUND_NOTIFICATION_ID + 1
//            )
//        )
        return downloadManager
    }

    override fun getScheduler(): PlatformScheduler? {
        return if(Util.SDK_INT >= 21) PlatformScheduler(this, JOB_ID) else null
    }

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int
    ) = NotificationCompat
        .Builder(
            /* context = */ this,
            /* notification = */ MyPreCacheHelper
                .getDownloadNotificationHelper(this)
                .buildProgressNotification(
                /* context            = */ this,
                /* smallIcon          = */ R.drawable.download,
                /* contentIntent      = */ null,
                /* message            = */
                    downloadManager.currentDownloads.map { it.request.data }.firstOrNull()
                        ?.let { Util.fromUtf8Bytes(it) } ?: "${downloads.size} in progress",
                /* downloads          = */ downloads,
                /* notMetRequirements = */ notMetRequirements
            )
        )
        .setContentTitle(appContext().resources.getString(R.string.caching))
        .setChannelId(DOWNLOAD_NOTIFICATION_CHANNEL_ID)
        // Add action in notification
        .addAction(
            NotificationCompat.Action.Builder(
                /* icon = */ R.drawable.close,
                /* title = */ getString(R.string.cancel),
                notificationActionReceiver.cancel.pendingIntent
//                /* intent = */ Intent(this,MyDownloadService::class.java).also {
//                    it.action = notificationActionReceiver.cancel.value
//                    it.putExtra("id", FOREGROUND_NOTIFICATION_ID + 1)
//                }
            ).build()
        ).build()

    /**
     * Creates and displays notifications for downloads when they complete or fail.
     *
     *
     * This helper will outlive the lifespan of a single instance of [MyPrecacheService].
     * It is static to avoid leaking the first [MyPrecacheService] instance.
     */
    private class TerminalStateNotificationHelper(
        private val context: Context,
        private val notificationHelper: DownloadNotificationHelper,
        firstNotificationId: Int
    ) : DownloadManager.Listener {
        private var nextNotificationId: Int = firstNotificationId

        override fun onDownloadChanged(
            downloadManager: DownloadManager,
            download: Download,
            finalException: Exception?
        ) {
            val notification: Notification = when (download.state) {
//                Download.STATE_DOWNLOADING -> {
//                    notificationHelper.buildProgressNotification(
//                        context,
//                        R.drawable.download_progress,
//                        null,
//                        Util.fromUtf8Bytes(download.request.data),
//                        downloadManager.currentDownloads,
//                        0
//                    )
//                }
//                Download.STATE_COMPLETED -> {
//                    notificationHelper.buildDownloadCompletedNotification(
//                        context,
//                        R.drawable.downloaded,
//                        null,
//                        Util.fromUtf8Bytes(download.request.data)
//                    )
//                }
                Download.STATE_FAILED -> {
                    notificationHelper.buildDownloadFailedNotification(
                        context,
                        R.drawable.alert_circle_not_filled,
                        null,
                        Util.fromUtf8Bytes(download.request.data)
                    )
                }
                else -> return
            }
            NotificationUtil.setNotification(context, nextNotificationId++, notification)

        }


    }

    inner class NotificationActionReceiver : ActionReceiver("it.fast4x.rimusic.precache_notification_action") {
        val cancel by action { context, intent ->
            runCatching {
//                sendSetStopReason(
//                     context,
//                     MyDownloadService::class.java,
//                     ACTION_SET_STOP_REASON,
//                     intent.getIntExtra("id", 0),
//                    true
//                    )
                sendPauseDownloads(
                    /* context         = */ context,
                    /* clazz           = */ MyPreCacheService::class.java,
                    /* foreground      = */ true
                )
            }.recoverCatching {
                sendPauseDownloads(
                    /* context         = */ context,
                    /* clazz           = */ MyPreCacheService::class.java,
                    /* foreground      = */ false
                )
            }
        }
    }

}