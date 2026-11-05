package com.comp1786.room.features.events.ui.list;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.comp1786.room.R;
import com.comp1786.room.databinding.EventListScreenBinding;
import com.comp1786.room.features.events.data.model.Event;
import com.comp1786.room.features.events.logic.EventViewModel;
import com.comp1786.room.features.events.ui.details.EventDetails;
import com.comp1786.room.features.events.ui.editor.EventEditor;
import com.comp1786.room.shared.ui.AppUi;
import java.util.Locale;
public class MainActivity extends AppCompatActivity {
    private EventListScreenBinding binding;
    private EventViewModel model;
    private EventEditor editor;
    private AppUi ui;
    private EventAdapter adapter;
    private java.util.List<Event> allEvents=java.util.Collections.emptyList();
    private String dateFilter="",organiserFilter="";
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        binding = EventListScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ui = new AppUi(this);
        ui.styleSystemBars(getWindow());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int padding = 0;
            view.setPadding(padding + bars.left, padding + bars.top, padding + bars.right, padding + bars.bottom);
            return insets;
        });
        model = new ViewModelProvider(this).get(EventViewModel.class);
        editor = new EventEditor(this,ui,model);
        EventDetails details = new EventDetails(this,ui,event -> editor.show(event,event));
        adapter = new EventAdapter(details::show);
        if (state != null) { dateFilter = state.getString("filterDate", ""); organiserFilter = state.getString("filterOrganiser", ""); }
        binding.list.setLayoutManager(new LinearLayoutManager(this));
        binding.list.setAdapter(adapter);
        binding.sessionsTab.setOnClickListener(view -> startActivity(new android.content.Intent(this,
                com.comp1786.room.features.sessions.ui.list.SessionActivity.class)));
        binding.add.setOnClickListener(view -> editor.show(null, null));
        model.getEvents().observe(this, events -> {
            allEvents = events == null ? java.util.Collections.emptyList() : events;
            renderEvents();
        });
        binding.search.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s,int start,int count,int after) {}
            public void onTextChanged(CharSequence s,int start,int before,int count) { renderEvents(); }
            public void afterTextChanged(android.text.Editable value) {}
        });
        binding.filter.setOnClickListener(view -> new EventFilterDialog(this,ui).show(dateFilter,organiserFilter,(date,organiser,clearSearch)-> {
            dateFilter=date; organiserFilter=organiser;
            if(clearSearch) binding.search.setText("");
            renderEvents();
        }));

        editor.restoreState(state);
        model.getOperation().observe(this, operation -> {
            if (operation == EventViewModel.Operation.DUPLICATE) {
                ui.showNotice(editor.window(),getString(R.string.duplicate_event),true);
                editor.enableActions();
                model.consumeOperation();
            } else if (operation == EventViewModel.Operation.ERROR) {
                ui.showNotice(editor.window(),getString(R.string.storage_error),true);
                editor.enableActions();
                model.consumeOperation();
            } else if (operation == EventViewModel.Operation.SUCCESS) {
                boolean deleted=editor.wasDeleting();
                editor.close();
                ui.showNotice(null,getString(deleted ? R.string.deleted_notice : R.string.saved_notice),false);
                model.consumeOperation();
            }
        });

    }
    private void renderEvents() {
        String query = binding.search.getText().toString().trim().toLowerCase(Locale.ROOT);
        java.util.List<Event> shown = new java.util.ArrayList<>();
        for (Event event : allEvents) {
            if (event.name.toLowerCase(Locale.ROOT).contains(query)
                && (dateFilter.isEmpty() || event.date.equals(dateFilter))
                && event.organiser.toLowerCase(Locale.ROOT).contains(organiserFilter.toLowerCase(Locale.ROOT))) shown.add(event);
        }
        adapter.submitList(shown);
        boolean filtered = !query.isEmpty() || !dateFilter.isEmpty() || !organiserFilter.isEmpty();
        binding.listTitle.setText(filtered ? R.string.filtered_section : R.string.events_section);
        binding.count.setText(getString(R.string.event_count,shown.size()));
        binding.empty.setText(filtered ? R.string.empty_search : R.string.empty);
        binding.empty.setVisibility(shown.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        state.putString("filterDate",dateFilter); state.putString("filterOrganiser",organiserFilter);
        editor.saveState(state);
        super.onSaveInstanceState(state);
    }
    @Override protected void onDestroy() {
        if(ui!=null) ui.close();
        super.onDestroy();
    }
}
