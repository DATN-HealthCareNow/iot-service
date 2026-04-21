package com.healthcarenow.iot.service;

import com.healthcarenow.iot.common.context.UserContextHolder;
import com.healthcarenow.iot.dto.ScheduleCreateRequest;
import com.healthcarenow.iot.entity.ExerciseSchedule;
import com.healthcarenow.iot.repository.ExerciseScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExerciseScheduleService {

  private final ExerciseScheduleRepository scheduleRepository;
  private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;
  private final com.healthcarenow.iot.client.CoreServiceClient coreServiceClient;
  private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

  public ExerciseSchedule createSchedule(ScheduleCreateRequest request) {
    String userId = UserContextHolder.getUserId();

    ExerciseSchedule schedule = ExerciseSchedule.builder()
        .userId(userId)
        .title(request.getTitle())
        .scheduleType(request.getScheduleType())
        .startDate(request.getStartDate())
        .reminderEnabled(request.isReminderEnabled())
        .sourceId(request.getSourceId())
        .recurrenceConfig(request.getRecurrenceConfig())
        .build();

    return scheduleRepository.save(schedule);
  }

  public List<ExerciseSchedule> getUpcomingSchedules() {
    String userId = UserContextHolder.getUserId();
    // Return all schedules for the user. Frontend handles today/recurring logic.
    return scheduleRepository.findByUserId(userId);
  }

  public ExerciseSchedule updateRecurrence(String id, ExerciseSchedule.RecurrenceConfig config) {
    ExerciseSchedule schedule = scheduleRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));

    schedule.setRecurrenceConfig(config);

    // Auto convert to recurring if single config is passed? Usually client sends
    // correct Type.
    if (config != null) {
      schedule.setScheduleType(ExerciseSchedule.ScheduleType.RECURRING);
    } else {
      schedule.setScheduleType(ExerciseSchedule.ScheduleType.ONE_TIME);
    }

    return scheduleRepository.save(schedule);
  }

  public ExerciseSchedule toggleSchedule(String id) {
    String userId = UserContextHolder.getUserId();
    ExerciseSchedule schedule = scheduleRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));
    
    // Check if it belongs to the user
    if (!userId.equals(schedule.getUserId())) {
      throw new IllegalArgumentException("Schedule not found or unauthorized");
    }

    schedule.setReminderEnabled(!schedule.isReminderEnabled());
    return scheduleRepository.save(schedule);
  }

  public void deleteSchedule(String id) {
    String userId = UserContextHolder.getUserId();
    ExerciseSchedule schedule = scheduleRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));
        
    // Check if it belongs to the user
    if (!userId.equals(schedule.getUserId())) {
      throw new IllegalArgumentException("Schedule not found or unauthorized");
    }

    scheduleRepository.deleteById(id);
  cleanupForbiddenFoodsBySource(userId, schedule.getSourceId());
  }

  public void deleteSchedules(List<String> ids) {
    if (ids == null || ids.isEmpty()) return;
    String userId = UserContextHolder.getUserId();
  java.util.Set<String> sourceIdsToCheck = new java.util.HashSet<>();
    for (String id : ids) {
        ExerciseSchedule schedule = scheduleRepository.findById(id).orElse(null);
        if (schedule != null && userId.equals(schedule.getUserId())) {
      if (schedule.getSourceId() != null) {
        sourceIdsToCheck.add(schedule.getSourceId());
      }
            scheduleRepository.deleteById(id);
        }
    }

  for (String sourceId : sourceIdsToCheck) {
    cleanupForbiddenFoodsBySource(userId, sourceId);
  }
  }

  public void cleanupForbiddenFoodsBySource(String userId, String sourceId) {
    if (sourceId == null || sourceId.isBlank()) {
      return;
    }

    List<ExerciseSchedule> schedules = scheduleRepository.findByUserId(userId);

    boolean hasActiveScheduleForSource = schedules.stream()
      .filter(s -> sourceId.equals(s.getSourceId()))
      .anyMatch(ExerciseSchedule::isReminderEnabled);

    if (hasActiveScheduleForSource) {
      return;
    }

    log.info("[ExerciseScheduleService] No more active schedules for user {} and source {}, sending RabbitMQ cleanup event", userId, sourceId);

    java.util.Map<String, Object> event = new java.util.HashMap<>();
    event.put("userId", userId);
    event.put("sourceId", sourceId);

    try {
      rabbitTemplate.convertAndSend("healthcare.events", "medication.cleanup.routing.key", event);
    } catch (Exception e) {
      log.error("Failed to send cleanup event", e);
      coreServiceClient.removeForbiddenFoodsBySource(userId, sourceId);
    }
  }

  public long cleanupPreviousWeekSchedules() {
    long deleted = scheduleRepository.count();
    scheduleRepository.deleteAll();
    
    log.info("[SCHEDULE_CLEANUP] Wiped out ALL {} exercise schedules for the new week.", deleted);
    return deleted;
  }
}
