package com.comp1786.room.features.events.logic;

import com.comp1786.room.features.events.data.model.Event;
import com.comp1786.room.shared.logic.SearchText;
import java.util.ArrayList;
import java.util.List;

public final class EventSearch {
    private EventSearch() {}

    public static List<Event> filter(List<Event> events, String query, String date, String organiser, String category) {
        String keyword = SearchText.normalize(query);
        String organiserKeyword = SearchText.normalize(organiser);
        List<Event> results = new ArrayList<>();
        for (Event event : events) {
            boolean textMatches = SearchText.normalize(event.name).contains(keyword)
                    || SearchText.normalize(event.description).contains(keyword);
            boolean dateMatches = date.isEmpty() || event.date.equals(date);
            boolean organiserMatches = SearchText.normalize(event.organiser).contains(organiserKeyword);
            if (textMatches && dateMatches && organiserMatches
                    && (category.isEmpty() || event.categoryCode.equals(category))) results.add(event);
        }
        return results;
    }
}
