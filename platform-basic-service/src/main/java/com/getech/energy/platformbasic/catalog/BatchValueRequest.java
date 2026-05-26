package com.getech.energy.platformbasic.catalog;

import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;

public record BatchValueRequest(@NotEmpty(message = "items are required") List<Item> items) {

    public record Item(
            Long capacityCenterId,
            Long indicatorId,
            Long statNodeId,
            String dataType,
            String periodType,
            String dataTime,
            BigDecimal value,
            String unit,
            String sourceType
    ) {
    }
}
