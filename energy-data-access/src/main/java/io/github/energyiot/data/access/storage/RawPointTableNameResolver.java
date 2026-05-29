package io.github.energyiot.data.access.storage;

import org.springframework.util.StringUtils;

import java.util.Locale;

public class RawPointTableNameResolver {

    private final String tablePrefix;

    public RawPointTableNameResolver(String tablePrefix) {
        if (!StringUtils.hasText(tablePrefix)) {
            throw new IllegalArgumentException("tablePrefix must not be blank");
        }
        this.tablePrefix = normalize(tablePrefix);
    }

    public String resolve(String tenantMark, String modelMark) {
        if (!StringUtils.hasText(tenantMark)) {
            throw new IllegalArgumentException("tenantMark must not be blank");
        }
        if (!StringUtils.hasText(modelMark)) {
            throw new IllegalArgumentException("modelMark must not be blank");
        }
        return tablePrefix + "_" + normalize(tenantMark) + "_" + normalize(modelMark);
    }

    private static String normalize(String value) {
        String lower = value.trim().toLowerCase(Locale.ROOT);
        String normalized = lower.replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+", "")
                .replaceAll("_+$", "");
        if (!StringUtils.hasText(normalized)) {
            throw new IllegalArgumentException("value contains no safe table name characters");
        }
        return normalized;
    }
}
