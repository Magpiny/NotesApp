package com.example.notesapp.core

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.notesapp.domain.model.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskReminderManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleReminder(task: Task) {
        val dueDate = task.dueDate ?: return
        
        // Schedule 10 minutes before
        scheduleAlarm(task, dueDate - 10 * 60 * 1000, 10)
        // Schedule 5 minutes before
        scheduleAlarm(task, dueDate - 5 * 60 * 1000, 5)
        // Schedule at exact time
        scheduleAlarm(task, dueDate, 0)
    }

    private fun scheduleAlarm(task: Task, triggerTime: Long, minutesBefore: Int) {
        if (triggerTime <= System.currentTimeMillis()) return

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.example.notesapp.TASK_REMINDER" // Unique action
            putExtra("taskId", task.id)
            putExtra("taskTitle", task.title)
            putExtra("minutesBefore", minutesBefore)
        }

        // Use a more robust unique request code for each task and its 3 reminder stages
        val requestCode = generateRequestCode(task.id, minutesBefore)
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                if (minutesBefore == 0) {
                    // Use setAlarmClock for the final reminder - highest priority and visible to user
                    val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, pendingIntent)
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    fun cancelReminder(taskId: String) {
        listOf(0, 5, 10).forEach { minutesBefore ->
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = "com.example.notesapp.TASK_REMINDER"
            }
            val requestCode = generateRequestCode(taskId, minutesBefore)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    internal fun generateRequestCode(taskId: String, minutesBefore: Int): Int {
        //taskId is a UUID string. Use a consistent hash and combine with minutesBefore
        return (taskId.hashCode() * 31) + minutesBefore
    }
}
