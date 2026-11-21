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
    @Test public void upgradeFromVersion6KeepsExistingRowsWithUnknownCreationTime() { verifyUpgrade(6); }
    @Test public void upgradeFromVersion5KeepsSyncIdentityAndUnknownDuration() { verifyUpgrade(5); }
    @Test public void upgradeFromVersion4KeepsPhotosAndCreatesSyncState() { verifyUpgrade(4); }
    @Test public void upgradeFromVersion1() { verifyUpgrade(1); }
    @Test public void upgradeFromVersion2KeepsUnassignedSessions() { verifyUpgrade(2); }
    @Test public void upgradeFromVersion3KeepsLinkedSessionsAndDefaultsMedia() { verifyUpgrade(3); }

    private void verifyUpgrade(int version) {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String name = "session-migration-test.db";
        context.deleteDatabase(name);
        SQLiteDatabase old = context.openOrCreateDatabase(name, 0, null);
        old.execSQL("CREATE TABLE events (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, date TEXT NOT NULL, location TEXT NOT NULL, organiser TEXT NOT NULL, type TEXT NOT NULL, status TEXT NOT NULL, normalizedName TEXT NOT NULL, description TEXT NOT NULL, contactEmail TEXT NOT NULL, contactPhone TEXT NOT NULL)");
        old.execSQL("CREATE UNIQUE INDEX index_events_normalizedName_date ON events(normalizedName,date)");
        old.execSQL("INSERT INTO events VALUES(1,'Keep me','2026-11-08','Campus','Team','Workshop','Upcoming','keep me','','','')");
        if (version >= 2) {
            old.execSQL("CREATE TABLE sessions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, time TEXT NOT NULL, speaker TEXT NOT NULL, room TEXT NOT NULL, type TEXT NOT NULL, description TEXT NOT NULL)");
            old.execSQL("INSERT INTO sessions VALUES(42, 'Legacy', '08:00', 'Guest', 'Hall', 'Keynote', 'Keep all fields')");
        }
        if (version >= 3) {
            old.execSQL("ALTER TABLE sessions ADD COLUMN eventId INTEGER REFERENCES events(id) ON DELETE CASCADE");
            old.execSQL("CREATE INDEX index_sessions_eventId ON sessions(eventId)");
            old.execSQL("UPDATE sessions SET eventId=1");
        }
        if (version >= 4) {
            old.execSQL("ALTER TABLE events ADD COLUMN categoryCode TEXT NOT NULL DEFAULT 'Other'");
            old.execSQL("ALTER TABLE events ADD COLUMN imageUri TEXT NOT NULL DEFAULT ''");
            old.execSQL("UPDATE events SET categoryCode='AI', imageUri='file:///cover.jpg'");
        }
        if (version >= 5) {
            old.execSQL("ALTER TABLE events ADD COLUMN publicId TEXT NOT NULL DEFAULT ''");
            old.execSQL("UPDATE events SET publicId='keep-public-id'");
            old.execSQL("CREATE UNIQUE INDEX index_events_publicId ON events(publicId)");
            old.execSQL("CREATE TABLE sync_state (id INTEGER NOT NULL PRIMARY KEY, sourceId TEXT NOT NULL, revision INTEGER NOT NULL DEFAULT 0, uploadedRevision INTEGER NOT NULL DEFAULT -1, syncedAt INTEGER NOT NULL DEFAULT 0, error TEXT NOT NULL DEFAULT '')");
            old.execSQL("INSERT INTO sync_state VALUES(1, 'keep-source', 7, 6, 123, '')");
        }
        if (version == 6) {
            old.execSQL("ALTER TABLE sessions ADD COLUMN endTime TEXT NOT NULL DEFAULT ''");
            old.execSQL("UPDATE sessions SET endTime='08:30'");
        }
        old.setVersion(version);
        old.close();
        EventDatabase db = open(context, name);
        try {
            assertEquals("Keep me", db.events().getAll().get(0).name);
            assertEquals(0, db.events().getAll().get(0).createdAt);
            assertEquals(version >= 4 ? "AI" : "Other", db.events().getAll().get(0).categoryCode);
            assertEquals(version >= 4 ? "file:///cover.jpg" : "", db.events().getAll().get(0).imageUri);
            String publicId = db.events().getAll().get(0).publicId;
            assertFalse(publicId.isEmpty());
            assertNotNull(db.sync().get());
            if (version >= 5) {
                assertEquals("keep-public-id", publicId);
                assertEquals("keep-source", db.sync().get().sourceId);
                assertEquals(7, db.sync().get().revision);
                assertEquals(6, db.sync().get().uploadedRevision);
            }
            if (version >= 2) {
                Session legacy = db.sessions().getAll().get(0);
                assertEquals(42, legacy.id);
                assertEquals(version == 6 ? "08:30" : "", legacy.endTime);
                if (version == 2) assertNull(legacy.eventId);
                else assertEquals(Long.valueOf(1), legacy.eventId);
                assertEquals("Keep all fields", legacy.description);
                legacy.eventId = 1L;
                db.sessions().update(legacy);
                db.sessions().delete(legacy);
            }
            Session first = new Session(0, 1L,"Welcome","09:30","Speaker","Room 1","Keynote","Opening");
            first.endTime = "10:15";
            first.id = db.sessions().insert(first);
            long second = db.sessions().insert(new Session(0, 1L,"Networking","10:30","","","Networking",""));
            assertTrue(first.id > 0);
            assertNotEquals(first.id, second);
            db.close();
            db = open(context, name);
            assertEquals(publicId, db.events().getAll().get(0).publicId);
            assertTrue(db.sync().get().pending());
            assertEquals(2, db.sessions().getAll().size());
            Session loaded = db.sessions().getAll().get(0);
            assertEquals("10:15", loaded.endTime);
            assertEquals("Speaker", loaded.speaker);
            assertEquals("Room 1", loaded.room);
            assertEquals("Opening", loaded.description);
            loaded.name = "Updated";
            loaded.time = "11:00";
            loaded.endTime = "12:00";
            assertEquals(1, db.sessions().update(loaded));
            assertEquals("Updated", db.sessions().getAll().get(1).name);
            assertEquals("12:00", db.sessions().getAll().get(1).endTime);
            assertEquals(1, db.sessions().delete(loaded));
            assertEquals(1, db.sessions().getAll().size());
            assertEquals(1, db.events().getAll().size());
            com.comp1786.room.features.events.data.model.Event other =
                    new com.comp1786.room.features.events.data.model.Event(0,"Other","2026-12-01","Hall","Team","Workshop","Upcoming");
            other.id = db.events().insert(other);
            db.sessions().insert(new Session(0, other.id, "Other session", "12:00", "", "", "Workshop", ""));
            db.events().delete(db.events().getAll().get(0));
            assertEquals(1, db.sessions().getAll().size());
            assertEquals(Long.valueOf(other.id), db.sessions().getAll().get(0).eventId);
            try {
                db.sessions().insert(new Session(0, 99999L, "Orphan", "13:00", "", "", "Workshop", ""));
                fail("Foreign key must reject missing parent");
            } catch (android.database.sqlite.SQLiteConstraintException expected) { }
        } finally { db.close(); context.deleteDatabase(name); }
    }

    private EventDatabase open(Context context, String name) {
        return Room.databaseBuilder(context, EventDatabase.class, name)
                .addMigrations(EventDatabase.MIGRATION_1_2, EventDatabase.MIGRATION_2_3, EventDatabase.MIGRATION_3_4, EventDatabase.MIGRATION_4_5, EventDatabase.MIGRATION_5_6, EventDatabase.MIGRATION_6_7)
                .addCallback(com.comp1786.room.features.cloud.data.SyncSchema.CALLBACK).build();
    }
}
