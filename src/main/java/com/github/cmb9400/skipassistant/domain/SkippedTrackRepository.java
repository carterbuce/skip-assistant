package com.github.cmb9400.skipassistant.domain;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

import java.util.List;

@Repository
public interface SkippedTrackRepository extends CrudRepository<SkippedTrackEntity, Long> {

    /**
     * Insert a track entity, if it already exists then increment its num_skips
     *
     * This method is kind of a hack, see the `@SQLInsert` on SkippedTrackEntity
     *  The issue with that is the entity is persisted in spring and so after the first
     *  insert, it will run a merge instead of an insert, so this is easier than implementing
     *  a new repository
     */
    @Transactional
    @Modifying(clearAutomatically=true)
    @Query(value="INSERT INTO SKIPPED_TRACK_ENTITY(NUM_SKIPS, PLAYLIST_ID, SONG_URI, USER_ID, SONG_NAME, PLAYLIST_NAME)" +
            " VALUES (?1, ?2, ?3, ?4, ?5, ?6) ON DUPLICATE KEY UPDATE NUM_SKIPS = NUM_SKIPS + 1", nativeQuery = true)
    public void insertOrUpdateCount(int numSkips, String playlistId, String songUri, String user,
                                    String songName, String playlistName);

    public List<SkippedTrackEntity> findByUserIdIsOrderByNumSkipsDescPlaylistNameDesc(String userId);
}
