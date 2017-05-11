package com.shangeeth.musicrocker.jdo;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by user on 04/05/17.
 */

public class SongDetailsJDO implements Serializable {

    public SongDetailsJDO(String title, String albumName, String albumId, String songId, int duration, int favStatus) {
        this.title = title;
        this.albumName = albumName;
        this.albumId = albumId;
        this.songId = songId;
        this.duration = duration;
        this.favouriteStatus = favStatus;
    }

    private String title;
    private String albumName;
    private String albumId;
    private String songId;
    private int duration;
    private int favouriteStatus;

    public int getDuration() {
        return duration;
    }

    public String getTitle() {
        return title;
    }


    public String getAlbumName() {
        return albumName;
    }


    public String getAlbumId() {
        return albumId;
    }


    public String getSongId() {
        return songId;
    }

    public int getFavouriteStatus() {
        return favouriteStatus;
    }

    @Override
    public String toString() {
        return getTitle() + " " + getAlbumId() + " " + getAlbumName() + " " + getSongId() + " " + getFavouriteStatus();
    }

    public void setFavouriteStatus(int favouriteStatus) {
        this.favouriteStatus = favouriteStatus;
    }

}
