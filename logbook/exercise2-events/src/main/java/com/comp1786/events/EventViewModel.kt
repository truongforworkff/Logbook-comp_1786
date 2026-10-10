package com.comp1786.events

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** RAM only: survives rotation, but deliberately does not survive process termination. */
class EventViewModel : ViewModel() {
    private val current = MutableStateFlow<List<Event>>(emptyList())
    val events = current.asStateFlow()
    private var nextId = 1L
    fun add(event: Event) {
        current.value = (current.value + event.copy(id = nextId++)).sortedBy { it.date }
    }
}
