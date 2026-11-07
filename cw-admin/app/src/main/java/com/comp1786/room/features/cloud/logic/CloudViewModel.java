package com.comp1786.room.features.cloud.logic;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import com.comp1786.room.features.cloud.data.SyncState;
import com.comp1786.room.features.events.data.local.EventDatabase;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class CloudViewModel extends AndroidViewModel {
    private final ExecutorService databaseWorker = Executors.newSingleThreadExecutor();

    public CloudViewModel(Application application) { super(application); }

    public LiveData<SyncState> state() { return EventDatabase.get(getApplication()).sync().observe(); }
    public LiveData<List<WorkInfo>> work() {
        return WorkManager.getInstance(getApplication()).getWorkInfosForUniqueWorkLiveData(CloudSync.IMMEDIATE);
    }

    public void uploadAll() {
        databaseWorker.execute(() -> {
            EventDatabase.get(getApplication()).sync().requestUpload();
            CloudSync.enqueue(getApplication());
        });
    }

    @Override protected void onCleared() { databaseWorker.shutdown(); }
}
