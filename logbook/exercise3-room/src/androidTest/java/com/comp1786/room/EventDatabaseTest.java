package com.comp1786.room;
import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.room.Room;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
public class EventDatabaseTest {
    private EventDatabase open(Context context, String filename) {
        return Room.databaseBuilder(context, EventDatabase.class, filename).build();
    }
    @Test public void crudPersistsAcrossDatabaseReopen() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String filename = "test-events.db";
        context.deleteDatabase(filename);
        EventDatabase database = open(context, filename);
        try {
            Event event = new Event(0, "Workshop", "2026-10-10", "Campus", "Student", "Workshop", "Upcoming");
            event.id = database.events().insert(event);
            database.close(); database = open(context, filename);
            assertEquals(event, database.events().getAll().get(0));
            Event updated = new Event(event.id, "Updated workshop", event.date, event.location, event.organiser, event.type, "Completed");
            database.events().update(updated);
            database.close(); database = open(context, filename);
            assertEquals(updated, database.events().getAll().get(0));
            database.events().delete(updated);
            database.close(); database = open(context, filename);
            assertTrue(database.events().getAll().isEmpty());
        } finally {
            database.close(); context.deleteDatabase(filename);
        }
    }
}
