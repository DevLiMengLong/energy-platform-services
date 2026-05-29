package io.github.energyiot.data.access.storage;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class JdbcTemplateRawPointOperations implements RawPointJdbcOperations {

    private final JdbcTemplate jdbcTemplate;

    public JdbcTemplateRawPointOperations(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void execute(String sql) {
        jdbcTemplate.execute(sql);
    }

    @Override
    public void batchUpdate(String sql, List<Object[]> args) {
        jdbcTemplate.batchUpdate(sql, args);
    }
}
