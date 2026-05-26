package com.getech.energy.platformbasic.logging;

import com.getech.energy.platformbasic.auth.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class LogClient {

    private static final Logger log = LoggerFactory.getLogger(LogClient.class);

    private final RestClient restClient;
    private final String baseUrl;

    public LogClient(RestClient logRestClient, @Value("${app.log-service.base-url}") String baseUrl) {
        this.restClient = logRestClient;
        this.baseUrl = baseUrl;
    }

    public void login(String traceId, Long tenantId, Long userId, String account, String status, String reason,
                      HttpServletRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("traceId", traceId);
        payload.put("tenantId", tenantId);
        payload.put("userId", userId);
        payload.put("account", account);
        payload.put("loginStatus", status);
        payload.put("failureReason", reason);
        payload.put("clientIp", clientIp(request));
        payload.put("userAgent", request.getHeader("User-Agent"));
        post("/api/logs/login", payload);
    }

    public void operation(String traceId, CurrentUser user, String moduleCode, String actionCode, String actionName,
                          String resourceType, String resourceId, HttpServletRequest request, boolean success,
                          String message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("traceId", traceId);
        payload.put("tenantId", user == null ? null : user.tenantId());
        payload.put("userId", user == null ? null : user.userId());
        payload.put("account", user == null ? null : user.account());
        payload.put("subsystemCode", "platform-basic");
        payload.put("moduleCode", moduleCode);
        payload.put("actionCode", actionCode);
        payload.put("actionName", actionName);
        payload.put("resourceType", resourceType);
        payload.put("resourceId", resourceId);
        payload.put("requestMethod", request.getMethod());
        payload.put("requestUri", request.getRequestURI());
        payload.put("clientIp", clientIp(request));
        payload.put("success", success);
        payload.put("message", message);
        post("/api/logs/operation", payload);
    }

    private void post(String path, Map<String, Object> payload) {
        try {
            restClient.post()
                    .uri(baseUrl + path)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException ex) {
            log.warn("Failed to call log service path={}: {}", path, ex.getMessage());
        }
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
