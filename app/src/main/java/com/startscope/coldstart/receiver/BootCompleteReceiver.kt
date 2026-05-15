package com.startscope.coldstart.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.startscope.coldstart.service.StartCollectorService
import com.startscope.coldstart.util.UsageAccessHelper
import com.startscope.coldstart.work.WorkScheduling

class BootCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        val appContext = context.applicationContext
        WorkScheduling.schedulePeriodic(appContext)
        if (UsageAccessHelper.isGranted(appContext)) {
            StartCollectorService.start(appContext)
        }
    }
}
