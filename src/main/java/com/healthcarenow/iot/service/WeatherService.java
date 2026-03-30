package com.healthcarenow.iot.service;

import com.healthcarenow.iot.entity.WeatherLog;
import com.healthcarenow.iot.repository.WeatherLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class WeatherService {

    private final WeatherLogRepository weatherLogRepository;

    /**
     * Cập nhật thời tiết cho người dùng vào ngày hôm nay
     */
    public WeatherLog upsertWeather(WeatherLog payload) {
        if (payload.getDateString() == null || payload.getDateString().isBlank()) {
            payload.setDateString(ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }

        return weatherLogRepository.findByUserIdAndDateString(payload.getUserId(), payload.getDateString())
                .map(existing -> {
                    // Update nội dung nhưng giữ nguyên ngày tạo (để TTL tính từ mốc ban đầu)
                    existing.setLocation(payload.getLocation());
                    existing.setTemperature(payload.getTemperature());
                    existing.setConditionDesc(payload.getConditionDesc());
                    existing.setIconCode(payload.getIconCode());
                    return weatherLogRepository.save(existing);
                })
                .orElseGet(() -> {
                    payload.setCreatedAt(Instant.now());
                    return weatherLogRepository.save(payload);
                });
    }

    /**
     * Lấy thời tiết hôm nay của user, nếu không có trả về rỗng hoắc mặc định
     */
    public WeatherLog getTodayWeather(String userId) {
        String today = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                
        return weatherLogRepository.findByUserIdAndDateString(userId, today)
                .orElse(null); // Bạn có thể thiết lập Default Weather nếu cần
    }
}
