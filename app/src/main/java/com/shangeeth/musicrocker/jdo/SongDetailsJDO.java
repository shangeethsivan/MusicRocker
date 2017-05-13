package com.shangeeth.musicrocker.jdo;

import java.io.Serializable;

/**
 * Created by user on 04/05/17.
 */

public class SongDetailsJDO implements Serializable {

    private String Title;
    private String AlbumName;
    private String AlbumId;
    private String SongId;
    private int Duration;
    private int FavouriteStatus;

    public SongDetailsJDO(String title, String albumName, String albumId, String songId, int duration, int favStatus) {
        this.Title = title;
        this.AlbumName = albumName;
        this.AlbumId = albumId;
        this.SongId = songId;
        this.Duration = duration;
        this.FavouriteStatus = favStatus;
    }

    public int getDuration() {
        return Duration;
    }

    public String getTitle() {
        return Title;
    }


    public String getAlbumName() {
        return AlbumName;
    }


    public String getAlbumId() {
        return AlbumId;
    }


    public String getSongId() {
        return SongId;
    }

    public int getFavouriteStatus() {
        return FavouriteStatus;
    }

    @Override
    public String toString() {
        return getTitle() + " " + getAlbumId() + " " + getAlbumName() + " " + getSongId() + " " + getFavouriteStatus();
    }

    public void setFavouriteStatus(int favouriteStatus) {
        this.FavouriteStatus = favouriteStatus;
    }

}
