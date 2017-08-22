package com.codelab.ocrexample;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Mohamed Habib on 22/08/2017.
 */

public class NetworkUtilies {

    public static boolean isConnectingToInternet(Context pContext) {
        ConnectivityManager connectivity = (ConnectivityManager) pContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (NetworkInfo anInfo : info)
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED)
                        return true;
        }

        return false;
    }
}
