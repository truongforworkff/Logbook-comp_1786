package com.comp1786.events

import org.junit.Assert.*
import org.junit.Test

class EventViewModelTest {
    @Test fun eventsAreSortedAndHaveDistinctIds() {
        val model = EventViewModel()
        val event = Event(name = "Workshop", date = "2026-10-11", location = "Campus", organiser = "Student", type = "Workshop")
        model.add(event)
        model.add(event.copy(date = "2026-10-10"))
        val result = model.events.value
        assertEquals(listOf("2026-10-10", "2026-10-11"), result.map { it.date })
        assertEquals(2, result.map { it.id }.distinct().size)
    }

    @Test fun newViewModelStartsEmptyWithoutPersistence() {
        val original = EventViewModel()
        original.add(Event(name = "Event", date = "2026-10-10", location = "Campus", organiser = "Student", type = "Other"))
        assertEquals(1, original.events.value.size)
        assertTrue(EventViewModel().events.value.isEmpty())
    }
}
