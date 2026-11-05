package com.comp1786.room.features.events.data.local;
import com.comp1786.room.features.events.data.model.Event;

import android.content.Context;
import com.comp1786.room.features.sessions.data.model.Session;
import com.comp1786.room.features.sessions.data.local.SessionDao;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
@Database(entities = {Event.class, Session.class}, version = 2, exportSchema = true)
public abstract class EventDatabase extends RoomDatabase {
    public abstract EventDao events();
    public abstract SessionDao sessions();
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS sessions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, time TEXT NOT NULL, speaker TEXT NOT NULL, room TEXT NOT NULL, type TEXT NOT NULL, description TEXT NOT NULL)");
        }
    };
    private static volatile EventDatabase instance;
    public static EventDatabase get(Context context) {
        if (instance == null) {
            synchronized (EventDatabase.class) {
                if (instance == null) instance = Room.databaseBuilder(context.getApplicationContext(),
                        EventDatabase.class, "events.db").addMigrations(MIGRATION_1_2).build();
            }
        }
        return instance;
    }
}
