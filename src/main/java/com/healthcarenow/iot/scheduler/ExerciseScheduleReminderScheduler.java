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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExerciseScheduleReminderScheduler {

  private final ExerciseScheduleRepository scheduleRepository;
  private final RabbitTemplate rabbitTemplate;
  private final com.healthcarenow.iot.service.ExerciseScheduleService exerciseScheduleService;
  private final Set<String> dispatchedMedicationKeys = ConcurrentHashMap.newKeySet();

  // Run every minute
  @Scheduled(
      cron = "0 * * * * *",
      zone = "Asia/Ho_Chi_Minh"
  )
  public void triggerReminders() {
    log.info("[ExerciseReminder] Checking for scheduled exercises...");

    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
    String currentTime = String.format("%02d:%02d", now.getHour(), now.getMinute());
    String today = now.toLocalDate().toString();

    dispatchedMedicationKeys.removeIf(key -> !key.startsWith(today + ":"));

    // Fetch all schedules. In production we might want to query selectively.
    List<ExerciseSchedule> allSchedules = scheduleRepository.findAll();

    for (ExerciseSchedule schedule : allSchedules) {
      if (!schedule.isReminderEnabled()) {
        continue;
      }

        boolean shouldRemind = false;
        boolean isMedication = schedule.getTitle() != null &&
          (schedule.getTitle().startsWith("Medication") || schedule.getTitle().startsWith("Uống thuốc"));

        if (isMedication) {
          // It's a medication schedule, use the medications array
          if (schedule.getMedications() != null) {
              for (Object medObj : schedule.getMedications()) {
                  if (medObj instanceof Map) {
                      Map<String, Object> medMap = (Map<String, Object>) medObj;
                      Object schedulesObj = medMap.get("schedules");
                      if (schedulesObj instanceof List) {
                          List<Map<String, Object>> medSchedules = (List<Map<String, Object>>) schedulesObj;
                          for (Map<String, Object> medSched : medSchedules) {
                              if (currentTime.equals(medSched.get("time"))) {
                                  shouldRemind = true;
                                  break;
                              }
                          }
                      }
                  }
                  if (shouldRemind) break;
              }
          }
      } else if (schedule.getScheduleType() == ExerciseSchedule.ScheduleType.RECURRING && schedule.getRecurrenceConfig() != null) {
        ExerciseSchedule.RecurrenceConfig config = schedule.getRecurrenceConfig();
        boolean timeMatch = false;
        
        if (config.getReminderTimes() != null && config.getReminderTimes().contains(currentTime)) {
            timeMatch = true;
        } else if (config.getReminderTime() != null && config.getReminderTime().equals(currentTime)) {
            timeMatch = true;
        }

        if (timeMatch) {
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
        if (isMedication) {
          String dedupeKey = today + ":" + currentTime + ":" + schedule.getUserId();
          if (!dispatchedMedicationKeys.add(dedupeKey)) {
            continue;
          }
        }
        sendNotification(schedule);
        
        // Disable one-time schedules after reminding
        if (schedule.getScheduleType() == ExerciseSchedule.ScheduleType.ONE_TIME) {
          schedule.setReminderEnabled(false);
          scheduleRepository.save(schedule);
          
          if (isMedication) {
              exerciseScheduleService.cleanupForbiddenFoodsBySource(schedule.getUserId(), schedule.getSourceId());
          }
        }
      }
    }
  }

  private void sendNotification(ExerciseSchedule schedule) {
    Map<String, Object> payload = new HashMap<>();
    Map<String, Object> event = new HashMap<>();
    boolean isMedication = schedule.getTitle() != null &&
      (schedule.getTitle().startsWith("Medication") || schedule.getTitle().startsWith("Uống thuốc"));
    
    if (isMedication) {
      String diagnosis = schedule.getDiagnosis();
      if (diagnosis == null || diagnosis.isBlank()) {
        diagnosis = "đơn thuốc của bạn";
      }
      payload.put("title", "Nhắc nhẹ: đến giờ uống thuốc");
      payload.put("body", "Nhắc nhỏ: đã đến giờ uống thuốc cho chẩn đoán " + diagnosis + ". Chúc bạn mau khỏe!");
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
