package com.comp1786.room.features.sessions.logic;

import com.comp1786.room.features.sessions.data.model.Session;
import com.comp1786.room.shared.logic.SearchText;
import java.util.ArrayList;
import java.util.List;

public final class SessionSearch {
    private SessionSearch() {}

    public static List<Session> filter(List<Session> sessions, String query) {
        String keyword = SearchText.normalize(query);
        List<Session> results = new ArrayList<>();
        for (Session session : sessions) {
            if (SearchText.normalize(session.name).contains(keyword)
                    || SearchText.normalize(session.description).contains(keyword)) results.add(session);
        }
        return results;
    }
}
