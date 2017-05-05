package com.shangeeth.musicrocker.ui;

import android.Manifest;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.shangeeth.musicrocker.R;
import com.shangeeth.musicrocker.adapters.MyRecViewAdapter;
import com.shangeeth.musicrocker.jdo.SongDetailsJDO;
import com.shangeeth.musicrocker.listeners.MyRecyclerViewOnClickListener;

import java.util.ArrayList;

public class SongsListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 101;
    protected RecyclerView mRecyclerView;
    private MyRecViewAdapter mAdapter;
    private ArrayList<SongDetailsJDO> mSongDetailsJDOs;
    private static final String TAG = "SongsListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.rec_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(SongsListActivity.this));
        mSongDetailsJDOs = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        } else {
            getLoaderManager().initLoader(LOADER_ID, null, this);

        }

        mAdapter = new MyRecViewAdapter(SongsListActivity.this, null);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnItemTouchListener(new MyRecyclerViewOnClickListener(this, new MyRecyclerViewOnClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                startActivity(new Intent(SongsListActivity.this, PlayerActivity.class)
                        .putExtra(getString(R.string.song_list), mSongDetailsJDOs)
                        .putExtra(getString(R.string.position), position));
                overridePendingTransition(R.anim.from_right, R.anim.scale_down);
            }
        }));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0) {
            boolean lGranted = true;
            for (int lResult : grantResults) {
                if (lResult == PackageManager.PERMISSION_DENIED)
                    lGranted = false;
            }
            if (lGranted)
                getLoaderManager().initLoader(0, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.e(TAG, "onCreateLoader: ");
        return new CursorLoader(this, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ALBUM_ID},
                null, null, MediaStore.Audio.Media.TITLE + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.e(TAG, "onLoadFinished: ");
        loadDataToArrayList(data);
        mAdapter.swapData(mSongDetailsJDOs);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.e(TAG, "onLoaderReset: ");
        mAdapter.swapData(null);
    }

    public void loadDataToArrayList(Cursor pCursor) {
        mSongDetailsJDOs.clear();
        if (pCursor.moveToFirst()) {
            do {
                mSongDetailsJDOs.add(new SongDetailsJDO(pCursor.getString(pCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                        pCursor.getString(pCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)), pCursor.getString(pCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)), pCursor.getString(pCursor.getColumnIndex(MediaStore.Audio.Media._ID))));

            } while (pCursor.moveToNext());
        }
    }
}
