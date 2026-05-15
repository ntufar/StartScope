package com.startscope.coldstart.data

import android.app.ApplicationStartInfo
import android.content.Context
import com.startscope.coldstart.data.local.StartDao
import com.startscope.coldstart.data.local.StartEntity
import com.startscope.coldstart.prefs.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class StartRepository(
    private val context: Context,
    private val dao: StartDao,
    private val userPreferences: UserPreferences,
) {
    fun observeStarts(): Flow<List<StartEntity>> = dao.observeStarts()

    fun observeDistinctPackages(): Flow<List<String>> = dao.observeDistinctPackages()

    fun observeStartsForPackageSince(packageName: String, sinceMs: Long): Flow<List<StartEntity>> =
        dao.observeStartsForPackageSince(packageName, sinceMs)

    suspend fun getStart(id: Long): StartEntity? = dao.getById(id)

    suspend fun insertFromAppStartInfoList(infos: List<ApplicationStartInfo>) =
        withContext(Dispatchers.Default) {
            if (infos.isEmpty()) return@withContext
            val wall = System.currentTimeMillis()
            val mono = System.nanoTime()
            val entities = infos.map { ApplicationStartInfoMapper.toEntity(context, it, wall, mono) }
            dao.insertAll(entities)
            prune()
        }

    private suspend fun prune() {
        val days = userPreferences.retentionDaysSnapshot().coerceIn(1, 30)
        val cutoff = System.currentTimeMillis() - days * 86_400_000L
        dao.deleteOlderThan(cutoff)
    }

    data class TypeAverages(
        val coldMs: Long?,
        val warmMs: Long?,
        val hotMs: Long?,
    )
}
