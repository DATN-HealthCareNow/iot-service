package com.healthcarenow.iot.service;

import com.healthcarenow.iot.dto.GpsBatchRequest;
import com.healthcarenow.iot.entity.GpsTrack;
import com.healthcarenow.iot.repository.GpsTrackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GpsTrackService {

  private final GpsTrackRepository gpsTrackRepository;

  @Async
  public void addBatchPoints(String activityId, GpsBatchRequest request) {
    log.info("Processing async GPS batch for activity: {}, size: {}", activityId, request.getPoints().size());

    GpsTrack track = gpsTrackRepository.findByActivityId(activityId)
        .orElseGet(() -> {
          return GpsTrack.builder()
              .activityId(activityId)
              .points(new ArrayList<>())
              // Set TTL expiration to 30 days from now
              .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
              .build();
        });

    // Convert DTOs to Entities
    List<GpsTrack.TrackPoint> newPoints = request.getPoints().stream().map(p -> GpsTrack.TrackPoint.builder()
        .loc(new GeoJsonPoint(p.getLng(), p.getLat())) // Longitude first in GeoJSON
        .ts(p.getTs())
        .acc(p.getAcc())
        .build()).toList();

    track.getPoints().addAll(newPoints);

    gpsTrackRepository.save(track);
  }

  public GpsTrack getTrackByActivityId(String activityId) {
    return gpsTrackRepository.findByActivityId(activityId)
        .orElse(null);
  }
}
