package com.comp1786.room.features.cloud.logic;

import android.content.Context;
import com.comp1786.room.BuildConfig;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.comp1786.room.features.events.data.local.EventDatabase;
import com.comp1786.room.features.cloud.data.*;
import com.google.firebase.firestore.*;
import java.util.concurrent.ExecutionException;

public final class CloudSyncWorker extends Worker {
    public CloudSyncWorker(@NonNull Context context, @NonNull WorkerParameters parameters) {
        super(context, parameters);
    }

    @NonNull @Override public Result doWork() {
        EventDatabase database = EventDatabase.get(getApplicationContext());
        long revision = -1;
        try {
            CloudSnapshot snapshot = CloudSnapshot.read(database);
            revision = snapshot.state.revision;
            if (!snapshot.state.pending()) return Result.success();
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            if (snapshot.state.isNew() && snapshot.events.isEmpty() && snapshot.sessions.isEmpty()) {
                CloudDownload.restore(getApplicationContext(), database, firestore);
                return Result.success();
            }
            if (BuildConfig.CLOUD_WRITE_KEY.isEmpty()) {
                throw new IllegalArgumentException("The Firebase write key is missing. Configure sync.properties and rebuild the admin app.");
            }
            CloudPublisher.publish(firestore, snapshot,
                    snapshot.payload(getApplicationContext()), BuildConfig.CLOUD_WRITE_KEY);
            // Acknowledge only the captured revision; newer local edits stay pending.
            database.sync().acknowledge(snapshot.state.revision, System.currentTimeMillis());
            return Result.success();
        } catch (IllegalArgumentException invalid) {
            database.sync().setError(revision, invalid.getMessage());
            return Result.success();
        } catch (Exception failure) {
            Throwable cause = failure instanceof ExecutionException ? failure.getCause() : failure;
            database.sync().setError(revision, message(cause));
            if (cause instanceof InterruptedException) Thread.currentThread().interrupt();
            // An actionable error must not cancel queued work for later local edits.
            return retryable(cause) ? Result.retry() : Result.success();
        }
    }

    static boolean retryable(Throwable error) {
        if (error instanceof IllegalStateException) return false;
        if (!(error instanceof FirebaseFirestoreException)) return true;
        FirebaseFirestoreException.Code code = ((FirebaseFirestoreException) error).getCode();
        return code != FirebaseFirestoreException.Code.PERMISSION_DENIED
                && code != FirebaseFirestoreException.Code.FAILED_PRECONDITION
                && code != FirebaseFirestoreException.Code.INVALID_ARGUMENT
                && code != FirebaseFirestoreException.Code.UNAUTHENTICATED;
    }

    static String message(Throwable error) {
        if (error instanceof FirebaseFirestoreException) {
            FirebaseFirestoreException problem = (FirebaseFirestoreException) error;
            if (problem.getCode() == FirebaseFirestoreException.Code.PERMISSION_DENIED)
                return "Could not upload changes. Update the cloud configuration for this admin app.";
            if ("LOCAL_BEHIND".equals(problem.getMessage()))
                return "Firebase has a different revision. Your local changes have been kept for review.";
            if ("SOURCE_MISMATCH".equals(problem.getMessage()))
                return "Firebase contains a different dataset. Use the device that downloaded or published that catalogue.";
            if (!retryable(problem))
                return "Cloud configuration is invalid. Update the admin app and sync again.";
        }
        return "Could not upload changes. The app will retry when you are online.";
    }
}
