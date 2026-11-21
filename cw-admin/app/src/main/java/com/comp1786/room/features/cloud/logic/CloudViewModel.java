package com.comp1786.room.features.cloud.logic;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import com.comp1786.room.features.cloud.data.SyncState;
import com.comp1786.room.features.cloud.data.CloudDownload;
import com.google.firebase.firestore.FirebaseFirestore;
import com.comp1786.room.features.events.data.local.EventDatabase;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class CloudViewModel extends AndroidViewModel {
    private final ExecutorService databaseWorker = Executors.newSingleThreadExecutor();

    private final MutableLiveData<Boolean> downloading = new MutableLiveData<>(false);
    private final MutableLiveData<String> downloadMessage = new MutableLiveData<>("");

    public CloudViewModel(Application application) { super(application); }

    public LiveData<SyncState> state() { return EventDatabase.get(getApplication()).sync().observe(); }
    public LiveData<List<WorkInfo>> work() {
        return WorkManager.getInstance(getApplication()).getWorkInfosForUniqueWorkLiveData(CloudSync.IMMEDIATE);
    }

    public void uploadAll() {
        downloadMessage.setValue("");
        CloudSync.enqueue(getApplication());
    }

    public LiveData<Boolean> downloading() { return downloading; }
    public LiveData<String> downloadMessage() { return downloadMessage; }

    public void download() {
        if (Boolean.TRUE.equals(downloading.getValue())) return;
        downloading.setValue(true);
        downloadMessage.setValue("");
        databaseWorker.execute(() -> {
            String message;
            try {
                boolean found = CloudDownload.restore(getApplication(), EventDatabase.get(getApplication()),
                        FirebaseFirestore.getInstance());
                message = found ? "Danh mục trên máy đã cập nhật từ Firebase."
                        : "Firebase chưa có danh mục sự kiện.";
            } catch (IllegalArgumentException | IllegalStateException error) {
                message = error.getMessage();
            } catch (Exception error) {
                if (error instanceof InterruptedException) Thread.currentThread().interrupt();
                message = "Chưa tải được từ Firebase. Kiểm tra mạng rồi thử lại.";
            }
            downloadMessage.postValue(message);
            downloading.postValue(false);
        });
    }

    @Override protected void onCleared() { databaseWorker.shutdown(); }
}
