package com.comp1786.room
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val date: String,
    val location: String,
    val organiser: String,
    val type: String,
    val status: String = "Upcoming"
)
