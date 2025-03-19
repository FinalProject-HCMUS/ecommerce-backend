package com.hcmus.ecommerce_backend.common.controller;

import com.hcmus.ecommerce_backend.common.service.DatabaseConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class DatabaseTestController {

    private final DatabaseConnectionService databaseConnectionService;

    @Autowired
    public DatabaseTestController(DatabaseConnectionService databaseConnectionService) {
        this.databaseConnectionService = databaseConnectionService;
    }

    @GetMapping("/db-test")
    public ResponseEntity<Map<String, Object>> testDatabaseConnection() {
        Map<String, Object> result = databaseConnectionService.testConnection();
        boolean isConnected = (boolean) result.get("connected");
        
        if (isConnected) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(500).body(result);
        }
    }
}