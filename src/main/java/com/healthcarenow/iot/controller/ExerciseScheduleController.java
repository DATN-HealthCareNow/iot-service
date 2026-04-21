package com.healthcarenow.iot.controller;

import com.healthcarenow.iot.dto.ScheduleCreateRequest;
import com.healthcarenow.iot.entity.ExerciseSchedule;
import com.healthcarenow.iot.service.ExerciseScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class ExerciseScheduleController {

  private final ExerciseScheduleService scheduleService;

  @PostMapping
  public ResponseEntity<ExerciseSchedule> createSchedule(@Valid @RequestBody ScheduleCreateRequest request) {
    return new ResponseEntity<>(scheduleService.createSchedule(request), HttpStatus.CREATED);
  }

  @GetMapping("/upcoming")
  public ResponseEntity<List<ExerciseSchedule>> getUpcoming() {
    return ResponseEntity.ok(scheduleService.getUpcomingSchedules());
  }

  @PutMapping("/{id}")
  public ResponseEntity<ExerciseSchedule> updateRecurrence(
      @PathVariable String id,
      @RequestBody ExerciseSchedule.RecurrenceConfig config) {
    return ResponseEntity.ok(scheduleService.updateRecurrence(id, config));
  }

  @PutMapping("/{id}/toggle")
  public ResponseEntity<ExerciseSchedule> toggleSchedule(@PathVariable String id) {
    return ResponseEntity.ok(scheduleService.toggleSchedule(id));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteSchedule(@PathVariable String id) {
    scheduleService.deleteSchedule(id);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/batch")
  public ResponseEntity<Void> deleteSchedules(@RequestBody List<String> ids) {
    scheduleService.deleteSchedules(ids);
    return ResponseEntity.noContent().build();
  }
}
