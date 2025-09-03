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

    @Query("SELECT * FROM event WHERE name LIKE '%' || :query || '%' order by name asc")
    suspend fun getSearchEvent(query: String): List<EventEntity>

    @Query("SELECT * FROM event WHERE active = 1 order by datetime(beginTime) asc")
    fun getUpcomingEvents(): LiveData<List<EventEntity>>

    @Query("SELECT * FROM event WHERE active = 0 order by datetime(beginTime) desc")
    fun getFinishedEvents(): LiveData<List<EventEntity>>

    @Query("SELECT * FROM event WHERE isFavorite = 1")
    fun getFavoriteEvents(): LiveData<List<EventEntity>>

    @Query("UPDATE event SET isFavorite = :isFavorite WHERE id = :eventId")
    suspend fun updateFavoriteStatus(eventId: Int, isFavorite: Boolean)

    @Query("SELECT * FROM event WHERE id = :eventId LIMIT 1")
    fun getEventById(eventId: Int): LiveData<EventEntity>

    @Query("SELECT * FROM event WHERE id = :eventId LIMIT 1")
    fun getEventByIdSync(eventId: Int) : EventEntity?

    @Query("SELECT * FROM event WHERE active = 1 order by datetime(beginTime) asc")
    suspend fun getUpcomingEventsSync(): List<EventEntity>

}