package com.imperialsoupgmail.tesseractexample;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

import static com.googlecode.tesseract.android.TessBaseAPI.OEM_CUBE_ONLY;

public class MainActivity extends AppCompatActivity {

    Bitmap image;
    private TessBaseAPI mTess;
    String datapath = "";
    ImageView imageView;
    TextView OCRTextView;
    Spinner spinner;
    final static String ENG = "eng";
    final static String ARA = "ara";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Nammu.init(this);

        imageView = (ImageView) findViewById(R.id.imageView);
        OCRTextView = (TextView) findViewById(R.id.OCRTextView);
        spinner = (Spinner) findViewById(R.id.spinner);

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Nammu.askForPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, new PermissionCallback() {
                @Override
                public void permissionGranted() {
                }

                @Override
                public void permissionRefused() {
                }
            });
        }

        EasyImage.configuration(this)
                .setImagesFolderName("EasyImage sample")
                .setCopyTakenPhotosToPublicGalleryAppFolder(true)
                .setCopyPickedImagesToPublicGalleryAppFolder(true)
                .setAllowMultiplePickInGallery(true);

        //initialize Tesseract API
        datapath = getFilesDir() + "/tesseract/";
        mTess = new TessBaseAPI();

    }

    private String getSelectedLanguageText() {
        String lang = "";
        if (spinner.getSelectedItemId() == 0) {
            lang = "ara";
        } else if (spinner.getSelectedItemId() == 1) {
            lang = "eng";
        }
        return lang;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
                e.printStackTrace();
            }

            @Override
            public void onImagesPicked(List<File> imageFiles, EasyImage.ImageSource source, int type) {
                if (imageFiles.size() > 0) {
                    image = getBitMap(imageFiles.get(0));
                    imageView.setImageBitmap(image);
                }
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {
                //Cancel handling, you might wanna remove taken photo if it was canceled
                if (source == EasyImage.ImageSource.CAMERA) {
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(MainActivity.this);
                    if (photoFile != null) photoFile.delete();
                }
            }
        });
    }

    Bitmap getBitMap(File file) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), bmOptions);
        bitmap = Bitmap.createBitmap(bitmap);
        return bitmap;
    }

    @Override
    protected void onDestroy() {
        // Clear any configuration that was done!
        EasyImage.clearConfiguration(this);
        super.onDestroy();
    }

    public void processImage(View view) {
        new MyAsyncTask().execute();
    }

    private boolean checkTrainingFile(String lang) {
        String dataFilePath;
        switch (lang) {
            case ENG:
                dataFilePath = datapath + "/tessdata/eng.traineddata";
                break;
            case ARA:
                dataFilePath = datapath + "/tessdata/ara.traineddata";
                break;
            default:
                return false;
        }
        File datafile = new File(dataFilePath);

        if (!datafile.exists()) {
            copyAssets("tessdata", datapath + "tessdata");
        }

        return datafile.exists();
    }

    //    private void copyFiles() {
//        try {
//            String filepath = datapath + "/tessdata/eng.traineddata";
//            AssetManager assetManager = getAssets();
//            InputStream instream = assetManager.open("tessdata/eng.traineddata");
//            OutputStream outstream = new FileOutputStream(filepath);
//
//            byte[] buffer = new byte[1024];
//            int read;
//            while ((read = instream.read(buffer)) != -1) {
//                outstream.write(buffer, 0, read);
//            }
//
//
//            outstream.flush();
//            outstream.close();
//            instream.close();
//
//            File file = new File(filepath);
//            if (!file.exists()) {
//                throw new FileNotFoundException();
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private void copyAssets(String sourceAssetsFolder, String dest) {
        if (!new File(dest).exists())
            new File(dest).mkdirs();
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list(sourceAssetsFolder);
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        if (files != null) for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(sourceAssetsFolder + "/" + filename);
                File outFile = new File(dest, filename);
                out = new FileOutputStream(outFile);
                copyFile(in, out);
            } catch (IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }


    public void chooseImage(View view) {
        EasyImage.openChooserWithGallery(this, "Choose or capture image", 0);
    }

    private class MyAsyncTask extends AsyncTask<Void, Void, String> {
        ProgressDialog pd;
        String OCRresult = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Processing .....");
            pd.show();
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            String language = getSelectedLanguageText();
            if (checkTrainingFile(language))
                mTess.init(datapath, language, OEM_CUBE_ONLY);
            else
                Toast.makeText(MainActivity.this, "Can't get training data for the selected language", Toast.LENGTH_LONG).show();
        }

        protected String doInBackground(Void... args) {
            try {
                mTess.setImage(image);
                OCRresult = mTess.getUTF8Text();
            } catch (Exception e) {
                Log.d("TESS", "Exception: " + e.getMessage());
            }
            return OCRresult;
        }

        protected void onPostExecute(String OCRresult) {
            if (OCRresult == null) {

                pd.dismiss();
                return;
            }
            OCRTextView.setText(OCRresult);
            pd.dismiss();
        }
    }
}
