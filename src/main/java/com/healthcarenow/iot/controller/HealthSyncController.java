package com.healthcarenow.iot.controller;

import com.healthcarenow.iot.entity.DailyHealth;
import com.healthcarenow.iot.service.HealthDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tracking")
@RequiredArgsConstructor
public class HealthSyncController {

    private final HealthDataService healthDataService;

    @PostMapping("/health-sync")
    public ResponseEntity<DailyHealth> syncHealthData(
            @RequestHeader(value = "x-user-id", required = true) String userId,
            @RequestBody DailyHealth payload) {
        
        // Ghi đè userId từ Header (bảo mật)
        payload.setUserId(userId);
        return ResponseEntity.ok(healthDataService.upsertHealthData(payload));
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
