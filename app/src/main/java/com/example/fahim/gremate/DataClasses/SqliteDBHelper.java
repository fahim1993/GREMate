package com.example.fahim.gremate.DataClasses;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by fahim on 23-Aug-17.
 */

public class SqliteDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "image.db";
    private static final String DB_TABLE = "table_image";

    private static final String KEY_WORDID = "wordid";
    private static final String KEY_URL = "url";
    private static final String KEY_IMAGE = "image";

    protected SQLiteDatabase database;

    private static final String CREATE_TABLE = "CREATE TABLE " + DB_TABLE + "(" +
            "id INTEGER PRIMARY KEY, "+
            KEY_WORDID + " TEXT, "+
            KEY_URL + " TEXT, "+
            KEY_IMAGE + " BLOB);";

    SqliteDBHelper(Context context){
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    void addImage( String wordId, String url, WordImage image) throws SQLiteException {
        if(database==null)database = this.getWritableDatabase();
        if(!image.isValid())return;
        WordImage tmp = getImage(wordId, url);
        if(tmp.isValid())return;

        ContentValues cv = new  ContentValues();
        cv.put(KEY_WORDID,    wordId);
        cv.put(KEY_URL,    url);
        cv.put(KEY_IMAGE,   image.getImageByteArray());
        database.insert( DB_TABLE, null, cv );
    }

    WordImage getImage(String wordId, String url){
        WordImage ret = new WordImage();
        if(database==null)database = this.getWritableDatabase();

        String query = "SELECT "+KEY_IMAGE+" FROM "+DB_TABLE+" WHERE "+KEY_URL+" = '"+url+"' AND " + KEY_WORDID + " = '" + wordId +"'";
        Cursor cursor = database.rawQuery( query, null );
        if (cursor.moveToFirst()) {
            ret.setImage(cursor.getBlob(cursor.getColumnIndex(KEY_IMAGE)));
        }
        cursor.close();
        return ret;
    }

    public void close(){
        if(database!=null)database.close();
    }
}
