package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HabitEntity

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditHabitDialog(
    initialHabit: HabitEntity? = null,
    onDismiss: () -> Unit,
    onSave: (
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
    ) -> Unit
) {
    var title by remember { mutableStateOf(initialHabit?.title ?: "") }
    var description by remember { mutableStateOf(initialHabit?.description ?: "") }
    var category by remember { mutableStateOf(initialHabit?.category ?: "Health") }
    var type by remember { mutableStateOf(initialHabit?.type ?: "CHECKBOX") }
    var targetValueStr by remember { mutableStateOf(initialHabit?.targetValue?.toInt()?.toString() ?: "1") }
    var unit by remember { mutableStateOf(initialHabit?.unit ?: "times") }
    var colorHex by remember { mutableStateOf(initialHabit?.colorHex ?: "#4F46E5") }
    var reminderTime by remember { mutableStateOf(initialHabit?.reminderTime ?: "08:00") }
    var syncToCalendar by remember { mutableStateOf(initialHabit?.calendarSyncEnabled ?: true) }

    val daysOfWeek = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
    var selectedDays by remember {
        mutableStateOf(
            initialHabit?.frequencyDays?.split(",")?.map { it.trim() }?.toSet()
                ?: daysOfWeek.toSet()
        )
    }

    var titleError by remember { mutableStateOf<String?>(null) }
    var targetError by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Health", "Fitness", "Mindfulness", "Study", "Productivity", "Finance")
    val colorOptions = listOf(
        "#4F46E5" to Color(0xFF4F46E5),
        "#FF5E7E" to Color(0xFFFF5E7E),
        "#10B981" to Color(0xFF10B981),
        "#00B4D8" to Color(0xFF00B4D8),
        "#F59E0B" to Color(0xFFF59E0B),
        "#9D4EDD" to Color(0xFF9D4EDD)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialHabit == null) "Create New Habit" else "Edit Habit",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Title Field
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = null
                    },
                    label = { Text("Habit Title *") },
                    placeholder = { Text("e.g., Morning Run, Read Books") },
                    isError = titleError != null,
                    supportingText = titleError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("habit_title_input"),
                    singleLine = true
                )

                // Description Field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("Why this habit matters...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                // Category Selector
                Text("Category", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                // Habit Type
                Text("Habit Type", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("CHECKBOX" to "Yes/No", "NUMERIC" to "Numeric", "TIMER" to "Timer").forEach { (t, label) ->
                        FilterChip(
                            selected = type == t,
                            onClick = {
                                type = t
                                if (t == "CHECKBOX") {
                                    targetValueStr = "1"
                                    unit = "times"
                                } else if (t == "TIMER") {
                                    targetValueStr = "20"
                                    unit = "mins"
                                } else if (t == "NUMERIC" && unit == "times") {
                                    targetValueStr = "8"
                                    unit = "glasses"
                                }
                            },
                            label = { Text(label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Target & Unit (for Numeric or Timer)
                if (type != "CHECKBOX") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = targetValueStr,
                            onValueChange = {
                                targetValueStr = it
                                targetError = null
                            },
                            label = { Text("Target") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = targetError != null,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Unit") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Color Tag Picker
                Text("Card Color", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    colorOptions.forEach { (hex, col) ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(col)
                                .clickable { colorHex = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (colorHex == hex) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Frequency Days
                Text("Repeat On", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    daysOfWeek.forEach { day ->
                        val isSelected = selectedDays.contains(day)
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .size(38.dp)
                                .clickable {
                                    selectedDays = if (isSelected) {
                                        if (selectedDays.size > 1) selectedDays - day else selectedDays
                                    } else {
                                        selectedDays + day
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = day.take(1),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Reminder Time
                OutlinedTextField(
                    value = reminderTime,
                    onValueChange = { reminderTime = it },
                    label = { Text("Daily Reminder Time (HH:mm)") },
                    placeholder = { Text("08:00") },
                    leadingIcon = {
                        Icon(Icons.Default.Alarm, contentDescription = "Alarm")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Google Calendar Sync Checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable { syncToCalendar = !syncToCalendar }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = syncToCalendar,
                        onCheckedChange = { syncToCalendar = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = "Google Calendar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Connect to Google Calendar", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Create calendar reminder event with notification alerts", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = "Title is required"
                        return@Button
                    }
                    val target = targetValueStr.toFloatOrNull() ?: 1.0f
                    if (target <= 0f) {
                        targetError = "Target must be greater than 0"
                        return@Button
                    }

                    onSave(
                        title,
                        description,
                        category,
                        type,
                        target,
                        unit,
                        colorHex,
                        selectedDays.joinToString(","),
                        reminderTime,
                        syncToCalendar
                    )
                },
                modifier = Modifier.testTag("save_habit_button")
            ) {
                Text("Save Habit")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
