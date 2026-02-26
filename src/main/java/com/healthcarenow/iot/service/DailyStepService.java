package com.healthcarenow.iot.service;

import com.healthcarenow.iot.common.context.UserContextHolder;
import com.healthcarenow.iot.dto.DailyStepSyncRequest;
import com.healthcarenow.iot.entity.DailyStep;
import com.healthcarenow.iot.repository.DailyStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyStepService {

  private final DailyStepRepository dailyStepRepository;

  public DailyStep syncSteps(DailyStepSyncRequest request) {
    String userId = UserContextHolder.getUserId();

    // Upsert Logic: Check if exists for the user and date
    Optional<DailyStep> existingOpt = dailyStepRepository.findByUserIdAndDate(userId, request.getDate());

    if (existingOpt.isPresent()) {
      DailyStep existing = existingOpt.get();
      // Merge logic depending on business rules. We could overwrite or add.
      // Let's assume we overwrite with the latest sync from source
      existing.setSteps(request.getSteps());
      existing.setSource(request.getSource());
      return dailyStepRepository.save(existing);
    } else {
      DailyStep newStep = DailyStep.builder()
          .userId(userId)
          .date(request.getDate())
          .steps(request.getSteps())
          .source(request.getSource())
          .build();
      try {
        return dailyStepRepository.save(newStep);
      } catch (DuplicateKeyException e) {
        log.warn("Concurrent insert for DailyStep user: {}, date: {}. Handled by GlobalExceptionHandler.", userId,
            request.getDate());
        throw e; // Let GlobalExceptionHandler handle the 409 Conflict
      }
    }
  }

  public List<DailyStep> getStepReport(Instant startDate, Instant endDate) {
    String userId = UserContextHolder.getUserId();
    return dailyStepRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
  }
}
