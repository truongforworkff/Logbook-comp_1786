package com.comp1786.room.features.events.logic;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
public final class EventValidation {
    private static final DateTimeFormatter INPUT = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private EventValidation() {}
    public static boolean validText(String value) { return !value.trim().isEmpty() && value.trim().length() <= 120; }
    public static boolean validDate(String value) {
        if (!value.matches("\\d{2}/\\d{2}/\\d{4}")) return false;
        try { int year = LocalDate.parse(value, INPUT).getYear(); return year >= 1900 && year <= 2100; }
        catch (java.time.format.DateTimeParseException error) { return false; }
    }
    public static String storageDate(String value) { return LocalDate.parse(value, INPUT).toString(); }
    public static String displayDate(String value) { return LocalDate.parse(value).format(INPUT); }
    public static boolean validEmail(String value) { return value.isEmpty() || (value.length() <= 254 && value.matches("[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)+")); }
    public static boolean validPhone(String value) { if (value.isEmpty()) return true; String digits = value.replaceAll("[^0-9]", ""); return value.matches("\\+?[0-9() .-]+") && digits.length() >= 7 && digits.length() <= 15; }
}
