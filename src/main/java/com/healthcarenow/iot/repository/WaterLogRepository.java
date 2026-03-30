package com.healthcarenow.iot.repository;

import com.healthcarenow.iot.entity.WaterLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WaterLogRepository extends MongoRepository<WaterLog, String> {
    List<WaterLog> findByUserIdAndDateString(String userId, String dateString);
}