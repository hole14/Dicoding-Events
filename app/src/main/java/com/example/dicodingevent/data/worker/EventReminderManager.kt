package com.example.dicodingevent.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.dicodingevent.R
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class EventReminderManager(private val context: Context) {
    companion object {
        const val EVENT_REMINDER_WORK_NAME = "event_reminder_name"
        const val CHECK_NEW_EVENT_WORK_NAME = "check_new_event_work"
        const val NEW_EVENT_CHANNEL_ID = "new_event_channel"
        const val NEW_EVENT_CHANNEL_NAME = "New Event Notification"
        const val REMINDER_CHANNEL_ID = "event_reminder_channel"
        const val REMINDER_CHANNEL_NAME = "Event Reminder"
    }

    private val preferencesManager = PreferencesManager(context)

    fun cancelAllEventServices() {
        WorkManager.getInstance(context).cancelAllWorkByTag("event_reminder")
        WorkManager.getInstance(context).cancelUniqueWork(CHECK_NEW_EVENT_WORK_NAME)
    }

    fun startAllEventServices() {
        if (preferencesManager.getEventNotification()) {
            startPeriodicNewEventCheck()
        }
    }

    fun stopAllEventServices() {
        cancelAllEventServices()
    }

    fun startPeriodicNewEventCheck() {
        val workRequest = PeriodicWorkRequestBuilder<CheckNewEventWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            CHECK_NEW_EVENT_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun scheduleEventReminder(eventId: Int, eventName: String, beginTime: String) {
        if (!preferencesManager.getEventNotification()) {
            return
        }

        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val eventDate = dateFormat.parse(beginTime)

            if (eventDate != null) {
                val currentTime = System.currentTimeMillis()
                val eventTime = eventDate.time
                val oneHourBefore = eventTime - (60 * 60 * 1000) // 1 hour before

                if (oneHourBefore > currentTime) {
                    val delay = oneHourBefore - currentTime
                    val data = Data.Builder()
                        .putInt("event_id", eventId)
                        .putString("event_name", eventName)
                        .putString("begin_time", beginTime)
                        .build()

                    val workRequest = OneTimeWorkRequestBuilder<EventReminderWorker>()
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setInputData(data)
                        .addTag("event_reminder")
                        .build()

                    WorkManager.getInstance(context).enqueueUniqueWork(
                        "${EVENT_REMINDER_WORK_NAME}_$eventId",
                        ExistingWorkPolicy.REPLACE,
                        workRequest
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showNewEventNotification(eventName: String, eventCount: Int = 1) {
        if (!preferencesManager.getEventNotification()) {
            return
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNewEventNotificationChannel(notificationManager)

        val contentText = if (eventCount == 1) {
            "Ada Event Baru: $eventName"
        } else {
            "Ada $eventCount Event Baru! Termasuk: $eventName"
        }

        val notification = NotificationCompat.Builder(context, NEW_EVENT_CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_notifications_active_24)
            .setContentTitle("Event Baru!")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setGroup("NEW_EVENTS")
            .build()

        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)
    }

    private fun createNewEventNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NEW_EVENT_CHANNEL_ID,
                NEW_EVENT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new events"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createReminderNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                REMINDER_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for event reminder notification"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}