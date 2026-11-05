package com.comp1786.room.features.events.logic;

import com.comp1786.room.features.events.data.model.Event;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class EventSearchTest {
    private List<Event> events() {
        Event ai = new Event(1,"Hội thảo AI","2026-11-17","Hall","Đại học Greenwich","Conference","Upcoming");
        ai.categoryCode = "AI";
        ai.description = "Ứng dụng trí tuệ nhân tạo";
        Event cloud = new Event(2,"Cloud Day","2026-11-18","Online","NVIDIA","Conference","Upcoming");
        cloud.categoryCode = "Technology";
        cloud.description = "Hạ tầng điện toán";
        return Arrays.asList(ai, cloud);
    }

    @Test public void matchesNamePrefixAndDescriptionWithoutCaseOrAccents() {
        assertEquals(1, EventSearch.filter(events(), "HOI TH", "", "", "").get(0).id);
        assertEquals(1, EventSearch.filter(events(), "TRI TUE", "", "", "").get(0).id);
        assertEquals(2, EventSearch.filter(events(), "dien toan", "", "", "").get(0).id);
    }

    @Test public void combinesKeywordDateAndOrganiserUsingAnd() {
        assertEquals(1, EventSearch.filter(events(), "ai", "2026-11-17", "DAI HOC", "").size());
        assertTrue(EventSearch.filter(events(), "ai", "2026-11-18", "DAI HOC", "").isEmpty());
        assertTrue(EventSearch.filter(events(), "ai", "", "NVIDIA", "").isEmpty());
        assertEquals(2, EventSearch.filter(events(), "", "", "nvi", "").get(0).id);
    }

    @Test public void categoryCombinesWithKeywordAndDate() {
        assertEquals(1, EventSearch.filter(events(), "", "", "", "AI").get(0).id);
        assertEquals(1, EventSearch.filter(events(), "tri tue", "2026-11-17", "DAI HOC", "AI").size());
        assertTrue(EventSearch.filter(events(), "ai", "", "", "Technology").isEmpty());
        assertTrue(EventSearch.filter(events(), "", "", "", "Unknown").isEmpty());
    }

    @Test public void blankSearchReturnsAllAndSpecialCharactersStayLiteral() {
        List<Event> source = events();
        assertEquals(2, EventSearch.filter(source, "  ", "", " ", "").size());
        assertTrue(EventSearch.filter(source, "%_'", "", "", "").isEmpty());
        assertEquals(2, source.size());
    }
}
