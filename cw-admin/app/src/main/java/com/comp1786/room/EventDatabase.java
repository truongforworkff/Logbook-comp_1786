package com.comp1786.room;
import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
@Database(entities = {Event.class}, version = 1, exportSchema = true)
public abstract class EventDatabase extends RoomDatabase {
    public abstract EventDao events();
    private static volatile EventDatabase instance;
    public static EventDatabase get(Context context) {
        if (instance == null) {
            synchronized (EventDatabase.class) {
                if (instance == null) instance = Room.databaseBuilder(context.getApplicationContext(),
                        EventDatabase.class, "events.db").build();
            }
        }
        return instance;
    }
}
