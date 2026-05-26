package com.getech.energy.platformbasic.auth;

import java.util.Map;

public record LoginResponse(String token, Map<String, Object> user) {
}
