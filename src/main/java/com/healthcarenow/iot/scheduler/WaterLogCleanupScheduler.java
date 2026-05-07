package com.healthcarenow.iot.scheduler;

import com.healthcarenow.iot.repository.WaterLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class WaterLogCleanupScheduler {

    private final WaterLogRepository waterLogRepository;

    @Scheduled(cron = "0 0 2 * * ?") // Run at 2 AM every day
    public void cleanupOldWaterLogs() {
        log.info("[WaterLogCleanup] Starting cleanup of water logs older than 7 days...");
        try {
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            waterLogRepository.deleteByCreatedAtBefore(sevenDaysAgo);
            log.info("[WaterLogCleanup] Finished cleaning up old water logs.");
        } catch (Exception e) {
            log.error("[WaterLogCleanup] Error during water log cleanup", e);
        }
    }
}
