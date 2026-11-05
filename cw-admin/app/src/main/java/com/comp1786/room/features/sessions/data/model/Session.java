package com.comp1786.room.features.sessions.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sessions")
public class Session {
    @PrimaryKey(autoGenerate = true) public long id;
    @NonNull public String name;
    @NonNull public String time;
    @NonNull public String speaker;
    @NonNull public String room;
    @NonNull public String type;
    @NonNull public String description;

    public Session(long id, @NonNull String name, @NonNull String time, @NonNull String speaker,
                   @NonNull String room, @NonNull String type, @NonNull String description) {
        this.id = id;
        this.name = name.trim();
        this.time = time.trim();
        this.speaker = speaker.trim();
        this.room = room.trim();
        this.type = type;
        this.description = description.trim();
    }
}
