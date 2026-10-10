package com.comp1786.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Event::class], version = 1, exportSchema = true)
abstract class EventDatabase : RoomDatabase() {
    abstract fun events(): EventDao
    companion object {
        @Volatile private var instance: EventDatabase? = null
        fun get(context: Context): EventDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, EventDatabase::class.java, "events.db")
                .build().also { instance = it }
        }
    }
}
