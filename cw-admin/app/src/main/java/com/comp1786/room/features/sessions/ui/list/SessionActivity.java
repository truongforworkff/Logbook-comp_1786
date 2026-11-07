package com.comp1786.room.features.sessions.ui.list;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.comp1786.room.R;
import com.comp1786.room.databinding.SessionListScreenBinding;
import com.comp1786.room.features.sessions.data.model.Session;
import com.comp1786.room.features.sessions.logic.SessionViewModel;
import com.comp1786.room.features.sessions.ui.editor.SessionEditorActivity;
import com.comp1786.room.shared.ui.AppUi;
import java.util.Collections;
import java.util.List;

public class SessionActivity extends AppCompatActivity {
    private SessionListScreenBinding binding;
    private AppUi ui;
    private SessionViewModel model;
    private SessionAdapter adapter;
    private LiveData<List<Session>> sessionsSource;
    private List<Session> allSessions = Collections.emptyList();
    private long eventId;
    private boolean global;
    private boolean unassigned;
    private int legacyCount;
    private final androidx.activity.result.ActivityResultLauncher<Intent> editor =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    boolean deleted = result.getData() != null && result.getData().getBooleanExtra("deleted", false);
                    ui.showNotice(null, getString(deleted ? R.string.session_deleted : R.string.session_saved), false);
                }
            });

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        binding = SessionListScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ui = new AppUi(this);
        setupWindow();
        long requestedEvent = getIntent().getLongExtra(SessionEditorActivity.EVENT_ID, 0);
        global = requestedEvent <= 0;
        eventId = state == null ? requestedEvent : state.getLong("selectedEvent", requestedEvent);
        unassigned = !global && getIntent().getBooleanExtra("unassigned", false);
        model = new ViewModelProvider(this).get(SessionViewModel.class);
        setupList();
        setupNavigation();
        setupSearch();
        observeLegacySessions();
        if (global) setupEventPicker();
        else {
            observeParent();
            selectEvent(eventId);
        }
    }

    private void setupWindow() {
        ui.styleSystemBars(getWindow());
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(
                    androidx.core.view.WindowInsetsCompat.Type.systemBars()
                    | androidx.core.view.WindowInsetsCompat.Type.ime());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    public static Intent intent(android.content.Context context, long eventId, boolean unassigned) {
        return new Intent(context, SessionActivity.class)
                .putExtra(SessionEditorActivity.EVENT_ID, eventId).putExtra("unassigned", unassigned);
    }

    private void setupNavigation() {
        binding.cloudTab.setOnClickListener(view -> startActivity(new android.content.Intent(this,
                com.comp1786.room.features.cloud.ui.CloudActivity.class)));
        binding.eventsTab.setOnClickListener(view -> finish());
        binding.sessionsTab.setOnClickListener(view -> {
            if (!global) startActivity(new Intent(this, SessionActivity.class));
        });
        binding.add.setVisibility(unassigned ? View.GONE : View.VISIBLE);
        binding.add.setEnabled(eventId > 0);
        binding.assignLegacy.setOnClickListener(view -> startActivity(intent(this, eventId, true)));
        binding.add.setOnClickListener(view -> openEditor(0));
    }

    private void setupSearch() {
        binding.sessionSearch.query.setEnabled(eventId > 0);
        com.comp1786.room.shared.ui.SessionSearchField.bind(binding.sessionSearch, query -> applySearch());
    }

    private void setupList() {
        adapter = new SessionAdapter(session -> {
            if (unassigned) openEditor(session.id);
            else openDetails(session.id);
        });
        binding.list.setLayoutManager(new LinearLayoutManager(this));
        binding.list.setAdapter(adapter);
    }

    private void setupEventPicker() {
        binding.eventPickerGroup.setVisibility(View.VISIBLE);
        binding.eventName.setText(R.string.session_choose_event_hint);
        SessionEventPicker picker = new SessionEventPicker(binding.eventPicker, this::selectEvent);
        model.getEvents().observe(this, events -> {
            if (events == null) return;
            binding.eventName.setText(events.isEmpty() ? R.string.session_no_events : R.string.session_choose_event_hint);
            picker.submit(events, eventId);
        });
    }

    private void selectEvent(long id) {
        if (id == eventId && sessionsSource != null) return;
        if (sessionsSource != null) sessionsSource.removeObservers(this);
        eventId = id;
        binding.add.setEnabled(id > 0);
        binding.sessionSearch.query.setEnabled(id > 0);
        allSessions = Collections.emptyList();
        updateLegacyAction();
        adapter.submitList(Collections.emptyList());
        binding.count.setText(getString(R.string.session_count, 0));
        binding.empty.setText(id == 0 ? R.string.session_choose_event_hint
                : unassigned ? R.string.session_assign_hint : R.string.session_empty);
        binding.empty.setVisibility(View.VISIBLE);
        if (id == 0) { sessionsSource = null; return; }
        sessionsSource = model.getSessions(id, unassigned);
        sessionsSource.observe(this, this::renderSessions);
    }

    private void renderSessions(List<Session> sessions) {
        if (sessions == null) return;
        allSessions = sessions;
        applySearch();
    }

    private void applySearch() {
        if (eventId <= 0) return;
        String query = binding.sessionSearch.query.getText().toString();
        List<Session> shown = com.comp1786.room.features.sessions.logic.SessionSearch.filter(allSessions, query);
        boolean searching = !query.trim().isEmpty();
        adapter.submitList(shown);
        binding.count.setText(searching
                ? getString(R.string.session_search_count, shown.size(), allSessions.size())
                : getString(R.string.session_count, shown.size()));
        binding.empty.setText(searching ? R.string.session_search_empty
                : unassigned ? R.string.session_assign_hint : R.string.session_empty);
        binding.empty.setVisibility(shown.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void observeParent() {
        model.getEvent(eventId).observe(this, event -> {
            if (event == null) { finish(); return; }
            binding.eventName.setText(getString(R.string.session_parent, event.name));
        });
    }

    private void observeLegacySessions() {
        model.getUnassignedCount().observe(this, count -> {
            legacyCount = count == null ? 0 : count;
            updateLegacyAction();
        });
        if (unassigned) binding.listTitle.setText(R.string.session_assign_title);
    }

    private void updateLegacyAction() {
        binding.assignLegacy.setVisibility(!unassigned && eventId > 0 && legacyCount > 0 ? View.VISIBLE : View.GONE);
    }

    private void openDetails(long id) {
        editor.launch(new Intent(this, com.comp1786.room.features.sessions.ui.details.SessionDetailsActivity.class)
                .putExtra(SessionEditorActivity.SESSION_ID, id)
                .putExtra(SessionEditorActivity.EVENT_ID, eventId));
    }

    private void openEditor(long id) {
        if (eventId <= 0) return;
        editor.launch(new Intent(this, SessionEditorActivity.class)
                .putExtra(SessionEditorActivity.SESSION_ID, id)
                .putExtra(SessionEditorActivity.EVENT_ID, eventId));
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        state.putLong("selectedEvent", eventId);
        super.onSaveInstanceState(state);
    }

    @Override protected void onDestroy() {
        if (ui != null) ui.close();
        super.onDestroy();
    }
}
