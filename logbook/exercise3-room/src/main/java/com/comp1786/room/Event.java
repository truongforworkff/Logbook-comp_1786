package com.comp1786.room;
import java.util.Objects;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity(tableName = "events")
public class Event {
    @PrimaryKey(autoGenerate = true)
    public long id;
    @NonNull
    public String name;
    @NonNull
    public String date;
    @NonNull
    public String location;
    @NonNull
    public String organiser;
    @NonNull
    public String type;
    @NonNull
    public String status;
    public Event(long id, @NonNull String name, @NonNull String date, @NonNull String location, @NonNull String organiser, @NonNull String type, @NonNull String status) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.location = location;
        this.organiser = organiser;
        this.type = type;
        this.status = status;
    }
    @Override public boolean equals(Object value) {
        if (this == value) return true;
        if (!(value instanceof Event)) return false;
        Event other = (Event) value;
        return id == other.id && Objects.equals(name, other.name) && Objects.equals(date, other.date) && Objects.equals(location, other.location) && Objects.equals(organiser, other.organiser) && Objects.equals(type, other.type) && Objects.equals(status, other.status);
    }
    @Override public int hashCode() { return Objects.hash(id, name, date, location, organiser, type, status); }
}
