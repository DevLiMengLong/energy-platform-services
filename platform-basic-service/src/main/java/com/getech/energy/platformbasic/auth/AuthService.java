package com.getech.energy.platformbasic.auth;

import com.getech.energy.platformbasic.common.ApiException;
import com.getech.energy.platformbasic.common.TraceContext;
import com.getech.energy.platformbasic.logging.LogClient;
import jakarta.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final JdbcClient jdbcClient;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final LogClient logClient;

    public AuthService(JdbcClient jdbcClient, PasswordEncoder passwordEncoder, TokenService tokenService, LogClient logClient) {
        this.jdbcClient = jdbcClient;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.logClient = logClient;
    }

    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String traceId = TraceContext.getTraceId(httpRequest);
        List<Map<String, Object>> users = jdbcClient.sql("""
                        SELECT u.id, u.tenant_id, u.org_id, u.account, u.username, u.phone, u.email,
                               u.password_hash, u.role_type, u.status, t.tenant_mark, t.tenant_name
                        FROM basic_user u
                        LEFT JOIN basic_tenant t ON t.id = u.tenant_id
                        WHERE u.account = :account
                        """)
                .param("account", request.account())
                .query()
                .listOfRows();
        Map<String, Object> user = users.isEmpty() ? null : users.get(0);
        if (user == null || !"ENABLED".equals(user.get("status"))) {
            logClient.login(traceId, null, null, request.account(), "FAILED", "account disabled or not found", httpRequest);
            throw new ApiException("AUTH_FAILED", "Invalid account or password");
        }
        Long tenantId = longOrNull(user.get("tenant_id"));
        Long userId = ((Number) user.get("id")).longValue();
        if (!passwordEncoder.matches(request.password(), String.valueOf(user.get("password_hash")))) {
            logClient.login(traceId, tenantId, userId, request.account(), "FAILED", "password mismatch", httpRequest);
            throw new ApiException("AUTH_FAILED", "Invalid account or password");
        }
        jdbcClient.sql("UPDATE basic_user SET last_login_at = :lastLoginAt WHERE id = :id")
                .param("lastLoginAt", Timestamp.valueOf(LocalDateTime.now()))
                .param("id", userId)
                .update();
        CurrentUser currentUser = new CurrentUser(userId, tenantId, String.valueOf(user.get("account")),
                String.valueOf(user.get("username")), String.valueOf(user.get("role_type")));
        logClient.login(traceId, tenantId, userId, request.account(), "SUCCESS", null, httpRequest);
        Map<String, Object> responseUser = new LinkedHashMap<>(user);
        responseUser.remove("password_hash");
        responseUser.put("last_login_at", LocalDateTime.now().toString());
        return new LoginResponse(tokenService.create(currentUser), responseUser);
    }

    public Map<String, Object> currentUser(CurrentUser currentUser) {
        return jdbcClient.sql("""
                        SELECT u.id, u.tenant_id, u.org_id, u.account, u.username, u.phone, u.email,
                               u.role_type, u.status, t.tenant_mark, t.tenant_name, o.org_name
                        FROM basic_user u
                        LEFT JOIN basic_tenant t ON t.id = u.tenant_id
                        LEFT JOIN basic_org_node o ON o.id = u.org_id
                        WHERE u.id = :id
                        """)
                .param("id", currentUser.userId())
                .query()
                .singleRow();
    }

    private Long longOrNull(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }
}
