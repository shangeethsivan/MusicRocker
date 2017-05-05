package com.shangeeth.musicrocker.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.shangeeth.musicrocker.R;
import com.shangeeth.musicrocker.jdo.SongDetailsJDO;

import java.util.ArrayList;

public class PlayerActivityNew extends AppCompatActivity {

    protected TextView mTitleTv;
    protected TextView mAlbumNameTv;
    protected TextView mStartTimeTv;
    protected TextView mEndTimeTv;

    protected ImageView mAlbumIv;
    protected ImageView mPreviousIv;
    protected ImageView mPlayOrPauseIv;
    protected ImageView mNextIv;

    protected SeekBar mSeekBar;

    private static final String TAG = "PlayerActivityNew";
    public static final String RECEIVER_FILTER = "com.shangeeth.musicrocker.ui.PlayerReceiver";

    private ArrayList<SongDetailsJDO> mSongDetailsJDOs;
    private int mCurrentSongPositionInList = 0;
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_player_new);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTitleTv = (TextView) findViewById(R.id.title_tv);
        mAlbumNameTv = (TextView) findViewById(R.id.album_name_tv);
        mStartTimeTv = (TextView) findViewById(R.id.start_time_tv);
        mEndTimeTv = (TextView) findViewById(R.id.end_time_tv);

        mAlbumIv = (ImageView) findViewById(R.id.album_iv);
        mPreviousIv = (ImageView) findViewById(R.id.previous_iv);
        mPlayOrPauseIv = (ImageView) findViewById(R.id.play_or_pause_iv);
        mNextIv = (ImageView) findViewById(R.id.next_iv);

        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);

        mSongDetailsJDOs = new ArrayList<>();

        if (getIntent().getExtras() != null) {

            Intent lIntent = getIntent();

            mSongDetailsJDOs = (ArrayList<SongDetailsJDO>) lIntent.getSerializableExtra(getString(R.string.song_list));
            mCurrentSongPositionInList = lIntent.getIntExtra(getString(R.string.position), 0);

//            loadSongDetails();
//            playOrPauseSong();

        } else {
            Log.e(TAG, "onCreate: No Data loaded");
        }

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getBooleanExtra(getString(R.string.is_new_song), false)) {

                    SongDetailsJDO mSongDetailsJDO = (SongDetailsJDO) intent.getSerializableExtra(getString(R.string.current_song_details));
                    mTitleTv.setText(mSongDetailsJDO.getTitle());
                    mAlbumNameTv.setText(mSongDetailsJDO.getAlbumName());

                }

            }
        };

        registerReceiver(mBroadcastReceiver, new IntentFilter(RECEIVER_FILTER));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }
}
