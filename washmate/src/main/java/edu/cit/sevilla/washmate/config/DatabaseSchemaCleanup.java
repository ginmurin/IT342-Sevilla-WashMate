package edu.cit.sevilla.washmate.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseSchemaCleanup {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void removeObsoleteColumns() {
        jdbcTemplate.execute("ALTER TABLE feedbacks DROP COLUMN IF EXISTS shop_id");
        jdbcTemplate.execute("ALTER TABLE feedbacks DROP COLUMN IF EXISTS status");
    }
}
