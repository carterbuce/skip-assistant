package com.github.cmb9400.skipassistant.controller;

import com.github.cmb9400.skipassistant.domain.SkippedTrackRepository;
import com.github.cmb9400.skipassistant.service.SpotifyPollingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageControllerImpl implements PageController {

    @Autowired
    SkippedTrackRepository skippedTrackRepository;

    @Autowired
    ApplicationContext applicationContext;

    @Override
    public String index(@RequestParam(value="name", required=false, defaultValue="World") String name, Model model) {
        SpotifyPollingService pollingService1 = (SpotifyPollingService) applicationContext.getBean("spotifyPollingService", "foo1");
        SpotifyPollingService pollingService2 = (SpotifyPollingService) applicationContext.getBean("spotifyPollingService", "foo2");

        pollingService1.findSkippedSongs();
        pollingService2.findSkippedSongs();

        model.addAttribute("name", name);
        return "index";
    }
}
