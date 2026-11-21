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
                throw new IllegalArgumentException("Bản Admin chưa có khóa ghi Firebase. Cấu hình sync.properties rồi build lại.");
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
                return "Chưa gửi được dữ liệu. Cấu hình kết nối Cloud của bản Admin cần được cập nhật.";
            if ("LOCAL_BEHIND".equals(problem.getMessage()))
                return "Firebase có phiên bản khác. Chỉnh sửa trên máy vẫn được giữ để xử lý xung đột.";
            if ("SOURCE_MISMATCH".equals(problem.getMessage()))
                return "Firebase thuộc một bộ dữ liệu khác. Hãy dùng máy đã tải danh mục từ Cloud hoặc máy đã công bố trước đó.";
            if (!retryable(problem))
                return "Cấu hình Cloud chưa hợp lệ. Cập nhật bản Admin rồi đồng bộ lại.";
        }
        return "Chưa gửi được dữ liệu. App sẽ thử lại khi có Internet.";
    }
}
