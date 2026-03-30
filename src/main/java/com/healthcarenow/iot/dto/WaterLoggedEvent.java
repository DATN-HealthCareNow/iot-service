package com.healthcarenow.iot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaterLoggedEvent {
    private String userId;
    private Integer amountMl;
    private String dateString;
}
