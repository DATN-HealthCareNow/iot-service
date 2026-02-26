package com.healthcarenow.iot.common.interceptor;

import com.healthcarenow.iot.common.context.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UserInterceptor implements HandlerInterceptor {

  private static final String USER_ID_HEADER = "X-User-Id";

  @Override
  public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
      @NonNull Object handler) {
    String userId = request.getHeader(USER_ID_HEADER);
    if (userId != null && !userId.isEmpty()) {
      UserContextHolder.setUserId(userId);
    }
    // Always return true to let the request pass through. If validation is strict,
    // we could throw Unauthorized here.
    // Assuming API Gateway has already validated the token, so missing header might
    // just be public endpoints.
    return true;
  }

  @Override
  public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
      @NonNull Object handler, Exception ex) {
    UserContextHolder.clear();
  }
}
