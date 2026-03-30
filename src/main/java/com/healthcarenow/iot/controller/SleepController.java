package com.healthcarenow.iot.controller;

import com.healthcarenow.iot.entity.SleepSession;
import com.healthcarenow.iot.service.SleepService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/iot/sleep")
@RequiredArgsConstructor
public class SleepController {

    private final SleepService sleepService;

    @PostMapping("/start")
    public ResponseEntity<SleepSession> startSleep(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody StartSleepRequest request) {
        
        SleepSession session = sleepService.startSleep(userId, request.getTargetSleepMinutes());
        return ResponseEntity.ok(session);
    }

    @PutMapping("/end/{id}")
    public ResponseEntity<SleepSession> endSleep(
            @PathVariable String id,
            @RequestBody EndSleepRequest request) {
        
        SleepSession session = sleepService.endSleep(id, request.getWakeupsCount());
        return ResponseEntity.ok(session);
    }

    @Data
    public static class StartSleepRequest {
        private Integer targetSleepMinutes;
    }

    @Data
    public static class EndSleepRequest {
        private Integer wakeupsCount;
    }
}
