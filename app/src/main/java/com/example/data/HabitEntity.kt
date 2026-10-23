package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "default_user",
    val title: String,
    val description: String = "",
    val category: String = "Health", // "Health", "Fitness", "Mindfulness", "Study", "Productivity", "Finance"
    val type: String = "CHECKBOX",   // "CHECKBOX", "NUMERIC", "TIMER"
    val targetValue: Float = 1.0f,
    val unit: String = "times",      // "times", "glasses", "mins", "pages", "steps", "km"
    val colorHex: String = "#4F46E5",
    val iconName: String = "check_circle",
    val frequencyDays: String = "MON,TUE,WED,THU,FRI,SAT,SUN",
    val reminderTime: String = "",   // e.g. "08:00"
    val calendarSyncEnabled: Boolean = false,
    val archived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
