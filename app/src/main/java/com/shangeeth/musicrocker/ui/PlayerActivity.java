package com.shangeeth.musicrocker.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.shangeeth.musicrocker.R;

public class PlayerActivity extends AppCompatActivity {

    private TextView mTitleTV;
    private TextView mAlbumNameTV;
    private ProgressBar mProgressBar;
    private TextView mStartTimeTv;
    private TextView mEndTimeTv;
    private ImageView mPreviousSongIv;
    private ImageView mPlayOrPauseIv;
    private ImageView mNextIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_player);

        mTitleTV = (TextView) findViewById(R.id.title_tv);
        mAlbumNameTV = (TextView) findViewById(R.id.album_name_tv);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mStartTimeTv = (TextView) findViewById(R.id.start_time_tv);
        mEndTimeTv = (TextView) findViewById(R.id.end_time_tv);

        mPreviousSongIv = (ImageView) findViewById(R.id.previous_iv);
        mPlayOrPauseIv = (ImageView) findViewById(R.id.play_or_pause_iv);
        mNextIv = (ImageView) findViewById(R.id.next_iv);

        setOnClickListeners();
    }

    public void setOnClickListeners() {

        mPreviousSongIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mPlayOrPauseIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mNextIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }


}
