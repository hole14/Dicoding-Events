package com.example.dicodingevent.data.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dicodingevent.data.di.Injection
import com.example.dicodingevent.data.repository.EventRepository

class EventViewModelFactory private constructor(private val eventRepository: EventRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventViewModel:: class.java)) {
            return EventViewModel(eventRepository) as T
        }
        throw IllegalArgumentException("Unknouwn ViewModel class" + modelClass.name)
    }
    companion object{
        @Volatile
        private var instance: EventViewModelFactory? = null
        fun getInstance(context: Context): EventViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: EventViewModelFactory(Injection.provideRepository(context))
            }.also { instance = it }
    }
}