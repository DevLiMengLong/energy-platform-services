package com.getech.energy.platformbasic.catalog;

import java.util.List;

public record ResourceDefinition(
        String key,
        String fromClause,
        String selectColumns,
        String fixedCondition,
        List<String> searchColumns,
        boolean tenantScoped,
        String tenantColumn,
        String defaultSort
) {
}
