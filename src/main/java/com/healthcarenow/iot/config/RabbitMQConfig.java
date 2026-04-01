package com.healthcarenow.iot.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  public static final String EXCHANGE_NAME = "healthcare.events";
  public static final String DLX_NAME = "healthcare.events.dlx";
  public static final String WATER_LOGGING_QUEUE = "water.logging.queue";
  public static final String WATER_LOGGING_ROUTING_KEY = "water.logging.routing.key";
  public static final String WATER_LOGGING_DLQ = "water.logging.dlq";
  public static final String WATER_LOGGING_DLQ_ROUTING_KEY = "water.logging.dlq.routing.key";

  @Bean
  public TopicExchange healthcareExchange() {
    return new TopicExchange(EXCHANGE_NAME);
  }

  @Bean
  public TopicExchange deadLetterExchange() {
    return new TopicExchange(DLX_NAME);
  }

  @Bean
  public Queue waterLoggingQueue() {
    return QueueBuilder.durable(WATER_LOGGING_QUEUE)
        .withArgument("x-dead-letter-exchange", DLX_NAME)
        .withArgument("x-dead-letter-routing-key", WATER_LOGGING_DLQ_ROUTING_KEY)
        .build();
  }

  @Bean
  public Queue waterLoggingDlq() {
    return QueueBuilder.durable(WATER_LOGGING_DLQ).build();
  }

  @Bean
  public Binding waterLoggingBinding(Queue waterLoggingQueue, TopicExchange healthcareExchange) {
    return BindingBuilder.bind(waterLoggingQueue).to(healthcareExchange).with(WATER_LOGGING_ROUTING_KEY);
  }

  @Bean
  public Binding waterLoggingDlqBinding(Queue waterLoggingDlq, TopicExchange deadLetterExchange) {
    return BindingBuilder.bind(waterLoggingDlq).to(deadLetterExchange).with(WATER_LOGGING_DLQ_ROUTING_KEY);
  }

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }
}
