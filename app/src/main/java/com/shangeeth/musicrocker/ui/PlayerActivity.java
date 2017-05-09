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
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.shangeeth.musicrocker.R;
import com.shangeeth.musicrocker.db.SongDetailTable;
import com.shangeeth.musicrocker.jdo.SongDetailsJDO;
import com.shangeeth.musicrocker.services.SongPlayerService;
import com.shangeeth.musicrocker.util.ConverterUtil;
import com.squareup.picasso.Picasso;

public class PlayerActivity extends AppCompatActivity {

    private TextView mTitleTv;
    private TextView mAlbumNameTv;
    private TextView mStartTimeTv;
    private TextView mEndTimeTv;

    private ImageView mAlbumIv;
    private ImageView mPreviousIv;
    private ImageView mPlayOrPauseIv;
    private ImageView mNextIv;
    private ImageView mFavIv;
    private ImageView mLoopIv;

    private SeekBar mSeekBar;

    private static final String TAG = "PlayerActivity";
    public static final String RECEIVER_FILTER = "com.shangeeth.musicrocker.ui.PlayerReceiver";

    private int mCurrentSongPositionInList = 0;
    private BroadcastReceiver mBroadcastReceiver;
    private Intent mIntent;

    private boolean isSongPlaying = false;

    public SongPlayerService mSongPlayerService;
    private ServiceConnection mServiceConnection;

    private String mCurrentSongId = "";
    private int mFavouriteStatus = 0;
    private boolean mDataChanged = false;
    public static int RESULT_CODE = 111;

    private int[] mLoopImages = {R.drawable.loop_disabled, R.drawable.loop_all, R.drawable.loop_one};
    int mCurrentLoopState = 0;

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
        mFavIv = (ImageView) findViewById(R.id.fav_iv);
        mLoopIv = (ImageView) findViewById(R.id.loop_iv);

        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);

        setOnClickListeners();

        if (getIntent().getExtras() != null) {

            Intent lIntent = getIntent();

            mCurrentSongPositionInList = lIntent.getIntExtra(getString(R.string.position), 0);

        } else {
            Log.e(TAG, "onCreate: No Data loaded");
        }

        initServiceCommunication();
        initReceivers();
        //Initially song will be playing set the play button active
        isSongPlaying = true;
        setPlayOrPauseImage();


    }

    private void setOnClickListeners() {

        mPlayOrPauseIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSongPlaying) {
                    isSongPlaying = false;
                    mSongPlayerService.playOrPauseSong();
                    setPlayOrPauseImage();
                } else {
                    isSongPlaying = true;
                    mSongPlayerService.playOrPauseSong();
                    setPlayOrPauseImage();
                }
            }
        });

        mNextIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSongPlayerService.playNextSong(true);
            }
        });

        mPreviousIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSongPlayerService.playPreviousSong();
            }
        });


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
                    mSongPlayerService.seekSong(mProgress);
            }
        });

        mFavIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDataChanged = true;

                if (mFavouriteStatus == 0)
                    mFavouriteStatus = 1;
                else
                    mFavouriteStatus = 0;
                setFavImage();
                updateFavDataInDb();
            }
        });

        mLoopIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentLoopState = (mCurrentLoopState >= 2 ? -1 : mCurrentLoopState) + 1;
                mSongPlayerService.setLoopState(mCurrentLoopState);
                mLoopIv.setImageResource(mLoopImages[mCurrentLoopState]);
            }
        });

    }


    /**
     * Initializes the components required to communicate with the service stars the service and binds it to this activity
     */
    private void initServiceCommunication() {

        mIntent = new Intent(this, SongPlayerService.class);

        startService(mIntent.putExtra(getString(R.string.position), mCurrentSongPositionInList));
        mServiceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

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

                    isSongPlaying = intent.getBooleanExtra(getString(R.string.is_song_playing), false);
                    setPlayOrPauseImage();

                    mCurrentSongId = lSongDetailsJDO.getSongId();
                    mFavouriteStatus = lSongDetailsJDO.getFavouriteStatus();
                    setFavImage();
                    //TODO: Set Current loop state here
                    mCurrentLoopState = intent.getIntExtra(getString(R.string.loop_state), 0);
                    mLoopIv.setImageResource(mLoopImages[mCurrentLoopState]);

                }


                if (intent.getBooleanExtra(getString(R.string.song_ended), false)) {
                    isSongPlaying = false;
                    setPlayOrPauseImage();
                }

            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(RECEIVER_FILTER));

    }

    private void setPlayOrPauseImage() {
        if (isSongPlaying)
            mPlayOrPauseIv.setImageResource(R.drawable.ic_pause_black_24dp);
        else
            mPlayOrPauseIv.setImageResource(R.drawable.ic_play);
    }

    private void setFavImage() {
        if (mFavouriteStatus == 1)
            mFavIv.setImageResource(R.drawable.fav);
        else
            mFavIv.setImageResource(R.drawable.fav_u);
    }

    private void updateFavDataInDb() {
        SongDetailTable lSongDetailTable = new SongDetailTable(this);
        lSongDetailTable.setFavouriteStatus(mCurrentSongId, mFavouriteStatus);
        mSongPlayerService.updateFavInJDO(mFavouriteStatus);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        unbindService(mServiceConnection);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CODE, new Intent().putExtra(getString(R.string.is_data_changed), mDataChanged));
        finish();
        overridePendingTransition(R.anim.scale_up,R.anim.to_right);
    }
}
