package com.codelab.ocrexample;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.codelab.ocrexample.data.model.Card;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.zxing.Result;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.UUID;
import java.util.regex.Pattern;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    // User Data-field types
        //*************//
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Address, Email, Job, Name, Other, Phone, URL})
    public @interface DataType {
    }

    public static final int Address = 0;
    public static final int Email = 1;
    public static final int Job = 2;
    public static final int Name = 3;
    public static final int Other = 4;
    public static final int Phone = 5;
    public static final int URL = 6;
         //*************//

    Bitmap mImageBitmap;
    ImageView mImageView;
    TextView mTextView;
    TextView OCREditText;
    String mImagePath;
    EditText mNotesET;
    LinearLayout containerLL;
    private ZXingScannerView mScannerView;


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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Nammu.init(this);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mTextView = (TextView) findViewById(R.id.textView);
        OCREditText = (TextView) findViewById(R.id.OCREditText);
        mNotesET = (EditText) findViewById(R.id.notes);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        containerLL = (LinearLayout) findViewById(R.id.data_container);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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
                mImageBitmap = Utils.getBitmap(mImagePath);
                mImageView.setImageBitmap(mImageBitmap);
                mTextView.setVisibility(GONE);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void runOCRClick(View view) {
        if (checkForImageCapture(view)) return;

        MyAsyncTask myAsyncTask = new MyAsyncTask();
        myAsyncTask.execute();
    }

    public void onSelectImageClick(View view) {
        CropImage.activity(null)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(MainActivity.this);
    }

    public void submitClick(View view) {
        if (checkForImageCapture(view)) return;
        if (checkForCardText(view)) return;
        //create Card object and save to db
        Card card = new Card(mImagePath, OCREditText.getText().toString(), mNotesET.getText().toString(), UUID.randomUUID());
        card.insert();

        OCREditText.setText("");
        mImageView.setImageDrawable(null);
        mImagePath = null;
        mImageBitmap = null;
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
        if (TextUtils.isEmpty(OCREditText.getText())) {
            Snackbar.make(view, "Card doesn't have text, Add some info", Snackbar.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    Dialog cameraDialog;

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

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.v("QR", rawResult.getText()); // Prints scan results
        Log.v("QR", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)

        String cardData = rawResult.getText().replace(";", "\n\n");
        cardData = cardData.replace("MECARD:", "");

        String data[] = cardData.split("[\\r?\\n]+"); // split result lines
        // Start data parsing
        for (String s : data) {
            if (s.startsWith("N:"))
                addRow(Name, s.substring(s.indexOf(":") + 1));

            else if (s.startsWith("TEL:"))
                addRow(Phone, s.substring(s.indexOf(":") + 1));
            else if (s.startsWith("ORG:"))
                addRow(Other, s.substring(s.indexOf(":") + 1));
            else if (s.startsWith("EMAIL:"))
                addRow(Email, s.substring(s.indexOf(":") + 1));

        }
        // end data parsing
//        OCREditText.setText(OCREditText.getText() + "\n" + cardData);


        cameraDialog.dismiss();
    }

    private class MyAsyncTask extends AsyncTask<Void, Void, String> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = setupProgressDialog();
        }

        protected String doInBackground(Void... args) {
            StringBuilder OCRresult = new StringBuilder();
            if (mImageBitmap != null) {
                try {
                    TextRecognizer textRecognizer = new TextRecognizer.Builder(MainActivity.this).build();
                    SparseArray<TextBlock> textBlocks = textRecognizer.detect(new Frame.Builder().setBitmap(mImageBitmap).build());
                    for (int i = 0; i < textBlocks.size(); i++) {
                        OCRresult.append(textBlocks.valueAt(i).getValue()).append("\n").append("\n");
                    }
                } catch (Exception e) {
                }
            }
            return OCRresult.toString();
        }

        protected void onPostExecute(String OCRresult) {
            if (OCRresult == null) {
                pd.dismiss();
                return;
            }
//            urlET.setText("");
//            emailEt.setText("");
            StringBuilder builder = new StringBuilder();
            String numbers = getPhoneNumbers(OCRresult);

            String liness[] = OCRresult.split("[\\r\\n/]+");
            for (String line : liness) {
                if (isValidEmail(line)) {
                    addRow(Email, line);
                } else if (isValidURL(line)) {
                    addRow(URL, getAccuString(line));

                } else if (!numbers.contains(line.trim())) {
                    addRow(Other, getAccuString(line));

                }

            }
            Log.v("Phone", numbers);

            OCREditText.setText(builder.toString());
//            otherET.setText(builder.toString());
//            otherSr.setSelection(3);
            pd.dismiss();
        }

    }

    private boolean isValidEmail(String text) {
        String email = text.trim().replaceAll("\\s", "");
        String pattern = "^[A-Za-z0-9+_.-]+@(.+)$\n";
        Pattern r = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private String getPhoneNumbers(String text) {
        StringBuilder stringBuilder = new StringBuilder();


        for (PhoneNumberMatch temp : PhoneNumberUtil.getInstance().findNumbers(text, "EG")) {


            stringBuilder.append(getAccuString(PhoneNumberUtil.getInstance().format(temp.number(), PhoneNumberUtil.PhoneNumberFormat.NATIONAL)) + "\n");
        }
        addRow(Phone, stringBuilder.toString());
        return stringBuilder.toString();


    }

    private boolean isValidPhoneNumber(String text) {
        String phone = text.trim().replaceAll("[^0-9]", "");
        String pattern = "^\\s*(?:\\+?(\\d{1,3}))?[-. (]*(\\d{3})[-. )]*(\\d{3})[-. ]*(\\d{4})(?: *x(\\d+))?\\s*$";
        Pattern r = Pattern.compile(pattern);
        return r.matcher(phone).matches();
    }

    private boolean isValidURL(String URL) {
        String url = URL.trim().replaceAll("\\s+", "");
        String pattern = "(https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]\\.[^\\s]{2,})";
        Pattern r = Pattern.compile(pattern);

        return r.matcher(url).matches();
    }

    private ProgressDialog setupProgressDialog() {
        return ProgressDialog.show(this, "Processing .....", null, true, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
        });
    }

    private String getAccuString(String line) {
        return line.replaceAll("\\s+", "");
    }

    private void addRow(@DataType int type, String text) {


        LinearLayout layout = new LinearLayout(MainActivity.this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        Spinner spinner = new Spinner(this);
        String array[] = getResources().getStringArray(R.array.data_types);
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, array));
        EditText editText = new EditText(this);
        editText.setText(text);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(params);
        spinner.setSelection(type);
        layout.addView(spinner);
        layout.addView(editText);
        containerLL.addView(layout);
//        return layout;
    }


}
