package com.comp1786.room;
import android.content.Context;
import androidx.room.Room;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
public class EventDatabaseTest {
    private Event event(String name,String date) { return new Event(0,name,date,"Campus","Manager","Workshop","Upcoming"); }
    @Test public void optionalFieldsPersistAndDuplicatePairIsRejected() {
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        try(EventDatabase db=Room.inMemoryDatabaseBuilder(context,EventDatabase.class).build()) {
            Event first=event("  Workshop  ","2026-11-08");
            first.description="Design together"; first.contactEmail="manager@example.com"; first.contactPhone="+84912345678";
            first.id=db.events().insert(first);
            Event loaded=db.events().getAll().get(0);
            assertEquals(first.description,loaded.description);
            assertEquals(first.contactEmail,loaded.contactEmail);
            assertEquals(first.contactPhone,loaded.contactPhone);
            try { db.events().insert(event("WORKSHOP","2026-11-08")); fail("Duplicate must be rejected"); }
            catch(android.database.sqlite.SQLiteConstraintException expected) { }
            db.events().insert(event("Workshop","2026-11-09"));
            loaded.description="Updated"; db.events().update(loaded);
            assertEquals(2,db.events().getAll().size());
            assertEquals("Updated",db.events().getAll().get(0).description);
            db.events().delete(loaded); assertEquals(1,db.events().getAll().size());
        }
    }
}
