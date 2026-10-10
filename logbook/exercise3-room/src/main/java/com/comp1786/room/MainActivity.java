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
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private EventViewModel model;
    private AlertDialog editor;
    private DialogEventBinding editorFields;
    private Event editingEvent;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int padding = (int) (20 * getResources().getDisplayMetrics().density);
            view.setPadding(padding + bars.left, padding + bars.top, padding + bars.right, padding + bars.bottom);
            return insets;
        });
        model = new ViewModelProvider(this).get(EventViewModel.class);
        EventAdapter adapter = new EventAdapter(event -> showEditor(event, event));
        binding.list.setLayoutManager(new LinearLayoutManager(this));
        binding.list.setAdapter(adapter);
        binding.add.setOnClickListener(view -> showEditor(null, null));
        model.getEvents().observe(this, events -> {
            adapter.submitList(events);
            binding.empty.setVisibility(events == null || events.isEmpty() ? View.VISIBLE : View.GONE);
        });

        if (state != null && state.getBoolean("editorOpen")) {
            Event draft = new Event(state.getLong("eventId"), state.getString("name", ""),
                    state.getString("date", ""), state.getString("location", ""),
                    state.getString("organiser", ""), state.getString("type", ""), state.getString("status", "Upcoming"));
            showEditor(draft.id == 0 ? null : draft, draft);
        }
        model.getOperation().observe(this, operation -> {
            if (operation == EventViewModel.Operation.ERROR) {
                Toast.makeText(this, R.string.storage_error, Toast.LENGTH_LONG).show();
                if (editor != null) editor.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                model.consumeOperation();
            } else if (operation == EventViewModel.Operation.SUCCESS) {
                if (editor != null) editor.dismiss();
                model.consumeOperation();
            }
        });

    }
    private void showEditor(Event existing, Event draft) {
        DialogEventBinding form = DialogEventBinding.inflate(getLayoutInflater());
        editorFields = form;
        editingEvent = existing;
        if (draft != null) {
            form.nameInput.setText(draft.name);
            form.dateInput.setText(draft.date);
            form.locationInput.setText(draft.location);
            form.organiserInput.setText(draft.organiser);
            form.typeInput.setSelection(Math.max(0, Arrays.asList(getResources().getStringArray(R.array.types)).indexOf(draft.type)));
            form.statusInput.setSelection(Math.max(0, Arrays.asList(getResources().getStringArray(R.array.statuses)).indexOf(draft.status)));
        }
        form.pickDate.setOnClickListener(view -> {
            String text = form.dateInput.getText().toString();
            LocalDate initial = EventValidation.validDate(text) ? LocalDate.parse(text) : LocalDate.now();
            new DatePickerDialog(this, (picker, year, month, day) ->
                    form.dateInput.setText(String.format(Locale.ROOT, "%04d-%02d-%02d", year, month + 1, day)),
                    initial.getYear(), initial.getMonthValue() - 1, initial.getDayOfMonth()).show();
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(existing == null ? R.string.add : R.string.edit)
                .setView(form.getRoot()).setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, null);
        if (existing != null) builder.setNeutralButton(R.string.delete, null);
        AlertDialog dialog = builder.create();
        editor = dialog;
        dialog.setOnDismissListener(ignored -> { editor = null; editorFields = null; editingEvent = null; });
        dialog.setOnShowListener(ignored -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                EditText[] fields = {form.nameInput, form.locationInput, form.organiserInput};
                for (EditText field : fields) {
                    field.setError(EventValidation.validText(field.getText().toString()) ? null : getString(R.string.required));
                }
                String date = form.dateInput.getText().toString().trim();
                form.dateInput.setError(EventValidation.validDate(date) ? null : getString(R.string.invalid_date));
                for (EditText field : new EditText[]{form.nameInput, form.locationInput, form.organiserInput, form.dateInput}) {
                    if (field.getError() != null) { field.requestFocus(); return; }
                }
                Event event = new Event(existing == null ? 0 : existing.id,
                        form.nameInput.getText().toString().trim(), date,
                        form.locationInput.getText().toString().trim(),
                        form.organiserInput.getText().toString().trim(), form.typeInput.getSelectedItem().toString(), form.statusInput.getSelectedItem().toString());
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                model.save(event);
            });
            if (existing != null) dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(view ->
                    new AlertDialog.Builder(this).setTitle(R.string.delete_title)
                            .setMessage(getString(R.string.delete_message, existing.name))
                            .setNegativeButton(R.string.cancel, null)
                            .setPositiveButton(R.string.delete, (confirmation, button) -> model.delete(existing)).show());
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!model.isBusy());
        });
        dialog.show();
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        if (editorFields != null) {
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
