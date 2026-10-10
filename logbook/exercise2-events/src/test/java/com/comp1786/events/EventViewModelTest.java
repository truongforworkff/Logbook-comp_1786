package com.comp1786.events;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import org.junit.Rule;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;
public class EventViewModelTest {
    @Rule public InstantTaskExecutorRule executorRule = new InstantTaskExecutorRule();
    @Test public void eventsAreSortedAndHaveDistinctIds() {
        EventViewModel model = new EventViewModel();
        model.add(new Event(0, "Workshop", "2026-10-11", "Campus", "Student", "Workshop"));
        model.add(new Event(0, "Workshop", "2026-10-10", "Campus", "Student", "Workshop"));
        List<Event> result = model.getEvents().getValue();
        assertEquals("2026-10-10", result.get(0).date);
        assertEquals("2026-10-11", result.get(1).date);
        assertNotEquals(result.get(0).id, result.get(1).id);
    }
    @Test public void newViewModelStartsEmptyWithoutPersistence() {
        EventViewModel original = new EventViewModel();
        original.add(new Event(0, "Event", "2026-10-10", "Campus", "Student", "Other"));
        assertEquals(1, original.getEvents().getValue().size());
        assertTrue(new EventViewModel().getEvents().getValue().isEmpty());
    }
}
