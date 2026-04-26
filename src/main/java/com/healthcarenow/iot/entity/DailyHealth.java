package com.healthcarenow.iot.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "daily_health_metrics")
@CompoundIndex(name = "user_date_idx", def = "{'userId': 1, 'dateString': 1}", unique = true)
public class DailyHealth {
    @Id
    private String id;
    @NotBlank(message = "userId must not be blank")
    private String userId;
    
    // Ngày định dạng YYYY-MM-DD
    private String dateString;

    // Ngày local (Asia/Ho_Chi_Minh) để thống nhất truy vấn theo ngày
    private String dateStringLocal;
    
    // Timestamp gốc chuyển từ Mobile lên
    private String rawDate;
    
    @Builder.Default
    private String source = "DanhK";
    
    private Metrics metrics;

    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metrics {
        // Activity fields: 0 là giá trị hợp lệ (user không đi bộ)
        @Builder.Default private Double steps = 0.0;
        @Builder.Default private Integer exerciseMinutes = 0;
        @Builder.Default private Integer googleExerciseMinutes = 0;
        @Builder.Default private Integer activeCalories = 0;
        @Builder.Default private Integer totalCalories = 0;
        @Builder.Default private Double distanceMeters = 0.0;

        // Wearable fields: null = không có thiết bị / không ghi nhận được
        // KHÔNG dùng @Builder.Default để tránh ghi đè null → 0
        private Integer sleepMinutes;       // null = no wearable
        private Integer heartRate;          // null = no wearable
        private Integer restingHeartRate;   // null = no wearable
    }
}
