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
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.shangeeth.musicrocker.R;
import com.shangeeth.musicrocker.db.SongDetailTable;
import com.shangeeth.musicrocker.jdo.SongDetailsJDO;
import com.shangeeth.musicrocker.ui.PlayerActivity;
import com.shangeeth.musicrocker.ui.SongsListActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class SongPlayerService extends Service implements MediaPlayer.OnCompletionListener {

    private static final String TAG = "SongPlayerService";
    public static final int NOTIF_ID = 100;
    private MediaPlayer mMediaPlayer;
    private int mCurrentSongPosition = -1;
    private boolean isSongPlaying = false;
    private ArrayList<SongDetailsJDO> mSongDetailsJDOs;

    private Timer mTimer;
    private int mLoopState = 0;

    private final IBinder mIBinder = new MyBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(this);
        mSongDetailsJDOs = new ArrayList<>();
        mSongDetailsJDOs = new SongDetailTable(this).getAllSongs();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {

            playSong(intent.getIntExtra(getString(R.string.position), 0), intent.getIntExtra(getString(R.string.song_duration), 0));

            //Send Broadcast containing the cong updated song details
            songUpdated(mSongDetailsJDOs.get(mCurrentSongPosition), true);

        }
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
        mTimer = new Timer();

        trackProgress();

        return mIBinder;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {

        Log.d(TAG, "onTaskRemoved: ");
        if (!isSongPlaying) {
            removeSharedPrefs();
            stopForeground(true);
            stopSelf();
        }


//        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//        SharedPreferences.Editor lEditor = mSharedPreferences.edit();
//
//        Log.d(TAG, "onTaskRemoved: ====="+isSongPlaying+" "+Build.VERSION.SDK_INT+" "+ mCurrentSongPosition);
//        if (isSongPlaying && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            lEditor.putInt(getString(R.string.song_position), mCurrentSongPosition);
//            lEditor.putInt(getString(R.string.song_duration), mMediaPlayer.getCurrentPosition());
//        } else if (!isSongPlaying)
//            stopSelf();
//        else {
//            lEditor.remove(getString(R.string.song_position));
//        }
//        lEditor.apply();
    }

    @Override
    public void onRebind(Intent intent) {

        Log.e(TAG, "onRebind: ");
        mTimer = new Timer();
        trackProgress();
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


    public void playSong(int pPosition, int pDuration) {

        if (mCurrentSongPosition != pPosition || mCurrentSongPosition == pPosition && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();

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
            Log.d(TAG, "playSong: ======" + mCurrentSongPosition + " " + pDuration + " " + mMediaPlayer.getDuration());
            mMediaPlayer.seekTo(pDuration);
            isSongPlaying = true;


            /*
                Calling start foreground method to keep the song play active in background
             */
            Intent lIntent = new Intent(getApplicationContext(), SongsListActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent lPendingIntent = PendingIntent.getActivity(getApplicationContext(), 100, lIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification lBuilder = new Notification.Builder(getApplicationContext()).setContentTitle(mSongDetailsJDOs.get(mCurrentSongPosition).getTitle())
                    .setContentText(mSongDetailsJDOs.get(mCurrentSongPosition).getAlbumName())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(lPendingIntent)
                    .build();

            startForeground(NOTIF_ID, lBuilder);


            /*
            Setting the shared preference for keeping track of weather the song is playing
             */
            SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor lEditor = mSharedPreferences.edit();
            lEditor.putBoolean(getString(R.string.is_song_playing), true);
            lEditor.putInt(getString(R.string.position), mCurrentSongPosition);
            lEditor.apply();

        }
    }


    public int playNextSong(boolean pIsLoop) {

        isSongPlaying = false;

        if (pIsLoop) {
            playSong(mCurrentSongPosition, 0);
            songUpdated(mSongDetailsJDOs.get(mCurrentSongPosition), true);
            return 0;
        } else if (mCurrentSongPosition + 1 < mSongDetailsJDOs.size()) {

            playSong(mCurrentSongPosition + 1, 0);

            Log.d(TAG, "playNextSong: " + mCurrentSongPosition);

            //Send Broadcast containing the cong updated song details
            songUpdated(mSongDetailsJDOs.get(mCurrentSongPosition), true);

            return 0;

        } else {
            return -1;
        }

    }


    public void playPreviousSong() {

        isSongPlaying = false;

        if (mCurrentSongPosition - 1 >= 0) {

            playSong(mCurrentSongPosition - 1, 0);

            //Send Broadcast containing the cong updated song details
            songUpdated(mSongDetailsJDOs.get(mCurrentSongPosition), true);
        } else {
            Toast.makeText(getApplicationContext(), "No other song is in the list click next to go to next song", Toast.LENGTH_SHORT).show();
        }


    }


    public void playOrPauseSong() {
        if (mMediaPlayer.isPlaying()) {
            isSongPlaying = false;
            mMediaPlayer.pause();
            removeSharedPrefs();
        } else {
            mMediaPlayer.start();
            isSongPlaying = true;
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
    public void trackProgress() {

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
        isSongPlaying = false;

        //TODO: Loop functionality

        Log.d(TAG, "onCompletion: =====" + mLoopState);
        if (mLoopState == 2)
            playNextSong(true);
        else {
            int lSongStat = playNextSong(false);
            if (lSongStat == -1 && mLoopState == 1) {
                playSong(0, 0);
                songUpdated(mSongDetailsJDOs.get(mCurrentSongPosition), true);
            }
        }

        if (!mMediaPlayer.isPlaying()) {

            removeSharedPrefs();
        }

    }

    public void removeSharedPrefs() {

        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor lEditor = mSharedPreferences.edit();
        lEditor.remove(getString(R.string.is_song_playing));
        lEditor.apply();
    }
}



