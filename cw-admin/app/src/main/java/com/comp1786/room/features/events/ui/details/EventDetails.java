package com.comp1786.room.features.events.ui.details;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import com.comp1786.room.R;
import com.comp1786.room.databinding.EventDetailsScreenBinding;
import com.comp1786.room.features.events.data.model.Event;
import com.comp1786.room.features.events.ui.list.EventAdapter;
import com.comp1786.room.shared.ui.AppUi;
public final class EventDetails {
    private final AppCompatActivity activity;
    private final AppUi ui;
    private final java.util.function.Consumer<Event> onEdit;
    public EventDetails(AppCompatActivity activity,AppUi ui,java.util.function.Consumer<Event> onEdit) {
        this.activity=activity; this.ui=ui; this.onEdit=onEdit;
    }
    public void show(Event event) {
        EventDetailsScreenBinding page = EventDetailsScreenBinding.inflate(activity.getLayoutInflater());
        page.title.setText(event.name); page.category.setText(event.type+" · "+EventAdapter.statusLabel(activity,event.status));
        page.date.setText(EventValidation.displayDate(event.date)); page.location.setText(event.location);
        page.organiser.setText(activity.getString(R.string.organised_by,event.organiser));
        page.description.setText(event.description.isEmpty()?activity.getString(R.string.no_description):event.description);
        page.contacts.setVisibility(event.contactEmail.isEmpty() && event.contactPhone.isEmpty()?View.GONE:View.VISIBLE);
        page.email.setText(event.contactEmail); page.email.setVisibility(event.contactEmail.isEmpty()?View.GONE:View.VISIBLE);
        page.phone.setText(event.contactPhone); page.phone.setVisibility(event.contactPhone.isEmpty()?View.GONE:View.VISIBLE);
        AlertDialog details = new AlertDialog.Builder(activity).setView(page.getRoot()).create();
        page.editAction.setOnClickListener(view -> { details.dismiss(); onEdit.accept(event); });
        page.back.setOnClickListener(view->details.dismiss());
        details.show(); ui.fullScreen(details,page.getRoot());
    }

}
