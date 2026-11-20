package com.comp1786.room.features.cloud.data;

import android.content.Context;
import androidx.room.Room;
import androidx.test.platform.app.InstrumentationRegistry;
import com.comp1786.room.features.events.data.local.EventDatabase;
import com.comp1786.room.features.events.data.model.Event;
import com.comp1786.room.features.sessions.data.model.Session;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class CloudIntervalTest {
    @Test public void payloadPreservesEndTimeAndLegacySessions() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        EventDatabase db = Room.inMemoryDatabaseBuilder(context, EventDatabase.class)
                .addCallback(SyncSchema.CALLBACK).build();
        try {
            Event event = new Event(0, "AI workshop", "2026-12-01", "Hall", "Team", "Workshop", "Upcoming");
            event.id = db.events().insert(event);
            Session session = new Session(0, event.id, "Opening", "09:00", "", "", "Keynote", "");
            session.endTime = "10:00";
            session.id = db.sessions().insert(session);
            assertEquals("10:00", firstSession(CloudSnapshot.read(db).payload(context)).get("endTime"));
            long revision = db.sync().get().revision;
            session.endTime = "";
            db.sessions().update(session);
            assertTrue(db.sync().get().revision > revision);
            assertEquals("", firstSession(CloudSnapshot.read(db).payload(context)).get("endTime"));
            session.endTime = "08:00";
            db.sessions().update(session);
            try {
                CloudSnapshot.read(db).payload(context);
                fail("Invalid interval must not reach Cloud");
            } catch (IllegalArgumentException expected) { }
        } finally { db.close(); }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> firstSession(Map<String, Object> payload) {
        Map<String, Object> event = ((List<Map<String, Object>>) payload.get("events")).get(0);
        return ((List<Map<String, Object>>) event.get("sessions")).get(0);
    }
}
