package com.comp1786.room.features.events.data.local;
import com.comp1786.room.features.events.data.model.Event;
import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.room.Room;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
public class EventDatabaseTest {
    private Event event(String name,String date) { return new Event(0,name,date,"Campus","Manager","Workshop","Upcoming"); }
    @Test public void optionalFieldsPersistAndDuplicatePairIsRejected() throws InterruptedException {
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        EventDatabase db=Room.inMemoryDatabaseBuilder(context,EventDatabase.class).build();
        try {
            Event first=event("  Workshop  ","2026-11-08");
            first.description="Design together"; first.contactEmail="manager@example.com"; first.contactPhone="+84912345678";
            first.categoryCode="Design"; first.imageUri="content://test/cover/1";
            first.id=db.events().insert(first);
            Event loaded=db.events().getAll().get(0);
            assertEquals(first.categoryCode,loaded.categoryCode);
            assertEquals(first.imageUri,loaded.imageUri);
            assertEquals(first.description,loaded.description);
            assertEquals(first.contactEmail,loaded.contactEmail);
            assertEquals(first.contactPhone,loaded.contactPhone);
            try { db.events().insert(event("WORKSHOP","2026-11-08")); fail("Duplicate must be rejected"); }
            catch(android.database.sqlite.SQLiteConstraintException expected) { }
            assertTrue(loaded.createdAt > 0);
            Event newer = event("Workshop", "2026-11-07");
            newer.createdAt = loaded.createdAt + 1;
            newer.id = db.events().insert(newer);
            long createdAt = loaded.createdAt;
            loaded.createdAt = 0;
            loaded.description="Updated"; loaded.imageUri=""; loaded.categoryCode="AI"; db.events().update(loaded);
            assertEquals(2,db.events().getAll().size());
            Event edited = db.events().getById(loaded.id);
            assertEquals("Updated", edited.description);
            assertEquals("", edited.imageUri);
            assertEquals("AI", edited.categoryCode);
            assertEquals("Other", db.events().getById(newer.id).categoryCode);
            assertEquals(createdAt, edited.createdAt);
            assertNewestFirst(db, newer.id, loaded.id);
            db.events().delete(loaded); assertEquals(1,db.events().getAll().size());
        } finally { db.close(); }
    }
    private void assertNewestFirst(EventDatabase db, long newer, long older) throws InterruptedException {
        LiveData<List<Event>> events = db.events().observeAll();
        ArrayBlockingQueue<List<Event>> result = new ArrayBlockingQueue<>(1);
        Observer<List<Event>> observer = result::offer;
        android.app.Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        instrumentation.runOnMainSync(() -> events.observeForever(observer));
        try {
            List<Event> rows = result.poll(5, TimeUnit.SECONDS);
            assertNotNull(rows);
            assertEquals(newer, rows.get(0).id);
            assertEquals(older, rows.get(1).id);
        } finally {
            instrumentation.runOnMainSync(() -> events.removeObserver(observer));
        }
    }
}
