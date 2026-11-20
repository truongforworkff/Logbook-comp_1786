package com.comp1786.room.features.auth.data;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import com.comp1786.room.features.auth.ui.AdminLoginActivity;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public final class AdminAuth {
    private static String approvedUid;
    private AdminAuth() {}

    public static boolean hasSession() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null && user.getUid().equals(approvedUid);
    }

    public static Task<Boolean> authorize() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return com.google.android.gms.tasks.Tasks.forResult(false);
        String uid = user.getUid();
        return FirebaseFirestore.getInstance().document("roles/" + uid).get().continueWith(task -> {
            if (!task.isSuccessful()) throw task.getException();
            boolean allowed = "admin".equals(task.getResult().getString("role"));
            FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
            if (allowed && current != null && uid.equals(current.getUid())) approvedUid = uid;
            else approvedUid = null;
            return allowed && hasSession();
        });
    }

    public static void signOut() {
        approvedUid = null;
        FirebaseAuth.getInstance().signOut();
    }

    public static void openLogin(AppCompatActivity activity) {
        activity.startActivity(new Intent(activity, AdminLoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        activity.finish();
    }
}
