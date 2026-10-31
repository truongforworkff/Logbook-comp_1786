package com.comp1786.room;
import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.List;
@Dao
public interface EventDao {
    @Query("SELECT * FROM events ORDER BY date ASC, id ASC") LiveData<List<Event>> observeAll();
    @Query("SELECT * FROM events ORDER BY date ASC, id ASC") List<Event> getAll();
    @Insert long insert(Event event);
    @Update void update(Event event);
    @Delete void delete(Event event);
}
