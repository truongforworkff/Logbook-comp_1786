package com.comp1786.room.features.cloud.logic;

import android.content.Context;
import android.net.*;
import androidx.annotation.NonNull;
import androidx.room.InvalidationTracker;
import androidx.work.*;
import com.comp1786.room.features.events.data.local.EventDatabase;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class CloudSync {
    public static final String IMMEDIATE = "eventa-cloud-now";
    private static final String PERIODIC = "eventa-cloud-recovery";
    private static boolean started;
    private CloudSync() {}
    private static boolean signedIn() {
        return com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public static synchronized void start(Context context) {
        if (started) return;
        started = true;
        Context app = context.getApplicationContext();
        EventDatabase.get(app).getInvalidationTracker().addObserver(
                new InvalidationTracker.Observer("events", "sessions") {
                    @Override public void onInvalidated(@NonNull Set<String> tables) { enqueue(app); }
                });
        PeriodicWorkRequest recovery = new PeriodicWorkRequest.Builder(
                CloudSyncWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(network()).build();
        WorkManager.getInstance(app).enqueueUniquePeriodicWork(
                PERIODIC, ExistingPeriodicWorkPolicy.KEEP, recovery);
        enqueue(app);
    }

    public static void enqueue(Context context) {
        if (!signedIn()) return;
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(CloudSyncWorker.class)
                .setConstraints(network()).setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS).build();
        // Append so an edit arriving just as a worker finishes is never dropped.
        WorkManager.getInstance(context).enqueueUniqueWork(
                IMMEDIATE, ExistingWorkPolicy.APPEND_OR_REPLACE, work);
    }

    public static void uploadAfterLogin(Context context) {
        java.util.concurrent.ExecutorService worker = java.util.concurrent.Executors.newSingleThreadExecutor();
        worker.execute(() -> {
            EventDatabase.get(context).sync().requestUpload();
            enqueue(context);
        });
        worker.shutdown();
    }

    public static void cancel(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(IMMEDIATE);
        WorkManager.getInstance(context).cancelUniqueWork(PERIODIC);
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
