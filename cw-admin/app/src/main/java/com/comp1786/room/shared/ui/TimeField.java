package com.comp1786.room.shared.ui;

import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TimePicker;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.comp1786.room.R;
import com.comp1786.room.databinding.SharedPopupBinding;
import com.comp1786.room.features.sessions.logic.SessionValidation;
import java.util.Locale;

public final class TimeField {
    public TimeField(AppCompatActivity activity, AppUi ui, EditText field) {
        field.setShowSoftInputOnFocus(false);
        field.setCursorVisible(false);
        field.setHint(R.string.choose_time_hint);
        field.setOnClickListener(view -> show(activity, ui, field));
    }

    private void show(AppCompatActivity activity, AppUi ui, EditText field) {
        String value = field.getText().toString();
        TimePicker clock = new TimePicker(activity);
        clock.setIs24HourView(true);
        clock.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        clock.setHour(SessionValidation.validTime(value) ? Integer.parseInt(value.substring(0, 2)) : 9);
        clock.setMinute(SessionValidation.validTime(value) ? Integer.parseInt(value.substring(3)) : 0);
        SharedPopupBinding panel = SharedPopupBinding.inflate(activity.getLayoutInflater());
        panel.title.setText(R.string.pick_time);
        panel.subtitle.setText(R.string.pick_time_hint);
        panel.body.addView(clock);
        panel.primary.setText(R.string.use_time);
        panel.secondary.setText(R.string.cancel);
        AlertDialog picker = ui.openPopup(panel);
        panel.secondary.setOnClickListener(view -> picker.dismiss());
        panel.primary.setOnClickListener(view -> {
            field.setText(String.format(Locale.ROOT, "%02d:%02d", clock.getHour(), clock.getMinute()));
            ui.inlineValidation(field, true, R.string.session_invalid_time);
            picker.dismiss();
        });
    }
}
