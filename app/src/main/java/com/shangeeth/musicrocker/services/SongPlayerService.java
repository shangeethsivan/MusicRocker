package com.shangeeth.musicrocker.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.shangeeth.musicrocker.R;
import com.shangeeth.musicrocker.db.SongDetailTable;
import com.shangeeth.musicrocker.jdo.SongDetailsJDO;
import com.shangeeth.musicrocker.ui.PlayerActivity;
import com.shangeeth.musicrocker.ui.SongsListActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class SongPlayerService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private static final String TAG = "SongPlayerService";
    public static final int NOTIF_ID = 100;
    private MediaPlayer mMediaPlayer;
    private int mCurrentSongPosition = -1;
    private ArrayList<SongDetailsJDO> mSongDetailsJDOs;

    private Timer mTimer;
    private int mLoopState = 0;

    private final IBinder mIBinder = new MyBinder();
    static SongPlayerService mInstance;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mSharedPrefEditor;

    //Constants
    public static final int LOOP_STATE_NO_R = 0;
    public static final int LOOP_STATE_ALL_R = 1;
    public static final int LOOP_STATE_SELF_R = 2;

    private int mCurrentSeekDuration;

    private boolean mIsTimerRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        mInstance = this;

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);

        mSongDetailsJDOs = new SongDetailTable(this).getAllSongs();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mSharedPrefEditor = mSharedPreferences.edit();

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: =========");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        mMediaPlayer.release();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        if(!mIsTimerRunning)
            startTimer();
        return mIBinder;
    }


    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: ");
        stopTimer();
        stopServiceIfMusicPlayerNotPlaying();
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        if(!mIsTimerRunning)
            startTimer();
        Log.e(TAG, "onRebind: ");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {

        Log.d(TAG, "onTaskRemoved: ");
        if(!mMediaPlayer.isPlaying())
            removeSharedPrefs();

        stopServiceIfMusicPlayerNotPlaying();

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "onError: " + what + " " + extra);
        return true;
    }


    /**
     * Custom Binder class to be returned to the Activity while binding in OnServiceConnected method in @{@link android.content.ServiceConnection class}
     */
    public class MyBinder extends Binder {
        public SongPlayerService getService() {
            return SongPlayerService.this;
        }
    }


    public void loadSong(String pSongId, int pSongDuration) {
        Log.d(TAG, "loadSong: ");
        int lPosition = 0;
        for (int i = 0; i < mSongDetailsJDOs.size(); i++) {
            if (mSongDetailsJDOs.get(i).getSongId().equals(pSongId)) {
                lPosition = i;
                break;
            }
        }

        playSong(lPosition, pSongDuration);

    }

    public void addNotification(){
            /*
                Calling start foreground method to keep the song play active in background
             */
        Intent lIntent = new Intent(getApplicationContext(), SongsListActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent lPendingIntent = PendingIntent.getActivity(getApplicationContext(), 100, lIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification lBuilder = new Notification.Builder(getApplicationContext()).setContentTitle(getString(R.string.app_name))
                .setContentText(mSongDetailsJDOs.get(mCurrentSongPosition).getTitle())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(lPendingIntent)
                .build();

        startForeground(NOTIF_ID, lBuilder);
    }
    public void removeNotification(){
        stopForeground(true);
    }

    public void playSong(int pPosition, int pDuration) {

        stopTimer();

        if (mCurrentSongPosition != pPosition || mCurrentSongPosition == pPosition && !mMediaPlayer.isPlaying()) {

            mMediaPlayer.reset();


            Log.d(TAG, "playSong: positions ===== " + mCurrentSongPosition + " " + pPosition);
            mCurrentSongPosition = pPosition;
            try {
                Uri lUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.parseLong(mSongDetailsJDOs.get(mCurrentSongPosition).getSongId()));
                mMediaPlayer.setDataSource(getApplicationContext(), lUri);
                mMediaPlayer.prepare();

            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mMediaPlayer.start();
            mMediaPlayer.seekTo(mCurrentSeekDuration);
//            Log.d(TAG, "playSong: ===" + mCurrentSongPosition + " " + pDuration + " " + mMediaPlayer.getDuration());

            //Setting current seek duration to seek after preparation
            mCurrentSeekDuration = pDuration;

            addNotification();

            addSharedPreference();

            startTimer();

            //Send Broadcast containing the cong updated song details
            songUpdated(mSongDetailsJDOs.get(mCurrentSongPosition), true);

        }
        else{
            startTimer();
        }
    }


    public void addSharedPreference(){
        /*
        Setting the shared preference for keeping track of weather the song is playing
        */
        mSharedPrefEditor.putBoolean(getString(R.string.is_song_playing), true);
        mSharedPrefEditor.putString(getString(R.string.song_id), mSongDetailsJDOs.get(mCurrentSongPosition).getSongId());
        mSharedPrefEditor.apply();

    }

    /**
     * Checks for the loop status and plays the next song on the queue
     *
     * @param pCallFromActivity
     */
    public void chooseNextSong(boolean pCallFromActivity) {

        if (pCallFromActivity) {
            playNextSong();
        } else if (mLoopState == LOOP_STATE_SELF_R) {
            Log.d(TAG, "chooseNextSong: loop State 2");
            playSong(mCurrentSongPosition, 0);
        } else if (mLoopState == LOOP_STATE_ALL_R) {
            playNextSong();
        } else if (mLoopState == LOOP_STATE_NO_R && mCurrentSongPosition + 1 <= mSongDetailsJDOs.size()) {
            playNextSong();
        }
    }

    /**
     * Plays the next Available song
     */
    private void playNextSong() {
        Log.d(TAG, "playNextSong: Called");
        if (mCurrentSongPosition + 1 < mSongDetailsJDOs.size()) {
            playSong(mCurrentSongPosition + 1, 0);
        } else {
            playSong(0, 0);
        }
    }

    public void playPreviousSong() {
        if (mCurrentSongPosition - 1 >= 0) {
            Log.d(TAG, "playPreviousSong: playSong: ===called");
            playSong(mCurrentSongPosition - 1, 0);
        } else {
            playSong(mSongDetailsJDOs.size() - 1, 0);
        }
    }

    public void playOrPauseSong() {

        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            stopTimer();
            removeNotification();
            removeSharedPrefs();
        } else {
            mMediaPlayer.start();
            startTimer();
            addNotification();
            addSharedPreference();
        }

    }

    public void seekSong(int position) {
        mMediaPlayer.seekTo(position);
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
                        .putExtra(getString(R.string.is_new_song), pIsNewSong)
                        .putExtra(getString(R.string.loop_state), mLoopState)
                        .putExtra(getString(R.string.is_song_playing), mMediaPlayer.isPlaying()));

    }


    /**
     * Tracks the progress of the song and updates it to the activity in a 1000ms interval
     */
    public void startTimer() {
        mIsTimerRunning = true;
        Log.d(TAG, "startTimer: ");
        mTimer = null;
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mMediaPlayer != null) {
                    try {
                        int lCurrentDuration = mMediaPlayer.getCurrentPosition();

                        //Current position for seek bar
                        //TODO: Broadcast the data using localBroadcast Manager
                        LocalBroadcastManager.
                                getInstance(getApplicationContext())
                                .sendBroadcast(new Intent().setAction(PlayerActivity.RECEIVER_FILTER)
                                        .putExtra(getString(R.string.current_position_for_seek), lCurrentDuration));
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 500, 500);
    }



    public void stopTimer() {
        mIsTimerRunning = false;
        Log.d(TAG, "stopTimer: ");
        if (mTimer != null) {
            mTimer.purge();
            mTimer.cancel();
        }
    }
    public void updateFavInJDO(int pFavStatus) {
        mSongDetailsJDOs.get(mCurrentSongPosition).setFavouriteStatus(pFavStatus);
    }

    public void setLoopState(int pLoopState) {
        mLoopState = pLoopState;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "onCompletion: =====" + mCurrentSongPosition);

        //TODO: Loop functionality
        chooseNextSong(false);

        if (!mMediaPlayer.isPlaying()) {
            Log.d(TAG, "onCompletion: =====" + mLoopState);
            removeSharedPrefs();
            LocalBroadcastManager.getInstance(getApplicationContext())
                    .sendBroadcast(new Intent(PlayerActivity.RECEIVER_FILTER)
                            .putExtra(getString(R.string.song_ended), true));
        }

    }

    public void removeSharedPrefs() {
        Log.d(TAG, "removeSharedPrefs: Called");
        mSharedPrefEditor.remove(getString(R.string.is_song_playing));
        mSharedPrefEditor.apply();
    }

    /**
     * gets the current running instance
     *
     * @return returns the reference to the running instance of the service
     */
    public static SongPlayerService getRunningInstance() {
        return mInstance;
    }

    /**
     * Called when data is being updated in DB
     */
    public void favChanged(int pPosition, int pFavStatus) {
        mSongDetailsJDOs.get(pPosition).setFavouriteStatus(pFavStatus);
    }

    /**
     * If media player is not playing it stops the service
     */
    private void stopServiceIfMusicPlayerNotPlaying() {
        if (!mMediaPlayer.isPlaying()) {
            removeSharedPrefs();
            stopForeground(true);
            stopSelf();
        }

    }

}



