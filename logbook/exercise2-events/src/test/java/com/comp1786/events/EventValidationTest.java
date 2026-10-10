package com.comp1786.events;
import org.junit.Test;
import static org.junit.Assert.*;
public class EventValidationTest {
    @Test public void leapYearDateAccepted() { assertTrue(EventValidation.validDate("2024-02-29")); }
    @Test public void impossibleOrMalformedDatesRejected() {
        for (String date : new String[]{"2025-02-29", "2026-04-31", "2026-13-01", "2026-1-01", "01/01/2026", "", "1899-12-31", "2101-01-01"}) {
            assertFalse(date, EventValidation.validDate(date));
        }
    }
    @Test public void textMustBePresentAndBounded() {
        assertFalse(EventValidation.validText("   "));
        assertFalse(EventValidation.validText(new String(new char[121]).replace('\0', 'x')));
        assertTrue(EventValidation.validText("  Workshop  "));
    }
}
