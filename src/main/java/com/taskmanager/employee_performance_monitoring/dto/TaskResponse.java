package com.taskmanager.employee_performance_monitoring.dto;

import com.taskmanager.employee_performance_monitoring.model.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(
        String id,
        String name,
        String description,
        String assignedTo,
        LocalDate dueDate,
        String status,
        int qualityRating,
        LocalDateTime createdAt,
        LocalDateTime completedAt,
        String proofLink,
        String proofDescription,
        String adminComment
) {
    public static TaskResponse from(Task t) {
        return new TaskResponse(
                t.getId(), t.getName(), t.getDescription(), t.getAssignedTo(),
                t.getDueDate(), t.getStatus(), t.getQualityRating(),
                t.getCreatedAt(), t.getCompletedAt(),
                t.getProofLink(), t.getProofDescription(), t.getAdminComment()
        );
    }
}
