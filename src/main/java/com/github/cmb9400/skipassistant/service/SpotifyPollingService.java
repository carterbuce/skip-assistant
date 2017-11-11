package com.github.cmb9400.skipassistant.service;

import com.github.cmb9400.skipassistant.domain.SkippedTrackConverter;
import com.github.cmb9400.skipassistant.domain.SkippedTrackRepository;
import com.github.cmb9400.skipassistant.exceptions.AlreadyRunningForUserException;
import com.wrapper.spotify.Api;
import com.wrapper.spotify.exceptions.WebApiException;
import com.wrapper.spotify.models.AuthorizationCodeCredentials;
import com.wrapper.spotify.models.CurrentlyPlayingTrack;
import com.wrapper.spotify.models.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.io.IOException;
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

    @Autowired
    SkippedTrackConverter skippedTrackConverter;


    private static final Logger LOGGER = LoggerFactory.getLogger(SpotifyPollingService.class);
    private String code;
    private Api api;
    private User user;


    public Api getApi() {
        return api;
    }

    public User getUser() {
        return user;
    }


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
            pollSongs();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void init() throws AlreadyRunningForUserException {
        try {
            api = spotifyHelperService.getApiBuilder().build();
            login();
        }
        catch (AlreadyRunningForUserException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Log in to the account using the authorization code and get the access token
     */
    private void login() throws IOException, WebApiException, AlreadyRunningForUserException, RuntimeException {
        try {
            LOGGER.info("Getting Tokens from Authorization Code...");
            AuthorizationCodeCredentials authorizationCodeCredentials = api.authorizationCodeGrant(code).build().get();
            api.setAccessToken(authorizationCodeCredentials.getAccessToken());
            api.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
            user = api.getMe().build().get();
            String userId = user.getId();

            // don't continue if already polling that user
            if (spotifyHelperService.runningUsers.containsKey(userId)) {
                LOGGER.error("Already polling user " + userId + "!");
                throw new AlreadyRunningForUserException(userId);
            }
            else {
                spotifyHelperService.runningUsers.put(userId, this);
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
        CurrentlyPlayingTrack mostRecent = null;

        while (true) {
            try {
                TimeUnit.MILLISECONDS.sleep(Long.parseLong(env.getProperty("polling.frequency.milliseconds")));

                // get current song
                CurrentlyPlayingTrack currentSong = api.getCurrentlyPlayingTrack().build().get();


                if (currentSong.equals(mostRecent)) {
                    // do nothing, the song hasn't changed
                }
                else {
                    LOGGER.info("Current track for " + user.getId() + ": " + currentSong.getItem().getName());

                    // check to see if the song was skipped
                    checkSkipped(mostRecent, currentSong);

                    // store most recently played song for further queries
                    mostRecent = currentSong;
                }
            }
            catch (Exception e){
                // refresh access token on 401 error
                if (e.getMessage().equals("401")) {
                    LOGGER.info("Refreshing access token...");
                    try {
                        api.setAccessToken(api.refreshAccessToken().build().get().getAccessToken());
                    }
                    catch (Exception e2) {
                        LOGGER.error(e2.getMessage());
                        e2.printStackTrace();
                    }
                }
                else {
                    LOGGER.error("Polling service failed!");
                    LOGGER.error(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * determine if a song was skipped and if so, save it to the repository
     * @param prevSong
     * @param nextSong
     */
    private void checkSkipped(CurrentlyPlayingTrack prevSong, CurrentlyPlayingTrack nextSong)
            throws WebApiException, IOException {

        // find which songs were skipped and save them to the repository
        if (spotifyHelperService.wasSkipped(prevSong, nextSong) &&
                spotifyHelperService.isValidPlaylistTrack(prevSong, user)) {

            String userId = user.getId();
            String songUri = prevSong.getItem().getUri();
            String songName = prevSong.getItem().getName();
            List<String> artistList = prevSong.getItem().getArtists().stream().map(x -> x.getName()).collect(Collectors.toList());
            String songArtistNames = songName + " - " + StringUtils.join(artistList, ", ");

            String playlistHref = prevSong.getContext().getHref();
            String playlistId = playlistHref.substring(playlistHref.lastIndexOf("/") + 1, playlistHref.length());
            String playlistName = api.getPlaylist(userId, playlistId).build().get().getName();

            LOGGER.info("Skipped song detected! \n    "
                    + userId + " skipped " + songName
                    + "\n    in playlist " + playlistName);

            skippedTrackRepository.insertOrUpdateCount(1, playlistId, songUri, userId, songArtistNames, playlistName);
        }
    }

}
