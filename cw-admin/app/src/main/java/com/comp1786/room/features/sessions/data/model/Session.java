package com.comp1786.room.features.sessions.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.annotation.Nullable;
import com.comp1786.room.features.events.data.model.Event;

@Entity(tableName = "sessions", foreignKeys = @ForeignKey(entity = Event.class,
        parentColumns = "id", childColumns = "eventId", onDelete = ForeignKey.CASCADE),
        indices = @Index("eventId"))
public class Session {
    @PrimaryKey(autoGenerate = true) public long id;
    // Null is reserved for sessions preserved from version 2, awaiting assignment by the user.
    @Nullable public Long eventId;
    @NonNull public String name;
    @NonNull public String time;
    @NonNull public String speaker;
    @NonNull public String room;
    @NonNull public String type;
    @NonNull public String description;

    public Session(long id, @Nullable Long eventId, @NonNull String name, @NonNull String time, @NonNull String speaker,
                   @NonNull String room, @NonNull String type, @NonNull String description) {
        this.id = id;
        this.eventId = eventId;
        this.name = name.trim();
        this.time = time.trim();
        this.speaker = speaker.trim();
        this.room = room.trim();
        this.type = type;
        this.description = description.trim();
    }
}
