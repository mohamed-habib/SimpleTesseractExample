package com.codelab.ocrexample;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
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
import android.text.TextUtils;
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
import java.util.Arrays;
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
    List<Pair<AutoCompleteTextView, EditText>> customFieldViews = new ArrayList<>();

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
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_dialog, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Choose language");
        dialogBuilder.setMessage("Request with: ");
        final AlertDialog b = dialogBuilder.create();

        final RadioGroup radioGroup = (RadioGroup) dialogView.findViewById(R.id.language_radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.arabic:
                        executeGoogleCloudOCR(Arrays.asList("ar"));
                        b.hide();
                        break;
                    case R.id.english:
                        executeGoogleCloudOCR(Arrays.asList("en"));
                        b.hide();
                        break;
                }

            }
        });

//        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//
//            }
//        });
        b.show();
    }

    private void executeGoogleCloudOCR(List<String> languages) {
        ocrGoogleVisionET.setText("");
        if (!getSelectedDirectory().equals("")) {
            Snackbar.make(findViewById(R.id.main_content), "Not implemented yet", Snackbar.LENGTH_LONG).show();
            bulkExecuteGoogleCloudOCR(directoryPath);
        } else {
            mainActivityPresenter.executeGoogleCloudOCR(languages);
        }
    }

    private void bulkExecuteGoogleCloudOCR(String directoryPath) {
        //todo
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

        final RadioGroup radioGroup = (RadioGroup) findViewById(R.id.choose_image_rg);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.single_image:
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        CropImage.activity(null).setGuidelines(CropImageView.Guidelines.ON).start(MainActivity.this);
                        radioGroup.clearCheck();
                        break;
                    case R.id.directory:
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        startDirectorySelector();
                        radioGroup.clearCheck();
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
        fieldsContainerLL.removeAllViews();
    }

    @Override
    public CardFields getCardFields() {
        CardFields cardFields = new CardFields();
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

    @Override
    public void addRows(List<Field> fieldList, boolean clearPreviousData) {
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
        ImageButton deleteIB = (ImageButton) row.findViewById(R.id.remove);
        String[] types = getResources().getStringArray(R.array.data_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, types);
        autoCompleteTextView.setAdapter(adapter);
        final Pair<AutoCompleteTextView, EditText> spinnerEditTextPair = new Pair<>(autoCompleteTextView, editText);

        deleteIB.setOnClickListener(new View.OnClickListener() {
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
