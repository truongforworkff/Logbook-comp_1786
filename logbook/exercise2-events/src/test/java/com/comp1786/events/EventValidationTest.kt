package com.comp1786.events

import org.junit.Assert.*
import org.junit.Test

class EventValidationTest {
    @Test fun leapYearDateAccepted() { assertTrue(EventValidation.validDate("2024-02-29")) }
    @Test fun impossibleOrMalformedDatesRejected() {
        listOf("2025-02-29", "2026-04-31", "2026-13-01", "2026-1-01", "01/01/2026", "", "1899-12-31", "2101-01-01").forEach {
            assertFalse(it, EventValidation.validDate(it))
        }
    }
    @Test fun textMustBePresentAndBounded() {
        assertFalse(EventValidation.validText("   "))
        assertFalse(EventValidation.validText("x".repeat(121)))
        assertTrue(EventValidation.validText("  Workshop  "))
    }
}
