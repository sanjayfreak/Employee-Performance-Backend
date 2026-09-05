package com.taskmanager.employee_performance_monitoring.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * A snapshot of a user's score. Written only when something actually
 * changes (a task is approved or rejected) — never on a page load.
 */
@Document(collection = "performance_logs")
@Data
public class PerformanceLog {

    @Id
    private String id;

    @Indexed
    private String userId;

    private double score;
    private double completionRate;
    private double onTimeRate;
    private double qualityScore;

    /** False when no task has been rated yet, so the UI can say so. */
    private boolean qualityRated;

    private LocalDateTime createdAt;
}
