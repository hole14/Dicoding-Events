package com.example.dicodingevent.data.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dicodingevent.data.entity.EventEntity

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: List<EventEntity>)

    @Query("select * from event where name Like '%' || :query || '%' order by name asc")
    suspend fun getSearchEvent(query: String): List<EventEntity>

    @Query("select * from event where active = 1 order by datetime(beginTime) asc")
    fun getUpcomingEvents(): LiveData<List<EventEntity>>

    @Query("select * from event where active = 0 order by datetime(beginTime) desc")
    fun getFinishedEvents(): LiveData<List<EventEntity>>

    @Query("select * from event where isFavorite = 1")
    fun getFavoriteEvents(): LiveData<List<EventEntity>>

    @Query("update event set isFavorite = :isFavorite where id = :eventId")
    suspend fun updateFavoriteStatus(eventId: Int, isFavorite: Boolean)

    @Query("select * from event where id = :eventId limit 1")
    fun getEventById(eventId: Int): LiveData<EventEntity>
}