package com.healthcarenow.iot.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@Document(collection = "sleep_sessions")
public class SleepSession {

    @Id
    private String id;

    @Indexed
    private String userId;

    private Status status;

    // Thời gian bắt đầu và kết thúc giấc ngủ
    private Instant startAt;
    private Instant endAt;

    // Số phút mục tiêu user muốn ngủ (lấy từ Core)
    private Integer targetSleepMinutes;
    
    // Số phút THỰC TẾ đã ngủ (Sau khi trừ hao tỉnh giấc)
    private Integer actualSleepMinutes;

    // Số lần điện thoại bị lắc mạnh giữa đêm 
    private Integer wakeupsCount;
    
    // Điểm số chất lượng giấc ngủ (1-100)
    private Integer sleepQuality;

    public enum Status {
        ACTIVE, FINISHED
    }
}
