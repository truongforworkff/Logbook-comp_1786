package com.comp1786.room.features.cloud.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

@Dao
public interface SyncStateDao {
    @Query("SELECT * FROM sync_state WHERE id = 1") SyncState get();
    @Query("SELECT * FROM sync_state WHERE id = 1") LiveData<SyncState> observe();
    @Query("UPDATE sync_state SET uploadedRevision = MAX(uploadedRevision, :revision), syncedAt = :time, error = CASE WHEN revision <= :revision THEN '' ELSE error END WHERE id = 1")
    void acknowledge(long revision, long time);
    @Query("UPDATE sync_state SET revision = revision + 1, error = '' WHERE id = 1")
    void requestUpload();
    @Query("UPDATE sync_state SET error = :message WHERE id = 1 AND revision = :revision AND uploadedRevision < :revision") void setError(long revision, String message);
}
