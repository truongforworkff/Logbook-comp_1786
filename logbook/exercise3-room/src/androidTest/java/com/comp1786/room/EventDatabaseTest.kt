package com.comp1786.room

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.room.Room
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventDatabaseTest {
    @Test fun crudPersistsAcrossDatabaseReopen() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val filename = "test-events.db"
        context.deleteDatabase(filename)
        fun open() = Room.databaseBuilder(context, EventDatabase::class.java, filename).build()
        var database = open()
        try {
            val event = Event(name = "Workshop", date = "2026-10-10", location = "Campus", organiser = "Student", type = "Workshop")
            val id = database.events().insert(event)
            database.close()
            database = open()
            assertEquals(event.copy(id = id), database.events().getAll().single())
            val updated = event.copy(id = id, name = "Updated workshop", status = "Completed")
            database.events().update(updated)
            database.close()
            database = open()
            assertEquals(updated, database.events().getAll().single())
            database.events().delete(updated)
            database.close()
            database = open()
            assertTrue(database.events().getAll().isEmpty())
        } finally {
            database.close()
            context.deleteDatabase(filename)
        }
    }
}
