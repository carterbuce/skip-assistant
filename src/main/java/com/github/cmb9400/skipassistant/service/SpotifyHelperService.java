package com.github.cmb9400.skipassistant.service;

import com.github.cmb9400.skipassistant.domain.SkippedTrackEntity;
import com.github.cmb9400.skipassistant.domain.SkippedTrackRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import com.wrapper.spotify.model_objects.special.SnapshotResult;
import com.wrapper.spotify.model_objects.specification.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class SpotifyHelperService {

    @Resource
    public Environment env;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    SkippedTrackRepository skippedTrackRepository;

    public Map<String, SpotifyPollingService> runningUsers = new HashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(SpotifyHelperService.class);


    /**
     * Get an API builder
     * @return a new API builder with client settings
     */
    public SpotifyApi.Builder getApiBuilder() {
        SpotifyApi.Builder builder = null;


        URI redirectUri = SpotifyHttpManager.makeUri(env.getProperty("spotify.redirect.uri"));
        builder = SpotifyApi.builder()
                .setClientId(env.getProperty("spotify.client.id"))
                .setClientSecret(env.getProperty("spotify.client.secret"))
                .setRedirectUri(redirectUri);

        return builder;
    }


    /**
     * Get a generated URL to authorize this app with spotify and log in
     * @return a URL string
     */
    public String getAuthorizationURL() {
        LOGGER.info("Getting Authorize URL");
        SpotifyApi api = getApiBuilder().build();

        // Set the necessary scopes that the application will need from the user
        String scopes = env.getProperty("spotify.oauth.scope");

        return api.authorizationCodeUri().scope(scopes).build().execute().toString();
    }


    /**
     * For a given user, get all tracks in the database that they've skipped
     */
    public List<SkippedTrackEntity> getTracksForUserId(String user) {
        return skippedTrackRepository.findByUserIdIsOrderByNumSkipsDescPlaylistNameDesc(user);
    }


    /**
     * Remove a track from both the internal database and that track's playlist
     */
    public void removeTrack(SkippedTrackEntity trackToRemove, SpotifyApi api, String userId) {
        // remove the track from its playlist
        JsonArray trackArray = new JsonArray();
        JsonObject trackMap = new JsonObject();

        // build a structure of [{"uri": "<track uri>"}]
        JsonElement trackUri = new JsonPrimitive(trackToRemove.getSongUri());
        trackMap.add("uri", trackUri);
        trackArray.add(trackMap);

        try {
            // remove the track from its playlist
            SnapshotResult result = api.removeTracksFromPlaylist(userId, trackToRemove.getPlaylistId(), trackArray).build().execute();
            // TODO add snapshotId into the request to support concurrent changes to playlists
            // remove the track from the database
            skippedTrackRepository.delete(trackToRemove);
        }
        catch(SpotifyWebApiException | IOException e){
            LOGGER.error(e.getMessage());
            // TODO return fail?
        }
    }


    /**
     * Create a new instance of a polling service
     * @param code the Authorization Code sent from Spotify
     * @return a new polling service object for given key
     */
    public SpotifyPollingService getNewPollingService(String code) {
        return (SpotifyPollingService) applicationContext.getBean("spotifyPollingService", code);
    }


    /**
     * Compares the "checkedSong" to the "nextSong" to see if the "checkedSong" was skipped or not
     * @param prevSong the RecentlyPlayedTrack to determine if it was skipped
     * @param nextSong The song played after checkedSong (more recent)
     * @return if prevSong was skipped or not
     */
    public Boolean wasSkipped(CurrentlyPlaying prevSong, CurrentlyPlaying nextSong) {
        if (prevSong == null || nextSong == null) return false;

        Long skipSensitivitySeconds = Long.parseLong(env.getProperty("polling.skip.sensitivity.seconds"));

        Long secondsBetween = (nextSong.getTimestamp() / 1000) - (prevSong.getTimestamp() / 1000);

        return secondsBetween < skipSensitivitySeconds;
    }


    /**
     * determine if a given song is part of a user's playlist
     * @param song a given played song
     * @param user the user that played the song
     * @return if the song is played from one of the user's playlists
     */
    public Boolean isValidPlaylistTrack(CurrentlyPlaying song, User user) {
        if (song.getContext() == null) {
            return false;
        }
        else {
            return song.getContext().getUri().contains(user.getId());
        }
    }

}
