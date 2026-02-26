package com.healthcarenow.iot.dto;

import com.healthcarenow.iot.entity.Activity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActivityStartRequest {

  @NotNull(message = "Activity type is required")
  private Activity.Type type;

  @NotNull(message = "Activity mode is required")
  private Activity.Mode mode;
}
