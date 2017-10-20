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
        model.addAttribute("name", name);
        return "index";
    }

    @Override
    public String login(Model model) {
        SpotifyPollingService pollingService1 = (SpotifyPollingService) applicationContext.getBean("spotifyPollingService", "foo1");

        String authURL = pollingService1.getAuthorizationURL();
        model.addAttribute("link", authURL);

        return "login";
    }

    @Override
    public String callback(@RequestParam(value="code", required=true) String code, Model model) {
        model.addAttribute("code", code);
        return "callback";
    }
}
