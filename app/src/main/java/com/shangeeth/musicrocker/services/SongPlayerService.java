package com.shangeeth.musicrocker.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;


public class SongPlayerService extends Service {

    private static final String TAG = "SongPlayerService";
    MediaPlayer mMediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate: ");
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(getApplicationContext(), Uri.parse("content://media/external/audio/media/21"));
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand: ");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: ");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
