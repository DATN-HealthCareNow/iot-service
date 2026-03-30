package com.healthcarenow.iot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WaterLogRequest {
    @JsonProperty("amount_ml")
    private Integer amountMl;

    @JsonProperty("adjustment_reason")
    private String adjustmentReason;
}