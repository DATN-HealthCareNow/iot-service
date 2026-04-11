package com.healthcarenow.iot.service;

import com.healthcarenow.iot.entity.DailyHealth;
import com.healthcarenow.iot.repository.DailyHealthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthDataService {

    private final DailyHealthRepository repository;

    private DailyHealth.Metrics buildZeroMetrics() {
        return DailyHealth.Metrics.builder()
                .steps(0.0)
                .exerciseMinutes(0)
                .googleExerciseMinutes(0)
                .activeCalories(0)
                .restingCalories(0)
                .sleepMinutes(0)
                .heartRate(0)
                .restingHeartRate(0)
                .distanceMeters(0.0)
                .build();
    }

    // 1. UPDATE/UPSERT TỪ MOBILE
    public DailyHealth upsertHealthData(DailyHealth payload) {
        log.info("[SYNC] Received health sync request | userId={}, date={}, source={}", 
                payload.getUserId(), payload.getDateString(), payload.getSource());
        log.debug("[SYNC] Payload metrics: {}", payload.getMetrics());
        
        if (payload.getMetrics() == null) {
            log.warn("[SYNC] ⚠️ Metrics is NULL! This will cause empty data in DB");
        } else {
            log.info("[SYNC] Metrics details: steps={}, activeCalories={}, exerciseMinutes={}, sleepMinutes={}", 
                    payload.getMetrics().getSteps(),
                    payload.getMetrics().getActiveCalories(),
                    payload.getMetrics().getExerciseMinutes(),
                    payload.getMetrics().getSleepMinutes());
        }
        
        return repository.findByUserIdAndDateString(payload.getUserId(), payload.getDateString())
                .map(existing -> {
                    log.info("[SYNC] Found existing record, merging data");
                    // Update đè dữ liệu
                    existing.setRawDate(payload.getRawDate());
                    existing.setDateStringLocal(payload.getDateStringLocal() != null ? payload.getDateStringLocal() : existing.getDateStringLocal());
                    existing.setSource(payload.getSource() != null ? payload.getSource() : "DanhK");
                    
                    if (payload.getMetrics() != null) {
                        DailyHealth.Metrics eMetrics = existing.getMetrics();
                        DailyHealth.Metrics pMetrics = payload.getMetrics();
                        
                        if (eMetrics == null) {
                            eMetrics = new DailyHealth.Metrics();
                            existing.setMetrics(eMetrics);
                            log.info("[SYNC] Created new metrics object");
                        }
                        
                        eMetrics.setSteps(pMetrics.getSteps() != null ? pMetrics.getSteps() : eMetrics.getSteps());
                        eMetrics.setExerciseMinutes(pMetrics.getExerciseMinutes() != null ? pMetrics.getExerciseMinutes() : eMetrics.getExerciseMinutes());
                        eMetrics.setGoogleExerciseMinutes(pMetrics.getGoogleExerciseMinutes() != null ? pMetrics.getGoogleExerciseMinutes() : eMetrics.getGoogleExerciseMinutes());
                        eMetrics.setActiveCalories(pMetrics.getActiveCalories() != null ? pMetrics.getActiveCalories() : eMetrics.getActiveCalories());
                        eMetrics.setRestingCalories(pMetrics.getRestingCalories() != null ? pMetrics.getRestingCalories() : eMetrics.getRestingCalories());
                        eMetrics.setSleepMinutes(pMetrics.getSleepMinutes() != null ? pMetrics.getSleepMinutes() : eMetrics.getSleepMinutes());
                        eMetrics.setHeartRate(pMetrics.getHeartRate() != null ? pMetrics.getHeartRate() : eMetrics.getHeartRate());
                        eMetrics.setRestingHeartRate(pMetrics.getRestingHeartRate() != null ? pMetrics.getRestingHeartRate() : eMetrics.getRestingHeartRate());
                        eMetrics.setDistanceMeters(pMetrics.getDistanceMeters() != null ? pMetrics.getDistanceMeters() : eMetrics.getDistanceMeters());
                        
                        log.info("[SYNC] Merged metrics: steps={}, googleExerciseMinutes={}, activeCalories={}, heartRate={}", 
                                eMetrics.getSteps(), 
                                eMetrics.getGoogleExerciseMinutes(),
                                eMetrics.getActiveCalories(),
                                eMetrics.getHeartRate());
                    }
                    
                    DailyHealth saved = repository.save(existing);
                    log.info("[SYNC] ✅ Successfully merged and saved");
                    return saved;
                })
                .orElseGet(() -> {
                    log.info("[SYNC] No existing record, creating new one");
                    if (payload.getMetrics() == null) {
                        log.warn("[SYNC] ⚠️ Creating record with NULL metrics!");
                        payload.setMetrics(new DailyHealth.Metrics());
                    }
                    if (payload.getDateStringLocal() == null || payload.getDateStringLocal().isBlank()) {
                        payload.setDateStringLocal(payload.getDateString());
                    }
                    DailyHealth saved = repository.save(payload);
                    log.info("[SYNC] ✅ Successfully created new record with id={}", saved.getId());
                    return saved;
                }); // Tạo mới nếu không tồn tại
    }

    // 2. SCRIPT SEED DATA (Tạo dữ liệu 7 ngày qua)
    public String seedHealthData(String userId, int daysToSeed) {
        Random random = new Random();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter rawDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");
        ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");

        for (int i = daysToSeed; i >= 0; i--) {
            ZonedDateTime targetDate = ZonedDateTime.now(zoneId).minusDays(i).withHour(0).withMinute(0).withSecond(0).withNano(0);
            
            double mockSteps = 5000 + random.nextInt(3501); 
            int mockExercise = 20 + random.nextInt(41);     
            int mockCalories = (int) (mockExercise * 7.5);  
            int mockSleep = 300 + random.nextInt(181);      
            int mockWater = 500 + random.nextInt(2001);     
            int mockHeartRate = 60 + random.nextInt(41);    

            DailyHealth.Metrics metrics = DailyHealth.Metrics.builder()
                    .steps(mockSteps)
                    .exerciseMinutes(mockExercise)
                    .googleExerciseMinutes(mockExercise)
                    .activeCalories(mockCalories)
                    .restingCalories(1500)
                    .sleepMinutes(mockSleep)
                    .heartRate(mockHeartRate)
                    .restingHeartRate(Math.max(45, mockHeartRate - 12))
                    .distanceMeters(mockSteps * 0.75)
                    .build();

            DailyHealth record = DailyHealth.builder()
                    .userId(userId)
                    .dateString(targetDate.format(dateFormatter))
                    .rawDate(targetDate.format(rawDateFormatter))
                    .source("DanhK")
                    .metrics(metrics)
                    .build();

            upsertHealthData(record);
        }

        return "✅ Đã tạo/update thành công " + (daysToSeed + 1) + " ngày dữ liệu mẫu cho User " + userId;
    }

        // 3. GET DỮ LIỆU NGÀY HIỆN TẠI
        // Nếu user chưa connect/sync health source thì trả về metrics = 0.
    public DailyHealth getDailyHealth(String userId, String dateString) {
        if (dateString == null || dateString.isBlank()) {
            dateString = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        
        final String finalDate = dateString;
        return repository.findByUserIdAndDateString(userId, finalDate)
            .map(existing -> {
                if ("Mock_API_HealthKit_Fake".equals(existing.getSource())) {
                existing.setSource("NO_HEALTH_SOURCE");
                existing.setMetrics(buildZeroMetrics());
                return repository.save(existing);
                }
                return existing;
            })
                .orElseGet(() -> {
                return DailyHealth.builder()
                            .userId(userId)
                            .dateString(finalDate)
                            .dateStringLocal(finalDate)
                            .rawDate(ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z")))
                    .source("NO_HEALTH_SOURCE")
                    .metrics(buildZeroMetrics())
                            .build();
                });
    }

    // 4. GET DỮ LIỆU LOG RANGE (TỪ START DATE ĐẾN END DATE)
    public List<DailyHealth> getHealthReport(String userId, String startDate, String endDate) {
        String normalizedStart = normalizeDate(startDate);
        String normalizedEnd = normalizeDate(endDate);

        if (normalizedStart.compareTo(normalizedEnd) > 0) {
            String tmp = normalizedStart;
            normalizedStart = normalizedEnd;
            normalizedEnd = tmp;
        }

        try {
            return repository.findByUserIdAndDateStringGreaterThanEqualAndDateStringLessThanEqualOrderByDateStringAsc(
                    userId,
                    normalizedStart,
                    normalizedEnd
            );
        } catch (Exception ex) {
            log.error("[REPORT] Failed to fetch report | userId={}, startDate={}, endDate={}, normalizedStart={}, normalizedEnd={}",
                    userId, startDate, endDate, normalizedStart, normalizedEnd, ex);
            return List.of();
        }
    }

    private String normalizeDate(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) {
            return ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }

        String value = rawDate.trim();
        if (value.length() >= 10 && value.charAt(4) == '-' && value.charAt(7) == '-') {
            return value.substring(0, 10);
        }

        try {
            return LocalDate.parse(value).format(DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ignored) {
            // Try with timezone-aware formats below.
        }

        try {
            return OffsetDateTime.parse(value).toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ignored) {
            // Try with Instant below.
        }

        try {
            return Instant.parse(value)
                    .atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                    .toLocalDate()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date format. Expected yyyy-MM-dd or ISO timestamp: " + rawDate, ex);
        }
    }
}
