package com.example.bella_lin.myapplication0806;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbRecordHelper extends SQLiteOpenHelper {

    private static final String TAG = "DbRecordHelper";
    private static final int RECORD_DATABASE_VERSION = 1;
    private static final String RECORD_CREATE_TABLE = "create table " + DbContact.RECORD_TABLE_NAME + "(id integer primary key autoincrement," +DbContact.RECORD_USER + " text," + DbContact.RECORD_NAME + " text,"+DbContact.RECORD_TYPE + " text," + DbContact.RECORD_TIME + " text," + DbContact.RECORD_CAL + " text," + DbContact.RECORD_FIST + " text,"+DbContact.RECORD_PHOTO+ " text);";
    private static final String RECORD_DROP_TABLE = "drop table if exists " + DbContact.RECORD_TABLE_NAME;

    public DbRecordHelper(Context context) {
        super(context, DbContact.DATABASE_NAME, null, RECORD_DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "record onCreate db");
        db.execSQL(RECORD_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "record onUpgrade db");
        db.execSQL(RECORD_DROP_TABLE);
        onCreate(db);
    }



    public void recordSaveToDb(SQLiteDatabase database,String user, String name,String type, String time,String cal, String fist,String photo ) {
        Log.d(TAG, "recordSaveToDb db");
        ContentValues contentvalues = new ContentValues();
        contentvalues.put(DbContact.RECORD_USER, user);
        contentvalues.put(DbContact.RECORD_NAME, name);
        contentvalues.put(DbContact.RECORD_TYPE, type);
        contentvalues.put(DbContact.RECORD_TIME, time);
        contentvalues.put(DbContact.RECORD_CAL, cal);
        contentvalues.put(DbContact.RECORD_FIST, fist);
        contentvalues.put(DbContact.RECORD_PHOTO, photo);

        Log.d(TAG, "recordSaveToDb name: "+name);
        database.insert(DbContact.RECORD_TABLE_NAME, null, contentvalues);
    }

    public Cursor recordReadDb() {
        Log.d(TAG, "recordReadDb IN");

        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + DbContact.RECORD_TABLE_NAME +";";
        Cursor c = db.rawQuery(sql, null);
        Log.d(TAG, "Cursor:"+c.getCount());
        return c;




//        String[] projection = {DbContact.RECORD_USER, DbContact.RECORD_NAME,DbContact.RECORD_TYPE, DbContact.RECORD_TIME,DbContact.RECORD_CAL, DbContact.RECORD_FIST,DbContact.RECORD_PHOTO};
////        String selection = DbContact.RECORD_USER + " LIKE ?";
////        String[] selection_args = {username};
//        Cursor c= database.query(DbContact.RECORD_TABLE_NAME, projection, null, null, null, null, DbContact.RECORD_TIME);
//        Log.d(TAG, "Cursor:"+c);
//        return c;

    }



}
