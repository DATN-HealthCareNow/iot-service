package com.healthcarenow.iot.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  public static final String EXCHANGE_NAME = "healthcare.events";
  public static final String WATER_LOGGING_QUEUE = "water.logging.queue";
  public static final String WATER_LOGGING_ROUTING_KEY = "water.logging.routing.key";

  @Bean
  public TopicExchange healthcareExchange() {
    return new TopicExchange(EXCHANGE_NAME);
  }

  @Bean
  public Queue waterLoggingQueue() {
    return new Queue(WATER_LOGGING_QUEUE, true);
  }

  @Bean
  public Binding waterLoggingBinding(Queue waterLoggingQueue, TopicExchange healthcareExchange) {
    return BindingBuilder.bind(waterLoggingQueue).to(healthcareExchange).with(WATER_LOGGING_ROUTING_KEY);
  }

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }
}
