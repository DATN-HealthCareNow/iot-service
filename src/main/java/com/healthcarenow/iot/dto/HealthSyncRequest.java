package com.healthcarenow.iot.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthSyncRequest {
    @JsonAlias({"userId", "user_id"})
    private String userId;

    @JsonAlias({"dateString", "date_string"})
    private String dateString;

    @JsonAlias({"dateStringLocal", "date_string_local"})
    private String dateStringLocal;

    @JsonAlias({"rawDate", "raw_date"})
    private String rawDate;

    private String source;

    private Metrics metrics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metrics {
        @JsonAlias({"steps"})
        private Double steps;

        @JsonAlias({"exerciseMinutes", "exercise_minutes"})
        private Integer exerciseMinutes;

        @JsonAlias({"googleExerciseMinutes", "google_exercise_minutes"})
        private Integer googleExerciseMinutes;

        @JsonAlias({"activeCalories", "active_calories"})
        private Integer activeCalories;

        @JsonAlias({"restingCalories", "resting_calories"})
        private Integer restingCalories;

        @JsonAlias({"sleepMinutes", "sleep_minutes"})
        private Integer sleepMinutes;

        @JsonAlias({"waterConsumedMl", "water_consumed_ml"})
        private Integer waterConsumedMl;

        @JsonAlias({"heartRate", "heart_rate"})
        private Integer heartRate;
    }
}
