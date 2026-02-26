package com.healthcarenow.iot.repository;

import com.healthcarenow.iot.entity.DailyStep;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyStepRepository extends MongoRepository<DailyStep, String> {
  Optional<DailyStep> findByUserIdAndDate(String userId, Instant date);

  List<DailyStep> findByUserIdAndDateBetween(String userId, Instant startDate, Instant endDate);
}
