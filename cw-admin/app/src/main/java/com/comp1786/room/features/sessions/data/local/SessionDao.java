package com.comp1786.room.features.sessions.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.comp1786.room.features.sessions.data.model.Session;
import java.util.List;

@Dao
public interface SessionDao {
    @Query("SELECT * FROM sessions WHERE eventId = :eventId ORDER BY time, id")
    LiveData<List<Session>> observeForEvent(long eventId);
    @Query("SELECT * FROM sessions WHERE eventId IS NULL ORDER BY time, id")
    LiveData<List<Session>> observeUnassigned();
    @Query("SELECT * FROM sessions WHERE id = :id")
    LiveData<Session> observeById(long id);
    @Query("SELECT COUNT(*) FROM sessions WHERE eventId = :eventId")
    LiveData<Integer> observeCount(long eventId);
    @Query("SELECT COUNT(*) FROM sessions WHERE eventId IS NULL")
    LiveData<Integer> observeUnassignedCount();
    @Query("SELECT * FROM sessions ORDER BY time, id")
    List<Session> getAll();
    @Insert long insert(Session session);
    @Update int update(Session session);
    @Delete int delete(Session session);
}
