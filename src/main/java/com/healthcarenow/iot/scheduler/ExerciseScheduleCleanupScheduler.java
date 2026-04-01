package com.healthcarenow.iot.scheduler;

import com.healthcarenow.iot.service.ExerciseScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExerciseScheduleCleanupScheduler {

  private final ExerciseScheduleService exerciseScheduleService;

  // Default: 05:00 every Monday (Vietnam timezone)
  @Scheduled(
      cron = "${schedule.cleanup.cron:0 0 5 ? * MON}",
      zone = "${schedule.cleanup.zone:Asia/Ho_Chi_Minh}"
  )
  public void cleanupPreviousWeekSchedules() {
    long deleted = exerciseScheduleService.cleanupPreviousWeekSchedules();
    log.info("[SCHEDULE_CLEANUP] Weekly cleanup finished. deleted={}", deleted);
  }
}
