package com.comp1786.room.features.events.data.local;
import com.comp1786.room.features.events.data.model.Event;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.List;
@Dao
public interface EventDao {
    @Query("SELECT * FROM events ORDER BY date ASC, id ASC") LiveData<List<Event>> observeAll();
    @Query("SELECT * FROM events ORDER BY date ASC, id ASC") List<Event> getAll();
    @Query("SELECT * FROM events WHERE id = :id") LiveData<Event> observeById(long id);
    @Query("SELECT publicId FROM events WHERE id = :id") String getPublicId(long id);
    @Insert long insertRow(Event event);
    @Update void updateRow(Event event);
    @Transaction default long insert(Event event) {
        if (event.publicId.isEmpty()) event.publicId = java.util.UUID.randomUUID().toString();
        return insertRow(event);
    }
    @Transaction default void update(Event event) {
        String publicId = getPublicId(event.id);
        if (publicId == null) throw new IllegalStateException("Event no longer exists");
        event.publicId = publicId;
        updateRow(event);
    }
    @Delete void delete(Event event);
}
