package com.github.cmb9400.skipassistant.domain;

import java.io.Serializable;

public class SkippedTrackModel implements Serializable{

    protected String userId;
    protected String playlistHref;
    protected String songUri;


    public SkippedTrackModel() {}

    public SkippedTrackModel(String userId, String playlistHref, String songUri) {
        this.userId = userId;
        this.playlistHref = playlistHref;
        this.songUri = songUri;
    }

    @Override
    public int hashCode(){
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((userId == null) ? 0 : userId.hashCode());
        result = prime * result
                + ((playlistHref == null) ? 0 : playlistHref.hashCode());
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
                    other.playlistHref.equals(this.playlistHref) &&
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

}
