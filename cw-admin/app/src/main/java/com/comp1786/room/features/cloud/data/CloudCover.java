package com.comp1786.room.features.cloud.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Base64;
import com.comp1786.room.shared.data.PhotoDecoder;
import java.io.ByteArrayOutputStream;

public final class CloudCover {
    private CloudCover() {}

    public static String thumbnail(Context context, String uri) {
        if (uri.isEmpty() || uri.startsWith(PhotoDecoder.CLOUD_THUMBNAIL_PREFIX)) return uri;
        Bitmap original = PhotoDecoder.decode(context.getContentResolver(), Uri.parse(uri));
        if (original == null) throw new IllegalArgumentException("Could not read the cover image. Choose another image or remove it before syncing.");
        Bitmap small = original;
        try {
            for (int edge : new int[]{640, 480, 320, 160}) {
                if (small != original) small.recycle();
                float ratio = Math.min(1f, (float) edge / Math.max(original.getWidth(), original.getHeight()));
                small = Bitmap.createScaledBitmap(original, Math.max(1, Math.round(original.getWidth() * ratio)),
                        Math.max(1, Math.round(original.getHeight() * ratio)), true);
                for (int quality : new int[]{75, 55, 35, 20}) {
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    if (!small.compress(Bitmap.CompressFormat.JPEG, quality, bytes))
                        throw new IllegalArgumentException("Could not compress the cover image.");
                    if (bytes.size() <= 32 * 1024)
                        return PhotoDecoder.CLOUD_THUMBNAIL_PREFIX + Base64.encodeToString(bytes.toByteArray(), Base64.NO_WRAP);
                }
            }
            throw new IllegalArgumentException("The thumbnail exceeds the sync size limit.");
        } finally {
            if (small != original) small.recycle();
            original.recycle();
        }
    }
}
