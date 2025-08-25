package com.example.dicodingevent.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dicodingevent.data.Result
import com.example.dicodingevent.data.entity.EventEntity
import com.example.dicodingevent.data.repository.EventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EventViewModel(private val eventRepository: EventRepository) : ViewModel() {
    val getUpcomingEvents: LiveData<Result<List<EventEntity>>> = eventRepository.getUpcomingEvents()
    val getFinishedEvents: LiveData<Result<List<EventEntity>>> = eventRepository.getFinishedEvents()

    private val _query = MutableLiveData<List<EventEntity>>()
    val query: LiveData<List<EventEntity>> get() = _query

    fun searchEvents(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = eventRepository.searchEvents(query)
            _query.postValue(result)
        }
    }
}