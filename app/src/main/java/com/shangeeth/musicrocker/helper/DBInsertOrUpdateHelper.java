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

public class DBInsertOrUpdateHelper {

    private static final String TAG = "DBInsertOrUpdateHelper";

    public void loadDataIntoDBFromContentProvider(Context pContext) {

        ProgressDialog lProgressDialog = new ProgressDialog(pContext);
        lProgressDialog.setMessage("Please Wait");
        lProgressDialog.setTitle("Loading");
        lProgressDialog.show();
        Cursor lCursor = pContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DURATION},
                null, null, MediaStore.Audio.Media.TITLE + " ASC");


        ArrayList<SongDetailsJDO> lList = new ArrayList<>();

        if (lCursor.moveToFirst()) {
            do {
                lList.add(new SongDetailsJDO(lCursor.getString(lCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)).trim(),
                        lCursor.getString(lCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)).trim(),
                        lCursor.getString(lCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)).trim(),
                        lCursor.getString(lCursor.getColumnIndex(MediaStore.Audio.Media._ID)).trim(),
                        lCursor.getInt(lCursor.getColumnIndex(MediaStore.Audio.Media.DURATION)), 0));
            } while (lCursor.moveToNext());
        }

        lCursor.close();

        SongDetailTable lSongDetailTable = new SongDetailTable(pContext);
        lSongDetailTable.insertSongs(lList);
        lProgressDialog.dismiss();

        Log.d(TAG, "loadDataIntoDBFromContentProvider: ==============");

    }
}
