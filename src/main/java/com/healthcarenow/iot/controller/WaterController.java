package com.healthcarenow.iot.controller;

import com.healthcarenow.iot.dto.WaterLogRequest;
import com.healthcarenow.iot.entity.WaterLog;
import com.healthcarenow.iot.service.WaterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/water")
@RequiredArgsConstructor
public class WaterController {

    private final WaterService waterService;

    @PostMapping("/log")
    public ResponseEntity<Void> logWater(
            @RequestHeader("x-user-id") String userId,
            @RequestBody WaterLogRequest request) {
        waterService.logWater(userId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/logs/today")
    public ResponseEntity<List<WaterLog>> getTodaysLogs(
            @RequestHeader("x-user-id") String userId) {
        return ResponseEntity.ok(waterService.getTodaysLogs(userId));
    }
}