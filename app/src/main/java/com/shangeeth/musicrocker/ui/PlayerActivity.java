package com.shangeeth.musicrocker.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.shangeeth.musicrocker.R;
import com.shangeeth.musicrocker.db.SongDetailTable;
import com.shangeeth.musicrocker.jdo.SongDetailsJDO;
import com.shangeeth.musicrocker.services.SongPlayerService;
import com.shangeeth.musicrocker.util.ConverterUtil;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {

    private TextView mTitleTv;
    private TextView mAlbumNameTv;
    private TextView mStartTimeTv;
    private TextView mEndTimeTv;

    private ImageView mAlbumIv;
    private ImageView mAlbumIv2;
    private ImageView mPreviousIv;
    private ImageView mPlayOrPauseIv;
    private ImageView mNextIv;
    private ImageView mFavIv;
    private ImageView mLoopIv;

    private SeekBar mSeekBar;

    private static final String TAG = "PlayerActivity";
    public static final String RECEIVER_FILTER = "com.shangeeth.musicrocker.ui.PlayerReceiver";

    private BroadcastReceiver mBroadcastReceiver;
    private Intent mIntent;

    private boolean mIsSongPlaying = false;

    public SongPlayerService mSongPlayerService;
    private ServiceConnection mServiceConnection;

    private String mCurrentSongId = "";
    private int mFavouriteStatus = 0;
    private boolean mDataChanged = false;
    public static int RESULT_CODE = 111;

    private int[] mLoopImages = {R.drawable.loop_disabled, R.drawable.loop_all, R.drawable.loop_one};
    int mCurrentLoopState = 0;

    boolean mIsActivityOpenedFirstTime = true;
    private Animation lAnimationNext;
    private Animation lAnimationNext2;
    private Animation lAnimationPrevious;
    private Animation lAnimationPrevious2;

    private SharedPreferences mSharedPreference;
    private SharedPreferences.Editor mSharedPrefEditor;
    private SongDetailsJDO mCurrentSongJDO;

    String mCurrentAlbumId = null;


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
        mAlbumIv2 = (ImageView) findViewById(R.id.album_iv_2);
        mPreviousIv = (ImageView) findViewById(R.id.previous_iv);
        mPlayOrPauseIv = (ImageView) findViewById(R.id.play_or_pause_iv);
        mNextIv = (ImageView) findViewById(R.id.next_iv);
        mFavIv = (ImageView) findViewById(R.id.fav_iv);
        mLoopIv = (ImageView) findViewById(R.id.loop_iv);

        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);


        setOnClickListeners();
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mSharedPrefEditor = mSharedPreference.edit();
        mSharedPrefEditor.apply();

        if (getIntent().getExtras() != null) {
            Intent lIntent = getIntent();
            mCurrentSongJDO = (SongDetailsJDO) lIntent.getSerializableExtra(getString(R.string.song_jdo));
            mCurrentSongId = mCurrentSongJDO.getSongId();
            loadSongDetails();

        } else {
            Log.e(TAG, "onCreate: No Data loaded");
        }

        initServiceCommunication();
        initReceivers();

        mIsSongPlaying = true;
        setPlayOrPauseImage();

    }

    private void setOnClickListeners() {

        mPlayOrPauseIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsSongPlaying) {
                    mIsSongPlaying = false;
                    mSongPlayerService.playOrPauseSong();
                    setPlayOrPauseImage();
                } else {
                    mIsSongPlaying = true;
                    mSongPlayerService.playOrPauseSong();
                    setPlayOrPauseImage();
                }
            }
        });
        lAnimationNext = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.from_right);
        lAnimationNext2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.to_left);
        lAnimationNext.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                loadImage();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mNextIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSeekBar.setProgress(0);
                mSongPlayerService.chooseNextSong(true);

                mAlbumIv2.startAnimation(lAnimationNext);
                mAlbumIv.startAnimation(lAnimationNext2);

            }
        });

        lAnimationPrevious = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.from_left);
        lAnimationPrevious2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.to_right);
        lAnimationPrevious.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                loadImage();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mPreviousIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: =====");
                mSeekBar.setProgress(0);
                mSongPlayerService.playPreviousSong();
                mAlbumIv2.startAnimation(lAnimationPrevious);
                mAlbumIv.startAnimation(lAnimationPrevious2);

            }
        });


        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            boolean mFromUser = false;
            int mProgress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mFromUser = fromUser;
                mProgress = progress;
                mStartTimeTv.setText(ConverterUtil.convertToString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mSongPlayerService.stopTimer();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSongPlayerService.startTimer();
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
                mCurrentLoopState = (mCurrentLoopState >= SongPlayerService.LOOP_STATE_SELF_R ? -1 : mCurrentLoopState) + 1;
                mSongPlayerService.setLoopState(mCurrentLoopState);
                mLoopIv.setImageResource(mLoopImages[mCurrentLoopState]);
            }
        });

    }


    /**
     * Initializes the components required to communicate with the service stars the service and binds it to this activity
     */
    private void initServiceCommunication() {

        mServiceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

                mSongPlayerService = ((SongPlayerService.MyBinder) service).getService();
                mSongPlayerService.loadSong(mCurrentSongId, 0);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        mIntent = new Intent(this, SongPlayerService.class);


        if (!mSharedPreference.getBoolean(getString(R.string.is_song_playing), false))
            startService(mIntent);

        bindService(mIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }


    /**
     * initialize the receiver with the
     */
    private void initReceivers() {

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Log.d(TAG, "onReceive: ");

                if (intent.getIntExtra(getString(R.string.current_position_for_seek), -1) != -1) {
                    int lTimeInMs = intent.getIntExtra(getString(R.string.current_position_for_seek), -1);
                    mStartTimeTv.setText(ConverterUtil.convertToString(lTimeInMs));
                    mSeekBar.setProgress(lTimeInMs);
                }

                if (intent.getBooleanExtra(getString(R.string.is_new_song), false)) {

                    mCurrentSongJDO = (SongDetailsJDO) intent.getSerializableExtra(getString(R.string.current_song_details));

                    loadSongDetails();

                    mIsSongPlaying = intent.getBooleanExtra(getString(R.string.is_song_playing), false);
                    setPlayOrPauseImage();
                    //TODO: Set Current loop state here
                    mCurrentLoopState = intent.getIntExtra(getString(R.string.loop_state), 0);
                    mLoopIv.setImageResource(mLoopImages[mCurrentLoopState]);

                }


                if (intent.getBooleanExtra(getString(R.string.song_ended), false)) {
                    mIsSongPlaying = false;
                    setPlayOrPauseImage();
                }

            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(RECEIVER_FILTER));

    }

    private void loadSongDetails() {

        mTitleTv.setText(mCurrentSongJDO.getTitle());

        if (mCurrentSongJDO.getAlbumName() != null)
            mAlbumNameTv.setText(mCurrentSongJDO.getAlbumName());
        else
            mAlbumNameTv.setText("<Unknown>");

        if (mIsActivityOpenedFirstTime) {
            mIsActivityOpenedFirstTime = false;

            if (mCurrentSongJDO.getAlbumId() != null) {

                Uri lAlbumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), Long.parseLong(mCurrentSongJDO.getAlbumId()));
                Picasso.with(PlayerActivity.this).load(lAlbumArtUri).centerCrop().resize(800, 800).placeholder(R.drawable.placeholder).into(mAlbumIv);
                Picasso.with(PlayerActivity.this).load(lAlbumArtUri).centerCrop().resize(800, 800).placeholder(R.drawable.placeholder).into(mAlbumIv2);

            } else
                mAlbumIv.setImageResource(R.drawable.placeholder);

        } else {

            if (mCurrentSongJDO.getAlbumId() != null) {
                mCurrentAlbumId = mCurrentSongJDO.getAlbumId();
                Uri lAlbumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), Long.parseLong(mCurrentSongJDO.getAlbumId()));
                Picasso.with(PlayerActivity.this).load(lAlbumArtUri).centerCrop().resize(800, 800).placeholder(R.drawable.placeholder).into(mAlbumIv2);

            } else {
                mAlbumIv2.setImageResource(R.drawable.placeholder);
                mCurrentAlbumId = null;
            }

        }
        mSeekBar.setMax(mCurrentSongJDO.getDuration());
        mEndTimeTv.setText(ConverterUtil.convertToString(mCurrentSongJDO.getDuration()));

        mCurrentSongId = mCurrentSongJDO.getSongId();
        mFavouriteStatus = mCurrentSongJDO.getFavouriteStatus();
        Log.d(TAG, "onReceive: " + mFavouriteStatus + "===== " + mCurrentSongId);
        setFavImage();

    }

    public void loadImage() {
        if (mCurrentAlbumId != null || !mCurrentAlbumId.equals("")) {
            Uri lAlbumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), Long.parseLong(mCurrentAlbumId));
            Picasso.with(PlayerActivity.this).load(lAlbumArtUri).centerCrop().resize(800, 800).placeholder(R.drawable.placeholder).into(mAlbumIv);
        } else {
            mAlbumIv.setImageResource(R.drawable.placeholder);
        }

    }

    private void setPlayOrPauseImage() {
        if (mIsSongPlaying)
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
        overridePendingTransition(R.anim.scale_up, R.anim.to_right);
    }
}
