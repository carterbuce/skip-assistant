package com.github.cmb9400.skipassistant.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkippedTrackRepository extends CrudRepository<SkippedTrackEntity, Long> {
}
