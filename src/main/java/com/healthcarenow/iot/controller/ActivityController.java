package com.healthcarenow.iot.controller;

import com.healthcarenow.iot.dto.ActivityFinishRequest;
import com.healthcarenow.iot.dto.ActivityStartRequest;
import com.healthcarenow.iot.dto.HeartRateUpdateRequest;
import com.healthcarenow.iot.entity.Activity;
import com.healthcarenow.iot.service.ActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
public class ActivityController {

  private final ActivityService activityService;

  @PostMapping("/start")
  public ResponseEntity<Activity> start(@Valid @RequestBody ActivityStartRequest request) {
    return new ResponseEntity<>(activityService.start(request), HttpStatus.CREATED);
  }

  @PatchMapping("/{id}/heart-rate")
  public ResponseEntity<Void> updateHeartRate(@PathVariable String id,
      @Valid @RequestBody HeartRateUpdateRequest request) {
    activityService.updateHeartRate(id, request);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{id}/finish")
  public ResponseEntity<Activity> finish(@PathVariable String id,
      @RequestBody(required = false) ActivityFinishRequest request) {
    return ResponseEntity.ok(activityService.finish(id, request != null ? request : new ActivityFinishRequest()));
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<Page<Activity>> getUserActivities(
      @PathVariable String userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startAt"));
    return ResponseEntity.ok(activityService.getUserActivities(userId, pageRequest));
  }
}
