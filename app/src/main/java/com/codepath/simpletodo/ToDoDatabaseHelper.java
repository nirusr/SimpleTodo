package com.codepath.simpletodo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sgovind on 9/28/15.
 */
public class ToDoDatabaseHelper extends SQLiteOpenHelper  {

    private final static String DB_NAME="TODO";
    private final static int DB_VERSION = 1;
    private final static String createTodoTbl  =
            ("CREATE TABLE TODO ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "NAME TEXT, " +
                    "STATUS INTEGER);");


    //Create a database
    public ToDoDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    //Create table
        db.execSQL(createTodoTbl);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }
}
