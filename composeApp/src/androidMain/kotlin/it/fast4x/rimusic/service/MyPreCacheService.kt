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

private const val JOB_ID = 7777
private const val FOREGROUND_NOTIFICATION_ID = 7878

@UnstableApi
class MyPreCacheService : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    DOWNLOAD_NOTIFICATION_CHANNEL_ID,
    R.string.caching, 0
) {

    override fun getDownloadManager(): DownloadManager {

        // This will only happen once, because getDownloadManager is guaranteed to be called only once
        // in the life cycle of the process.
        val downloadManager: DownloadManager = MyPreCacheHelper.getDownloadManager(this)
        // Notification for pre cache not necessary
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
                /* message            = */ "${downloads.size} in progress",
                /* downloads          = */ downloads,
                /* notMetRequirements = */ notMetRequirements
            )
        )
        .setContentTitle(appContext().resources.getString(R.string.caching))
        .setChannelId(DOWNLOAD_NOTIFICATION_CHANNEL_ID)
        /*
        // Add action in notification
        .addAction(
            NotificationCompat.Action.Builder(
                /* icon = */ R.drawable.close,
                /* title = */ getString(R.string.cancel),
                /* intent = */ null //TODO notificationActionReceiver.cancel.pendingIntent
            ).build()
        )
        */
        .build()

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
                Download.STATE_COMPLETED -> {
                    notificationHelper.buildDownloadCompletedNotification(
                        context,
                        R.drawable.downloaded,
                        null,
                        Util.fromUtf8Bytes(download.request.data)
                    )
                }
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

}