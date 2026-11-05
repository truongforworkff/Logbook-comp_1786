package com.comp1786.room.features.sessions.ui.editor;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.comp1786.room.R;
import com.comp1786.room.databinding.SessionEditorScreenBinding;
import com.comp1786.room.databinding.SharedPopupBinding;
import com.comp1786.room.features.sessions.data.model.Session;
import com.comp1786.room.features.sessions.logic.SessionValidation;
import com.comp1786.room.features.sessions.logic.SessionViewModel;
import com.comp1786.room.shared.ui.AppUi;

public class SessionEditorActivity extends AppCompatActivity {
    public static final String EVENT_ID = "eventId";
    public static final String SESSION_ID = "sessionId";
    private SessionEditorScreenBinding binding;
    private SessionViewModel model;
    private AppUi ui;
    private long sessionId;
    private long eventId;
    private boolean loaded;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        binding = SessionEditorScreenBinding.inflate(getLayoutInflater());
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
        eventId = getIntent().getLongExtra(EVENT_ID, 0);
        if (eventId <= 0) { finish(); return; }
        sessionId = getIntent().getLongExtra(SESSION_ID, 0);
        model = new ViewModelProvider(this).get(SessionViewModel.class);
        setupForm();
        observeParent();
        restoreDraft(state);
        observeResult();
        loadExisting();
    }

    private void setupForm() {
        binding.title.setText(sessionId == 0 ? R.string.session_add : R.string.session_edit);
        binding.sessionId.setText(sessionId == 0 ? getString(R.string.session_new_id)
                : getString(R.string.session_id, sessionId));
        binding.typeInput.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, SessionValidation.TYPES));
        binding.back.setOnClickListener(view -> finish());
        binding.moreOptions.setOnClickListener(view -> toggleOptional());
        binding.save.setOnClickListener(view -> save());
        binding.delete.setVisibility(sessionId == 0 ? View.GONE : View.VISIBLE);
        binding.delete.setOnClickListener(view -> confirmDelete());
        loaded = sessionId == 0;
        setActionsEnabled(loaded && !model.isBusy());
    }

    private void toggleOptional() {
        boolean expanded = binding.optionalFields.getVisibility() != View.VISIBLE;
        showOptional(expanded);
    }

    private void showOptional(boolean expanded) {
        binding.optionalFields.setVisibility(expanded ? View.VISIBLE : View.GONE);
        binding.moreOptions.setText(expanded ? R.string.less_options : R.string.session_more);
    }

    private void observeParent() {
        model.getEvent(eventId).observe(this, event -> {
            if (event == null) {
                finish();
                return;
            }
            binding.eventName.setText(getString(R.string.session_parent, event.name));
        });
    }

    private void loadExisting() {
        if (loaded) return;
        model.getSession(sessionId).observe(this, session -> {
            if (session == null && model.isBusy()) return;
            if (session == null || (session.eventId != null && session.eventId != eventId)) {
                finish();
                return;
            }
            if (loaded) return;
            fillForm(session);
            loaded = true;
            setActionsEnabled(!model.isBusy());
        });
    }

    private void fillForm(Session session) {
        binding.nameInput.setText(session.name);
        binding.timeInput.setText(session.time);
        binding.speakerInput.setText(session.speaker);
        binding.roomInput.setText(session.room);
        binding.descriptionInput.setText(session.description);
        binding.typeInput.setSelection(Math.max(0, SessionValidation.TYPES.indexOf(session.type)));
        showOptional(!session.speaker.isEmpty() || !session.room.isEmpty() || !session.description.isEmpty());
    }

    private Session readForm() {
        return new Session(sessionId, eventId, binding.nameInput.getText().toString(),
                binding.timeInput.getText().toString(), binding.speakerInput.getText().toString(),
                binding.roomInput.getText().toString(), binding.typeInput.getSelectedItem().toString(),
                binding.descriptionInput.getText().toString());
    }

    private void save() {
        Session session = readForm();
        boolean nameValid = ui.inlineValidation(binding.nameInput,
                SessionValidation.validName(session.name), R.string.required);
        boolean timeValid = ui.inlineValidation(binding.timeInput,
                SessionValidation.validTime(session.time), R.string.session_invalid_time);
        if (!nameValid || !timeValid) {
            (nameValid ? binding.timeInput : binding.nameInput).requestFocus();
            return;
        }
        setActionsEnabled(false);
        model.save(session);
    }

    private void confirmDelete() {
        SharedPopupBinding panel = SharedPopupBinding.inflate(getLayoutInflater());
        panel.title.setText(R.string.session_delete_title);
        panel.subtitle.setText(R.string.session_delete_hint);
        ui.reviewRow(panel.body, R.string.session_name, binding.nameInput.getText().toString());
        panel.primary.setText(R.string.delete);
        panel.secondary.setText(R.string.session_keep);
        AlertDialog popup = ui.openPopup(panel);
        panel.secondary.setOnClickListener(view -> popup.dismiss());
        panel.primary.setOnClickListener(view -> {
            popup.dismiss();
            setActionsEnabled(false);
            model.delete(readForm());
        });
    }

    private void observeResult() {
        model.getResult().observe(this, result -> {
            if (result == SessionViewModel.Result.IDLE) return;
            if (result == SessionViewModel.Result.ERROR) {
                setActionsEnabled(loaded);
                ui.showNotice(null, getString(R.string.storage_error), true);
            } else {
                setResult(RESULT_OK, new android.content.Intent().putExtra("deleted",
                        result == SessionViewModel.Result.DELETED));
                finish();
            }
            model.consumeResult();
        });
    }

    private void setActionsEnabled(boolean enabled) {
        binding.save.setEnabled(enabled);
        binding.delete.setEnabled(enabled);
    }

    private void restoreDraft(Bundle state) {
        if (state == null || !state.getBoolean("loaded")) return;
        fillForm(new Session(sessionId, eventId, state.getString("name", ""), state.getString("time", ""),
                state.getString("speaker", ""), state.getString("room", ""),
                state.getString("type", "Workshop"), state.getString("description", "")));
        showOptional(state.getBoolean("optional"));
        loaded = true;
        setActionsEnabled(!model.isBusy());
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        Session draft = readForm();
        state.putBoolean("loaded", loaded);
        state.putString("name", draft.name);
        state.putString("time", draft.time);
        state.putString("speaker", draft.speaker);
        state.putString("room", draft.room);
        state.putString("type", draft.type);
        state.putString("description", draft.description);
        state.putBoolean("optional", binding.optionalFields.getVisibility() == View.VISIBLE);
        super.onSaveInstanceState(state);
    }

    @Override protected void onDestroy() {
        if (ui != null) ui.close();
        super.onDestroy();
    }
}
