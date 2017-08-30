package com.codelab.ocrexample;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.codelab.ocrexample.data.model.FieldDB;
import com.codelab.ocrexample.data.model.Item;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.zxing.Result;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    Bitmap mImageBitmap;
    ImageView mImageView;
    TextView mTextView;
    EditText ocrMobileVisionET;
    EditText ocrGoogleVisionET;
    EditText mNotesET;
    LinearLayout fieldsContainerLL;

    Dialog cameraDialog;
    ImageButton addBtn;
    String mImagePath;
    //holds the fields to get the latest data onSubmit
    List<Pair<Spinner, EditText>> fieldViews = new ArrayList<>();
    List<Pair<AutoCompleteTextView, EditText>> customFieldViews = new ArrayList<>();

    private ZXingScannerView mScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Nammu.init(this);

        initViews();

        checkForPermissions();

        //enable scrolling at edit text inside scroll view
        ocrMobileVisionET.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }
        });
        //enable scrolling at edit text inside scroll view
        ocrGoogleVisionET.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }
        });

    }

    private void checkForPermissions() {
        int storagePermissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (storagePermissionCheck != PackageManager.PERMISSION_GRANTED) {
            Nammu.askForPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, new PermissionCallback() {
                @Override
                public void permissionGranted() {
                }

                @Override
                public void permissionRefused() {
                }
            });
        }

        int cameraPermissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (cameraPermissionCheck != PackageManager.PERMISSION_GRANTED) {
            Nammu.askForPermission(this, Manifest.permission.CAMERA, new PermissionCallback() {
                @Override
                public void permissionGranted() {
                }

                @Override
                public void permissionRefused() {
                }
            });
        }
    }

    private void initViews() {
        mImageView = (ImageView) findViewById(R.id.imageView);
        mTextView = (TextView) findViewById(R.id.textView);
        ocrMobileVisionET = (EditText) findViewById(R.id.ocr_mv_et);
        ocrGoogleVisionET = (EditText) findViewById(R.id.ocr_gv_et);
        mNotesET = (EditText) findViewById(R.id.notes);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        fieldsContainerLL = (LinearLayout) findViewById(R.id.data_container);
        addBtn = (ImageButton) findViewById(R.id.addBtn);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_btn:
                startActivity(new Intent(MainActivity.this, SearchActivtiy.class));
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                mImagePath = resultUri.getPath();
                try {
                    File src = new File(mImagePath);
                    Utils.copyFile(new File(mImagePath), new File(Environment.getExternalStorageDirectory() + "/OCR/" + src.getName()));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mImageBitmap = Utils.getBitmap(mImagePath);
                mImageView.setImageBitmap(mImageBitmap);
                mTextView.setVisibility(GONE);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void runOCRClick_MV(View view) {
        if (checkForImageCapture(view)) return;

        ocrMobileVisionET.setText("");
        new MobileVisionAsyncTask().execute();
    }

    public void RunOCRClick_GV(View view) {
        if (checkForImageCapture(view)) return;

        ocrGoogleVisionET.setText("");
        executeGoogleCloudOCR();
    }

    public void onSelectImageClick(View view) {
        CropImage.activity(null).setGuidelines(CropImageView.Guidelines.ON).start(MainActivity.this);
    }

    public void submitClick(View view) {
        if (checkForImageCapture(view)) return;
        if (checkForCardText(view)) return;
        //create Card object and save to db
        CardFields cardFields = getCardFields();
        Card card = new Card(mImagePath, ocrMobileVisionET.getText().toString(), ocrGoogleVisionET.getText().toString()
                , mNotesET.getText().toString());
//        cardFields.getAddresses(), cardFields.getEmails()
//                , cardFields.getJobs(), cardFields.getNames(), cardFields.getPhones(), cardFields.getUrls(), cardFields.getOthers())
        card.insert();
        for (String key : cardFields.getKeys()) {
            FieldDB field = new FieldDB(key, card.getId());
            field.insert();
            for (String item : cardFields.getValues(key)) {
                Item itemi = new Item(item, field.getID());
                itemi.insert();
            }
        }
        ocrMobileVisionET.setText("");
        ocrGoogleVisionET.setText("");
        mNotesET.setText("");
        mImageView.setImageDrawable(null);
        mImagePath = null;
        mImageBitmap = null;
        fieldViews.clear();
        fieldsContainerLL.removeAllViews();

    }

    @NonNull
    private CardFields getCardFields() {
        CardFields cardFields = new CardFields(MainActivity.this);
        for (Pair spinnerEdiText : fieldViews) {
            Spinner spinner = (Spinner) spinnerEdiText.first;
            EditText editText = (EditText) spinnerEdiText.second;
            String type = (String) spinner.getSelectedItem();
            String line = editText.getText().toString();
            cardFields.createField(type, line);
        }
        for (Pair autoCompleteEdiText : customFieldViews) {
            AutoCompleteTextView autoCompleteTV = (AutoCompleteTextView) autoCompleteEdiText.first;
            EditText editText = (EditText) autoCompleteEdiText.second;
            String type = autoCompleteTV.getText().toString().trim();
            String line = editText.getText().toString().trim();
            if (!TextUtils.isEmpty(type) || !TextUtils.isEmpty(line))
                cardFields.createField(type, line);
        }
        return cardFields;
    }

    private boolean checkForImageCapture(View view) {
        if (mImagePath == null) {
            Snackbar.make(view, "Choose card image", Snackbar.LENGTH_SHORT).setAction("CHOOSE IMAGE", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSelectImageClick(null);
                }
            }).show();
            return true;
        }
        return false;
    }

    private boolean checkForCardText(View view) {
        if (TextUtils.isEmpty(ocrMobileVisionET.getText()) && TextUtils.isEmpty(ocrGoogleVisionET.getText())) {
            Snackbar.make(view, "Card doesn't have text, Add some info", Snackbar.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    public void onScanQRClick(View view) {
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera(0);          // Start camera
        showCameraDialog();
    }

    private void showCameraDialog() {
        if (cameraDialog == null) {
            cameraDialog = new Dialog(this);
            cameraDialog.setContentView(mScannerView);
            cameraDialog.show();
            cameraDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    mScannerView.stopCamera();
                }
            });
        } else {
            cameraDialog.show();
        }
    }

    //QR result
    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.v("QR", rawResult.getText()); // Prints scan results
        Log.v("QR", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)

        String cardData = rawResult.getText().replace(";", "\n\n");
        cardData = cardData.replace("MECARD:", "");

        ocrMobileVisionET.setText(cardData);

        List<Field> fieldList = FieldsParsing.parseQRCode(cardData);

        addRows(fieldList, false);

        addBtn.setVisibility(View.VISIBLE);

        cameraDialog.dismiss();
    }

    private ProgressDialog setupProgressDialog() {
        return ProgressDialog.show(this, "Processing .....", null, true, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
        });
    }

    private void executeGoogleCloudOCR() {
        List<String> languages = Arrays.asList("en");
        String base64Image = Utils.bitmapToBase64(mImageBitmap);

        if (NetworkUtilies.isConnectingToInternet(this)) {
            final ProgressDialog pd = setupProgressDialog();

            APIs.callGoogleCloudOCRAPI(this, languages, base64Image, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    pd.dismiss();
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
                                    String description = textAnnotationJsonObject.getString("description");
                                    Log.d("onResponse", "description: " + description);

                                    ocrGoogleVisionET.setText(description);

                                    List<Field> fieldList = FieldsParsing.parseOCRResult(description);
                                    addRows(fieldList, true);
                                    addBtn.setVisibility(View.VISIBLE);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else
                        Log.d("onResponse", "Response is Null");

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    pd.dismiss();

                    if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                        Toast.makeText(MainActivity.this, "TimeoutError || NoConnectionError", Toast.LENGTH_LONG).show();
                    } else if (error instanceof AuthFailureError) {
                        Toast.makeText(MainActivity.this, "AuthFailureError", Toast.LENGTH_LONG).show();
                    } else if (error instanceof ServerError) {
                        Toast.makeText(MainActivity.this, "ServerError", Toast.LENGTH_LONG).show();
                    } else if (error instanceof NetworkError) {
                        Toast.makeText(MainActivity.this, "NetworkError", Toast.LENGTH_LONG).show();
                    } else if (error instanceof ParseError) {
                        Toast.makeText(MainActivity.this, "ParseError", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            Snackbar.make(mImageView, "No Internet Connection", Snackbar.LENGTH_LONG).show();
        }
    }

    private void addRows(List<Field> fieldList, boolean clearPreviousData) {
        fieldsContainerLL.removeAllViewsInLayout();

        for (final Field field : fieldList) {
            final LinearLayout layout = new LinearLayout(MainActivity.this);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            fieldsContainerLL.setWeightSum(10);
            fieldsContainerLL.setPadding(5, 5, 5, 5);
            layout.setMinimumHeight(40);
            layout.setLayoutParams(params);

            Spinner typeSP = ViewsUtils.createTypeSP(this, field.getType());
            EditText lineET = ViewsUtils.createLineET(this, field.getLine());

            final Pair<Spinner, EditText> spinnerEditTextPair = new Pair<>(typeSP, lineET);
            ImageButton deleteIB = ViewsUtils.createDeleteIB(this, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fieldsContainerLL.removeView(layout);
                    fieldViews.remove(spinnerEditTextPair);
                }
            });
            layout.addView(typeSP);
            layout.addView(lineET);
            layout.addView(deleteIB);

            fieldsContainerLL.addView(layout);
            if (clearPreviousData)
                fieldViews.clear();
            fieldViews.add(spinnerEditTextPair);
        }

    }

    private void addCustomRow() {

        final View row = LayoutInflater.from(MainActivity.this).inflate(R.layout.new_row, null);
        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) row.findViewById(R.id.type);
        EditText editText = (EditText) row.findViewById(R.id.data);
        String[] types = getResources().getStringArray(R.array.data_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, types);
        autoCompleteTextView.setAdapter(adapter);
        final Pair<AutoCompleteTextView, EditText> spinnerEditTextPair = new Pair<>(autoCompleteTextView, editText);

        ImageButton deleteIB = ViewsUtils.createDeleteIB(this, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
////                fieldsContainerLL.removeView(layout);
//                fieldViews.remove(spinnerEditTextPair);
            }
        });
        row.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fieldsContainerLL.removeView(row);
                customFieldViews.remove(spinnerEditTextPair);
            }
        });

        fieldsContainerLL.addView(row);

        customFieldViews.add(spinnerEditTextPair);
    }

    public void AddCustomRowClick(View view) {
        addCustomRow();
    }

    public void ShowHideMVbtnClick(View view) {
        show(ocrMobileVisionET, (ImageButton) view);
    }

    public void ShowHideGVbtnClick(View view) {
        show(ocrGoogleVisionET, (ImageButton) view);

    }

    private void show(final EditText editText, ImageButton button) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        final boolean show = editText.getVisibility() == View.VISIBLE;
        editText.setVisibility(show ? View.GONE : View.VISIBLE);
        editText.animate().setDuration(shortAnimTime)
                .translationYBy(show ? -editText.getHeight() : editText.getHeight())
                .alpha(
                        show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                editText.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        button.setImageResource(show ? R.drawable.ic_show : R.drawable.ic_hide);

    }

    private class MobileVisionAsyncTask extends AsyncTask<Void, Void, String> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = setupProgressDialog();
        }

        protected String doInBackground(Void... args) {
            StringBuilder ocrResultLines = new StringBuilder();
            StringBuilder ocrResultWords = new StringBuilder();
            if (mImageBitmap != null) {
                TextRecognizer textRecognizer = null;
                try {
                    textRecognizer = new TextRecognizer.Builder(MainActivity.this).build();
                    SparseArray<TextBlock> textBlocks = textRecognizer.detect(new Frame.Builder().setBitmap(mImageBitmap).build());
                    for (int i = 0; i < textBlocks.size(); i++) {
                        ocrResultLines.append(textBlocks.valueAt(i).getValue()).append("\n").append("\n");
                        for (int j = 0; j < textBlocks.valueAt(i).getComponents().size(); j++) {
                            ocrResultWords.append(textBlocks.valueAt(i).getComponents().get(j).getValue()).append(",");
                        }
                    }
                } catch (Exception e) {
                } finally {
                    if (textRecognizer != null)
                        textRecognizer.release();
                }
            }
            Log.d("WORDS", ocrResultWords.toString());
            return ocrResultLines.toString();
        }

        protected void onPostExecute(String ocrResult) {
            if (ocrResult == null) {
                pd.dismiss();
                return;
            }
            ocrMobileVisionET.setText(ocrResult);

            List<Field> fieldList = FieldsParsing.parseOCRResult(ocrResult);
            addRows(fieldList, false);
            addBtn.setVisibility(View.VISIBLE);
            pd.dismiss();
        }

    }


}
