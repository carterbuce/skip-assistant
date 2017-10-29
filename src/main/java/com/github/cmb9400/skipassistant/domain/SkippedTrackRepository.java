package com.github.cmb9400.skipassistant.domain;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface SkippedTrackRepository extends CrudRepository<SkippedTrackEntity, Long> {

    @Transactional
    @Modifying(clearAutomatically=true)
    @Query(value="INSERT INTO SKIPPED_TRACK_ENTITY(NUM_SKIPS, PLAYLIST_HREF, SONG_URI, USER_ID) VALUES (?1, ?2, ?3, ?4) ON DUPLICATE KEY UPDATE NUM_SKIPS = NUM_SKIPS + 1", nativeQuery = true)
    public void insertOrUpdateCount(int numSkips, String playlist, String song, String user);

}
