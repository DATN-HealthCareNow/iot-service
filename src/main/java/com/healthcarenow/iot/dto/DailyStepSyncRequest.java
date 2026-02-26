package com.healthcarenow.iot.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class DailyStepSyncRequest {

  @NotNull(message = "Date is required")
  private Instant date;

  @Min(value = 0, message = "Steps cannot be negative")
  private int steps;

  @NotBlank(message = "Source is required (e.g., GoogleFit, HealthKit)")
  private String source;
}
