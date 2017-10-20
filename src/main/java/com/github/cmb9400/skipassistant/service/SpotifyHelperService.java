package com.github.cmb9400.skipassistant.service;

import com.wrapper.spotify.Api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.Arrays;
import java.util.List;

@Service
public class SpotifyHelperService {

    @Resource
    public Environment env;

    @Autowired
    ApplicationContext applicationContext;


    public SpotifyHelperService() {
    }

    public Api.Builder getApiBuilder() {
        return Api.builder()
                .clientId(env.getProperty("spotify.client.id"))
                .clientSecret(env.getProperty("spotify.client.secret"))
                .redirectURI(env.getProperty("spotify.redirect.uri"));
    }


    public String getAuthorizationURL() {
        // Create the API object
        Api api = getApiBuilder().build();

        // Set the necessary scopes that the application will need from the user
        String scopes = env.getProperty("spotify.oauth.scope");
        List<String> scopeList = Arrays.asList(scopes.split(","));

        // Set a state. This is used to prevent cross site request forgeries.
        String state = env.getProperty("spotify.oauth.state");

        return api.createAuthorizeURL(scopeList, state);
    }


    public SpotifyPollingService getNewPollingService(String key) {
        return (SpotifyPollingService) applicationContext.getBean("spotifyPollingService", key);
    }

}
