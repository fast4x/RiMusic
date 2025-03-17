package it.fast4x.rimusic.service

import android.app.Notification
import android.content.Context
import android.content.Intent
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
import it.fast4x.rimusic.service.MyDownloadHelper.DOWNLOAD_NOTIFICATION_CHANNEL_ID
import it.fast4x.rimusic.utils.ActionReceiver

private const val JOB_ID = 8888
private const val FOREGROUND_NOTIFICATION_ID = 8989

@UnstableApi
class MyDownloadService : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    DOWNLOAD_NOTIFICATION_CHANNEL_ID,
    R.string.download, 0
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
        val downloadManager: DownloadManager = MyDownloadHelper.getDownloadManager(this)
        val downloadNotificationHelper: DownloadNotificationHelper =
            MyDownloadHelper.getDownloadNotificationHelper(this)
        downloadManager.addListener(
            TerminalStateNotificationHelper(
                this,
                downloadNotificationHelper,
                FOREGROUND_NOTIFICATION_ID + 1
            )
        )
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
            /* notification = */ MyDownloadHelper
                .getDownloadNotificationHelper(this)
                .buildProgressNotification(
                /* context            = */ this,
                /* smallIcon          = */ R.drawable.download_progress,
                /* contentIntent      = */ null,
                /* message            = */ downloadManager.currentDownloads.map { it.request.data }.firstOrNull()
                        ?.let { Util.fromUtf8Bytes(it) } ?: "${downloads.size} in progress",
                /* downloads          = */ downloads,
                /* notMetRequirements = */ notMetRequirements
            )
        )
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
        )

        .build()

    /**
     * Creates and displays notifications for downloads when they complete or fail.
     *
     *
     * This helper will outlive the lifespan of a single instance of [MyDownloadService].
     * It is static to avoid leaking the first [MyDownloadService] instance.
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

    inner class NotificationActionReceiver : ActionReceiver("it.fast4x.rimusic.download_notification_action") {
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
                    /* clazz           = */ MyDownloadService::class.java,
                    /* foreground      = */ true
                )
            }.recoverCatching {
                sendPauseDownloads(
                    /* context         = */ context,
                    /* clazz           = */ MyDownloadService::class.java,
                    /* foreground      = */ false
                )
            }
        }
    }

}