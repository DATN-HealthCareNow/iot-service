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
@Document(collection = "exercise_schedules")
public class ExerciseSchedule {

  @Id
  private String id;

  @Indexed
  private String userId;

  private String title;

  private ScheduleType scheduleType;

  private Instant startDate;

  private boolean reminderEnabled;

  private String sourceId; // Reference to medical record or scan

  private RecurrenceConfig recurrenceConfig;

  public enum ScheduleType {
    ONE_TIME, RECURRING
  }

  @Data
  @Builder
  public static class RecurrenceConfig {
    private List<Integer> repeatDays; // e.g., Calendar.MONDAY or custom integer representation
    private String reminderTime; // e.g., "08:00", stored as HH:MM string
    private List<String> reminderTimes; // Support multiple times per day
  }
}
