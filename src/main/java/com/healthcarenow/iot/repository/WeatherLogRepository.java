package com.healthcarenow.iot.repository;

import com.healthcarenow.iot.entity.WeatherLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WeatherLogRepository extends MongoRepository<WeatherLog, String> {
    Optional<WeatherLog> findByUserIdAndDateString(String userId, String dateString);
}
