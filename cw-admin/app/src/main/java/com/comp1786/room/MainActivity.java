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
        EventAdapter adapter = new EventAdapter(this::showDetails);
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
            draft.description = state.getString("description", "");
            draft.contactEmail = state.getString("email", "");
            draft.contactPhone = state.getString("phone", "");
            showEditor(draft.id == 0 ? null : draft, draft);
        }
        model.getOperation().observe(this, operation -> {
            if (operation == EventViewModel.Operation.DUPLICATE) {
                Toast.makeText(this, R.string.duplicate_event, Toast.LENGTH_LONG).show();
                if (editor != null) editor.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                model.consumeOperation();
            } else if (operation == EventViewModel.Operation.ERROR) {
                Toast.makeText(this, R.string.storage_error, Toast.LENGTH_LONG).show();
                if (editor != null) editor.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                model.consumeOperation();
            } else if (operation == EventViewModel.Operation.SUCCESS) {
                if (editor != null) editor.dismiss();
                model.consumeOperation();
            }
        });

    }
    private void showDetails(Event event) {
        String text = getString(R.string.review_details, event.name, EventValidation.displayDate(event.date), event.location, event.organiser, event.type, event.status, event.description, event.contactEmail, event.contactPhone);
        new AlertDialog.Builder(this).setTitle(event.name).setMessage(text)
            .setPositiveButton(R.string.edit, (d, w) -> showEditor(event, event))
            .setNegativeButton(R.string.close, null).show();
    }
    private void showEditor(Event existing, Event draft) {
        DialogEventBinding form = DialogEventBinding.inflate(getLayoutInflater());
        editorFields = form;
        editingEvent = existing;
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
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        model.save(event);
                    }).show();
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
