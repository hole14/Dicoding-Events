package com.example.dicodingevent.data.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dicodingevent.data.repository.SettingRepository
import com.example.dicodingevent.data.retrofit.ApiService
import com.example.dicodingevent.data.room.EventDao
import com.example.dicodingevent.data.worker.EventReminderManager
import com.example.dicodingevent.data.worker.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingViewModel(application: Application, private val eventDao: EventDao, private val apiService: ApiService): AndroidViewModel(application) {
    private val preferencesManager = PreferencesManager(application)
    private val reminderManager = EventReminderManager(application)

    private val settingRepository = SettingRepository(
        context = application,
        eventDao = eventDao,
        apiService = apiService,
    )

    private val _eventNotification = MutableStateFlow(false)
    val eventNotification: StateFlow<Boolean> = _eventNotification.asStateFlow()

    companion object {
        private const val TAG = "SettingViewModel"
    }

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _eventNotification.value = preferencesManager.getEventNotification()
        }
    }

    fun setEventNotification(enable: Boolean) {
        viewModelScope.launch {
            Log.d(TAG, "Setting event notification: $enable")
            _eventNotification.value = enable
            preferencesManager.setEventNotification(enable)

            if (enable) {
                startAllNotificationServices()
            } else {
                stopAllNotificationServices()
            }
        }
    }

    private suspend fun startAllNotificationServices() {
        try {
            Log.d(TAG, "Starting all notification services")

            reminderManager.startAllEventServices()

            settingRepository.refreshUpcomingEvents()

            settingRepository.scheduleAllUpcomingReminders()

            Log.d(TAG, "All notification services started successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error starting notification services", e)
            e.printStackTrace()
        }
    }

    private fun stopAllNotificationServices() {
        try {
            Log.d(TAG, "Stopping all notification services")

            reminderManager.stopAllEventServices()

            Log.d(TAG, "All notification services stopped")

        } catch (e: Exception) {
            Log.e(TAG, "Error stopping notification services", e)
            e.printStackTrace()
        }
    }

    fun manualRefreshEvents() {
        viewModelScope.launch {
            try {
                if (_eventNotification.value) {
                    settingRepository.refreshUpcomingEvents()
                    Log.d(TAG, "Manual refresh completed")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in manual refresh", e)
                e.printStackTrace()
            }
        }
    }
}