package com.comp1786.room.features.sessions.ui.list;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.comp1786.room.R;
import com.comp1786.room.features.events.data.model.Event;
import com.comp1786.room.features.events.logic.EventValidation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongConsumer;

final class SessionEventPicker {
    private final Spinner spinner;
    private final LongConsumer onSelect;

    SessionEventPicker(Spinner spinner, LongConsumer onSelect) {
        this.spinner = spinner;
        this.onSelect = onSelect;
    }

    void submit(List<Event> events, long selectedId) {
        List<Event> choices = new ArrayList<>(events);
        List<String> labels = new ArrayList<>();
        labels.add(spinner.getContext().getString(R.string.session_choose_event));
        int selected = 0;
        for (int index = 0; index < choices.size(); index++) {
            Event event = choices.get(index);
            labels.add(spinner.getContext().getString(R.string.session_event_option,
                    event.name, EventValidation.displayDate(event.date)));
            if (event.id == selectedId) selected = index + 1;
        }
        spinner.setOnItemSelectedListener(null);
        spinner.setAdapter(new ArrayAdapter<>(spinner.getContext(),
                android.R.layout.simple_spinner_dropdown_item, labels));
        spinner.setSelection(selected);
        onSelect.accept(selected == 0 ? 0 : choices.get(selected - 1).id);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onSelect.accept(position == 0 ? 0 : choices.get(position - 1).id);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { onSelect.accept(0); }
        });
    }
}
