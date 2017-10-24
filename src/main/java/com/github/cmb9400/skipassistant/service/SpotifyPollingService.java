package com.github.cmb9400.skipassistant.service;

import com.github.cmb9400.skipassistant.domain.SkippedTrackRepository;
import com.wrapper.spotify.Api;
import com.wrapper.spotify.exceptions.WebApiException;
import com.wrapper.spotify.models.AuthorizationCodeCredentials;
import com.wrapper.spotify.models.Page;
import com.wrapper.spotify.models.RecentlyPlayedTrack;
import com.wrapper.spotify.models.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Scope("prototype")
public class SpotifyPollingService {

    @Resource
    public Environment env;

    @Autowired
    private SkippedTrackRepository skippedTrackRepository;

    @Autowired
    SpotifyHelperService spotifyHelperService;


    private static final Logger LOGGER = LoggerFactory.getLogger(SpotifyPollingService.class);
    private String code;
    private Api api;
    private User user;


    /**
     * Constructor for a polling service object. Key is not autowired, it is passed in during creation
     * @param code the user's authorization code
     */
    @SuppressWarnings("SpringJavaAutowiringInspection")
    public SpotifyPollingService(String code) {
        this.code = code;
    }


    /**
     * Start the polling service
     */
    @Async
    public void run() throws RuntimeException {
        try {
            init();
            login();
            pollSongs();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void init() {
        api = spotifyHelperService.getApiBuilder().build();
    }


    /**
     * Log in to the account using the authorization code and get the access token
     * TODO deal with token expiration and refreshing
     */
    private void login() throws IOException, WebApiException, RuntimeException {
        try {
            LOGGER.info("Getting Tokens from Authorization Code...");
            AuthorizationCodeCredentials authorizationCodeCredentials = api.authorizationCodeGrant(code).build().get();
            api.setAccessToken(authorizationCodeCredentials.getAccessToken());
            api.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
            user = api.getMe().build().get();

            // don't continue if already polling that user
            if (spotifyHelperService.runningUserIds.contains(user.getId())) {
                LOGGER.error("Already polling user " + user.getId() + "!");
                throw new RuntimeException("Already polling user " + user.getId() + "!");
            }
            else {
                spotifyHelperService.runningUserIds.add(user.getId());
            }
        }
        catch (WebApiException | IOException e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
    }


    /**
     * Start searching for skipped songs.
     * This method will continuously poll the spotify service for recently played tracks
     * Once it polls the service, it will store the most recently played song
     * @see <a href=https://developer.spotify.com/web-api/web-api-personalization-endpoints/get-recently-played/>Spotify Docs</a>
     * TODO deal with 429 too many requests response code
     */
    private void pollSongs() {
        LOGGER.info("Searching for skipped songs...");
        RecentlyPlayedTrack mostRecent = null;

        while (true) {
            try {
                TimeUnit.SECONDS.sleep(Long.parseLong(env.getProperty("polling.frequency.seconds")));

                // get recently played songs
                Page<RecentlyPlayedTrack> songs;
                if (mostRecent == null) {
                    // get the 20 most recently played tracks
                    songs = api.getRecentlyPlayedTracks().build().get();
                }
                else {
                    // get at most 20 recently played tracks after (and including) the last one seen
                    String after = Long.toString(mostRecent.getPlayedAt().getTime());
                    songs = api.getRecentlyPlayedTracks().build(after).get();
                }

                // store most recently played song for further queries
                mostRecent = songs.getItems().get(0);
                LOGGER.info("Most recent track for " + user.getId() + ": " + mostRecent.getTrack().getName());

                // find which songs were skipped and save them to the repository
                findSkippedSongs(songs);
            }
            catch (Exception e){
                LOGGER.error("Polling service failed!");
                LOGGER.error(e.getMessage());
                e.printStackTrace();
            }
        }
    }


    /**
     * Determine which RecentlyPlayedTracks from a list were skipped and save them to the repository
     * @param songs a list of RecentlyPlayedTracks
     */
    private void findSkippedSongs(Page<RecentlyPlayedTrack> songs) {
        List<RecentlyPlayedTrack> songsList = songs.getItems();
        List<RecentlyPlayedTrack> skippedSongs = new ArrayList<>();

        // position 0 is the most recently played song
        if (songsList.size() < 2) return;

        // find all the skipped songs, iterating from most recent to oldest play time
        for (int i = 0; i < songsList.size() - 1; i++) {
            RecentlyPlayedTrack newerSong = songsList.get(i);
            RecentlyPlayedTrack olderSong = songsList.get(i+1);

            if (spotifyHelperService.wasSkipped(olderSong, newerSong) &&
                    spotifyHelperService.isValidPlaylistTrack(olderSong, user)) {
                skippedSongs.add(olderSong);
            }
        }

        // save the skipped songs
        if(skippedSongs.size() > 0) {
            LOGGER.info("Skipped songs detected: ");
            LOGGER.info(skippedSongs.stream().map(e -> e.getTrack().getName()).collect(Collectors.joining(", ")));
            // TODO skippedTrackRepository.save(new SkippedTrackEntity(code, "bar"));
        }


    }

}
