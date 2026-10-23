package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_history")
data class ActivityHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long? = null,
    val habitTitle: String,
    val actionType: String, // "CREATED", "COMPLETED", "UPDATED", "DELETED", "STREAK_MILESTONE", "CALENDAR_SYNC"
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
