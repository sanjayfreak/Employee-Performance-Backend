package com.taskmanager.employee_performance_monitoring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AssignTaskRequest(

        @NotBlank(message = "Task name is required")
        @Size(max = 140, message = "Task name is too long")
        String name,

        @Size(max = 2000, message = "Description is too long")
        String description,

        @NotBlank(message = "An assignee is required")
        String assignedTo,

        @NotNull(message = "A due date is required")
        LocalDate dueDate
) {}
