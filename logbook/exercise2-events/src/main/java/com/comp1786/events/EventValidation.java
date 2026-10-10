package com.comp1786.events;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
public final class EventValidation {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private EventValidation() {}
    public static boolean validText(String value) {
        int length = value.trim().length();
        return length >= 1 && length <= 120;
    }
    public static boolean validDate(String value) {
        if (!value.matches("\\d{4}-\\d{2}-\\d{2}")) return false;
        try {
            int year = LocalDate.parse(value, DATE_FORMAT).getYear();
            return year >= 1900 && year <= 2100;
        } catch (DateTimeParseException error) { return false; }
    }
}
