package com.comp1786.room.features.sessions.logic;

import com.comp1786.room.features.sessions.data.model.Session;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class SessionSearchTest {
    private List<Session> sessions() {
        return Arrays.asList(
                new Session(1, 10L, "Đón khách", "09:00", "", "", "Networking", "Kết nối cộng đồng AI"),
                new Session(2, 10L, "Thực hành", "10:00", "", "", "Workshop", "Lập trình tác nhân tự động"));
    }

    @Test public void findsNamePrefixAndDescriptionWithVietnameseText() {
        assertEquals(1, SessionSearch.filter(sessions(), "DON KH").get(0).id);
        assertEquals(1, SessionSearch.filter(sessions(), "CONG DONG").get(0).id);
        assertEquals(2, SessionSearch.filter(sessions(), "tac nhan").get(0).id);
    }

    @Test public void clearRestoresResultsAndNoMatchDoesNotChangeSource() {
        List<Session> source = sessions();
        assertTrue(SessionSearch.filter(source, "unmatched%_").isEmpty());
        assertEquals(2, SessionSearch.filter(source, " ").size());
        assertEquals(2, source.size());
    }
}
