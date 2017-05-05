package com.shangeeth.musicrocker.ui;

import android.content.ContentUris;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.shangeeth.musicrocker.R;
import com.shangeeth.musicrocker.jdo.SongDetailsJDO;
import com.shangeeth.musicrocker.services.SongPlayerService;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    private TextView mTitleTV;
    private TextView mAlbumNameTV;
    private TextView mStartTimeTv;
    private TextView mEndTimeTv;

    private SeekBar mSeekBar;

    private ImageView mPreviousSongIv;
    private ImageView mPlayOrPauseIv;
    private ImageView mNextIv;
    private ImageView mAlbumIV;

    private ArrayList<SongDetailsJDO> mSongDetailsJDOs;

    private int mCurrentSongPositionInList;

    private MediaPlayer mMediaPlayer;

    private boolean isSongPlaying = false;

    private static final String TAG = "PlayerActivity";

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTitleTV = (TextView) findViewById(R.id.title_tv);
        mAlbumNameTV = (TextView) findViewById(R.id.album_name_tv);
        mStartTimeTv = (TextView) findViewById(R.id.start_time_tv);
        mEndTimeTv = (TextView) findViewById(R.id.end_time_tv);


        mPreviousSongIv = (ImageView) findViewById(R.id.previous_iv);
        mPlayOrPauseIv = (ImageView) findViewById(R.id.play_or_pause_iv);
        mNextIv = (ImageView) findViewById(R.id.next_iv);
        mAlbumIV = (ImageView) findViewById(R.id.album_iv);

        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);

        mSongDetailsJDOs = new ArrayList<>();

        setOnClickListeners();


        mMediaPlayer = new MediaPlayer();
        mHandler = new Handler();

        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
        }

        mMediaPlayer.setOnCompletionListener(this);


        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            boolean mFromUser = false;
            int mProgress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mFromUser = fromUser;
                mProgress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mFromUser)
                    mMediaPlayer.seekTo(mProgress);
            }
        });

    }

    public void trackProgress() {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mMediaPlayer != null) {
                    int mCurrentPosition = mMediaPlayer.getCurrentPosition();
                    mSeekBar.setProgress(mCurrentPosition);

                    int lCurrentDuration = mMediaPlayer.getCurrentPosition();
                    //second Conversion

                    lCurrentDuration = lCurrentDuration / 1000;

                    int lSeconds = lCurrentDuration % 60;
                    int lMinutes = lCurrentDuration / 60;

                    mStartTimeTv.setText(String.format("%02d:%02d", lMinutes, lSeconds));
                }
                mHandler.postDelayed(this, 1000);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mHandler.removeCallbacksAndMessages(null);
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
    }

    private void loadSongDetails() {

        Log.e(TAG, "onCreate: " + mSongDetailsJDOs.get(mCurrentSongPositionInList) + " " + mCurrentSongPositionInList);
        Uri lUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.parseLong(mSongDetailsJDOs.get(mCurrentSongPositionInList).getSongId()));
        Log.e(TAG, "loadSongDetails: "+lUri );
        try {
            mMediaPlayer.setDataSource(this, lUri);
            mMediaPlayer.prepare();
            mSeekBar.setMax(mMediaPlayer.getDuration());
            trackProgress();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Song not found in device", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mTitleTV.setText(mSongDetailsJDOs.get(mCurrentSongPositionInList).getTitle());

        if (mSongDetailsJDOs.get(mCurrentSongPositionInList).getAlbumName() != null)
            mAlbumNameTV.setText(mSongDetailsJDOs.get(mCurrentSongPositionInList).getAlbumName());
        else
            mAlbumNameTV.setText("<unknown>");

        if (mSongDetailsJDOs.get(mCurrentSongPositionInList).getAlbumId() != null) {

            Uri lAlbumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), Long.parseLong(mSongDetailsJDOs.get(mCurrentSongPositionInList).getAlbumId()));
            Picasso.with(this).load(lAlbumArtUri).centerCrop().resize(800, 800).placeholder(R.drawable.placeholder).into(mAlbumIV);

        } else
            mAlbumIV.setImageResource(R.drawable.placeholder);

        mStartTimeTv.setText("00:00");

        int lSongDuration = mMediaPlayer.getDuration();

        //to seconds Conversion
        lSongDuration = lSongDuration / 1000;

        int lSeconds = lSongDuration % 60;
        int lMinutes = lSongDuration / 60;

        mEndTimeTv.setText(String.format("%02d:%02d", lMinutes, lSeconds));

    }


    public void setOnClickListeners() {

        mPreviousSongIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isSongPlaying = false;
                mMediaPlayer.reset();

                if (mCurrentSongPositionInList - 1 >= 0) {

                    mCurrentSongPositionInList = mCurrentSongPositionInList - 1;
                    loadSongDetails();
                    playOrPauseSong();

                } else {
                    loadSongDetails();
                    mPlayOrPauseIv.setImageResource(R.drawable.ic_play);
                    Toast.makeText(PlayerActivity.this, "No other song is in the list click next to go to next song", Toast.LENGTH_SHORT).show();
                }
            }


        });
        mPlayOrPauseIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                playOrPauseSong();
                startService(new Intent(PlayerActivity.this, SongPlayerService.class));
            }
        });

        mNextIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isSongPlaying = false;
                mMediaPlayer.reset();

                if (mCurrentSongPositionInList + 1 < mSongDetailsJDOs.size()) {

                    mCurrentSongPositionInList = mCurrentSongPositionInList + 1;
                    loadSongDetails();
                    playOrPauseSong();

                } else {
                    loadSongDetails();
                    mPlayOrPauseIv.setImageResource(R.drawable.ic_play);
                    Toast.makeText(PlayerActivity.this, "No other song is in the list click Previous to go to Previous song", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void playOrPauseSong() {

        if (isSongPlaying) {
            isSongPlaying = false;
            mMediaPlayer.pause();
            mPlayOrPauseIv.setImageResource(R.drawable.ic_play);
        } else {
            isSongPlaying = true;
            mMediaPlayer.start();
            mPlayOrPauseIv.setImageResource(R.drawable.ic_pause_black_24dp);
        }
    }


    @Override
    public void onCompletion(MediaPlayer mp) {

        isSongPlaying = false;
        Log.e(TAG, "onCompletion: " + mp);

        mMediaPlayer.reset();

        if (mCurrentSongPositionInList != mSongDetailsJDOs.size() - 1) {

            mCurrentSongPositionInList = mCurrentSongPositionInList + 1;
            loadSongDetails();
            playOrPauseSong();

        } else {
            loadSongDetails();
            mPlayOrPauseIv.setImageResource(R.drawable.ic_play);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.scale_up, R.anim.to_right);
    }
}
