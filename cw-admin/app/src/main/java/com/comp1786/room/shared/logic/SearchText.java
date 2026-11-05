package com.comp1786.room.shared.logic;

import java.text.Normalizer;
import java.util.Locale;

public final class SearchText {
    private SearchText() {}

    public static String normalize(String value) {
        String lower = value.trim().toLowerCase(Locale.ROOT).replace('đ', 'd');
        return Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
    }
}
