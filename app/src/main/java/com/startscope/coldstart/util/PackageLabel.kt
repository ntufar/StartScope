package com.startscope.coldstart.util

import android.content.pm.PackageManager

fun packageLabel(pm: PackageManager, packageName: String): String {
    return try {
        val info = pm.getApplicationInfo(
            packageName,
            PackageManager.ApplicationInfoFlags.of(0L),
        )
        pm.getApplicationLabel(info).toString()
    } catch (_: Exception) {
        packageName
    }
}
