package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.HabitEntity
import com.example.data.HabitRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: HabitRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = HabitRepository(db)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun read_appName_fromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("HabitFlow", appName)
    }

    @Test
    fun habitRepository_createAndToggleHabit() = runBlocking {
        val habit = HabitEntity(
            title = "Daily Meditation",
            category = "Mindfulness",
            type = "CHECKBOX",
            targetValue = 1f,
            unit = "times"
        )
        val habitId = repository.createHabit(habit)
        assertTrue(habitId > 0)

        val insertedHabit = db.habitDao().getHabitById(habitId)
        assertNotNull(insertedHabit)
        assertEquals("Daily Meditation", insertedHabit?.title)

        // Toggle completion
        repository.toggleHabitCompletion(insertedHabit!!)
        val todayHabits = repository.observeTodayHabits().first()
        val meditationItem = todayHabits.find { it.habit.id == habitId }
        assertNotNull(meditationItem)
        assertTrue(meditationItem!!.isCompletedToday)
    }

    @Test
    fun habitRepository_authenticationAndRoles() = runBlocking {
        // Register standard user
        val regResult = repository.registerWithEmail("user@example.com", "pass123", "User One")
        assertTrue(regResult.isSuccess)
        val user = regResult.getOrNull()
        assertNotNull(user)
        assertEquals("USER", user?.role)

        // Register designated admin
        val adminResult = repository.registerWithEmail("pklaw9855@gmail.com", "admin123", "Admin")
        assertTrue(adminResult.isSuccess)
        val admin = adminResult.getOrNull()
        assertNotNull(admin)
        assertEquals("ADMIN", admin?.role)

        // Active profile should now be the logged-in admin
        val activeProfile = repository.userProfile.first()
        assertNotNull(activeProfile)
        assertEquals("ADMIN", activeProfile?.role)

        // Switch back by logging in as standard user
        val loginResult = repository.loginWithEmail("user@example.com", "pass123")
        assertTrue(loginResult.isSuccess)
        val switchedProfile = repository.userProfile.first()
        assertEquals("USER", switchedProfile?.role)
    }
}
