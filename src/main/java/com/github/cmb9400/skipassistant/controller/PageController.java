package com.github.cmb9400.skipassistant.controller;

import com.github.cmb9400.skipassistant.domain.SkippedTrackEntity;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpSession;

public interface PageController {

    @RequestMapping("/")
    public String index(Model model, HttpSession session);

    @RequestMapping("/callback")
    public String callback(String code, Model model, HttpSession session);

    @RequestMapping(value = "/remove", method= RequestMethod.POST)
    public String processForm(SkippedTrackEntity song, Model model, HttpSession session);


}
