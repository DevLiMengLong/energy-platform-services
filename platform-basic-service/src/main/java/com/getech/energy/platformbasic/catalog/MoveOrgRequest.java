package com.getech.energy.platformbasic.catalog;

import jakarta.validation.constraints.NotNull;

public record MoveOrgRequest(
        @NotNull(message = "org id is required") Long orgId,
        Long parentId,
        Integer sortOrder
) {
}
