package com.comp1786.room.features.events.ui.details;

import android.content.Intent;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import com.comp1786.room.R;
import com.comp1786.room.databinding.EventDetailsScreenBinding;
import com.comp1786.room.databinding.EventScheduleItemBinding;
import com.comp1786.room.features.events.data.local.EventDatabase;
import com.comp1786.room.features.sessions.data.local.SessionDao;
import com.comp1786.room.features.sessions.data.model.Session;
import com.comp1786.room.features.sessions.ui.details.SessionDetailsActivity;
import com.comp1786.room.features.sessions.ui.editor.SessionEditorActivity;
import com.comp1786.room.features.sessions.ui.list.SessionActivity;
import java.util.List;

public final class EventSchedule {
    private final AppCompatActivity activity;
    private final long eventId;
    private List<Session> allSessions = java.util.Collections.emptyList();

    public EventSchedule(AppCompatActivity activity, long eventId) {
        this.activity = activity;
        this.eventId = eventId;
    }

    public void bind(EventDetailsScreenBinding page, AlertDialog dialog) {
        com.comp1786.room.shared.ui.SessionSearchField.bind(page.sessionSearch, query -> render(page));
        page.addSessionAction.setOnClickListener(view -> openEditor());
        page.legacySessions.setOnClickListener(view ->
                activity.startActivity(SessionActivity.intent(activity, eventId, true)));
        SessionDao dao = EventDatabase.get(activity).sessions();
        LiveData<List<Session>> sessions = dao.observeForEvent(eventId);
        Observer<List<Session>> scheduleObserver = rows -> {
            if (rows == null) return;
            allSessions = rows;
            page.sessionSearch.getRoot().setVisibility(rows.isEmpty() ? View.GONE : View.VISIBLE);
            render(page);
        };
        LiveData<Integer> unassigned = dao.observeUnassignedCount();
        Observer<Integer> legacyObserver = count ->
                page.legacySessions.setVisibility(count != null && count > 0 ? View.VISIBLE : View.GONE);
        sessions.observe(activity, scheduleObserver);
        unassigned.observe(activity, legacyObserver);
        dialog.setOnDismissListener(ignored -> {
            sessions.removeObserver(scheduleObserver);
            unassigned.removeObserver(legacyObserver);
        });
    }

    private void render(EventDetailsScreenBinding page) {
        String query = page.sessionSearch.query.getText().toString();
        List<Session> sessions = com.comp1786.room.features.sessions.logic.SessionSearch.filter(allSessions, query);
        boolean searching = !query.trim().isEmpty();
        page.sessionCount.setText(searching
                ? activity.getString(R.string.session_search_count, sessions.size(), allSessions.size())
                : activity.getString(R.string.session_count, allSessions.size()));
        page.scheduleEmpty.setText(searching ? R.string.session_search_empty : R.string.event_schedule_empty);
        page.scheduleEmpty.setVisibility(sessions.isEmpty() ? View.VISIBLE : View.GONE);
        page.scheduleItems.removeAllViews();
        for (Session session : sessions) {
            EventScheduleItemBinding row = EventScheduleItemBinding.inflate(
                    activity.getLayoutInflater(), page.scheduleItems, false);
            bindRow(row, session);
            page.scheduleItems.addView(row.getRoot());
        }
    }

    private void bindRow(EventScheduleItemBinding row, Session session) {
        row.time.setText(session.time);
        row.name.setText(session.name);
        String detail = session.room.isEmpty() ? session.speaker : session.room;
        row.meta.setText(detail.isEmpty() ? session.type
                : activity.getString(R.string.session_type_room, session.type, detail));
        boolean showSpeaker = !session.speaker.isEmpty() && !session.room.isEmpty();
        row.speaker.setVisibility(showSpeaker ? View.VISIBLE : View.GONE);
        row.speaker.setText(activity.getString(R.string.session_speaker_value, session.speaker));
        row.getRoot().setOnClickListener(view -> openDetails(session.id));
    }

    private void openDetails(long sessionId) {
        activity.startActivity(new Intent(activity, SessionDetailsActivity.class)
                .putExtra(SessionEditorActivity.EVENT_ID, eventId)
                .putExtra(SessionEditorActivity.SESSION_ID, sessionId));
    }

    private void openEditor() {
        activity.startActivity(new Intent(activity, SessionEditorActivity.class)
                .putExtra(SessionEditorActivity.EVENT_ID, eventId));
    }
}
