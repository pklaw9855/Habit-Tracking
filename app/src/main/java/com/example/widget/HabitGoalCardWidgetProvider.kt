package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HabitGoalCardWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun triggerUpdate(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, HabitGoalCardWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (id in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getInstance(context)
                val habits = db.habitDao().getActiveHabitsSync()
                val todayKey = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                val logs = db.habitLogDao().getLogsForDateSync(todayKey)

                val total = habits.size
                val completed = logs.count { it.isCompleted }
                val progressPercent = if (total > 0) ((completed.toFloat() / total.toFloat()) * 100).toInt() else 0

                val views = RemoteViews(context.packageName, R.layout.widget_goal_card)
                views.setTextViewText(R.id.widget_app_title, "HabitFlow")
                views.setTextViewText(R.id.widget_progress_text, "$completed of $total Completed")
                views.setProgressBar(R.id.widget_progress_bar, 100, progressPercent, false)
                views.setTextViewText(
                    R.id.widget_streak_badge,
                    if (completed > 0) "🔥 $completed Done Today" else "⚡ Keep Going"
                )

                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    1001,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_goal_root, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}
