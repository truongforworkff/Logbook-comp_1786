package com.comp1786.room.features.cloud.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sync_state")
public class SyncState {
    @PrimaryKey public int id = 1;
    @NonNull public String sourceId;
    @ColumnInfo(defaultValue = "0") public long revision;
    @ColumnInfo(defaultValue = "-1") public long uploadedRevision = -1;
    @ColumnInfo(defaultValue = "0") public long syncedAt;
    @NonNull @ColumnInfo(defaultValue = "''") public String error = "";

    public SyncState(@NonNull String sourceId) { this.sourceId = sourceId; }
    public boolean pending() { return revision > uploadedRevision; }
}
