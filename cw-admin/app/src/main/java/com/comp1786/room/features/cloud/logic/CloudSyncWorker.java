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
            if (!CloudSync.hasInternet(getApplicationContext())) {
                database.sync().setError(revision, "Chờ kết nối Internet. Thay đổi vẫn được lưu trên thiết bị.");
                return Result.retry();
            }
            CloudPublisher.publish(FirebaseFirestore.getInstance(), snapshot,
                    snapshot.payload(getApplicationContext()), BuildConfig.CLOUD_WRITE_KEY);
            // Acknowledge only the captured revision; newer local edits stay pending.
            database.sync().acknowledge(snapshot.state.revision, System.currentTimeMillis());
            return Result.success();
        } catch (IllegalArgumentException invalid) {
            database.sync().setError(revision, invalid.getMessage());
            return Result.failure();
        } catch (Exception failure) {
            Throwable cause = failure instanceof ExecutionException ? failure.getCause() : failure;
            database.sync().setError(revision, message(cause));
            if (cause instanceof InterruptedException) Thread.currentThread().interrupt();
            return retryable(cause) ? Result.retry() : Result.failure();
        }
    }

    static boolean retryable(Throwable error) {
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
                return "Bản dữ liệu trên thiết bị cũ hơn Cloud. Khôi phục bản sao Admin mới nhất trước khi đồng bộ.";
            if ("SOURCE_MISMATCH".equals(problem.getMessage()))
                return "Cloud đang thuộc một bộ dữ liệu Admin khác. Khôi phục bản sao của bộ dữ liệu đó để tiếp tục.";
            if (!retryable(problem))
                return "Cấu hình Cloud chưa hợp lệ. Cập nhật bản Admin rồi đồng bộ lại.";
        }
        return "Chưa gửi được dữ liệu. App sẽ thử lại khi có Internet.";
    }
}
