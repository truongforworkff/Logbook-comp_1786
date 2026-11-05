package com.comp1786.room.features.events.ui.editor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import com.comp1786.room.R;
import com.comp1786.room.databinding.EventEditorScreenBinding;
import com.comp1786.room.databinding.SharedPopupBinding;
import com.comp1786.room.features.events.data.model.Event;
import com.comp1786.room.features.events.logic.EventValidation;
import com.comp1786.room.features.events.logic.EventViewModel;
import com.comp1786.room.features.events.ui.list.EventAdapter;
import com.comp1786.room.shared.ui.AppUi;
import java.util.Arrays;
public final class EventEditor {
    private final AppCompatActivity activity;
    private final AppUi ui;
    private final EventViewModel model;
    private AlertDialog dialog;
    private EventEditorScreenBinding fields;
    private Event editingEvent;
    private boolean deleting;
    public EventEditor(AppCompatActivity activity,AppUi ui,EventViewModel model) {
        this.activity=activity; this.ui=ui; this.model=model;
    }
    public void restoreState(Bundle state) {
        if (state != null && state.getBoolean("editorOpen")) {
            Event draft = new Event(state.getLong("eventId"), state.getString("name", ""),
                    state.getString("date", ""), state.getString("location", ""),
                    state.getString("organiser", ""), state.getString("type", ""), state.getString("status", "Upcoming"));
            draft.description = state.getString("description", "");
            draft.contactEmail = state.getString("email", "");
            draft.contactPhone = state.getString("phone", "");
            show(draft.id == 0 ? null : draft, draft);
        }
    }
    public boolean wasDeleting() { return deleting; }
    public android.view.Window window() { return dialog!=null && dialog.isShowing()?dialog.getWindow():null; }
    public void close() { if(dialog!=null) dialog.dismiss(); }
    public void enableActions() { if(fields!=null) { fields.reviewAction.setEnabled(true); fields.deleteAction.setEnabled(true); } }
    public void show(Event existing, Event draft) {
        EventEditorScreenBinding form = EventEditorScreenBinding.inflate(activity.getLayoutInflater());
        fields = form;
        deleting = false;
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
            form.typeInput.setSelection(Math.max(0, Arrays.asList(activity.getResources().getStringArray(R.array.types)).indexOf(draft.type)));
            form.statusInput.setSelection(Math.max(0, Arrays.asList(activity.getResources().getStringArray(R.array.statuses)).indexOf(draft.status)));
        }
        com.comp1786.room.shared.ui.DateField datePicker =
                new com.comp1786.room.shared.ui.DateField(activity, ui, form.dateInput, false);
        form.pickDate.setOnClickListener(view -> datePicker.show());
        form.moreOptions.setOnClickListener(view -> {
            boolean expand = form.optionalFields.getVisibility() != View.VISIBLE;
            form.optionalFields.setVisibility(expand ? View.VISIBLE : View.GONE);
            form.moreOptions.setText(expand ? R.string.less_options : R.string.more_options);
        });
        if (draft != null && (!draft.description.isEmpty() || !draft.contactEmail.isEmpty() || !draft.contactPhone.isEmpty())) {
            form.optionalFields.setVisibility(View.VISIBLE); form.moreOptions.setText(R.string.less_options);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setView(form.getRoot());
        form.deleteAction.setVisibility(existing == null ? View.GONE : View.VISIBLE);
        AlertDialog dialog = builder.create();
        this.dialog = dialog;
        form.back.setOnClickListener(view -> dialog.dismiss());
        dialog.setOnDismissListener(ignored -> { this.dialog = null; fields = null; editingEvent = null; });
        dialog.setOnShowListener(ignored -> {
            form.reviewAction.setOnClickListener(view -> {
                EditText firstInvalid=null;
                for(EditText field:new EditText[]{form.nameInput,form.locationInput,form.organiserInput}) {
                    if(!ui.inlineValidation(field,EventValidation.validText(field.getText().toString()),R.string.required) && firstInvalid==null) firstInvalid=field;
                }
                String date=form.dateInput.getText().toString().trim();
                if(!ui.inlineValidation(form.dateInput,EventValidation.validDate(date),R.string.invalid_date) && firstInvalid==null) firstInvalid=form.dateInput;
                String email=form.emailInput.getText().toString().trim(), phone=form.phoneInput.getText().toString().trim();
                if(!ui.inlineValidation(form.emailInput,EventValidation.validEmail(email),R.string.invalid_email) && firstInvalid==null) firstInvalid=form.emailInput;
                if(!ui.inlineValidation(form.phoneInput,EventValidation.validPhone(phone),R.string.invalid_phone) && firstInvalid==null) firstInvalid=form.phoneInput;
                if(firstInvalid!=null) {
                    firstInvalid.requestFocus();
                    if(firstInvalid==form.emailInput || firstInvalid==form.phoneInput) {
                        form.optionalFields.setVisibility(View.VISIBLE); form.moreOptions.setText(R.string.less_options);
                    }
                    ui.showNotice(dialog.getWindow(),activity.getString(R.string.check_fields_notice),true); return;
                }
                Event event = new Event(existing == null ? 0 : existing.id,
                        form.nameInput.getText().toString().trim(), EventValidation.storageDate(date),
                        form.locationInput.getText().toString().trim(),
                        form.organiserInput.getText().toString().trim(), form.typeInput.getSelectedItem().toString(), form.statusInput.getSelectedItem().toString());
                event.description = form.descriptionInput.getText().toString().trim();
                event.contactEmail = email;
                event.contactPhone = phone;
                SharedPopupBinding panel=SharedPopupBinding.inflate(activity.getLayoutInflater());
                panel.title.setText(R.string.review_title); panel.subtitle.setText(R.string.review_subtitle);
                ui.reviewRow(panel.body,R.string.name,event.name); ui.reviewRow(panel.body,R.string.date,date);
                ui.reviewRow(panel.body,R.string.location,event.location); ui.reviewRow(panel.body,R.string.organiser,event.organiser);
                ui.reviewRow(panel.body,R.string.type,event.type); ui.reviewRow(panel.body,R.string.status,EventAdapter.statusLabel(activity,event.status));
                if(!event.description.isEmpty()) ui.reviewRow(panel.body,R.string.description,event.description);
                if(!email.isEmpty()) ui.reviewRow(panel.body,R.string.email,email);
                if(!phone.isEmpty()) ui.reviewRow(panel.body,R.string.phone,phone);
                panel.primary.setText(R.string.save); panel.secondary.setText(R.string.back_edit);
                AlertDialog review=ui.openPopup(panel);
                panel.secondary.setOnClickListener(button->review.dismiss());
                panel.primary.setOnClickListener(button->{
                    panel.primary.setEnabled(false); form.reviewAction.setEnabled(false);
                    deleting=false; review.dismiss(); model.save(event);
                });
            });
            if(existing!=null) form.deleteAction.setOnClickListener(view->{
                SharedPopupBinding panel=SharedPopupBinding.inflate(activity.getLayoutInflater());
                panel.title.setText(R.string.delete_title); panel.subtitle.setText(R.string.delete_with_sessions);
                ui.reviewRow(panel.body,R.string.name,existing.name);
                panel.primary.setText(R.string.delete); panel.secondary.setText(R.string.keep_event);
                AlertDialog confirmation=ui.openPopup(panel);
                panel.secondary.setOnClickListener(button->confirmation.dismiss());
                panel.primary.setOnClickListener(button->{
                    deleting=true; confirmation.dismiss(); form.reviewAction.setEnabled(false); form.deleteAction.setEnabled(false);
                    model.delete(existing);
                });
            });
            form.reviewAction.setEnabled(!model.isBusy());
        });
        dialog.show();
        ui.fullScreen(dialog,form.getRoot());
    }

    public void saveState(Bundle state) {
        if (fields != null) {
            state.putString("description", fields.descriptionInput.getText().toString());
            state.putString("email", fields.emailInput.getText().toString());
            state.putString("phone", fields.phoneInput.getText().toString());
            state.putBoolean("editorOpen", true);
            state.putLong("eventId", editingEvent == null ? 0 : editingEvent.id);
            state.putString("name", fields.nameInput.getText().toString());
            state.putString("date", fields.dateInput.getText().toString());
            state.putString("location", fields.locationInput.getText().toString());
            state.putString("organiser", fields.organiserInput.getText().toString());
            state.putString("type", fields.typeInput.getSelectedItem().toString());
            state.putString("status", fields.statusInput.getSelectedItem().toString());
        }
    }
}
