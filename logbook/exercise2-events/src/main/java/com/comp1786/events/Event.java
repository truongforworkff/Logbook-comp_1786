package com.comp1786.events;
import java.util.Objects;
public class Event {
    public long id;
    public String name;
    public String date;
    public String location;
    public String organiser;
    public String type;
    public Event(long id, String name, String date, String location, String organiser, String type) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.location = location;
        this.organiser = organiser;
        this.type = type;
    }
    @Override public boolean equals(Object value) {
        if (this == value) return true;
        if (!(value instanceof Event)) return false;
        Event other = (Event) value;
        return id == other.id && Objects.equals(name, other.name) && Objects.equals(date, other.date) && Objects.equals(location, other.location) && Objects.equals(organiser, other.organiser) && Objects.equals(type, other.type);
    }
    @Override public int hashCode() { return Objects.hash(id, name, date, location, organiser, type); }
}
