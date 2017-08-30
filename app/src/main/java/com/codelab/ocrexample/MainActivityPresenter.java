package com.codelab.ocrexample;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.codelab.ocrexample.data.APIs;
import com.codelab.ocrexample.data.model.Card;
import com.codelab.ocrexample.data.model.CardFields;
import com.codelab.ocrexample.data.model.Field;
import com.codelab.ocrexample.mobilevision.AsyncTaskListener;
import com.codelab.ocrexample.mobilevision.MobileVisionTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by Mohamed Habib on 30/08/2017.
 */

public class MainActivityPresenter implements MainActivityContractor.Presenter {
    Context context;
    private MainActivityContractor.View view;

    public MainActivityPresenter(Context context, MainActivityContractor.View view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void calculateBulkMobileVisionOCR() {
        MobileVisionTask.BulkCalculateOCR bulkCalculateOCR = new MobileVisionTask.BulkCalculateOCR(context);
        bulkCalculateOCR.setAsyncTaskListener(new AsyncTaskListener() {
            @Override
            public void onPreExecute() {
                view.showProgressBar();
            }

            public void onPostExecute(String result) {
                view.hideProgressBar();
            }
        });
        bulkCalculateOCR.execute(view.getSelectedDirectory());

    }

    @Override
    public void imageSelected() {
        try {
            if (view.getImagePath() != null) {
                File src = new File(view.getImagePath());
                Utils.copyFile(new File(view.getImagePath()), new File(Environment.getExternalStorageDirectory() + "/OCR/" + src.getName()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (view.getImagePath() != null) {
            view.updateImageView(Utils.getBitmap(view.getImagePath()));
        }
        view.hideStatusText();
    }

    private boolean hasImageCaptured() {
        return view.getImagePath() != null && !TextUtils.isEmpty(view.getImagePath());
    }

    @Override
    public void executeGoogleCloudOCR() {
        List<String> languages = Arrays.asList("en");
        if (hasImageCaptured()) {
            Bitmap imageBitmap = Utils.getBitmap(view.getImagePath());
            String base64Image = Utils.bitmapToBase64(imageBitmap);

            if (NetworkUtilies.isConnectingToInternet(context)) {
                view.showProgressBar();

                APIs.callGoogleCloudOCRAPI(context, languages, base64Image, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        view.hideProgressBar();

                        String ocrResult = "";
                        Log.d("onResponse", "Response");
                        if (response != null) {
                            try {
                                Log.d("onResponse", "Response in not Null   " + response.toString());
                                JSONArray responsesJsonArray = response.getJSONArray("responses");
                                if (responsesJsonArray.length() > 0) {
                                    JSONObject jsonObject = responsesJsonArray.getJSONObject(0);
                                    JSONArray textAnnotationsJsonArray = jsonObject.getJSONArray("textAnnotations");
                                    if (textAnnotationsJsonArray.length() > 0) {
                                        JSONObject textAnnotationJsonObject = textAnnotationsJsonArray.getJSONObject(0);
                                        ocrResult = textAnnotationJsonObject.getString("description");
                                        Log.d("onResponse", "description: " + ocrResult);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (!ocrResult.equals("")) {
                                view.updateGoogleVisionEditText(ocrResult);
                                List<Field> fieldList = FieldsParsing.parseOCRResult(ocrResult);
                                view.addRows(fieldList);
                                view.showAddFieldButton();
                            }

                        } else
                            Log.d("onResponse", "Response is Null");

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        view.hideProgressBar();

                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            view.showToast("TimeoutError || NoConnectionError");
                        } else if (error instanceof AuthFailureError) {
                            view.showToast("AuthFailureError");
                        } else if (error instanceof ServerError) {

                            view.showToast("ServerError");
                        } else if (error instanceof NetworkError) {
                            view.showToast("NetworkError");

                        } else if (error instanceof ParseError) {

                            view.showToast("ParseError");
                        }
                    }
                });
            } else {
                view.showNetworkError();
            }
        } else {
            view.showNoSelectedImageError();
        }
    }

    @Override
    public void onSubmit() {
        if (!hasImageCaptured()) {
            view.showNoSelectedImageError();
            return;
        }

        if (TextUtils.isEmpty(view.getGoogleVisionText()) && TextUtils.isEmpty(view.getMobileVisionText())) {
            view.showMobileVisionTextError();
            return;
        }
        //create Card object and save to db
        CardFields cardFields = view.getCardFields();
        Card card = new Card(view.getImagePath(), view.getMobileVisionText(), view.getGoogleVisionText()
                , view.getNotesText(), UUID.randomUUID(), cardFields.getAddresses(), cardFields.getEmails()
                , cardFields.getJobs(), cardFields.getNames(), cardFields.getPhones(), cardFields.getUrls(), cardFields.getOthers());
        card.insert();

        view.cleanForm();
    }

    @Override
    public void calculateMobileVisionOCR() {
        if (!hasImageCaptured()) {
            view.showNoSelectedImageError();
        }
        MobileVisionTask.CalculateOCR calculateOCR = new MobileVisionTask.CalculateOCR(context);
        calculateOCR.setAsyncTaskListener(new AsyncTaskListener() {
            @Override
            public void onPreExecute() {
                view.showProgressBar();
            }

            @Override
            public void onPostExecute(String ocrResult) {
                if (ocrResult == null) {
                    view.hideProgressBar();
                    return;
                }
                view.updateMobileVisionEditText(ocrResult);
                List<Field> fieldList = FieldsParsing.parseOCRResult(ocrResult);
                //List<Field> fieldList = FieldsParsing.parseOCRResult(ocrResultWordList);
                view.addRows(fieldList);
                view.showAddFieldButton();
                view.hideProgressBar();
            }
        });
        calculateOCR.execute(view.getImagePath());
    }


}
