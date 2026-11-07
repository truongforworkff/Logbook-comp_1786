package com.comp1786.room.shared.data;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import java.io.InputStream;
import java.io.IOException;

public final class PhotoDecoder {
    private PhotoDecoder() {}
    public static Bitmap decode(ContentResolver resolver, Uri uri) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            try (InputStream input = resolver.openInputStream(uri)) {
                BitmapFactory.decodeStream(input, null, bounds);
            }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            while (bounds.outWidth / options.inSampleSize > 1024
                    || bounds.outHeight / options.inSampleSize > 1024) options.inSampleSize *= 2;
            Bitmap bitmap;
            try (InputStream input = resolver.openInputStream(uri)) {
                bitmap = BitmapFactory.decodeStream(input, null, options);
            }
            if (bitmap == null) return null;
            return orient(resolver, uri, bitmap);
        } catch (IOException | SecurityException | IllegalArgumentException error) {
            return null;
        }
    }

    private static Bitmap orient(ContentResolver resolver, Uri uri, Bitmap bitmap) {
        int orientation = ExifInterface.ORIENTATION_NORMAL;
        try (InputStream input = resolver.openInputStream(uri)) {
            orientation = new ExifInterface(input).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (IOException | IllegalArgumentException ignored) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        switch (orientation) {
            case 2: matrix.setScale(-1, 1); break;
            case 3: matrix.setRotate(180); break;
            case 4: matrix.setScale(1, -1); break;
            case 5: matrix.setRotate(90); matrix.postScale(-1, 1); break;
            case 6: matrix.setRotate(90); break;
            case 7: matrix.setRotate(270); matrix.postScale(-1, 1); break;
            case 8: matrix.setRotate(270); break;
            default: return bitmap;
        }
        Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (rotated != bitmap) bitmap.recycle();
        return rotated;
    }
}
