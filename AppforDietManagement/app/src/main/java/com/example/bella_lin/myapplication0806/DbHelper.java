package com.example.bella_lin.myapplication0806;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {

    private static final String TAG = "DbHelper";
    private static final int USER_DATABASE_VERSION = 1;
    private static final String USER_CREATE_TABLE = "create table " + DbContact.USER_TABLE_NAME + "(num integer primary key autoincrement," + DbContact.USER_NAME + " text," + DbContact.USER_PSWD + " text);";
    private static final String USER_DROP_TABLE = "drop table if exists " + DbContact.USER_TABLE_NAME;

    public DbHelper(Context context) {
        super(context, DbContact.DATABASE_NAME, null, USER_DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate db");
        db.execSQL(USER_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade db");
        db.execSQL(USER_DROP_TABLE);
        onCreate(db);
    }


    public void userSaveToDb(String name, String pswd, SQLiteDatabase database) {
        Log.d(TAG, "userSaveToDb db");
        ContentValues contentvalues = new ContentValues();
        contentvalues.put(DbContact.USER_NAME, name);
        contentvalues.put(DbContact.USER_PSWD, pswd);
        database.insert(DbContact.USER_TABLE_NAME, null, contentvalues);
    }

    public Cursor userReadDb(SQLiteDatabase database) {
        Log.d(TAG, "userReadDb IN");
        String[] projection = {DbContact.USER_NAME, DbContact.USER_PSWD};
        return (database.query(DbContact.USER_TABLE_NAME, projection, null, null, null, null, null));

    }


    public void userUpdateDb(String name, String pswd, SQLiteDatabase database) {
        Log.d(TAG, "userUpdateDb db");
        ContentValues contentvalues = new ContentValues();
        contentvalues.put(DbContact.USER_PSWD, pswd);
        String selection = DbContact.USER_NAME + " LIKE ?";
        String[] selection_args = {name};
        database.update(DbContact.USER_TABLE_NAME, contentvalues, selection, selection_args);
    }
}
