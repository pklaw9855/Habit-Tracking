package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        HabitEntity::class,
        HabitLogEntity::class,
        UserProfileEntity::class,
        GlobalHabitTemplateEntity::class,
        ActivityHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun globalHabitTemplateDao(): GlobalHabitTemplateDao
    abstract fun activityHistoryDao(): ActivityHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit_flow.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate default starter data
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getInstance(context)
                                populateStarterData(database)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateStarterData(db: AppDatabase) {
            // Starter User Profiles (both user and designated admin)
            db.userProfileDao().insertProfile(
                UserProfileEntity(
                    userId = "user_admin",
                    displayName = "System Administrator",
                    email = "pklaw9855@gmail.com",
                    phoneNumber = "+15551234567",
                    passwordHash = "admin123",
                    authProvider = "EMAIL",
                    role = "ADMIN",
                    isLoggedIn = true,
                    avatarEmoji = "🛡️",
                    dailyTargetCount = 5
                )
            )

            db.userProfileDao().insertProfile(
                UserProfileEntity(
                    userId = "user_standard",
                    displayName = "Alex Carter",
                    email = "alex.carter@habitflow.app",
                    phoneNumber = "+15559876543",
                    passwordHash = "user123",
                    authProvider = "EMAIL",
                    role = "USER",
                    isLoggedIn = false,
                    avatarEmoji = "⚡",
                    dailyTargetCount = 4
                )
            )

            // Starter habits
            val defaultHabits = listOf(
                HabitEntity(
                    title = "Drink 2L Water",
                    description = "Stay energized and hydrated throughout the day",
                    category = "Health",
                    type = "NUMERIC",
                    targetValue = 8f,
                    unit = "glasses",
                    colorHex = "#00B4D8",
                    iconName = "water_drop",
                    frequencyDays = "MON,TUE,WED,THU,FRI,SAT,SUN",
                    reminderTime = "09:00"
                ),
                HabitEntity(
                    title = "Morning Jog / Workout",
                    description = "Daily 20-minute movement routine",
                    category = "Fitness",
                    type = "TIMER",
                    targetValue = 20f,
                    unit = "mins",
                    colorHex = "#FF5E7E",
                    iconName = "fitness_center",
                    frequencyDays = "MON,TUE,WED,THU,FRI",
                    reminderTime = "07:15"
                ),
                HabitEntity(
                    title = "Read 15 Pages",
                    description = "Read non-fiction or educational book",
                    category = "Study",
                    type = "NUMERIC",
                    targetValue = 15f,
                    unit = "pages",
                    colorHex = "#4F46E5",
                    iconName = "menu_book",
                    frequencyDays = "MON,TUE,WED,THU,FRI,SAT,SUN",
                    reminderTime = "21:00"
                ),
                HabitEntity(
                    title = "Mindful Meditation",
                    description = "10 minutes of deep breathwork & stillness",
                    category = "Mindfulness",
                    type = "TIMER",
                    targetValue = 10f,
                    unit = "mins",
                    colorHex = "#10B981",
                    iconName = "spa",
                    frequencyDays = "MON,TUE,WED,THU,FRI,SAT,SUN",
                    reminderTime = "08:00"
                )
            )

            defaultHabits.forEach { habit ->
                val id = db.habitDao().insertHabit(habit)
                db.activityHistoryDao().insertActivity(
                    ActivityHistoryEntity(
                        habitId = id,
                        habitTitle = habit.title,
                        actionType = "CREATED",
                        details = "Initialized default starter habit"
                    )
                )
            }

            // Starter Global Admin Templates
            val templates = listOf(
                GlobalHabitTemplateEntity(
                    title = "Drink 8 Glasses of Water",
                    description = "Fundamental hydration routine",
                    category = "Health",
                    type = "NUMERIC",
                    targetValue = 8f,
                    unit = "glasses",
                    colorHex = "#00B4D8",
                    iconName = "water_drop"
                ),
                GlobalHabitTemplateEntity(
                    title = "10,000 Steps",
                    description = "Active daily walking target",
                    category = "Fitness",
                    type = "NUMERIC",
                    targetValue = 10000f,
                    unit = "steps",
                    colorHex = "#FF6B6B",
                    iconName = "directions_walk"
                ),
                GlobalHabitTemplateEntity(
                    title = "Deep Focus Work",
                    description = "45-minute uninterrupted flow state block",
                    category = "Productivity",
                    type = "TIMER",
                    targetValue = 45f,
                    unit = "mins",
                    colorHex = "#9D4EDD",
                    iconName = "laptop"
                ),
                GlobalHabitTemplateEntity(
                    title = "Evening Reflection & Journal",
                    description = "Review achievements and lessons of the day",
                    category = "Mindfulness",
                    type = "CHECKBOX",
                    targetValue = 1f,
                    unit = "times",
                    colorHex = "#6BCB77",
                    iconName = "edit_note"
                ),
                GlobalHabitTemplateEntity(
                    title = "No Screen 30m Before Bed",
                    description = "Improve sleep quality by unplugging",
                    category = "Health",
                    type = "CHECKBOX",
                    targetValue = 1f,
                    unit = "times",
                    colorHex = "#FFD166",
                    iconName = "bedtime"
                )
            )
            db.globalHabitTemplateDao().insertTemplates(templates)
        }
    }
}
