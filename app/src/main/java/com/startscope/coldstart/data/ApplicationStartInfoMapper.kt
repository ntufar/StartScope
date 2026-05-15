package com.startscope.coldstart.data

import android.app.ApplicationStartInfo
import android.content.Context
import android.content.pm.PackageManager
import com.startscope.coldstart.data.local.StartEntity

object ApplicationStartInfoMapper {

    fun toEntity(
        context: Context,
        info: ApplicationStartInfo,
        wallClockAnchorMs: Long,
        anchorMonoNs: Long,
    ): StartEntity {
        val pm = context.packageManager
        val packageUid = info.packageUid
        val packageName = resolvePackageName(pm, packageUid)
        val ts = info.startupTimestamps ?: emptyMap()
        val launchNs = ts[ApplicationStartInfo.START_TIMESTAMP_LAUNCH]?.takeIf { it > 0L }
            ?: ts[ApplicationStartInfo.START_TIMESTAMP_FORK]?.takeIf { it > 0L }
        val firstFrameNs = ts[ApplicationStartInfo.START_TIMESTAMP_FIRST_FRAME]?.takeIf { it > 0L }
        val fullyDrawnNs = ts[ApplicationStartInfo.START_TIMESTAMP_FULLY_DRAWN]?.takeIf { it > 0L }
        val ttidMs = if (launchNs != null && firstFrameNs != null) {
            (firstFrameNs - launchNs) / 1_000_000L
        } else {
            null
        }
        val ttfdMs = if (launchNs != null && fullyDrawnNs != null) {
            (fullyDrawnNs - launchNs) / 1_000_000L
        } else {
            null
        }
        val timestampMs = if (launchNs != null) {
            wallClockAnchorMs + (launchNs - anchorMonoNs) / 1_000_000L
        } else {
            wallClockAnchorMs
        }
        val intentAction = info.intent?.action?.take(512)
        val dedupeKey = buildDedupeKey(
            packageUid = packageUid,
            launchNs = launchNs,
            firstFrameNs = firstFrameNs,
            fullyDrawnNs = fullyDrawnNs,
            pid = info.pid,
            reason = info.reason,
            startType = info.startType,
        )
        return StartEntity(
            packageName = packageName,
            processName = info.processName.orEmpty(),
            pid = info.pid,
            packageUid = packageUid,
            startType = info.startType,
            reason = info.reason,
            launchMode = info.launchMode,
            wasForceStopped = info.wasForceStopped(),
            startupState = info.startupState,
            ttidMs = ttidMs,
            ttfdMs = ttfdMs,
            timestampMs = timestampMs,
            intentAction = intentAction,
            dedupeKey = dedupeKey,
        )
    }

    private fun resolvePackageName(pm: PackageManager, uid: Int): String {
        val pkgs = pm.getPackagesForUid(uid)
        if (!pkgs.isNullOrEmpty()) return pkgs[0]
        val named = pm.getNameForUid(uid) ?: return "uid:$uid"
        return named.substringBefore(':')
    }

    private fun buildDedupeKey(
        packageUid: Int,
        launchNs: Long?,
        firstFrameNs: Long?,
        fullyDrawnNs: Long?,
        pid: Int,
        reason: Int,
        startType: Int,
    ): String =
        "$packageUid|${launchNs ?: 0L}|${firstFrameNs ?: 0L}|${fullyDrawnNs ?: 0L}|$pid|$reason|$startType"
}
