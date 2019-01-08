/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.bella_lin.myapplication0806;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Google Cloud Vision
 * Creator: Bella Lin
 * Date: 2018/08/20
 **/


public class CameraGCVActivity extends Activity {
    private static final String CLOUD_VISION_API_KEY = BuildConfig.API_KEY;// BuildConfig.API_KEY;
    public static final String FILE_NAME = "temp.jpg";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final int MAX_LABEL_RESULTS = 10;
    private static final int MAX_DIMENSION = 1200;

    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;

    private TextView mImageDetails;
    private ImageView mMainImage;
    //存回傳的結果值
    private static ArrayList<String> ItemDes = new ArrayList<>();

    private static String ItemUri = "";
    private static final String TAG = "GoogleVision.class";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cameragcv);

        Log.d(TAG, "進入 CameraGCVActivity.class");

        // 接bundle---
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        if (intent.getExtras() != null) {
            Log.d(TAG, "bundle.getInt: " + bundle.getInt("ChooseWay"));
            if (bundle.getInt("ChooseWay") == 0) {
                Log.d(TAG, "開啟相簿");
                startGalleryChooser(); //相簿
            } else if (bundle.getInt("ChooseWay") == 1) {
                Log.d(TAG, "開啟相機");
                startCamera(); //開啟照相機
            }
        } else {
            Log.d(TAG, "沒有bundle");
        }

        mImageDetails = findViewById(R.id.tv_details);
        mMainImage = findViewById(R.id.main_image);
    }

    //相簿
    public void startGalleryChooser() {
        if (CameraGCVpu.requestPermission(this, GALLERY_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                    GALLERY_IMAGE_REQUEST);
        }
    }

    //相機
    public void startCamera() {
        if (CameraGCVpu.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    //取得相片資料夾
    public File getCameraFile() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);//DIRECTORY_DCIM
        return new File(dir, FILE_NAME);
    }

    //回傳結果
    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        StringBuilder message = new StringBuilder("");

        ItemDes.clear();
        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();

        Log.d(TAG, "labels: " + labels);
        if (labels != null) {
            //判斷一下回傳數，最多呈現五個
            int count = labels.size();
            if (labels.size() > 5) {
                count = 5;}
            Log.d(TAG, "count: " + count);

            //加入dialog msg中
            for (int i = 0; i < count; i++) {
                String desc = labels.get(i).getDescription();
                Float score = labels.get(i).getScore();
                String meg = String.format(Locale.TAIWAN, "%s: %1.3f",desc ,score);
                Log.d(TAG,meg);
                ItemDes.add(meg);
                message.append(meg);
                message.append("\n");
            }

        } else {
//            Toast.makeText(getApplicationContext(),
//                    getResources().getString(R.string.image_nothing),
//                    Toast.LENGTH_LONG).show();
            ItemDes.add(getResources().getString(R.string.image_nothing));
            message.append(getResources().getString(R.string.image_nothing));
        }

        return message.toString();
    }

    //跳回前頁
    private void GoBackDialog() {
        //跳出單選框
        new AlertDialog.Builder(CameraGCVActivity.this)
                .setTitle("是否為以下辨識結果?")
                .setSingleChoiceItems(ItemDes.toArray(new String[ItemDes.size()]), 0,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //傳回inputActivity
                                String itemdes = ItemDes.get(which);
                                Intent intent = new Intent(
                                        CameraGCVActivity.this,
                                        InputActivity.class);
                                Log.d(TAG, "1回傳itemdes: " + itemdes);

                                if (itemdes.equals(getResources().getString(R.string.image_allno)) || itemdes.equals(getResources().getString(R.string.image_nothing))) {
                                    itemdes = ItemDes.get(which);
                                } else {
                                    itemdes = ItemDes.get(which).substring(0, itemdes.indexOf(":"));
                                }

                                Log.d(TAG, "2回傳itemdes: " + itemdes);
                                intent.putExtra("ItemDes", itemdes);
                                intent.putExtra("ItemImage", ItemUri);
                                startActivity(intent);
                                finish();
                            }
                        }).show();
    }

    //認證api
    private void callCloudVision(final Bitmap bitmap) {
        // Switch text to loading
        Toast.makeText(getApplicationContext(),
                R.string.loading_message,
                Toast.LENGTH_LONG).show();

        mImageDetails.setText(R.string.loading_message);

        // Do the real work in an async task, because we need to use the network anyway
        try {
            AsyncTask<Object, Void, String> labelDetectionTask = new LableDetectionTask(this, prepareAnnotationRequest(bitmap));
            labelDetectionTask.execute();
        } catch (IOException e) {
            Log.d(TAG, "failed to make API request because of other IOException " +
                    e.getMessage());

        }
    }

    //設定圖片
    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            uploadImage(data.getData());
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            uploadImage(photoUri);//photoUri
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST:
                if (CameraGCVpu.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
                    startCamera();//相機
                }
                break;
            case GALLERY_PERMISSIONS_REQUEST:
                if (CameraGCVpu.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
                    startGalleryChooser();//相簿
                }
                break;
        }
    }

    public void uploadImage(Uri uri) {

        if (uri != null) {
            ItemUri = String.valueOf(uri); //把照片路徑記錄起來
            try {
                // scale the image to save on bandwidth
                Bitmap bitmapImage =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                                MAX_DIMENSION);

                callCloudVision(bitmapImage);
                mMainImage.setImageBitmap(bitmapImage);

            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            ItemUri = ""; //把照片路徑記錄起來
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }

    private Vision.Images.Annotate prepareAnnotationRequest(Bitmap bitmap) throws IOException {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        VisionRequestInitializer requestInitializer =
                new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                    /**
                     * We override this so we can inject important identifying fields into the HTTP
                     * headers. This enables use of a restricted cloud platform API key.
                     */
                    @Override
                    protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                            throws IOException {
                        super.initializeVisionRequest(visionRequest);

                        String packageName = getPackageName();
                        visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                        String sig = CameraGCVpmu.getSignature(getPackageManager(), packageName);

                        visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                    }
                };

        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(requestInitializer);

        Vision vision = builder.build();

        BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                new BatchAnnotateImagesRequest();
        batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

            // Add the image
            Image base64EncodedImage = new Image();
            // Convert the bitmap to a JPEG
            // Just in case it's a format that Android understands but Cloud Vision
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            // Base64 encode the JPEG
            base64EncodedImage.encodeContent(imageBytes);
            annotateImageRequest.setImage(base64EncodedImage);

            // add the features we want
            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                Feature labelDetection = new Feature();
                labelDetection.setType("LABEL_DETECTION");
                labelDetection.setMaxResults(MAX_LABEL_RESULTS);
                add(labelDetection);
            }});


            // Add the list of one thing to the request
            add(annotateImageRequest);
        }});

        Vision.Images.Annotate annotateRequest =
                vision.images().annotate(batchAnnotateImagesRequest);
        // Due to a bug: requests to Vision API containing large images fail when GZipped.
        annotateRequest.setDisableGZipContent(true);
        Log.d(TAG, "created Cloud Vision request object, sending request");

        return annotateRequest;
    }

    private class LableDetectionTask extends AsyncTask<Object, Void, String> {
        private final WeakReference<CameraGCVActivity> mActivityWeakReference;
        private Vision.Images.Annotate mRequest;

        LableDetectionTask(CameraGCVActivity activity, Vision.Images.Annotate annotate) {
            mActivityWeakReference = new WeakReference<>(activity);
            mRequest = annotate;
        }

        //背景處理:接意外結果
        @Override
        protected String doInBackground(Object... params) {
            try {
                Log.d(TAG, "created Cloud Vision request object, sending request");
                BatchAnnotateImagesResponse response = mRequest.execute();
                return convertResponseToString(response);
            } catch (GoogleJsonResponseException e) {
                Log.d(TAG, "failed to make API request because " + e.getContent());
                ItemDes.add(getResources().getString(R.string.image_nothing));
            } catch (IOException e) {
                Log.d(TAG, "failed to make API request because of other IOException " +
                        e.getMessage());
                ItemDes.add(getResources().getString(R.string.image_nothing));
            }
            return "Cloud Vision API request failed. Check logs for details.";
        }

        //印出結果
        protected void onPostExecute(String result) {
            CameraGCVActivity activity = mActivityWeakReference.get();
            if (activity != null && !activity.isFinishing()) {

                Log.d(TAG, "RESULT all: " + result);
                ItemDes.add(getResources().getString(R.string.image_allno));//都不對我要自己輸入
                GoBackDialog();
            }
        }
    }

}