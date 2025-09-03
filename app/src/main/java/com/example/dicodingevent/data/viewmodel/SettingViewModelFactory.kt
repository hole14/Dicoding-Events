package com.example.dicodingevent.data.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dicodingevent.data.retrofit.ApiService
import com.example.dicodingevent.data.room.EventDao

class SettingViewModelFactory(private val application: Application, private val eventDao: EventDao, private val apiService: ApiService): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingViewModel::class.java)){
            return SettingViewModel(application, eventDao, apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}