package com.example.dicodingevent.data.worker

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import java.sql.Timestamp

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_EVENT_NOTIFICATION = "event_notification"
        private const val KEY_LAST_EVENT_CHECK = "last_event_check"
    }

    // Satu fungsi untuk semua notifikasi event
    fun getEventNotification(): Boolean {
        return sharedPreferences.getBoolean(KEY_EVENT_NOTIFICATION, false)
    }

    fun setEventNotification(enable: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_EVENT_NOTIFICATION, enable)
        }
    }

    fun getLastEventCheck(): Long {
        return sharedPreferences.getLong(KEY_LAST_EVENT_CHECK, 0L)
    }

    fun setLastEventCheck(timestamp: Long) {
        sharedPreferences.edit {
            putLong(KEY_LAST_EVENT_CHECK, timestamp)
        }
    }
    fun getNewEventNotification(): Boolean = getEventNotification()
    fun getEventReminderNotification(): Boolean = getEventNotification()
}