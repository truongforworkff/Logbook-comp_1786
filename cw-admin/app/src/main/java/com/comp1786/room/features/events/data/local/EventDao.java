package com.comp1786.room.features.events.data.local;
import com.comp1786.room.features.events.data.model.Event;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.List;
@Dao
public interface EventDao {
    @Query("SELECT * FROM events ORDER BY createdAt DESC, date DESC, id DESC") LiveData<List<Event>> observeAll();
    @Query("SELECT * FROM events ORDER BY date ASC, id ASC") List<Event> getAll();
    @Query("SELECT * FROM events WHERE id = :id") LiveData<Event> observeById(long id);
    @Query("SELECT * FROM events WHERE id = :id") Event getById(long id);
    @Insert long insertRow(Event event);
    @Update void updateRow(Event event);
    @Transaction default long insert(Event event) {
        if (event.publicId.isEmpty()) event.publicId = java.util.UUID.randomUUID().toString();
        if (event.createdAt == 0) event.createdAt = System.currentTimeMillis();
        return insertRow(event);
    }
    @Transaction default void update(Event event) {
        Event saved = getById(event.id);
        if (saved == null) throw new IllegalStateException("Event no longer exists");
        event.publicId = saved.publicId;
        event.createdAt = saved.createdAt;
        updateRow(event);
    }
    @Delete void delete(Event event);
}
