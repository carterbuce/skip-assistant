package com.github.cmb9400.skipassistant.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

public interface PageController {

    @RequestMapping("/")
    public String index(String name, Model model);

    @RequestMapping("/login")
    public String login(Model model);

    @RequestMapping("/callback")
    public String callback(String code, Model model);


}
