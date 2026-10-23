package com.example.widget

import android.content.Context

object HabitWidgetUpdater {
    fun triggerAll(context: Context) {
        HabitGoalCardWidgetProvider.triggerUpdate(context)
        HabitStreakBarWidgetProvider.triggerUpdate(context)
    }
}
