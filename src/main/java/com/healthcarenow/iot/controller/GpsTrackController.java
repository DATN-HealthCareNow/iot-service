package com.healthcarenow.iot.controller;

import com.healthcarenow.iot.dto.GpsBatchRequest;
import com.healthcarenow.iot.entity.GpsTrack;
import com.healthcarenow.iot.service.GpsTrackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gps-tracks")
@RequiredArgsConstructor
public class GpsTrackController {

  private final GpsTrackService gpsTrackService;

  @PostMapping("/{activityId}/batch")
  public ResponseEntity<Void> batchPoints(@PathVariable String activityId,
      @Valid @RequestBody GpsBatchRequest request) {
    gpsTrackService.addBatchPoints(activityId, request);
    return ResponseEntity.accepted().build(); // 202 Accepted because it's async processing
  }

  @GetMapping("/{activityId}")
  public ResponseEntity<GpsTrack> getTrack(@PathVariable String activityId) {
    GpsTrack track = gpsTrackService.getTrackByActivityId(activityId);
    if (track == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(track);
  }
}
