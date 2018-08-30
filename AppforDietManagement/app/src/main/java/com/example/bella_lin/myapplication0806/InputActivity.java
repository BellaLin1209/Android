package com.example.bella_lin.myapplication0806;
/**
 * Date:2018/08/06
 * Creator:Bella Lin
 * ProjectName:AppforDietManagement
 * Description:
 * 1.內容須包含早午晚三餐以及其他各種點心宵夜
 * 2.記錄吃的時間並拍照存圖
 * 3.每次吃的熱量紀錄(可從照片分析出食物熱量，但至少要可自行輸入)
 * <p>
 * Date : 2018/08/22
 * 新增
 * 1. Google Cloud Vision API 視覺相關技術可辨識人臉.物品等，可順利辨識出相片，回饋為英文。
 * 2. 自動計算熱量
 **/

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class InputActivity extends MainActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    // 變數宣告
    private static final String TAG = "InputActivity";
    private Spinner spinClass, spinFist;
    private Button btnCamera;
    private Button btnSubmit;
    private ImageView imPhoto;
    private EditText edTime, edCalorie, edName;
    private TextView tvTitle;

    // 登入
    private String mUserName = "";

    // 時間設定
    private static final int SHOW_DATAPICK = 0;
    private static final int DATE_DIALOG_ID = 1;
    private int mYear, mMonth, mDay;

    //照相機
    public static final int TAKE_PHOTO = 1; //拍照
    private Uri mImageUri = null; //圖片路徑
    private String mFilename; //圖片名稱
    private String mPhotoSaveFolder = "/bella";
    private String mPhotoSaveDCIM = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + mPhotoSaveFolder;
    private String mPhotoSaveSD = Environment.getExternalStorageDirectory() + mPhotoSaveFolder;
    private String mPhotoType = ".jpg";

    //紀錄食物相片
    public static SharedPreferences PREF_FoodRecord;
    public static String prefFoodTable = "FoodRecordTable";//資料表名
    public static String prefFoodName = "FoodName";//食物名
    public static String prefFoodPhotoPath = "FoodPhotoPath";//圖片路徑
    public static String prefFoodFist = "FoodFist";//份量
    private static String mFoodName, mFoodPhotoPath, mFoodFist;

    String[] fistarry = new String[]{"0.5", "1.0", "1.5", "2.0", "2.5", "3.0", "3.5", "4.0", "4.5", "5.0"};
    Float FOOD_CAL = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        //sharedpreference 宣告 (紀錄食物)
        PREF_FoodRecord = getSharedPreferences(prefFoodTable, MODE_PRIVATE);

        findViewById();

        setDateTime();// 取得時間

        detectIntent(); // 接掃描成功的 Intent


    }

    // 接掃描成功的 Intent
    private void detectIntent() {
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {


            Log.d(TAG, "bundle.getBoolean: " + bundle.getBoolean("CemeraSuccess"));
            if (bundle.getBoolean("CemeraSuccess")) {

                mFoodName = getSharedPreferences(prefFoodTable, MODE_PRIVATE)
                        .getString(prefFoodName, "");
                mFoodPhotoPath = getSharedPreferences(prefFoodTable, MODE_PRIVATE)
                        .getString(prefFoodPhotoPath, "");
                mFoodFist = getSharedPreferences(prefFoodTable, MODE_PRIVATE)
                        .getString(prefFoodFist, "");

                Log.d(TAG, "Name : " + mFoodName + " , Fist : " + mFoodFist + " , Path : " + mFoodPhotoPath);

                edName.setText(mFoodName);
                Float fistTemp = Float.valueOf(mFoodFist);
                if (fistTemp <= 0.5) {
                    spinFist.setSelection(0);
                } else if (fistTemp > 0.5 && fistTemp <= 1.0) {
                    spinFist.setSelection(1);
                } else if (fistTemp > 1.0 && fistTemp <= 1.5) {
                    spinFist.setSelection(2);
                } else if (fistTemp > 1.5 && fistTemp <= 2.0) {
                    spinFist.setSelection(3);
                } else if (fistTemp > 2.0 && fistTemp <= 2.5) {
                    spinFist.setSelection(4);
                } else if (fistTemp > 2.5 && fistTemp <= 3.0) {
                    spinFist.setSelection(5);
                } else if (fistTemp > 3.0 && fistTemp <= 3.5) {
                    spinFist.setSelection(6);
                } else if (fistTemp > 3.5 && fistTemp <= 4.0) {
                    spinFist.setSelection(7);
                } else if (fistTemp > 4.0 && fistTemp <= 4.5) {
                    spinFist.setSelection(8);
                } else if (fistTemp > 4.5 && fistTemp <= 5.0) {
                    spinFist.setSelection(9);
                } else {
                    spinFist.setSelection(9);
                }
            } else if (bundle.getString("ItemDes").length() != 0) {
                String des = bundle.getString("ItemDes");
                if (des.equals(getResources().getString(R.string.image_allno))||des.equals(getResources().getString(R.string.image_nothing))) {
                    edName.setText("");
                } else {
                    edName.setText(des);
                }


            } else if (bundle.get("ItemImage") != null) {

                Uri uri = (Uri) bundle.get("ItemImage");
                Log.d(TAG, "bundle.ItemImage: " + uri);
                mImageUri = uri;
                imPhoto.setImageURI(uri);

            } else {
                mImageUri = null;
                Log.d(TAG, "無照片資料");
            }


        } else {
            Log.d(TAG, "無Bundle資料");

        }
    }

    // 宣告
    private void findViewById() {
        //使用者名
        mUserName = getSharedPreferences(prefName, MODE_PRIVATE)
                .getString(prefAcc, "");

        tvTitle = findViewById(R.id.tv_title);
        tvTitle.setOnClickListener(this);

        //類別
        spinClass = findViewById(R.id.spin_class);
        ArrayAdapter<String> cAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, getResources().getStringArray(R.array.input_classifyArray));
        cAdapter.setDropDownViewResource(R.layout.spinner_dropdown);//展開後的樣式
        spinClass.setAdapter(cAdapter);
        final Animation animC = AnimationUtils.loadAnimation(this, R.anim.spinner_expand);
        spinClass.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                arg0.startAnimation(animC);
                return false;
            }
        });
        //判斷目前的時間-來先自動化分類選項
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        if (hour > 5 && hour < 11) {
            spinClass.setSelection(0);//早餐
        } else if (hour >= 11 && hour < 13) {
            spinClass.setSelection(1);//午餐
        } else if (hour > 17 & hour < 19) {
            spinClass.setSelection(2);//晚餐
        } else {
            spinClass.setSelection(3);//其他
        }


        //份量
        spinFist = findViewById(R.id.spin_fist);
        ArrayAdapter<String> fAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, fistarry);//
        fAdapter.setDropDownViewResource(R.layout.spinner_dropdown);//展開後的樣式
        spinFist.setAdapter(fAdapter);
        final Animation animF = AnimationUtils.loadAnimation(this, R.anim.spinner_expand);
        spinFist.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                arg0.startAnimation(animF);
                return false;
            }
        });
        spinFist.setSelection(1);//預設為1份
        spinFist.setOnItemSelectedListener(this);

        //名稱
        edName = findViewById(R.id.ed_name);
        edName.addTextChangedListener(textWatcher);

        //時間
        edTime = findViewById(R.id.ed_time);

        //熱量
        edCalorie = findViewById(R.id.ed_calorie);

        //相機
        btnCamera = findViewById(R.id.btn_camera);
        btnCamera.setOnClickListener(this);
        imPhoto = findViewById(R.id.im_photo);

        //送出
        btnSubmit = findViewById(R.id.btn_submit);
        btnSubmit.setOnClickListener(this);
    }

    // GOOGLE VISION CAMERA
    private void selectWay(int which) {
        Log.d(TAG, "選擇: " + which);
        Intent i = new Intent();
        i.setClass(getApplicationContext(), CameraGCVActivity.class);
        i.putExtra("ChooseWay", which);
        startActivity(i);
        finish();
    }

    // 普通相機
    private void getNormalCamera() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date(System.currentTimeMillis());
        mFilename = sdf.format(date);
        //存到SD卡
        //File outputImage = new File(photoSaveSD, filename+photoType);
        //存到DCIM
        File outputImage = new File(mPhotoSaveDCIM, mFilename + mPhotoType);

        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //把File轉成Uri並開啟相機
        mImageUri = Uri.fromFile(outputImage);
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE"); //照相
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri); //指定圖片存的位置
        startActivityForResult(intent, TAKE_PHOTO); //開啟相機
    }


    /******* 時間設定開始 *******/
    // 設置日期
    private void setDateTime() {
        initializeViews();
        final Calendar c = Calendar.getInstance();

        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        int mH = c.get(Calendar.HOUR_OF_DAY);//取得目前時間

        updateDateDisplay();
    }

    // 初始化和UI介面
    private void initializeViews() {
        // 將時間顯示在textView上
        edTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = new Message();
                if (edTime.equals((TextView) v)) {
                    msg.what = InputActivity.SHOW_DATAPICK;
                }
                InputActivity.this.HandlerDateAndTime.sendMessage(msg);
            }
        });
    }

    // 更新日期顯示
    private void updateDateDisplay() {
        edTime.setText(new StringBuilder().append(mYear).append("-")
                .append((mMonth + 1) < 10 ? "0" + (mMonth + 1) : (mMonth + 1))
                .append("-").append((mDay < 10) ? "0" + mDay : mDay));
    }

    // 日期控制
    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            updateDateDisplay();
        }
    };

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this, mDateSetListener, mYear, mMonth,
                        mDay);
        }
        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DATE_DIALOG_ID:
                ((DatePickerDialog) dialog).updateDate(mYear, mMonth, mDay);
                break;
        }
    }

    // 處裡日期和時間控制的Handler
    Handler HandlerDateAndTime = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case InputActivity.SHOW_DATAPICK:
                    showDialog(DATE_DIALOG_ID);
                    break;
            }
        }

    };
    /**時間設定結束 **/


    /**
     * 照相機 開始
     **/
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode != RESULT_OK) {
                Log.d(TAG, "ActivityResult resultCode error");
                return;
            }
            if (requestCode == TAKE_PHOTO) {
                //刷新相簿
                Intent intentBc = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intentBc.setData(mImageUri);
                this.sendBroadcast(intentBc);
                //將圖片解析成Bitmap
                Bitmap bitmap = BitmapFactory.decodeStream(
                        getContentResolver().openInputStream(mImageUri));
                //將剪裁後的照片顯示
                imPhoto.setVisibility(View.VISIBLE);
                imPhoto.setImageURI(mImageUri);
                Log.d(TAG, "imageUri=" + mImageUri.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /** 照相機 結束 **/


    /**
     * 份量監聽
     **/
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        Log.d(TAG, " FOOD_CAL = " + FOOD_CAL);
        if (!TextUtils.isEmpty(edCalorie.getText().toString())) {
            Float totalCal = FOOD_CAL * (Float.valueOf(fistarry[position]));
            edCalorie.setText(String.valueOf(totalCal));
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    /**
     * 打字監聽
     **/
    private TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
//            Log.d("TAG","afterTextChanged--------------->");

            String value = edName.getText().toString();
            Log.d("TAG", "afterTextChanged---value = " + value);

            if (!TextUtils.isEmpty(value)) {
                //判斷食物熱量資料庫是否已有資料
                ArrayList<String[]> foodDATA = sqldb.selectFood("food_name", value);
                if (foodDATA.size() == 0) {
                    Log.d("TAG", "沒有資料 : " + value);
                    FOOD_CAL = 0f;
                    edCalorie.setText("");
                    spinFist.setSelection(1);
                } else {
                    Float cal = Float.parseFloat(foodDATA.get(0)[3].toString().trim());
                    edCalorie.setText(String.valueOf(cal));
                    FOOD_CAL = cal;
                    spinFist.setSelection(1);
                    Log.d("TAG", "database : " + foodDATA.get(0)[0] + " , " + foodDATA.get(0)[1] + " , " + foodDATA.get(0)[2] + " , " + foodDATA.get(0)[3]);
                }
            } else {
                edCalorie.setText("");
                FOOD_CAL = 0f;
                spinFist.setSelection(1);
            }

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            // TODO Auto-generated method stub
            Log.d("TAG", "beforeTextChanged--------------->");
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            Log.d("TAG", "onTextChanged--------------->");
        }
    };

    /**
     * 點擊監聽
     **/
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            //測試自動補值用
            case R.id.tv_title:
//
                String[] arrType = getResources().getStringArray(R.array.input_classifyArray);

                for (int i = 1; i < 28; i++) {

                    String day = new StringBuilder().append("2018-08").append("-").append((i < 10) ? "0" + i : i).toString();
                    for (int j = 0; j < 4; j++) {
                        Random r = new Random();
                        int i1 = r.nextInt(300 - 200 + 1) + 200; //100~200
                        int i2 = r.nextInt(500 - 210 + 1) + 210; //210~500
                        int i3 = r.nextInt(800 - 500 + 1) + 500; //500~800
                        int i4 = r.nextInt(90 - 0 + 1) + 0; //0~90
                        if (j == 0) {
                            Log.d(TAG, day + " " + arrType[j] + " Random數: " + i1);
                            sqldb.InsertUserFood(new String[]{mUserName,"麵包"+i, arrType[j], day,  String.valueOf(i1), "1", ""});
                        } else if (j == 1) {
                            Log.d(TAG, day + " " + arrType[j] + " Random數: " + i2);
                            sqldb.InsertUserFood(new String[]{mUserName,"炒飯"+i, arrType[j], day,  String.valueOf(i2), "1", ""});
                        } else if (j == 2) {
                            Log.d(TAG, day + " " + arrType[j] + " Random數: " + i3);
                            sqldb.InsertUserFood(new String[]{mUserName, "麵線"+i, arrType[j], day, String.valueOf(i3), "1", ""});
                        } else if (j == 3) {
                            Log.d(TAG, day + " " + arrType[j] + " Random數: " + i4);
                            sqldb.InsertUserFood(new String[]{mUserName,"奶茶"+i, arrType[j], day,  String.valueOf(i4), "1", ""});
                        }
                    }

                }
                for (int i = 1; i < 10; i++) {

                    String day = new StringBuilder().append("2018-09").append("-").append((i < 10) ? "0" + i : i).toString();
                    for (int j = 0; j < 4; j++) {
                        Random r = new Random();
                        int i1 = r.nextInt(300 - 200 + 1) + 200; //100~200
                        int i2 = r.nextInt(500 - 210 + 1) + 210; //210~500
                        int i3 = r.nextInt(800 - 500 + 1) + 500; //500~800
                        int i4 = r.nextInt(90 - 0 + 1) + 0; //0~90
                        if (j == 0) {
                            Log.d(TAG, day + " " + arrType[j] + " Random數: " + i1);
                            sqldb.InsertUserFood(new String[]{mUserName,"雞腿堡"+i, arrType[j], day,  String.valueOf(i1), "1", ""});
                        } else if (j == 1) {
                            Log.d(TAG, day + " " + arrType[j] + " Random數: " + i2);
                            sqldb.InsertUserFood(new String[]{mUserName,"廣東粥"+i, arrType[j], day,  String.valueOf(i2), "1", ""});
                        } else if (j == 2) {
                            Log.d(TAG, day + " " + arrType[j] + " Random數: " + i3);
                            sqldb.InsertUserFood(new String[]{mUserName, "炒烏龍麵"+i, arrType[j], day, String.valueOf(i3), "1", ""});
                        } else if (j == 3) {
                            Log.d(TAG, day + " " + arrType[j] + " Random數: " + i4);
                            sqldb.InsertUserFood(new String[]{mUserName,"三合一咖啡"+i, arrType[j], day,  String.valueOf(i4), "1", ""});
                        }
                    }

                }


                Toast.makeText(getApplicationContext(),
                        "預設新增資料成功",
                        Toast.LENGTH_SHORT).show();

                break;

            //選擇時間
            case R.id.ed_time:
                //出現時間選項
                Message msg = new Message();
                if (edTime.getText().toString().equals((TextView) v)) {
                    msg.what = InputActivity.SHOW_DATAPICK;
                }
                InputActivity.this.HandlerDateAndTime.sendMessage(msg);
                break;


            //相機
            case R.id.btn_camera:
                //檢查權限
                int permission = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    //未取得權限，向使用者要求允許權限
                    Toast t = Toast.makeText(getApplicationContext(),
                            "請至 設定/應用程式 開啟權限",
                            Toast.LENGTH_SHORT);
                    t.show();
                    //和使用者要求權限
                    ActivityCompat.requestPermissions(InputActivity.this,
                            new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                            1);
                } else {
                    //已有權限，可進行檔案存取
                    new AlertDialog.Builder(InputActivity.this)
                            .setTitle("請選擇辨識模式")
                            .setSingleChoiceItems(new String[]{"普通相機", "比例相機", "Google Vision 相機"}, 0,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            dialog.dismiss();

                                            switch (which) {
                                                case 0:
                                                    //取得普通相機
                                                    getNormalCamera();

                                                    break;
                                                case 1:
                                                    //取得比例相機
                                                    Intent intent = new Intent(
                                                            InputActivity.this,
                                                            CameraTakerActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                    break;
                                                case 2:
                                                    //取得GOOGLE VISION 相機
                                                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(InputActivity.this);
                                                    builder
                                                            .setMessage(R.string.dialog_select_prompt)
                                                            .setNegativeButton(R.string.dialog_select_gallery, new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    selectWay(0);
                                                                }
                                                            })
                                                            .setPositiveButton(R.string.dialog_select_camera, new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    selectWay(1);
                                                                }
                                                            });
                                                    builder.create().show();

                                                    break;
                                            }
                                        }
                                    }).setNegativeButton("取消", null).show();

                }
                break;

            //送出
            case R.id.btn_submit:

                //取得內容
                String time = edTime.getText().toString();
                String type = spinClass.getSelectedItem().toString();
                String calorie = edCalorie.getText().toString();
                String fist = spinFist.getSelectedItem().toString();
                String name = edName.getText().toString();

                //防空值
                if (TextUtils.isEmpty(time) || TextUtils.isEmpty(type) || TextUtils.isEmpty(calorie)) {
                    Toast t1 = Toast.makeText(getApplicationContext(),
                            "請將資料填妥",
                            Toast.LENGTH_SHORT);
                    t1.show();

                } else {
                    //存進資料庫
                    Log.e("bella", "username=" + mUserName);

                    //圖片路徑
                    String photo = "null";
                    Log.d(TAG, "存入的mImageUri: " + mImageUri);
                    if (mImageUri == null) {

                        photo = "null";
                    } else {
                        photo = mImageUri.toString();
                    }


                    String datasave[] = {mUserName, name, type, time, calorie, fist, photo};
                    sqldb.InsertUserFood(datasave);

                    Log.d(TAG, "儲存: " + mUserName+ "," + name  + "," + type + "," + time + "," + calorie + "," + fist + "," + photo);
                    Toast t2 = Toast.makeText(getApplicationContext(), mUserName + " , " + type + "," +
                                    time + "," + calorie + ", save success!",
                            Toast.LENGTH_SHORT);
                    t2.show();

                    //回到首頁
                    Intent intent = new Intent(
                            InputActivity.this,
                            MainActivity.class);
                    startActivity(intent);
                    finish();

                }
                break;


            default:
                break;
        }

    }


}

