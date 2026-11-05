package com.comp1786.room.features.sessions.logic;

import com.comp1786.room.features.sessions.data.model.Session;
import java.util.Arrays;
import java.util.List;

public final class SessionValidation {
    public static final List<String> TYPES = java.util.Collections.unmodifiableList(Arrays.asList(
            "Workshop", "Presentation", "Panel Discussion", "Networking", "Keynote", "Others"));
    private SessionValidation() {}

    public static boolean validName(String value) {
        return !value.trim().isEmpty() && value.trim().length() <= 120;
    }

    public static boolean validTime(String value) {
        return value.matches("([01][0-9]|2[0-3]):[0-5][0-9]");
    }

    public static boolean valid(Session session) {
        return session.id >= 0 && session.eventId != null && session.eventId > 0 && validName(session.name) && validTime(session.time)
                && TYPES.contains(session.type) && session.speaker.length() <= 120
                && session.room.length() <= 120 && session.description.length() <= 2000;
    }
}
