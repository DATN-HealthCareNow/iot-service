package com.healthcarenow.iot.controller;

import com.healthcarenow.iot.dto.DailyStepSyncRequest;
import com.healthcarenow.iot.entity.DailyStep;
import com.healthcarenow.iot.service.DailyStepService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/steps")
@RequiredArgsConstructor
public class DailyStepController {

  private final DailyStepService dailyStepService;

  @PostMapping("/sync")
  public ResponseEntity<DailyStep> syncSteps(@Valid @RequestBody DailyStepSyncRequest request) {
    return ResponseEntity.ok(dailyStepService.syncSteps(request));
  }

  @GetMapping("/report")
  public ResponseEntity<List<DailyStep>> getReport(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {
    return ResponseEntity.ok(dailyStepService.getStepReport(startDate, endDate));
  }
}
