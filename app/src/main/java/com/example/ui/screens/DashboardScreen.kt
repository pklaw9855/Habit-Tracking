package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HabitEntity
import com.example.ui.components.AddEditHabitDialog
import com.example.ui.components.HabitItemCard
import com.example.ui.components.WeeklyStreakChart
import com.example.ui.components.rememberNotificationPermissionLauncher
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoPrimary
import com.example.ui.viewmodel.HabitViewModel
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(
    viewModel: HabitViewModel,
    onNavigateToProfile: () -> Unit
) {
    val habits by viewModel.todayHabits.collectAsState()
    val weeklyData by viewModel.weeklyStreak.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var habitToEdit by remember { mutableStateOf<HabitEntity?>(null) }

    val categories = listOf("All", "Health", "Fitness", "Mindfulness", "Study", "Productivity", "Finance")

    val filteredHabits = if (selectedCategory == "All") {
        habits
    } else {
        habits.filter { it.habit.category.equals(selectedCategory, ignoreCase = true) }
    }

    val totalHabits = habits.size
    val completedHabits = habits.count { it.isCompletedToday }
    val overallRatio = if (totalHabits > 0) completedHabits.toFloat() / totalHabits.toFloat() else 0f
    val currentMaxStreak = habits.maxOfOrNull { it.currentStreak } ?: 0

    val launchNotification = rememberNotificationPermissionLauncher(
        onPermissionGranted = { viewModel.triggerTestNotification("Daily Motivation") },
        onPermissionDenied = { /* Handled gracefully */ }
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_habit_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Greeting, Avatar, Role badge
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Hello, ${userProfile?.displayName ?: "Builder"}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Role Badge (USER vs ADMIN)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (userProfile?.role == "ADMIN") MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = userProfile?.role ?: "USER",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (userProfile?.role == "ADMIN") EmeraldSuccess else IndigoPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy")),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Avatar & Test Notification quick button
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { launchNotification() },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Test Notification",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userProfile?.avatarEmoji ?: "⚡",
                                fontSize = 22.sp
                            )
                        }
                    }
                }
            }

            // Daily Progress Overview Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Today's Target Progress",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "$completedHabits of $totalHabits Completed (${(overallRatio * 100).toInt()}%)",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            if (overallRatio >= 1f && totalHabits > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldSuccess),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Stars,
                                        contentDescription = "Perfect Score",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { overallRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surface
                        )
                    }
                }
            }

            // Weekly Interactive Streak Chart
            item {
                WeeklyStreakChart(
                    weeklyData = weeklyData,
                    currentStreakCount = currentMaxStreak,
                    onDayClick = { day ->
                        viewModel.clearUserMessage()
                    }
                )
            }

            // Category Filter Chips
            item {
                Column {
                    Text(
                        text = "Habit Categories",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { cat ->
                            ElevatedFilterChip(
                                selected = selectedCategory == cat,
                                onClick = { viewModel.setSelectedCategory(cat) },
                                label = { Text(cat) },
                                modifier = Modifier.testTag("filter_chip_$cat")
                            )
                        }
                    }
                }
            }

            // Habits Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Habits (${filteredHabits.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Habits List or Empty State
            if (filteredHabits.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No habits found in this category",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap the + button below or adopt templates from the Admin panel",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredHabits, key = { it.habit.id }) { item ->
                    HabitItemCard(
                        item = item,
                        onToggle = { viewModel.toggleHabit(item.habit) },
                        onProgressDelta = { delta -> viewModel.updateHabitProgress(item.habit, delta) },
                        onEdit = { habitToEdit = item.habit },
                        onDelete = { viewModel.deleteHabit(item.habit) },
                        onCalendarSync = { viewModel.syncHabitToGoogleCalendar(item.habit) },
                        onTestNotification = { viewModel.triggerTestNotification(item.habit.title) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Add / Edit Dialog Modal
    if (showAddDialog || habitToEdit != null) {
        AddEditHabitDialog(
            initialHabit = habitToEdit,
            onDismiss = {
                showAddDialog = false
                habitToEdit = null
            },
            onSave = { title, desc, cat, type, target, unit, color, freq, reminder, sync ->
                viewModel.saveHabit(
                    id = habitToEdit?.id ?: 0L,
                    title = title,
                    description = desc,
                    category = cat,
                    type = type,
                    targetValue = target,
                    unit = unit,
                    colorHex = color,
                    frequencyDays = freq,
                    reminderTime = reminder,
                    syncToCalendar = sync
                )
                showAddDialog = false
                habitToEdit = null
            }
        )
    }
}
