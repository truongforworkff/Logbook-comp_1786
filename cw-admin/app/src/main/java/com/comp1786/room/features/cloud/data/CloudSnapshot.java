package com.comp1786.room.features.cloud.data;

import android.content.Context;
import com.comp1786.room.features.events.data.local.EventDatabase;
import com.comp1786.room.features.events.data.model.Event;
import com.comp1786.room.features.sessions.data.model.Session;
import com.google.firebase.firestore.FieldValue;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class CloudSnapshot {
    public final SyncState state;
    public final List<Event> events;
    public final List<Session> sessions;

    public CloudSnapshot(SyncState state, List<Event> events, List<Session> sessions) {
        this.state = state;
        this.events = events;
        this.sessions = sessions;
    }

    public static CloudSnapshot read(EventDatabase database) {
        return database.runInTransaction(() -> new CloudSnapshot(
                database.sync().get(), database.events().getAll(), database.sessions().getAll()));
    }

    public Map<String, Object> payload(Context context) {
        if (events.size() > 100 || sessions.size() > 500)
            throw new IllegalArgumentException("Danh mục vượt giới hạn 100 sự kiện hoặc 500 session.");
        Set<Long> ids = new HashSet<>();
        for (Event event : events) ids.add(event.id);
        for (Session session : sessions) {
            if (session.eventId == null || !ids.contains(session.eventId))
                throw new IllegalArgumentException("Gắn tất cả session vào sự kiện trước khi đồng bộ.");
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, String> covers = new HashMap<>();
        for (Event event : events) rows.add(eventRow(context, event, covers));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("schemaVersion", 1L);
        result.put("sourceId", state.sourceId);
        result.put("revision", state.revision);
        result.put("events", rows);
        // shortcut: one atomic catalog for this coursework; split documents above 512 KiB.
        if (new JSONObject(result).toString().getBytes(StandardCharsets.UTF_8).length > 512 * 1024)
            throw new IllegalArgumentException("Danh mục và ảnh thu nhỏ vượt 512 KB. Giảm số lượng hoặc ảnh trước khi đồng bộ.");
        result.put("updatedAt", FieldValue.serverTimestamp());
        return result;
    }

    private Map<String, Object> eventRow(Context context, Event event, Map<String, String> covers) {
        if (event.publicId.isEmpty()) throw new IllegalArgumentException("Sự kiện thiếu ID đồng bộ.");
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", event.publicId);
        row.put("name", event.name);
        row.put("date", event.date);
        row.put("location", event.location);
        row.put("organiser", event.organiser);
        row.put("type", event.type);
        row.put("status", event.status);
        row.put("categoryCode", event.categoryCode);
        row.put("description", event.description);
        row.put("contactEmail", event.contactEmail);
        row.put("contactPhone", event.contactPhone);
        if (!covers.containsKey(event.imageUri)) covers.put(event.imageUri, CloudCover.thumbnail(context, event.imageUri));
        row.put("image", covers.get(event.imageUri));
        List<Map<String, Object>> linked = new ArrayList<>();
        for (Session session : sessions) {
            if (Objects.equals(session.eventId, event.id)) linked.add(sessionRow(event.publicId, session));
        }
        row.put("sessions", linked);
        validateText(row);
        return row;
    }

    private void validateText(Map<String, Object> row) {
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (!entry.getKey().equals("image") && entry.getValue() instanceof String
                    && ((String) entry.getValue()).length() > 16384)
                throw new IllegalArgumentException("Nội dung quá dài để đồng bộ: " + entry.getKey());
        }
    }

    private Map<String, Object> sessionRow(String eventId, Session session) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", eventId + "-session-" + session.id);
        row.put("name", session.name);
        row.put("time", session.time);
        row.put("speaker", session.speaker);
        row.put("room", session.room);
        row.put("type", session.type);
        row.put("description", session.description);
        validateText(row);
        return row;
    }
}
