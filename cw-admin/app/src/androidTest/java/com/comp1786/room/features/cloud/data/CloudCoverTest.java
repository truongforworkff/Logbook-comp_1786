package com.comp1786.room.features.cloud.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class CloudCoverTest {
    @Test public void optionalPhotoCompressesToBoundedJpegAndMissingFileIsReported() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("", CloudCover.thumbnail(context, ""));
        File image = new File(context.getCacheDir(), "cloud-cover-test.png");
        Bitmap bitmap = Bitmap.createBitmap(1600, 1200, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(0xffcc8899);
        try (OutputStream stream = new FileOutputStream(image)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        } finally { bitmap.recycle(); }
        try {
            String encoded = CloudCover.thumbnail(context, android.net.Uri.fromFile(image).toString());
            assertTrue(encoded.startsWith("data:image/jpeg;base64,"));
            byte[] bytes = Base64.decode(encoded.substring(23), Base64.DEFAULT);
            assertTrue(bytes.length <= 32768);
            assertEquals(255, bytes[0] & 255);
            assertEquals(216, bytes[1] & 255);
        } finally { image.delete(); }
        try {
            CloudCover.thumbnail(context, android.net.Uri.fromFile(image).toString());
            fail("A missing photo must not silently erase a published cover");
        } catch (IllegalArgumentException expected) { }
    }
}
