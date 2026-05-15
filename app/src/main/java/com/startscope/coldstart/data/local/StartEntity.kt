package com.startscope.coldstart.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "starts",
    indices = [
        Index(value = ["timestampMs"]),
        Index(value = ["packageName", "timestampMs"]),
        Index(value = ["dedupeKey"], unique = true),
    ],
)
data class StartEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val processName: String,
    val pid: Int,
    val packageUid: Int,
    val startType: Int,
    val reason: Int,
    val launchMode: Int,
    val wasForceStopped: Boolean,
    val startupState: Int,
    val ttidMs: Long?,
    val ttfdMs: Long?,
    val timestampMs: Long,
    val intentAction: String?,
    val dedupeKey: String,
)
