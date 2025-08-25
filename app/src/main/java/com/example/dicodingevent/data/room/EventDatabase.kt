package com.example.dicodingevent.data.room

import android.content.Context
import android.provider.CalendarContract
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.dicodingevent.data.entity.EventEntity

@Database(entities = [EventEntity::class], version = 2, exportSchema = false)
abstract class EventDatabase: RoomDatabase() {
    abstract fun eventDao():EventDao
    companion object{
        @Volatile
        private var instance: EventDatabase? = null
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE event ADD COLUMN active INTEGER NOT NULL DEFAULT 1")
            }

        }
        fun getInstance(context: Context): EventDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    EventDatabase::class.java,
                    "Event.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
            }
    }
}