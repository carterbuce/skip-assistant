package com.github.cmb9400.skipassistant.service;

import com.github.cmb9400.skipassistant.controller.PageControllerImpl;
import com.github.cmb9400.skipassistant.domain.SkippedTrackEntity;
import com.github.cmb9400.skipassistant.domain.SkippedTrackRepository;
import com.wrapper.spotify.Api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.concurrent.TimeUnit;

@Service
@Scope("prototype")
public class SpotifyPollingService {

    @Resource
    public Environment env;

    @Autowired
    private SkippedTrackRepository skippedTrackRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(PageControllerImpl.class);
    private String key;
    private Api api;


    /**
     * Constructor for a polling service object. Key is not autowired, it is passed in during creation
     * @param key TODO placeholder until method implemented
     */
    @SuppressWarnings("SpringJavaAutowiringInspection")
    public SpotifyPollingService(String key) {
        this.key = key;
    }


    /**
     * Start searching for skipped songs asynchronously
     * TODO placeholder until method implemented
     */
    @Async
    public void findSkippedSongs() {
        for (int i = 0; i < 3; i++) {
            try {
                LOGGER.info("Found skipped song: " + key);
                skippedTrackRepository.save(new SkippedTrackEntity(key, "bar"));
                TimeUnit.SECONDS.sleep(10);
            }
            catch (InterruptedException e){
                LOGGER.error("Polling service interrupted!");
            }
        }
    }

}
