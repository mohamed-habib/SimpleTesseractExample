package com.codelab.ocrexample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by Mohamed Habib on 15/08/2017.
 */

public class Utils {
//    public static Bitmap getBitmap(String filePath) {
//        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
//        Bitmap bitmap = BitmapFactory.decodeFile(filePath, bmOptions);
//        bitmap = Bitmap.createBitmap(bitmap);
//        return bitmap;
//    }

    public static Bitmap getBitmap(String fullPath) {

        int targetW = 1000; //your required width
        int targetH = 2000; //your required height

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fullPath, bmOptions);

        int scaleFactor = 1;
        scaleFactor = calculateInSampleSize(bmOptions, targetW, targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor * 2;
        bmOptions.inPurgeable = true;

        return BitmapFactory.decodeFile(fullPath, bmOptions);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

    public static String bitmapToBase64(Bitmap bitmap)
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();

        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

        return encoded;
    }

    public static void copyFile(File src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }
}
