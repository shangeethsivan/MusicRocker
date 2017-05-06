package com.shangeeth.musicrocker.services;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.shangeeth.musicrocker.R;
import com.shangeeth.musicrocker.jdo.SongDetailsJDO;
import com.shangeeth.musicrocker.ui.PlayerActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class SongPlayerService extends Service implements MediaPlayer.OnCompletionListener {

    private static final String TAG = "SongPlayerService";

    private MediaPlayer mMediaPlayer;
    private int mCurrentSongPosition = -1;
    private boolean isSongPlaying = false;
    private ArrayList<SongDetailsJDO> mSongDetailsJDOs;

    private Timer mTimer;

    private final IBinder mIBinder = new MyBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(this);
        mSongDetailsJDOs = new ArrayList<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {

            mSongDetailsJDOs = (ArrayList<SongDetailsJDO>) intent.getSerializableExtra(getString(R.string.song_list));
            playSong(intent.getIntExtra(getString(R.string.position), 0));

            //Send Broadcast containing the cong updated song details
            songUpdated(mSongDetailsJDOs.get(mCurrentSongPosition), true);


        }
        return START_STICKY;
    }


    public void playSong(int pPosition) {

        if (mCurrentSongPosition != pPosition) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            //TODO: Request an update to the UI once all data is loaded

            mCurrentSongPosition = pPosition;

            try {
                Uri lUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.parseLong(mSongDetailsJDOs.get(mCurrentSongPosition).getSongId()));
                Log.e(TAG, "onStartCommand: " + lUri);
                mMediaPlayer.setDataSource(getApplicationContext(), lUri);
                mMediaPlayer.prepare();

            } catch (IOException e) {
                e.printStackTrace();
            }

            mMediaPlayer.start();
            isSongPlaying = true;

        }
    }


    public void playNextSong() {

        isSongPlaying = false;

        if (mCurrentSongPosition + 1 < mSongDetailsJDOs.size()) {

            playSong(mCurrentSongPosition + 1);
            Log.d(TAG, "playNextSong: " + mCurrentSongPosition);

            //Send Broadcast containing the cong updated song details
            songUpdated(mSongDetailsJDOs.get(mCurrentSongPosition), true);

        } else {
            Toast.makeText(getApplicationContext(), "No other song is in the list click Previous to go to Previous song", Toast.LENGTH_SHORT).show();
        }

    }

    public void playPreviousSong() {

        isSongPlaying = false;

        if (mCurrentSongPosition - 1 >= 0) {

            playSong(mCurrentSongPosition - 1);

            //Send Broadcast containing the cong updated song details
            songUpdated(mSongDetailsJDOs.get(mCurrentSongPosition), true);
        } else {
            Toast.makeText(getApplicationContext(), "No other song is in the list click next to go to next song", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        mMediaPlayer.release();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        mTimer = new Timer();

        trackProgress();

        return mIBinder;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {

        Log.d(TAG, "onTaskRemoved: ");
        if (!isSongPlaying)
            stopSelf();
    }

    @Override
    public void onRebind(Intent intent) {

        Log.e(TAG, "onRebind: ");
        mTimer = new Timer();
        trackProgress();
    }

    public void playOrPauseSong() {
        if (mMediaPlayer.isPlaying()) {
            isSongPlaying = false;
            mMediaPlayer.pause();
        } else {
            mMediaPlayer.start();
            isSongPlaying = true;
        }
    }

    public void seekSong(int position){

    }

    public class MyBinder extends Binder {
        public SongPlayerService getService() {
            return SongPlayerService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: ");
        mTimer.cancel();
        mTimer = null;
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "onCompletion: " + mCurrentSongPosition);
        isSongPlaying = false;

        //TODO: play next Song
        playNextSong();
//        mp.reset();
//        stopSelf();
    }


    /**
     * An update sent to the activity once there is a call to onStartCommand or if the song changes
     *
     * @param pSongDetailsJDO The details of the song which was fetched from a content provider
     * @param pIsNewSong      is it a new song
     */
    public void songUpdated(SongDetailsJDO pSongDetailsJDO, boolean pIsNewSong) {
        LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(new Intent().setAction(PlayerActivity.RECEIVER_FILTER)
                        .putExtra(getString(R.string.current_song_details), pSongDetailsJDO)
                        .putExtra(getString(R.string.is_new_song), pIsNewSong));
    }


    /**
     * Tracks the progress of the song and updates it to the activity in a 1000ms interval
     */
    public void trackProgress() {

        Log.d(TAG, "trackProgress: ");
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mMediaPlayer != null) {
                    int lCurrentDuration = mMediaPlayer.getCurrentPosition();

                    //Current position for seek bar
                    //TODO: Broadcast the data using localBroadcast Manager
                    LocalBroadcastManager.
                            getInstance(getApplicationContext())
                            .sendBroadcast(new Intent().setAction(PlayerActivity.RECEIVER_FILTER)
                                    .putExtra(getString(R.string.current_position_for_seek), lCurrentDuration));
                }
            }
        }, 200, 200);
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                Log.e(TAG, "run: =======" + mMediaPlayer + isSongPlaying);
//                if (mMediaPlayer != null) {
//
//                    int lCurrentDuration = mMediaPlayer.getCurrentPosition();
//
//                    //Current position for seek bar
//                    //TODO: Broadcast the data using localBroadcast Manager
//                    LocalBroadcastManager.
//                            getInstance(getApplicationContext())
//                            .sendBroadcast(new Intent().setAction(PlayerActivity.RECEIVER_FILTER)
//                                    .putExtra(getString(R.string.current_position_for_seek), lCurrentDuration));
//                }
//                mHandler.postDelayed(this, 1000);
//            }
//        });
    }

}


