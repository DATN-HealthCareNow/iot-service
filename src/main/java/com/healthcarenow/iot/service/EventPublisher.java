package com.healthcarenow.iot.service;

import com.healthcarenow.iot.common.event.ActivityCompletedEvent;
import com.healthcarenow.iot.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

  private final RabbitTemplate rabbitTemplate;

  @Async
  public void publishActivityCompleted(ActivityCompletedEvent event) {
    log.info("Publishing ActivityCompletedEvent for activityId: {}", event.getActivityId());
    // Routing key pattern assumes receiver listens to 'iot.activity.completed'
    rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "iot.activity.completed", event);
  }

  @Async
  public void publishNotification(com.healthcarenow.iot.dto.NotificationEvent event) {
    log.info("Sending notification event from IoT: {}", event.getEventType());
    rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "notification.event", event);
  }
}
