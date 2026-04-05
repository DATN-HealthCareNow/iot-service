package com.healthcarenow.iot.controller;

import com.healthcarenow.iot.dto.dashboard.RecentActivityResponse;
import com.healthcarenow.iot.entity.Activity;
import com.healthcarenow.iot.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/v1/internal/activities")
@RequiredArgsConstructor
public class InternalActivityController {

  private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_INSTANT;

  private final ActivityRepository activityRepository;

  @GetMapping("/recent")
  public ResponseEntity<List<RecentActivityResponse>> getRecentActivities(
      @RequestParam(defaultValue = "8") int size) {
    int normalizedSize = Math.max(1, Math.min(size, 50));
    PageRequest pageRequest = PageRequest.of(0, normalizedSize, Sort.by(Sort.Direction.DESC, "startAt"));

    List<RecentActivityResponse> response = activityRepository.findAll(pageRequest)
        .stream()
        .map(this::toResponse)
        .toList();

    return ResponseEntity.ok(response);
  }

  private RecentActivityResponse toResponse(Activity activity) {
    String timestamp = activity.getEndAt() != null
        ? TIMESTAMP_FORMATTER.format(activity.getEndAt())
        : activity.getStartAt() != null ? TIMESTAMP_FORMATTER.format(activity.getStartAt()) : null;

    String actionType = activity.getType() != null
        ? activity.getType().name() + (activity.getMode() != null ? " / " + activity.getMode().name() : "")
        : "Activity";

    String details;
    if (activity.getSummaryMetrics() != null) {
      details = String.format(
          "Duration %ds · %.0f kcal · Avg HR %.0f bpm",
          activity.getSummaryMetrics().getTotalDuration(),
          activity.getSummaryMetrics().getActiveCalories(),
          activity.getSummaryMetrics().getAvgHeartRate());
    } else if (activity.getStatus() == Activity.Status.ACTIVE) {
      details = "Activity in progress";
    } else {
      details = "No summary available";
    }

    return RecentActivityResponse.builder()
        .id(activity.getId())
        .timestamp(timestamp)
        .userId(activity.getUserId())
        .actionType(actionType)
        .status(activity.getStatus() != null ? activity.getStatus().name() : "UNKNOWN")
        .details(details)
        .build();
  }
}