package com.comp1786.room.features.events.logic;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class EventCategories {
    public static final List<String> CODES = Collections.unmodifiableList(Arrays.asList(
            "Other", "AI", "Technology", "Design", "Business", "Education", "Community"));
    private EventCategories() {}
    public static boolean valid(String code) { return CODES.contains(code); }
    public static int position(String code) { return Math.max(0, CODES.indexOf(code)); }
}
