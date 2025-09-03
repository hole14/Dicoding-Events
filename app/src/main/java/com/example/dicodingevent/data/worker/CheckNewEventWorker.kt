package com.example.dicodingevent.data.worker

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.dicodingevent.data.repository.SettingRepository
import com.example.dicodingevent.data.retrofit.ApiConfig
import com.example.dicodingevent.data.room.EventDatabase
import com.example.dicodingevent.screens.SettingScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CheckNewEventWorker(context: Context, workerParams: WorkerParameters): CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val dataBase = Room.databaseBuilder(
                    applicationContext,
                    EventDatabase::class.java,
                    "event_database"
                ).build()

                val eventDao = dataBase.eventDao()
                val apiService = ApiConfig.getApiService()

                val settingRepository = SettingRepository(
                    context = applicationContext,
                    eventDao = eventDao,
                    apiService = apiService
                )

                settingRepository.refreshUpcomingEvents()

                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.retry()
            }
        }
    }

}