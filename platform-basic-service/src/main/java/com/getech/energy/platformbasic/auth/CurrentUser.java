package com.getech.energy.platformbasic.auth;

public record CurrentUser(
        Long userId,
        Long tenantId,
        String account,
        String username,
        String roleType
) {

    public boolean platformAdmin() {
        return "PLATFORM_ADMIN".equals(roleType);
    }

    public boolean tenantUser() {
        return tenantId != null;
    }
}
