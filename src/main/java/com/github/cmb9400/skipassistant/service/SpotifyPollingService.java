package com.github.cmb9400.skipassistant.service;

import com.github.cmb9400.skipassistant.controller.PageControllerImpl;
import com.github.cmb9400.skipassistant.domain.SkippedTrackEntity;
import com.github.cmb9400.skipassistant.domain.SkippedTrackRepository;
import com.wrapper.spotify.Api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Scope("prototype")
public class SpotifyPollingService {

    @Resource
    public Environment env;

    @Autowired
    private SkippedTrackRepository skippedTrackRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(PageControllerImpl.class);
    private String key;
    private Api api;


    @SuppressWarnings("SpringJavaAutowiringInspection")
    public SpotifyPollingService(String key) {
        this.key = key;
    }


    public String getAuthorizationURL() {
        // Create the API object
        this.api = Api.builder()
                .clientId(env.getProperty("spotify.client.id"))
                .clientSecret(env.getProperty("spotify.client.secret"))
                .redirectURI(env.getProperty("spotify.redirect.uri"))
                .build();

        // Set the necessary scopes that the application will need from the user
        String scopes = env.getProperty("spotify.oauth.scope");
        List<String> scopeList = Arrays.asList(scopes.split(","));

        // Set a state. This is used to prevent cross site request forgeries.
        String state = env.getProperty("spotify.oauth.state");

        return this.api.createAuthorizeURL(scopeList, state);
    }


    @Async
    public void findSkippedSongs() {
        for (int i = 0; i < 3; i++) {
            try {
                LOGGER.info("Found skipped song: " + key);
                skippedTrackRepository.save(new SkippedTrackEntity(key, "bar"));
                TimeUnit.SECONDS.sleep(10);
            }
            catch (InterruptedException e){
                LOGGER.error("Polling service interrupted!");
            }
        }
    }

}
