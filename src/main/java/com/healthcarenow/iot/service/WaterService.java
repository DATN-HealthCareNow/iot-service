package com.healthcarenow.iot.service;

import com.healthcarenow.iot.config.RabbitMQConfig;
import com.healthcarenow.iot.dto.EventEnvelope;
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
import java.util.UUID;

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

        // Update Daily Health Metrics
        DailyHealth dailyHealth = dailyHealthRepository.findByUserIdAndDateString(userId, today)
                .orElseGet(() -> {
                    DailyHealth.Metrics metrics = new DailyHealth.Metrics();
                    return DailyHealth.builder()
                            .userId(userId)
                            .dateString(today)
                            .rawDate(ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z")))
                            .source("WaterLog")
                            .metrics(metrics)
                            .build();
                });
        
        if (dailyHealth.getMetrics() == null) {
            dailyHealth.setMetrics(new DailyHealth.Metrics());
        }
        dailyHealthRepository.save(dailyHealth);

        // Publish event to core-service
        WaterLoggedEvent event = WaterLoggedEvent.builder()
                .userId(userId)
                .amountMl(request.getAmountMl())
                .dateString(today)
                .build();

        String eventId = UUID.randomUUID().toString();
        EventEnvelope<WaterLoggedEvent> envelope = EventEnvelope.<WaterLoggedEvent>builder()
                .eventId(eventId)
                .eventType("water.logged")
                .eventVersion(1)
                .timestamp(ZonedDateTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .correlationId(eventId)
                .payload(event)
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.WATER_LOGGING_ROUTING_KEY, envelope);
        log.info("Published water event envelope id={} for user {} with amount {}", eventId, userId, request.getAmountMl());
    }

    public List<WaterLog> getTodaysLogs(String userId) {
        String today = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        return waterLogRepository.findByUserIdAndDateString(userId, today);
    }
}