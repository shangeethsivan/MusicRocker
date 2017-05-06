package com.shangeeth.musicrocker.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.shangeeth.musicrocker.R;
import com.shangeeth.musicrocker.jdo.SongDetailsJDO;
import com.shangeeth.musicrocker.services.SongPlayerService;
import com.shangeeth.musicrocker.util.ConverterUtil;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {

    protected TextView mTitleTv;
    protected TextView mAlbumNameTv;
    protected TextView mStartTimeTv;
    protected TextView mEndTimeTv;

    protected ImageView mAlbumIv;
    protected ImageView mPreviousIv;
    protected ImageView mPlayOrPauseIv;
    protected ImageView mNextIv;

    protected SeekBar mSeekBar;

    private static final String TAG = "PlayerActivity";
    public static final String RECEIVER_FILTER = "com.shangeeth.musicrocker.ui.PlayerReceiver";

    private ArrayList<SongDetailsJDO> mSongDetailsJDOs;
    private int mCurrentSongPositionInList = 0;
    private BroadcastReceiver mBroadcastReceiver;
    private Intent mIntent;

    private boolean isSongPlaying = false;

    public SongPlayerService mSongPlayerService;
    private ServiceConnection mServiceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

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

        } else {
            Log.e(TAG, "onCreate: No Data loaded");
        }

        initServiceCommunication();
        initReceivers();
        //Initially song will be playing set the play button active
        isSongPlaying = true;
        mPlayOrPauseIv.setImageResource(R.drawable.ic_pause_black_24dp);

        setOnClickListeners();


    }

    private void setOnClickListeners() {

        mPlayOrPauseIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSongPlaying) {
                    isSongPlaying = false;
                    mSongPlayerService.playOrPauseSong();
                    mPlayOrPauseIv.setImageResource(R.drawable.ic_play);
                } else {
                    isSongPlaying = true;
                    mSongPlayerService.playOrPauseSong();
                    mPlayOrPauseIv.setImageResource(R.drawable.ic_pause_black_24dp);
                }
            }
        });

        mNextIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSongPlayerService.playNextSong();
            }
        });

        mPreviousIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSongPlayerService.playPreviousSong();
            }
        });

    }


    /**
     * Initializes the components required to communicate with the service stars the service and binds it to this activity
     */
    private void initServiceCommunication() {

        mIntent = new Intent(this, SongPlayerService.class);

        startService(mIntent.putExtra(getString(R.string.song_list), mSongDetailsJDOs).putExtra(getString(R.string.position), mCurrentSongPositionInList));
        mServiceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

                Log.d(TAG, "onServiceConnected: " + name);
                mSongPlayerService = ((SongPlayerService.MyBinder) service).getService();

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        bindService(mIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }


    /**
     * initialize the receiver with the
     */
    private void initReceivers() {

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getIntExtra(getString(R.string.current_position_for_seek), -1) != -1) {
                    int lTimeInMs = intent.getIntExtra(getString(R.string.current_position_for_seek), -1);
                    mStartTimeTv.setText(ConverterUtil.convertToString(lTimeInMs));
                    mSeekBar.setProgress(lTimeInMs);
                }
                if (intent.getBooleanExtra(getString(R.string.is_new_song), false)) {

                    SongDetailsJDO lSongDetailsJDO = (SongDetailsJDO) intent.getSerializableExtra(getString(R.string.current_song_details));
                    mTitleTv.setText(lSongDetailsJDO.getTitle());

                    if (lSongDetailsJDO.getAlbumName() != null)
                        mAlbumNameTv.setText(lSongDetailsJDO.getAlbumName());
                    else
                        mAlbumNameTv.setText("<Unknown>");

                    if (lSongDetailsJDO.getAlbumId() != null) {

                        Uri lAlbumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), Long.parseLong(lSongDetailsJDO.getAlbumId()));
                        Picasso.with(PlayerActivity.this).load(lAlbumArtUri).centerCrop().resize(800, 800).placeholder(R.drawable.placeholder).into(mAlbumIv);

                    } else
                        mAlbumIv.setImageResource(R.drawable.placeholder);

                    mSeekBar.setMax(lSongDetailsJDO.getDuration());
                    mEndTimeTv.setText(ConverterUtil.convertToString(lSongDetailsJDO.getDuration()));

                }

            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(RECEIVER_FILTER));

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        //TODO: unbindService();
        unbindService(mServiceConnection);
    }
}
