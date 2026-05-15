package com.startscope.coldstart.service

import android.app.ActivityManager
import android.app.ApplicationStartInfo
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.startscope.coldstart.StartScopeApplication
import com.startscope.coldstart.MainActivity
import com.startscope.coldstart.R
import com.startscope.coldstart.util.UsageAccessHelper
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.function.Consumer

class StartCollectorService : Service() {

    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var activityManager: ActivityManager

    private val completionListener =
        Consumer<ApplicationStartInfo> { info ->
            val app = applicationContext as? StartScopeApplication ?: return@Consumer
            app.applicationScope.launch {
                app.container.repository.insertFromAppStartInfoList(listOf(info))
            }
        }

    override fun onCreate() {
        super.onCreate()
        activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        ensureChannel()
        val notification = buildNotification()
        startForeground(
            NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
        )
        activityManager.addApplicationStartInfoCompletionListener(
            executor,
            completionListener,
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        if (::activityManager.isInitialized) {
            try {
                activityManager.removeApplicationStartInfoCompletionListener(completionListener)
            } catch (_: Throwable) {
                // Best-effort unregister
            }
        }
        executor.shutdown()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.notification_channel_desc)
        }
        nm.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val pending = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setContentIntent(pending)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "startscope_collector"
        private const val NOTIFICATION_ID = 42

        fun start(context: Context) {
            if (!UsageAccessHelper.isGranted(context)) return
            val intent = Intent(context, StartCollectorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                @Suppress("DEPRECATION")
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, StartCollectorService::class.java))
        }
    }
}
