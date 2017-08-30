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
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
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
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.codelab.ocrexample.data.model.CardFields;
import com.codelab.ocrexample.data.model.Field;
import com.google.zxing.Result;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;
import net.rdrei.android.dirchooser.DirectoryChooserConfig;

import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity implements MainActivityContractor.View, ZXingScannerView.ResultHandler {
    public static final int REQUEST_DIRECTORY = 300;

    ImageView mImageView;
    TextView mStatusText;
    EditText ocrMobileVisionET;
    EditText ocrGoogleVisionET;
    EditText mNotesET;
    LinearLayout fieldsContainerLL;
    Dialog cameraDialog;
    ImageButton addBtn;
    String mImagePath;
    //holds the fields to get the latest data onSubmit
    List<Pair<Spinner, EditText>> fieldViews = new ArrayList<>();
    private ZXingScannerView mScannerView;
    String directoryPath = "";
    View bottomSheet;
    BottomSheetBehavior mBottomSheetBehavior;
    private ProgressDialog pd;
    MainActivityPresenter mainActivityPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Nammu.init(this);
        mainActivityPresenter = new MainActivityPresenter(this, this);


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
        mStatusText = (TextView) findViewById(R.id.textView);
        ocrMobileVisionET = (EditText) findViewById(R.id.ocr_mv_et);
        ocrGoogleVisionET = (EditText) findViewById(R.id.ocr_gv_et);
        mNotesET = (EditText) findViewById(R.id.notes);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        fieldsContainerLL = (LinearLayout) findViewById(R.id.data_container);
        addBtn = (ImageButton) findViewById(R.id.addBtn);
        bottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    mBottomSheetBehavior.setPeekHeight(0);
                }
            }

            @Override
            public void onSlide(View bottomSheet, float slideOffset) {
            }
        });
        mBottomSheetBehavior.setPeekHeight(0);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
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
    public String getImagePath() {
        return mImagePath;
    }

    @Override
    public void showToast(String text) {
        Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                mImagePath = resultUri.getPath();
                mainActivityPresenter.imageSelected();
                directoryPath = "";
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        } else if (requestCode == REQUEST_DIRECTORY) {
            if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
                cleanForm();
                directoryPath = data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR);
                showStatusTextView();
                updateStatusTextView("Reading from " + directoryPath);
            }
        }
    }

    private void showStatusTextView() {
        mStatusText.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideStatusText() {
        mStatusText.setVisibility(GONE);
    }

    @Override
    public String getSelectedDirectory() {
        return directoryPath;
    }

    public void runOCRClick_MV(View view) {
        ocrMobileVisionET.setText("");
        if (!getSelectedDirectory().equals(""))
            mainActivityPresenter.calculateBulkMobileVisionOCR();
        else
            mainActivityPresenter.calculateMobileVisionOCR();
    }

    public void RunOCRClick_GV(View view) {
        ocrGoogleVisionET.setText("");
        if (!getSelectedDirectory().equals("")) {
            Snackbar.make(view, "Not implemented yet", Snackbar.LENGTH_LONG).show();
            bulkExecuteGoogleCloudOCR(directoryPath);
        } else {
            mainActivityPresenter.executeGoogleCloudOCR();
        }
    }

    @Override
    public void showNoSelectedImageError() {
        showSnackBar("Choose card image", "CHOOSE IMAGE", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectImageClick(null);
            }
        });
    }

    //TODO: add new Button
    public void RunOCRClick_GV_AND_MV(View view) {
        /// TODO:
    }

    public void onSelectImageClick(View view) {
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.choose_image_rg);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.single_image:
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        CropImage.activity(null).setGuidelines(CropImageView.Guidelines.ON).start(MainActivity.this);
                        break;
                    case R.id.directory:
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        startDirectorySelector();
                        break;

                }
            }
        });

    }

    private void startDirectorySelector() {
        final Intent chooserIntent = new Intent(MainActivity.this, DirectoryChooserActivity.class);
        final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                .newDirectoryName("DirChooserSample")
                .allowReadOnlyDirectory(true)
                .allowNewDirectoryNameModification(false)
                .build();
        chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_CONFIG, config);
        startActivityForResult(chooserIntent, REQUEST_DIRECTORY);
    }

    public void submitClick(View view) {
        mainActivityPresenter.onSubmit();
    }

    @Override
    public void cleanForm() {
        ocrMobileVisionET.setText("");
        ocrGoogleVisionET.setText("");
        showStatusTextView();
        updateStatusTextView("Choose Image");
        mNotesET.setText("");
        mImageView.setImageDrawable(null);
        mImagePath = null;
        fieldViews.clear();
        fieldsContainerLL.removeAllViewsInLayout();
    }

    @Override
    public CardFields getCardFields() {
        CardFields cardFields = new CardFields();
        for (Pair spinnerEdiText : fieldViews) {
            Spinner spinner = (Spinner) spinnerEdiText.first;
            EditText editText = (EditText) spinnerEdiText.second;
            int type = spinner.getSelectedItemPosition();
            String line = editText.getText().toString();
            cardFields.createField(type, line);
        }
        return cardFields;
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

        addRows(fieldList);

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

    //// TODO
    private void bulkExecuteGoogleCloudOCR(String directoryPath) {
//
//        File directory = new File(directoryPath);
//
//        for (File imgFile : directory.listFiles()) {
//            if (!imgFile.isDirectory()) {
//                String imagePath = imgFile.getPath();
//            }
//        }
//        List<String> languages = Arrays.asList("en");
//        Bitmap imageBitmap = Utils.getBitmap(bitmapPath);
//        String base64Image = Utils.bitmapToBase64(imagePath);
//
//        if (NetworkUtilies.isConnectingToInternet(this)) {
//            final ProgressDialog pd = setupProgressDialog();
//
//            APIs.callGoogleCloudOCRAPI(this, languages, base64Image, new Response.Listener<JSONObject>() {
//                @Override
//                public void onResponse(JSONObject response) {
//                    pd.dismiss();
//
//                    String ocrResult = "";
//                    Log.d("onResponse", "Response");
//                    if (response != null) {
//                        try {
//                            Log.d("onResponse", "Response in not Null   " + response.toString());
//                            JSONArray responsesJsonArray = response.getJSONArray("responses");
//                            if (responsesJsonArray.length() > 0) {
//                                JSONObject jsonObject = responsesJsonArray.getJSONObject(0);
//                                JSONArray textAnnotationsJsonArray = jsonObject.getJSONArray("textAnnotations");
//                                if (textAnnotationsJsonArray.length() > 0) {
//                                    JSONObject textAnnotationJsonObject = textAnnotationsJsonArray.getJSONObject(0);
//                                    ocrResult = textAnnotationJsonObject.getString("description");
//                                    Log.d("onResponse", "description: " + ocrResult);
//                                }
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                        if (!ocrResult.equals("")) {
//                            ocrGoogleVisionET.setText(ocrResult);
//
//                            List<Field> fieldList = FieldsParsing.parseOCRResult(ocrResult);
//                            addRows(fieldList);
//                            addBtn.setVisibility(View.VISIBLE);
//                        }
//
//                    } else
//                        Log.d("onResponse", "Response is Null");
//
//                }
//            }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    pd.dismiss();
//
//                    if (error instanceof TimeoutError || error instanceof NoConnectionError) {
//                        Toast.makeText(MainActivity.this, "TimeoutError || NoConnectionError", Toast.LENGTH_LONG).show();
//                    } else if (error instanceof AuthFailureError) {
//                        Toast.makeText(MainActivity.this, "AuthFailureError", Toast.LENGTH_LONG).show();
//                    } else if (error instanceof ServerError) {
//                        Toast.makeText(MainActivity.this, "ServerError", Toast.LENGTH_LONG).show();
//                    } else if (error instanceof NetworkError) {
//                        Toast.makeText(MainActivity.this, "NetworkError", Toast.LENGTH_LONG).show();
//                    } else if (error instanceof ParseError) {
//                        Toast.makeText(MainActivity.this, "ParseError", Toast.LENGTH_LONG).show();
//                    }
//                }
//            });
//        } else {
//            Snackbar.make(mImageView, "No Internet Connection", Snackbar.LENGTH_LONG).show();
//        }
    }

    @Override
    public void addRows(List<Field> fieldList) {
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

            fieldViews.add(spinnerEditTextPair);
        }

    }

    private void addCustomRow() {

        final View row = LayoutInflater.from(MainActivity.this).inflate(R.layout.new_row, null);
        row.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fieldsContainerLL.removeView(row);
            }
        });
        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) row.findViewById(R.id.type);
        String[] types = getResources().getStringArray(R.array.data_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, types);
        autoCompleteTextView.setAdapter(adapter);

        fieldsContainerLL.addView(row);
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

    @Override
    public void showProgressBar() {
        pd = setupProgressDialog();
    }

    @Override
    public void hideProgressBar() {
        if (pd != null)
            pd.hide();
    }

    @Override
    public void updateMobileVisionEditText(String text) {
        ocrMobileVisionET.setText(text);
    }

    @Override
    public String getMobileVisionText() {
        return ocrMobileVisionET.getText().toString();
    }

    @Override
    public String getGoogleVisionText() {
        return ocrGoogleVisionET.getText().toString();
    }

    @Override
    public String getNotesText() {
        return mNotesET.getText().toString();
    }

    @Override
    public void updateGoogleVisionEditText(String text) {
        ocrGoogleVisionET.setText(text);
    }

    @Override
    public void updateImageView(Bitmap image) {
        mImageView.setImageBitmap(image);
    }

    @Override
    public void updateStatusTextView(String text) {
        mStatusText.setText(text);
    }

    @Override
    public void showSnackBar(String text, String actionText, View.OnClickListener actionListener) {
        Snackbar.make(findViewById(R.id.main_content), text, Snackbar.LENGTH_SHORT).setAction(actionText, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectImageClick(null);
            }
        }).show();
    }

    @Override
    public void showNetworkError() {
        Snackbar.make(findViewById(R.id.main_content), "No Internet  Connection", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showMobileVisionTextError() {
        Snackbar.make(findViewById(R.id.main_content), "Card doesn't have text, Add some info", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showAddFieldButton() {
        addBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideAddFieldButton() {
        addBtn.setVisibility(View.GONE);
    }
}
