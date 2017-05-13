package com.shangeeth.musicrocker.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.shangeeth.musicrocker.R;
import com.shangeeth.musicrocker.adapters.SongListAdapter;
import com.shangeeth.musicrocker.db.SongDetailTable;
import com.shangeeth.musicrocker.helper.CommonHelper;
import com.shangeeth.musicrocker.jdo.SongDetailsJDO;
import com.shangeeth.musicrocker.listeners.MyRecyclerViewOnClickListener;
import com.shangeeth.musicrocker.services.SongPlayerService;

import java.util.ArrayList;

public class SongsListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {

    private RecyclerView mRecyclerView;
    private SongListAdapter mAdapter;
    private ArrayList<SongDetailsJDO> mSongDetailsJDOs;
    private TextView mNoSongTV;

    private static final int LOADER_ID = 101;
    private int REQUEST_CODE = 102;

    int position = 0;

    private static final String TAG = "SongsListActivity";
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mPrefEditor;
    private SharedPreferences.OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.rec_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(SongsListActivity.this));
        mNoSongTV = (TextView) findViewById(R.id.no_song_tv);
        mSongDetailsJDOs = new ArrayList<>();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefEditor = mSharedPreferences.edit();
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

        loadData();

    }


    /**
     * Load data into DB if activity opened for first time else load data into recyclerView from DB
     */
    private void loadData() {

        boolean lIsAppLoadingFirstTime = mSharedPreferences.getBoolean(getString(R.string.is_app_loading_first_time), true);

        if (lIsAppLoadingFirstTime) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            } else {
                mPrefEditor.putBoolean(getString(R.string.is_app_loading_first_time), false);
                mPrefEditor.apply();
                new LoadDataToDbBackground().execute();

                // TODO: Create Loader here
            }
        } else {
            loadDataToRecyclerView();

            if (mSharedPreferences.getBoolean(getString(R.string.is_song_playing), false)) {
                // TODO: Create Loader here
                SongDetailsJDO lJDO = getSongJDO(mSharedPreferences.getString(getString(R.string.song_id), ""));
                startActivityForResult(new Intent(SongsListActivity.this, PlayerActivity.class)
                        .putExtra(getString(R.string.song_jdo), lJDO), REQUEST_CODE);
            }

        }

    }

    /**
     * Loads the data from Content provider to DB in background using the @{@link AsyncTask} class
     */
    private class LoadDataToDbBackground extends AsyncTask<Void, Integer, Void> {

        ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(SongsListActivity.this);
            mProgressDialog.setMessage("Please Wait");
            mProgressDialog.setTitle("Loading");
            mProgressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mProgressDialog.dismiss();
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... params) {

            CommonHelper lHelper = new CommonHelper();
            lHelper.loadSongToDB(SongsListActivity.this);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadDataToRecyclerView();
                }
            });

            return null;
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (data != null && data.getExtras() != null && resultCode == PlayerActivity.RESULT_CODE) {
                //if data changed reload the recyclerView
                if (data.getBooleanExtra(getString(R.string.is_data_changed), false)) {
                    mSongDetailsJDOs = new SongDetailTable(this).getAllSongs();
                    mAdapter.swapData(mSongDetailsJDOs);
                }
            }
        }
//        updateCurrentSongIndication();
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


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.TITLE + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        ArrayList<SongDetailsJDO> lSongDetailsNew = new ArrayList<>();

        if (data.moveToFirst()) {
            do {
                lSongDetailsNew.add(new SongDetailsJDO(data.getString(data.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                        data.getString(data.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                        data.getString(data.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)),
                        data.getString(data.getColumnIndex(MediaStore.Audio.Media._ID)),
                        data.getInt(data.getColumnIndex(MediaStore.Audio.Media.DURATION)), 0));
            } while (data.moveToNext());
        }
        compareDataAndMakeChangesToDB(lSongDetailsNew);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Log.d(TAG, "onWindowFocusChanged: ");
        updateCurrentSongIndication();
    }

    /**
     * Updates the song drawable anim in recycler view
     */
    private void updateCurrentSongIndication() {
        if (mSharedPreferences.getBoolean(getString(R.string.is_song_playing), false)) {
            mAdapter.updateSongPlayStatus(true, mSharedPreferences.getString(getString(R.string.song_id), ""));
            mRecyclerView.smoothScrollToPosition(getPositionOfSongId(mSharedPreferences.getString(getString(R.string.song_id), "")));
        } else {
            mAdapter.updateSongPlayStatus(false, "-1");
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateCurrentSongIndication();
    }


    /**
     * Called from the loader
     *
     * @param pSongDetailsNew
     */
    private void compareDataAndMakeChangesToDB(ArrayList<SongDetailsJDO> pSongDetailsNew) {

        Log.d(TAG, "compareDataAndMakeChangesToDB: Called ============");

        ArrayList<String> lSongIdsToBeDeleted = new ArrayList<>();
        for (SongDetailsJDO lSongDetailsJDO : mSongDetailsJDOs) {
            lSongIdsToBeDeleted.add(lSongDetailsJDO.getSongId());
        }
        ArrayList<SongDetailsJDO> lNewSongsToBeAdded = new ArrayList<>();

        for (SongDetailsJDO lSongDetailsJDO : pSongDetailsNew) {
            if (lSongIdsToBeDeleted.contains(lSongDetailsJDO.getSongId())) {
                lSongIdsToBeDeleted.remove(lSongDetailsJDO.getSongId());
            } else
                lNewSongsToBeAdded.add(lSongDetailsJDO);
        }


        if (lSongIdsToBeDeleted.size() > 0 || lNewSongsToBeAdded.size() > 0) {

            SongDetailTable lSongDetailTable = new SongDetailTable(this);
            lSongDetailTable.removeSongsForIds(lSongIdsToBeDeleted);
            lSongDetailTable.insertSongs(lNewSongsToBeAdded);
            loadDataToRecyclerView();
        /*
        Notify the change in DB and update the list in it.
         */
//
//            SongPlayerService lSongPlayerService = SongPlayerService.getRunningInstance();
//            if (lSongPlayerService != null)
//                lSongPlayerService.dataChanged();
        }

    }

    /**
     * On Fav clicked called in xml of recylerview item
     *
     * @param pView
     */
    public void onFavClick(View pView) {
        int lPosition = mRecyclerView.getChildLayoutPosition((View) pView.getParent());

        SongDetailsJDO lSongDetailsJDO = mAdapter.getItemAtPosition(lPosition);
        String lSongId = lSongDetailsJDO.getSongId();
        SongDetailTable lSongDetailTable = new SongDetailTable(this);

        int lNewFavStatus = lSongDetailsJDO.getFavouriteStatus() == 0 ? 1 : 0;
        lSongDetailTable.setFavouriteStatus(lSongId, lNewFavStatus);
        mAdapter.favChanged(lPosition, lNewFavStatus);

        SongPlayerService mSongPlayerService = SongPlayerService.getRunningInstance();
        if (mSongPlayerService != null)
            mSongPlayerService.favChanged(lPosition, lNewFavStatus);
    }

    /**
     * Called when the row is clicked.
     *
     * @param pView
     */
    public void onRowClick(View pView) {

        int lPosition = mRecyclerView.getChildLayoutPosition(pView);
        SongDetailsJDO lJDO = mAdapter.getItemAtPosition(lPosition);

        startActivityForResult(new Intent(SongsListActivity.this, PlayerActivity.class)
                .putExtra(getString(R.string.song_jdo), lJDO), REQUEST_CODE);

        overridePendingTransition(R.anim.from_right, R.anim.scale_down);
    }

    /**
     * Gets the SongDetails JDO for the specified ID
     *
     * @param pSongId
     * @return
     */
    private SongDetailsJDO getSongJDO(String pSongId) {
        SongDetailsJDO lJDO = null;
        for (SongDetailsJDO lSongDetailsJDO : mSongDetailsJDOs) {
            if (lSongDetailsJDO.getSongId().equals(pSongId)) {
                lJDO = lSongDetailsJDO;
                break;
            }
        }
        return lJDO;
    }


    /**
     * Filters the Recycler View while doing a search
     *
     * @param pText the text to be searched
     */
    private void filterRecView(String pText) {
        if (pText != null) {
            if (pText.equals("")) {
                mAdapter.swapData(mSongDetailsJDOs);
                toggleVisibilityForNoResult(mSongDetailsJDOs.size(), pText);
            } else {
                ArrayList<SongDetailsJDO> lSongDetailsJDOs = new ArrayList<>();

                pText = pText.toLowerCase();
                for (SongDetailsJDO lDetailsJDO : mSongDetailsJDOs) {
                    if (lDetailsJDO.getTitle().toLowerCase().contains(pText) || lDetailsJDO.getAlbumName() != null && lDetailsJDO.getAlbumName().toLowerCase().contains(pText))
                        lSongDetailsJDOs.add(lDetailsJDO);
                }
                toggleVisibilityForNoResult(lSongDetailsJDOs.size(), pText);
                mAdapter.swapData(lSongDetailsJDOs);
            }
        }

    }

    /**
     * Toggle visibility of the no result found text
     *
     * @param pNumberOfSongs
     * @param query
     */
    public void toggleVisibilityForNoResult(int pNumberOfSongs, String query) {

        if (pNumberOfSongs == 0) {
            mNoSongTV.setVisibility(View.VISIBLE);
            mNoSongTV.setText(getString(R.string.nosong) + " " + query);
        } else
            mNoSongTV.setVisibility(View.INVISIBLE);
    }

    /**
     * Loads data from DB to RecView
     */
    public void loadDataToRecyclerView() {
        //Loading data to RecyclerView
        mSongDetailsJDOs = new SongDetailTable(this).getAllSongs();
        mAdapter = new SongListAdapter(SongsListActivity.this, mSongDetailsJDOs);
        mRecyclerView.setAdapter(mAdapter);
    }


    public int getPositionOfSongId(String pSongId) {
        int lPostion = -1;
        for (int i = 0; i < mSongDetailsJDOs.size(); i++) {
            if (mSongDetailsJDOs.get(i).getSongId().equals(pSongId)) {
                lPostion = i;
                break;
            }
        }
        return lPostion;
    }
}
