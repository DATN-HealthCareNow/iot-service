package com.healthcarenow.iot.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "weather_logs")
// Đảm bảo 1 user chỉ có 1 bản ghi thời tiết cho 1 ngày cụ thể
@CompoundIndex(name = "user_date_idx", def = "{'userId': 1, 'dateString': 1}", unique = true)
public class WeatherLog {

    @Id
    private String id;

    private String userId;

    // Ngày định dạng YYYY-MM-DD
    private String dateString;

    // Vị trí (VD: HCMC, Hanoi, ...)
    private String location;

    // Nhiệt độ hiện tại
    private Double temperature;

    // Mô tả thời tiết (Sunny, Rainy, Cloudy,...)
    private String conditionDesc;

    // Mã icon thời tiết để map bên Mobile App
    private String iconCode;

    // Tạo TTL Index (Time-To-Live): Tự động xóa Document này sau 7 ngày (7 * 24 * 60 * 60 = 604800 giây)
    @Indexed(expireAfterSeconds = 604800)
    @CreatedDate
    private Instant createdAt;
}
