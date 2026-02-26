package com.healthcarenow.iot.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityCompletedEvent {
  private String activityId;
  private String userId;
  private int activeCalories;
  private long totalDurationStr; // in seconds or similar
}
