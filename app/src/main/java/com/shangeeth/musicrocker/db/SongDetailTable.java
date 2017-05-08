package com.shangeeth.musicrocker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.shangeeth.musicrocker.jdo.SongDetailsJDO;

import java.util.ArrayList;

/**
 * Created by Shangeeth Sivan on 07/05/17.
 */

public class SongDetailTable {

    Context mContext;

    private static final String TAG = "SongDetailTable";

    public static final String TABLE_NAME = "song_details";
    public static final String _ID = "id";
    public static final String SONG_ID = "song_id";
    public static final String TITLE = "title";
    public static final String ALBUM_NAME = "album_name";
    public static final String ALBUM_ID = "album_id";
    public static final String DURATION = "duration";
    public static final String FAVOURITE = "favourite";

    private static final String SQL_CREATE_ENTRIES = " CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
            + SONG_ID + " INT PRIMARY KEY,"
            + TITLE + " TEXT,"
            + ALBUM_NAME + " TEXT,"
            + ALBUM_ID + " INT,"
            + DURATION + " INT,"
            + FAVOURITE + " INT)";


    public SongDetailTable(Context mContext) {
        this.mContext = mContext;
    }


    public static void createTable(SQLiteDatabase pDb) {
        pDb.execSQL(SQL_CREATE_ENTRIES);
    }


    /**
     * Insert the data from the @pSongDetailsJDOs into the @TABLE
     *
     * @param pSongDetailsJDOs Arraylist of SongDetailJDO
     */
    public void insertRows(ArrayList<SongDetailsJDO> pSongDetailsJDOs) {

        SQLiteDatabase lSqLiteDatabase = new SongDetailDB(mContext).getWritableDatabase();
        lSqLiteDatabase.beginTransaction();
        try {
            for (SongDetailsJDO lSongDetailsJDO : pSongDetailsJDOs) {

                ContentValues lContentValues = new ContentValues();

                lContentValues.put(SONG_ID, lSongDetailsJDO.getSongId());
                lContentValues.put(TITLE, lSongDetailsJDO.getTitle());
                lContentValues.put(ALBUM_NAME, lSongDetailsJDO.getAlbumName());
                lContentValues.put(ALBUM_ID, lSongDetailsJDO.getAlbumId());
                lContentValues.put(DURATION, lSongDetailsJDO.getDuration());

                lSqLiteDatabase.insert(TABLE_NAME, null, lContentValues);
            }
            lSqLiteDatabase.setTransactionSuccessful();
        } finally {
            lSqLiteDatabase.endTransaction();
        }
        lSqLiteDatabase.close();
    }

    /**
     * Fetches all the songs available in db
     *
     * @return returns the song list as a arraylist of @{@link SongDetailsJDO}
     */
    public ArrayList<SongDetailsJDO> getAllSongs() {

        ArrayList<SongDetailsJDO> lSongDetailsJDOs = new ArrayList<>();
        SQLiteDatabase lSqLiteDatabase = new SongDetailDB(mContext).getReadableDatabase();

        Cursor lCursor = lSqLiteDatabase.query(TABLE_NAME, null, null, null, null, null, TITLE + " ASC");
        if (lCursor.moveToFirst()) {
            do {
                lSongDetailsJDOs.add(new SongDetailsJDO(
                        lCursor.getString(lCursor.getColumnIndex(TITLE)),
                        lCursor.getString(lCursor.getColumnIndex(ALBUM_NAME)),
                        lCursor.getString(lCursor.getColumnIndex(ALBUM_ID)),
                        lCursor.getString(lCursor.getColumnIndex(SONG_ID)),
                        lCursor.getInt(lCursor.getColumnIndex(DURATION)),
                        lCursor.getInt(lCursor.getColumnIndex(FAVOURITE))));
            } while (lCursor.moveToNext());
        }
        return lSongDetailsJDOs;
    }

    /**
     * Sets the favourite status of the song
     *
     * @param pSongId    the song Id
     * @param pFavStatus the favourite status
     */
    public void setFavouriteStatus(String pSongId, int pFavStatus) {

        Log.d(TAG, "setFavouriteStatus: ======"+pSongId +" "+pFavStatus);
        SQLiteDatabase lSqLiteDatabase = new SongDetailDB(mContext).getWritableDatabase();

        ContentValues lValues = new ContentValues();
        lValues.put(FAVOURITE, pFavStatus);

        lSqLiteDatabase.update(TABLE_NAME, lValues, SONG_ID + "=?", new String[]{String.valueOf(pSongId)});

        lSqLiteDatabase.close();
    }
}
