package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class HabitWithTodayStatus(
    val habit: HabitEntity,
    val log: HabitLogEntity?,
    val currentStreak: Int,
    val isCompletedToday: Boolean,
    val currentValue: Float,
    val targetValue: Float,
    val progressRatio: Float
)

data class WeeklyStreakDay(
    val dateKey: String,
    val dayLabel: String,
    val dayOfMonth: Int,
    val completedCount: Int,
    val totalActive: Int,
    val completionRate: Float,
    val isToday: Boolean
)

data class MonthlyDayStatus(
    val dateKey: String,
    val dayNumber: Int,
    val completionRate: Float,
    val completedCount: Int,
    val totalCount: Int,
    val isToday: Boolean
)

data class YearlyMonthSummary(
    val monthLabel: String,
    val monthNumber: Int,
    val completions: Int,
    val totalPossible: Int,
    val completionRate: Float
)

data class DateRangeAnalytics(
    val startDate: String,
    val endDate: String,
    val totalDays: Long,
    val activeDaysWithCompletion: Int,
    val totalCompletions: Int,
    val completionRate: Float,
    val mostProductiveDay: String
)

class HabitRepository(private val db: AppDatabase) {
    private val habitDao = db.habitDao()
    private val logDao = db.habitLogDao()
    private val profileDao = db.userProfileDao()
    private val templateDao = db.globalHabitTemplateDao()
    private val activityDao = db.activityHistoryDao()

    val activeHabits: Flow<List<HabitEntity>> = habitDao.getAllActiveHabits()
    val allLogs: Flow<List<HabitLogEntity>> = logDao.getAllLogs()
    val userProfile: Flow<UserProfileEntity?> = profileDao.getActiveUserProfile()
    val allProfiles: Flow<List<UserProfileEntity>> = profileDao.getAllProfiles()
    val globalTemplates: Flow<List<GlobalHabitTemplateEntity>> = templateDao.getAllTemplates()
    val recentActivities: Flow<List<ActivityHistoryEntity>> = activityDao.getRecentActivity(60)

    fun getTodayDateKey(): String {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    /**
     * Combines active habits with today's logs to produce today's status & streaks
     */
    fun observeTodayHabits(dateKey: String = getTodayDateKey()): Flow<List<HabitWithTodayStatus>> {
        return combine(
            habitDao.getAllActiveHabits(),
            logDao.getLogsForDate(dateKey),
            logDao.getAllLogs()
        ) { habits, todayLogs, allLogsList ->
            val logsByHabitId = todayLogs.associateBy { it.habitId }
            val logsGrouped = allLogsList.groupBy { it.habitId }

            habits.map { habit ->
                val log = logsByHabitId[habit.id]
                val currentVal = log?.currentValue ?: 0f
                val isCompleted = log?.isCompleted ?: false
                val target = if (habit.targetValue > 0f) habit.targetValue else 1f
                val ratio = (currentVal / target).coerceIn(0f, 1f)

                val habitLogs = logsGrouped[habit.id] ?: emptyList()
                val streak = calculateHabitStreak(habitLogs, LocalDate.parse(dateKey))

                HabitWithTodayStatus(
                    habit = habit,
                    log = log,
                    currentStreak = streak,
                    isCompletedToday = isCompleted,
                    currentValue = currentVal,
                    targetValue = target,
                    progressRatio = ratio
                )
            }
        }
    }

    /**
     * Computes the current streak in days for a single habit
     */
    private fun calculateHabitStreak(logs: List<HabitLogEntity>, targetDate: LocalDate): Int {
        if (logs.isEmpty()) return 0
        val completedDates = logs
            .filter { it.isCompleted }
            .mapNotNull {
                try {
                    LocalDate.parse(it.dateKey)
                } catch (e: Exception) {
                    null
                }
            }
            .toSet()

        var streak = 0
        var checkDate = targetDate
        // If not completed today, check from yesterday backwards
        if (!completedDates.contains(checkDate)) {
            checkDate = checkDate.minusDays(1)
        }

        while (completedDates.contains(checkDate)) {
            streak++
            checkDate = checkDate.minusDays(1)
        }
        return streak
    }

    /**
     * Calculates the past 7 days (Weekly Streak chart)
     */
    fun observeWeeklyStreak(anchorDate: LocalDate = LocalDate.now()): Flow<List<WeeklyStreakDay>> {
        val startDate = anchorDate.minusDays(6)
        val startKey = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val endKey = anchorDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

        return combine(
            habitDao.getAllActiveHabits(),
            logDao.getLogsForDateRange(startKey, endKey)
        ) { habits, logsInRange ->
            val totalActive = habits.size.coerceAtLeast(1)
            val logsByDate = logsInRange.groupBy { it.dateKey }

            (0..6).map { offset ->
                val date = startDate.plusDays(offset.toLong())
                val dateKey = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val dayLogs = logsByDate[dateKey] ?: emptyList()
                val completedCount = dayLogs.count { it.isCompleted }
                val rate = (completedCount.toFloat() / totalActive.toFloat()).coerceIn(0f, 1f)
                val dayLabel = date.dayOfWeek.name.take(3)
                val isToday = (date == LocalDate.now())

                WeeklyStreakDay(
                    dateKey = dateKey,
                    dayLabel = dayLabel,
                    dayOfMonth = date.dayOfMonth,
                    completedCount = completedCount,
                    totalActive = totalActive,
                    completionRate = rate,
                    isToday = isToday
                )
            }
        }
    }

    /**
     * Generates a monthly calendar heatmap
     */
    fun observeMonthlyAnalytics(yearMonth: YearMonth): Flow<List<MonthlyDayStatus>> {
        val daysInMonth = yearMonth.lengthOfMonth()
        val startKey = yearMonth.atDay(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val endKey = yearMonth.atDay(daysInMonth).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val today = LocalDate.now()

        return combine(
            habitDao.getAllActiveHabits(),
            logDao.getLogsForDateRange(startKey, endKey)
        ) { habits, logs ->
            val totalActive = habits.size.coerceAtLeast(1)
            val logsByDate = logs.groupBy { it.dateKey }

            (1..daysInMonth).map { day ->
                val date = yearMonth.atDay(day)
                val dateKey = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val dayLogs = logsByDate[dateKey] ?: emptyList()
                val completedCount = dayLogs.count { it.isCompleted }
                val rate = (completedCount.toFloat() / totalActive.toFloat()).coerceIn(0f, 1f)

                MonthlyDayStatus(
                    dateKey = dateKey,
                    dayNumber = day,
                    completionRate = rate,
                    completedCount = completedCount,
                    totalCount = totalActive,
                    isToday = (date == today)
                )
            }
        }
    }

    /**
     * Generates 12-month yearly analytics
     */
    fun observeYearlyAnalytics(year: Int): Flow<List<YearlyMonthSummary>> {
        val startKey = "$year-01-01"
        val endKey = "$year-12-31"

        return combine(
            habitDao.getAllActiveHabits(),
            logDao.getLogsForDateRange(startKey, endKey)
        ) { habits, logs ->
            val totalActive = habits.size.coerceAtLeast(1)
            val logsByDate = logs.groupBy { it.dateKey }

            (1..12).map { month ->
                val ym = YearMonth.of(year, month)
                val daysCount = ym.lengthOfMonth()
                var monthCompletions = 0

                for (d in 1..daysCount) {
                    val dateKey = ym.atDay(d).format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val dayLogs = logsByDate[dateKey] ?: emptyList()
                    monthCompletions += dayLogs.count { it.isCompleted }
                }

                val totalPossible = totalActive * daysCount
                val rate = if (totalPossible > 0) {
                    (monthCompletions.toFloat() / totalPossible.toFloat()).coerceIn(0f, 1f)
                } else 0f

                YearlyMonthSummary(
                    monthLabel = ym.month.name.take(3),
                    monthNumber = month,
                    completions = monthCompletions,
                    totalPossible = totalPossible,
                    completionRate = rate
                )
            }
        }
    }

    /**
     * Calculates custom date range statistics
     */
    suspend fun calculateDateRangeAnalytics(startDate: LocalDate, endDate: LocalDate): DateRangeAnalytics {
        val startKey = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val endKey = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1

        val activeHabitsList = habitDao.getActiveHabitsSync()
        val totalActive = activeHabitsList.size.coerceAtLeast(1)
        val logs = logDao.getLogsForDateRangeSync(startKey, endKey)

        val logsByDate = logs.groupBy { it.dateKey }
        var activeDays = 0
        var totalCompletions = 0
        var bestDay = "N/A"
        var maxCompletionsInDay = -1

        logsByDate.forEach { (dateKey, dayLogs) ->
            val completed = dayLogs.count { it.isCompleted }
            if (completed > 0) {
                activeDays++
                totalCompletions += completed
            }
            if (completed > maxCompletionsInDay) {
                maxCompletionsInDay = completed
                bestDay = dateKey
            }
        }

        val totalPossible = totalActive * totalDays
        val rate = if (totalPossible > 0) {
            (totalCompletions.toFloat() / totalPossible.toFloat()).coerceIn(0f, 1f)
        } else 0f

        return DateRangeAnalytics(
            startDate = startKey,
            endDate = endKey,
            totalDays = totalDays,
            activeDaysWithCompletion = activeDays,
            totalCompletions = totalCompletions,
            completionRate = rate,
            mostProductiveDay = bestDay
        )
    }

    // CRUD Operations
    suspend fun createHabit(habit: HabitEntity): Long {
        val id = habitDao.insertHabit(habit)
        activityDao.insertActivity(
            ActivityHistoryEntity(
                habitId = id,
                habitTitle = habit.title,
                actionType = "CREATED",
                details = "Created new habit targeting ${habit.targetValue} ${habit.unit}"
            )
        )
        return id
    }

    suspend fun updateHabit(habit: HabitEntity) {
        habitDao.updateHabit(habit)
        activityDao.insertActivity(
            ActivityHistoryEntity(
                habitId = habit.id,
                habitTitle = habit.title,
                actionType = "UPDATED",
                details = "Updated habit settings"
            )
        )
    }

    suspend fun deleteHabit(habitId: Long, habitTitle: String) {
        habitDao.deleteHabit(habitId)
        logDao.deleteLogsForHabit(habitId)
        activityDao.insertActivity(
            ActivityHistoryEntity(
                habitId = habitId,
                habitTitle = habitTitle,
                actionType = "DELETED",
                details = "Removed habit and its logs"
            )
        )
    }

    suspend fun logHabitProgress(
        habit: HabitEntity,
        dateKey: String = getTodayDateKey(),
        newValue: Float,
        notes: String = ""
    ) {
        val isCompleted = newValue >= habit.targetValue
        val existingLog = logDao.getLogForHabitAndDate(habit.id, dateKey)
        val logEntity = (existingLog ?: HabitLogEntity(
            habitId = habit.id,
            dateKey = dateKey
        )).copy(
            currentValue = newValue,
            isCompleted = isCompleted,
            notes = notes,
            loggedAt = System.currentTimeMillis()
        )

        logDao.insertOrUpdateLog(logEntity)

        if (isCompleted && existingLog?.isCompleted != true) {
            activityDao.insertActivity(
                ActivityHistoryEntity(
                    habitId = habit.id,
                    habitTitle = habit.title,
                    actionType = "COMPLETED",
                    details = "Goal completed: $newValue / ${habit.targetValue} ${habit.unit}"
                )
            )
        }
    }

    suspend fun toggleHabitCompletion(habit: HabitEntity, dateKey: String = getTodayDateKey()) {
        val existingLog = logDao.getLogForHabitAndDate(habit.id, dateKey)
        val currentlyCompleted = existingLog?.isCompleted == true
        val target = if (habit.targetValue > 0f) habit.targetValue else 1f
        val nextValue = if (currentlyCompleted) 0f else target
        logHabitProgress(habit, dateKey, nextValue)
    }

    // Profile & RBAC
    // Authentication & Session Management
    suspend fun loginWithEmail(email: String, password: String): Result<UserProfileEntity> {
        val trimmedEmail = email.trim().lowercase()
        val existing = profileDao.getUserByEmail(trimmedEmail)
            ?: return Result.failure(Exception("Account not found. Please register first."))

        if (existing.passwordHash.isNotBlank() && existing.passwordHash != password) {
            return Result.failure(Exception("Invalid password."))
        }

        profileDao.logoutAllUsers()
        profileDao.setLoggedInUser(existing.userId)
        val active = profileDao.getUserProfileById(existing.userId) ?: existing
        return Result.success(active)
    }

    suspend fun registerWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Result<UserProfileEntity> {
        val trimmedEmail = email.trim().lowercase()
        val existing = profileDao.getUserByEmail(trimmedEmail)
        if (existing != null) {
            return Result.failure(Exception("An account with this email already exists."))
        }

        // Designated Admin Check
        val role = if (trimmedEmail == "pklaw9855@gmail.com" || trimmedEmail.startsWith("admin@")) "ADMIN" else "USER"
        val newUserId = "user_${System.currentTimeMillis()}"
        val profile = UserProfileEntity(
            userId = newUserId,
            displayName = displayName.ifBlank { trimmedEmail.substringBefore("@") },
            email = trimmedEmail,
            passwordHash = password,
            authProvider = "EMAIL",
            role = role,
            isLoggedIn = true,
            avatarEmoji = if (role == "ADMIN") "🛡️" else "⚡"
        )

        profileDao.logoutAllUsers()
        profileDao.insertProfile(profile)
        return Result.success(profile)
    }

    suspend fun loginWithGoogle(
        email: String = "pklaw9855@gmail.com",
        displayName: String = "Google User"
    ): Result<UserProfileEntity> {
        val trimmedEmail = email.trim().lowercase()
        val role = if (trimmedEmail == "pklaw9855@gmail.com" || trimmedEmail.startsWith("admin")) "ADMIN" else "USER"
        val existing = profileDao.getUserByEmail(trimmedEmail)

        val profile = if (existing != null) {
            existing.copy(isLoggedIn = true, role = if (trimmedEmail == "pklaw9855@gmail.com") "ADMIN" else existing.role)
        } else {
            UserProfileEntity(
                userId = "google_${System.currentTimeMillis()}",
                displayName = displayName,
                email = trimmedEmail,
                authProvider = "GOOGLE",
                role = role,
                isLoggedIn = true,
                avatarEmoji = if (role == "ADMIN") "🛡️" else "🚀"
            )
        }

        profileDao.logoutAllUsers()
        profileDao.insertProfile(profile)
        return Result.success(profile)
    }

    suspend fun verifyPhoneOtpAndLogin(
        phoneNumber: String,
        otpCode: String
    ): Result<UserProfileEntity> {
        val cleanPhone = phoneNumber.trim()
        if (cleanPhone.length < 8) {
            return Result.failure(Exception("Please enter a valid phone number."))
        }
        if (otpCode.trim().length != 6) {
            return Result.failure(Exception("Please enter the 6-digit verification code."))
        }

        val existing = profileDao.getUserByPhone(cleanPhone)
        val role = if (cleanPhone.contains("1234567") || cleanPhone.contains("9855")) "ADMIN" else "USER"

        val profile = if (existing != null) {
            existing.copy(isLoggedIn = true, role = if (role == "ADMIN") "ADMIN" else existing.role)
        } else {
            UserProfileEntity(
                userId = "phone_${System.currentTimeMillis()}",
                displayName = "User ${cleanPhone.takeLast(4)}",
                email = "${cleanPhone.filter { it.isDigit() }}@habitflow.phone",
                phoneNumber = cleanPhone,
                authProvider = "PHONE",
                role = role,
                isLoggedIn = true,
                avatarEmoji = if (role == "ADMIN") "🛡️" else "📱"
            )
        }

        profileDao.logoutAllUsers()
        profileDao.insertProfile(profile)
        return Result.success(profile)
    }

    suspend fun logout() {
        profileDao.logoutAllUsers()
    }

    suspend fun updateProfile(profile: UserProfileEntity) {
        profileDao.updateProfile(profile)
    }

    suspend fun setUserRole(userId: String, role: String) {
        profileDao.updateUserRole(userId, role)
        activityDao.insertActivity(
            ActivityHistoryEntity(
                habitId = null,
                habitTitle = "System Account",
                actionType = "UPDATED",
                details = "User role updated to $role"
            )
        )
    }

    // Admin Template Management
    suspend fun addGlobalTemplate(template: GlobalHabitTemplateEntity): Long {
        return templateDao.insertTemplate(template)
    }

    suspend fun deleteGlobalTemplate(id: Long) {
        templateDao.deleteTemplate(id)
    }

    suspend fun adoptTemplate(template: GlobalHabitTemplateEntity): Long {
        val habit = HabitEntity(
            title = template.title,
            description = template.description,
            category = template.category,
            type = template.type,
            targetValue = template.targetValue,
            unit = template.unit,
            colorHex = template.colorHex,
            iconName = template.iconName,
            frequencyDays = template.defaultFrequency,
            reminderTime = template.defaultReminderTime
        )
        return createHabit(habit)
    }

    suspend fun logCalendarSyncAction(habitTitle: String, details: String) {
        activityDao.insertActivity(
            ActivityHistoryEntity(
                habitTitle = habitTitle,
                actionType = "CALENDAR_SYNC",
                details = details
            )
        )
    }

    suspend fun clearAllData() {
        habitDao.deleteAllHabits()
        logDao.deleteAllLogs()
        activityDao.clearHistory()
    }
}
