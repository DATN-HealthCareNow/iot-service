package com.healthcarenow.iot.controller;

import com.healthcarenow.iot.entity.WeatherLog;
import com.healthcarenow.iot.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/iot/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    // App Mobile đẩy dữ liệu thời tiết lên Backend để lưu giữ lịch sử 7 ngày
    @PostMapping("/sync")
    public ResponseEntity<WeatherLog> syncWeather(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody WeatherLog request) {
        request.setUserId(userId);
        return ResponseEntity.ok(weatherService.upsertWeather(request));
    }

    // App Mobile lấy dữ liệu thời tiết đã ghi nhận hôm nay
    @GetMapping("/today")
    public ResponseEntity<WeatherLog> getTodayWeather(
            @RequestHeader("X-User-Id") String userId) {
        WeatherLog log = weatherService.getTodayWeather(userId);
        if (log == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(log);
    }
}
