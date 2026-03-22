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

    // 3. GET DỮ LIỆU NGÀY HIỆN TẠI (KÈM CHỨC NĂNG API LẬU FAKE DATA NẾU CHƯA CÓ)
    public DailyHealth getDailyHealth(String userId, String dateString) {
        if (dateString == null || dateString.isBlank()) {
            dateString = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        
        final String finalDate = dateString;
        return repository.findByUserIdAndDateString(userId, finalDate)
                .orElseGet(() -> {
                    // Khởi tạo data giả (API lậu) theo yêu cầu cho tới khi có Native Module (HealthKit thật)
                    Random random = new Random();
                    DailyHealth.Metrics metrics = DailyHealth.Metrics.builder()
                            .steps((double) (2500 + random.nextInt(6000))) // Random 2500 -> 8500 bước
                            .exerciseMinutes(20 + random.nextInt(40))      // Random 20 -> 60 phút
                            .activeCalories(150 + random.nextInt(350))     // Random 150 -> 500 kcal
                            .restingCalories(1400)
                            .sleepMinutes(360 + random.nextInt(180))       // 6 tiếng -> 9 tiếng
                            .waterConsumedMl(1000 + random.nextInt(1500))  // 1 -> 2.5 lít
                            .build();

                    DailyHealth fakeData = DailyHealth.builder()
                            .userId(userId)
                            .dateString(finalDate)
                            .rawDate(ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z")))
                            .source("Mock_API_HealthKit_Fake")
                            .metrics(metrics)
                            .build();
                    
                    // Lưu lại DB để lần gọi GET tiếp theo trong ngày sẽ lấy data đã bị Mock chứ không random nhảy số lại liên tục
                    return repository.save(fakeData);
                });
    }
}
