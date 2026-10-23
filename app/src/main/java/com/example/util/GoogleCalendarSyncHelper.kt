package com.example.util

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import com.example.data.HabitEntity
import java.util.Calendar

object GoogleCalendarSyncHelper {

    /**
     * Dispatches an Intent to Google Calendar to create a recurring goal reminder
     */
    fun addToGoogleCalendar(context: Context, habit: HabitEntity) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, "[HabitFlow] ${habit.title}")
            putExtra(
                CalendarContract.Events.DESCRIPTION,
                "${habit.description}\nTarget: ${habit.targetValue} ${habit.unit}\nCategory: ${habit.category}\nTracked via HabitFlow"
            )

            // Calculate start and end time
            val cal = Calendar.getInstance()
            if (habit.reminderTime.isNotBlank() && habit.reminderTime.contains(":")) {
                val parts = habit.reminderTime.split(":")
                val hour = parts[0].toIntOrNull() ?: 8
                val min = parts[1].toIntOrNull() ?: 0
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, min)
                cal.set(Calendar.SECOND, 0)
                if (cal.before(Calendar.getInstance())) {
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.timeInMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, cal.timeInMillis + 30 * 60 * 1000) // 30 min duration
            } else {
                cal.set(Calendar.HOUR_OF_DAY, 9)
                cal.set(Calendar.MINUTE, 0)
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.timeInMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, cal.timeInMillis + 30 * 60 * 1000)
            }

            // Recurrence rule: Daily or customized
            val rrule = buildRRuleFromFrequency(habit.frequencyDays)
            putExtra(CalendarContract.Events.RRULE, rrule)
            putExtra(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_PRIVATE)
            putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
        }

        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "No calendar app available: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun buildRRuleFromFrequency(freqDays: String): String {
        if (freqDays.isBlank() || freqDays == "MON,TUE,WED,THU,FRI,SAT,SUN") {
            return "FREQ=DAILY"
        }
        val daysMap = mapOf(
            "MON" to "MO",
            "TUE" to "TU",
            "WED" to "WE",
            "THU" to "TH",
            "FRI" to "FR",
            "SAT" to "SA",
            "SUN" to "SU"
        )
        val selected = freqDays.split(",")
            .mapNotNull { daysMap[it.trim()] }
            .joinToString(",")

        return if (selected.isNotEmpty()) {
            "FREQ=WEEKLY;BYDAY=$selected"
        } else {
            "FREQ=DAILY"
        }
    }
}
