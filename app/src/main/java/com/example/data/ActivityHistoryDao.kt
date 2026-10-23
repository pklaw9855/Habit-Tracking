package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityHistoryDao {
    @Query("SELECT * FROM activity_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentActivity(limit: Int = 50): Flow<List<ActivityHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityHistoryEntity): Long

    @Query("DELETE FROM activity_history")
    suspend fun clearHistory()
}
