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

    public EventSchedule(AppCompatActivity activity, long eventId) {
        this.activity = activity;
        this.eventId = eventId;
    }

    public void bind(EventDetailsScreenBinding page, AlertDialog dialog) {
        page.addSessionAction.setOnClickListener(view -> openEditor());
        page.legacySessions.setOnClickListener(view ->
                activity.startActivity(SessionActivity.intent(activity, eventId, true)));
        SessionDao dao = EventDatabase.get(activity).sessions();
        LiveData<List<Session>> sessions = dao.observeForEvent(eventId);
        Observer<List<Session>> scheduleObserver = rows -> {
            if (rows != null) render(page, rows);
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

    private void render(EventDetailsScreenBinding page, List<Session> sessions) {
        page.sessionCount.setText(activity.getString(R.string.session_count, sessions.size()));
        page.scheduleEmpty.setVisibility(sessions.isEmpty() ? View.VISIBLE : View.GONE);
        page.scheduleItems.removeAllViews();
        for (int index = 0; index < sessions.size(); index++) {
            Session session = sessions.get(index);
            EventScheduleItemBinding row = EventScheduleItemBinding.inflate(
                    activity.getLayoutInflater(), page.scheduleItems, false);
            bindRow(row, session, index == sessions.size() - 1);
            page.scheduleItems.addView(row.getRoot());
        }
    }

    private void bindRow(EventScheduleItemBinding row, Session session, boolean last) {
        row.time.setText(session.time);
        row.name.setText(session.name);
        row.meta.setText(session.room.isEmpty() ? session.type
                : activity.getString(R.string.session_type_room, session.type, session.room));
        row.speaker.setVisibility(session.speaker.isEmpty() ? View.GONE : View.VISIBLE);
        row.speaker.setText(activity.getString(R.string.session_speaker_value, session.speaker));
        row.connector.setVisibility(last ? View.INVISIBLE : View.VISIBLE);
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
