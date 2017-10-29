package com.github.cmb9400.skipassistant.domain;

import org.hibernate.annotations.SQLInsert;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Entity
@IdClass(SkippedTrackModel.class)
@SQLInsert(sql="INSERT INTO SKIPPED_TRACK_ENTITY(NUM_SKIPS, PLAYLIST_HREF, SONG_URI, USER_ID) VALUES (?, ?, ?, ?) " +
        "ON DUPLICATE KEY UPDATE NUM_SKIPS = NUM_SKIPS + 1")
public class SkippedTrackEntity {

    @Id String userId;
    @Id String playlistHref;
    @Id String songUri;
    Integer numSkips;


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPlaylistHref() {
        return playlistHref;
    }

    public void setPlaylistHref(String playlistHref) {
        this.playlistHref = playlistHref;
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
