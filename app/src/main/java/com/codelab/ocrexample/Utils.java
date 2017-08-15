package com.codelab.ocrexample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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
}
