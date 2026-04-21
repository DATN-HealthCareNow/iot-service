package com.healthcarenow.iot.scheduler;

import com.healthcarenow.iot.entity.ExerciseSchedule;
import com.healthcarenow.iot.repository.ExerciseScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExerciseScheduleReminderScheduler {

  private final ExerciseScheduleRepository scheduleRepository;
  private final RabbitTemplate rabbitTemplate;
  private final com.healthcarenow.iot.service.ExerciseScheduleService exerciseScheduleService;

  // Run every minute
  @Scheduled(
      cron = "0 * * * * *",
      zone = "Asia/Ho_Chi_Minh"
  )
  public void triggerReminders() {
    log.info("[ExerciseReminder] Checking for scheduled exercises...");

    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
    String currentTime = String.format("%02d:%02d", now.getHour(), now.getMinute());

    // Fetch all schedules. In production we might want to query selectively.
    List<ExerciseSchedule> allSchedules = scheduleRepository.findAll();

    for (ExerciseSchedule schedule : allSchedules) {
      if (!schedule.isReminderEnabled()) {
        continue;
      }

      boolean shouldRemind = false;

      if (schedule.getScheduleType() == ExerciseSchedule.ScheduleType.RECURRING && schedule.getRecurrenceConfig() != null) {
        ExerciseSchedule.RecurrenceConfig config = schedule.getRecurrenceConfig();
        if (config.getReminderTime() != null && config.getReminderTime().equals(currentTime)) {
          if (matchesRepeatDay(config.getRepeatDays(), now.getDayOfWeek().getValue())) {
            shouldRemind = true;
          }
        }
      } else if (schedule.getScheduleType() == ExerciseSchedule.ScheduleType.ONE_TIME && schedule.getStartDate() != null) {
        // ONE_TIME checks if startDate is within this minute
        ZonedDateTime startTime = schedule.getStartDate().atZone(ZoneId.of("Asia/Ho_Chi_Minh"));
        if (startTime.getYear() == now.getYear() && 
            startTime.getDayOfYear() == now.getDayOfYear() &&
            startTime.getHour() == now.getHour() && 
            startTime.getMinute() == now.getMinute()) {
          shouldRemind = true;
        }
      }

      if (shouldRemind) {
        sendNotification(schedule);
        
        // Disable one-time schedules after reminding
        if (schedule.getScheduleType() == ExerciseSchedule.ScheduleType.ONE_TIME) {
          schedule.setReminderEnabled(false);
          scheduleRepository.save(schedule);
          
          if (schedule.getTitle() != null && schedule.getTitle().startsWith("Uống thuốc")) {
              exerciseScheduleService.cleanupForbiddenFoodsBySource(schedule.getUserId(), schedule.getSourceId());
          }
        }
      }
    }
  }

  private void sendNotification(ExerciseSchedule schedule) {
    Map<String, Object> payload = new HashMap<>();
    Map<String, Object> event = new HashMap<>();
    boolean isMedication = schedule.getTitle() != null && schedule.getTitle().startsWith("Uống thuốc");
    
    if (isMedication) {
        payload.put("title", "Đã đến giờ uống thuốc!");
        payload.put("body", "Bạn có liều thuốc cần uống: " + schedule.getTitle());
        event.put("eventType", "MEDICATION_REMINDER");
    } else {
        payload.put("title", "Đã đến giờ tập luyện!");
        payload.put("body", "Bạn có lịch tập: " + schedule.getTitle() + ". Hãy chuẩn bị và bắt đầu ngay nhé.");
        event.put("eventType", "ACTIVITY_REMINDER");
    }
    
    payload.put("language", "vi");

    event.put("userId", schedule.getUserId());
    event.put("priority", "HIGH");
    event.put("payload", payload);

    try {
      rabbitTemplate.convertAndSend("healthcare.events", "notification.queue", event);
      log.info("[ExerciseReminder] Sent notification for schedule {} to user {}", schedule.getId(), schedule.getUserId());
    } catch (Exception e) {
      log.error("[ExerciseReminder] Failed to send notification via RabbitMQ for schedule " + schedule.getId(), e);
    }
  }

  // Maps Java DayOfWeek (Monday=1, Sunday=7) to Calendar/Android format (Sunday=1, Monday=2...)
  private int mapJavaDayToCalendar(int javaDay) {
    if (javaDay == 7) return 1; // Sunday
    return javaDay + 1; // Mon -> 2
  }

  private boolean matchesRepeatDay(List<Integer> repeatDays, int javaDayOfWeek) {
    if (repeatDays == null || repeatDays.isEmpty()) {
      return false;
    }

    int calendarDay = mapJavaDayToCalendar(javaDayOfWeek);
    int zeroBasedDay = javaDayOfWeek % 7;

    return repeatDays.contains(javaDayOfWeek)
        || repeatDays.contains(calendarDay)
        || repeatDays.contains(zeroBasedDay);
  }
}
