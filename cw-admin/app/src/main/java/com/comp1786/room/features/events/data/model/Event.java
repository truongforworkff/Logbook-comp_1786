package com.comp1786.room.features.events.data.model;

import java.util.Objects;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity(tableName = "events", indices = @androidx.room.Index(value = {"normalizedName", "date"}, unique = true))
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
    @NonNull public String normalizedName;
    @NonNull public String description = "";
    @NonNull public String contactEmail = "";
    @NonNull public String contactPhone = "";
    public Event(long id, @NonNull String name, @NonNull String date, @NonNull String location, @NonNull String organiser, @NonNull String type, @NonNull String status) {
        this.id = id;
        this.name = name.trim();
        this.normalizedName = name.trim().toLowerCase(java.util.Locale.ROOT);
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
        return id == other.id && Objects.equals(name, other.name) && Objects.equals(date, other.date) && Objects.equals(location, other.location) && Objects.equals(organiser, other.organiser) && Objects.equals(type, other.type) && Objects.equals(status, other.status) && Objects.equals(description, other.description) && Objects.equals(contactEmail, other.contactEmail) && Objects.equals(contactPhone, other.contactPhone);
    }
    @Override public int hashCode() { return Objects.hash(id, name, date, location, organiser, type, status, description, contactEmail, contactPhone); }
}
