package com.comp1786.room.features.cloud.data;

import android.content.Context;
import android.graphics.Bitmap;
import com.comp1786.room.features.events.data.local.EventDatabase;
import com.comp1786.room.features.events.data.model.Event;
import com.comp1786.room.features.events.logic.EventValidation;
import com.comp1786.room.features.sessions.data.model.Session;
import com.comp1786.room.features.sessions.logic.SessionValidation;
import com.comp1786.room.shared.data.PhotoDecoder;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class CloudDownload {
    private static final String UNSENT = "Upload the changes on this device before downloading the catalogue.";
    private CloudDownload() {}

    public static boolean restore(Context context, EventDatabase database, FirebaseFirestore firestore)
            throws Exception {
        DocumentSnapshot remote = Tasks.await(
                CloudPublisher.catalog(firestore).get(Source.SERVER), 25, TimeUnit.SECONDS);
        if (!remote.exists()) return false;
        String sourceId = remote.getString("sourceId");
        Long revision = remote.getLong("revision");
        if (!Long.valueOf(1).equals(remote.getLong("schemaVersion"))
                || sourceId == null || sourceId.length() != 36 || revision == null || revision < 0) {
            throw invalidCatalog();
        }
        List<?> events = rows(remote.get("events"), 100);
        database.runInTransaction(() -> restoreRows(context, database, events, sourceId, revision));
        return true;
    }

    private static void restoreRows(Context context, EventDatabase db, List<?> events,
            String sourceId, long revision) {
        SyncState local = db.sync().get();
        List<Event> previous = db.events().getAll();
        boolean fresh = local.isNew() && previous.isEmpty() && db.sessions().getAll().isEmpty();
        if (!fresh) {
            if (local.pending()) throw new IllegalStateException(UNSENT);
            if (!local.sourceId.equals(sourceId)) {
                throw new IllegalStateException("The cloud catalogue belongs to a different dataset. Your local data has been kept.");
            }
            if (local.revision >= revision) return;
        }
        Map<String, Event> previousById = new HashMap<>();
        for (Event event : previous) {
            previousById.put(event.publicId, event);
            db.events().delete(event);
        }
        Set<Long> sessionIds = new HashSet<>();
        for (Object row : events) {
            Map<?, ?> fields = row(row);
            Event event = event(fields);
            Event old = previousById.get(event.publicId);
            if (old != null) {
                event.id = old.id;
                if (event.createdAt == 0) event.createdAt = old.createdAt;
                preserveOriginalCover(context, old, event);
            }
            long eventId = db.events().insertRow(event);
            for (Object session : rows(fields.get("sessions"), 500)) {
                Session entry = session(row(session), event.publicId, eventId);
                if (!sessionIds.add(entry.id) || sessionIds.size() > 500) throw invalidCatalog();
                db.sessions().insert(entry);
            }
        }
        // Import triggers count the individual writes. The transaction adopts the published revision.
        db.sync().restore(sourceId, revision, System.currentTimeMillis());
    }

    private static Event event(Map<?, ?> fields) {
        String date = required(fields, "date");
        String endDate = optional(fields, "endDate");
        try {
            String day = EventValidation.displayDate(date);
            String end = endDate.isEmpty() ? "" : EventValidation.displayDate(endDate);
            if (!EventValidation.validDate(day) || !EventValidation.validEndDate(day, end)) {
                throw invalidCatalog();
            }
        } catch (java.time.format.DateTimeParseException invalid) {
            throw invalidCatalog();
        }
        Event event = new Event(0, required(fields, "name"), date, required(fields, "location"),
                required(fields, "organiser"), required(fields, "type"), required(fields, "status"));
        event.endDate = endDate;
        event.publicId = required(fields, "id");
        if (event.publicId.length() > 160) throw invalidCatalog();
        Object createdAt = fields.get("createdAt");
        if (createdAt != null) {
            if (!(createdAt instanceof Long) || (Long) createdAt <= 0 || (Long) createdAt > 253402300799999L) {
                throw invalidCatalog();
            }
            event.createdAt = (Long) createdAt;
        }
        String category = optional(fields, "categoryCode");
        if (!category.isEmpty()) event.categoryCode = category;
        event.description = optional(fields, "description");
        event.contactEmail = optional(fields, "contactEmail");
        event.contactPhone = optional(fields, "contactPhone");
        Object cover = fields.get("image");
        if (cover == null || "".equals(cover)) return event;
        if (!(cover instanceof String)) throw invalidCatalog();
        Bitmap image = PhotoDecoder.decodeCloudThumbnail((String) cover);
        if (image == null) throw invalidCatalog();
        image.recycle();
        event.imageUri = (String) cover;
        return event;
    }

    private static Session session(Map<?, ?> fields, String publicEventId, long eventId) {
        String prefix = CloudSnapshot.sessionPrefix(publicEventId);
        String publicId = required(fields, "id");
        if (!publicId.startsWith(prefix)) throw invalidCatalog();
        String suffix = publicId.substring(prefix.length());
        if (!suffix.matches("[1-9][0-9]{0,18}")) throw invalidCatalog();
        long id;
        try {
            id = Long.parseLong(suffix);
        } catch (NumberFormatException invalid) {
            throw invalidCatalog();
        }
        if (id == Long.MAX_VALUE) throw invalidCatalog();
        String start = required(fields, "time");
        String end = optional(fields, "endTime");
        if (!SessionValidation.validTime(start)
                || (!end.isEmpty() && !SessionValidation.validRange(start, end))) throw invalidCatalog();
        Session session = new Session(id, eventId, required(fields, "name"), start,
                optional(fields, "speaker"), optional(fields, "room"), required(fields, "type"),
                optional(fields, "description"));
        session.endTime = end;
        return session;
    }

    private static void preserveOriginalCover(Context context, Event old, Event imported) {
        if (imported.imageUri.isEmpty() || old.imageUri.isEmpty()
                || old.imageUri.startsWith(PhotoDecoder.CLOUD_THUMBNAIL_PREFIX)) return;
        try {
            if (CloudCover.thumbnail(context, old.imageUri).equals(imported.imageUri)) {
                imported.imageUri = old.imageUri;
            }
        } catch (IllegalArgumentException ignored) {
            // Restore the published thumbnail when the original is no longer accessible.
        }
    }

    private static String required(Map<?, ?> row, String key) {
        String value = optional(row, key);
        if (value.trim().isEmpty()) throw invalidCatalog();
        return value;
    }

    private static String optional(Map<?, ?> row, String key) {
        Object value = row.get(key);
        if (value == null) return "";
        if (!(value instanceof String) || ((String) value).length() > 16384) throw invalidCatalog();
        return (String) value;
    }

    private static List<?> rows(Object value, int limit) {
        if (!(value instanceof List<?>) || ((List<?>) value).size() > limit) throw invalidCatalog();
        return (List<?>) value;
    }

    private static Map<?, ?> row(Object value) {
        if (!(value instanceof Map<?, ?>)) throw invalidCatalog();
        return (Map<?, ?>) value;
    }

    private static IllegalArgumentException invalidCatalog() {
        return new IllegalArgumentException("The Firebase catalogue is invalid. Your local data has been kept.");
    }
}
