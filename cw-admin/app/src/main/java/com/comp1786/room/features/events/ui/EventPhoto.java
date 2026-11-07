package com.comp1786.room.features.events.ui;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public final class EventPhoto {
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private EventPhoto() {}

    public static void load(ImageView target, String value, Consumer<Boolean> completed) {
        target.setTag(value);
        target.setImageDrawable(null);
        target.setVisibility(value.isEmpty() ? View.GONE : View.VISIBLE);
        if (value.isEmpty()) return;
        ContentResolver resolver = target.getContext().getApplicationContext().getContentResolver();
        WORKER.execute(() -> {
            Bitmap bitmap = com.comp1786.room.shared.data.PhotoDecoder.decode(resolver, Uri.parse(value));
            MAIN.post(() -> {
                if (!value.equals(target.getTag())) return;
                target.setImageBitmap(bitmap);
                target.setVisibility(bitmap == null ? View.GONE : View.VISIBLE);
                if (completed != null) completed.accept(bitmap != null);
            });
        });
    }

}
