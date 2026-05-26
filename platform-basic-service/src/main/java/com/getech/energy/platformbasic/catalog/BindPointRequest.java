package com.getech.energy.platformbasic.catalog;

import jakarta.validation.constraints.NotNull;

public record BindPointRequest(
        @NotNull(message = "collection point id is required") Long collectionPointId
) {
}
