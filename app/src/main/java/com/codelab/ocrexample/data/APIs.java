package com.codelab.ocrexample.data;

import android.content.Context;

import com.android.volley.Response;
import com.codelab.ocrexample.data.model.Feature;
import com.codelab.ocrexample.data.model.Image;
import com.codelab.ocrexample.data.model.ImageContext;
import com.codelab.ocrexample.data.model.Request;
import com.codelab.ocrexample.data.model.SendDataRequest;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mohamed Habib on 22/08/2017.
 */

public class APIs {


    public static void callGoogleCloudOCRAPI(Context context, List<String> languages, String base64Image,
                                             Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {

        SendDataRequest sendDataRequest = buildRequest(languages, base64Image);
        ReadImageAPI.readImage(context, successListener, errorListener, false, sendDataRequest);

    }

    private static SendDataRequest buildRequest(List<String> languages, String base64Image) {

        List<Request> requests = new ArrayList<>();
        List<Feature> features = new ArrayList<>();
        Request request = new Request();
        Feature feature = new Feature();
        Image image = new Image();
        ImageContext imageContext = new ImageContext();

        image.setContent(base64Image);
        feature.setType("TEXT_DETECTION");
        imageContext.setLanguageHints(languages);

        request.setFeatures(features);
        request.setImage(image);
        request.setImageContext(imageContext);

        features.add(feature);
        requests.add(request);

        SendDataRequest sendDataRequest = new SendDataRequest();

        sendDataRequest.setRequests(requests);
        return sendDataRequest;
    }

}
