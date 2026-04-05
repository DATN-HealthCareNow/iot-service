package com.healthcarenow.iot.dto.dashboard;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecentActivityResponse {
  private String id;
  private String timestamp;
  private String userId;
  private String actionType;
  private String status;
  private String details;
}