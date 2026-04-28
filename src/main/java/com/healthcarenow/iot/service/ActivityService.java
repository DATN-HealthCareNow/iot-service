package com.healthcarenow.iot.service;

import com.healthcarenow.iot.common.context.UserContextHolder;
import com.healthcarenow.iot.common.event.ActivityCompletedEvent;
import com.healthcarenow.iot.dto.ActivityFinishRequest;
import com.healthcarenow.iot.dto.ActivityStartRequest;
import com.healthcarenow.iot.dto.HeartRateUpdateRequest;
import com.healthcarenow.iot.entity.Activity;
import com.healthcarenow.iot.entity.DailyHealth;
import com.healthcarenow.iot.repository.ActivityRepository;
import com.healthcarenow.iot.repository.DailyHealthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService {

  private final ActivityRepository activityRepository;
  private final DailyHealthRepository dailyHealthRepository;
  private final EventPublisher eventPublisher;

  public Activity start(ActivityStartRequest request) {
    String userId = UserContextHolder.getUserId();

    Activity activity = Activity.builder()
        .userId(userId)
        .type(request.getType())
        .mode(request.getMode())
        .status(Activity.Status.ACTIVE)
        .startAt(Instant.now())
        .heartRateSamples(new ArrayList<>())
        .build();

    return activityRepository.save(activity);
  }

  public void updateHeartRate(String id, HeartRateUpdateRequest request) {
    Activity activity = activityRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Activity not found"));

    if (activity.getStatus() != Activity.Status.ACTIVE) {
      throw new IllegalArgumentException("Cannot update heart rate for finished activity");
    }

    Activity.HeartRateSample sample = Activity.HeartRateSample.builder()
        .timestamp(request.getTimestamp())
        .bpm(request.getBpm())
        .build();

    activity.getHeartRateSamples().add(sample);
    activityRepository.save(activity);
  }

  public Activity finish(String id, ActivityFinishRequest request) {
    Activity activity = activityRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Activity not found"));

    if (activity.getStatus() == Activity.Status.FINISHED) {
      return activity;
    }

    activity.setEndAt(Instant.now());
    activity.setStatus(Activity.Status.FINISHED);

    long durationSecs = request.getExerciseMinutes() != null ? request.getExerciseMinutes() * 60L : Duration.between(activity.getStartAt(), activity.getEndAt()).getSeconds();
    double avgHr = activity.getHeartRateSamples().stream()
        .mapToDouble(Activity.HeartRateSample::getBpm)
        .average()
        .orElse(0.0);

    // Core business logic: Calorie formula
    double calories = request.getActiveCalories() != null ? request.getActiveCalories() : calculateCalories(activity, durationSecs, request);

    Activity.SummaryMetrics summary = Activity.SummaryMetrics.builder()
        .totalDuration(durationSecs)
        .avgHeartRate(avgHr)
        .activeCalories(calories)
        .build();

    activity.setSummaryMetrics(summary);

    // Map context
    if (activity.getMode() == Activity.Mode.OUTDOOR && request.getDistanceMeter() != null) {
      double pace = durationSecs / (request.getDistanceMeter() / 1000.0); // sec/km
      Activity.OutdoorContext out = Activity.OutdoorContext.builder()
          .distanceMeter(request.getDistanceMeter())
          .pace(pace)
          .build();
      activity.setOutdoorContext(out);
    } else if (activity.getMode() == Activity.Mode.INDOOR && request.getWorkoutLogs() != null) {
      Activity.IndoorContext in = Activity.IndoorContext.builder()
          .workoutLog(request.getWorkoutLogs().stream().map(l -> Activity.IndoorContext.WorkoutLog.builder()
              .exercise(l.getExercise())
              .reps(l.getReps())
              .weight(l.getWeight())
              .build()).toList())
          .build();
      activity.setIndoorContext(in);
    }

    final Activity savedActivity = activityRepository.save(activity);

    // Increment metrics in DailyHealth
    String dateStr = ZonedDateTime.ofInstant(savedActivity.getStartAt(), ZoneId.of("Asia/Ho_Chi_Minh"))
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    dailyHealthRepository.findByUserIdAndDateString(savedActivity.getUserId(), dateStr)
        .ifPresentOrElse(dh -> {
          DailyHealth.Metrics mx = dh.getMetrics();
          if (mx == null) {
            mx = new DailyHealth.Metrics();
            dh.setMetrics(mx);
          }
          int addedMins = (int) (durationSecs / 60);
          mx.setExerciseMinutes((mx.getExerciseMinutes() != null ? mx.getExerciseMinutes() : 0) + addedMins);
          mx.setActiveCalories((mx.getActiveCalories() != null ? mx.getActiveCalories() : 0) + (int) calories);
          if (request.getDistanceMeter() != null) {
            mx.setDistanceMeters(
                (mx.getDistanceMeters() != null ? mx.getDistanceMeters() : 0.0) + request.getDistanceMeter());
          }
          dailyHealthRepository.save(dh);
        }, () -> {
          DailyHealth dh = DailyHealth.builder()
              .userId(savedActivity.getUserId())
              .dateString(dateStr)
              .source("Activity")
              .metrics(DailyHealth.Metrics.builder()
                  .exerciseMinutes((int) (durationSecs / 60))
                  .activeCalories((int) calories)
                  .distanceMeters(request.getDistanceMeter() != null ? request.getDistanceMeter().doubleValue() : 0.0)
                  .build())
              .build();
          dailyHealthRepository.save(dh);
        });

    // Publish Event async
    ActivityCompletedEvent event = ActivityCompletedEvent.builder()
        .activityId(savedActivity.getId())
        .userId(savedActivity.getUserId())
        .activeCalories((int) calories)
        .totalDurationStr(durationSecs)
        .build();
    eventPublisher.publishActivityCompleted(event);

    // Không ghi log lịch sử tập luyện nữa, xóa record Activity
    activityRepository.deleteById(savedActivity.getId());

    return savedActivity;
  }

  private double calculateCalories(Activity activity, long durationSecs, ActivityFinishRequest request) {
    // Simplified mockup formulas. In real life, these also use user's
    // weight/age/gender from Core Service
    double durationMins = durationSecs / 60.0;
    return switch (activity.getType()) {
      case RUN -> {
        double distKm = request.getDistanceMeter() != null ? request.getDistanceMeter() / 1000.0 : 0;
        // rule of thumb: 1 km = 60-70 cals approx
        yield distKm * 65.0;
      }
      case YOGA -> durationMins * 3.0; // Approx 3 cal/min
      case STRETCHING -> durationMins * 2.5; // Approx 2.5 cal/min
      case GYM -> durationMins * 5.0; // Approx 5 cal/min
      case HIKE -> durationMins * 6.0; // Approx 6 cal/min
    };
  }

  public Page<Activity> getUserActivities(String userId, Pageable pageable) {
    return activityRepository.findByUserId(userId, pageable);
  }
}
