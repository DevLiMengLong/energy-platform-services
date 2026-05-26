package com.getech.energy.platformbasic.catalog;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
public class CodeGenerator {

    private static final DateTimeFormatter DAY = DateTimeFormatter.BASIC_ISO_DATE;

    private final JdbcClient jdbcClient;

    public CodeGenerator(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public String next(String prefix, String tableName, String codeColumn, Long tenantId) {
        String dayPrefix = prefix + "_" + LocalDate.now().format(DAY);
        String sql = "SELECT COUNT(1) FROM " + tableName + " WHERE " + codeColumn + " LIKE :prefix";
        long count;
        if (tenantId == null) {
            count = jdbcClient.sql(sql)
                    .param("prefix", dayPrefix + "%")
                    .query(Long.class)
                    .single();
        } else {
            count = jdbcClient.sql(sql + " AND tenant_id = :tenantId")
                    .param("prefix", dayPrefix + "%")
                    .param("tenantId", tenantId)
                    .query(Long.class)
                    .single();
        }
        return dayPrefix + String.format("%04d", count + 1);
    }
}
