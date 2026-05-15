package com.startscope.coldstart.data

import android.app.ApplicationStartInfo
import android.content.Context
import com.startscope.coldstart.R

object StartReasonStrings {
    fun label(context: Context, reason: Int): String =
        when (reason) {
            ApplicationStartInfo.START_REASON_ALARM -> context.getString(R.string.reason_alarm)
            ApplicationStartInfo.START_REASON_BACKUP -> context.getString(R.string.reason_backup)
            ApplicationStartInfo.START_REASON_BOOT_COMPLETE ->
                context.getString(R.string.reason_boot_complete)
            ApplicationStartInfo.START_REASON_BROADCAST -> context.getString(R.string.reason_broadcast)
            ApplicationStartInfo.START_REASON_CONTENT_PROVIDER ->
                context.getString(R.string.reason_content_provider)
            ApplicationStartInfo.START_REASON_JOB -> context.getString(R.string.reason_job)
            ApplicationStartInfo.START_REASON_LAUNCHER -> context.getString(R.string.reason_launcher)
            ApplicationStartInfo.START_REASON_LAUNCHER_RECENTS ->
                context.getString(R.string.reason_launcher_recents)
            ApplicationStartInfo.START_REASON_OTHER -> context.getString(R.string.reason_other)
            ApplicationStartInfo.START_REASON_PUSH -> context.getString(R.string.reason_push)
            ApplicationStartInfo.START_REASON_SERVICE -> context.getString(R.string.reason_service)
            ApplicationStartInfo.START_REASON_START_ACTIVITY ->
                context.getString(R.string.reason_start_activity)
            else -> context.getString(R.string.reason_unknown, reason)
        }
}
