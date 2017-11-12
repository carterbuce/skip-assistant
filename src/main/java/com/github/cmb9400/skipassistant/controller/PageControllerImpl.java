package com.github.cmb9400.skipassistant.controller;

import com.github.cmb9400.skipassistant.domain.SkippedTrackEntity;
import com.github.cmb9400.skipassistant.domain.SkippedTrackRepository;
import com.github.cmb9400.skipassistant.exceptions.AlreadyRunningForUserException;
import com.github.cmb9400.skipassistant.service.SpotifyHelperService;
import com.github.cmb9400.skipassistant.service.SpotifyPollingService;
import com.wrapper.spotify.Api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

import java.util.List;

@Controller
public class PageControllerImpl implements PageController {



    @Autowired
    SkippedTrackRepository skippedTrackRepository;

    @Autowired
    SpotifyHelperService spotifyHelperService;


    @Override
    public String index(Model model, HttpSession session) {
        if (session.getAttribute("api") == null) {
            String authURL = spotifyHelperService.getAuthorizationURL();
            model.addAttribute("loginURL", authURL);

            return "index";
        }
        else {
            model.addAttribute("user", session.getAttribute("user"));

            List<SkippedTrackEntity> songs = spotifyHelperService.getTracksForUserId((String) session.getAttribute("user"));
            model.addAttribute("songs", songs);
            model.addAttribute("song", new SkippedTrackEntity()); // placeholder for the remove button

            return "songList";
        }
    }


    @Override
    public String callback(@RequestParam(value="code", required=true) String code, Model model, HttpSession session) {
        try {
            SpotifyPollingService pollingService = spotifyHelperService.getNewPollingService(code);

            try {
                pollingService.init();

                session.setAttribute("user", pollingService.getUser().getId());
                session.setAttribute("api", pollingService.getApi());

                pollingService.run();
            }
            catch (AlreadyRunningForUserException e) {
                String userId = e.getMessage();
                session.setAttribute("user", userId);
                session.setAttribute("api", spotifyHelperService.runningUsers.get(userId).getApi());
            }

            return "redirect:/";
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    @Override
    public String removeFromPlaylist(@ModelAttribute("song") SkippedTrackEntity song, Model model, HttpSession session) {
        spotifyHelperService.removeTrack(song, (Api) session.getAttribute("api"), (String) session.getAttribute("user"));

        return "redirect:/";
    }


    @Override
    public String keepInPlaylist(@ModelAttribute("song") SkippedTrackEntity song, Model model, HttpSession session) {
        skippedTrackRepository.delete(song);

        return "redirect:/";
    }

}
