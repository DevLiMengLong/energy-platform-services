package com.getech.energy.platformbasic.catalog;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record CatalogActionRequest(
        @NotBlank(message = "module code is required") String moduleCode,
        @NotBlank(message = "action code is required") String actionCode,
        @NotBlank(message = "action name is required") String actionName,
        Long targetId,
        String targetName,
        Map<String, Object> payload
) {
}
