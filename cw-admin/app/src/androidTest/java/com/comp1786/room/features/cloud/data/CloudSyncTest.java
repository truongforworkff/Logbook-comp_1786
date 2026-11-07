package com.comp1786.room.features.cloud.data;

import android.content.Context;
import androidx.room.Room;
import androidx.test.platform.app.InstrumentationRegistry;
import com.comp1786.room.features.events.data.local.EventDatabase;
import com.comp1786.room.features.events.data.model.Event;
import com.comp1786.room.features.sessions.data.model.Session;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.*;
import com.google.firebase.auth.FirebaseAuth;
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
        FirebaseAuth auth = FirebaseAuth.getInstance(app);
        auth.useEmulator("10.0.2.2", 9098);
        FirebaseFirestore cloud = FirebaseFirestore.getInstance(app);
        cloud.setFirestoreSettings(new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false).build());
        cloud.useEmulator("10.0.2.2", 8086);
        EventDatabase db = Room.inMemoryDatabaseBuilder(context, EventDatabase.class)
                .addCallback(SyncSchema.CALLBACK).build();
        try {
            String uid = await(auth.createUserWithEmailAndPassword(
                    UUID.randomUUID() + "@example.invalid", "emulator-only-password")).getUser().getUid();
            resetEmulator();
            DocumentReference target = cloud.document("catalogues/main");
            CloudSnapshot empty = CloudSnapshot.read(db);
            try {
                CloudPublisher.publish(cloud, empty, empty.payload(context, uid), uid);
                fail("Unapproved account cannot publish");
            } catch (java.util.concurrent.ExecutionException expected) {
                assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED,
                        ((FirebaseFirestoreException) expected.getCause()).getCode());
            }
            ownerRequest("PATCH", "/admins/" + uid, "{\"fields\":{\"enabled\":{\"booleanValue\":true}}}");
            Event event = new Event(0, "AI workshop", "2026-11-17", "Campus", "Team", "Workshop", "Upcoming");
            event.id = db.events().insert(event);
            String stableId = event.publicId;
            Session session = new Session(0, event.id, "Opening", "09:00", "Speaker", "A201", "Keynote", "Welcome");
            session.id = db.sessions().insert(session);
            CloudSnapshot first = CloudSnapshot.read(db);
            assertTrue(first.state.pending());
            CloudPublisher.publish(cloud, first, first.payload(context, uid), uid);
            db.sync().acknowledge(first.state.revision, 1);
            assertFalse(db.sync().get().pending());
            assertEquals(1, ((List<?>) await(target.get(Source.SERVER)).get("events")).size());
            event.name = "Updated AI workshop";
            db.events().update(event);
            assertEquals(stableId, db.events().getAll().get(0).publicId);
            CloudSnapshot second = CloudSnapshot.read(db);
            CloudPublisher.publish(cloud, second, second.payload(context, uid), uid);
            try {
                CloudPublisher.publish(cloud, first, first.payload(context, uid), uid);
                fail("An older local copy must not be silently acknowledged");
            } catch (java.util.concurrent.ExecutionException expected) {
                assertEquals("LOCAL_BEHIND", expected.getCause().getMessage());
            }
            assertEquals(Long.valueOf(second.state.revision), await(target.get(Source.SERVER)).getLong("revision"));
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
            CloudPublisher.publish(cloud, deleted, deleted.payload(context, uid), uid);
            db.sync().acknowledge(deleted.state.revision, 3);
            assertTrue(((List<?>) await(target.get(Source.SERVER)).get("events")).isEmpty());
            assertFalse(db.sync().get().pending());
            SyncState foreignState = new SyncState(UUID.randomUUID().toString());
            foreignState.revision = deleted.state.revision + 1;
            CloudSnapshot foreign = new CloudSnapshot(foreignState, Collections.emptyList(), Collections.emptyList());
            try {
                CloudPublisher.publish(cloud, foreign, foreign.payload(context, uid), uid);
                fail("Different local database must not overwrite the catalog");
            } catch (java.util.concurrent.ExecutionException expected) {
                assertEquals("SOURCE_MISMATCH", expected.getCause().getMessage());
            }
            db.sessions().insert(new Session(0, null, "Unassigned", "12:00", "", "", "Workshop", ""));
            try { CloudSnapshot.read(db).payload(context, uid); fail("Orphans must not disappear silently"); }
            catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("session")); }
        } finally {
            db.close();
            await(cloud.terminate());
            app.delete();
        }
    }

    private void resetEmulator() throws Exception {
        ownerRequest("DELETE", "", null);
    }

    private void ownerRequest(String method, String path, String body) throws Exception {
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
