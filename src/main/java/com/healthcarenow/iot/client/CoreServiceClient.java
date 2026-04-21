package com.healthcarenow.iot.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class CoreServiceClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.core-service.url:http://core-service:8081}")
    private String coreServiceUrl;

    @Value("${app.internal-token:hcn-internal-secret-2024}")
    private String internalToken;

    public void clearForbiddenFoods(String userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Internal-Token", internalToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String url = coreServiceUrl + "/api/v1/internal/users/" + userId + "/forbidden-foods";
            restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
            log.info("[CoreServiceClient] Cleared forbidden foods for user: {}", userId);
        } catch (Exception e) {
            log.error("[CoreServiceClient] Failed to clear forbidden foods for user: " + userId, e);
        }
    }

    public void removeForbiddenFoodsBySource(String userId, String sourceId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Internal-Token", internalToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String url = coreServiceUrl + "/api/v1/internal/users/" + userId + "/forbidden-foods/" + sourceId;
            restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
            log.info("[CoreServiceClient] Cleared forbidden foods for user {} and source {}", userId, sourceId);
        } catch (Exception e) {
            log.error("[CoreServiceClient] Failed to clear forbidden foods for user {} and source {}", userId, sourceId, e);
        }
    }
}
