package com.github.cmb9400.skipassistant.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageControllerImpl implements PageController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PageControllerImpl.class);

    @Override
    public String index(@RequestParam(value="name", required=false, defaultValue="World") String name, Model model) {
        model.addAttribute("name", name);
        return "index";
    }
}
