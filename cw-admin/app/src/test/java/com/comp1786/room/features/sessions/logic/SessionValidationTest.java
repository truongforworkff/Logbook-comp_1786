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
    @Test public void parentEventIsRequired() {
        assertFalse(SessionValidation.valid(new Session(0, null, "Welcome", "09:30", "", "", "Workshop", "")));
        assertFalse(SessionValidation.valid(new Session(0, 0L, "Welcome", "09:30", "", "", "Workshop", "")));
    }
    @Test public void optionalFieldsMayBeEmptyAndTypesAreRestricted() {
        for (String type : SessionValidation.TYPES)
            assertTrue(SessionValidation.valid(new Session(0, 1L,"Welcome","09:30","","",type,"")));
        assertFalse(SessionValidation.valid(new Session(0, 1L,"Welcome","09:30","","","invalid","")));
        assertFalse(SessionValidation.valid(new Session(0, 1L,"Welcome","09:30","x".repeat(121),"","Workshop","")));
    }
}
