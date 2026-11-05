package com.comp1786.room.features.events.ui.list;

import com.comp1786.room.features.events.logic.EventCategories;
import com.comp1786.room.shared.ui.DateField;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import com.comp1786.room.R;
import com.comp1786.room.databinding.SharedPopupBinding;
import com.comp1786.room.features.events.logic.EventValidation;
import com.comp1786.room.shared.ui.AppUi;
public final class EventFilterDialog {
    public interface Listener { void apply(String date,String organiser,String category,boolean clearSearch); }
    private final AppCompatActivity activity;
    private final AppUi ui;
    public EventFilterDialog(AppCompatActivity activity,AppUi ui) { this.activity=activity; this.ui=ui; }
    public void show(String dateFilter,String organiserFilter,String categoryFilter,Listener listener) {
        SharedPopupBinding panel = SharedPopupBinding.inflate(activity.getLayoutInflater());
        panel.title.setText(R.string.filter_title); panel.subtitle.setText(R.string.filter_subtitle);
        EditText date = ui.filterField(panel.body,activity.getString(R.string.filter_date),dateFilter.isEmpty() ? "" : EventValidation.displayDate(dateFilter),R.id.filterDate);
        new DateField(activity, ui, date, true);
        EditText organiser = ui.filterField(panel.body,activity.getString(R.string.filter_organiser),organiserFilter,R.id.filterOrganiser);
        Spinner category = categoryField(panel.body, categoryFilter);
        panel.primary.setText(R.string.apply_filter); panel.secondary.setText(R.string.cancel);
        panel.tertiary.setText(R.string.clear_filter); panel.tertiary.setVisibility(View.VISIBLE);
        AlertDialog filter = ui.openPopup(panel);
        panel.primary.setOnClickListener(view -> {
            String entered = date.getText().toString().trim();
            if (!ui.inlineValidation(date,entered.isEmpty() || EventValidation.validDate(entered),R.string.invalid_date)) { date.requestFocus(); return; }
            listener.apply(entered.isEmpty() ? "" : EventValidation.storageDate(entered),organiser.getText().toString().trim(),category.getSelectedItemPosition() == 0 ? "" : EventCategories.CODES.get(category.getSelectedItemPosition()-1),false);
            filter.dismiss();
        });
        panel.secondary.setOnClickListener(view -> filter.dismiss());
        panel.tertiary.setOnClickListener(view -> {
            listener.apply("","","",true); filter.dismiss();
        });
    }
    private Spinner categoryField(LinearLayout parent, String selected) {
        TextView label = new TextView(activity);
        label.setText(R.string.event_category);
        label.setTextColor(activity.getColor(R.color.muted));
        parent.addView(label);
        java.util.List<String> choices = new ArrayList<>();
        choices.add(activity.getString(R.string.all_categories));
        choices.addAll(Arrays.asList(activity.getResources().getStringArray(R.array.event_categories)));
        Spinner spinner = new Spinner(activity);
        spinner.setId(R.id.filterCategory);
        spinner.setBackgroundResource(R.drawable.event_card);
        spinner.setPadding(ui.dp(12), 0, ui.dp(12), 0);
        spinner.setAdapter(new ArrayAdapter<>(activity,
                android.R.layout.simple_spinner_dropdown_item, choices));
        spinner.setSelection(selected.isEmpty() ? 0
                : EventCategories.position(selected) + 1);
        label.setLabelFor(spinner.getId());
        parent.addView(spinner, new LinearLayout.LayoutParams(-1, ui.dp(52)));
        return spinner;
    }
}

