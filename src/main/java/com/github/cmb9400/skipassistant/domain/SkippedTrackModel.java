package com.github.cmb9400.skipassistant.domain;

import java.io.Serializable;

public class SkippedTrackModel implements Serializable{

    protected String userId;
    protected String playlistId;
    protected String songUri;


    public SkippedTrackModel() {}

    public SkippedTrackModel(String userId, String playlistId, String songUri) {
        this.userId = userId;
        this.playlistId = playlistId;
        this.songUri = songUri;
    }

    @Override
    public int hashCode(){
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((userId == null) ? 0 : userId.hashCode());
        result = prime * result
                + ((playlistId == null) ? 0 : playlistId.hashCode());
        result = prime * result
                + ((songUri == null) ? 0 : songUri.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj){
        if (this == obj) return true;
        if (obj == null) return false;

        if (obj instanceof SkippedTrackModel) {
            SkippedTrackModel other = (SkippedTrackModel) obj;

            return other.userId.equals(this.userId) &&
                    other.playlistId.equals(this.playlistId) &&
                    other.songUri.equals(this.songUri);
        } else {
            return false;
        }
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

}
