package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ActivityHistoryEntity
import com.example.data.AppDatabase
import com.example.data.DateRangeAnalytics
import com.example.data.GlobalHabitTemplateEntity
import com.example.data.HabitEntity
import com.example.data.HabitRepository
import com.example.data.HabitWithTodayStatus
import com.example.data.MonthlyDayStatus
import com.example.data.UserProfileEntity
import com.example.data.WeeklyStreakDay
import com.example.data.YearlyMonthSummary
import com.example.util.GoogleCalendarSyncHelper
import com.example.util.HabitNotificationManager
import com.example.widget.HabitWidgetUpdater
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class HabitViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HabitRepository(AppDatabase.getInstance(application))
    val context: Context get() = getApplication<Application>().applicationContext

    // Selected Date for Dashboard inspection
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // Selected Month for Monthly Analytics
    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    // Selected Year for Yearly Analytics
    private val _selectedYear = MutableStateFlow(LocalDate.now().year)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    // Custom Date Range for Analytics
    private val _customRangeStart = MutableStateFlow(LocalDate.now().minusDays(14))
    val customRangeStart: StateFlow<LocalDate> = _customRangeStart.asStateFlow()

    private val _customRangeEnd = MutableStateFlow(LocalDate.now())
    val customRangeEnd: StateFlow<LocalDate> = _customRangeEnd.asStateFlow()

    private val _customRangeAnalytics = MutableStateFlow<DateRangeAnalytics?>(null)
    val customRangeAnalytics: StateFlow<DateRangeAnalytics?> = _customRangeAnalytics.asStateFlow()

    // Category filter for Dashboard
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // UI Feedback Message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Streams from Repository
    val todayHabits: StateFlow<List<HabitWithTodayStatus>> = repository.observeTodayHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weeklyStreak: StateFlow<List<WeeklyStreakDay>> = repository.observeWeeklyStreak()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyAnalytics: StateFlow<List<MonthlyDayStatus>> = repository.observeMonthlyAnalytics(YearMonth.now())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val yearlyAnalytics: StateFlow<List<YearlyMonthSummary>> = repository.observeYearlyAnalytics(LocalDate.now().year)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allProfiles: StateFlow<List<UserProfileEntity>> = repository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val globalTemplates: StateFlow<List<GlobalHabitTemplateEntity>> = repository.globalTemplates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentActivities: StateFlow<List<ActivityHistoryEntity>> = repository.recentActivities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        HabitNotificationManager.createNotificationChannel(context)
        refreshCustomRangeAnalytics()
    }

    fun setSelectedCategory(cat: String) {
        _selectedCategory.value = cat
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun setSelectedMonth(ym: YearMonth) {
        _selectedMonth.value = ym
    }

    fun nextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    fun previousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun setSelectedYear(year: Int) {
        _selectedYear.value = year
    }

    fun setCustomRange(start: LocalDate, end: LocalDate) {
        _customRangeStart.value = start
        _customRangeEnd.value = end
        refreshCustomRangeAnalytics()
    }

    fun refreshCustomRangeAnalytics() {
        viewModelScope.launch {
            val analytics = repository.calculateDateRangeAnalytics(
                _customRangeStart.value,
                _customRangeEnd.value
            )
            _customRangeAnalytics.value = analytics
        }
    }

    // Authentication Actions
    fun loginWithEmail(email: String, password: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.loginWithEmail(email, password)
            result.onSuccess {
                _userMessage.value = "Welcome back, ${it.displayName}!"
            }.onFailure {
                onError(it.message ?: "Login failed")
            }
        }
    }

    fun registerWithEmail(email: String, password: String, displayName: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.registerWithEmail(email, password, displayName)
            result.onSuccess {
                _userMessage.value = "Account created! Role: ${it.role}"
            }.onFailure {
                onError(it.message ?: "Registration failed")
            }
        }
    }

    fun loginWithGoogle(email: String, displayName: String) {
        viewModelScope.launch {
            val result = repository.loginWithGoogle(email, displayName)
            result.onSuccess {
                _userMessage.value = "Signed in as ${it.displayName} (${it.role})"
            }
        }
    }

    fun verifyPhoneOtp(phoneNumber: String, otpCode: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.verifyPhoneOtpAndLogin(phoneNumber, otpCode)
            result.onSuccess {
                _userMessage.value = "Verified! Welcome ${it.displayName}"
            }.onFailure {
                onError(it.message ?: "Verification failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _userMessage.value = "Signed out"
        }
    }

    // Habit Actions
    fun toggleHabit(habit: HabitEntity) {
        viewModelScope.launch {
            val dateKey = _selectedDate.value.format(DateTimeFormatter.ISO_LOCAL_DATE)
            repository.toggleHabitCompletion(habit, dateKey)
            HabitWidgetUpdater.triggerAll(context)
        }
    }

    fun updateHabitProgress(habit: HabitEntity, delta: Float) {
        viewModelScope.launch {
            val dateKey = _selectedDate.value.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val currentItem = todayHabits.value.firstOrNull { it.habit.id == habit.id }
            val currentVal = currentItem?.currentValue ?: 0f
            val nextVal = (currentVal + delta).coerceAtLeast(0f)
            repository.logHabitProgress(habit, dateKey, nextVal)
            HabitWidgetUpdater.triggerAll(context)
        }
    }

    fun saveHabit(
        id: Long = 0,
        title: String,
        description: String,
        category: String,
        type: String,
        targetValue: Float,
        unit: String,
        colorHex: String,
        frequencyDays: String,
        reminderTime: String,
        syncToCalendar: Boolean
    ) {
        viewModelScope.launch {
            val habit = HabitEntity(
                id = id,
                title = title.trim(),
                description = description.trim(),
                category = category,
                type = type,
                targetValue = targetValue,
                unit = unit.trim(),
                colorHex = colorHex,
                frequencyDays = frequencyDays,
                reminderTime = reminderTime.trim(),
                calendarSyncEnabled = syncToCalendar
            )

            val habitId = if (id == 0L) {
                repository.createHabit(habit)
            } else {
                repository.updateHabit(habit)
                id
            }

            if (reminderTime.isNotBlank()) {
                HabitNotificationManager.scheduleHabitReminder(context, habitId, habit.title, reminderTime)
            } else {
                HabitNotificationManager.cancelReminder(context, habitId)
            }

            if (syncToCalendar) {
                GoogleCalendarSyncHelper.addToGoogleCalendar(context, habit.copy(id = habitId))
                repository.logCalendarSyncAction(habit.title, "Synced to Google Calendar")
            }

            _userMessage.value = "Habit saved successfully!"
            HabitWidgetUpdater.triggerAll(context)
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.deleteHabit(habit.id, habit.title)
            HabitNotificationManager.cancelReminder(context, habit.id)
            _userMessage.value = "Habit '${habit.title}' deleted."
            HabitWidgetUpdater.triggerAll(context)
        }
    }

    fun syncHabitToGoogleCalendar(habit: HabitEntity) {
        viewModelScope.launch {
            GoogleCalendarSyncHelper.addToGoogleCalendar(context, habit)
            repository.logCalendarSyncAction(habit.title, "Exported event to Google Calendar")
            _userMessage.value = "Opening Google Calendar for '${habit.title}'"
        }
    }

    fun triggerTestNotification(habitTitle: String) {
        HabitNotificationManager.showImmediateTestNotification(context, habitTitle)
        _userMessage.value = "Notification sent to status bar!"
    }

    fun updateProfile(
        displayName: String,
        email: String,
        avatarEmoji: String,
        dailyTarget: Int
    ) {
        viewModelScope.launch {
            val current = userProfile.value ?: return@launch
            val updated = current.copy(
                displayName = displayName.trim(),
                email = email.trim(),
                avatarEmoji = avatarEmoji,
                dailyTargetCount = dailyTarget
            )
            repository.updateProfile(updated)
            _userMessage.value = "Profile updated."
        }
    }

    // Admin Template Actions
    fun createGlobalTemplate(
        title: String,
        description: String,
        category: String,
        type: String,
        targetValue: Float,
        unit: String,
        colorHex: String
    ) {
        viewModelScope.launch {
            val template = GlobalHabitTemplateEntity(
                title = title.trim(),
                description = description.trim(),
                category = category,
                type = type,
                targetValue = targetValue,
                unit = unit.trim(),
                colorHex = colorHex,
                iconName = "star"
            )
            repository.addGlobalTemplate(template)
            _userMessage.value = "Admin template created!"
        }
    }

    fun deleteGlobalTemplate(templateId: Long) {
        viewModelScope.launch {
            repository.deleteGlobalTemplate(templateId)
            _userMessage.value = "Template deleted."
        }
    }

    fun adoptTemplate(template: GlobalHabitTemplateEntity) {
        viewModelScope.launch {
            repository.adoptTemplate(template)
            _userMessage.value = "Added '${template.title}' to your habits!"
            HabitWidgetUpdater.triggerAll(context)
        }
    }
}
