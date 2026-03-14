package com.healthcarenow.iot.service;

import com.healthcarenow.iot.entity.DailyHealth;
import com.healthcarenow.iot.repository.DailyHealthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class HealthDataService {

    private final DailyHealthRepository repository;

    // 1. UPDATE/UPSERT TỪ MOBILE
    public DailyHealth upsertHealthData(DailyHealth payload) {
        return repository.findByUserIdAndDateString(payload.getUserId(), payload.getDateString())
                .map(existing -> {
                    // Update đè dữ liệu
                    existing.setRawDate(payload.getRawDate());
                    existing.setSource(payload.getSource() != null ? payload.getSource() : "DanhK");
                    
                    if (payload.getMetrics() != null) {
                        DailyHealth.Metrics eMetrics = existing.getMetrics();
                        DailyHealth.Metrics pMetrics = payload.getMetrics();
                        
                        if (eMetrics == null) {
                            eMetrics = new DailyHealth.Metrics();
                            existing.setMetrics(eMetrics);
                        }
                        
                        eMetrics.setSteps(pMetrics.getSteps() != null ? pMetrics.getSteps() : eMetrics.getSteps());
                        eMetrics.setExerciseMinutes(pMetrics.getExerciseMinutes() != null ? pMetrics.getExerciseMinutes() : eMetrics.getExerciseMinutes());
                        eMetrics.setActiveCalories(pMetrics.getActiveCalories() != null ? pMetrics.getActiveCalories() : eMetrics.getActiveCalories());
                        eMetrics.setRestingCalories(pMetrics.getRestingCalories() != null ? pMetrics.getRestingCalories() : eMetrics.getRestingCalories());
                        eMetrics.setSleepMinutes(pMetrics.getSleepMinutes() != null ? pMetrics.getSleepMinutes() : eMetrics.getSleepMinutes());
                        eMetrics.setWaterConsumedMl(pMetrics.getWaterConsumedMl() != null ? pMetrics.getWaterConsumedMl() : eMetrics.getWaterConsumedMl());
                    }
                    return repository.save(existing);
                })
                .orElseGet(() -> {
                    if (payload.getMetrics() == null) {
                        payload.setMetrics(new DailyHealth.Metrics());
                    }
                    return repository.save(payload);
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

            DailyHealth.Metrics metrics = DailyHealth.Metrics.builder()
                    .steps(mockSteps)
                    .exerciseMinutes(mockExercise)
                    .activeCalories(mockCalories)
                    .restingCalories(1500)
                    .sleepMinutes(mockSleep)
                    .waterConsumedMl(mockWater)
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
}
