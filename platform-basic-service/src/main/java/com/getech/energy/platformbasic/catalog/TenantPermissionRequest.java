package com.getech.energy.platformbasic.catalog;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record TenantPermissionRequest(
        @NotNull(message = "tenant id is required") Long tenantId,
        List<Long> subsystemIds,
        List<Long> menuIds
) {
}
