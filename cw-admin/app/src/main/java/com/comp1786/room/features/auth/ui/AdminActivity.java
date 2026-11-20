package com.comp1786.room.features.auth.ui;

import androidx.appcompat.app.AppCompatActivity;
import com.comp1786.room.features.auth.data.AdminAuth;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public abstract class AdminActivity extends AppCompatActivity {
    private ListenerRegistration roleListener;
    private FirebaseAuth.AuthStateListener authListener;

    protected boolean requireAdmin() {
        if (AdminAuth.hasSession()) return true;
        AdminAuth.openLogin(this);
        return false;
    }

    @Override protected void onStart() {
        super.onStart();
        if (isFinishing() || !requireAdmin()) return;
        authListener = auth -> { if (!AdminAuth.hasSession()) AdminAuth.openLogin(this); };
        FirebaseAuth.getInstance().addAuthStateListener(authListener);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        roleListener = FirebaseFirestore.getInstance().document("roles/" + uid)
                .addSnapshotListener(this, (role, error) -> {
                    if (error != null || role == null || !"admin".equals(role.getString("role"))) {
                        AdminAuth.signOut();
                        AdminAuth.openLogin(this);
                    }
                });
    }

    @Override protected void onStop() {
        if (roleListener != null) roleListener.remove();
        if (authListener != null) FirebaseAuth.getInstance().removeAuthStateListener(authListener);
        super.onStop();
    }
}
