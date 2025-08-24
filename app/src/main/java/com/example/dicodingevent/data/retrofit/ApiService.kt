package com.example.dicodingevent.data.retrofit

import com.example.dicodingevent.data.respone.EventResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("events")
    suspend fun getUpcomingEvents(@Query("active") active: Int): EventResponse

    @GET("events")
    suspend fun getFinishedEvents(@Query("active") active: Int): EventResponse

    @GET("events?active=-1&limit=1")
    suspend fun getLatestEvent(): EventResponse
}