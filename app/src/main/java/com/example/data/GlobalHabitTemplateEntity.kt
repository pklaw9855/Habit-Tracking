package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "global_habit_templates")
data class GlobalHabitTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String,
    val type: String,
    val targetValue: Float,
    val unit: String,
    val colorHex: String,
    val iconName: String,
    val defaultFrequency: String = "MON,TUE,WED,THU,FRI,SAT,SUN",
    val defaultReminderTime: String = "08:00",
    val createdByRole: String = "ADMIN",
    val isOfficial: Boolean = true
)
