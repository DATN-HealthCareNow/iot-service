package com.healthcarenow.iot.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class HeartRateUpdateRequest {

  @NotNull(message = "Timestamp is required")
  private Instant timestamp;

  @Min(value = 30, message = "BPM must be at least 30")
  private double bpm;
}
