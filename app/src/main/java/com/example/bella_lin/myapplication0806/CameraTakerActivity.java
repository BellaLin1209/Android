package com.example.bella_lin.myapplication0806;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Fist Camera - CameraTakerActivity
 *
 * Developer: Bella Lin
 * Date: 2018/08/17
 * Description:
 * 取得相機、定義相片大小及儲存位置，並傳至CameraShowerAcrivity進行分析
 */
public class CameraTakerActivity extends Activity implements Callback {

    private static final String TAG = "CameraTakerActivity";

    // 定義照片儲存位置
    public static final String SAVE_PICTURE_PATH = Environment
            .getExternalStorageDirectory().getPath() + "/bella";

    // 宣告用於預覽使用的SurfaceView及SurfaceHolder物件
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;

    private ImageView mDrawRect;

    // 宣告相機變數
    private Camera mCamera = null;
    private Camera.Parameters mParameters;
    private ImageButton takePic;// 拍照按鈕
    private DisplayMetrics metrics;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 全螢幕顯示
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 不顯示標題列
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_food_camera);

        mDrawRect = (ImageView) findViewById(R.id.image_drewract);

        // 偵測解析度大小
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
       Log.d(TAG, "原始解析度大小 w:" + metrics.widthPixels + ",h:"
                + metrics.heightPixels);

        mSurfaceView = (SurfaceView) findViewById(R.id.surface_View1);

        // 獲取SurfaceHolder對象
        mSurfaceHolder = mSurfaceView.getHolder();

        // 註冊回傳監聽器
        mSurfaceHolder.addCallback(this);

        // 設置SurfaceHolder的類型
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // 自定義Toast
        Toast toast = Toast.makeText(getApplicationContext(), "觸碰畫面任意處進行拍照",
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);// 在畫面中間顯示
        LinearLayout toastView = (LinearLayout) toast.getView();
        ImageView imageCodeProject = new ImageView(getApplicationContext());
        imageCodeProject.setImageResource(R.drawable.camera_click);// Toast要顯示的圖片
        toastView.addView(imageCodeProject, 0);
        toast.show();

        // takePic = (ImageButton) findViewById(R.id.btn_shutter);
        mDrawRect.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                mCamera.autoFocus(afcb);// 先對焦 在拍照存檔
            }
        });

    }

    public void surfaceCreated(SurfaceHolder holder) {
        // 啟動相機服務
        mCamera = Camera.open();
        try {
            // 設置預覽
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            // 釋放所占資源
            mCamera.release();
        }

    }



    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // 獲取預設的相機屬性
        mParameters = mCamera.getParameters();
        // // 設置尺寸
        mParameters.setPictureSize(640, 480);
        // 設置圖片格式
        mParameters.setPictureFormat(PixelFormat.JPEG);
        /**
         * 開啟閃光燈 mParameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
         **/

        // 設置相機屬性
        mCamera.setParameters(mParameters);
        // 開始預覽
        mCamera.startPreview();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        /**
         * 關閉閃光燈 mParameters.setFlashMode(Parameters.FLASH_MODE_OFF);
         * mCamera.setParameters(mParameters);
         **/
        // 停止預覽
        mCamera.stopPreview();
        // 釋放佔用資源
        mCamera.release();
    }

    // 拍照後輸出圖片
    public PictureCallback pic = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            /* onPictureTaken傳入的第一個參數即為相片的byte */
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 4;
            opts.inJustDecodeBounds = true;

            // 檢查照片儲存目錄是否存在
            File path = new File(SAVE_PICTURE_PATH);
            if (!path.exists()) {// 目錄不存在
                path.mkdir();// 建立目錄
            }

            // 宣告點陣圖物件變數
            Bitmap sourceBitmap = BitmapFactory.decodeByteArray(data, 0,
                    data.length);

            if (sourceBitmap.getWidth() < sourceBitmap.getHeight()) {
                Matrix MatrixChange = new Matrix();
                MatrixChange.setRotate(90);// 選轉照片後儲存
                Bitmap rotateBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0,
                        sourceBitmap.getWidth(), sourceBitmap.getHeight(),
                        MatrixChange, true);
                sourceBitmap.recycle();
                sourceBitmap = Bitmap.createBitmap(rotateBitmap, 0, 0,
                        rotateBitmap.getWidth(), rotateBitmap.getHeight());
                rotateBitmap.recycle();
            }

           Log.d(TAG, "w=" + sourceBitmap.getWidth() + " h="
                    + sourceBitmap.getHeight());
            int savePicW = (int) (sourceBitmap.getWidth() * 0.45);//////************
            int savePicH = sourceBitmap.getHeight();
            int rightStartX = sourceBitmap.getWidth() - savePicW;

            String newDate = calendar();
            // saveBitmapToFile(SAVE_PICTURE_PATH, newDate, sourceBitmap);//儲存原圖

            // 儲存左邊矩形內影像
            Bitmap leftBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0,
                    savePicW, savePicH);
            saveBitmapToFile(SAVE_PICTURE_PATH, newDate + "S1", leftBitmap);
            leftBitmap.recycle();

            // 儲存右邊矩形內影像
            Bitmap rightBitmap = Bitmap.createBitmap(sourceBitmap, rightStartX,
                    0, savePicW, savePicH);
            saveBitmapToFile(SAVE_PICTURE_PATH, newDate + "S2", rightBitmap);
            rightBitmap.recycle();
            sourceBitmap.recycle();

            //記錄起來
            InputActivity.PREF_FoodRecord.edit().putString(InputActivity.prefFoodPhotoPath, SAVE_PICTURE_PATH + "/" + newDate + "S2"
                    + ".jpg").commit();

            // 換頁
            Intent intent = new Intent();
            intent.setClass(CameraTakerActivity.this, CameraShowerActivity.class);

            // new一個Bundle物件，並將要傳遞的資料傳入
            Bundle bundle = new Bundle();
            bundle.putString("PATH", newDate + "S1");// 傳遞String
            bundle.putString("PATH2", newDate + "S2");

            // 將Bundle物件傳給intent
            intent.putExtras(bundle);

            // 切換Activity
            startActivity(intent);
            finish();
        }
    };

    /**
     * @param dirpath
     * @param fileName
     * @param savebitmap
     * @return if successfully return image file path,else return null
     */
    public static String saveBitmapToFile(String dirpath, String fileName,
                                          Bitmap savebitmap) {
        File dir = new File(dirpath);

        if (!dir.exists()) {
            dir.mkdirs();
        }
        File picFile = new File(dirpath, fileName + ".jpg");

        if (picFile.exists()) {
            picFile.delete();
        }

        try {
            picFile.createNewFile();
        } catch (IOException e2) {
            e2.printStackTrace();
        }

        FileOutputStream fos = null;
        boolean success = false;
        try {
            fos = new FileOutputStream(picFile);
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
        }
        success = savebitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
        if (fos != null) {
            try {
                fos.flush();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();//
            }
        }
        if (success) {
            return picFile.getAbsolutePath();
        } else {
            picFile.delete();
            return null;
        }

    }

    private ShutterCallback shutterCallback = new ShutterCallback() {
        public void onShutter() {
            // Shutter has closed
        }
    };

    private PictureCallback rawCallback = new PictureCallback() {
        public void onPictureTaken(byte[] _data, Camera _camera) {
            // TODO Handle RAW image data
        }
    };
    public AutoFocusCallback afcb = new AutoFocusCallback() {

        public void onAutoFocus(boolean success, Camera camera) {

            if (success) {
                mCamera.takePicture(shutterCallback, rawCallback, pic);
               Log.d(TAG, "對焦成功開始拍照存檔");
            } else {
                Toast.makeText(CameraTakerActivity.this, "對焦失敗! 請再試一次", Toast.LENGTH_SHORT);
            }
        }
    };

    // 取得目前時間
    public String calendar() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DATE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        String date = new StringBuilder().append(year).append((month < 10) ? "0" + month : month).append(day).append(hour).append(minute).append(second).toString();
      
        return date;
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        finish();
        super.onDestroy();
    }

}