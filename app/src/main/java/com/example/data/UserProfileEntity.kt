package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey
    val userId: String = "default_user",
    val displayName: String = "Habit Builder",
    val email: String = "builder@habitflow.app",
    val phoneNumber: String = "",
    val passwordHash: String = "",
    val authProvider: String = "EMAIL", // "EMAIL", "GOOGLE", "PHONE"
    val role: String = "USER", // "USER" or "ADMIN"
    val isLoggedIn: Boolean = true,
    val avatarEmoji: String = "⚡",
    val dailyTargetCount: Int = 5,
    val notificationsEnabled: Boolean = true,
    val defaultReminderTime: String = "08:30",
    val calendarSyncEnabled: Boolean = true,
    val themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    val createdAt: Long = System.currentTimeMillis()
)
