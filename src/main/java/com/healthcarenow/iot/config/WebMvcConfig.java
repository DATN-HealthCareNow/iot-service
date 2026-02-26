package com.healthcarenow.iot.config;

import com.healthcarenow.iot.common.interceptor.UserInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  private final UserInterceptor userInterceptor;

  public WebMvcConfig(UserInterceptor userInterceptor) {
    this.userInterceptor = userInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(userInterceptor)
        .addPathPatterns("/api/v1/**"); // Apply strictly to our API paths
  }
}
