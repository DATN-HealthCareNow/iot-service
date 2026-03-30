package com.healthcarenow.iot.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@Document(collection = "activities")
public class Activity {

  @Id
  private String id;

  @Indexed
  private String userId;

  private Type type;
  private Mode mode;
  private Status status;

  private Instant startAt;
  private Instant endAt;

  private SummaryMetrics summaryMetrics;
  private OutdoorContext outdoorContext;
  private IndoorContext indoorContext;

  private List<HeartRateSample> heartRateSamples;

  public enum Type {
    RUN, CYCLING, WALKING, STRETCHING, YOGA, GYM, HIKE
  }

  public enum Mode {
    OUTDOOR, INDOOR
  }

  public enum Status {
    ACTIVE, FINISHED
  }

  @Data
  @Builder
  public static class SummaryMetrics {
    private long totalDuration; // Example: in seconds
    private double activeCalories;
    private double avgHeartRate;
  }

  @Data
  @Builder
  public static class OutdoorContext {
    private String gpsTrackId;
    private double distanceMeter;
    private double pace;
  }

  @Data
  @Builder
  public static class IndoorContext {
    private List<WorkoutLog> workoutLog;

    @Data
    @Builder
    public static class WorkoutLog {
      private String exercise;
      private int reps;
      private double weight;
    }
  }

  @Data
  @Builder
  public static class HeartRateSample {
    private Instant timestamp;
    private double bpm;
  }
}
