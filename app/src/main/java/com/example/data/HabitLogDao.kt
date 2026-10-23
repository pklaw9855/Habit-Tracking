package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitLogDao {
    @Query("SELECT * FROM habit_logs WHERE dateKey = :dateKey")
    fun getLogsForDate(dateKey: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE dateKey = :dateKey")
    suspend fun getLogsForDateSync(dateKey: String): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND dateKey = :dateKey LIMIT 1")
    suspend fun getLogForHabitAndDate(habitId: Long, dateKey: String): HabitLogEntity?

    @Query("SELECT * FROM habit_logs WHERE dateKey BETWEEN :startDate AND :endDate ORDER BY dateKey ASC")
    fun getLogsForDateRange(startDate: String, endDate: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE dateKey BETWEEN :startDate AND :endDate ORDER BY dateKey ASC")
    suspend fun getLogsForDateRangeSync(startDate: String, endDate: String): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY dateKey DESC")
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY dateKey DESC")
    suspend fun getLogsForHabitSync(habitId: Long): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs ORDER BY dateKey DESC")
    fun getAllLogs(): Flow<List<HabitLogEntity>>

    @Query("SELECT COUNT(*) FROM habit_logs WHERE isCompleted = 1")
    fun getTotalCompletedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLog(log: HabitLogEntity): Long

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId")
    suspend fun deleteLogsForHabit(habitId: Long)

    @Query("DELETE FROM habit_logs")
    suspend fun deleteAllLogs()
}
