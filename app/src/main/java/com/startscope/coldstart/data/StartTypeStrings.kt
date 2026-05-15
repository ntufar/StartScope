package com.startscope.coldstart.data

import android.app.ApplicationStartInfo
import android.content.Context
import com.startscope.coldstart.R

object StartTypeStrings {
    fun label(context: Context, startType: Int): String =
        when (startType) {
            ApplicationStartInfo.START_TYPE_COLD -> context.getString(R.string.start_type_cold)
            ApplicationStartInfo.START_TYPE_WARM -> context.getString(R.string.start_type_warm)
            ApplicationStartInfo.START_TYPE_HOT -> context.getString(R.string.start_type_hot)
            else -> context.getString(R.string.start_type_unknown)
        }
}
