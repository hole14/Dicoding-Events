package com.example.dicodingevent.data.repository

import android.content.Context
import android.util.Log
import com.example.dicodingevent.data.entity.EventEntity
import com.example.dicodingevent.data.respone.ListEventsItem
import com.example.dicodingevent.data.retrofit.ApiService
import com.example.dicodingevent.data.room.EventDao
import com.example.dicodingevent.data.worker.EventReminderManager
import com.example.dicodingevent.data.worker.PreferencesManager

class SettingRepository(
    private val context: Context,
    private val eventDao: EventDao,
    private val apiService: ApiService
) {
    private val reminderManager = EventReminderManager(context)
    private val preferencesManager = PreferencesManager(context)

    companion object {
        private const val TAG = "SettingRepository"
    }

    suspend fun refreshUpcomingEvents() {
        try {
            if (!preferencesManager.getEventNotification()) {
                return
            }

            val existingEvents = eventDao.getUpcomingEventsSync()
            val existingEventIds = existingEvents.map { it.id }.toSet()

            val apiResponse = apiService.getUpcomingEvents(1)
            val newEventsFromApi = apiResponse.listEvents?.mapNotNull { it?.toEventEntity() } ?: emptyList()

            // Filter truly new events (not in database yet)
            val reallyNewEvents = newEventsFromApi.filter { event ->
                event.id !in existingEventIds && event.active == 1
            }

            Log.d(TAG, "Found ${reallyNewEvents.size} new events")

            // Show notification for new events (karena satu switch mengatur semua)
            if (reallyNewEvents.isNotEmpty()) {
                when (reallyNewEvents.size) {
                    1 -> reminderManager.showNewEventNotification(reallyNewEvents.first().name)
                    else -> reminderManager.showNewEventNotification(
                        reallyNewEvents.first().name,
                        reallyNewEvents.size
                    )
                }
            }

            // Insert/update all events to database
            eventDao.insertEvent(newEventsFromApi)

            // Schedule reminders untuk semua new events (karena satu switch mengatur semua)
            scheduleRemindersForNewEvents(reallyNewEvents)

            // Update last check timestamp
            preferencesManager.setLastEventCheck(System.currentTimeMillis())

        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing upcoming events", e)
            e.printStackTrace()
        }
    }

    private fun scheduleRemindersForNewEvents(newEvents: List<EventEntity>) {
        newEvents.filter { it.active == 1 }.forEach { event ->
            reminderManager.scheduleEventReminder(
                eventId = event.id,
                eventName = event.name,
                beginTime = event.beginTime
            )
            Log.d(TAG, "Scheduled reminder for event: ${event.name}")
        }
    }

    suspend fun scheduleAllUpcomingReminders() {
        try {
            val upcomingEvents = eventDao.getUpcomingEventsSync()
            upcomingEvents.forEach { event ->
                reminderManager.scheduleEventReminder(
                    eventId = event.id,
                    eventName = event.name,
                    beginTime = event.beginTime
                )
            }
            Log.d(TAG, "Scheduled reminders for ${upcomingEvents.size} upcoming events")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling reminders", e)
            e.printStackTrace()
        }
    }
}

fun ListEventsItem.toEventEntity(): EventEntity {
    return EventEntity(
        id = this.id ?: 0,
        name = this.name ?: "",
        summary = this.summary ?: "",
        ownerName = this.ownerName ?: "",
        cityName = this.cityName ?: "",
        beginTime = this.beginTime ?: "",
        endTime = this.endTime ?: "",
        imageLogo = this.imageLogo ?: "",
        mediaCover = this.mediaCover ?: "",
        link = this.link ?: "",
        description = this.description ?: "",
        category = this.category ?: "",
        quota = this.quota ?: 0,
        registrants = this.registrants ?: 0,
        active = 1
    )
}
