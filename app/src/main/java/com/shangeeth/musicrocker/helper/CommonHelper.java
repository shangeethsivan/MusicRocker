package com.shangeeth.musicrocker.helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.shangeeth.musicrocker.db.SongDetailTable;
import com.shangeeth.musicrocker.jdo.SongDetailsJDO;

import java.util.ArrayList;

/**
 * Created by Shangeeth Sivan on 07/05/17.
 */

public class CommonHelper {

    private static final String TAG = "CommonHelper";

    /**
     * Load the songs details from Content Provider to DB
     * @param pContext
     */
    public void loadSongToDB(Context pContext) {

        Cursor lCursor = pContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DURATION},
                null, null, MediaStore.Audio.Media.TITLE + " ASC");
        ArrayList<SongDetailsJDO> lList = new ArrayList<>();

        if (lCursor.moveToFirst()) {
            do {
                lList.add(new SongDetailsJDO(lCursor.getString(lCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)).trim(),
                        lCursor.getString(lCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                        lCursor.getString(lCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)),
                        lCursor.getString(lCursor.getColumnIndex(MediaStore.Audio.Media._ID)),
                        lCursor.getInt(lCursor.getColumnIndex(MediaStore.Audio.Media.DURATION)), 0));
            } while (lCursor.moveToNext());
        }

        lCursor.close();
        SongDetailTable lSongDetailTable = new SongDetailTable(pContext);
        lSongDetailTable.insertSongs(lList);

        Log.d(TAG, "loadSongToDB: ==============");
    }
}
