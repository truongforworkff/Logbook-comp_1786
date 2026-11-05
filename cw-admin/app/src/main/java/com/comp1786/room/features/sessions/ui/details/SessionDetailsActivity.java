package com.comp1786.room.features.sessions.ui.details;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import com.comp1786.room.R;
import com.comp1786.room.databinding.SessionDetailsScreenBinding;
import com.comp1786.room.features.events.logic.EventValidation;
import com.comp1786.room.features.sessions.data.model.Session;
import com.comp1786.room.features.sessions.logic.SessionViewModel;
import com.comp1786.room.features.sessions.ui.editor.SessionEditorActivity;
import com.comp1786.room.shared.ui.AppUi;

public class SessionDetailsActivity extends AppCompatActivity {
    private SessionDetailsScreenBinding binding;
    private AppUi ui;
    private long eventId;
    private long sessionId;
    private final ActivityResultLauncher<Intent> editor = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != RESULT_OK) return;
                setResult(RESULT_OK, result.getData());
                if (result.getData() != null && result.getData().getBooleanExtra("deleted", false)) finish();
            });

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        binding = SessionDetailsScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ui = new AppUi(this);
        setupWindow();
        eventId = getIntent().getLongExtra(SessionEditorActivity.EVENT_ID, 0);
        sessionId = getIntent().getLongExtra(SessionEditorActivity.SESSION_ID, 0);
        if (eventId <= 0 || sessionId <= 0) { finish(); return; }
        setupNavigation();
        observeDetails();
    }

    private void setupWindow() {
        ui.styleSystemBars(getWindow());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    private void setupNavigation() {
        binding.back.setOnClickListener(view -> finish());
        binding.edit.setEnabled(false);
        binding.edit.setOnClickListener(view -> editor.launch(new Intent(this, SessionEditorActivity.class)
                .putExtra(SessionEditorActivity.EVENT_ID, eventId)
                .putExtra(SessionEditorActivity.SESSION_ID, sessionId)));
    }

    private void observeDetails() {
        SessionViewModel model = new ViewModelProvider(this).get(SessionViewModel.class);
        model.getEvent(eventId).observe(this, event -> {
            if (event == null) { finish(); return; }
            binding.eventName.setText(getString(R.string.session_parent, event.name));
            binding.date.setText(EventValidation.displayDate(event.date));
        });
        model.getSession(sessionId).observe(this, session -> {
            if (session == null || session.eventId == null || session.eventId != eventId) {
                finish();
                return;
            }
            renderSession(session);
        });
    }

    private void renderSession(Session session) {
        binding.title.setText(session.name);
        binding.type.setText(session.type);
        binding.time.setText(session.time);
        binding.speaker.setText(valueOrPlaceholder(session.speaker));
        binding.room.setText(valueOrPlaceholder(session.room));
        binding.description.setText(session.description.isEmpty()
                ? getString(R.string.session_no_description) : session.description);
        binding.edit.setEnabled(true);
    }

    private String valueOrPlaceholder(String value) {
        return value.isEmpty() ? getString(R.string.not_provided) : value;
    }

    @Override protected void onDestroy() {
        if (ui != null) ui.close();
        super.onDestroy();
    }
}
