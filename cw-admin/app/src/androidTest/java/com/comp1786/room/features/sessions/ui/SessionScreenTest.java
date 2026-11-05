package com.comp1786.room.features.sessions.ui;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.widget.EditText;
import androidx.test.platform.app.InstrumentationRegistry;
import com.comp1786.room.R;
import com.comp1786.room.features.events.data.local.EventDatabase;
import com.comp1786.room.features.sessions.data.local.SessionDao;
import com.comp1786.room.features.sessions.data.model.Session;
import com.comp1786.room.features.sessions.ui.editor.SessionEditorActivity;
import org.junit.Test;
import static org.junit.Assert.*;

public class SessionScreenTest {
    private long eventId;
    @Test public void invalidInputThenCreateEditAndDelete() throws Exception {
        Instrumentation test = InstrumentationRegistry.getInstrumentation();
        Context context = test.getTargetContext();
        SessionDao dao = EventDatabase.get(context).sessions();
        com.comp1786.room.features.events.data.model.Event parent =
                new com.comp1786.room.features.events.data.model.Event(0, "Session UI parent", "2026-12-20", "Hall", "Team", "Workshop", "Upcoming");
        parent.id = EventDatabase.get(context).events().insert(parent);
        eventId = parent.id;
        com.comp1786.room.features.events.data.model.Event other =
                new com.comp1786.room.features.events.data.model.Event(0, "Other UI event", "2026-12-21", "Hall", "Team", "Workshop", "Upcoming");
        other.id = EventDatabase.get(context).events().insert(other);
        dao.insert(new Session(0, other.id, "Hidden in other event", "12:00", "", "", "Workshop", "Optional details"));
        verifySessionTab(test, context);
        SessionEditorActivity activity = launch(test, context, 0);
        long id = 0;
        try {
            test.runOnMainSync(() -> activity.findViewById(R.id.save).performClick());
            test.waitForIdleSync();
            assertFalse(activity.isFinishing());
            fill(test, activity, R.id.nameInput, "Session UI test");
            fill(test, activity, R.id.timeInput, "25:00");
            test.runOnMainSync(() -> activity.findViewById(R.id.save).performClick());
            assertFalse(activity.isFinishing());
            fill(test, activity, R.id.timeInput, "09:30");
            test.runOnMainSync(() -> activity.findViewById(R.id.moreOptions).performClick());
            fill(test, activity, R.id.speakerInput, "Guest");
            fill(test, activity, R.id.descriptionInput, "Optional details");
            screenshot(test, "09-session-editor.png");
            test.runOnMainSync(() -> activity.findViewById(R.id.save).performClick());
            awaitFinished(test, activity);
            for (Session session : dao.getAll()) if (session.name.equals("Session UI test")) id = session.id;
            assertTrue(id > 0);
            verifyEventList(test, context);

            SessionEditorActivity edit = launch(test, context, id);
            try {
                awaitLoaded(test, edit);
                fill(test, edit, R.id.nameInput, "Session UI updated");
                test.runOnMainSync(() -> edit.findViewById(R.id.save).performClick());
                awaitFinished(test, edit);
                final long savedId = id;
                assertTrue(dao.getAll().stream().anyMatch(s -> s.id == savedId && s.name.equals("Session UI updated") && s.speaker.equals("Guest")));
            } finally { test.runOnMainSync(edit::finish); }

            SessionEditorActivity remove = launch(test, context, id);
            try {
                awaitLoaded(test, remove);
                test.runOnMainSync(() -> remove.findViewById(R.id.delete).performClick());
                clickPopup(test, "secondary");
                assertFalse(remove.isFinishing());
                test.runOnMainSync(() -> remove.findViewById(R.id.delete).performClick());
                screenshot(test, "10-session-delete.png");
                clickPopup(test, "primary");
                awaitFinished(test, remove);
                final long deletedId = id;
                assertFalse(dao.getAll().stream().anyMatch(s -> s.id == deletedId));
            } finally { test.runOnMainSync(remove::finish); }
        } finally {
            test.runOnMainSync(activity::finish);
            for (Session session : dao.getAll())
                if (session.id == id) dao.delete(session);
            EventDatabase.get(context).events().delete(parent);
            EventDatabase.get(context).events().delete(other);
        }
    }

    private void verifySessionTab(Instrumentation test, Context context) throws Exception {
        com.comp1786.room.features.events.ui.list.MainActivity home =
                (com.comp1786.room.features.events.ui.list.MainActivity) test.startActivitySync(
                        new Intent(context, com.comp1786.room.features.events.ui.list.MainActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        Instrumentation.ActivityMonitor tabMonitor = test.addMonitor(
                com.comp1786.room.features.sessions.ui.list.SessionActivity.class.getName(), null, false);
        android.app.Activity tab = null;
        try {
            test.runOnMainSync(() -> home.findViewById(R.id.sessionsTab).performClick());
            tab = test.waitForMonitorWithTimeout(tabMonitor, 10000);
            assertNotNull("Session tab opens", tab);
            final android.app.Activity page = tab;
            boolean[] selected = {false};
            long deadline = android.os.SystemClock.uptimeMillis() + 10000;
            while (!selected[0] && android.os.SystemClock.uptimeMillis() < deadline) {
                test.runOnMainSync(() -> {
                    android.widget.Spinner picker = page.findViewById(R.id.eventPicker);
                    if (picker.getAdapter() == null) return;
                    for (int index = 1; index < picker.getAdapter().getCount(); index++) {
                        if (picker.getAdapter().getItem(index).toString().startsWith("Session UI parent ·")) {
                            assertFalse("Must choose an event before adding", page.findViewById(R.id.add).isEnabled());
                            picker.setSelection(index);
                            selected[0] = true;
                            break;
                        }
                    }
                });
                if (!selected[0]) Thread.sleep(50);
            }
            assertTrue("Parent event appears in picker", selected[0]);
            test.waitForIdleSync();
            screenshot(test, "15-session-tab.png");
            Instrumentation.ActivityMonitor editorMonitor = test.addMonitor(SessionEditorActivity.class.getName(), null, false);
            try {
                test.runOnMainSync(() -> page.findViewById(R.id.add).performClick());
                SessionEditorActivity form = (SessionEditorActivity) test.waitForMonitorWithTimeout(editorMonitor, 10000);
                assertNotNull("Adding from tab opens shared form", form);
                boolean[] parentReady = {false};
                deadline = android.os.SystemClock.uptimeMillis() + 10000;
                while (!parentReady[0] && android.os.SystemClock.uptimeMillis() < deadline) {
                    test.runOnMainSync(() -> parentReady[0] =
                            ((android.widget.TextView) form.findViewById(R.id.eventName)).getText().toString().contains("Session UI parent"));
                    if (!parentReady[0]) Thread.sleep(50);
                }
                assertTrue("Form is linked to chosen event", parentReady[0]);
                screenshot(test, "16-session-tab-create.png");
                test.runOnMainSync(form::finish);
            } finally { test.removeMonitor(editorMonitor); }
        } finally {
            test.removeMonitor(tabMonitor);
            if (tab != null) {
                final android.app.Activity page = tab;
                test.runOnMainSync(page::finish);
            }
            test.runOnMainSync(home::finish);
        }
    }

    private void verifyEventList(Instrumentation test, Context context) throws Exception {
        com.comp1786.room.features.sessions.ui.list.SessionActivity list =
                (com.comp1786.room.features.sessions.ui.list.SessionActivity) test.startActivitySync(
                        com.comp1786.room.features.sessions.ui.list.SessionActivity.intent(context, eventId, false)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        try {
            long deadline = android.os.SystemClock.uptimeMillis() + 10000;
            boolean[] ready = {false};
            while (!ready[0] && android.os.SystemClock.uptimeMillis() < deadline) {
                test.runOnMainSync(() -> ready[0] = "1 session".contentEquals(
                        ((android.widget.TextView) list.findViewById(R.id.count)).getText())
                        && ((androidx.recyclerview.widget.RecyclerView) list.findViewById(R.id.list)).getChildCount() > 0);
                if (!ready[0]) Thread.sleep(50);
            }
            assertTrue("Only the selected event's session appears", ready[0]);
            screenshot(test, "11-event-sessions.png");
            test.runOnMainSync(() -> ((EditText) list.findViewById(R.id.query)).setText("OPTIONAL"));
            awaitSessionCount(test, list, "1 / 1 session");
            test.runOnMainSync(() -> ((EditText) list.findViewById(R.id.query)).setText("No match%"));
            awaitSessionCount(test, list, "0 / 1 session");
            screenshot(test, "18-session-no-search-result.png");
            test.runOnMainSync(() -> list.findViewById(R.id.clearQuery).performClick());
            awaitSessionCount(test, list, "1 session");
            verifyReadOnlyDetails(test, list);
        } finally { test.runOnMainSync(list::finish); }
    }

    private void awaitSessionCount(Instrumentation test, android.app.Activity page, String expected) throws Exception {
        long deadline = android.os.SystemClock.uptimeMillis() + 10000;
        boolean[] ready = {false};
        while (!ready[0] && android.os.SystemClock.uptimeMillis() < deadline) {
            test.runOnMainSync(() -> ready[0] = expected.contentEquals(
                    ((android.widget.TextView) page.findViewById(R.id.count)).getText()));
            if (!ready[0]) Thread.sleep(50);
        }
        assertTrue("Session search count: " + expected, ready[0]);
        test.waitForIdleSync();
    }

    private void verifyReadOnlyDetails(Instrumentation test,
            com.comp1786.room.features.sessions.ui.list.SessionActivity list) throws Exception {
        Instrumentation.ActivityMonitor monitor = test.addMonitor(
                com.comp1786.room.features.sessions.ui.details.SessionDetailsActivity.class.getName(), null, false);
        com.comp1786.room.features.sessions.ui.details.SessionDetailsActivity details = null;
        try {
            test.runOnMainSync(() -> ((androidx.recyclerview.widget.RecyclerView) list.findViewById(R.id.list))
                    .getChildAt(0).performClick());
            details = (com.comp1786.room.features.sessions.ui.details.SessionDetailsActivity)
                    test.waitForMonitorWithTimeout(monitor, 10000);
            assertNotNull("Card opens a read-only detail screen", details);
            final com.comp1786.room.features.sessions.ui.details.SessionDetailsActivity page = details;
            boolean[] loaded = {false};
            long deadline = android.os.SystemClock.uptimeMillis() + 10000;
            while (!loaded[0] && android.os.SystemClock.uptimeMillis() < deadline) {
                test.runOnMainSync(() -> loaded[0] = page.findViewById(R.id.edit).isEnabled());
                if (!loaded[0]) Thread.sleep(50);
            }
            assertTrue(loaded[0]);
            test.runOnMainSync(() -> {
                assertEquals("Guest", ((android.widget.TextView) page.findViewById(R.id.speaker)).getText().toString());
                assertEquals("Optional details", ((android.widget.TextView) page.findViewById(R.id.description)).getText().toString());
                assertFalse(page.findViewById(R.id.title) instanceof EditText);
                assertNull(page.findViewById(R.id.save));
            });
            screenshot(test, "13-session-details.png");
            Instrumentation.ActivityMonitor editMonitor = test.addMonitor(SessionEditorActivity.class.getName(), null, false);
            try {
                test.runOnMainSync(() -> page.findViewById(R.id.edit).performClick());
                SessionEditorActivity edit = (SessionEditorActivity) test.waitForMonitorWithTimeout(editMonitor, 10000);
                assertNotNull("Editing is a separate action", edit);
                awaitLoaded(test, edit);
                test.runOnMainSync(edit::finish);
            } finally { test.removeMonitor(editMonitor); }
        } finally {
            test.removeMonitor(monitor);
            if (details != null) {
                final android.app.Activity page = details;
                test.runOnMainSync(page::finish);
            }
        }
    }

    private SessionEditorActivity launch(Instrumentation test, Context context, long id) {
        return (SessionEditorActivity) test.startActivitySync(new Intent(context, SessionEditorActivity.class)
                .putExtra(SessionEditorActivity.EVENT_ID, eventId).putExtra(SessionEditorActivity.SESSION_ID, id).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private void fill(Instrumentation test, SessionEditorActivity activity, int field, String value) {
        test.runOnMainSync(() -> ((EditText) activity.findViewById(field)).setText(value));
    }

    private void awaitLoaded(Instrumentation test, SessionEditorActivity activity) throws Exception {
        long deadline = android.os.SystemClock.uptimeMillis() + 10000;
        boolean[] loaded = {false};
        while (!loaded[0] && android.os.SystemClock.uptimeMillis() < deadline) {
            test.runOnMainSync(() -> loaded[0] = activity.findViewById(R.id.save).isEnabled());
            if (!loaded[0]) Thread.sleep(50);
        }
        assertTrue("Existing session loaded", loaded[0]);
    }

    private void awaitFinished(Instrumentation test, SessionEditorActivity activity) throws Exception {
        long deadline = android.os.SystemClock.uptimeMillis() + 10000;
        while (!activity.isFinishing() && android.os.SystemClock.uptimeMillis() < deadline) {
            test.waitForIdleSync();
            Thread.sleep(50);
        }
        assertTrue("Database operation completed", activity.isFinishing());
    }

    private void clickPopup(Instrumentation test, String id) throws Exception {
        android.accessibilityservice.AccessibilityServiceInfo info = test.getUiAutomation().getServiceInfo();
        info.flags |= android.accessibilityservice.AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        test.getUiAutomation().setServiceInfo(info);
        test.getUiAutomation().waitForIdle(300, 5000);
        android.view.accessibility.AccessibilityNodeInfo root = test.getUiAutomation().getRootInActiveWindow();
        assertNotNull(root);
        java.util.List<android.view.accessibility.AccessibilityNodeInfo> nodes =
                root.findAccessibilityNodeInfosByViewId("com.comp1786.eventa.admin:id/" + id);
        assertFalse(nodes.isEmpty());
        assertTrue(nodes.get(0).performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK));
        test.waitForIdleSync();
    }

    private void screenshot(Instrumentation test, String name) throws Exception {
        test.getUiAutomation().waitForIdle(300, 5000);
        try (java.io.InputStream stream = new android.os.ParcelFileDescriptor.AutoCloseInputStream(
                test.getUiAutomation().executeShellCommand("mkdir -p /data/local/tmp/eventa-screenshots"))) {
            while (stream.read() != -1) { }
        }
        try (java.io.InputStream stream = new android.os.ParcelFileDescriptor.AutoCloseInputStream(
                test.getUiAutomation().executeShellCommand("screencap -p /data/local/tmp/eventa-screenshots/" + name))) {
            byte[] bytes = new byte[1024];
            while (stream.read(bytes) != -1) { }
        }
    }
}
