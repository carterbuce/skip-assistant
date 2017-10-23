package com.github.cmb9400.skipassistant.service;

import com.wrapper.spotify.Api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Service
public class SpotifyHelperService {

    @Resource
    public Environment env;

    @Autowired
    ApplicationContext applicationContext;

    public List<String> runningUserIds = new ArrayList<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(SpotifyHelperService.class);


    /**
     * Get an API builder
     * @return a new API builder with client settings
     */
    public Api.Builder getApiBuilder() {
        return Api.builder()
                .clientId(env.getProperty("spotify.client.id"))
                .clientSecret(env.getProperty("spotify.client.secret"))
                .redirectURI(env.getProperty("spotify.redirect.uri"));
    }


    /**
     * Get a generated URL to authorize this app with spotify and log in
     * @return a URL string
     */
    public String getAuthorizationURL() {
        LOGGER.info("Getting Authorize URL");
        Api api = getApiBuilder().build();

        // Set the necessary scopes that the application will need from the user
        String scopes = env.getProperty("spotify.oauth.scope");
        List<String> scopeList = Arrays.asList(scopes.split(","));

        // Set a state. This is used to prevent cross site request forgeries.
        String state = env.getProperty("spotify.oauth.state");

        return api.createAuthorizeURL(scopeList, state);
    }


    /**
     * Create a new instance of a polling service
     * @param code the Authorization Code sent from Spotify
     * @return a new polling service object for given key
     */
    public SpotifyPollingService getNewPollingService(String code) {
        return (SpotifyPollingService) applicationContext.getBean("spotifyPollingService", code);
    }

}
