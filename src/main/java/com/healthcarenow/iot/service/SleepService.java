package com.healthcarenow.iot.service;

import com.healthcarenow.iot.entity.DailyHealth;
import com.healthcarenow.iot.entity.SleepSession;
import com.healthcarenow.iot.repository.DailyHealthRepository;
import com.healthcarenow.iot.repository.SleepSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class SleepService {

    private final SleepSessionRepository sleepSessionRepository;
    private final HealthDataService healthDataService;
    private final DailyHealthRepository dailyHealthRepository;

    public SleepSession startSleep(String userId, Integer targetSleepMinutes) {
        // Kiểm tra xem có session nào đang chạy không
        sleepSessionRepository.findFirstByUserIdAndStatusOrderByStartAtDesc(userId, SleepSession.Status.ACTIVE)
                .ifPresent(session -> {
                    session.setStatus(SleepSession.Status.FINISHED);
                    sleepSessionRepository.save(session);
                });

        SleepSession session = SleepSession.builder()
                .userId(userId)
                .status(SleepSession.Status.ACTIVE)
                .startAt(Instant.now())
                .targetSleepMinutes(targetSleepMinutes)
                .build();
        return sleepSessionRepository.save(session);
    }

    public SleepSession endSleep(String id, Integer wakeupsCount) {
        SleepSession session = sleepSessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Sleep Session tương ứng"));

        if (session.getStatus() == SleepSession.Status.FINISHED) {
            return session;
        }

        Instant now = Instant.now();
        session.setEndAt(now);
        session.setStatus(SleepSession.Status.FINISHED);
        session.setWakeupsCount(wakeupsCount);

        // Logic chấm điểm và tính toán
        long durationMin = Duration.between(session.getStartAt(), now).toMinutes();
        int penalty = (wakeupsCount != null ? wakeupsCount : 0) * 5; // Trừ mỗi lần tỉnh 5 phút
        int actualSleep = Math.max(0, (int) durationMin - penalty);
        session.setActualSleepMinutes(actualSleep);

        int quality = 100;
        if (session.getTargetSleepMinutes() != null && session.getTargetSleepMinutes() > 0) {
            double ratio = (double) actualSleep / session.getTargetSleepMinutes();
            quality = (int) Math.min(100, Math.max(10, ratio * 100)); // MAX 100 đ, MIN 10 đ
        }
        session.setSleepQuality(quality);

        SleepSession saved = sleepSessionRepository.save(session);

        // Cập nhật lên DailyHealth
        String dateString = now.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        DailyHealth dh = healthDataService.getDailyHealth(session.getUserId(), dateString);
        
        // Cộng dồn thời gian ngủ thực tế
        DailyHealth.Metrics metrics = dh.getMetrics();
        if (metrics == null) {
            metrics = new DailyHealth.Metrics();
            dh.setMetrics(metrics);
        }
        int currentSleep = metrics.getSleepMinutes() != null ? metrics.getSleepMinutes() : 0;
        metrics.setSleepMinutes(currentSleep + actualSleep);
        
        dailyHealthRepository.save(dh);

        return saved;
    }
}
