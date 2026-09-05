package com.taskmanager.employee_performance_monitoring.dto;

import com.taskmanager.employee_performance_monitoring.model.Warning;

import java.time.LocalDateTime;

public record WarningResponse(
        String id,
        String userId,
        int level,
        String message,
        boolean resolved,
        LocalDateTime createdAt
) {
    public static WarningResponse from(Warning w) {
        return new WarningResponse(w.getId(), w.getUserId(), w.getLevel(),
                w.getMessage(), w.isResolved(), w.getCreatedAt());
    }
}
