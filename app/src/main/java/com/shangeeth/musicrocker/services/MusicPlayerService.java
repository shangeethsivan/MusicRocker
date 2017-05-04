package com.shangeeth.musicrocker.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.IntDef;
import android.util.Log;
import android.widget.Toast;

import com.shangeeth.musicrocker.R;

import java.io.IOException;


/**
 * Created by user on 04/05/17.
 */

public class MusicPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    MediaPlayer mMediaPlayer;
    private static final String TAG = "MusicPlayerService";


    private void initializePlayer(Uri pSongUri) {

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        if (mMediaPlayer.isPlaying())
            mMediaPlayer.stop();
        try {
            mMediaPlayer.setDataSource(getApplicationContext(), pSongUri);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand: " );

        String lUri = intent.getStringExtra(getString(R.string.music_uri));
        if (!lUri.equals("")) {
            initializePlayer(Uri.parse(lUri));
        } else
            Toast.makeText(getApplicationContext(), "Error in initializing the player\nSong Uri empty",
                    Toast.LENGTH_SHORT).show();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind: " );
        return null;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "onError: " + extra);
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }
}
