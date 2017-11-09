package com.github.cmb9400.skipassistant.controller;

import com.github.cmb9400.skipassistant.domain.SkippedTrackEntity;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpSession;

public interface PageController {

    /**
     * The index of the service.
     * Provides a login link if not logged in already,
     * otherwise shows a list of skipped songs and relevant actions
     */
    @RequestMapping("/")
    public String index(Model model, HttpSession session);

    /**
     * callback endpoint for the Spotify OAuth to hit
     */
    @RequestMapping("/callback")
    public String callback(String code, Model model, HttpSession session);

    /**
     * Remove a given song from its playlist as well as from the internal db of skipped songs
     */
    @RequestMapping(value = "/remove", method= RequestMethod.POST)
    public String removeFromPlaylist(SkippedTrackEntity song, Model model, HttpSession session);

    /**
     * Remove a given song from the internal db, but not from its playlist
     */
    @RequestMapping(value = "/dontremove", method= RequestMethod.POST)
    public String keepInPlaylist(SkippedTrackEntity song, Model model, HttpSession session);


}
