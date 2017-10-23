package com.github.cmb9400.skipassistant.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class SkippedTrackEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id; // this can get removed

    String songId;
    String playlistId;

    // userId
    // playlistId
    // songURI
    // numberSkips
    // most recently played date? this may help prevent duplicates between runs of server, but won't help with long uptimes
    // composite primary key: (userid, playlistid, songid)

    protected SkippedTrackEntity() {}


    public SkippedTrackEntity(String songId, String playlistId) {
        this.songId = songId;
        this.playlistId = playlistId;
    }


    @Override
    public String toString() {
        return String.format(
                "SkippedTrack[id=%d, songId='%s', playlistId='%s']",
                id, songId, playlistId);
    }
}
