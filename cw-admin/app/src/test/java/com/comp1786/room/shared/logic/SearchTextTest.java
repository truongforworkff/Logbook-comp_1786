package com.comp1786.room.shared.logic;

import org.junit.Test;
import static org.junit.Assert.*;

public class SearchTextTest {
    @Test public void handlesVietnameseAndCombiningMarks() {
        assertEquals("dien toan", SearchText.normalize("  ĐIỆN TOÁN "));
        assertEquals(SearchText.normalize("cộng đồng"), SearchText.normalize("co\u0323ng đo\u0302\u0300ng"));
        assertEquals("", SearchText.normalize("  "));
    }
}
