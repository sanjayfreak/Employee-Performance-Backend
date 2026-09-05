package com.taskmanager.employee_performance_monitoring.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 80, message = "Name is too long")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 72, message = "Password must be 6-72 characters")
        String password,

        @NotBlank(message = "Role is required")
        @Pattern(regexp = "(?i)EMPLOYEE|ADMIN", message = "Role must be EMPLOYEE or ADMIN")
        String role,

        String department
) {}
