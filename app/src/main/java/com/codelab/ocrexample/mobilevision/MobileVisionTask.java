package com.codelab.ocrexample.mobilevision;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import com.codelab.ocrexample.FieldsParsing;
import com.codelab.ocrexample.Utils;
import com.codelab.ocrexample.data.model.Card;
import com.codelab.ocrexample.data.model.CardFields;
import com.codelab.ocrexample.data.model.Field;
import com.codelab.ocrexample.data.model.FieldDB;
import com.codelab.ocrexample.data.model.Item;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mohamed Habib on 30/08/2017.
 */

public class MobileVisionTask {

    public static class CalculateOCR extends AsyncTask<String, Void, String> {
        List<String> ocrResultWordList = new ArrayList<>();
        private AsyncTaskListener asyncTaskListener;
        Context context;

        public CalculateOCR(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            asyncTaskListener.onPreExecute();
        }

        protected String doInBackground(String... args) {
            StringBuilder ocrResultLines = getOCRResult(context, args[0], ocrResultWordList);

            return ocrResultLines.toString();
        }

        @Override
        protected void onPostExecute(String ocrResult) {
            asyncTaskListener.onPostExecute(ocrResult);
        }

        public void setAsyncTaskListener(AsyncTaskListener asyncTaskListener) {
            this.asyncTaskListener = asyncTaskListener;
        }


    }

    /**
     * gets the OCR result of the images at the given directory path
     * and saves them in the db automatically
     */
    public static class BulkCalculateOCR extends AsyncTask<String, Void, String> {
        private AsyncTaskListener asyncTaskListener;
        Context context;

        public BulkCalculateOCR(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            asyncTaskListener.onPreExecute();
        }

        @Override
        protected String doInBackground(String... args) {
            String path = args[0];
            File file = new File(path);
            if (file.isDirectory()) {
                for (File imgFile : file.listFiles()) {
                    if (!imgFile.isDirectory()) {
                        String imagePath = imgFile.getPath();
                        StringBuilder ocrResultLines = getOCRResult(context, imagePath, null);
                        List<Field> fieldList = FieldsParsing.parseOCRResult(ocrResultLines.toString());

                        CardFields cardFields = new CardFields();
                        for (Field field : fieldList) {
                            cardFields.createField(field.getTypeValue(), field.getLine());
                        }
                        Card card = new Card(imagePath, ocrResultLines.toString(), "", "");
                        card.insert();
                        for (String key : cardFields.getKeys()) {
                            FieldDB field = new FieldDB(key, card.getId());
                            field.insert();
                            for (String item : cardFields.getValues(key)) {
                                Item itemi = new Item(item, field.getID());
                                itemi.insert();
                            }
                        }

                    }
                }
            }

            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            asyncTaskListener.onPostExecute(s);
        }

        public void setAsyncTaskListener(AsyncTaskListener asyncTaskListener) {
            this.asyncTaskListener = asyncTaskListener;
        }
    }

    /**
     * @param ocrResultWordList: result is passed here by reference
     * @param imagePath
     * @return string containing the result in lines separated by 2 "\n" and also returns list of words to the the @param ocrResultWordList
     */
    @NonNull
    private static StringBuilder getOCRResult(Context context, String imagePath, List<String> ocrResultWordList) {
        Bitmap imageBitmap = Utils.getBitmap(imagePath);
        StringBuilder ocrResultLines = new StringBuilder();
        StringBuilder ocrResultWords = new StringBuilder();
        if (imageBitmap != null) {
            TextRecognizer textRecognizer = null;
            try {
                textRecognizer = new TextRecognizer.Builder(context).build();
                SparseArray<TextBlock> textBlocks = textRecognizer.detect(new Frame.Builder().setBitmap(imageBitmap).build());
                for (int i = 0; i < textBlocks.size(); i++) {
                    String value = textBlocks.valueAt(i).getValue();
                    ocrResultLines.append(value).append("\n").append("\n");
                    for (int j = 0; j < textBlocks.valueAt(i).getComponents().size(); j++) {
                        String word = textBlocks.valueAt(i).getComponents().get(j).getValue();
                        if (ocrResultWordList != null)
                            ocrResultWordList.add(word);
                        ocrResultWords.append(word).append(",");
                    }
                }
            } catch (Exception ignore) {
            } finally {
                if (textRecognizer != null)
                    textRecognizer.release();
            }
        }
        Log.d("WORDS", ocrResultWords.toString());
        return ocrResultLines;
    }

}
