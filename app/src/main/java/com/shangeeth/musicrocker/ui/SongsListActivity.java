package com.shangeeth.musicrocker.ui;

import android.Manifest;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;

import com.shangeeth.musicrocker.R;
import com.shangeeth.musicrocker.adapters.MyRecViewAdapter;
import com.shangeeth.musicrocker.db.SongDetailTable;
import com.shangeeth.musicrocker.helper.DBInsertOrUpdateHelper;
import com.shangeeth.musicrocker.jdo.SongDetailsJDO;
import com.shangeeth.musicrocker.listeners.MyRecyclerViewOnClickListener;

import java.util.ArrayList;

public class SongsListActivity extends AppCompatActivity {


    protected RecyclerView mRecyclerView;
    private MyRecViewAdapter mAdapter;
    private ArrayList<SongDetailsJDO> mSongDetailsJDOs;
    private TextView mNoSongTV;

    private static final String TAG = "SongsListActivity";
    private int REQUEST_CODE = 101;
    private static final int LOADER_ID = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.rec_view);
        mNoSongTV = (TextView) findViewById(R.id.no_song_tv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(SongsListActivity.this));
        mSongDetailsJDOs = new ArrayList<>();

        mRecyclerView.addOnItemTouchListener(new MyRecyclerViewOnClickListener(this, new MyRecyclerViewOnClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                startActivityForResult(new Intent(SongsListActivity.this, PlayerActivity.class)
                        .putExtra(getString(R.string.position), position), REQUEST_CODE);
                overridePendingTransition(R.anim.from_right, R.anim.scale_down);
            }
        }));


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        } else
            loadData();

    }


    /**
     * Load data into DB if activity opened for first time else load data into recyclerView from DB
     */
    private void loadData() {

        SharedPreferences lSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean lIsAppLoadingFirstTime = lSharedPreferences.getBoolean(getString(R.string.is_app_loading_first_time), true);

        if (lIsAppLoadingFirstTime) {
            SharedPreferences.Editor lEditor = lSharedPreferences.edit();
            lEditor.putBoolean(getString(R.string.is_app_loading_first_time), false);
            lEditor.apply();

            //Load data into Database
            DBInsertOrUpdateHelper lHelper = new DBInsertOrUpdateHelper();
            lHelper.loadDataIntoDBFromContentProvider(this);

            //TODO: Once db work is over load data into recycler view

            loadDataToRecView();
            //TODO: REMOVE =--= Data loading from content provider change it to DB and use loader manager for updating DB

        } else {

            loadDataToRecView();

            if (lSharedPreferences.getBoolean(getString(R.string.is_song_playing), false)) {
                startActivityForResult(new Intent(SongsListActivity.this, PlayerActivity.class)
                        .putExtra(getString(R.string.position), lSharedPreferences.getInt(getString(R.string.position), 0)), REQUEST_CODE);
                overridePendingTransition(R.anim.from_right, R.anim.scale_down);
            }

        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0) {
            boolean lGranted = true;
            for (int lResult : grantResults) {
                if (lResult == PackageManager.PERMISSION_DENIED)
                    lGranted = false;
            }
            if (lGranted)
                loadData();
        }
    }

    /**
     * Loads data from DB to RecView
     */
    public void loadDataToRecView() {
        mSongDetailsJDOs = new SongDetailTable(this).getAllSongs();
        mAdapter = new MyRecViewAdapter(SongsListActivity.this, mSongDetailsJDOs);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (data.getExtras() != null && resultCode == PlayerActivity.RESULT_CODE) {
                //if data changed reload the recyclerView
                if (data.getBooleanExtra(getString(R.string.is_data_changed), false))
                    loadDataToRecView();

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater lMenuInflater = getMenuInflater();
        lMenuInflater.inflate(R.menu.menu_song_list, menu);

        SearchManager lSearchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        SearchView lSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        lSearchView.setSearchableInfo(lSearchManager.getSearchableInfo(getComponentName()));
        lSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterRecView(newText);
                return true;
            }
        });

        return true;
    }

    private void filterRecView(String pText) {
        if (pText.equals("")) {
            mAdapter.swapData(mSongDetailsJDOs);
            toggleVisibilityForNoResult(mSongDetailsJDOs.size(), pText);
        } else {
            ArrayList<SongDetailsJDO> lSongDetailsJDOs = new ArrayList<>();

            pText = pText.toLowerCase();
            for (SongDetailsJDO lDetailsJDO : mSongDetailsJDOs) {
                if (lDetailsJDO.getTitle().toLowerCase().contains(pText) || lDetailsJDO.getAlbumName().toLowerCase().contains(pText))
                    lSongDetailsJDOs.add(lDetailsJDO);
            }
            toggleVisibilityForNoResult(lSongDetailsJDOs.size(), pText);
            mAdapter.swapData(lSongDetailsJDOs);
        }

    }

    public void toggleVisibilityForNoResult(int pNumberOfSongs, String query) {

        if (pNumberOfSongs == 0) {
            mNoSongTV.setVisibility(View.VISIBLE);
            mNoSongTV.setText(getString(R.string.nosong) + " " + query);
        } else
            mNoSongTV.setVisibility(View.INVISIBLE);
    }


}
