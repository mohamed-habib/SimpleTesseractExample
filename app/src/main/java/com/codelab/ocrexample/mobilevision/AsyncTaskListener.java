package com.codelab.ocrexample.mobilevision;

/**
 * Created by Mohamed Habib on 30/08/2017.
 */

public interface AsyncTaskListener {
    void onPreExecute();

    void onPostExecute(String result);
}
