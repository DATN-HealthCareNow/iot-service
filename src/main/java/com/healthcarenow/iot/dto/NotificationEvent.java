package com.healthcarenow.iot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
  private String eventType;
  private String userId;
  private String priority;
  private Map<String, Object> payload;
}
