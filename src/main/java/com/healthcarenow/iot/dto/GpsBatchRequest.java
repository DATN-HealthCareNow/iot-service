package com.healthcarenow.iot.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class GpsBatchRequest {

  @NotEmpty(message = "Points list cannot be empty")
  private List<GpsPointDto> points;

  @Data
  public static class GpsPointDto {
    @NotNull(message = "Latitude is required")
    private Double lat;

    @NotNull(message = "Longitude is required")
    private Double lng;

    @NotNull(message = "Timestamp is required")
    private Instant ts;

    private double acc;
  }
}
