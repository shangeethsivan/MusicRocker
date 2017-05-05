package com.shangeeth.musicrocker.adapters;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.shangeeth.musicrocker.R;
import com.shangeeth.musicrocker.jdo.SongDetailsJDO;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 03/05/17.
 */

public class MyRecViewAdapter extends RecyclerView.Adapter<MyRecViewAdapter.MyViewHolder> {

    ArrayList<SongDetailsJDO> mSongDetailsJDOs;
    LayoutInflater mLayoutInflater;
    Context mContext;
    private static final String TAG = "MyRecViewAdapter";

    public MyRecViewAdapter(Context context, ArrayList<SongDetailsJDO> pSongDetailsJDOs) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSongDetailsJDOs = pSongDetailsJDOs;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.e(TAG, "onCreateViewHolder: " + viewType);

        View lView = mLayoutInflater.inflate(R.layout.recycler_view_item, parent, false);

        return new MyViewHolder(lView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        Uri lUri = null;
        if (mSongDetailsJDOs.get(position).getAlbumId() != null)
            lUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), Long.parseLong(mSongDetailsJDOs.get(position).getAlbumId()));

        Picasso.with(mContext).load(lUri).fit().placeholder(R.drawable.placeholder).into(holder.albumImageIV);

        String lTrackName = mSongDetailsJDOs.get(position).getTitle();
        if (lTrackName != null)
            holder.trackNameTV.setText(lTrackName.trim());
        else
            holder.trackNameTV.setText("<Unknown>");

        String lAlbumName = mSongDetailsJDOs.get(position).getAlbumName();
        if (lAlbumName != null)
            holder.albumAndArtistDetailsTV.setText(lAlbumName.trim());
        else
            holder.albumAndArtistDetailsTV.setText("<Unknown>");

        Log.e(TAG, "onBindViewHolder: " + position + lTrackName + ":" + lAlbumName);
    }

    @Override
    public int getItemCount() {
        if (mSongDetailsJDOs != null)
            return mSongDetailsJDOs.size();
        else
            return 0;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView albumImageIV;
        TextView trackNameTV;
        TextView albumAndArtistDetailsTV;

        public MyViewHolder(View itemView) {
            super(itemView);
            albumImageIV = (ImageView) itemView.findViewById(R.id.album_artwork_iv);
            trackNameTV = (TextView) itemView.findViewById(R.id.title_name_tv);
            albumAndArtistDetailsTV = (TextView) itemView.findViewById(R.id.artist_author_name_tv);
        }
    }

    public void swapData(ArrayList<SongDetailsJDO> pSongDetailsJDOs) {
        mSongDetailsJDOs = pSongDetailsJDOs;
        notifyDataSetChanged();
    }

    public List<SongDetailsJDO> getData() {
        return mSongDetailsJDOs;
    }
}
