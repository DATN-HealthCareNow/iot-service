package com.healthcarenow.iot.dto;

import com.healthcarenow.iot.entity.ExerciseSchedule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class ScheduleCreateRequest {

  @NotBlank(message = "Title is required")
  private String title;

  @NotNull(message = "Schedule type is required")
  private ExerciseSchedule.ScheduleType scheduleType;

  @NotNull(message = "Start date is required")
  private Instant startDate;

  private boolean reminderEnabled;

  private ExerciseSchedule.RecurrenceConfig recurrenceConfig;
}
