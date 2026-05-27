package com.getech.energy.platformbasic.catalog;

import java.util.List;
import java.util.Map;

public record ResourceDefinition(
        String key,
        String fromClause,
        String selectColumns,
        String fixedCondition,
        List<String> searchColumns,
        Map<String, String> filterColumns,
        boolean tenantScoped,
        String tenantColumn,
        String defaultSort
) {
}
