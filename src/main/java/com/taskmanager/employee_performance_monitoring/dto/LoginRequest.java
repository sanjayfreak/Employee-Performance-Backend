package com.taskmanager.employee_performance_monitoring.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Password is required")
        String password,

        /** Optional. When sent, the account must have this role. */
        String role
) {}
