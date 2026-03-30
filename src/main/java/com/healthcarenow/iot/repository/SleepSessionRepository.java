package com.healthcarenow.iot.repository;

import com.healthcarenow.iot.entity.SleepSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SleepSessionRepository extends MongoRepository<SleepSession, String> {
    Page<SleepSession> findByUserId(String userId, Pageable pageable);
    Optional<SleepSession> findFirstByUserIdAndStatusOrderByStartAtDesc(String userId, SleepSession.Status status);
}
