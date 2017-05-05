package com.shangeeth.musicrocker.jdo;

import android.net.Uri;

import java.io.Serializable;

/**
 * Created by user on 04/05/17.
 */

public class SongDetailsJDO implements Serializable {

    public SongDetailsJDO(String title, String albumName, String albumId, String songId) {
        this.title = title;
        this.albumName = albumName;
        this.albumId = albumId;
        this.songId = songId;
    }

    private String title;
    private String albumName;
    private String albumId;
    private String songId;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    @Override
    public String toString() {
        return getTitle()+" "+getAlbumId()+" "+getAlbumName()+" "+getSongId();
    }
}
