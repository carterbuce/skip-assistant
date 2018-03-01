package com.github.cmb9400.skipassistant.service;

import com.github.cmb9400.skipassistant.domain.SkippedTrackConverter;
import com.github.cmb9400.skipassistant.domain.SkippedTrackRepository;
import com.github.cmb9400.skipassistant.exceptions.AlreadyRunningForUserException;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import com.wrapper.spotify.model_objects.specification.User;

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
import java.util.Arrays;
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
    private SpotifyApi api;
    private User user;


    public SpotifyApi getApi() {
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
    private void login() throws IOException, SpotifyWebApiException, AlreadyRunningForUserException, RuntimeException {
        try {
            LOGGER.info("Getting Tokens from Authorization Code...");
            AuthorizationCodeCredentials authorizationCodeCredentials = api.authorizationCode(code).build().execute();
            api.setAccessToken(authorizationCodeCredentials.getAccessToken());
            api.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
            user = api.getCurrentUsersProfile().build().execute();
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
        catch (SpotifyWebApiException | IOException e) {
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
        CurrentlyPlaying mostRecent = null;

        while (true) {
            try {
                TimeUnit.MILLISECONDS.sleep(Long.parseLong(env.getProperty("polling.frequency.milliseconds")));

                // get current song
                CurrentlyPlaying currentSong = api.getUsersCurrentlyPlayingTrack().build().execute();


                if (currentSong == null || currentSong.equals(mostRecent)) {
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
//            catch (EmptyResponseException e) {
//                LOGGER.debug("No tracks playing for " + user.getId());
//            }
            catch (Exception e){
                // refresh access token on 401 error
                if (e.getMessage() != null && e.getMessage().equals("401")) {
                    LOGGER.info("Refreshing access token...");
                    try {
                        api.authorizationCodeRefresh().build().execute();
                    }
                    catch (Exception e2) {
                        LOGGER.error("Failed to refresh access token!");
                        LOGGER.error(e2.getMessage());
                        // e2.printStackTrace();
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
    private void checkSkipped(CurrentlyPlaying prevSong, CurrentlyPlaying nextSong)
            throws SpotifyWebApiException, IOException {

        // find which songs were skipped and save them to the repository
        if (spotifyHelperService.wasSkipped(prevSong, nextSong) &&
                spotifyHelperService.isValidPlaylistTrack(prevSong, user)) {

            String userId = user.getId();
            String songUri = prevSong.getItem().getUri();
            String songId = prevSong.getItem().getId();
            String songName = prevSong.getItem().getName();
            List<String> artistList = Arrays.stream(prevSong.getItem().getArtists()).map(x -> x.getName()).collect(Collectors.toList());
            String songArtistNames = songName + " - " + StringUtils.join(artistList, ", ");

            String playlistHref = prevSong.getContext().getHref();
            String playlistId = playlistHref.substring(playlistHref.lastIndexOf("/") + 1, playlistHref.length());
            String playlistName = api.getPlaylist(userId, playlistId).build().execute().getName();

            String previewUrl = api.getTrack(songId).build().execute().getPreviewUrl();

            if (previewUrl == null || previewUrl.equals("null")) {
                previewUrl = null;
            }

            LOGGER.info("Skipped song detected! \n    "
                    + userId + " skipped " + songName
                    + "\n    in playlist " + playlistName);

            skippedTrackRepository.insertOrUpdateCount(1, playlistId, songUri, userId, songArtistNames, playlistName, previewUrl);
        }
    }

}
