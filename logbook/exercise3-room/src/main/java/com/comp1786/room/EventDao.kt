package com.comp1786.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY date ASC, id ASC")
    fun observeAll(): Flow<List<Event>>
    @Query("SELECT * FROM events ORDER BY date ASC, id ASC")
    suspend fun getAll(): List<Event>
    @Insert suspend fun insert(event: Event): Long
    @Update suspend fun update(event: Event)
    @Delete suspend fun delete(event: Event)
}
