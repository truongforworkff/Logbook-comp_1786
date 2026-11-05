package com.comp1786.room.shared.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import com.comp1786.room.databinding.SharedSessionSearchBinding;
import java.util.function.Consumer;

public final class SessionSearchField {
    private SessionSearchField() {}

    public static void bind(SharedSessionSearchBinding field, Consumer<String> onChange) {
        field.clearQuery.setOnClickListener(view -> field.query.setText(""));
        field.query.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence text, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable text) {}
            @Override public void onTextChanged(CharSequence text, int start, int before, int count) {
                field.clearQuery.setVisibility(text.length() == 0 ? View.GONE : View.VISIBLE);
                onChange.accept(text.toString());
            }
        });
    }
}
