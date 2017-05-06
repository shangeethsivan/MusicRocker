package com.shangeeth.musicrocker.jdo;

import android.net.Uri;

import java.io.Serializable;

/**
 * Created by user on 04/05/17.
 */

public class SongDetailsJDO implements Serializable {

    public SongDetailsJDO(String title, String albumName, String albumId, String songId, int duration) {
        this.title = title;
        this.albumName = albumName;
        this.albumId = albumId;
        this.songId = songId;
        this.duration = duration;
    }

    private String title;
    private String albumName;
    private String albumId;
    private String songId;
    private int duration;

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

    @Override
    public String toString() {
        return getTitle() + " " + getAlbumId() + " " + getAlbumName() + " " + getSongId();
    }
}
