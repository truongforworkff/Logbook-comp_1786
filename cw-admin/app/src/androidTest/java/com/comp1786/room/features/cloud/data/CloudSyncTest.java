package com.comp1786.room.features.cloud.data;

import android.content.Context;
import androidx.room.Room;
import androidx.test.platform.app.InstrumentationRegistry;
import com.comp1786.room.features.events.data.local.EventDatabase;
import com.comp1786.room.features.events.data.model.Event;
import com.comp1786.room.features.sessions.data.model.Session;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.*;
import com.google.firebase.firestore.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;

public class CloudSyncTest {
    private <T> T await(com.google.android.gms.tasks.Task<T> task) throws Exception {
        return Tasks.await(task, 20, TimeUnit.SECONDS);
    }

    @Test public void durableRevisionsPublishCrudAndRejectForeignSources() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        FirebaseApp app = FirebaseApp.initializeApp(context, new FirebaseOptions.Builder()
                .setProjectId("demo-eventa").setApplicationId("1:123:android:test")
                .setApiKey("fake-emulator-api-key").build(), "sync-test-" + UUID.randomUUID());
        FirebaseFirestore cloud = FirebaseFirestore.getInstance(app);
        cloud.setFirestoreSettings(new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false).build());
        cloud.useEmulator("10.0.2.2", 8086);
        com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance(app);
        auth.useEmulator("10.0.2.2", 9098);
        EventDatabase db = Room.inMemoryDatabaseBuilder(context, EventDatabase.class)
                .addCallback(SyncSchema.CALLBACK).build();
        EventDatabase restored = Room.inMemoryDatabaseBuilder(context, EventDatabase.class)
                .addCallback(SyncSchema.CALLBACK).build();
        try {
            resetEmulator();
            DocumentReference target = cloud.document("role_catalogues/main");
            CloudSnapshot empty = CloudSnapshot.read(db);
            try {
                CloudPublisher.publish(cloud, empty, payload(context, empty, auth));
                fail("A signed-out client cannot publish");
            } catch (java.util.concurrent.ExecutionException expected) {
                assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED,
                        ((FirebaseFirestoreException) expected.getCause()).getCode());
            }
            String email = "publisher-" + UUID.randomUUID() + "@test.eventa";
            await(auth.createUserWithEmailAndPassword(email, "EmulatorOnly123!"));
            try {
                CloudDownload.restore(context, restored, cloud);
                fail("An attendee cannot restore an Admin database");
            } catch (FirebaseFirestoreException expected) {
                assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, expected.getCode());
            }
            String uid = auth.getCurrentUser().getUid();
            ownerRequest("PATCH", "/roles/" + uid, "{\"fields\":{\"role\":{\"stringValue\":\"admin\"}}}");
            Event event = new Event(0, "AI workshop", "2026-11-17", "Campus", "Team", "Workshop", "Upcoming");
            event.endDate = "2026-11-19";
            event.id = db.events().insert(event);
            String stableId = event.publicId;
            Session session = new Session(0, event.id, "Opening", "09:00", "Speaker", "A201", "Keynote", "Welcome");
            session.endTime = "10:00";
            session.id = db.sessions().insert(session);
            CloudSnapshot first = CloudSnapshot.read(db);
            assertTrue(first.state.pending());
            CloudPublisher.publish(cloud, first, payload(context, first, auth));
            db.sync().acknowledge(first.state.revision, 1);
            assertFalse(db.sync().get().pending());
            List<?> published = (List<?>) await(target.get(Source.SERVER)).get("events");
            assertEquals(1, published.size());
            Map<?, ?> publishedEvent = (Map<?, ?>) published.get(0);
            Map<?, ?> publishedSession = (Map<?, ?>) ((List<?>) publishedEvent.get("sessions")).get(0);
            assertEquals("10:00", publishedSession.get("endTime"));
            assertEquals(event.createdAt, publishedEvent.get("createdAt"));
            assertEquals(event.endDate, publishedEvent.get("endDate"));
            assertTrue(CloudDownload.restore(context, restored, cloud));
            Event restoredEvent = restored.events().getAll().get(0);
            Session restoredSession = restored.sessions().getAll().get(0);
            assertEquals(stableId, restoredEvent.publicId);
            assertEquals(event.createdAt, restoredEvent.createdAt);
            assertEquals(event.endDate, restoredEvent.endDate);
            assertEquals(session.id, restoredSession.id);
            assertEquals(Long.valueOf(restoredEvent.id), restoredSession.eventId);
            assertEquals("10:00", restoredSession.endTime);
            assertEquals(first.state.sourceId, restored.sync().get().sourceId);
            assertEquals(first.state.revision, restored.sync().get().uploadedRevision);
            assertFalse(restored.sync().get().pending());
            CloudPublisher.publish(cloud, first, payload(context, first, auth));
            event.name = "Updated AI workshop";
            db.events().update(event);
            assertEquals(stableId, db.events().getAll().get(0).publicId);
            CloudSnapshot second = CloudSnapshot.read(db);
            restoredEvent.name = event.name;
            restored.events().update(restoredEvent);
            CloudSnapshot restoredEdit = CloudSnapshot.read(restored);
            assertEquals(second.state.revision, restoredEdit.state.revision);
            CloudPublisher.publish(cloud, restoredEdit, payload(context, restoredEdit, auth));
            restored.sync().acknowledge(restoredEdit.state.revision, 2);
            assertEquals(event.name, ((Map<?, ?>) ((List<?>) await(target.get(Source.SERVER))
                    .get("events")).get(0)).get("name"));
            try {
                CloudPublisher.publish(cloud, first, payload(context, first, auth));
                fail("An older local copy must not be silently acknowledged");
            } catch (java.util.concurrent.ExecutionException expected) {
                assertEquals("LOCAL_BEHIND", expected.getCause().getMessage());
            }
            assertEquals(Long.valueOf(second.state.revision), await(target.get(Source.SERVER)).getLong("revision"));
            assertTrue(CloudDownload.restore(context, restored, cloud));
            Event latest = restored.events().getAll().get(0);
            assertEquals("Updated AI workshop", latest.name);
            assertEquals(restoredEvent.id, latest.id);

            // Two devices can create the same revision with different edits.
            latest.name = "Edited on downloaded device";
            restored.events().update(latest);
            try {
                CloudDownload.restore(context, restored, cloud);
                fail("Downloading must preserve pending edits");
            } catch (IllegalStateException expected) {
                assertEquals(latest.name, restored.events().getAll().get(0).name);
            }
            db.sync().acknowledge(second.state.revision, 2);
            event.name = "Edited on original device";
            db.events().update(event);
            CloudSnapshot third = CloudSnapshot.read(db);
            CloudPublisher.publish(cloud, third, payload(context, third, auth));
            db.sync().acknowledge(third.state.revision, 3);
            CloudSnapshot competing = CloudSnapshot.read(restored);
            assertEquals(third.state.revision, competing.state.revision);
            try {
                CloudPublisher.publish(cloud, competing, payload(context, competing, auth));
                fail("A different write must not be treated as a retry");
            } catch (java.util.concurrent.ExecutionException expected) {
                assertEquals("LOCAL_BEHIND", expected.getCause().getMessage());
            }
            restored.events().update(latest);
            CloudSnapshot higher = CloudSnapshot.read(restored);
            try {
                CloudPublisher.publish(cloud, higher, payload(context, higher, auth));
                fail("A higher local revision with an older base must not replace the catalog");
            } catch (java.util.concurrent.ExecutionException expected) {
                assertEquals("LOCAL_BEHIND", expected.getCause().getMessage());
            }
            // An edit after snapshot capture cannot be acknowledged by an earlier upload.
            session.name = "New session title";
            db.sessions().update(session);
            db.sync().acknowledge(second.state.revision, 2);
            assertTrue(db.sync().get().pending());
            db.sync().setError(second.state.revision, "stale error");
            assertEquals("", db.sync().get().error);
            await(cloud.disableNetwork());
            db.events().delete(event);
            assertTrue(db.sessions().getAll().isEmpty());
            CloudSnapshot deleted = CloudSnapshot.read(db);
            assertTrue(deleted.state.pending());
            await(cloud.enableNetwork());
            CloudPublisher.publish(cloud, deleted, payload(context, deleted, auth));
            db.sync().acknowledge(deleted.state.revision, 3);
            assertTrue(((List<?>) await(target.get(Source.SERVER)).get("events")).isEmpty());
            assertEquals(uid, await(target.get(Source.SERVER)).getString("updatedBy"));
            assertFalse(db.sync().get().pending());
            SyncState foreignState = new SyncState(UUID.randomUUID().toString());
            foreignState.revision = deleted.state.revision + 1;
            CloudSnapshot foreign = new CloudSnapshot(foreignState, Collections.emptyList(), Collections.emptyList());
            try {
                CloudPublisher.publish(cloud, foreign, payload(context, foreign, auth));
                fail("Different local database must not overwrite the catalog");
            } catch (java.util.concurrent.ExecutionException expected) {
                assertEquals("SOURCE_MISMATCH", expected.getCause().getMessage());
            }
            db.sessions().insert(new Session(0, null, "Unassigned", "12:00", "", "", "Workshop", ""));
            try { CloudSnapshot.read(db).payload(context); fail("Orphans must not disappear silently"); }
            catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("agenda item")); }
        } finally {
            restored.close();
            db.close();
            await(cloud.terminate());
            app.delete();
        }
    }

    private Map<String, Object> payload(Context context, CloudSnapshot snapshot,
            com.google.firebase.auth.FirebaseAuth auth) {
        Map<String, Object> payload = snapshot.payload(context);
        payload.put("updatedBy", auth.getCurrentUser() == null ? "signed-out" : auth.getCurrentUser().getUid());
        return payload;
    }

    private void resetEmulator() throws Exception {
        ownerRequest("DELETE", "", null);
    }

    public static void ownerRequest(String method, String path, String body) throws Exception {
        String root = path.isEmpty()
                ? "http://10.0.2.2:8086/emulator/v1/projects/demo-eventa/databases/(default)/documents"
                : "http://10.0.2.2:8086/v1/projects/demo-eventa/databases/(default)/documents" + path;
        HttpURLConnection connection = (HttpURLConnection) new URL(root).openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Authorization", "Bearer owner");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        try {
            if (body != null) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                try (java.io.OutputStream stream = connection.getOutputStream()) {
                    stream.write(body.getBytes(StandardCharsets.UTF_8));
                }
            }
            assertTrue("Emulator owner request failed: " + connection.getResponseCode(),
                    connection.getResponseCode() >= 200 && connection.getResponseCode() < 300);
        } finally { connection.disconnect(); }
    }
}
