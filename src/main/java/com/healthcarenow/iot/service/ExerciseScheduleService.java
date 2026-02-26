package com.healthcarenow.iot.service;

import com.healthcarenow.iot.common.context.UserContextHolder;
import com.healthcarenow.iot.dto.ScheduleCreateRequest;
import com.healthcarenow.iot.entity.ExerciseSchedule;
import com.healthcarenow.iot.repository.ExerciseScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExerciseScheduleService {

  private final ExerciseScheduleRepository scheduleRepository;

  public ExerciseSchedule createSchedule(ScheduleCreateRequest request) {
    String userId = UserContextHolder.getUserId();

    ExerciseSchedule schedule = ExerciseSchedule.builder()
        .userId(userId)
        .title(request.getTitle())
        .scheduleType(request.getScheduleType())
        .startDate(request.getStartDate())
        .reminderEnabled(request.isReminderEnabled())
        .recurrenceConfig(request.getRecurrenceConfig())
        .build();

    return scheduleRepository.save(schedule);
  }

  public List<ExerciseSchedule> getUpcomingSchedules() {
    String userId = UserContextHolder.getUserId();
    // Return schedules that start after Now
    return scheduleRepository.findByUserIdAndStartDateAfterOrderByStartDateAsc(userId, Instant.now());
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
}
