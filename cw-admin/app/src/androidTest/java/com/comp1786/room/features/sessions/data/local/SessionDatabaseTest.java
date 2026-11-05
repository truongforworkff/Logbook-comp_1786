package com.comp1786.room.features.sessions.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import androidx.room.Room;
import androidx.test.platform.app.InstrumentationRegistry;
import com.comp1786.room.features.events.data.local.EventDatabase;
import com.comp1786.room.features.sessions.data.model.Session;
import org.junit.Test;
import static org.junit.Assert.*;

public class SessionDatabaseTest {
    @Test public void upgradeKeepsEventsAndSessionsSurviveReopen() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String name = "session-migration-test.db";
        context.deleteDatabase(name);
        SQLiteDatabase old = context.openOrCreateDatabase(name, 0, null);
        old.execSQL("CREATE TABLE events (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, date TEXT NOT NULL, location TEXT NOT NULL, organiser TEXT NOT NULL, type TEXT NOT NULL, status TEXT NOT NULL, normalizedName TEXT NOT NULL, description TEXT NOT NULL, contactEmail TEXT NOT NULL, contactPhone TEXT NOT NULL)");
        old.execSQL("CREATE UNIQUE INDEX index_events_normalizedName_date ON events(normalizedName,date)");
        old.execSQL("INSERT INTO events VALUES(1,'Keep me','2026-11-08','Campus','Team','Workshop','Upcoming','keep me','','','')");
        old.setVersion(1);
        old.close();
        EventDatabase db = open(context, name);
        try {
            assertEquals("Keep me", db.events().getAll().get(0).name);
            Session first = new Session(0,"Welcome","09:30","Speaker","Room 1","Keynote","Opening");
            first.id = db.sessions().insert(first);
            long second = db.sessions().insert(new Session(0,"Networking","10:30","","","Networking",""));
            assertTrue(first.id > 0);
            assertNotEquals(first.id, second);
            db.close();
            db = open(context, name);
            assertEquals(2, db.sessions().getAll().size());
            Session loaded = db.sessions().getAll().get(0);
            assertEquals("Speaker", loaded.speaker);
            assertEquals("Room 1", loaded.room);
            assertEquals("Opening", loaded.description);
            loaded.name = "Updated";
            loaded.time = "11:00";
            assertEquals(1, db.sessions().update(loaded));
            assertEquals("Updated", db.sessions().getAll().get(1).name);
            assertEquals(1, db.sessions().delete(loaded));
            assertEquals(1, db.sessions().getAll().size());
            assertEquals(1, db.events().getAll().size());
        } finally { db.close(); context.deleteDatabase(name); }
    }

    private EventDatabase open(Context context, String name) {
        return Room.databaseBuilder(context, EventDatabase.class, name)
                .addMigrations(EventDatabase.MIGRATION_1_2).build();
    }
}
