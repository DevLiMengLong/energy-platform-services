package com.getech.energy.platformbasic.auth;

import com.getech.energy.platformbasic.common.ApiException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private final String secret;

    public TokenService(@Value("${app.auth.token-secret}") String secret) {
        this.secret = secret;
    }

    public String create(CurrentUser user) {
        String payload = "%d|%s|%s|%s|%s|%d".formatted(
                user.userId(),
                user.tenantId() == null ? "" : user.tenantId().toString(),
                user.account(),
                user.username(),
                user.roleType(),
                Instant.now().getEpochSecond());
        String signature = sign(payload);
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString((payload + "|" + signature).getBytes(StandardCharsets.UTF_8));
    }

    public CurrentUser parse(String token) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split("\\|", -1);
            if (parts.length != 7) {
                throw new ApiException("UNAUTHORIZED", "Invalid authentication token");
            }
            String payload = String.join("|", parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
            if (!MessageDigest.isEqual(sign(payload).getBytes(StandardCharsets.UTF_8), parts[6].getBytes(StandardCharsets.UTF_8))) {
                throw new ApiException("UNAUTHORIZED", "Invalid authentication token");
            }
            Long tenantId = parts[1].isBlank() ? null : Long.valueOf(parts[1]);
            return new CurrentUser(Long.valueOf(parts[0]), tenantId, parts[2], parts[3], parts[4]);
        } catch (IllegalArgumentException ex) {
            throw new ApiException("UNAUTHORIZED", "Invalid authentication token");
        }
    }

    private String sign(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((payload + "|" + secret).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
