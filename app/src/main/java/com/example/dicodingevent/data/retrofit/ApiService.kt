package com.example.dicodingevent.data.retrofit

import com.example.dicodingevent.data.respone.EventsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("events")
    suspend fun getUpcomingEvents(@Query("active") active: Int): EventsResponse

    @GET("events")
    suspend fun getFinishedEvents(@Query("active") active: Int): EventsResponse
}