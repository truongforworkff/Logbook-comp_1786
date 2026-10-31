package com.comp1786.room.features.events.ui.list;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import com.comp1786.room.R;
import com.comp1786.room.databinding.SharedPopupBinding;
import com.comp1786.room.features.events.logic.EventValidation;
import com.comp1786.room.shared.ui.AppUi;
public final class EventFilterDialog {
    public interface Listener { void apply(String date,String organiser,boolean clearSearch); }
    private final AppCompatActivity activity;
    private final AppUi ui;
    public EventFilterDialog(AppCompatActivity activity,AppUi ui) { this.activity=activity; this.ui=ui; }
    public void show(String dateFilter,String organiserFilter,Listener listener) {
        SharedPopupBinding panel = SharedPopupBinding.inflate(activity.getLayoutInflater());
        panel.title.setText(R.string.filter_title); panel.subtitle.setText(R.string.filter_subtitle);
        EditText date = ui.filterField(panel.body,activity.getString(R.string.filter_date),dateFilter.isEmpty() ? "" : EventValidation.displayDate(dateFilter));
        date.setHint(R.string.date_hint); date.setInputType(android.text.InputType.TYPE_CLASS_DATETIME | android.text.InputType.TYPE_DATETIME_VARIATION_DATE);
        EditText organiser = ui.filterField(panel.body,activity.getString(R.string.filter_organiser),organiserFilter);
        panel.primary.setText(R.string.apply_filter); panel.secondary.setText(R.string.cancel);
        panel.tertiary.setText(R.string.clear_filter); panel.tertiary.setVisibility(View.VISIBLE);
        AlertDialog filter = ui.openPopup(panel);
        panel.primary.setOnClickListener(view -> {
            String entered = date.getText().toString().trim();
            if (!ui.inlineValidation(date,entered.isEmpty() || EventValidation.validDate(entered),R.string.invalid_date)) { date.requestFocus(); return; }
            listener.apply(entered.isEmpty() ? "" : EventValidation.storageDate(entered),organiser.getText().toString().trim(),false);
            filter.dismiss();
        });
        panel.secondary.setOnClickListener(view -> filter.dismiss());
        panel.tertiary.setOnClickListener(view -> {
            listener.apply("","",true); filter.dismiss();
        });
    }

}
