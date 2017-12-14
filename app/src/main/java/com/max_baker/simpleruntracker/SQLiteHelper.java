package com.max_baker.simpleruntracker;
/**
 * Written By: Max Baker
 * Last Modified; 12/7/17
 * Maintains the SQLite database of runs
 */


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

class SQLiteHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "contactsDB";
    private static final int DB_VERSION = 1;
    //table name
    private static final String TABLE_NAME = "runTable";
    //column names
    private static final String ID = "_id";
    private static final String MINUTES = "minutes";
    private static final String SECONDS= "seconds";
    private static final String DISTANCE = "distance";

    private static final String TAG = "Database";

    SQLiteHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlCreateTable = "CREATE TABLE "+TABLE_NAME +
        "( "+ID +" INTEGER PRIMARY KEY AUTOINCREMENT, " + MINUTES+" INTEGER, " + SECONDS+" INTEGER, "+ DISTANCE + " FLOAT" +")";
        Log.d(TAG, sqlCreateTable);
        db.execSQL(sqlCreateTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //skipped
    }

    void insertRun(Run run){
        String sqlInsert = "INSERT INTO " + TABLE_NAME +
                " VALUES (null, '" +run.getMinutes()+"','"+run.getSeconds()+ "','"+run.getDistance()+  "')";
        Log.d(TAG, sqlInsert);
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(sqlInsert);
        db.close();

    }

    private Cursor getSelectAllRunsCursor(){
        String sqlSelect = "SELECT * FROM "+ TABLE_NAME;
        Log.d(TAG, sqlSelect);
        SQLiteDatabase db =getReadableDatabase();
        return db.rawQuery(sqlSelect,null);
    }

    ArrayList<Run> selectAllRunsList()
    {
        ArrayList<Run> runs= new ArrayList<Run>();
        Cursor cursor = getSelectAllRunsCursor();
        while(cursor.moveToNext())
        {
            runs.add(new Run(cursor.getInt(1), cursor.getInt(2), cursor.getFloat(3)));
        }
        return runs;
    }

    void deleteAllRuns()
    {
        String sqlUpdate= "DELETE FROM " + TABLE_NAME ;
        SQLiteDatabase db= getWritableDatabase();
        Log.d(TAG,sqlUpdate);
        db.execSQL(sqlUpdate);
        db.close();
    }

}
