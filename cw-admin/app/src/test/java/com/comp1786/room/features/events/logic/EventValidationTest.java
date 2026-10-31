package com.comp1786.room.features.events.logic;
import com.comp1786.room.features.events.data.model.Event;
import org.junit.Test;
import static org.junit.Assert.*;
public class EventValidationTest {
    @Test public void strictDatesAndRoundTrip() {
        assertTrue(EventValidation.validDate("29/02/2024"));
        for (String date : new String[]{"29/02/2025","31/04/2026","1/01/2026","2026-01-01","01/01/2101",""}) assertFalse(date, EventValidation.validDate(date));
        assertEquals("2024-02-29", EventValidation.storageDate("29/02/2024"));
        assertEquals("29/02/2024", EventValidation.displayDate("2024-02-29"));
    }
    @Test public void optionalContactsAcceptEmptyButRejectMalformedInput() {
        assertTrue(EventValidation.validEmail(""));
        assertTrue(EventValidation.validEmail("manager@example.com"));
        assertFalse(EventValidation.validEmail("manager@"));
        assertFalse(EventValidation.validEmail("not an email"));
        assertTrue(EventValidation.validPhone(""));
        assertTrue(EventValidation.validPhone("+84 912 345 678"));
        assertFalse(EventValidation.validPhone("-------"));
        assertFalse(EventValidation.validPhone("123"));
        assertFalse(EventValidation.validPhone("1234567890123456"));
    }
    @Test public void requiredTextAndDuplicateKeyNormalization() {
        assertFalse(EventValidation.validText("   "));
        assertFalse(EventValidation.validText(new String(new char[121]).replace('\0','x')));
        Event first = new Event(0,"  Workshop  ","2026-11-08","Campus","Student","Workshop","Upcoming");
        Event other = new Event(0,"workshop","2026-11-08","Campus","Student","Workshop","Upcoming");
        assertEquals(first.normalizedName,other.normalizedName);
    }
}
