package com.comp1786.room.features.cloud.ui;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.WorkInfo;
import com.comp1786.room.R;
import com.comp1786.room.databinding.CloudScreenBinding;
import com.comp1786.room.features.cloud.data.SyncState;
import com.comp1786.room.features.cloud.logic.CloudSync;
import com.comp1786.room.features.cloud.logic.CloudViewModel;
import com.comp1786.room.shared.ui.AppUi;
import java.util.List;

public final class CloudActivity extends AppCompatActivity {
    private CloudScreenBinding binding;
    private CloudViewModel model;
    private AppUi ui;
    private SyncState state;
    private boolean syncing;

    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        binding = CloudScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ui = new AppUi(this);
        ui.styleSystemBars(getWindow());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        model = new ViewModelProvider(this).get(CloudViewModel.class);
        setupActions();
        observeState();
    }

    private void setupActions() {
        binding.back.setOnClickListener(view -> finish());
        binding.upload.setOnClickListener(view -> model.uploadAll());
    }

    private void observeState() {
        model.state().observe(this, value -> { state = value; render(); });
        model.work().observe(this, this::renderWork);
    }

    private void renderWork(List<WorkInfo> work) {
        syncing = false;
        if (work != null) for (WorkInfo info : work) {
            if (!info.getState().isFinished()) syncing = true;
        }
        render();
    }

    private void render() {
        binding.progress.setVisibility(syncing ? View.VISIBLE : View.GONE);
        binding.upload.setEnabled(!syncing);
        int label = R.string.cloud_loading;
        if (state != null) {
            if (state.pending()) label = !CloudSync.hasInternet(this) ? R.string.cloud_offline
                    : syncing ? R.string.cloud_running : R.string.cloud_waiting;
            else label = R.string.cloud_done;
        }
        binding.status.setText(label);
        binding.lastSync.setText(state == null || state.syncedAt == 0 ? getString(R.string.cloud_never)
                : getString(R.string.cloud_last, android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", state.syncedAt)));
        String error = state == null ? "" : state.error;
        binding.syncError.setText(error);
        binding.syncError.setVisibility(error.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override protected void onResume() {
        super.onResume();
        if (model != null) render();
    }
    @Override protected void onDestroy() { if (ui != null) ui.close(); super.onDestroy(); }
}
