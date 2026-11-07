package com.comp1786.room.features.cloud.data;

import androidx.annotation.NonNull;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import java.util.UUID;

public final class SyncSchema {
    private SyncSchema() {}

    public static void migrate(SupportSQLiteDatabase db) {
        db.execSQL("ALTER TABLE events ADD COLUMN publicId TEXT NOT NULL DEFAULT ''");
        createState(db);
        db.execSQL("UPDATE events SET publicId = ? || '-event-' || id",
                new Object[]{UUID.randomUUID().toString()});
        // Preserve favourite IDs already published in the original G/H demo catalog.
        db.execSQL("UPDATE events SET publicId = ? WHERE name = ? AND date = ?", new Object[]{"gtc-2026", "NVIDIA GTC 2026 — AI và điện toán tăng tốc", "2026-03-16"});
        db.execSQL("UPDATE events SET publicId = ? WHERE name = ? AND date = ?", new Object[]{"ignite-2026", "Microsoft Ignite 2026 — AI cho doanh nghiệp", "2026-11-17"});
        db.execSQL("UPDATE events SET publicId = ? WHERE name = ? AND date = ?", new Object[]{"gtc-dc-2026", "NVIDIA GTC Washington, D.C. 2026", "2026-11-30"});
        db.execSQL("UPDATE events SET publicId = ? WHERE name = ? AND date = ?", new Object[]{"macbook-m6", "Apple giới thiệu MacBook Pro 14 inch với M6", "2026-10-27"});
        db.execSQL("UPDATE events SET publicId = ? WHERE name = ? AND date = ?", new Object[]{"imac-m6", "Apple giới thiệu iMac với M6", "2026-10-27"});
        db.execSQL("CREATE UNIQUE INDEX index_events_publicId ON events(publicId)");
        install(db);
    }

    private static void createState(SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS sync_state (id INTEGER NOT NULL, sourceId TEXT NOT NULL, revision INTEGER NOT NULL DEFAULT 0, uploadedRevision INTEGER NOT NULL DEFAULT -1, syncedAt INTEGER NOT NULL DEFAULT 0, error TEXT NOT NULL DEFAULT '', PRIMARY KEY(id))");
    }

    public static void install(SupportSQLiteDatabase db) {
        db.execSQL("INSERT OR IGNORE INTO sync_state (id, sourceId, revision, uploadedRevision, syncedAt, error) VALUES (1, ?, 0, -1, 0, '')",
                new Object[]{UUID.randomUUID().toString()});
        for (String table : new String[]{"events", "sessions"}) {
            for (String action : new String[]{"INSERT", "UPDATE", "DELETE"}) {
                db.execSQL("CREATE TRIGGER IF NOT EXISTS eventa_sync_" + table + "_" + action
                        + " AFTER " + action + " ON " + table
                        + " BEGIN UPDATE sync_state SET revision = revision + 1, error = '' WHERE id = 1; END");
            }
        }
    }

    public static final RoomDatabase.Callback CALLBACK = new RoomDatabase.Callback() {
        @Override public void onCreate(@NonNull SupportSQLiteDatabase db) { install(db); }
        @Override public void onOpen(@NonNull SupportSQLiteDatabase db) { install(db); }
    };
}
