package com.comp1786.room.features.cloud.logic;

import android.content.Context;
import android.net.*;
import androidx.annotation.NonNull;
import androidx.room.InvalidationTracker;
import androidx.work.*;
import com.comp1786.room.features.events.data.local.EventDatabase;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class CloudSync {
    public static final String IMMEDIATE = "eventa-cloud-now";
    private static final String PERIODIC = "eventa-cloud-recovery";
    private static boolean started;
    private static com.google.firebase.firestore.ListenerRegistration roleChanges;
    private CloudSync() {}

    public static synchronized void start(Context context) {
        if (started) return;
        started = true;
        Context app = context.getApplicationContext();
        EventDatabase.get(app).getInvalidationTracker().addObserver(
                new InvalidationTracker.Observer("events", "sessions") {
                    @Override public void onInvalidated(@NonNull Set<String> tables) { enqueue(app); }
                });
        FirebaseAuth.getInstance().addAuthStateListener(auth -> {
            WorkManager manager = WorkManager.getInstance(app);
            if (roleChanges != null) roleChanges.remove();
            if (auth.getCurrentUser() == null) {
                manager.cancelUniqueWork(IMMEDIATE);
                manager.cancelUniqueWork(PERIODIC);
            } else {
                PeriodicWorkRequest recovery = new PeriodicWorkRequest.Builder(
                        CloudSyncWorker.class, 15, TimeUnit.MINUTES)
                        .setConstraints(network()).build();
                manager.enqueueUniquePeriodicWork(PERIODIC, ExistingPeriodicWorkPolicy.KEEP, recovery);
                enqueue(app);
                roleChanges = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .document("admins/" + auth.getCurrentUser().getUid())
                        .addSnapshotListener((role, error) -> {
                            if (role != null && Boolean.TRUE.equals(role.getBoolean("enabled"))) enqueue(app);
                        });
            }
        });
    }

    public static void enqueue(Context context) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(CloudSyncWorker.class)
                .setConstraints(network()).setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS).build();
        // Append so an edit arriving just as a worker finishes is never dropped.
        WorkManager.getInstance(context).enqueueUniqueWork(
                IMMEDIATE, ExistingWorkPolicy.APPEND_OR_REPLACE, work);
    }

    private static Constraints network() {
        return new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
    }

    public static boolean hasInternet(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities connection = manager.getNetworkCapabilities(manager.getActiveNetwork());
        return connection != null && connection.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }
}
