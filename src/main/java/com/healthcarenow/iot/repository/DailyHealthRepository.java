package com.healthcarenow.iot.repository;

import com.healthcarenow.iot.entity.DailyHealth;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface DailyHealthRepository extends MongoRepository<DailyHealth, String> {
    Optional<DailyHealth> findByUserIdAndDateString(String userId, String dateString);
    List<DailyHealth> findTop7ByUserIdOrderByDateStringDesc(String userId);
    
    // Range query: dateString lexically sortable (YYYY-MM-DD format)
    @Query("{ 'userId': ?0, 'dateString': { $gte: ?1, $lte: ?2 } }")
    List<DailyHealth> findByUserIdAndDateStringGreaterThanEqualAndDateStringLessThanEqualOrderByDateStringAsc(
            String userId, String startDate, String endDate);
}

