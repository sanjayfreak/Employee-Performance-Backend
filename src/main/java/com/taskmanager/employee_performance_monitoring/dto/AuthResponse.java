package com.taskmanager.employee_performance_monitoring.dto;

/** What the client stores after signing in. Never contains a password. */
public record AuthResponse(
        String token,
        String userId,
        String name,
        String email,
        String role
) {}
