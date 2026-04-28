package com.healthcarenow.iot.dto;

import lombok.Data;

import java.util.List;

@Data
public class ActivityFinishRequest {

  // Only needed for Outdoor
  private Double distanceMeter;

  // Optional frontend overrides for Gym/Yoga
  @com.fasterxml.jackson.annotation.JsonProperty("exercise_minutes")
  private Integer exerciseMinutes;
  
  @com.fasterxml.jackson.annotation.JsonProperty("active_calories")
  private Double activeCalories;

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
