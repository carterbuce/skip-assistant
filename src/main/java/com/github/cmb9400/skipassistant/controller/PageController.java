package com.github.cmb9400.skipassistant.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

public interface PageController {

    @RequestMapping("/")
    public String index(Model model, HttpSession session);

    @RequestMapping("/callback")
    public String callback(String code, Model model, HttpSession session);


}
