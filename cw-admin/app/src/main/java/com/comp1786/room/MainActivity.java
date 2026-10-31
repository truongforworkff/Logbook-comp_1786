package com.comp1786.room;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.comp1786.room.databinding.ActivityMainBinding;
import com.comp1786.room.databinding.DialogEventBinding;
import com.comp1786.room.databinding.DialogDetailsBinding;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private EventViewModel model;
    private AlertDialog editor;
    private DialogEventBinding editorFields;
    private Event editingEvent;
    private EventAdapter adapter;
    private java.util.List<Event> allEvents = java.util.Collections.emptyList();
    private String dateFilter = "", organiserFilter = "";
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int padding = 0;
            view.setPadding(padding + bars.left, padding + bars.top, padding + bars.right, padding + bars.bottom);
            return insets;
        });
        model = new ViewModelProvider(this).get(EventViewModel.class);
        adapter = new EventAdapter(this::showDetails);
        if (state != null) { dateFilter = state.getString("filterDate", ""); organiserFilter = state.getString("filterOrganiser", ""); }
        binding.list.setLayoutManager(new LinearLayoutManager(this));
        binding.list.setAdapter(adapter);
        binding.add.setOnClickListener(view -> showEditor(null, null));
        model.getEvents().observe(this, events -> {
            allEvents = events == null ? java.util.Collections.emptyList() : events;
            renderEvents();
        });
        binding.search.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s,int start,int count,int after) {}
            public void onTextChanged(CharSequence s,int start,int before,int count) { renderEvents(); }
            public void afterTextChanged(android.text.Editable value) {}
        });
        binding.filter.setOnClickListener(view -> showFilters());

        if (state != null && state.getBoolean("editorOpen")) {
            Event draft = new Event(state.getLong("eventId"), state.getString("name", ""),
                    state.getString("date", ""), state.getString("location", ""),
                    state.getString("organiser", ""), state.getString("type", ""), state.getString("status", "Upcoming"));
            draft.description = state.getString("description", "");
            draft.contactEmail = state.getString("email", "");
            draft.contactPhone = state.getString("phone", "");
            showEditor(draft.id == 0 ? null : draft, draft);
        }
        model.getOperation().observe(this, operation -> {
            if (operation == EventViewModel.Operation.DUPLICATE) {
                Toast.makeText(this, R.string.duplicate_event, Toast.LENGTH_LONG).show();
                if (editorFields != null) editorFields.reviewAction.setEnabled(true);
                model.consumeOperation();
            } else if (operation == EventViewModel.Operation.ERROR) {
                Toast.makeText(this, R.string.storage_error, Toast.LENGTH_LONG).show();
                if (editorFields != null) editorFields.reviewAction.setEnabled(true);
                model.consumeOperation();
            } else if (operation == EventViewModel.Operation.SUCCESS) {
                if (editor != null) editor.dismiss();
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
    private void showFilters() {
        android.widget.LinearLayout fields = new android.widget.LinearLayout(this);
        fields.setOrientation(android.widget.LinearLayout.VERTICAL); fields.setPadding(dp(24),dp(8),dp(24),dp(16));
        EditText date = filterField(fields,getString(R.string.filter_date),dateFilter.isEmpty() ? "" : EventValidation.displayDate(dateFilter));
        date.setHint(R.string.date_hint); date.setInputType(android.text.InputType.TYPE_CLASS_DATETIME | android.text.InputType.TYPE_DATETIME_VARIATION_DATE);
        EditText organiser = filterField(fields,getString(R.string.filter_organiser),organiserFilter);
        AlertDialog filter = new AlertDialog.Builder(this).setTitle(R.string.filter_title).setView(fields)
            .setNegativeButton(R.string.cancel,null).setNeutralButton(R.string.clear_filter,null).setPositiveButton(R.string.apply_filter,null).create();
        filter.setOnShowListener(ignored -> {
            filter.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                String entered = date.getText().toString().trim();
                if (!entered.isEmpty() && !EventValidation.validDate(entered)) { date.setError(getString(R.string.invalid_date)); return; }
                dateFilter = entered.isEmpty() ? "" : EventValidation.storageDate(entered);
                organiserFilter = organiser.getText().toString().trim();
                renderEvents(); filter.dismiss();
            });
            filter.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(view -> {
                dateFilter = ""; organiserFilter = ""; binding.search.setText(""); renderEvents(); filter.dismiss();
            });
        });
        filter.show();
    }
    private EditText filterField(android.widget.LinearLayout parent,String label,String value) {
        android.widget.TextView text = new android.widget.TextView(this); text.setText(label); parent.addView(text);
        EditText input = new EditText(this); input.setText(value); input.setSingleLine(true); input.setMinHeight(dp(48));
        input.setId(View.generateViewId()); text.setLabelFor(input.getId()); parent.addView(input);
        return input;
    }
    private int dp(int value) { return (int)(value * getResources().getDisplayMetrics().density); }
    private void fullScreen(AlertDialog dialog,View root) {
        android.view.Window window = dialog.getWindow();
        if (window == null) return;
        window.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT,android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        window.setBackgroundDrawableResource(R.color.canvas);
        window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        ViewCompat.setOnApplyWindowInsetsListener(root,(view,insets)-> {
            androidx.core.graphics.Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left,bars.top,bars.right,bars.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
        android.widget.Button primary = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (primary == null) return;
        primary.setTextColor(android.graphics.Color.WHITE); primary.setAllCaps(false);
        primary.setBackgroundResource(R.drawable.primary_button); ViewCompat.setBackgroundTintList(primary,null);
        primary.setMinHeight(dp(52)); primary.setPadding(dp(20),dp(12),dp(20),dp(12));
    }
    private void showDetails(Event event) {
        DialogDetailsBinding page = DialogDetailsBinding.inflate(getLayoutInflater());
        page.title.setText(event.name); page.category.setText(event.type+" · "+EventAdapter.statusLabel(this,event.status));
        page.date.setText(EventValidation.displayDate(event.date)); page.location.setText(event.location);
        page.organiser.setText(getString(R.string.organised_by,event.organiser));
        page.description.setText(event.description.isEmpty()?getString(R.string.no_description):event.description);
        page.contacts.setVisibility(event.contactEmail.isEmpty() && event.contactPhone.isEmpty()?View.GONE:View.VISIBLE);
        page.email.setText(event.contactEmail); page.email.setVisibility(event.contactEmail.isEmpty()?View.GONE:View.VISIBLE);
        page.phone.setText(event.contactPhone); page.phone.setVisibility(event.contactPhone.isEmpty()?View.GONE:View.VISIBLE);
        AlertDialog details = new AlertDialog.Builder(this).setView(page.getRoot()).create();
        page.editAction.setOnClickListener(view -> { details.dismiss(); showEditor(event,event); });
        page.back.setOnClickListener(view->details.dismiss());
        details.show(); fullScreen(details,page.getRoot());
    }
    private void showEditor(Event existing, Event draft) {
        DialogEventBinding form = DialogEventBinding.inflate(getLayoutInflater());
        editorFields = form;
        editingEvent = existing;
        form.editorTitle.setText(existing == null ? R.string.create_title : R.string.edit_title);
        if (draft != null) {
            form.descriptionInput.setText(draft.description);
            form.emailInput.setText(draft.contactEmail);
            form.phoneInput.setText(draft.contactPhone);
            form.nameInput.setText(draft.name);
            form.dateInput.setText(draft.date.matches("\\d{4}-\\d{2}-\\d{2}") ? EventValidation.displayDate(draft.date) : draft.date);
            form.locationInput.setText(draft.location);
            form.organiserInput.setText(draft.organiser);
            form.typeInput.setSelection(Math.max(0, Arrays.asList(getResources().getStringArray(R.array.types)).indexOf(draft.type)));
            form.statusInput.setSelection(Math.max(0, Arrays.asList(getResources().getStringArray(R.array.statuses)).indexOf(draft.status)));
        }
        form.pickDate.setOnClickListener(view -> {
            String text = form.dateInput.getText().toString();
            LocalDate initial = EventValidation.validDate(text) ? LocalDate.parse(EventValidation.storageDate(text)) : LocalDate.now();
            new DatePickerDialog(this, (picker, year, month, day) ->
                    form.dateInput.setText(String.format(Locale.ROOT, "%02d/%02d/%04d", day, month + 1, year)),
                    initial.getYear(), initial.getMonthValue() - 1, initial.getDayOfMonth()).show();
        });
        form.moreOptions.setOnClickListener(view -> {
            boolean expand = form.optionalFields.getVisibility() != View.VISIBLE;
            form.optionalFields.setVisibility(expand ? View.VISIBLE : View.GONE);
            form.moreOptions.setText(expand ? R.string.less_options : R.string.more_options);
        });
        if (draft != null && (!draft.description.isEmpty() || !draft.contactEmail.isEmpty() || !draft.contactPhone.isEmpty())) {
            form.optionalFields.setVisibility(View.VISIBLE); form.moreOptions.setText(R.string.less_options);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setView(form.getRoot());
        form.deleteAction.setVisibility(existing == null ? View.GONE : View.VISIBLE);
        AlertDialog dialog = builder.create();
        editor = dialog;
        form.back.setOnClickListener(view -> dialog.dismiss());
        dialog.setOnDismissListener(ignored -> { editor = null; editorFields = null; editingEvent = null; });
        dialog.setOnShowListener(ignored -> {
            form.reviewAction.setOnClickListener(view -> {
                EditText[] fields = {form.nameInput, form.locationInput, form.organiserInput};
                for (EditText field : fields) {
                    field.setError(EventValidation.validText(field.getText().toString()) ? null : getString(R.string.required));
                }
                String date = form.dateInput.getText().toString().trim();
                form.dateInput.setError(EventValidation.validDate(date) ? null : getString(R.string.invalid_date));
                for (EditText field : new EditText[]{form.nameInput, form.locationInput, form.organiserInput, form.dateInput}) {
                    if (field.getError() != null) { field.requestFocus(); return; }
                }
                String email = form.emailInput.getText().toString().trim();
                String phone = form.phoneInput.getText().toString().trim();
                form.emailInput.setError(EventValidation.validEmail(email) ? null : getString(R.string.invalid_email));
                form.phoneInput.setError(EventValidation.validPhone(phone) ? null : getString(R.string.invalid_phone));
                if (form.emailInput.getError() != null) { form.emailInput.requestFocus(); return; }
                if (form.phoneInput.getError() != null) { form.phoneInput.requestFocus(); return; }
                Event event = new Event(existing == null ? 0 : existing.id,
                        form.nameInput.getText().toString().trim(), EventValidation.storageDate(date),
                        form.locationInput.getText().toString().trim(),
                        form.organiserInput.getText().toString().trim(), form.typeInput.getSelectedItem().toString(), form.statusInput.getSelectedItem().toString());
                event.description = form.descriptionInput.getText().toString().trim();
                event.contactEmail = email;
                event.contactPhone = phone;
                String review = getString(R.string.review_details, event.name, date, event.location, event.organiser, event.type, event.status,
                        event.description.isEmpty() ? getString(R.string.not_provided) : event.description,
                        email.isEmpty() ? getString(R.string.not_provided) : email,
                        phone.isEmpty() ? getString(R.string.not_provided) : phone);
                new AlertDialog.Builder(this).setTitle(R.string.review_title).setMessage(review)
                    .setNegativeButton(R.string.back_edit, null)
                    .setPositiveButton(R.string.save, (confirmation, button) -> {
                        form.reviewAction.setEnabled(false);
                        model.save(event);
                    }).show();
            });
            if (existing != null) form.deleteAction.setOnClickListener(view ->
                    new AlertDialog.Builder(this).setTitle(R.string.delete_title)
                            .setMessage(getString(R.string.delete_message, existing.name))
                            .setNegativeButton(R.string.cancel, null)
                            .setPositiveButton(R.string.delete, (confirmation, button) -> model.delete(existing)).show());
            form.reviewAction.setEnabled(!model.isBusy());
        });
        dialog.show();
        fullScreen(dialog,form.getRoot());
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        state.putString("filterDate",dateFilter); state.putString("filterOrganiser",organiserFilter);
        if (editorFields != null) {
            state.putString("description", editorFields.descriptionInput.getText().toString());
            state.putString("email", editorFields.emailInput.getText().toString());
            state.putString("phone", editorFields.phoneInput.getText().toString());
            state.putBoolean("editorOpen", true);
            state.putLong("eventId", editingEvent == null ? 0 : editingEvent.id);
            state.putString("name", editorFields.nameInput.getText().toString());
            state.putString("date", editorFields.dateInput.getText().toString());
            state.putString("location", editorFields.locationInput.getText().toString());
            state.putString("organiser", editorFields.organiserInput.getText().toString());
            state.putString("type", editorFields.typeInput.getSelectedItem().toString());
            state.putString("status", editorFields.statusInput.getSelectedItem().toString());
        }
        super.onSaveInstanceState(state);
    }
}
