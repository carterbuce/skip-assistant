package com.github.cmb9400.skipassistant.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Entity
@IdClass(SkippedTrackModel.class)
public class SkippedTrackEntity {

    @Id String userId;
    @Id String playlistId;
    @Id String songUri;

    Integer numSkips;
    String songName;
    String playlistName;


    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
    }

    public String getSongUri() {
        return songUri;
    }

    public void setSongUri(String songUri) {
        this.songUri = songUri;
    }

    public Integer getNumSkips() {
        return numSkips;
    }

    public void setNumSkips(Integer numSkips) {
        this.numSkips = numSkips;
    }


}
