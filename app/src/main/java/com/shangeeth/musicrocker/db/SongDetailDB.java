package com.shangeeth.musicrocker.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Shangeeth Sivan on 07/05/17.
 */

public class SongDetailDB extends SQLiteOpenHelper {
    public SongDetailDB(Context context) {
        super(context, SongDetailDB.class.getSimpleName(), null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        SongDetailTable.createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //IF needed upgrade the db
    }

}
