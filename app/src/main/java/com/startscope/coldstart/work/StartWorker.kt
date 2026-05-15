package com.startscope.coldstart.work

import android.app.ActivityManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.startscope.coldstart.StartScopeApplication
import com.startscope.coldstart.util.UsageAccessHelper

class StartWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        if (!UsageAccessHelper.isGranted(applicationContext)) {
            return Result.success()
        }
        val am = applicationContext.getSystemService(ActivityManager::class.java) ?: return Result.success()
        val list = am.getHistoricalProcessStartReasons(200) ?: return Result.success()
        val app = applicationContext.applicationContext as StartScopeApplication
        app.container.repository.insertFromAppStartInfoList(list)
        return Result.success()
    }
}
