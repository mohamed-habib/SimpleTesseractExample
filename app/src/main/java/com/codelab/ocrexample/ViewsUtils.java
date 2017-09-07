package com.codelab.ocrexample;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Created by Mohamed Habib on 22/08/2017.
 */

public class ViewsUtils {

    @NonNull
    public static Spinner createTypeSP(Context context, int type) {
        final Spinner typeSP = new Spinner(context);
        String array[] = context.getResources().getStringArray(R.array.data_types);
        typeSP.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, array));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.weight = 3;
        typeSP.setSelection(type);
        return typeSP;
    }

    @NonNull
    public static EditText createLineET(Context context, String line) {
        final EditText editText = new EditText(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.weight = 6;
        editText.setLayoutParams(layoutParams);
        editText.setText(line);
        int padding = context.getResources().getDimensionPixelSize(R.dimen.padding);
        editText.setPadding(padding, padding, padding, padding);
        return editText;
    }

    @NonNull
    public static ImageButton createDeleteIB(Context context, View.OnClickListener onClickListener) {
        ImageButton imageButton = new ImageButton(context);
        imageButton.setImageResource(R.drawable.close);
        imageButton.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        layoutParams.weight = 1;
        imageButton.setLayoutParams(layoutParams);
        imageButton.setOnClickListener(onClickListener);
        return imageButton;
    }


    public static TextView createTypeTV(Context context, String type) {
        final TextView textView = new TextView(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(layoutParams);

        textView.setText(String.format("%s: ", type));
        textView.setTypeface(null, Typeface.BOLD);
        return textView;
    }

    public static TextView createLineTV(Context context, String line) {
        final TextView textView = new TextView(context);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setText(line);
        return textView;
    }
}
