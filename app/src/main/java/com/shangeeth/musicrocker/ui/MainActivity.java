package com.shangeeth.musicrocker.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.shangeeth.musicrocker.R;
import com.shangeeth.musicrocker.adapters.MyRecViewAdapter;

public class MainActivity extends AppCompatActivity {

    protected RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        } else {
            loadSongList();
        }

    }

    private void loadSongList() {
        new LoadMediaInBackground().execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0) {
            boolean lGranted = true;
            for (int lResult : grantResults) {
                if (lResult == PackageManager.PERMISSION_DENIED) {
                    lGranted = false;
                }
            }
            if (lGranted) {
                loadSongList();
            }
        }
    }


    private class LoadMediaInBackground extends AsyncTask<Void, Void, Void> {

        ProgressDialog mProgressDialog;
        private Cursor mCursor;

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setTitle("Please Wait");
            mProgressDialog.setMessage("Loading ...");
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            ContentResolver lContentResolver = getContentResolver();
            mCursor = lContentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.ALBUM_ID},
                    null, null, MediaStore.Audio.Media.TITLE+" ASC");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mProgressDialog.dismiss();
            mRecyclerView = (RecyclerView) findViewById(R.id.rec_view);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
            mRecyclerView.setAdapter(new MyRecViewAdapter(MainActivity.this, mCursor));

        }
    }
}
