package com.healthcarenow.iot.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "water_logs")
public class WaterLog {
    @Id
    private String id;
    private String userId;
    private Integer amountMl;
    private String adjustmentReason;
    private String dateString;
    
    @CreatedDate
    @Indexed(expireAfterSeconds = 604800) // TTL 7 ngày (7 * 24 * 60 * 60 s)
    private LocalDateTime createdAt;
}