package com.example.dicodingevent.data.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.room.Room
import com.example.dicodingevent.data.repository.SettingRepository
import com.example.dicodingevent.data.retrofit.ApiConfig
import com.example.dicodingevent.data.room.EventDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {

            Log.d(TAG, "Boot completed, restarting notification services")

            val preferencesManager = PreferencesManager(context)
            val reminderManager = EventReminderManager(context)

            if (preferencesManager.getEventNotification()) {
                Log.d(TAG, "Event notifications enabled, restarting services")

                reminderManager.startAllEventServices()

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val database = Room.databaseBuilder(
                            context,
                            EventDatabase::class.java,
                            "event_database"
                        ).build()

                        val eventDao = database.eventDao()
                        val apiService = ApiConfig.getApiService()

                        val settingRepository = SettingRepository(
                            context = context,
                            eventDao = eventDao,
                            apiService = apiService
                        )

                        // Reschedule all upcoming event reminders
                        settingRepository.scheduleAllUpcomingReminders()
                        Log.d(TAG, "All notification services restarted after boot")

                    } catch (e: Exception) {
                        Log.e(TAG, "Error restarting services after boot", e)
                        e.printStackTrace()
                    }
                }
            } else {
                Log.d(TAG, "Event notifications disabled, not starting services")
            }
        }
    }
}