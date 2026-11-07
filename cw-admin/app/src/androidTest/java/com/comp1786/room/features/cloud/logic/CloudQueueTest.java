package com.comp1786.room.features.cloud.logic;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.work.NetworkType;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import com.comp1786.room.features.events.data.local.EventDatabase;
import com.comp1786.room.features.events.data.model.Event;
import com.comp1786.room.features.sessions.data.model.Session;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;

public class CloudQueueTest {
    @Test public void editsQueueNetworkConstrainedSyncWithoutPressingUpload() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        EventDatabase database = EventDatabase.get(context);
        WorkManager manager = WorkManager.getInstance(context);
        CloudSync.start(context);
        Event event = new Event(0, "Auto sync " + UUID.randomUUID(), "2026-12-01",
                "Test campus", "Test team", "Workshop", "Upcoming");
        try {
            Set<UUID> before = workIds(manager);
            event.id = database.events().insert(event);
            awaitNewWork(manager, before);
            before = workIds(manager);
            Session session = new Session(0, event.id, "Opening", "09:00", "", "", "Keynote", "");
            session.id = database.sessions().insert(session);
            awaitNewWork(manager, before);
            before = workIds(manager);
            event.description = "Edited locally";
            database.events().update(event);
            awaitNewWork(manager, before);
            before = workIds(manager);
            session.name = "Updated session";
            database.sessions().update(session);
            awaitNewWork(manager, before);
            before = workIds(manager);
            database.events().delete(event);
            awaitNewWork(manager, before);
            assertFalse(database.sessions().getAll().stream().anyMatch(value -> value.id == session.id));
        } finally {
            if (event.id != 0) database.events().delete(event);
        }
    }

    private Set<UUID> workIds(WorkManager manager) throws Exception {
        Set<UUID> ids = new HashSet<>();
        for (WorkInfo info : manager.getWorkInfosForUniqueWork(CloudSync.IMMEDIATE).get(10, TimeUnit.SECONDS)) {
            ids.add(info.getId());
        }
        return ids;
    }

    private void awaitNewWork(WorkManager manager, Set<UUID> previous) throws Exception {
        long deadline = android.os.SystemClock.uptimeMillis() + 15000;
        while (android.os.SystemClock.uptimeMillis() < deadline) {
            for (WorkInfo info : manager.getWorkInfosForUniqueWork(CloudSync.IMMEDIATE).get(10, TimeUnit.SECONDS)) {
                if (!previous.contains(info.getId())) {
                    assertEquals(NetworkType.CONNECTED, info.getConstraints().getRequiredNetworkType());
                    return;
                }
            }
            Thread.sleep(100);
        }
        fail("A committed event/session change must automatically queue sync");
    }
}
