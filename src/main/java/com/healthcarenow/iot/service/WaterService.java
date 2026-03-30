package com.healthcarenow.iot.service;

import com.healthcarenow.iot.config.RabbitMQConfig;
import com.healthcarenow.iot.dto.WaterLogRequest;
import com.healthcarenow.iot.dto.WaterLoggedEvent;
import com.healthcarenow.iot.entity.DailyHealth;
import com.healthcarenow.iot.entity.WaterLog;
import com.healthcarenow.iot.entity.WeatherLog;
import com.healthcarenow.iot.repository.DailyHealthRepository;
import com.healthcarenow.iot.repository.WaterLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WaterService {

    private final WaterLogRepository waterLogRepository;
    private final DailyHealthRepository dailyHealthRepository;
    private final WeatherService weatherService;
    private final RabbitTemplate rabbitTemplate;

    public void logWater(String userId, WaterLogRequest request) {
        String today = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        WaterLog logEntry = WaterLog.builder()
                .userId(userId)
                .amountMl(request.getAmountMl())
                .adjustmentReason(request.getAdjustmentReason())
                .dateString(today)
                .createdAt(LocalDateTime.now())
                .build();
        waterLogRepository.save(logEntry);

        // Publish event to core-service
        WaterLoggedEvent event = WaterLoggedEvent.builder()
                .userId(userId)
                .amountMl(request.getAmountMl())
                .dateString(today)
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.WATER_LOGGING_ROUTING_KEY, event);
        log.info("Published WaterLoggedEvent for user {} with amount {}", userId, request.getAmountMl());
    }

    public List<WaterLog> getTodaysLogs(String userId) {
        String today = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        return waterLogRepository.findByUserIdAndDateString(userId, today);
    }
}