package com.healthcarenow.iot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaterProgressDTO {
    @JsonProperty("current_amount")
    private Integer currentAmount;

    @JsonProperty("goal_amount")
    private Integer goalAmount;

    private Double percentage;

    private List<WaterLogDTO> logs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WaterLogDTO {
        @JsonProperty("amount_ml")
        private Integer amountMl;

        @JsonProperty("adjustment_reason")
        private String adjustmentReason;

        private String time; // e.g., "10:30 AM"
    }
}