package com.example.dicodingevent.data.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.room.Room
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.dicodingevent.R
import com.example.dicodingevent.data.entity.EventEntity
import com.example.dicodingevent.data.room.EventDatabase
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventReminderWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    companion object {
        const val NOTIFICATION_ID = 1002
    }

    override fun doWork(): Result {
        return try {
            val eventId = inputData.getInt("event_id", 0)
            val eventName = inputData.getString("event_name") ?: "Event"
            val beginTime = inputData.getString("begin_time") ?: ""
            val eventDetail = getEventById(eventId)

            if (eventDetail != null) {
                showEventReminderNotification(eventDetail, beginTime)
            } else {
                showSimpleEventReminderNotification(eventName, beginTime)
            }

            Result.success()
        } catch (e : Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun getEventById(eventId: Int): EventEntity? {
        return try {
            val database = Room.databaseBuilder(
                applicationContext,
                EventDatabase::class.java,
                "event_database"
            ).build()

            val eventDao = database.eventDao()

            runBlocking {
                eventDao.getEventByIdSync(eventId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun showEventReminderNotification(event: EventEntity, beginTime: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val reminderManager = EventReminderManager(applicationContext)

        reminderManager.createReminderNotificationChannel(notificationManager)

        val timeFormatted = formatBeginTime(beginTime)

        val intent = Intent().apply {
            putExtra("event_id", event.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            event.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, EventReminderManager.REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_notifications_active_24)
            .setContentTitle("Event segera mulai")
            .setContentText("${event.name} akan dimulai pada $timeFormatted")
            .setStyle(NotificationCompat.BigTextStyle().bigText("${event.name} akan dimulai pada $timeFormatted\n\nLokasi: ${event.cityName}\nPenyelenggara: ${event.ownerName}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setGroup("EVENT_REMINDERS")
            .build()

        notificationManager.notify(NOTIFICATION_ID + System.currentTimeMillis().toInt(), notification)
    }

    private fun formatBeginTime(beginTime: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            val date = inputFormat.parse(beginTime)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            e.printStackTrace()
            beginTime
        }
    }

    private fun showSimpleEventReminderNotification(eventName: String, beginTime: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val reminderManager = EventReminderManager(applicationContext)

        reminderManager.createReminderNotificationChannel(notificationManager)

        val timeFormatted = formatBeginTime(beginTime)

        val notification = NotificationCompat.Builder(applicationContext, EventReminderManager.REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_notifications_active_24)
            .setContentTitle("Event Reminder")
            .setContentText("$eventName akan dimulai pada $timeFormatted")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setGroup("EVENT_REMINDERS")
            .build()

        notificationManager.notify(NOTIFICATION_ID + System.currentTimeMillis().toInt(), notification)
    }
}