package com.example.bella_lin.myapplication0806;

/**
 * Date:2018/08/06
 * Creator:Bella Lin
 * ProjectName:AppforDietManagement
 * Description:
 * 1. 寫一個飲食管理Android APP
 * 2. 產生每個月的飲食熱量曲線圖
 * 3. 將飲食紀錄寫入sSqlDb資料庫儲存並上傳雲端
 * 4. 拿其他Android device裝同一個APP讀取雲端上的儲存資料並成功顯示原先記錄的資料
 **/

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity implements View.OnClickListener {

    //變數宣告
    private static final String TAG = "MainActivity";
    private Button btnInput, btnAnalysis, btnBackup, btnLogin;
    private TextView tvUser;
    private Intent intentMain;

    //資料庫
    public static SQlite sqldb;


    //登入宣告
    public static SharedPreferences PREF_LOGINGCHECK;
    public static String login = "";
    public static String prefName = "loginCheck";//資料表名
    public static String prefAcc = "username";//欄位名
    public static String prefPswd = "password";//欄位名


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //資料庫(本機端)
        sqldb = new SQlite(this, "BellaSQL", null, 1);

        //sharedpreference 宣告 (判斷登入與否)
        PREF_LOGINGCHECK = getSharedPreferences(prefName, MODE_PRIVATE);
        login = PREF_LOGINGCHECK.getString(prefAcc, "");


        findViewById();


        //判斷食物熱量資料庫是否已有資料
        ArrayList<String[]> foodDATA = sqldb.selectAllFood();
        if (foodDATA.size() == 0) {
            saveFoodDatabase("food.txt");//將食物熱量資料匯入資料庫
        } else {
            Log.d(TAG, "食物資料庫已匯入\n");
        }

    }

    //宣告
    private void findViewById() {
        tvUser = findViewById(R.id.tv_hellouser);

        //輸入
        btnInput = findViewById(R.id.btn_input);
        btnInput.setOnClickListener(this);

        //分析
        btnAnalysis = findViewById(R.id.btn_analysis);
        btnAnalysis.setOnClickListener(this);

        //備份
        btnBackup = findViewById(R.id.btn_backup);
        btnBackup.setOnClickListener(this);

        //登入
        btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(this);
        showLoginBtn();

    }

    //登入後的客製顯示
    private void showLoginBtn() {
        login = getSharedPreferences(prefName, MODE_PRIVATE)
                .getString(prefAcc, "");

        if (login.isEmpty()) {
            //顯示登入
            tvUser.setText(getString(R.string.all_welcome));
            btnLogin.setText(getString(R.string.all_login));
        } else {
            //顯示登出
            String word=getResources().getString(R.string.all_welcome);
            String user =PREF_LOGINGCHECK.getString(prefAcc, "");
            tvUser.setText(word+" !  " +user );
            btnLogin.setText(getString(R.string.all_logout));
        }
    }

    //點擊監測
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            //輸入
            case R.id.btn_input:
                if (login.isEmpty()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.login_warning), Toast.LENGTH_SHORT).show();
                } else {
                    intentMain = new Intent();
                    intentMain.setClass(MainActivity.this, InputActivity.class);
                    startActivity(intentMain);
                }
                break;

            //分析
            case R.id.btn_analysis:
                if (login.isEmpty()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.login_warning), Toast.LENGTH_SHORT).show();
                } else {
                    intentMain = new Intent();
                    intentMain.setClass(MainActivity.this, AnalysisActivity.class);
                    startActivity(intentMain);
                }
                break;

            case R.id.btn_login:
                //判斷是否登入
                if (login.isEmpty()) {
                    //登入
                    final View DialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.view_logindialog, null);
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.all_login))
                            .setMessage(getString(R.string.login_message))
                            .setView(DialogView)
                            .setPositiveButton(R.string.all_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    EditText et_acc = DialogView.findViewById(R.id.et_acc);
                                    EditText et_pwd = DialogView.findViewById(R.id.et_pwd);

                                    String userName = et_acc.getText().toString();
                                    String pwd = et_pwd.getText().toString();

                                    if (TextUtils.isEmpty(userName)) {
                                        Toast.makeText(getApplicationContext(), getString(R.string.login_acc_content), Toast.LENGTH_SHORT).show();
                                        onStop();
                                    } else if (TextUtils.isEmpty(pwd)) {
                                        Toast.makeText(getApplicationContext(), getString(R.string.login_pwd_content), Toast.LENGTH_SHORT).show();
                                        onStop();
                                    } else {
                                        //確認是否有此用戶
                                        ArrayList<String[]> temp = sqldb.selectUser(SQlite.field_name, userName);

                                        if (!temp.isEmpty() && temp.get(0)[1].equals(userName)) {
                                            //有,登入
                                            Toast.makeText(getApplicationContext(), getString(R.string.all_welcomelogin) + userName, Toast.LENGTH_SHORT).show();

                                            //記錄起來
                                            PREF_LOGINGCHECK.edit().putString(prefAcc, userName).commit();
                                            PREF_LOGINGCHECK.edit().putString(prefPswd, pwd).commit();

                                            showLoginBtn();
                                        } else {
                                            //沒有,自動註冊
                                            sqldb.InsertUser(new String[]{userName, pwd});
                                            Toast.makeText(getApplicationContext(), getString(R.string.all_welcomeregister) + userName, Toast.LENGTH_SHORT).show();
                                            //記錄起來
                                            PREF_LOGINGCHECK.edit().putString(prefAcc, userName).commit();
                                            PREF_LOGINGCHECK.edit().putString(prefPswd, pwd).commit();
                                            showLoginBtn();
                                        }

                                    }
                                }
                            })
                            .setNegativeButton(getString(R.string.all_no), null).show();
                } else {
                    //登出
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.all_logout))
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setMessage(getString(R.string.logout_message))
                            .setPositiveButton(getString(R.string.all_yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //記錄起來
                                    PREF_LOGINGCHECK.edit().putString(prefAcc, "").commit();

                                    showLoginBtn();
                                    Toast.makeText(getApplicationContext(), getString(R.string.all_logout), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton(getString(R.string.all_no), null).show();
                }
                break;

            //備份
            case R.id.btn_backup:
                //判斷是否已登入
                if (login.isEmpty()) {
//                                                //未登入則不可進行備份服務
//                    Toast.makeText(getApplicationContext(), "請先登入", Toast.LENGTH_SHORT).show();
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.all_backup))
                            .setMessage(getString(R.string.all_backupmessage))
                            .setPositiveButton(R.string.all_yes, null).show();
                } else {
                    //選擇進行匯出或匯入
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.all_backup))
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setSingleChoiceItems(
                                    new String[]{getString(R.string.all_Export), getString(R.string.all_Import)}, 0,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            if (which == 0) {
                                                //匯出
                                                Toast.makeText(getApplicationContext(), getString(R.string.all_Export) + getString(R.string.all_executing), Toast.LENGTH_SHORT).show();
                                                /**bella: 匯出至雲端 **/
                                                //使用者資料
                                                submitData();
                                                //飲食紀錄
                                                selectRecordSQL();


                                            } else if (which == 1) {
                                                //匯入
                                                Toast.makeText(getApplicationContext(), getString(R.string.all_Import) + getString(R.string.all_executing), Toast.LENGTH_SHORT).show();
                                                /**bella: 雲端匯入 **/

                                                getJSON("http://cylapp.000webhostapp.com/getData.php");
                                            }
                                            dialog.dismiss();
                                        }
                                    })
                            .setNegativeButton(getString(R.string.all_no), null).show();
                }
                break;

            default:
                break;
        }
    }
    //匯入record
    private void getJSON(final String urlWebService) {

        class GetJSON extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }


            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                try {
                    loadIntoListView(s);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(urlWebService);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }
                    return sb.toString().trim() + "";
                } catch (Exception e) {
                    return null;
                }
            }
        }
        GetJSON getJSON = new GetJSON();
        getJSON.execute();
    }

    //匯入record
    private void loadIntoListView(String json) throws JSONException {
        if(!TextUtils.isEmpty(json)){
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                String user = obj.getString(DbContact.RECORD_USER);
                String name = obj.getString(DbContact.RECORD_NAME);
                String type = obj.getString(DbContact.RECORD_TYPE);
                String time = obj.getString(DbContact.RECORD_TIME);
                String cal = obj.getString(DbContact.RECORD_CAL);
                String fist = obj.getString(DbContact.RECORD_FIST);
                String photo = obj.getString(DbContact.RECORD_PHOTO);

                /**加入sqlite**/
                Log.d(TAG, "insqlite: " + user + "," + name + "," + type + "," + time + "," + cal + "," + fist + "," + photo);

                sqldb.InsertUserFood(new String[]{user, name, type, time, cal, fist, photo});
            }
            Log.d(TAG, "insqlite 完成 ");
            Toast t = Toast.makeText(getApplicationContext(),
                    "匯入成功!",
                    Toast.LENGTH_SHORT);
            t.show();
        }else{

            Toast t = Toast.makeText(getApplicationContext(),
                    "無匯入資料",
                    Toast.LENGTH_SHORT);
            t.show();
        }
    }

    //查詢record
    private void selectRecordSQL() {

        // SQLite搜尋db資料庫
        ArrayList<String[]> mRecordData = new ArrayList<>();
        mRecordData = sqldb.selectAllRecord();
        Log.d(TAG, "mRecordData size=" + mRecordData.size());

        if (mRecordData.size() == 0) {
            Toast t = Toast.makeText(getApplicationContext(),
                    "查無資料",
                    Toast.LENGTH_SHORT);
            t.show();

        } else {
            Log.d(TAG, "saveToStorage IN");

            for (int i = 0; i < mRecordData.size(); i++) {
                DbRecordHelper RecordDB = new DbRecordHelper(this);
                SQLiteDatabase database = RecordDB.getReadableDatabase();
                Log.d(TAG, "mRecordData.get(i)[1]:" + mRecordData.get(i)[1]);
                RecordDB.recordSaveToDb(database, mRecordData.get(i)[1], mRecordData.get(i)[2], mRecordData.get(i)[3],
                        mRecordData.get(i)[4], mRecordData.get(i)[5], mRecordData.get(i)[6], mRecordData.get(i)[7]);
                RecordDB.close();
                saveToRecordServer(mRecordData.get(i)[1], mRecordData.get(i)[2], mRecordData.get(i)[3],
                        mRecordData.get(i)[4], mRecordData.get(i)[5], mRecordData.get(i)[6], mRecordData.get(i)[7]);
            }
            Toast.makeText(getApplicationContext(), getString(R.string.all_backupSuccess), Toast.LENGTH_SHORT).show();

        }
    }


    //內建食物熱量資料庫
    public void saveFoodDatabase(String fileroot) {

        AssetManager assetManager = getAssets();
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open(fileroot);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] bytes = new byte[4096];
            int len = 0;
            while ((len = inputStream.read(bytes)) > 0) {
                byteArrayOutputStream.write(bytes, 0, len);
            }
            String text = new String(byteArrayOutputStream.toByteArray(), "UTF8");

            String[] temp = text.split("\n");

            for (String content : temp) {
                String[] tempT = content.split(",");
                //存進資料庫中
                sqldb.InsertFood(tempT);
                Log.i("BELLA", tempT[0] + " , " + tempT[1] + " , " + tempT[2]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //送出user備份請求
    public void submitData() {
        String name = PREF_LOGINGCHECK.getString(prefAcc, "");
        String pswd = PREF_LOGINGCHECK.getString(prefPswd, "");

        Log.d(TAG, "submitData username: " + name + ", pswd: " + pswd);

        saveToStorage(name, pswd);
        saveToUserServer(name, pswd);//連結PHP
    }

    //連結USER php
    private void saveToUserServer(String name, String pswd) {
//        Log.d(TAG, "saveToUserServer IN: name:" + name + " , pswd: " + pswd);
        if (checkNetworkConnetion()) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, DbContact.USER_SERVER_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
//                    Log.d(TAG, "response: " + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        String Response = jsonObject.getString("response");
                        if (Response.equals("OK")) {

//                            saveToStorage(name, pswd);
                            Toast.makeText(getApplicationContext(), getString(R.string.all_backupSuccess), Toast.LENGTH_SHORT).show();
                        } else {

//                            saveToStorage(name, pswd);
                            Toast.makeText(getApplicationContext(), getString(R.string.all_backupFailed), Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        Log.d(TAG, "JSON ERROR: ");
                        e.printStackTrace();

                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "onErrorResponse: " + error);
//                    saveToStorage(name, pswd);
                    Toast.makeText(getApplicationContext(), getString(R.string.all_backupFailed), Toast.LENGTH_SHORT).show();

                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("name", name);
                    params.put("pswd", pswd);
                    return params;
                }

            };

            MySingleton.getInstance(MainActivity.this).addToRequestQue(stringRequest);

        } else {
            Log.d(TAG, "沒有網路連線");
            Toast.makeText(getApplicationContext(), "無網路" + getString(R.string.all_backupFailed), Toast.LENGTH_SHORT).show();
        }

    }


    //連結record php
    private void saveToRecordServer(String user, String name, String type, String time, String cal, String fist, String photo) {

        if (checkNetworkConnetion()) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, DbContact.RECORD_SERVER_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
//                    Log.d(TAG, "response: " + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        String Response = jsonObject.getString("response");
                        if (Response.equals("OK")) {
                            Log.d(TAG, "REOCRD RESPONSE OK");

                        } else {
                            Log.d(TAG, "REOCRD RESPONSE FAILED");
                        }
                    } catch (JSONException e) {
                        Log.d(TAG, "JSON ERROR: ");
                        e.printStackTrace();

                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "RECORD ErrorResponse: " + error);
                    Toast.makeText(getApplicationContext(), getString(R.string.all_backupFailed), Toast.LENGTH_SHORT).show();

                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();

                    params.put("record_userID", user);
                    params.put("record_name", name);
                    params.put("record_type", type);
                    params.put("record_time", time);
                    params.put("record_cal", cal);
                    params.put("record_fist", fist);
                    params.put("record_photo", photo);
                    return params;
                }
            };
            MySingleton.getInstance(MainActivity.this).addToRequestQue(stringRequest);

        } else {
            Log.d(TAG, "沒有網路連線");
            Toast.makeText(getApplicationContext(), "無網路" + getString(R.string.all_backupFailed), Toast.LENGTH_SHORT).show();
        }
    }


    //確認網路連線
    public boolean checkNetworkConnetion() {
        Log.d(TAG, "checkNetworkConnetion");
        ConnectivityManager conMan = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = conMan.getActiveNetworkInfo();
        return (netinfo != null && netinfo.isConnected());

    }


    //insert db
    private void saveToStorage(String name, String pswd) {
        Log.d(TAG, "saveToStorage IN");
        DbHelper dbHelper = new DbHelper(this);
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        dbHelper.userSaveToDb(name, pswd, database);

//        readFromStorage();
        dbHelper.close();
    }


}