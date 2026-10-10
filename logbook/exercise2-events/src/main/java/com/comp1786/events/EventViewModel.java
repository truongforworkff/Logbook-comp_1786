package com.comp1786.events;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
/** RAM only: retained on rotation, discarded on process termination. */
public class EventViewModel extends ViewModel {
    private final MutableLiveData<List<Event>> events = new MutableLiveData<>(Collections.emptyList());
    private long nextId = 1;
    public LiveData<List<Event>> getEvents() { return events; }
    public void add(Event event) {
        List<Event> next = new ArrayList<>(events.getValue());
        next.add(new Event(nextId++, event.name, event.date, event.location, event.organiser, event.type));
        next.sort(Comparator.comparing(item -> item.date));
        events.setValue(Collections.unmodifiableList(next));
    }
}
