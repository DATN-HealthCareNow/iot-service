package com.healthcarenow.iot.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@Document(collection = "daily_steps")
@CompoundIndexes({
    @CompoundIndex(name = "user_date_idx", def = "{'userId': 1, 'date': 1}", unique = true)
})
public class DailyStep {

  @Id
  private String id;

  @Indexed
  private String userId;

  private Instant date; // Note: Ensure UTC Timezone handling

  private int steps;
  private double caloriesFromSteps;
  private String source;
  private Instant createdAt;
}
