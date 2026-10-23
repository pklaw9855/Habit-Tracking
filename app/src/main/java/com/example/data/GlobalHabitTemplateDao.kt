package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GlobalHabitTemplateDao {
    @Query("SELECT * FROM global_habit_templates ORDER BY id ASC")
    fun getAllTemplates(): Flow<List<GlobalHabitTemplateEntity>>

    @Query("SELECT * FROM global_habit_templates ORDER BY id ASC")
    suspend fun getAllTemplatesSync(): List<GlobalHabitTemplateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: GlobalHabitTemplateEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTemplates(templates: List<GlobalHabitTemplateEntity>)

    @Query("DELETE FROM global_habit_templates WHERE id = :id")
    suspend fun deleteTemplate(id: Long)
}
