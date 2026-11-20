package com.comp1786.room.features.sessions.logic;
import com.comp1786.room.features.sessions.data.model.Session;
import org.junit.Test;
import static org.junit.Assert.*;

public class SessionValidationTest {
    @Test public void requiredNameAndStrict24HourTime() {
        assertFalse(SessionValidation.validName("  "));
        assertFalse(SessionValidation.validName("x".repeat(121)));
        for (String value : new String[]{"00:00", "09:30", "23:59"}) assertTrue(SessionValidation.validTime(value));
        for (String value : new String[]{"", "9:30", "24:00", "12:60", "09:30 AM"}) assertFalse(SessionValidation.validTime(value));
    }
    @Test public void endTimeMustFollowStartWithinTheSameDay() {
        assertTrue(SessionValidation.validRange("09:30", "10:00"));
        for (String end : new String[]{"", "09:30", "09:00", "24:00", "10:60", "9:45"})
            assertFalse(SessionValidation.validRange("09:30", end));
        assertFalse(SessionValidation.validRange("23:00", "01:00"));
        assertFalse(SessionValidation.validRange("9:00", "10:00"));
    }
    @Test public void parentEventIsRequired() {
        assertFalse(SessionValidation.valid(new Session(0, null, "Welcome", "09:30", "", "", "Workshop", "")));
        assertFalse(SessionValidation.valid(new Session(0, 0L, "Welcome", "09:30", "", "", "Workshop", "")));
    }
    @Test public void optionalFieldsMayBeEmptyAndTypesAreRestricted() {
        for (String type : SessionValidation.TYPES)
            {
                Session session = new Session(0, 1L, "Welcome", "09:30", "", "", type, "");
                session.endTime = "10:30";
                assertTrue(SessionValidation.valid(session));
            }
        Session session = new Session(0, 1L, "Welcome", "09:30", "", "", "invalid", "");
        session.endTime = "10:30";
        assertFalse(SessionValidation.valid(session));
        session.type = "Workshop";
        session.speaker = "x".repeat(121);
        assertFalse(SessionValidation.valid(session));
    }
}
