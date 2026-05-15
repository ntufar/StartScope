package com.startscope.coldstart.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StartDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entities: List<StartEntity>)

    @Query("DELETE FROM starts WHERE timestampMs < :cutoffMs")
    suspend fun deleteOlderThan(cutoffMs: Long)

    @Query("SELECT * FROM starts ORDER BY timestampMs DESC LIMIT 5000")
    fun observeStarts(): Flow<List<StartEntity>>

    @Query("SELECT DISTINCT packageName FROM starts ORDER BY packageName ASC")
    fun observeDistinctPackages(): Flow<List<String>>

    @Query(
        """
        SELECT * FROM starts WHERE packageName = :packageName
        AND timestampMs >= :sinceMs
        ORDER BY timestampMs DESC
        """,
    )
    fun observeStartsForPackageSince(packageName: String, sinceMs: Long): Flow<List<StartEntity>>

    @Query("SELECT * FROM starts WHERE id = :id")
    suspend fun getById(id: Long): StartEntity?
}
