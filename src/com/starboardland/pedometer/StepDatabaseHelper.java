package com.starboardland.pedometer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Jun on 3/19/2015.
 */
public class StepDatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "steps.sqlite";
    public static final String TABLE_STEP = "step";
    public static final String COL_COUNT = "count";
    public static final int VERSION = 1;
    private static StepDatabaseHelper db;

    private StepDatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    public static StepDatabaseHelper getInstance(Context context){
        if(db == null){
            db = new StepDatabaseHelper(context);
        }
        return db;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //crate the steps table
        sqLiteDatabase.execSQL("create table " + TABLE_STEP + "(_id integer primary key autoincrement," + COL_COUNT +" float)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        //nothing needs to be done (as for now)
    }

    public boolean insertSteps(float count){
        ContentValues cv = new ContentValues( );
        cv.put(COL_COUNT, count);
        getWritableDatabase().insert(TABLE_STEP, null, cv);
        return false;
    }

    //todo may be useful if we wantto clean the count every time we shut down the app
//    public void clearDB(){
//
//    }

    public float getLastCount(){
        Cursor cur = getReadableDatabase().query(DB_NAME,null,null,null,null,null,null );
        return cur.getFloat(1);
    }
}
