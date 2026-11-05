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
        setupNavigation();
        observeSessions();
    }

    private void setupNavigation() {
        binding.eventsTab.setOnClickListener(view -> finish());
        binding.add.setOnClickListener(view -> openEditor(0));
    }

    private void observeSessions() {
        SessionAdapter adapter = new SessionAdapter(session -> openEditor(session.id));
        binding.list.setLayoutManager(new LinearLayoutManager(this));
        binding.list.setAdapter(adapter);
        SessionViewModel model = new ViewModelProvider(this).get(SessionViewModel.class);
        model.getSessions().observe(this, sessions -> {
            if (sessions == null) return;
            adapter.submitList(sessions);
            binding.count.setText(getString(R.string.session_count, sessions.size()));
            binding.empty.setVisibility(sessions.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void openEditor(long id) {
        editor.launch(new Intent(this, SessionEditorActivity.class).putExtra(SessionEditorActivity.SESSION_ID, id));
    }

    @Override protected void onDestroy() {
        if (ui != null) ui.close();
        super.onDestroy();
    }
}
