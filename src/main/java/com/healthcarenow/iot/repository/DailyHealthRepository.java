package com.healthcarenow.iot.repository;

import com.healthcarenow.iot.entity.DailyHealth;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DailyHealthRepository extends MongoRepository<DailyHealth, String> {
    Optional<DailyHealth> findByUserIdAndDateString(String userId, String dateString);
}
