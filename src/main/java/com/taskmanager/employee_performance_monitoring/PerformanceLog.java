package com.taskmanager.employee_performance_monitoring.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "performance_logs")
@Data
public class PerformanceLog {

    @Id
    private String id;

    private String userId;
    private double score;

    private double completionRate;
    private double onTimeRate;
    private double qualityScore;

    private LocalDateTime createdAt;
}