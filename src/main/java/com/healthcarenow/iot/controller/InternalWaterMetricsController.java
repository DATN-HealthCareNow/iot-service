package com.healthcarenow.iot.controller;

import com.healthcarenow.iot.entity.DailyHealth;
import com.healthcarenow.iot.entity.WeatherLog;
import com.healthcarenow.iot.repository.DailyHealthRepository;
import com.healthcarenow.iot.service.WeatherService;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/internal")
@RequiredArgsConstructor
public class InternalWaterMetricsController {

    private final WeatherService weatherService;
    private final DailyHealthRepository dailyHealthRepository;

    @GetMapping("/water-metrics/{userId}")
    public ResponseEntity<WaterMetricsResponse> getWaterMetrics(
            @PathVariable("userId") String userId,
            @RequestHeader(value = "X-Internal-Token", required = false) String token) {

        if (!"hcn-internal-secret-2024".equals(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Double temperature = null;
        WeatherLog weatherLog = weatherService.getTodayWeather(userId);
        if (weatherLog != null) {
            temperature = weatherLog.getTemperature();
        }

        double avgExercise = 0.0;
        List<DailyHealth> last7Days = dailyHealthRepository.findTop7ByUserIdOrderByDateStringDesc(userId);
        if (last7Days != null && !last7Days.isEmpty()) {
            int totalExerciseMins = 0;
            for (DailyHealth dh : last7Days) {
                if (dh.getMetrics() != null && dh.getMetrics().getExerciseMinutes() != null) {
                    totalExerciseMins += dh.getMetrics().getExerciseMinutes();
                }
            }
            avgExercise = (double) totalExerciseMins / last7Days.size();
        }

        return ResponseEntity.ok(WaterMetricsResponse.builder()
                .temperature(temperature)
                .avgExerciseMinutes(avgExercise)
                .build());
    }

    @Data
    @Builder
    public static class WaterMetricsResponse {
        private Double temperature;
        private Double avgExerciseMinutes;
    }
}
