package com.healthcarenow.iot.repository;

import com.healthcarenow.iot.entity.GpsTrack;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GpsTrackRepository extends MongoRepository<GpsTrack, String> {
  Optional<GpsTrack> findByActivityId(String activityId);
}
