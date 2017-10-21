package com.github.cmb9400.skipassistant.service;

import com.github.cmb9400.skipassistant.domain.SkippedTrackRepository;
import com.wrapper.spotify.Api;
import com.wrapper.spotify.exceptions.WebApiException;
import com.wrapper.spotify.models.AuthorizationCodeCredentials;
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
import java.util.concurrent.TimeUnit;

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
     * @param code TODO placeholder until method implemented
     */
    @SuppressWarnings("SpringJavaAutowiringInspection")
    public SpotifyPollingService(String code) {
        this.code = code;
    }


    public void run() throws RuntimeException {
        try {
            init();
            login();
            findSkippedSongs();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void init() {
        api = spotifyHelperService.getApiBuilder().build();
    }


    private void login() throws IOException, WebApiException {
        try {
            LOGGER.info("Getting Tokens from Authorization Code...");
            AuthorizationCodeCredentials authorizationCodeCredentials = api.authorizationCodeGrant(code).build().get();
            api.setAccessToken(authorizationCodeCredentials.getAccessToken());
            api.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
            user = api.getMe().build().get();
        }
        catch (WebApiException | IOException e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
    }


    /**
     * Start searching for skipped songs
     * TODO placeholder until method implemented
     */
    @Async
    protected void findSkippedSongs() {
        LOGGER.info("Searching for skipped songs...");

        while (true) {
            try {
                String songName = "foo";
                LOGGER.info("Currently playing song for " + user.getDisplayName() + ": " + songName);
//                skippedTrackRepository.save(new SkippedTrackEntity(code, "bar"));
                TimeUnit.SECONDS.sleep(10);
            }
            catch (Exception e){
                LOGGER.error("Polling service failed!");
                LOGGER.error(e.getMessage());
            }
        }
    }

}
