package com.kindtail.adoptmate.batch;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DummyDataGenerator {

    private final JdbcTemplate jdbcTemplate;

    public DummyDataGenerator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertDummyAdoptions(int totalCount) {
        String sql = "INSERT INTO adoption (status, housing_type, phone, has_pet, reason, version, is_deleted, created_at, updated_at) " +
                     "VALUES (?, ?, '010-0000-0000', '없음', '테스트 사유', 0, false, NOW(), NOW())";
        int batchSize = 10000;
        List<Object[]> batchArgs = new ArrayList<>();

        for (int i = 1; i <= totalCount; i++) {
            batchArgs.add(new Object[]{"PENDING", "APARTMENT"});
            if (i % batchSize == 0 || i == totalCount) {
                jdbcTemplate.batchUpdate(sql, batchArgs);
                batchArgs.clear();
            }
        }
    }
}
