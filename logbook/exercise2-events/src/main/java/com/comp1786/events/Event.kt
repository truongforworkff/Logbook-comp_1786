package com.comp1786.events
data class Event(
    val id: Long = 0,
    val name: String,
    val date: String,
    val location: String,
    val organiser: String,
    val type: String
)
