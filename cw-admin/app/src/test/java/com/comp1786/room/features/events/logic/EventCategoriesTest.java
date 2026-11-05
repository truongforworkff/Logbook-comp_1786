package com.comp1786.room.features.events.logic;

import org.junit.Test;
import static org.junit.Assert.*;

public class EventCategoriesTest {
    @Test public void codesAreStableAndUnknownValuesFallBackToOther() {
        assertEquals("Other", EventCategories.CODES.get(0));
        assertTrue(EventCategories.valid("AI"));
        assertFalse(EventCategories.valid("ai"));
        assertFalse(EventCategories.valid(null));
        assertEquals(0, EventCategories.position("Unknown"));
        for (String code : EventCategories.CODES) {
            assertEquals(code, EventCategories.CODES.get(EventCategories.position(code)));
        }
    }
}
