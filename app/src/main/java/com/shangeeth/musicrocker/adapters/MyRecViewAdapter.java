package com.shangeeth.musicrocker.adapters;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
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
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

/**
 * Created by user on 03/05/17.
 */

public class MyRecViewAdapter extends RecyclerView.Adapter<MyRecViewAdapter.MyViewHolder> {

    Cursor mCursor;
    LayoutInflater mLayoutInflater;
    Context mContext;
    private static final String TAG = "MyRecViewAdapter";

    public MyRecViewAdapter(Context context, Cursor pCursor) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mCursor = pCursor;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.e(TAG, "onCreateViewHolder: "+viewType);

        View lView = mLayoutInflater.inflate(R.layout.recycler_view_item, parent, false);

        return new MyViewHolder(lView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        Uri lUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mCursor.getLong(mCursor.getColumnIndex(MediaStore.Audio.Media._ID)));
        Picasso.with(mContext).load(lUri).placeholder(R.drawable.placeholder).into(holder.albumImageIV);

        String lTrackName = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
        if (lTrackName != null)
            holder.trackNameTV.setText(lTrackName.trim());

        String lAlbumName = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
        if (lAlbumName != null)
            holder.albumAndArtistDetailsTV.setText(lAlbumName.trim());
        Log.e(TAG, "onBindViewHolder: " + position + lTrackName+":"+lAlbumName);
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
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
}
