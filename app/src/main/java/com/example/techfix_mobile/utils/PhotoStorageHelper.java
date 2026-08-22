package com.example.techfix_mobile.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PhotoStorageHelper {

    // Keeps the encoded string safely under Firestore's 1MB document limit
    private static final int MAX_DIMENSION = 800;
    private static final int JPEG_QUALITY = 60;

    public static String encodePhotoAsBase64(Context context, Uri photoUri) throws IOException {
        Bitmap original = decodeSampledBitmap(context, photoUri);
        Bitmap resized = resizeIfNeeded(original);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, baos);
        byte[] bytes = baos.toByteArray();

        return "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    private static Bitmap decodeSampledBitmap(Context context, Uri uri) throws IOException {
        InputStream input = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input);
        if (input != null) input.close();
        if (bitmap == null) throw new IOException("Could not decode photo");
        return bitmap;
    }

    private static Bitmap resizeIfNeeded(Bitmap original) {
        int width = original.getWidth();
        int height = original.getHeight();

        if (width <= MAX_DIMENSION && height <= MAX_DIMENSION) {
            return original;
        }

        float ratio = Math.min(
                (float) MAX_DIMENSION / width,
                (float) MAX_DIMENSION / height
        );
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(original, newWidth, newHeight, true);
    }
}