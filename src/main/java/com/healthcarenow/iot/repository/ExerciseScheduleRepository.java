package com.healthcarenow.iot.repository;

import com.healthcarenow.iot.entity.ExerciseSchedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ExerciseScheduleRepository extends MongoRepository<ExerciseSchedule, String> {
  List<ExerciseSchedule> findByUserIdAndStartDateAfterOrderByStartDateAsc(String userId, Instant date);
}
