package com.codelab.ocrexample.data;

import android.content.Context;

import com.codelab.ocrexample.data.model.SendDataRequest;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ReadImageAPI {

    public static void readImage(Context pContext,
                                 com.android.volley.Response.Listener<JSONObject> responseListener
            , com.android.volley.Response.ErrorListener errorListener
            , boolean cacheEnabled, SendDataRequest sendDataRequest) {


        String tag_json_obj = "reatText";
        String serverUrl = "https://vision.googleapis.com";
        String servicePath = "/v1/images:annotate?key=" + SecretKeys.API_KEY;

        Gson gson = new Gson();
        String json = gson.toJson(sendDataRequest);

        final String contentType = "application/json";
        final HashMap<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json, text/plain, */*");

        try {
            VolleyConnector.getInstance(serverUrl)
                    .post(servicePath, headers, new JSONObject(json), contentType, tag_json_obj, responseListener
                            , errorListener, cacheEnabled);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}