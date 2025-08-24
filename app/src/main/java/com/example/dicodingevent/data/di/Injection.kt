package com.example.dicodingevent.data.di

import android.content.Context
import com.example.dicodingevent.data.repository.EventRepository
import com.example.dicodingevent.data.retrofit.ApiConfig
import com.example.dicodingevent.data.room.EventDatabase

object Injection {
    fun provideRepository(context: Context): EventRepository {
        val apiService = ApiConfig.getApiService()
        val database = EventDatabase.getInstance(context)
        val dao = database.eventDao()
        return EventRepository.getInstance(dao, apiService)
    }
}