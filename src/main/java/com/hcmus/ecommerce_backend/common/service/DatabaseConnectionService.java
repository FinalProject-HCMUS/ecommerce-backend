package com.hcmus.ecommerce_backend.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DatabaseConnectionService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DatabaseConnectionService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> testConnection() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Test query
            String dbVersion = jdbcTemplate.queryForObject("SELECT version()", String.class);
            result.put("status", "success");
            result.put("connected", true);
            result.put("databaseVersion", dbVersion);
        } catch (Exception e) {
            result.put("status", "error");
            result.put("connected", false);
            result.put("message", e.getMessage());
        }
        
        return result;
    }
}