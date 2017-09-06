package com.codelab.ocrexample;

import android.graphics.Bitmap;

import com.codelab.ocrexample.data.model.CardFields;
import com.codelab.ocrexample.data.model.Field;

import java.util.List;

/**
 * Created by Mohamed Habib on 30/08/2017.
 */

public interface MainActivityContractor {
    interface View {
        String getImagePath();

        void showToast(String text);

        void hideStatusText();

        String getSelectedDirectory();

        void showNoSelectedImageError();

        void cleanForm();

        CardFields getCardFields();

        void addRows(List<Field> fieldList, boolean clearPreviousData);

        void showProgressBar();

        void hideProgressBar();

        void updateMobileVisionEditText(String text);

        String getMobileVisionText();

        String getGoogleVisionText();

        String getNotesText();

        void updateGoogleVisionEditText(String text);

        void updateImageView(Bitmap image);

        void updateStatusTextView(String text);

        void showSnackBar(String text, String buttonText, android.view.View.OnClickListener actionListener);

        void showNetworkError();

        void showMobileVisionTextError();

        void showAddFieldButton();

        void hideAddFieldButton();

    }

    interface Presenter {
        void calculateBulkMobileVisionOCR();

        void imageSelected();

        void executeGoogleCloudOCR();

        void onSubmit();

        void calculateMobileVisionOCR();

    }
}
