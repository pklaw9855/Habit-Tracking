package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_logs",
    indices = [
        Index(value = ["habitId", "dateKey"], unique = true),
        Index(value = ["dateKey"])
    ]
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val dateKey: String, // format "yyyy-MM-dd"
    val currentValue: Float = 0f,
    val isCompleted: Boolean = false,
    val notes: String = "",
    val loggedAt: Long = System.currentTimeMillis()
)
