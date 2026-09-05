package com.taskmanager.employee_performance_monitoring.controller;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Public health check — confirms the app is up and the database answers. */
@RestController
@RequestMapping("/health")
public class HealthController {

    private final MongoTemplate mongoTemplate;

    public HealthController(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        try {
            int collections = mongoTemplate.getCollectionNames().size();
            return ResponseEntity.ok(Map.of(
                    "status", "UP",
                    "database", "connected",
                    "collections", collections));
        } catch (Exception e) {
            return ResponseEntity.status(503).body(Map.of(
                    "status", "DEGRADED",
                    "database", "unreachable",
                    "error", String.valueOf(e.getMessage())));
        }
    }
}
