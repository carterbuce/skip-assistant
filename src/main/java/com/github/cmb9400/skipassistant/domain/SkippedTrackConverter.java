package com.github.cmb9400.skipassistant.domain;

import org.springframework.stereotype.Component;

@Component
public class SkippedTrackConverter {

    public SkippedTrackModel toModel(SkippedTrackEntity entity) {
        SkippedTrackModel model = new SkippedTrackModel();

        model.setPlaylistHref(entity.getPlaylistHref());
        model.setSongUri(entity.getSongUri());
        model.setUserId(entity.getUserId());

        return model;
    }


    public SkippedTrackEntity toEntity(SkippedTrackModel model) {
        SkippedTrackEntity entity = new SkippedTrackEntity();

        entity.setPlaylistHref(model.getPlaylistHref());
        entity.setSongUri(model.getSongUri());
        entity.setUserId(model.getUserId());
        entity.setNumSkips(1);

        return entity;
    }

}
