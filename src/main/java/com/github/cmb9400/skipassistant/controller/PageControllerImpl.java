package com.github.cmb9400.skipassistant.controller;

import com.github.cmb9400.skipassistant.domain.SkippedTrackEntity;
import com.github.cmb9400.skipassistant.domain.SkippedTrackRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageControllerImpl implements PageController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PageControllerImpl.class);

    @Autowired
    SkippedTrackRepository skippedTrackRepository;

    @Autowired
    ApplicationContext context;

    @Override
    public String index(@RequestParam(value="name", required=false, defaultValue="World") String name, Model model) {
        skippedTrackRepository.save(new SkippedTrackEntity("foo", "bar"));

        model.addAttribute("name", name);
        return "index";
    }
}
