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
    public static final String CLOUD_THUMBNAIL_PREFIX = "data:image/jpeg;base64,";

    private PhotoDecoder() {}

    public static Bitmap decodeCloudThumbnail(String value) {
        if (!value.startsWith(CLOUD_THUMBNAIL_PREFIX) || value.length() > 43715) return null;
        try {
            byte[] bytes = android.util.Base64.decode(
                    value.substring(CLOUD_THUMBNAIL_PREFIX.length()), android.util.Base64.DEFAULT);
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bounds);
            if (!"image/jpeg".equals(bounds.outMimeType) || bounds.outWidth <= 0 || bounds.outHeight <= 0
                    || bounds.outWidth > 640 || bounds.outHeight > 640 || bytes.length > 32 * 1024) return null;
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (IllegalArgumentException invalid) {
            return null;
        }
    }
    public static Bitmap decode(ContentResolver resolver, Uri uri) {
        if ("data".equals(uri.getScheme())) return decodeCloudThumbnail(uri.toString());
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
