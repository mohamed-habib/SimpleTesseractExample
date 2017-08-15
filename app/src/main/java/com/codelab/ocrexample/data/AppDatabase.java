package com.codelab.ocrexample.data;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Created by Mohamed Habib on 15/08/2017.
 */
@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION)
public class AppDatabase {
    public static final String NAME = "OCR_POC_AppDatabase";

    public static final int VERSION = 1;
}
