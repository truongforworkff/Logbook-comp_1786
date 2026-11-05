package com.comp1786.room.features.events.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.File;
import java.io.FileOutputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class EventPhotoTest {
    @Test public void largePhotoIsSampledAndMissingPhotoIsSafe() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File file = new File(context.getCacheDir(), "event-cover-test.jpg");
        Bitmap source = Bitmap.createBitmap(2400, 1200, Bitmap.Config.ARGB_8888);
        try {
            try (FileOutputStream output = new FileOutputStream(file)) {
                assertTrue(source.compress(Bitmap.CompressFormat.JPEG, 85, output));
            }
            android.media.ExifInterface exif = new android.media.ExifInterface(file.getPath());
            exif.setAttribute(android.media.ExifInterface.TAG_ORIENTATION, "6");
            exif.saveAttributes();
            Bitmap result = EventPhoto.decode(context.getContentResolver(), Uri.fromFile(file));
            assertNotNull(result);
            assertTrue(result.getWidth() <= 1024 && result.getHeight() <= 1024);
            assertEquals(result.getWidth() * 2, result.getHeight());
            result.recycle();
        } finally {
            source.recycle();
            file.delete();
        }
        assertNull(EventPhoto.decode(context.getContentResolver(), Uri.fromFile(file)));
        try (FileOutputStream output = new FileOutputStream(file)) { output.write(new byte[]{1, 2, 3}); }
        try { assertNull(EventPhoto.decode(context.getContentResolver(), Uri.fromFile(file))); }
        finally { file.delete(); }
    }
}
