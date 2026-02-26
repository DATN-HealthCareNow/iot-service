package com.healthcarenow.iot.dto;

import lombok.Data;

import java.util.List;

@Data
public class ActivityFinishRequest {

  // Only needed for Outdoor
  private Double distanceMeter;

  // Needed for all cases to calculate duration if we allow client overriding or
  // just base data
  // Assuming end calculation happens server side via server Instant.now(), we
  // just expect context here

  // Only needed for Indoor
  private List<WorkoutLogDto> workoutLogs;

  @Data
  public static class WorkoutLogDto {
    private String exercise;
    private int reps;
    private double weight;
  }
}
