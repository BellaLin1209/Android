package com.example.bella_lin.myapplication0806;

/**
 * Date:2018/08/06
 * Creator:Bella Lin
 * ProjectName:AppforDietManagement
 * TABLE:
 * 1.USER
 * 2.RECORD
 **/


import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class SQlite extends SQLiteOpenHelper {

    final static String TAG = "SQLite";

    // 資料表名稱
    final static String USER_TABLE = "USER";//使用者
    final static String RECORD_TABLE = "RECORD";//飲食紀錄
    final static String FOOD_TABLE = "FOOD";//食物資料庫


    // 使用者欄位名稱
    final static String field_auto = "auto";//自動編號
    final static String field_name = "name";//帳號
    final static String field_pswd = "pswd";//密碼

    // 使用者飲食記錄欄位名稱
    final static String field_record_auto = "record_auto"; //自動編號
    final static String field_record_user = "record_user"; //定義是誰的資料
    final static String field_record_name = "record_name"; //名稱
    final static String field_record_type = "record_type"; //類別
    final static String field_record_time = "record_time"; //日期
    final static String field_record_cal = "record_cal"; //卡路里
    final static String field_record_fist = "record_fist"; //份量
    final static String field_record_photo = "record_photo"; //照片


    // 食物熱量欄位名稱
    final static String food_auto = "food_auto";//自動編號
    final static String food_name = "food_name";//名稱
    final static String food_engname = "food_engname";//英文名稱
    final static String food_cal = "food_cal";//熱量

    public SQlite(Context context, String DATABASE_NAME, CursorFactory factory,
                  int DATABASE_VERSION) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        // TODO Auto-generated constructor stub
    }

    /**
     * 創建資料表
     **/
    @Override
    public void onCreate(SQLiteDatabase sSqlDb) {
        // TODO Auto-generated method stub

        // 食物熱量資料表
        String sqlf = "CREATE TABLE "
                + FOOD_TABLE
                + " ("
                + food_auto + " INTEGER primary key autoincrement, "
                + food_name + " text, "
                + food_engname + " text, "
                + food_cal + " text)";
        sSqlDb.execSQL(sqlf);


        // 使用者資料表
        String sqlu = "CREATE TABLE "
                + USER_TABLE
                + " ("
                + field_auto + " INTEGER primary key autoincrement, "
                + field_name + " text UNIQUE, "
                + field_pswd + " text)";
        sSqlDb.execSQL(sqlu);




        // 記錄飲食資料表
        String sqlr = "CREATE TABLE "
                + RECORD_TABLE
                + " ("
                + field_record_auto + " INTEGER primary key autoincrement, "
                + field_record_user + " text, "
                + field_record_name + " text, "
                + field_record_type + " text, "
                + field_record_time + " text, "
                + field_record_cal + " text, "
                + field_record_fist + " text, "
                + field_record_photo + " text)";
        sSqlDb.execSQL(sqlr);


        String alertf = "CREATE UNIQUE INDEX 'UNIQF' ON "
                + RECORD_TABLE +"("
                + field_record_user +" , "
                + field_record_name+" , "
                + field_record_type +" , "
                + field_record_time+" , "
                + field_record_cal+" , "
                + field_record_fist +" , "
                + field_record_photo +" );";
        sSqlDb.execSQL(alertf);





        Log.d(TAG, "sqlite資料表已建完");

    }

    @Override
    public void onUpgrade(SQLiteDatabase sSqlDb, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub

    }

    /**
     * 新增
     **/
    // 新增使用者資料
    public void InsertUser(String[] text) {
        ContentValues cv = new ContentValues();
        SQLiteDatabase sSqlDbw = this.getWritableDatabase();
        cv.put(field_name, text[0]);
        cv.put(field_pswd, text[1]);
        sSqlDbw.replace(USER_TABLE, null, cv);
        sSqlDbw.close();
    }

    // 新增飲食記錄
    public void InsertUserFood(String[] text) {
        ContentValues cv = new ContentValues();
        SQLiteDatabase sSqlDbw = this.getWritableDatabase();
        cv.put(field_record_user, text[0]);
        cv.put(field_record_name, text[1]);
        cv.put(field_record_type, text[2]);
        cv.put(field_record_time, text[3]);
        cv.put(field_record_cal, text[4]);
        cv.put(field_record_fist, text[5]);
        cv.put(field_record_photo, text[6]);
        sSqlDbw.replace(RECORD_TABLE, null, cv);
        sSqlDbw.close();
    }

    // 新增食物記錄
    public void InsertFood(String[] text) {
        ContentValues cv = new ContentValues();
        SQLiteDatabase sSqlDbw = this.getWritableDatabase();
        cv.put(food_name, text[0]);
        cv.put(food_engname, text[1]);
        cv.put(food_cal, text[2]);
        sSqlDbw.replace(FOOD_TABLE, null, cv);
        sSqlDbw.close();
    }

    /**
     * 搜尋
     **/
    // 搜尋食物資料(用條件)
    public ArrayList<String[]> selectFood(String field, String value) {

        ArrayList<String[]> temp = new ArrayList<String[]>();
        SQLiteDatabase sSqlDb = this.getReadableDatabase();


        Cursor cusor = sSqlDb.query(FOOD_TABLE, null, field + " like '%"
                + value + "%'" + " OR " + food_engname + " like '%"
                + value + "%'", null, null, null, null);

        while (cusor.moveToNext()) {
            String[] data = new String[]{cusor.getString(0),
                    cusor.getString(1), cusor.getString(2), cusor.getString(3)};
            temp.add(data);
        }
        sSqlDb.close();
        cusor.close();
        return temp;
    }

    // 搜尋食物資料(全部)
    public ArrayList<String[]> selectAllFood() {
        ArrayList<String[]> temp = new ArrayList<String[]>();
        SQLiteDatabase sSqlDbr = this.getReadableDatabase();
        Cursor cusor = sSqlDbr.query(FOOD_TABLE, null, null, null, null, null, null);
        while (cusor.moveToNext()) {
            String[] data = new String[]{cusor.getString(0),
                    cusor.getString(1), cusor.getString(2), cusor.getString(3)};
            temp.add(data);
        }
        sSqlDbr.close();
        cusor.close();
        return temp;
    }


    // 搜尋使用者資料(全部)
    public ArrayList<String[]> selectAllUser() {
        ArrayList<String[]> temp = new ArrayList<String[]>();
        SQLiteDatabase sSqlDbr = this.getReadableDatabase();
        Cursor cusor = sSqlDbr.query(USER_TABLE, null, null, null, null, null, null);
        while (cusor.moveToNext()) {
            String[] data = new String[]{cusor.getString(0),
                    cusor.getString(1), cusor.getString(2)};
            temp.add(data);
        }
        sSqlDbr.close();
        cusor.close();
        return temp;
    }

    // 搜尋使用者資料(用條件)
    public ArrayList<String[]> selectUser(String field, String condi) {

        ArrayList<String[]> temp = new ArrayList<String[]>();
        SQLiteDatabase sSqlDb = this.getReadableDatabase();


        Cursor cusor = sSqlDb.query(USER_TABLE, null, field + " = " + condi
                , null, null, null, null);

        while (cusor.moveToNext()) {
            String[] data = new String[]{cusor.getString(0),
                    cusor.getString(1), cusor.getString(2)};
            temp.add(data);
        }
        sSqlDb.close();
        cusor.close();
        return temp;
    }

    // 搜尋飲食記錄(全部)
    public ArrayList<String[]> selectAllRecord() {
        ArrayList<String[]> temp = new ArrayList<String[]>();
        SQLiteDatabase sSqlDbr = this.getReadableDatabase();
        Cursor cusor = sSqlDbr
                .query(RECORD_TABLE, null, null, null, null, null, null);
        while (cusor.moveToNext()) {
            String[] data = new String[]{cusor.getString(0),
                    cusor.getString(1), cusor.getString(2), cusor.getString(3), cusor.getString(4), cusor.getString(5), cusor.getString(6), cusor.getString(7)};
            temp.add(data);
        }
        sSqlDbr.close();
        cusor.close();
        return temp;
    }

    // 搜尋飲食記錄(指定日期)
    public ArrayList<String[]> selectRecord(String user, String time, String desc) {
        Log.d(TAG, "sqldb selectFoodRecord date in");

        ArrayList<String[]> temp = new ArrayList<String[]>();
        SQLiteDatabase sSqlDb = this.getReadableDatabase();
        Cursor cusor = sSqlDb.query(RECORD_TABLE, null, field_record_user + " like '%"
                + user + "%' and " + field_record_time + " like '%"
                + time + "%'", null, null, null, field_record_time + " " + desc);

        while (cusor.moveToNext()) {
            String[] data = new String[]{cusor.getString(0),
                    cusor.getString(1), cusor.getString(2), cusor.getString(3), cusor.getString(4), cusor.getString(5), cusor.getString(6), cusor.getString(7)};
            temp.add(data);
        }
        sSqlDb.close();
        cusor.close();
        return temp;
    }


    /**
     * 修改
     **/
    // 修改使用者資料
    public void UpdateUser(String num, String id, String pswd) {
        ContentValues cv = new ContentValues();
        SQLiteDatabase sSqlDbw = this.getWritableDatabase();
        String where = field_auto + " = " + num;
        cv.put(field_name, id);
        cv.put(field_pswd, pswd);
        sSqlDbw.update(USER_TABLE, cv, where, null);
        sSqlDbw.close();
    }

    /**
     * 刪除
     **/
    // 刪除全部飲食紀錄
    public void DeleteRecord() {
        SQLiteDatabase sSqlDbw = this.getWritableDatabase();
        sSqlDbw.delete(RECORD_TABLE, null, null);
    }


    // 刪除全部使用者資料
    public void deleteUser() {
        SQLiteDatabase sSqlDbw = this.getWritableDatabase();
        sSqlDbw.delete(USER_TABLE, null, null);
        sSqlDbw.close();
    }

}
