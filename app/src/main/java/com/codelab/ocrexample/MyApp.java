package com.codelab.ocrexample;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.facebook.stetho.Stetho;
import com.raizlabs.android.dbflow.config.FlowManager;

/**
 * Created by Mohamed Habib on 15/08/2017.
 */
public class MyApp extends Application {

    public static final String TAG = MyApp.class.getSimpleName();
    private static MyApp mInstance;
    private RequestQueue mRequestQueue;

    public static synchronized MyApp getInstance() {
//        if (mInstance == null) {//// TODO: 03/01/2017 consider doing this to get rid of the static reference, @check the warning at declaring mInstance
//            mInstance = new PME_Application();
//        }
//        return mInstance;
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FlowManager.init(this);
        mInstance = this;
        Stetho.initializeWithDefaults(this);

    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

}
