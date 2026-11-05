package com.comp1786.room.features.sessions.ui.list;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.comp1786.room.R;
import com.comp1786.room.databinding.SessionListScreenBinding;
import com.comp1786.room.features.sessions.logic.SessionViewModel;
import com.comp1786.room.features.sessions.ui.editor.SessionEditorActivity;
import com.comp1786.room.shared.ui.AppUi;

public class SessionActivity extends AppCompatActivity {
    private SessionListScreenBinding binding;
    private AppUi ui;
    private long eventId;
    private boolean unassigned;
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
        ui.styleSystemBars(getWindow());
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(
                    androidx.core.view.WindowInsetsCompat.Type.systemBars()
                    | androidx.core.view.WindowInsetsCompat.Type.ime());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        eventId = getIntent().getLongExtra(SessionEditorActivity.EVENT_ID, 0);
        unassigned = getIntent().getBooleanExtra("unassigned", false);
        if (eventId <= 0) { finish(); return; }
        setupNavigation();
        observeSessions();
    }

    public static Intent intent(android.content.Context context, long eventId, boolean unassigned) {
        return new Intent(context, SessionActivity.class)
                .putExtra(SessionEditorActivity.EVENT_ID, eventId).putExtra("unassigned", unassigned);
    }

    private void setupNavigation() {
        binding.eventsTab.setOnClickListener(view -> finish());
        binding.add.setVisibility(unassigned ? View.GONE : View.VISIBLE);
        binding.assignLegacy.setOnClickListener(view -> startActivity(intent(this, eventId, true)));
        binding.add.setOnClickListener(view -> openEditor(0));
    }

    private void observeSessions() {
        SessionAdapter adapter = new SessionAdapter(session -> openEditor(session.id));
        binding.list.setLayoutManager(new LinearLayoutManager(this));
        binding.list.setAdapter(adapter);
        SessionViewModel model = new ViewModelProvider(this).get(SessionViewModel.class);
        observeParent(model);
        observeLegacySessions(model);
        model.getSessions(eventId, unassigned).observe(this, sessions -> {
            if (sessions == null) return;
            adapter.submitList(sessions);
            binding.count.setText(getString(R.string.session_count, sessions.size()));
            binding.empty.setVisibility(sessions.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void observeParent(SessionViewModel model) {
        model.getEvent(eventId).observe(this, event -> {
            if (event == null) { finish(); return; }
            binding.eventName.setText(getString(R.string.session_parent, event.name));
        });
    }

    private void observeLegacySessions(SessionViewModel model) {
        model.getUnassignedCount().observe(this, count ->
                binding.assignLegacy.setVisibility(!unassigned && count != null && count > 0 ? View.VISIBLE : View.GONE));
        if (unassigned) {
            binding.listTitle.setText(R.string.session_assign_title);
            binding.empty.setText(R.string.session_assign_hint);
        }
    }

    private void openEditor(long id) {
        editor.launch(new Intent(this, SessionEditorActivity.class).putExtra(SessionEditorActivity.SESSION_ID, id).putExtra(SessionEditorActivity.EVENT_ID, eventId));
    }

    @Override protected void onDestroy() {
        if (ui != null) ui.close();
        super.onDestroy();
    }
}
