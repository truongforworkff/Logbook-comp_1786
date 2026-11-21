package com.comp1786.room.features.auth.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.comp1786.room.R;
import com.comp1786.room.databinding.AdminLoginScreenBinding;
import com.comp1786.room.databinding.SharedPopupBinding;
import com.comp1786.room.features.auth.data.AdminAuth;
import com.comp1786.room.features.cloud.logic.CloudSync;
import com.comp1786.room.features.events.ui.list.MainActivity;
import com.comp1786.room.shared.ui.AppUi;
import com.google.firebase.auth.FirebaseAuth;

public final class AdminLoginActivity extends AppCompatActivity {
    private AdminLoginScreenBinding binding;
    private AppUi ui;
    private boolean busy;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        binding = AdminLoginScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ui = new AppUi(this);
        ui.styleSystemBars(getWindow());
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(
                    androidx.core.view.WindowInsetsCompat.Type.systemBars() | androidx.core.view.WindowInsetsCompat.Type.ime());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        binding.signIn.setOnClickListener(view -> signIn());
        binding.password.setSaveEnabled(false);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) checkRole();
    }

    private void signIn() {
        String email = binding.email.getText().toString().trim();
        String password = binding.password.getText().toString();
        boolean emailValid = ui.inlineValidation(binding.email,
                android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches(), R.string.auth_invalid_email);
        boolean passwordValid = ui.inlineValidation(binding.password, !password.isEmpty(), R.string.required);
        if (busy || !emailValid || !passwordValid) return;
        setBusy(true);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) checkRole();
                    else showError(getString(R.string.auth_invalid_credentials));
                });
    }

    private void checkRole() {
        setBusy(true);
        AdminAuth.authorize().addOnCompleteListener(this, task -> {
            if (!task.isSuccessful()) {
                showError(getString(R.string.auth_connection_error));
            } else if (!Boolean.TRUE.equals(task.getResult())) {
                AdminAuth.signOut();
                showError(getString(R.string.auth_admin_required));
            } else {
                CloudSync.start(this);
                CloudSync.syncAfterLogin(this);
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });
    }

    private void setBusy(boolean value) {
        busy = value;
        binding.signIn.setEnabled(!value);
        binding.email.setEnabled(!value);
        binding.password.setEnabled(!value);
        binding.status.setText(value ? R.string.auth_checking : R.string.auth_admin_hint);
    }

    private void showError(String message) {
        setBusy(false);
        ui.showNotice(null, message, true);
    }

    public static void showAccount(AdminActivity activity) {
        AppUi ui = new AppUi(activity);
        SharedPopupBinding panel = SharedPopupBinding.inflate(activity.getLayoutInflater());
        panel.title.setText(R.string.auth_account);
        com.google.firebase.auth.FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        panel.subtitle.setText(user == null ? "" : user.getEmail());
        panel.primary.setText(R.string.auth_sign_out);
        panel.secondary.setText(R.string.cancel);
        androidx.appcompat.app.AlertDialog popup = ui.openPopup(panel);
        panel.secondary.setOnClickListener(view -> popup.dismiss());
        panel.primary.setOnClickListener(view -> {
            popup.dismiss();
            CloudSync.cancel(activity);
            AdminAuth.signOut();
            AdminAuth.openLogin(activity);
        });
    }

    @Override protected void onDestroy() {
        if (ui != null) ui.close();
        super.onDestroy();
    }
}
