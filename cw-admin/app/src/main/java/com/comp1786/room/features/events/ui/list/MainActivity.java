package com.comp1786.room.features.events.ui.list;

import android.content.Intent;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

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
import com.comp1786.room.features.events.logic.EventSearch;
import com.comp1786.room.features.events.logic.EventViewModel;
import com.comp1786.room.features.events.ui.details.EventDetails;
import com.comp1786.room.features.events.ui.editor.EventEditor;
import com.comp1786.room.features.sessions.ui.list.SessionActivity;
import com.comp1786.room.shared.ui.AppUi;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EventListScreenBinding binding;
    private EventViewModel model;
    private EventEditor editor;
    private AppUi ui;
    private EventAdapter adapter;
    private List<Event> allEvents = Collections.emptyList();
    private String dateFilter = "";
    private String organiserFilter = "";
    private String categoryFilter = "";
    private final ActivityResultLauncher<String[]> photoPicker =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null && editor != null) editor.acceptImage(uri);
            });

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        binding = EventListScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ui = new AppUi(this);
        setupWindow();
        restoreFilters(state);
        model = new ViewModelProvider(this).get(EventViewModel.class);
        editor = new EventEditor(this, ui, model, () -> photoPicker.launch(new String[]{"image/*"}));
        setupList();
        setupNavigation();
        setupSearch();
        observeEvents();
        editor.restoreState(state);
        model.getOperation().observe(this, this::handleOperation);
    }

    private void setupWindow() {
        ui.styleSystemBars(getWindow());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    private void restoreFilters(Bundle state) {
        if (state == null) return;
        dateFilter = state.getString("filterDate", "");
        organiserFilter = state.getString("filterOrganiser", "");
        categoryFilter = state.getString("filterCategory", "");
    }

    private void setupList() {
        EventDetails details = new EventDetails(this, ui, event -> editor.show(event, event));
        adapter = new EventAdapter(details::show);
        binding.list.setLayoutManager(new LinearLayoutManager(this));
        binding.list.setAdapter(adapter);
    }

    private void setupNavigation() {
        binding.cloudTab.setOnClickListener(view -> startActivity(new android.content.Intent(this,
                com.comp1786.room.features.cloud.ui.CloudActivity.class)));
        binding.sessionsTab.setOnClickListener(view ->
                startActivity(new Intent(this, SessionActivity.class)));
        binding.add.setOnClickListener(view -> editor.show(null, null));
    }

    private void setupSearch() {
        binding.search.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence text, int start, int count, int after) {}
            @Override public void afterTextChanged(android.text.Editable text) {}
            @Override public void onTextChanged(CharSequence text, int start, int before, int count) {
                renderEvents();
            }
        });
        binding.filter.setOnClickListener(view -> showFilters());
    }

    private void showFilters() {
        new EventFilterDialog(this, ui).show(dateFilter, organiserFilter, categoryFilter, (date, organiser, category, clearSearch) -> {
            dateFilter = date;
            organiserFilter = organiser;
            categoryFilter = category;
            if (clearSearch) binding.search.setText("");
            renderEvents();
        });
    }

    private void observeEvents() {
        model.getEvents().observe(this, events -> {
            allEvents = events == null ? Collections.emptyList() : events;
            renderEvents();
        });
    }

    private void renderEvents() {
        String query = binding.search.getText().toString().trim();
        List<Event> shown = EventSearch.filter(allEvents, query, dateFilter, organiserFilter, categoryFilter);
        adapter.submitList(shown);
        boolean filtered = !query.isEmpty() || !dateFilter.isEmpty() || !organiserFilter.isEmpty() || !categoryFilter.isEmpty();
        binding.listTitle.setText(filtered ? R.string.filtered_section : R.string.events_section);
        binding.count.setText(getString(R.string.event_count, shown.size()));
        binding.empty.setText(filtered ? R.string.empty_search : R.string.empty);
        binding.empty.setVisibility(shown.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void handleOperation(EventViewModel.Operation operation) {
        if (operation == EventViewModel.Operation.IDLE) return;
        if (operation == EventViewModel.Operation.SUCCESS) {
            boolean deleted = editor.wasDeleting();
            editor.close();
            ui.showNotice(null, getString(deleted ? R.string.deleted_notice : R.string.saved_notice), false);
        } else {
            int message = operation == EventViewModel.Operation.DUPLICATE
                    ? R.string.duplicate_event : R.string.storage_error;
            ui.showNotice(editor.window(), getString(message), true);
            editor.enableActions();
        }
        model.consumeOperation();
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        state.putString("filterDate", dateFilter);
        state.putString("filterOrganiser", organiserFilter);
        state.putString("filterCategory", categoryFilter);
        editor.saveState(state);
        super.onSaveInstanceState(state);
    }

    @Override protected void onDestroy() {
        if (ui != null) ui.close();
        super.onDestroy();
    }
}
