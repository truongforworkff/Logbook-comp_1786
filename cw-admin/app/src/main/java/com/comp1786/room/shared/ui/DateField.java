package com.comp1786.room.shared.ui;

import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.comp1786.room.R;
import com.comp1786.room.databinding.SharedPopupBinding;
import com.comp1786.room.features.events.logic.EventValidation;
import java.time.LocalDate;
import java.time.ZoneId;

public final class DateField {
    private final AppCompatActivity activity;
    private final AppUi ui;
    private final EditText field;
    private final boolean optional;

    public DateField(AppCompatActivity activity, AppUi ui, EditText field, boolean optional) {
        this.activity = activity;
        this.ui = ui;
        this.field = field;
        this.optional = optional;
        field.setShowSoftInputOnFocus(false);
        field.setCursorVisible(false);
        field.setHint(R.string.choose_date_hint);
        field.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_calendar, 0);
        field.setCompoundDrawablePadding(ui.dp(10));
        field.setOnClickListener(view -> show());
    }

    public void show() {
        String entered = field.getText().toString().trim();
        LocalDate initial = EventValidation.validDate(entered)
                ? LocalDate.parse(EventValidation.storageDate(entered)) : LocalDate.now();
        LocalDate[] selected = {initial};
        SharedPopupBinding panel = SharedPopupBinding.inflate(activity.getLayoutInflater());
        panel.title.setText(R.string.pick_date);
        panel.subtitle.setText(R.string.date_subtitle);
        panel.body.addView(createCalendar(initial, selected), new LinearLayout.LayoutParams(-1, ui.dp(320)));
        panel.primary.setText(R.string.use_date);
        panel.secondary.setText(R.string.cancel);
        if (optional) {
            panel.tertiary.setText(R.string.clear_date);
            panel.tertiary.setVisibility(View.VISIBLE);
        }
        AlertDialog picker = ui.openPopup(panel);
        panel.secondary.setOnClickListener(view -> picker.dismiss());
        panel.primary.setOnClickListener(view -> {
            setValue(EventValidation.displayDate(selected[0].toString()));
            picker.dismiss();
        });
        panel.tertiary.setOnClickListener(view -> {
            setValue("");
            picker.dismiss();
        });
    }

    private CalendarView createCalendar(LocalDate initial, LocalDate[] selected) {
        CalendarView calendar = new CalendarView(activity);
        ZoneId zone = ZoneId.systemDefault();
        calendar.setMinDate(LocalDate.of(1900, 1, 1).atStartOfDay(zone).toInstant().toEpochMilli());
        calendar.setMaxDate(LocalDate.of(2100, 12, 31).atStartOfDay(zone).toInstant().toEpochMilli());
        calendar.setDate(initial.atStartOfDay(zone).toInstant().toEpochMilli());
        calendar.setOnDateChangeListener((view, year, month, day) ->
                selected[0] = LocalDate.of(year, month + 1, day));
        return calendar;
    }

    private void setValue(String value) {
        field.setText(value);
        ui.inlineValidation(field, true, R.string.invalid_date);
    }
}
