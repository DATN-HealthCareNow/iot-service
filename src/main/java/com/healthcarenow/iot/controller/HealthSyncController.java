package com.healthcarenow.iot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcarenow.iot.dto.HealthSyncRequest;
import com.healthcarenow.iot.entity.DailyHealth;
import com.healthcarenow.iot.service.HealthDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tracking")
@RequiredArgsConstructor
@Slf4j
public class HealthSyncController {

    private final HealthDataService healthDataService;
    private final ObjectMapper objectMapper;

    @PostMapping("/health-sync")
    public ResponseEntity<DailyHealth> syncHealthData(
            @RequestHeader(value = "x-user-id", required = true) String userId,
            @RequestBody HealthSyncRequest request) {
        
        log.info("[ENDPOINT] POST /api/v1/tracking/health-sync called with userId={}", userId);
        log.debug("[ENDPOINT] Raw request: {}", request);
        
        if (request == null) {
            log.error("[ENDPOINT] ❌ Request is NULL!");
            return ResponseEntity.badRequest().build();
        }

        String resolvedDateStringLocal = request.getDateStringLocal();
        if (resolvedDateStringLocal == null || resolvedDateStringLocal.isBlank()) {
            resolvedDateStringLocal = request.getDateString();
        }
        if (resolvedDateStringLocal == null || resolvedDateStringLocal.isBlank()) {
            resolvedDateStringLocal = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            log.warn("[ENDPOINT] date_string_local is missing, fallback to {}", resolvedDateStringLocal);
        }

        String resolvedRawDate = request.getRawDate();
        if (resolvedRawDate == null || resolvedRawDate.isBlank()) {
            resolvedRawDate = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            log.warn("[ENDPOINT] raw_date is missing, fallback to {}", resolvedRawDate);
        }
        
        // Convert HealthSyncRequest to DailyHealth
        log.info("[ENDPOINT] Converting HealthSyncRequest to DailyHealth...");
        DailyHealth payload = DailyHealth.builder()
                .userId(userId)  // Override with header value for security
            .dateString(resolvedDateStringLocal)
            .dateStringLocal(resolvedDateStringLocal)
            .rawDate(resolvedRawDate)
                .source(request.getSource() != null ? request.getSource() : "GOOGLE_FIT")
                .build();
        
        // Map metrics
        if (request.getMetrics() != null) {
            DailyHealth.Metrics metrics = DailyHealth.Metrics.builder()
                    .steps(request.getMetrics().getSteps() != null ? request.getMetrics().getSteps() : 0.0)
                    .exerciseMinutes(request.getMetrics().getExerciseMinutes() != null ? request.getMetrics().getExerciseMinutes() : 0)
                    .googleExerciseMinutes(request.getMetrics().getGoogleExerciseMinutes() != null ? request.getMetrics().getGoogleExerciseMinutes() : 0)
                    .activeCalories(request.getMetrics().getActiveCalories() != null ? request.getMetrics().getActiveCalories() : 0)
                    .restingCalories(request.getMetrics().getRestingCalories() != null ? request.getMetrics().getRestingCalories() : 1400)
                    .sleepMinutes(request.getMetrics().getSleepMinutes() != null ? request.getMetrics().getSleepMinutes() : 0)
                    .heartRate(request.getMetrics().getHeartRate() != null ? request.getMetrics().getHeartRate() : 0)
                    .build();
            payload.setMetrics(metrics);
        } else {
            payload.setMetrics(DailyHealth.Metrics.builder().build());
            log.warn("[ENDPOINT] metrics is missing, using default metrics values");
        }
        
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            log.debug("[ENDPOINT] Converted DailyHealth payload: {}", jsonPayload);
        } catch (Exception e) {
            log.error("[ENDPOINT] Error serializing payload for logging", e);
        }
        
        log.info("[ENDPOINT] Calling service.upsertHealthData()...");
        DailyHealth result = healthDataService.upsertHealthData(payload);
        log.info("[ENDPOINT] ✅ Sync completed successfully, result id={}", result.getId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/seed-health-data")
    public ResponseEntity<String> seedHealthData(
            @RequestHeader(value = "x-user-id", required = true) String userId,
            @RequestParam(defaultValue = "7") int days) {
        
        // Sử dụng luôn userId từ Header
        return ResponseEntity.ok(healthDataService.seedHealthData(userId, days));
    }

    @GetMapping("/daily")
    public ResponseEntity<DailyHealth> getDailyHealth(
            @RequestHeader(value = "x-user-id", required = true) String userId,
            @RequestParam(required = false, defaultValue = "") String date) {
        
        // Gọi service lấy data thật, nếu không có sẽ tự động xả hàng Random giả
        return ResponseEntity.ok(healthDataService.getDailyHealth(userId, date));
    }

    @GetMapping("/report")
    public ResponseEntity<List<DailyHealth>> getHealthReport(
            @RequestHeader(value = "x-user-id", required = true) String userId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return ResponseEntity.ok(healthDataService.getHealthReport(userId, startDate, endDate));
    }
}
