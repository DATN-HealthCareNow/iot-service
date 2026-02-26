package com.healthcarenow.iot.repository;

import com.healthcarenow.iot.entity.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityRepository extends MongoRepository<Activity, String> {
  Page<Activity> findByUserId(String userId, Pageable pageable);
}
