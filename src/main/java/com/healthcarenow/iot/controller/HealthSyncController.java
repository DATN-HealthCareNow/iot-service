package com.healthcarenow.iot.controller;

import com.healthcarenow.iot.entity.DailyHealth;
import com.healthcarenow.iot.service.HealthDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tracking")
@RequiredArgsConstructor
public class HealthSyncController {

    private final HealthDataService healthDataService;

    @PostMapping("/health-sync")
    public ResponseEntity<DailyHealth> syncHealthData(@RequestBody DailyHealth payload) {
        return ResponseEntity.ok(healthDataService.upsertHealthData(payload));
    }

    @GetMapping("/seed-health-data")
    public ResponseEntity<String> seedHealthData(
            @RequestParam String userId,
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(healthDataService.seedHealthData(userId, days));
    }
}
