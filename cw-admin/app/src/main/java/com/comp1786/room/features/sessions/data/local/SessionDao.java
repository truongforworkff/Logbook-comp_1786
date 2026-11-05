package com.comp1786.room.features.sessions.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.comp1786.room.features.sessions.data.model.Session;
import java.util.List;

@Dao
public interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY time, id")
    LiveData<List<Session>> observeAll();
    @Query("SELECT * FROM sessions ORDER BY time, id")
    List<Session> getAll();
    @Insert long insert(Session session);
    @Update int update(Session session);
    @Delete int delete(Session session);
}
