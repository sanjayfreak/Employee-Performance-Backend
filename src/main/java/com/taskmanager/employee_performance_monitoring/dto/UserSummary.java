package com.taskmanager.employee_performance_monitoring.dto;

/** One row of the admin's employee table. */
public record UserSummary(
        String id,
        String name,
        String email,
        String department,
        double score,
        boolean qualityRated
) {}
