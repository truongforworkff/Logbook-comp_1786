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
    private boolean creatingAccount;

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
        creatingAccount = saved != null && saved.getBoolean("creatingAccount");
        setupActions();
        observeState();
    }

    private void setupActions() {
        binding.back.setOnClickListener(view -> finish());
        binding.login.setOnClickListener(view -> {
            String password = binding.password.getText().toString();
            if (creatingAccount && !password.equals(binding.confirmPassword.getText().toString())) {
                ui.showNotice(null, getString(R.string.cloud_password_mismatch), true);
                return;
            }
            String email = binding.email.getText().toString().trim();
            if (creatingAccount) model.createAccount(email, password);
            else model.signIn(email, password);
            binding.password.setText("");
            binding.confirmPassword.setText("");
        });
        binding.accountMode.setOnClickListener(view -> {
            creatingAccount = !creatingAccount;
            binding.password.setText("");
            binding.confirmPassword.setText("");
            render();
        });
        binding.upload.setOnClickListener(view -> model.uploadAll());
        binding.logout.setOnClickListener(view -> model.signOut());
    }

    private void observeState() {
        model.user().observe(this, value -> render());
        model.signingIn().observe(this, value -> render());
        model.error().observe(this, value -> {
            binding.loginError.setText(value);
            binding.loginError.setVisibility(value == null || value.isEmpty() ? View.GONE : View.VISIBLE);
        });
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
        boolean signedIn = model.user().getValue() != null;
        boolean signingIn = Boolean.TRUE.equals(model.signingIn().getValue());
        binding.loginGroup.setVisibility(signedIn ? View.GONE : View.VISIBLE);
        binding.accountGroup.setVisibility(signedIn ? View.VISIBLE : View.GONE);
        binding.login.setEnabled(!signingIn);
        binding.accountMode.setEnabled(!signingIn);
        binding.confirmGroup.setVisibility(creatingAccount ? View.VISIBLE : View.GONE);
        binding.login.setText(creatingAccount ? R.string.cloud_create : R.string.cloud_login);
        binding.loginNote.setText(creatingAccount ? R.string.cloud_create_note : R.string.cloud_login_note);
        binding.accountMode.setText(creatingAccount ? R.string.cloud_login_link : R.string.cloud_create_link);
        binding.progress.setVisibility(signingIn || syncing ? View.VISIBLE : View.GONE);
        binding.upload.setEnabled(signedIn && !syncing);
        if (signedIn) binding.accountEmail.setText(model.user().getValue().getEmail());
        int label = R.string.cloud_loading;
        if (!signedIn) label = R.string.cloud_not_signed_in;
        else if (state != null) {
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

    @Override protected void onSaveInstanceState(Bundle saved) {
        saved.putBoolean("creatingAccount", creatingAccount);
        super.onSaveInstanceState(saved);
    }

    @Override protected void onResume() {
        super.onResume();
        if (model != null) render();
    }
    @Override protected void onDestroy() { if (ui != null) ui.close(); super.onDestroy(); }
}
